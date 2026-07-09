package com.rrrrz.tinyvow.data.lockscreen

import android.content.Context
import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.db.LockScreenTimerAppConfigEntity
import com.rrrrz.tinyvow.data.db.LockScreenTimerAppDayEntity
import com.rrrrz.tinyvow.data.db.LockScreenTimerAppSegmentEntity
import com.rrrrz.tinyvow.data.db.LockScreenTimerAppStatus
import com.rrrrz.tinyvow.data.media.UsageInterval
import com.rrrrz.tinyvow.data.media.mergeUsageIntervals
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

data class LockScreenTimerAppSettingsRow(
    val config: LockScreenTimerAppConfigEntity,
    val todayLockScreenMillis: Long,
    val todayForegroundUsageMillis: Long,
    val isActive: Boolean,
    val activeStartedAt: Long?,
    val lastStatus: LockScreenTimerAppStatus,
)

class LockScreenTimerAppRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getDatabase(context),
    private val baseUsageRepository: UsageStatsUsageRepository = UsageStatsUsageRepository(context),
) : SpecialUsageOverride {
    private val zoneId = ZoneId.systemDefault()
    private val configDao = database.lockScreenTimerAppConfigDao()
    private val dayDao = database.lockScreenTimerAppDayDao()
    private val segmentDao = database.lockScreenTimerAppSegmentDao()
    private val preferences = ManagedAppPreferences(context)

    suspend fun getConfigs(): List<LockScreenTimerAppConfigEntity> =
        withContext(Dispatchers.IO) {
            configDao.getAll()
        }

    suspend fun getEnabledPackageNames(): List<String> =
        withContext(Dispatchers.IO) {
            configDao.getEnabledPackageNames()
        }

    suspend fun isPackageEnabled(packageName: String): Boolean =
        withContext(Dispatchers.IO) {
            configDao.get(packageName)?.enabled == true
        }

    suspend fun addOrEnableApp(app: ManagedApp) {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val existing = configDao.get(app.packageName)
            configDao.upsert(
                LockScreenTimerAppConfigEntity(
                    packageName = app.packageName,
                    appLabelSnapshot = app.appName,
                    enabled = true,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now,
                ),
            )
        }
    }

    suspend fun removeApp(packageName: String) {
        withContext(Dispatchers.IO) {
            configDao.disable(packageName, System.currentTimeMillis())
        }
    }

    suspend fun buildSettingsRows(): List<LockScreenTimerAppSettingsRow> =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val dayStartHour = preferences.getDayBoundaryHourOnce()
            val today = businessDate(now, dayStartHour)
            val dayStart = ArchiveDateUtils.startOfDayMillis(LocalDate.parse(today), zoneId, dayStartHour)
            val configs = configDao.getAll().filter { it.enabled }
            val dayRows = dayDao.getByDate(today).associateBy { it.packageName }
            configs.map { config ->
                val day = dayRows[config.packageName]
                LockScreenTimerAppSettingsRow(
                    config = config,
                    todayLockScreenMillis = getLockScreenMillis(config.packageName, dayStart, now),
                    todayForegroundUsageMillis = baseUsageRepository.getUsageMillis(config.packageName, dayStart, now),
                    isActive = day?.isActive == true,
                    activeStartedAt = day?.activeStartedAt,
                    lastStatus = day?.lastStatus ?: LockScreenTimerAppStatus.UNKNOWN,
                )
            }
        }

    suspend fun startLockScreenTimer(
        packageName: String,
        nowMillis: Long = System.currentTimeMillis(),
    ) {
        withContext(Dispatchers.IO) {
            if (configDao.get(packageName)?.enabled != true) return@withContext
            val date = businessDate(nowMillis, preferences.getDayBoundaryHourOnce())
            val existing = dayDao.get(packageName, date)
            if (existing?.isActive == true) {
                dayDao.upsert(
                    existing.copy(
                        lastStatus = LockScreenTimerAppStatus.ACTIVE,
                        updatedAt = nowMillis,
                    ),
                )
                return@withContext
            }
            dayDao.upsert(
                LockScreenTimerAppDayEntity(
                    packageName = packageName,
                    timerDate = date,
                    trustedLockMillis = existing?.trustedLockMillis ?: 0L,
                    isActive = true,
                    activeStartedAt = nowMillis,
                    lastStatus = LockScreenTimerAppStatus.ACTIVE,
                    updatedAt = nowMillis,
                ),
            )
        }
    }

    suspend fun stopLockScreenTimer(
        packageName: String,
        status: LockScreenTimerAppStatus,
        nowMillis: Long = System.currentTimeMillis(),
    ) {
        withContext(Dispatchers.IO) {
            val date = businessDate(nowMillis, preferences.getDayBoundaryHourOnce())
            val existing = dayDao.get(packageName, date)
                ?: dayDao.getLatestForPackage(packageName)
                ?: return@withContext
            if (!existing.isActive || existing.activeStartedAt == null) {
                dayDao.upsert(
                    existing.copy(
                        isActive = false,
                        activeStartedAt = null,
                        lastStatus = status,
                        updatedAt = nowMillis,
                    ),
                )
                return@withContext
            }
            val endMillis = (existing.activeStartedAt + MAX_TRUSTED_LOCK_SESSION_MILLIS)
                .coerceAtMost(nowMillis)
                .coerceAtLeast(existing.activeStartedAt)
            val delta = endMillis - existing.activeStartedAt
            if (delta > 0L) {
                recordTrustedLockInterval(packageName, existing.activeStartedAt, endMillis, nowMillis)
            }
            dayDao.upsert(
                existing.copy(
                    trustedLockMillis = existing.trustedLockMillis + delta,
                    isActive = false,
                    activeStartedAt = null,
                    lastStatus = status,
                    updatedAt = nowMillis,
                ),
            )
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
        val lockScreenIntervals = getLockScreenIntervals(packageName, startMillis, endMillis)
        return mergeUsageIntervals(
            intervals = foregroundSessions.map { UsageInterval(it.startTime, it.endTime) } + lockScreenIntervals,
            rangeStart = startMillis,
            rangeEnd = endMillis,
        )
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

    private suspend fun getLockScreenMillis(
        packageName: String,
        startMillis: Long,
        endMillis: Long,
    ): Long =
        mergeUsageIntervals(
            intervals = getLockScreenIntervals(packageName, startMillis, endMillis),
            rangeStart = startMillis,
            rangeEnd = endMillis,
        )

    private suspend fun getLockScreenIntervals(
        packageName: String,
        startMillis: Long,
        endMillis: Long,
    ): List<UsageInterval> =
        withContext(Dispatchers.IO) {
            val stored =
                segmentDao.getOverlapping(packageName, startMillis, endMillis)
                    .map { UsageInterval(it.startMillis, it.endMillis) }
            val ongoing =
                dayDao.getLatestForPackage(packageName)
                    ?.takeIf { it.isActive && it.activeStartedAt != null }
                    ?.let { day ->
                        val cappedEnd = (day.activeStartedAt!! + MAX_TRUSTED_LOCK_SESSION_MILLIS)
                            .coerceAtMost(endMillis)
                        if (cappedEnd > startMillis && cappedEnd > day.activeStartedAt) {
                            UsageInterval(day.activeStartedAt, cappedEnd)
                        } else {
                            null
                        }
                    }
            if (ongoing != null) stored + ongoing else stored
        }

    private suspend fun recordTrustedLockInterval(
        packageName: String,
        startMillis: Long,
        endMillis: Long,
        nowMillis: Long,
    ) {
        if (endMillis <= startMillis) return
        val previous = segmentDao.getLatestEndingAt(packageName, startMillis)
        if (previous != null) {
            segmentDao.updateEnd(
                id = previous.id,
                endMillis = endMillis,
                updatedAt = nowMillis,
            )
        } else {
            val dayStartHour = preferences.getDayBoundaryHourOnce()
            segmentDao.insert(
                LockScreenTimerAppSegmentEntity(
                    packageName = packageName,
                    timerDate = ArchiveDateUtils.formatDate(
                        ArchiveDateUtils.localDateAt(startMillis, zoneId, dayStartHour),
                    ),
                    startMillis = startMillis,
                    endMillis = endMillis,
                    createdAt = nowMillis,
                    updatedAt = nowMillis,
                ),
            )
        }
    }

    private fun businessDate(nowMillis: Long, dayStartHour: Int): String =
        BusinessDay.today(zoneId, dayStartHour, nowMillis).toString()

    companion object {
        const val MAX_TRUSTED_LOCK_SESSION_MILLIS: Long = 4L * 60L * 60_000L
    }
}
