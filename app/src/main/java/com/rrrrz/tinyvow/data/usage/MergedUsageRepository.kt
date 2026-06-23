package com.rrrrz.tinyvow.data.usage

import android.content.Context
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.special.SpecialAppUsageRepository
import com.rrrrz.tinyvow.data.special.WEREAD_PACKAGE_NAME
import com.rrrrz.tinyvow.data.time.BusinessDay
import java.time.ZoneId

interface SpecialUsageOverride {
    suspend fun isReplacementEnabled(groupType: GroupType?): Boolean
    suspend fun replacementUsageMillis(
        packageName: String,
        startMillis: Long,
        endMillis: Long,
        groupType: GroupType?,
    ): Long?
    suspend fun getUsageInPeriod(period: LimitPeriod): Long
}

class MergedUsageRepository(
    private val baseRepository: UsageRepository,
    private val specialRepository: SpecialUsageOverride,
) : UsageRepository {
    constructor(context: Context) : this(
        baseRepository = UsageStatsUsageRepository(context),
        specialRepository = SpecialAppUsageRepository(context),
    )

    override suspend fun getTodayUsageMillis(packageName: String): Long =
        getUsageInPeriod(packageName, LimitPeriod.DAILY)

    override suspend fun getUsageInPeriod(packageName: String, period: LimitPeriod): Long =
        getUsageStatsInPeriod(period)[packageName] ?: 0L

    suspend fun getUsageInPeriod(
        packageName: String,
        period: LimitPeriod,
        groupType: GroupType?,
    ): Long = getUsageStatsInPeriod(period, groupType)[packageName] ?: 0L

    override suspend fun getUsageStatsInPeriod(period: LimitPeriod): Map<String, Long> =
        getUsageStatsInPeriod(period, null)

    override suspend fun getUsageStatsInPeriod(period: LimitPeriod, groupType: GroupType?): Map<String, Long> {
        val base = baseRepository.getUsageStatsInPeriod(period).toMutableMap()
        if (specialRepository.isReplacementEnabled(groupType)) {
            val bounds = usagePeriodBounds(period)
            val replacement =
                specialRepository.replacementUsageMillis(
                    packageName = WEREAD_PACKAGE_NAME,
                    startMillis = bounds.startMillis,
                    endMillis = bounds.endMillis,
                    groupType = groupType,
                )
            if (replacement != null) {
                base[WEREAD_PACKAGE_NAME] = replacement
            }
        }
        return base
    }

    override suspend fun getYesterdayUsageMillis(packageName: String): Long {
        val zoneId = ZoneId.systemDefault()
        val dayStartHour = BusinessDay.cachedStartHour()
        val yesterday = BusinessDay.today(zoneId, dayStartHour).minusDays(1)
        val yesterdayStart = BusinessDay.startOfDayMillis(yesterday, zoneId, dayStartHour)
        val todayStart = BusinessDay.nextDayStartMillis(yesterday, zoneId, dayStartHour)
        return getUsageStats(yesterdayStart, todayStart, null)[packageName] ?: 0L
    }

    override suspend fun getUsageMillis(packageName: String, startMillis: Long, endMillis: Long): Long =
        getUsageStats(startMillis, endMillis)[packageName] ?: 0L

    suspend fun getUsageMillis(
        packageName: String,
        startMillis: Long,
        endMillis: Long,
        groupType: GroupType?,
    ): Long = getUsageStats(startMillis, endMillis, groupType)[packageName] ?: 0L

    override suspend fun getUsageStats(startMillis: Long, endMillis: Long): Map<String, Long> =
        getUsageStats(startMillis, endMillis, null)

    override suspend fun getUsageStats(
        startMillis: Long,
        endMillis: Long,
        groupType: GroupType?,
    ): Map<String, Long> {
        val base = baseRepository.getUsageStats(startMillis, endMillis).toMutableMap()
        if (specialRepository.isReplacementEnabled(groupType)) {
            val replacement =
                specialRepository.replacementUsageMillis(
                    packageName = WEREAD_PACKAGE_NAME,
                    startMillis = startMillis,
                    endMillis = endMillis,
                    groupType = groupType,
                )
            if (replacement != null) {
                base[WEREAD_PACKAGE_NAME] = replacement
            }
        }
        return base
    }

    override suspend fun getUsageSessions(startMillis: Long, endMillis: Long): List<AppSession> =
        baseRepository.getUsageSessions(startMillis, endMillis)

    override suspend fun getAppOpenCount(startMillis: Long, endMillis: Long): Map<String, Int> =
        baseRepository.getAppOpenCount(startMillis, endMillis)
}
