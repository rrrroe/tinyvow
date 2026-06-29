package com.rrrrz.tinyvow.data.media

import android.content.Context
import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.db.MediaAppConfigEntity
import com.rrrrz.tinyvow.data.db.MediaAppPlaybackDayEntity
import com.rrrrz.tinyvow.data.db.MediaAppPlaybackSegmentEntity
import com.rrrrz.tinyvow.data.db.MediaAppPlaybackStatus
import com.rrrrz.tinyvow.data.repository.ArchiveDateUtils
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.time.BusinessDay
import com.rrrrz.tinyvow.data.usage.SpecialUsageOverride
import com.rrrrz.tinyvow.data.usage.UsageStatsUsageRepository
import com.rrrrz.tinyvow.data.usage.usagePeriodBounds
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class MediaAppSettingsRow(
    val config: MediaAppConfigEntity,
    val todayTrustedPlaybackMillis: Long,
    val todayForegroundUsageMillis: Long,
    val todayUntrustedGapMillis: Long,
    val isPlaying: Boolean,
    val lastStatus: MediaAppPlaybackStatus,
    val lastConfirmedAt: Long?,
)

class MediaAppPlaybackRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getDatabase(context),
    private val baseUsageRepository: UsageStatsUsageRepository = UsageStatsUsageRepository(context),
) : SpecialUsageOverride {
    private val zoneId = ZoneId.systemDefault()
    private val configDao = database.mediaAppConfigDao()
    private val dayDao = database.mediaAppPlaybackDayDao()
    private val segmentDao = database.mediaAppPlaybackSegmentDao()
    private val preferences = ManagedAppPreferences(context)

    suspend fun getConfigs(): List<MediaAppConfigEntity> =
        withContext(Dispatchers.IO) {
            configDao.getAll()
        }

    suspend fun getEnabledPackageNames(): List<String> =
        withContext(Dispatchers.IO) {
            configDao.getEnabledPackageNames()
        }

    suspend fun addOrEnableApp(app: ManagedApp) {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val existing = configDao.get(app.packageName)
            configDao.upsert(
                MediaAppConfigEntity(
                    packageName = app.packageName,
                    appLabelSnapshot = app.appName,
                    enabled = true,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now,
                )
            )
        }
    }

    suspend fun removeApp(packageName: String) {
        withContext(Dispatchers.IO) {
            configDao.disable(packageName, System.currentTimeMillis())
        }
    }

    suspend fun buildSettingsRows(): List<MediaAppSettingsRow> =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val dayStartHour = preferences.getDayBoundaryHourOnce()
            val today = businessDate(now, dayStartHour)
            val dayStart = ArchiveDateUtils.startOfDayMillis(LocalDate.parse(today), zoneId, dayStartHour)
            val configs = configDao.getAll().filter { it.enabled }
            val dayRows = dayDao.getByDate(today).associateBy { it.packageName }
            configs.map { config ->
                val foreground = baseUsageRepository.getUsageMillis(config.packageName, dayStart, now)
                val day = dayRows[config.packageName]
                MediaAppSettingsRow(
                    config = config,
                    todayTrustedPlaybackMillis = day?.trustedPlaybackMillis ?: 0L,
                    todayForegroundUsageMillis = foreground,
                    todayUntrustedGapMillis = day?.untrustedGapMillis ?: 0L,
                    isPlaying = day?.isPlaying == true,
                    lastStatus = day?.lastStatus ?: MediaAppPlaybackStatus.UNKNOWN,
                    lastConfirmedAt = day?.lastConfirmedAt,
                )
            }
        }

    suspend fun recordPlaybackStatus(
        packageName: String,
        status: MediaAppPlaybackStatus,
        nowMillis: Long = System.currentTimeMillis(),
        countGapSinceLastConfirmation: Boolean,
    ) {
        withContext(Dispatchers.IO) {
            if (configDao.get(packageName)?.enabled != true) return@withContext
            val date = businessDate(nowMillis, preferences.getDayBoundaryHourOnce())
            val existing = dayDao.get(packageName, date)
            val current = existing?.toAccountingState()
                ?: MediaPlaybackAccountingState(
                    trustedPlaybackMillis = 0L,
                    untrustedGapMillis = 0L,
                    isPlaying = false,
                    activeStartedAt = null,
                    lastConfirmedAt = null,
                    lastStatus = MediaAppPlaybackStatus.UNKNOWN,
                )
            val trustedInterval =
                MediaAppPlaybackAccountant.trustedIntervalForStatus(
                    current = current,
                    status = status,
                    nowMillis = nowMillis,
                    countGapSinceLastConfirmation = countGapSinceLastConfirmation,
                )
            val next =
                MediaAppPlaybackAccountant.applyStatus(
                    current = current,
                    status = status,
                    nowMillis = nowMillis,
                    countGapSinceLastConfirmation = countGapSinceLastConfirmation,
                )
            dayDao.upsert(
                MediaAppPlaybackDayEntity(
                    packageName = packageName,
                    playbackDate = date,
                    trustedPlaybackMillis = next.trustedPlaybackMillis,
                    untrustedGapMillis = next.untrustedGapMillis,
                    isPlaying = next.isPlaying,
                    activeStartedAt = next.activeStartedAt,
                    lastConfirmedAt = next.lastConfirmedAt,
                    lastStatus = next.lastStatus,
                    updatedAt = nowMillis,
                )
            )
            if (trustedInterval != null) {
                recordTrustedPlaybackInterval(packageName, trustedInterval, nowMillis)
            }
        }
    }

    suspend fun markListenerDisconnected(nowMillis: Long = System.currentTimeMillis()) {
        withContext(Dispatchers.IO) {
            val packages = configDao.getEnabledPackageNames()
            packages.forEach { packageName ->
                recordPlaybackStatus(
                    packageName = packageName,
                    status = MediaAppPlaybackStatus.UNKNOWN,
                    nowMillis = nowMillis,
                    countGapSinceLastConfirmation = true,
                )
            }
        }
    }

    override suspend fun isReplacementEnabled(groupType: GroupType?): Boolean =
        getEnabledPackageNames().isNotEmpty()

    override suspend fun replacementPackageNames(groupType: GroupType?): Set<String> =
        getEnabledPackageNames().toSet()

    override suspend fun replacementUsageMillis(
        packageName: String,
        startMillis: Long,
        endMillis: Long,
        groupType: GroupType?,
    ): Long? {
        if (endMillis <= startMillis) return 0L
        val config = withContext(Dispatchers.IO) { configDao.get(packageName) } ?: return null
        if (!config.enabled) return null
        val foregroundSessions =
            baseUsageRepository.getUsageSessions(startMillis, endMillis)
                .filter { it.packageName == packageName }
        val playbackSegments =
            withContext(Dispatchers.IO) {
                segmentDao.getOverlapping(packageName, startMillis, endMillis)
            }
        val mergedUsageMillis =
            mergeUsageIntervals(
                intervals = foregroundSessions.map { UsageInterval(it.startTime, it.endTime) } +
                    playbackSegments.map { UsageInterval(it.startMillis, it.endMillis) },
                rangeStart = startMillis,
                rangeEnd = endMillis,
            )
        val legacyPlaybackMillis = legacyPlaybackMillis(packageName, startMillis, endMillis)
        return maxOf(mergedUsageMillis, legacyPlaybackMillis)
    }

    private suspend fun recordTrustedPlaybackInterval(
        packageName: String,
        interval: MediaPlaybackTrustedInterval,
        nowMillis: Long,
    ) {
        if (interval.endMillis <= interval.startMillis) return
        val previous = segmentDao.getLatestEndingAt(packageName, interval.startMillis)
        if (previous != null) {
            segmentDao.updateEnd(
                id = previous.id,
                endMillis = interval.endMillis,
                updatedAt = nowMillis,
            )
        } else {
            val dayStartHour = preferences.getDayBoundaryHourOnce()
            segmentDao.insert(
                MediaAppPlaybackSegmentEntity(
                    packageName = packageName,
                    playbackDate = ArchiveDateUtils.formatDate(
                        ArchiveDateUtils.localDateAt(interval.startMillis, zoneId, dayStartHour),
                    ),
                    startMillis = interval.startMillis,
                    endMillis = interval.endMillis,
                    createdAt = nowMillis,
                    updatedAt = nowMillis,
                ),
            )
        }
    }

    private suspend fun legacyPlaybackMillis(
        packageName: String,
        startMillis: Long,
        endMillis: Long,
    ): Long {
        val dayStartHour = preferences.getDayBoundaryHourOnce()
        val startDate = ArchiveDateUtils.localDateAt(startMillis, zoneId, dayStartHour)
        val endDate = ArchiveDateUtils.localDateAt((endMillis - 1).coerceAtLeast(startMillis), zoneId, dayStartHour)
        return withContext(Dispatchers.IO) {
            dayDao.getByPackageAndDateRange(
                packageName = packageName,
                fromDate = ArchiveDateUtils.formatDate(startDate),
                toDate = ArchiveDateUtils.formatDate(endDate),
            ).sumOf { it.trustedPlaybackMillis }
        }
    }

    override suspend fun getUsageInPeriod(period: LimitPeriod): Long {
        val dayStartHour = preferences.getDayBoundaryHourOnce()
        val now = System.currentTimeMillis()
        val bounds = usagePeriodBounds(
            period = period,
            zoneId = zoneId,
            currentDate = BusinessDay.today(zoneId, dayStartHour, now),
            nowMillis = now,
            dayStartHour = dayStartHour,
        )
        return replacementPackageNames(null).sumOf { packageName ->
            replacementUsageMillis(packageName, bounds.startMillis, bounds.endMillis, null) ?: 0L
        }
    }

    private fun MediaAppPlaybackDayEntity.toAccountingState(): MediaPlaybackAccountingState =
        MediaPlaybackAccountingState(
            trustedPlaybackMillis = trustedPlaybackMillis,
            untrustedGapMillis = untrustedGapMillis,
            isPlaying = isPlaying,
            activeStartedAt = activeStartedAt,
            lastConfirmedAt = lastConfirmedAt,
            lastStatus = lastStatus,
        )

    private fun businessDate(nowMillis: Long, dayStartHour: Int): String =
        BusinessDay.today(zoneId, dayStartHour, nowMillis).toString()
}

internal data class UsageInterval(
    val startMillis: Long,
    val endMillis: Long,
)

internal fun mergeUsageIntervals(
    intervals: List<UsageInterval>,
    rangeStart: Long,
    rangeEnd: Long,
): Long {
    if (rangeEnd <= rangeStart) return 0L
    val clipped =
        intervals
            .mapNotNull { interval ->
                val start = maxOf(interval.startMillis, rangeStart)
                val end = minOf(interval.endMillis, rangeEnd)
                if (end > start) UsageInterval(start, end) else null
            }
            .sortedBy { it.startMillis }
    var total = 0L
    var currentStart: Long? = null
    var currentEnd = 0L
    clipped.forEach { interval ->
        val start = currentStart
        if (start == null) {
            currentStart = interval.startMillis
            currentEnd = interval.endMillis
        } else if (interval.startMillis <= currentEnd) {
            currentEnd = maxOf(currentEnd, interval.endMillis)
        } else {
            total += currentEnd - start
            currentStart = interval.startMillis
            currentEnd = interval.endMillis
        }
    }
    val start = currentStart
    if (start != null) {
        total += currentEnd - start
    }
    return total
}
