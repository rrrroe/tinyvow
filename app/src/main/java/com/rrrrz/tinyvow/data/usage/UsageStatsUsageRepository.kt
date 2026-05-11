package com.rrrrz.tinyvow.data.usage

import android.app.usage.UsageStatsManager
import android.content.Context
import com.rrrrz.tinyvow.data.db.LimitPeriod
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsageStatsUsageRepository(
    private val context: Context,
) : UsageRepository {
    override suspend fun getTodayUsageMillis(packageName: String): Long =
        getUsageInPeriod(packageName, LimitPeriod.DAILY)

    override suspend fun getUsageInPeriod(packageName: String, period: LimitPeriod): Long =
        getUsageStatsInPeriod(period)[packageName] ?: 0L

    override suspend fun getUsageStatsInPeriod(period: LimitPeriod): Map<String, Long> =
        withContext(Dispatchers.Default) {
            val bounds = usagePeriodBounds(period)
            aggregateUsageStats(bounds.startMillis, bounds.endMillis)
        }

    override suspend fun getYesterdayUsageMillis(packageName: String): Long =
        withContext(Dispatchers.Default) {
            val zoneId = ZoneId.systemDefault()
            val yesterday = LocalDate.now(zoneId).minusDays(1)

            val startMillis = yesterday.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endMillis = yesterday.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

            getUsageMillis(packageName, startMillis, endMillis)
        }

    override suspend fun getUsageMillis(packageName: String, startMillis: Long, endMillis: Long): Long =
        withContext(Dispatchers.Default) {
            aggregateUsageStats(startMillis, endMillis)[packageName] ?: 0L
        }

    override suspend fun getUsageStats(startMillis: Long, endMillis: Long): Map<String, Long> =
        withContext(Dispatchers.Default) {
            aggregateUsageStats(startMillis, endMillis)
        }

    override suspend fun getUsageSessions(startMillis: Long, endMillis: Long): List<AppSession> =
        withContext(Dispatchers.Default) {
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            
            val sessions = mutableListOf<AppSession>()
            val lastResumed = mutableMapOf<String, Long>()
            val events = usageStatsManager.queryEvents(startMillis, endMillis)
            val event = android.app.usage.UsageEvents.Event()
            
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val pkg = event.packageName
                if (event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED) {
                    lastResumed[pkg] = event.timeStamp
                } else if (event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_PAUSED ||
                           event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_STOPPED) {
                    val start = lastResumed.remove(pkg)
                    if (start != null && start < event.timeStamp) {
                        sessions.add(AppSession(pkg, start, event.timeStamp))
                    }
                }
            }
            // handle apps that are currently resumed
            lastResumed.forEach { (pkg, start) ->
                if (start < endMillis) {
                    sessions.add(AppSession(pkg, start, minOf(System.currentTimeMillis(), endMillis)))
                }
            }
            sessions.sortedBy { it.startTime }
        }

    override suspend fun getAppOpenCount(startMillis: Long, endMillis: Long): Map<String, Int> =
        withContext(Dispatchers.Default) {
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val counts = mutableMapOf<String, Int>()
            val events = usageStatsManager.queryEvents(startMillis, endMillis)
            val event = android.app.usage.UsageEvents.Event()
            
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED) {
                    counts[event.packageName] = counts.getOrDefault(event.packageName, 0) + 1
                }
            }
            counts
        }

    private fun aggregateUsageStats(startMillis: Long, endMillis: Long): Map<String, Long> {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        return usageStatsManager
            .queryAndAggregateUsageStats(startMillis, endMillis)
            .mapValues { (_, stats) -> stats.totalTimeInForeground }
    }
}

internal data class UsagePeriodBounds(
    val startMillis: Long,
    val endMillis: Long,
)

internal fun usagePeriodBounds(
    period: LimitPeriod,
    zoneId: ZoneId = ZoneId.systemDefault(),
    currentDate: LocalDate = LocalDate.now(zoneId),
    nowMillis: Long = System.currentTimeMillis(),
): UsagePeriodBounds {
    val startMillis = when (period) {
        LimitPeriod.DAILY -> currentDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        LimitPeriod.WEEKLY -> currentDate.minusDays(6).atStartOfDay(zoneId).toInstant().toEpochMilli()
        LimitPeriod.MONTHLY -> currentDate.with(TemporalAdjusters.firstDayOfMonth())
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
    }
    return UsagePeriodBounds(startMillis = startMillis, endMillis = nowMillis)
}
