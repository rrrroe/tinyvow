package com.rrrrz.tinyvow.data.special

import android.content.Context
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.db.PointLedgerEntryType
import com.rrrrz.tinyvow.data.db.RewardType
import com.rrrrz.tinyvow.data.db.SpecialAppConfigEntity
import com.rrrrz.tinyvow.data.db.SpecialAppPointCreditEntity
import com.rrrrz.tinyvow.data.db.SpecialAppUsagePreference
import com.rrrrz.tinyvow.data.db.SpecialAppUsageSnapshotEntity
import com.rrrrz.tinyvow.data.repository.ArchiveDateUtils
import com.rrrrz.tinyvow.data.repository.PointsRepository
import com.rrrrz.tinyvow.data.repository.calculateTargetBonusPoints
import com.rrrrz.tinyvow.data.repository.calculateUsageEarnedPoints
import com.rrrrz.tinyvow.data.repository.parseRewardPayload
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.usage.SpecialUsageOverride
import com.rrrrz.tinyvow.data.usage.UsageStatsUsageRepository
import com.rrrrz.tinyvow.data.usage.usagePeriodBounds
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class SpecialAppUsageSource(
    val provider: String,
    val syncedAt: Long,
)

private enum class EffectiveSpecialAppSource {
    READING,
    PHONE,
}

private data class EffectiveSpecialAppUsage(
    val usageMillis: Long,
    val source: EffectiveSpecialAppSource?,
    val sourceTimestamp: Long,
)

data class WeReadSettingsState(
    val config: SpecialAppConfigEntity?,
    val hasApiKey: Boolean,
    val apiKeyPreview: String?,
    val todayUsageMillis: Long,
    val weekUsageMillis: Long,
    val monthUsageMillis: Long,
    val todayReadingMillis: Long,
    val weekReadingMillis: Long,
    val monthReadingMillis: Long,
    val todayPhoneUsageMillis: Long,
    val weekPhoneUsageMillis: Long,
    val monthPhoneUsageMillis: Long,
)

data class WeReadApiCheckResult(
    val mode: String,
    val targetDate: LocalDate,
    val totalUsageMillis: Long,
    val dailyBucketCount: Int,
    val targetDateUsageMillis: Long?,
    val hasTargetDateBucket: Boolean,
    val hasUsableDailyBuckets: Boolean,
)

data class WeReadSyncSummary(
    val monthsQueried: Int,
    val totalUsageMillis: Long,
    val dailyBucketCount: Int,
    val savedSnapshotCount: Int,
)

data class WeReadHistoryDay(
    val date: LocalDate,
    val readingUsageMillis: Long,
    val readingBucketAvailable: Boolean,
    val phoneUsageMillis: Long,
    val readingSyncedAt: Long,
    val phoneCollectedAt: Long,
) {
    val hasAnyHistory: Boolean = readingBucketAvailable || phoneCollectedAt > 0L
}

data class WeReadHistoryRefreshSummary(
    val fromDate: LocalDate,
    val toDate: LocalDate,
    val datesUpdated: Int,
    val readingBucketCount: Int,
    val phoneUsageUpdatedCount: Int,
)

class SpecialAppUsageRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getDatabase(context),
    private val apiClient: WeReadApiClient = WeReadApiClient(),
    private val apiKeyStore: WeReadApiKeyStore = WeReadApiKeyStore(context),
) : SpecialUsageOverride {
    private val zoneId = ZoneId.systemDefault()
    private val configDao = database.specialAppConfigDao()
    private val snapshotDao = database.specialAppUsageSnapshotDao()
    private val pointCreditDao = database.specialAppPointCreditDao()
    private val crossRefDao = database.crossRefDao()
    private val groupDao = database.appGroupDao()
    private val activeRewardEffectDao = database.activeRewardEffectDao()
    private val baseUsageRepository = UsageStatsUsageRepository(context)
    private val pointsRepository = PointsRepository(context, database)

    fun observeWeReadConfig(): Flow<SpecialAppConfigEntity?> = configDao.observe(WEREAD_PROVIDER)

    suspend fun getWeReadConfig(): SpecialAppConfigEntity = ensureWeReadConfig()

    suspend fun buildSettingsState(): WeReadSettingsState =
        withContext(Dispatchers.IO) {
            val config = ensureWeReadConfig()
            val apiKey = apiKeyStore.get()
            val today = LocalDate.now(zoneId)
            val weekStart = today.minusDays(6)
            val monthStart = today.withDayOfMonth(1)
            val todaySnapshots = getSnapshotsForDateRange(today, today)
            val weekSnapshots = getSnapshotsForDateRange(weekStart, today)
            val monthSnapshots = getSnapshotsForDateRange(monthStart, today)
            WeReadSettingsState(
                config = config,
                hasApiKey = apiKey?.isNotBlank() == true,
                apiKeyPreview = apiKey?.takeIf { it.isNotBlank() }?.let(::maskApiKey),
                todayUsageMillis = sumEffectiveUsageInRange(today, today, config.usagePreference).usageMillis,
                weekUsageMillis = sumEffectiveUsageInRange(weekStart, today, config.usagePreference).usageMillis,
                monthUsageMillis = sumEffectiveUsageInRange(monthStart, today, config.usagePreference).usageMillis,
                todayReadingMillis = sumReadingUsage(todaySnapshots),
                weekReadingMillis = sumReadingUsage(weekSnapshots),
                monthReadingMillis = sumReadingUsage(monthSnapshots),
                todayPhoneUsageMillis = sumPhoneUsage(todaySnapshots),
                weekPhoneUsageMillis = sumPhoneUsage(weekSnapshots),
                monthPhoneUsageMillis = sumPhoneUsage(monthSnapshots),
            )
        }

    suspend fun saveWeReadApiKey(apiKey: String?) {
        apiKeyStore.save(apiKey)
    }

    suspend fun clearWeReadApiKeyAndDisable() {
        withContext(Dispatchers.IO) {
            apiKeyStore.save(null)
            val now = System.currentTimeMillis()
            val current = ensureWeReadConfig()
            configDao.upsert(
                current.copy(
                    enabledForControl = false,
                    enabledForEncourage = false,
                    syncEnabled = false,
                    lastSyncAt = 0L,
                    lastSuccessAt = 0L,
                    lastError = null,
                    updatedAt = now,
                )
            )
        }
    }

    suspend fun updateWeReadConfig(
        enabledForControl: Boolean,
        enabledForEncourage: Boolean,
        syncEnabled: Boolean,
        usagePreference: SpecialAppUsagePreference? = null,
    ) {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val current = ensureWeReadConfig()
            configDao.upsert(
                current.copy(
                    enabledForControl = enabledForControl,
                    enabledForEncourage = enabledForEncourage,
                    syncEnabled = syncEnabled,
                    usagePreference = usagePreference ?: current.usagePreference,
                    updatedAt = now,
                )
            )
        }
    }

    suspend fun updateWeReadUsagePreference(usagePreference: SpecialAppUsagePreference) {
        withContext(Dispatchers.IO) {
            val current = ensureWeReadConfig()
            configDao.upsert(current.copy(usagePreference = usagePreference, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun testWeReadApi(targetDate: LocalDate = LocalDate.now(zoneId)): Result<WeReadApiCheckResult> =
        withContext(Dispatchers.IO) {
            val apiKey = apiKeyStore.get()
            if (apiKey.isNullOrBlank()) {
                return@withContext Result.failure(IllegalStateException("WeRead API key is not set"))
            }
            runCatching {
                val month = YearMonth.from(targetDate)
                val parsed = fetchMonthUsage(apiKey, month)
                WeReadApiCheckResult(
                    mode = "monthly",
                    targetDate = targetDate,
                    totalUsageMillis = parsed.totalUsageMillis,
                    dailyBucketCount = parsed.dailyUsageMillis.count { YearMonth.from(it.key) == month },
                    targetDateUsageMillis = parsed.dailyUsageMillis[targetDate],
                    hasTargetDateBucket = parsed.dailyUsageMillis.containsKey(targetDate),
                    hasUsableDailyBuckets = parsed.dailyUsageMillis.any { YearMonth.from(it.key) == month && it.value > 0L },
                )
            }
        }

    suspend fun syncWeReadNow(): Result<WeReadSyncSummary> =
        withContext(Dispatchers.IO) {
            val config = ensureWeReadConfig()
            val now = System.currentTimeMillis()
            val apiKey = apiKeyStore.get()
            if (apiKey.isNullOrBlank()) {
                val error = "WeRead API key is not set"
                configDao.updateSyncState(WEREAD_PROVIDER, now, config.lastSuccessAt, error, now)
                return@withContext Result.failure(IllegalStateException(error))
            }
            runCatching {
                val today = LocalDate.now(zoneId)
                val months = buildList {
                    add(YearMonth.from(today))
                    if (today.dayOfMonth <= 7) add(YearMonth.from(today.minusMonths(1)))
                }.distinct()
                var totalUsageMillis = 0L
                var dailyBucketCount = 0
                val snapshots = months.flatMap { month ->
                    val result = fetchMonthUsage(apiKey, month)
                    totalUsageMillis += result.totalUsageMillis
                    dailyBucketCount += result.dailyUsageMillis.count { YearMonth.from(it.key) == month }
                    buildSnapshotsForDates(
                        dates = result.dailyUsageMillis.keys.filter { YearMonth.from(it) == month },
                        readingUsage = result.dailyUsageMillis,
                        readingSyncedAt = now,
                        collectPhoneUsage = true,
                    )
                }
                if (snapshots.isNotEmpty()) {
                    snapshotDao.upsertAll(snapshots)
                }
                configDao.updateSyncState(WEREAD_PROVIDER, now, now, null, now)
                creditRemoteEncourageUsage(today, now)
                WeReadSyncSummary(
                    monthsQueried = months.size,
                    totalUsageMillis = totalUsageMillis,
                    dailyBucketCount = dailyBucketCount,
                    savedSnapshotCount = snapshots.size,
                )
            }.onFailure { error ->
                configDao.updateSyncState(
                    provider = WEREAD_PROVIDER,
                    lastSyncAt = now,
                    lastSuccessAt = config.lastSuccessAt,
                    lastError = error.message ?: error.javaClass.simpleName,
                    updatedAt = now,
                )
            }
        }

    suspend fun syncMissingWeReadHistoryUpToYesterday(
        daysBack: Long = DEFAULT_HISTORY_BACKFILL_DAYS,
    ): Result<WeReadHistoryRefreshSummary> =
        withContext(Dispatchers.IO) {
            val today = LocalDate.now(zoneId)
            val yesterday = today.minusDays(1)
            val fromDate = today.minusDays(daysBack).coerceAtLeast(LocalDate.of(2000, 1, 1))
            if (yesterday.isBefore(fromDate)) {
                return@withContext Result.success(WeReadHistoryRefreshSummary(fromDate, yesterday, 0, 0, 0))
            }
            val existing = getSnapshotsForDateRange(fromDate, yesterday).associateBy { LocalDate.parse(it.usageDate) }
            val missingDates =
                generateSequence(fromDate) { it.plusDays(1) }
                    .takeWhile { !it.isAfter(yesterday) }
                    .filter { date -> existing[date]?.sourceSyncedAt ?: 0L <= 0L }
                    .toList()
            if (missingDates.isEmpty()) {
                return@withContext Result.success(collectPhoneUsageForDates(existing.keys.toList()))
            }
            refreshWeReadHistoryForDates(missingDates)
        }

    suspend fun refreshWeReadHistoryMonth(month: YearMonth): Result<WeReadHistoryRefreshSummary> =
        withContext(Dispatchers.IO) {
            val today = LocalDate.now(zoneId)
            val startDate = month.atDay(1)
            val endDate = minOf(month.atEndOfMonth(), today)
            if (endDate.isBefore(startDate)) {
                return@withContext Result.success(WeReadHistoryRefreshSummary(startDate, endDate, 0, 0, 0))
            }
            refreshWeReadHistoryForDates(
                generateSequence(startDate) { it.plusDays(1) }
                    .takeWhile { !it.isAfter(endDate) }
                    .toList()
            )
        }

    suspend fun refreshWeReadHistoryDate(date: LocalDate): Result<WeReadHistoryRefreshSummary> =
        withContext(Dispatchers.IO) {
            refreshWeReadHistoryForDates(listOf(date))
        }

    suspend fun refreshPhoneHistoryMonth(month: YearMonth): WeReadHistoryRefreshSummary =
        withContext(Dispatchers.IO) {
            val today = LocalDate.now(zoneId)
            val startDate = month.atDay(1)
            val endDate = minOf(month.atEndOfMonth(), today)
            if (endDate.isBefore(startDate)) {
                return@withContext WeReadHistoryRefreshSummary(startDate, endDate, 0, 0, 0)
            }
            collectPhoneUsageForDates(
                generateSequence(startDate) { it.plusDays(1) }
                    .takeWhile { !it.isAfter(endDate) }
                    .toList()
            )
        }

    suspend fun getWeReadHistoryMonth(month: YearMonth): List<WeReadHistoryDay> =
        withContext(Dispatchers.IO) {
            val today = LocalDate.now(zoneId)
            val startDate = month.atDay(1)
            val endDate = minOf(month.atEndOfMonth(), today)
            val snapshots =
                if (endDate.isBefore(startDate)) {
                    emptyMap()
                } else {
                    getSnapshotsForDateRange(startDate, endDate).associateBy { LocalDate.parse(it.usageDate) }
                }
            generateSequence(startDate) { it.plusDays(1) }
                .takeWhile { !it.isAfter(month.atEndOfMonth()) }
                .map { date ->
                    val snapshot = snapshots[date]
                    WeReadHistoryDay(
                        date = date,
                        readingUsageMillis = snapshot?.usageMillis ?: 0L,
                        readingBucketAvailable = snapshot?.readingBucketAvailable == true,
                        phoneUsageMillis = snapshot?.phoneUsageMillis ?: 0L,
                        readingSyncedAt = snapshot?.sourceSyncedAt ?: 0L,
                        phoneCollectedAt = snapshot?.phoneCollectedAt ?: 0L,
                    )
                }
                .toList()
        }

    suspend fun shouldUseSyncBasedEncouragePoints(): Boolean {
        val config = configDao.get(WEREAD_PROVIDER) ?: return false
        return config.syncEnabled &&
            config.enabledForEncourage &&
            config.usagePreference == SpecialAppUsagePreference.READING_FIRST
    }

    override suspend fun isReplacementEnabled(groupType: GroupType?): Boolean {
        val config = configDao.get(WEREAD_PROVIDER) ?: return false
        if (!config.syncEnabled || config.lastSuccessAt <= 0L) return false
        return when (groupType) {
            GroupType.CONTROL -> config.enabledForControl
            GroupType.ENCOURAGE -> config.enabledForEncourage
            null -> config.enabledForControl || config.enabledForEncourage
        }
    }

    override suspend fun replacementUsageMillis(
        packageName: String,
        startMillis: Long,
        endMillis: Long,
        groupType: GroupType?,
    ): Long? {
        if (packageName != WEREAD_PACKAGE_NAME) return null
        if (!isReplacementEnabled(groupType)) return null
        val startDate = ArchiveDateUtils.localDateAt(startMillis, zoneId)
        val endDate = ArchiveDateUtils.localDateAt((endMillis - 1).coerceAtLeast(startMillis), zoneId)
        val config = configDao.get(WEREAD_PROVIDER) ?: ensureWeReadConfig()
        return sumEffectiveUsageInRange(startDate, endDate, config.usagePreference).takeIf { it.hasOverride }?.usageMillis
    }

    suspend fun usageSourceForDate(
        packageName: String,
        date: String,
        groupType: GroupType?,
    ): SpecialAppUsageSource? {
        if (packageName != WEREAD_PACKAGE_NAME || !isReplacementEnabled(groupType)) return null
        val config = configDao.get(WEREAD_PROVIDER) ?: return null
        val targetDate = LocalDate.parse(date)
        val snapshot = snapshotDao.getByDate(WEREAD_PROVIDER, date)
        val effectiveUsage =
            resolveEffectiveUsage(
                date = targetDate,
                snapshot = snapshot,
                preference = config.usagePreference,
                livePhoneUsageMillis = null,
                today = LocalDate.now(zoneId),
            )
        val provider =
            when (effectiveUsage.source) {
                EffectiveSpecialAppSource.READING -> WEREAD_PROVIDER.name
                EffectiveSpecialAppSource.PHONE -> "PHONE_USAGE"
                null -> null
            } ?: return null
        if (effectiveUsage.usageMillis <= 0L) return null
        return SpecialAppUsageSource(provider = provider, syncedAt = effectiveUsage.sourceTimestamp)
    }

    suspend fun getUsageMillis(startMillis: Long, endMillis: Long): Long {
        if (endMillis <= startMillis) return 0L
        val startDate = ArchiveDateUtils.localDateAt(startMillis, zoneId)
        val endDate = ArchiveDateUtils.localDateAt((endMillis - 1).coerceAtLeast(startMillis), zoneId)
        val config = configDao.get(WEREAD_PROVIDER) ?: ensureWeReadConfig()
        return sumEffectiveUsageInRange(startDate, endDate, config.usagePreference).usageMillis
    }

    override suspend fun getUsageInPeriod(period: LimitPeriod): Long {
        val bounds = usagePeriodBounds(period, zoneId)
        return getUsageMillis(bounds.startMillis, bounds.endMillis)
    }

    private fun fetchMonthUsage(
        apiKey: String,
        month: YearMonth,
    ): WeReadPeriodUsage {
        val baseTimeSeconds = month.atDay(1).atStartOfDay(zoneId).toEpochSecond()
        return WeReadUsageParser.parsePeriodUsage(
            apiClient.fetchReadData(apiKey, mode = "monthly", baseTimeSeconds = baseTimeSeconds),
            zoneId,
        )
    }

    private suspend fun refreshWeReadHistoryForDates(dates: List<LocalDate>): Result<WeReadHistoryRefreshSummary> {
        if (dates.isEmpty()) {
            val today = LocalDate.now(zoneId)
            return Result.success(WeReadHistoryRefreshSummary(today, today, 0, 0, 0))
        }
        val config = ensureWeReadConfig()
        val now = System.currentTimeMillis()
        val apiKey = apiKeyStore.get()
        if (apiKey.isNullOrBlank()) {
            val error = "WeRead API key is not set"
            configDao.updateSyncState(WEREAD_PROVIDER, now, config.lastSuccessAt, error, now)
            return Result.failure(IllegalStateException(error))
        }
        return runCatching {
            val requestedDates = dates.distinct().sorted()
            val readingUsageByDate = mutableMapOf<LocalDate, Long>()
            requestedDates.groupBy { YearMonth.from(it) }.forEach { (month, datesInMonth) ->
                val monthUsage = fetchMonthUsage(apiKey, month)
                datesInMonth.forEach { date ->
                    monthUsage.dailyUsageMillis[date]?.let { readingUsageByDate[date] = it }
                }
            }
            val snapshots =
                buildSnapshotsForDates(
                    dates = requestedDates,
                    readingUsage = readingUsageByDate,
                    readingSyncedAt = now,
                    collectPhoneUsage = true,
                )
            if (snapshots.isNotEmpty()) {
                snapshotDao.upsertAll(snapshots)
            }
            configDao.updateSyncState(WEREAD_PROVIDER, now, now, null, now)
            WeReadHistoryRefreshSummary(
                fromDate = requestedDates.first(),
                toDate = requestedDates.last(),
                datesUpdated = requestedDates.size,
                readingBucketCount = readingUsageByDate.size,
                phoneUsageUpdatedCount = snapshots.count { it.phoneCollectedAt > 0L },
            )
        }.onFailure { error ->
            configDao.updateSyncState(
                provider = WEREAD_PROVIDER,
                lastSyncAt = now,
                lastSuccessAt = config.lastSuccessAt,
                lastError = error.message ?: error.javaClass.simpleName,
                updatedAt = now,
            )
        }
    }

    private suspend fun collectPhoneUsageForDates(dates: List<LocalDate>): WeReadHistoryRefreshSummary {
        val now = System.currentTimeMillis()
        val snapshots =
            dates.distinct().sorted().map { date ->
                buildSnapshot(
                    date = date,
                    readingUsageMillis = null,
                    readingBucketAvailable = null,
                    readingSyncedAt = null,
                    phoneUsageMillis = readPhoneUsageForDate(date),
                    phoneCollectedAt = now,
                )
            }
        if (snapshots.isNotEmpty()) {
            snapshotDao.upsertAll(snapshots)
        }
        val first = dates.minOrNull() ?: LocalDate.now(zoneId)
        val last = dates.maxOrNull() ?: first
        return WeReadHistoryRefreshSummary(
            fromDate = first,
            toDate = last,
            datesUpdated = snapshots.size,
            readingBucketCount = 0,
            phoneUsageUpdatedCount = snapshots.size,
        )
    }

    private suspend fun buildSnapshotsForDates(
        dates: Iterable<LocalDate>,
        readingUsage: Map<LocalDate, Long>,
        readingSyncedAt: Long,
        collectPhoneUsage: Boolean,
    ): List<SpecialAppUsageSnapshotEntity> {
        val phoneCollectedAt = if (collectPhoneUsage) System.currentTimeMillis() else null
        return dates.distinct().map { date ->
            buildSnapshot(
                date = date,
                readingUsageMillis = readingUsage[date],
                readingBucketAvailable = readingUsage.containsKey(date),
                readingSyncedAt = readingSyncedAt,
                phoneUsageMillis = if (collectPhoneUsage) readPhoneUsageForDate(date) else null,
                phoneCollectedAt = phoneCollectedAt,
            )
        }
    }

    private suspend fun buildSnapshot(
        date: LocalDate,
        readingUsageMillis: Long?,
        readingBucketAvailable: Boolean?,
        readingSyncedAt: Long?,
        phoneUsageMillis: Long?,
        phoneCollectedAt: Long?,
    ): SpecialAppUsageSnapshotEntity {
        val dateString = ArchiveDateUtils.formatDate(date)
        val existing = snapshotDao.getByDate(WEREAD_PROVIDER, dateString)
        val now = System.currentTimeMillis()
        return SpecialAppUsageSnapshotEntity(
            id = "${WEREAD_PROVIDER.name}:$dateString",
            provider = WEREAD_PROVIDER,
            packageName = WEREAD_PACKAGE_NAME,
            usageDate = dateString,
            usageMillis = readingUsageMillis ?: existing?.usageMillis ?: 0L,
            readingBucketAvailable = readingBucketAvailable ?: existing?.readingBucketAvailable ?: false,
            phoneUsageMillis = phoneUsageMillis ?: existing?.phoneUsageMillis ?: 0L,
            phoneCollectedAt = phoneCollectedAt ?: existing?.phoneCollectedAt ?: 0L,
            sourceSyncedAt = readingSyncedAt ?: existing?.sourceSyncedAt ?: 0L,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
        )
    }

    private suspend fun readPhoneUsageForDate(date: LocalDate): Long {
        val startMillis = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endMillis = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return baseUsageRepository.getUsageMillis(WEREAD_PACKAGE_NAME, startMillis, minOf(endMillis, System.currentTimeMillis()))
    }

    private suspend fun ensureWeReadConfig(): SpecialAppConfigEntity {
        configDao.get(WEREAD_PROVIDER)?.let { return it }
        val now = System.currentTimeMillis()
        val config =
            SpecialAppConfigEntity(
                provider = WEREAD_PROVIDER,
                packageName = WEREAD_PACKAGE_NAME,
                createdAt = now,
                updatedAt = now,
            )
        configDao.upsert(config)
        return config
    }

    private suspend fun getUsageForDate(date: LocalDate): Long {
        val dateString = ArchiveDateUtils.formatDate(date)
        val config = configDao.get(WEREAD_PROVIDER) ?: ensureWeReadConfig()
        return resolveEffectiveUsage(
            date = date,
            snapshot = snapshotDao.getByDate(WEREAD_PROVIDER, dateString),
            preference = config.usagePreference,
            livePhoneUsageMillis = if (date == LocalDate.now(zoneId)) readPhoneUsageForDate(date) else null,
            today = LocalDate.now(zoneId),
        ).usageMillis
    }

    private suspend fun getSnapshotsForDateRange(startDate: LocalDate, endDate: LocalDate): List<SpecialAppUsageSnapshotEntity> {
        if (endDate.isBefore(startDate)) return emptyList()
        return snapshotDao.getByDateRange(
            provider = WEREAD_PROVIDER,
            from = ArchiveDateUtils.formatDate(startDate),
            to = ArchiveDateUtils.formatDate(endDate),
        )
    }

    private fun sumEffectiveUsage(
        snapshots: List<SpecialAppUsageSnapshotEntity>,
        preference: SpecialAppUsagePreference,
    ): Long = snapshots.sumOf { it.effectiveUsageMillis(preference) }

    private suspend fun sumEffectiveUsageInRange(
        startDate: LocalDate,
        endDate: LocalDate,
        preference: SpecialAppUsagePreference,
    ): EffectiveRangeUsage {
        if (endDate.isBefore(startDate)) return EffectiveRangeUsage(0L, false)
        val today = LocalDate.now(zoneId)
        val snapshotsByDate = getSnapshotsForDateRange(startDate, endDate).associateBy { LocalDate.parse(it.usageDate) }
        val livePhoneUsageToday =
            if (!today.isBefore(startDate) && !today.isAfter(endDate)) {
                readPhoneUsageForDate(today)
            } else {
                null
            }
        var total = 0L
        var hasOverride = false
        var date = startDate
        while (!date.isAfter(endDate)) {
            val effectiveUsage =
                resolveEffectiveUsage(
                    date = date,
                    snapshot = snapshotsByDate[date],
                    preference = preference,
                    livePhoneUsageMillis = if (date == today) livePhoneUsageToday else null,
                    today = today,
                )
            total += effectiveUsage.usageMillis
            hasOverride = hasOverride || effectiveUsage.source != null
            date = date.plusDays(1)
        }
        return EffectiveRangeUsage(total, hasOverride)
    }

    private fun sumReadingUsage(snapshots: List<SpecialAppUsageSnapshotEntity>): Long =
        snapshots.sumOf { if (it.readingBucketAvailable) it.usageMillis else 0L }

    private fun sumPhoneUsage(snapshots: List<SpecialAppUsageSnapshotEntity>): Long =
        snapshots.sumOf { it.phoneUsageMillis }

    private fun SpecialAppUsageSnapshotEntity.effectiveUsageMillis(preference: SpecialAppUsagePreference): Long =
        when (preference) {
            SpecialAppUsagePreference.PHONE_FIRST -> phoneUsageMillis
            SpecialAppUsagePreference.READING_FIRST -> if (readingBucketAvailable) usageMillis else phoneUsageMillis
        }

    private fun SpecialAppUsageSnapshotEntity.hasEffectiveHistory(preference: SpecialAppUsagePreference): Boolean =
        when (preference) {
            SpecialAppUsagePreference.PHONE_FIRST -> phoneCollectedAt > 0L || readingBucketAvailable
            SpecialAppUsagePreference.READING_FIRST -> readingBucketAvailable || phoneCollectedAt > 0L
        }

    private fun resolveEffectiveUsage(
        date: LocalDate,
        snapshot: SpecialAppUsageSnapshotEntity?,
        preference: SpecialAppUsagePreference,
        livePhoneUsageMillis: Long?,
        today: LocalDate,
    ): EffectiveSpecialAppUsage {
        val readingAvailable = snapshot?.readingBucketAvailable == true
        val readingUsageMillis = snapshot?.usageMillis ?: 0L
        val phoneAvailable = if (date == today) true else (snapshot?.phoneCollectedAt ?: 0L) > 0L
        val phoneUsageMillis = if (date == today) livePhoneUsageMillis ?: snapshot?.phoneUsageMillis ?: 0L else snapshot?.phoneUsageMillis ?: 0L
        val phoneTimestamp = snapshot?.phoneCollectedAt ?: 0L
        val readingTimestamp = snapshot?.sourceSyncedAt ?: 0L
        return when (preference) {
            SpecialAppUsagePreference.READING_FIRST ->
                when {
                    readingAvailable ->
                        EffectiveSpecialAppUsage(
                            usageMillis = readingUsageMillis,
                            source = EffectiveSpecialAppSource.READING,
                            sourceTimestamp = readingTimestamp,
                        )
                    phoneAvailable ->
                        EffectiveSpecialAppUsage(
                            usageMillis = phoneUsageMillis,
                            source = EffectiveSpecialAppSource.PHONE,
                            sourceTimestamp = phoneTimestamp,
                        )
                    else -> EffectiveSpecialAppUsage(0L, null, 0L)
                }
            SpecialAppUsagePreference.PHONE_FIRST ->
                when {
                    phoneAvailable ->
                        EffectiveSpecialAppUsage(
                            usageMillis = phoneUsageMillis,
                            source = EffectiveSpecialAppSource.PHONE,
                            sourceTimestamp = phoneTimestamp,
                        )
                    readingAvailable ->
                        EffectiveSpecialAppUsage(
                            usageMillis = readingUsageMillis,
                            source = EffectiveSpecialAppSource.READING,
                            sourceTimestamp = readingTimestamp,
                        )
                    else -> EffectiveSpecialAppUsage(0L, null, 0L)
                }
        }
    }

    private suspend fun creditRemoteEncourageUsage(today: LocalDate, now: Long) {
        val config = configDao.get(WEREAD_PROVIDER) ?: return
        if (!config.syncEnabled ||
            !config.enabledForEncourage ||
            config.usagePreference != SpecialAppUsagePreference.READING_FIRST
        ) {
            return
        }
        val todayString = ArchiveDateUtils.formatDate(today)
        val todayStart = ArchiveDateUtils.startOfDayMillis(today, zoneId)
        val currentRemoteUsage = getUsageForDate(today)
        if (currentRemoteUsage <= 0L) return
        val groupIds = crossRefDao.getGroupIdsForPackageSync(WEREAD_PACKAGE_NAME)
        val groups = groupDao.getGroupsByIdsSync(groupIds).filter { it.type == GroupType.ENCOURAGE }
        for (group in groups) {
            if (group.pointsPerMinute > 0.0) {
                val credit = pointCreditDao.get(WEREAD_PROVIDER, group.id, todayString)
                val creditedUsage = credit?.creditedUsageMillis ?: 0L
                val deltaUsage = (currentRemoteUsage - creditedUsage).coerceAtLeast(0L)
                if (deltaUsage >= 1_000L) {
                    val multiplier = currentEncouragePointsMultiplier(group.id, now)
                    val deltaPoints = calculateUsageEarnedPoints(deltaUsage, group.pointsPerMinute) * multiplier
                    pointsRepository.record(
                        deltaPoints = deltaPoints,
                        entryType = PointLedgerEntryType.USAGE_EARN,
                        occurredAt = now,
                        group = group,
                        sourceRefId = "special:${WEREAD_PROVIDER.name}:${group.id}:$todayString:$currentRemoteUsage",
                        note = "WeRead remote usage earn",
                    )
                    pointCreditDao.upsert(
                        SpecialAppPointCreditEntity(
                            id = "${WEREAD_PROVIDER.name}:${group.id}:$todayString",
                            provider = WEREAD_PROVIDER,
                            groupId = group.id,
                            creditDate = todayString,
                            creditedUsageMillis = currentRemoteUsage,
                            updatedAt = now,
                        )
                    )
                }
            }
            if (group.lastBonusAt < todayStart) {
                val totalTodayUsage =
                    crossRefDao.getPackageNamesForGroupSync(group.id).sumOf { packageName ->
                        if (packageName == WEREAD_PACKAGE_NAME) {
                            currentRemoteUsage
                        } else {
                            baseUsageRepository.getTodayUsageMillis(packageName)
                        }
                    }
                if (totalTodayUsage >= group.limitMinutes * 60_000L) {
                    val multiplier = currentEncouragePointsMultiplier(group.id, now)
                    pointsRepository.recordTargetBonusEarn(
                        group = group,
                        deltaPoints = calculateTargetBonusPoints(group.limitMinutes, group.pointsPerMinute) * multiplier,
                        occurredAt = now,
                    )
                    groupDao.insertGroup(group.copy(lastBonusAt = now, updatedAt = now))
                }
            }
        }
    }

    private suspend fun currentEncouragePointsMultiplier(groupId: String, now: Long): Double {
        val effect =
            activeRewardEffectDao
                .getActiveForGroup(groupId, now)
                .firstOrNull { it.effectType == RewardType.DOUBLE_POINTS_DAY }
                ?: return 1.0
        return parseRewardPayload(effect.payloadJson).pointsMultiplier.coerceAtLeast(1.0)
    }

    companion object {
        private const val DEFAULT_HISTORY_BACKFILL_DAYS = 90L
    }
}

private fun maskApiKey(apiKey: String): String {
    val normalized = WeReadApiKeyStore.normalize(apiKey)
    if (normalized.length <= 10) return "****"
    return "${normalized.take(6)}...${normalized.takeLast(4)}"
}

private data class EffectiveRangeUsage(
    val usageMillis: Long,
    val hasOverride: Boolean,
)
