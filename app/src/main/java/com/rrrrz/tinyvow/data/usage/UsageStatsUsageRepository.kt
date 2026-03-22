package com.rrrrz.tinyvow.data.usage

import android.app.usage.UsageStatsManager
import android.content.Context
import com.rrrrz.tinyvow.data.db.LimitPeriod
import java.time.LocalDate
import java.time.ZoneId
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsageStatsUsageRepository(
    private val context: Context,
) : UsageRepository {
    override suspend fun getTodayUsageMillis(packageName: String): Long =
        getUsageInPeriod(packageName, LimitPeriod.DAILY)

    override suspend fun getUsageInPeriod(packageName: String, period: LimitPeriod): Long =
        withContext(Dispatchers.Default) {
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val zoneId = ZoneId.systemDefault()
            val now = LocalDate.now(zoneId)
            
            val startMillis = when (period) {
                LimitPeriod.DAILY -> now.atStartOfDay(zoneId).toInstant().toEpochMilli()
                LimitPeriod.WEEKLY -> now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .atStartOfDay(zoneId).toInstant().toEpochMilli()
                LimitPeriod.MONTHLY -> now.with(TemporalAdjusters.firstDayOfMonth())
                    .atStartOfDay(zoneId).toInstant().toEpochMilli()
            }
            
            val endMillis = System.currentTimeMillis()

            usageStatsManager
                .queryAndAggregateUsageStats(startMillis, endMillis)[packageName]
                ?.totalTimeInForeground
                ?: 0L
        }

    override suspend fun getYesterdayUsageMillis(packageName: String): Long =
        withContext(Dispatchers.Default) {
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val zoneId = ZoneId.systemDefault()
            val yesterday = LocalDate.now(zoneId).minusDays(1)
            
            val startMillis = yesterday.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endMillis = yesterday.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

            usageStatsManager
                .queryAndAggregateUsageStats(startMillis, endMillis)[packageName]
                ?.totalTimeInForeground
                ?: 0L
        }
}
