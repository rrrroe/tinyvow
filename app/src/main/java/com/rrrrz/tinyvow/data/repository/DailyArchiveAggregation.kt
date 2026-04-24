package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.usage.AppSession
import kotlin.math.max
import kotlin.math.min

private const val HOUR_IN_MILLIS = 60L * 60L * 1000L
private const val NIGHT_END_HOUR = 6
private const val NIGHT_START_HOUR = 22

internal data class AppBehaviorSummary(
    val sessionCount: Int,
    val longestSessionMillis: Long,
    val nightUsageMillis: Long,
    val hourlyUsageMillis: LongArray,
)

internal fun summarizeAppBehavior(
    sessions: List<AppSession>,
    dayStart: Long,
    nextDayStart: Long,
): AppBehaviorSummary {
    val buckets = LongArray(24)
    var longestSessionMillis = 0L
    var nightUsageMillis = 0L
    var sessionCount = 0

    sessions.forEach { session ->
        val sessionStart = max(session.startTime, dayStart)
        val sessionEnd = min(session.endTime, nextDayStart)
        if (sessionEnd <= sessionStart) {
            return@forEach
        }

        sessionCount += 1
        longestSessionMillis = max(longestSessionMillis, sessionEnd - sessionStart)

        val earlyNightEnd = dayStart + NIGHT_END_HOUR * HOUR_IN_MILLIS
        val lateNightStart = dayStart + NIGHT_START_HOUR * HOUR_IN_MILLIS
        nightUsageMillis += overlapDuration(sessionStart, sessionEnd, dayStart, earlyNightEnd)
        nightUsageMillis += overlapDuration(sessionStart, sessionEnd, lateNightStart, nextDayStart)

        for (hour in buckets.indices) {
            val hourStart = dayStart + hour * HOUR_IN_MILLIS
            val hourEnd = hourStart + HOUR_IN_MILLIS
            buckets[hour] += overlapDuration(sessionStart, sessionEnd, hourStart, hourEnd)
        }
    }

    return AppBehaviorSummary(
        sessionCount = sessionCount,
        longestSessionMillis = longestSessionMillis,
        nightUsageMillis = nightUsageMillis,
        hourlyUsageMillis = buckets,
    )
}

internal fun allocateGroupEarnedPoints(
    totalPoints: Double,
    packageNames: List<String>,
    usageByPackage: Map<String, Long>,
): Map<String, Double> {
    if (packageNames.isEmpty()) {
        return emptyMap()
    }
    if (totalPoints == 0.0) {
        return packageNames.associateWith { 0.0 }
    }

    val weights =
        packageNames.associateWith { packageName ->
            max(usageByPackage[packageName] ?: 0L, 0L).toDouble()
        }
    val totalWeight = weights.values.sum()

    if (totalWeight <= 0.0) {
        val evenShare = totalPoints / packageNames.size
        return packageNames.associateWith { evenShare }
    }

    return packageNames.associateWith { packageName ->
        totalPoints * (weights[packageName] ?: 0.0) / totalWeight
    }
}

private fun overlapDuration(
    start: Long,
    end: Long,
    rangeStart: Long,
    rangeEnd: Long,
): Long = max(0L, min(end, rangeEnd) - max(start, rangeStart))
