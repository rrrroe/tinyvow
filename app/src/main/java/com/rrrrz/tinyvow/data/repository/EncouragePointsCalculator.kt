package com.rrrrz.tinyvow.data.repository

import kotlin.math.max

internal fun calculateUsageEarnedPoints(
    usageMillis: Long,
    pointsPerMinute: Double,
): Double {
    if (usageMillis <= 0L || pointsPerMinute <= 0.0) return 0.0
    return usageMillis / 60_000.0 * pointsPerMinute
}

internal fun calculateTargetBonusPoints(
    targetMinutes: Int,
    pointsPerMinute: Double,
): Double {
    if (pointsPerMinute <= 0.0) return 0.0
    return max(targetMinutes, 0) * pointsPerMinute
}

internal fun calculateEncourageTargetPoints(
    targetMinutes: Int,
    pointsPerMinute: Double,
): Double =
    calculateUsageEarnedPoints(
        usageMillis = max(targetMinutes, 0) * 60_000L,
        pointsPerMinute = pointsPerMinute,
    ) + calculateTargetBonusPoints(targetMinutes, pointsPerMinute)

internal fun calculateEncourageEarnedPoints(
    usageMillis: Long,
    targetMinutes: Int,
    pointsPerMinute: Double,
    targetReachedDuringWindow: Boolean,
): Double {
    val usagePoints = calculateUsageEarnedPoints(usageMillis, pointsPerMinute)
    val bonusPoints =
        if (targetReachedDuringWindow) {
            calculateTargetBonusPoints(targetMinutes, pointsPerMinute)
        } else {
            0.0
        }
    return usagePoints + bonusPoints
}
