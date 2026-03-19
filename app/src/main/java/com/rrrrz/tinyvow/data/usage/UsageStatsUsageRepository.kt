package com.rrrrz.tinyvow.data.usage

import android.app.usage.UsageStatsManager
import android.content.Context
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsageStatsUsageRepository(
    private val context: Context,
) : UsageRepository {
    override suspend fun getTodayUsageMillis(packageName: String): Long =
        withContext(Dispatchers.Default) {
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val zoneId = ZoneId.systemDefault()
            val startOfDayMillis = LocalDate.now(zoneId)
                .atStartOfDay(zoneId)
                .toInstant()
                .toEpochMilli()
            val endMillis = System.currentTimeMillis()

            usageStatsManager
                .queryAndAggregateUsageStats(startOfDayMillis, endMillis)[packageName]
                ?.totalTimeInForeground
                ?: 0L
        }
}
