package com.rrrrz.tinyvow.data.usage

import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.db.GroupType

data class AppSession(
    val packageName: String,
    val startTime: Long,
    val endTime: Long
)

interface UsageRepository {
    suspend fun getTodayUsageMillis(packageName: String): Long
    suspend fun getUsageInPeriod(packageName: String, period: LimitPeriod): Long
    suspend fun getUsageStatsInPeriod(period: LimitPeriod): Map<String, Long>
    suspend fun getUsageStatsInPeriod(period: LimitPeriod, groupType: GroupType?): Map<String, Long> =
        getUsageStatsInPeriod(period)
    suspend fun getYesterdayUsageMillis(packageName: String): Long
    suspend fun getUsageMillis(packageName: String, startMillis: Long, endMillis: Long): Long
    suspend fun getUsageStats(startMillis: Long, endMillis: Long): Map<String, Long>
    suspend fun getUsageStats(startMillis: Long, endMillis: Long, groupType: GroupType?): Map<String, Long> =
        getUsageStats(startMillis, endMillis)
    suspend fun getUsageSessions(startMillis: Long, endMillis: Long): List<AppSession>
    suspend fun getAppOpenCount(startMillis: Long, endMillis: Long): Map<String, Int>
}
