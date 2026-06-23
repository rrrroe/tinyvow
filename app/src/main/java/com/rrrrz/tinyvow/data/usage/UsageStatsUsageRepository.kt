package com.rrrrz.tinyvow.data.usage

import android.app.usage.UsageStatsManager
import android.content.Context
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.time.BusinessDay
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
            aggregateUsageFromSessions(queryUsageSessions(bounds.startMillis, bounds.endMillis))
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
            aggregateUsageFromSessions(queryUsageSessions(startMillis, endMillis))[packageName] ?: 0L
        }

    override suspend fun getUsageStats(startMillis: Long, endMillis: Long): Map<String, Long> =
        withContext(Dispatchers.Default) {
            aggregateUsageFromSessions(queryUsageSessions(startMillis, endMillis))
        }

    override suspend fun getUsageSessions(startMillis: Long, endMillis: Long): List<AppSession> =
        withContext(Dispatchers.Default) {
            queryUsageSessions(startMillis, endMillis)
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

    private fun queryUsageSessions(startMillis: Long, endMillis: Long): List<AppSession> {
        if (endMillis <= startMillis) return emptyList()
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val lookbackStart = (startMillis - SESSION_LOOKBACK_MS).coerceAtLeast(0L)
        val events = usageStatsManager.queryEvents(lookbackStart, endMillis)
        val event = android.app.usage.UsageEvents.Event()
        val transitions = mutableListOf<UsageEventTransition>()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            when (event.eventType) {
                android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED -> {
                    transitions.add(
                        UsageEventTransition(
                            packageName = pkg,
                            className = event.className,
                            timeStamp = event.timeStamp,
                            type = UsageEventTransitionType.RESUMED,
                        ),
                    )
                }
                android.app.usage.UsageEvents.Event.ACTIVITY_PAUSED,
                android.app.usage.UsageEvents.Event.ACTIVITY_STOPPED -> {
                    transitions.add(
                        UsageEventTransition(
                            packageName = pkg,
                            className = event.className,
                            timeStamp = event.timeStamp,
                            type = UsageEventTransitionType.PAUSED,
                        ),
                    )
                }
            }
        }
        return buildSessionsFromTransitions(
            transitions = transitions,
            rangeStart = startMillis,
            rangeEnd = endMillis,
            nowMillis = System.currentTimeMillis(),
        )
    }

    private companion object {
        // Query a small lookback window so sessions that started before midnight can still be
        // clipped into today's range instead of leaking the whole previous-day foreground total.
        private const val SESSION_LOOKBACK_MS = 24L * 60L * 60L * 1000L
    }
}

internal fun aggregateUsageFromSessions(sessions: List<AppSession>): Map<String, Long> =
    buildMap {
        sessions.forEach { session ->
            if (session.endTime <= session.startTime) return@forEach
            put(session.packageName, (get(session.packageName) ?: 0L) + (session.endTime - session.startTime))
        }
    }

internal enum class UsageEventTransitionType {
    RESUMED,
    PAUSED,
}

internal data class UsageEventTransition(
    val packageName: String,
    val className: String?,
    val timeStamp: Long,
    val type: UsageEventTransitionType,
)

internal fun buildSessionsFromTransitions(
    transitions: List<UsageEventTransition>,
    rangeStart: Long,
    rangeEnd: Long,
    nowMillis: Long,
): List<AppSession> {
    if (rangeEnd <= rangeStart) return emptyList()
    val sessions = mutableListOf<AppSession>()
    val states = mutableMapOf<String, PackageUsageState>()

    transitions
        .sortedBy { it.timeStamp }
        .forEach { transition ->
            val state = states.getOrPut(transition.packageName) { PackageUsageState() }
            when (transition.type) {
                UsageEventTransitionType.RESUMED -> {
                    if (!state.isActive) {
                        state.activeStart = transition.timeStamp
                    }
                    val className = transition.className
                    if (className.isNullOrBlank()) {
                        state.anonymousDepth += 1
                    } else {
                        state.activeClasses.add(className)
                    }
                }
                UsageEventTransitionType.PAUSED -> {
                    val className = transition.className
                    val changed =
                        if (className.isNullOrBlank()) {
                            if (state.anonymousDepth > 0) {
                                state.anonymousDepth -= 1
                                true
                            } else {
                                false
                            }
                        } else {
                            state.activeClasses.remove(className)
                        }
                    if (changed && !state.isActive) {
                        val start = state.activeStart
                        if (start != null && start < transition.timeStamp) {
                            clipSessionToRange(
                                session = AppSession(transition.packageName, start, transition.timeStamp),
                                rangeStart = rangeStart,
                                rangeEnd = rangeEnd,
                            )?.let(sessions::add)
                        }
                        state.activeStart = null
                    }
                }
            }
        }

    states.forEach { (packageName, state) ->
        val start = state.activeStart
        if (state.isActive && start != null) {
            clipSessionToRange(
                session = AppSession(packageName, start, minOf(nowMillis, rangeEnd)),
                rangeStart = rangeStart,
                rangeEnd = rangeEnd,
            )?.let(sessions::add)
        }
    }
    return sessions.sortedBy { it.startTime }
}

private data class PackageUsageState(
    val activeClasses: MutableSet<String> = mutableSetOf(),
    var anonymousDepth: Int = 0,
    var activeStart: Long? = null,
) {
    val isActive: Boolean
        get() = activeClasses.isNotEmpty() || anonymousDepth > 0
}

internal fun clipSessionToRange(
    session: AppSession,
    rangeStart: Long,
    rangeEnd: Long,
): AppSession? {
    if (rangeEnd <= rangeStart) return null
    val clippedStart = maxOf(session.startTime, rangeStart)
    val clippedEnd = minOf(session.endTime, rangeEnd)
    if (clippedEnd <= clippedStart) return null
    return session.copy(startTime = clippedStart, endTime = clippedEnd)
}

internal data class UsagePeriodBounds(
    val startMillis: Long,
    val endMillis: Long,
)

internal fun usagePeriodBounds(
    period: LimitPeriod,
    zoneId: ZoneId = ZoneId.systemDefault(),
    currentDate: LocalDate = BusinessDay.today(zoneId, BusinessDay.cachedStartHour(), System.currentTimeMillis()),
    nowMillis: Long = System.currentTimeMillis(),
    dayStartHour: Int = BusinessDay.cachedStartHour(),
): UsagePeriodBounds {
    val startMillis = when (period) {
        LimitPeriod.DAILY -> BusinessDay.startOfDayMillis(currentDate, zoneId, dayStartHour)
        LimitPeriod.WEEKLY -> BusinessDay.startOfDayMillis(currentDate.minusDays(6), zoneId, dayStartHour)
        LimitPeriod.MONTHLY ->
            BusinessDay.startOfDayMillis(
                currentDate.with(TemporalAdjusters.firstDayOfMonth()),
                zoneId,
                dayStartHour,
            )
    }
    return UsagePeriodBounds(startMillis = startMillis, endMillis = nowMillis)
}
