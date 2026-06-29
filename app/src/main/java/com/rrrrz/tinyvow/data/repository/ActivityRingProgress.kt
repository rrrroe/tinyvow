package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.DailyGroupArchiveEntity
import com.rrrrz.tinyvow.data.db.GroupType

internal data class ActivityRingProgressSnapshot(
    val controlProgress: Double,
    val encourageProgress: Double,
    val growthProgress: Double,
    val controlAvailable: Boolean,
    val encourageAvailable: Boolean,
    val growthAvailable: Boolean,
    val growthTargetPoints: Double,
) {
    val ringsCompleted: Boolean
        get() =
            controlAvailable &&
                encourageAvailable &&
                growthAvailable &&
                controlProgress >= 1.0 &&
                encourageProgress >= 1.0 &&
                growthProgress >= 1.0
}

internal fun buildActivityRingProgressSnapshot(
    groupSnapshots: List<DailyGroupArchiveEntity>,
    pointsEarned: Double,
): ActivityRingProgressSnapshot {
    val controlProgressValues =
        groupSnapshots
            .filter {
                it.groupType == GroupType.CONTROL &&
                    (it.effectiveLimitMillisAtClose > 0L || it.limitMinutes > 0 || it.completed)
            }
            .map { snapshot ->
                if (snapshot.rewardExempted) {
                    1.0
                } else if (snapshot.effectiveLimitMillisAtClose <= 0L) {
                    if (snapshot.completed) 1.0 else 0.0
                } else {
                    val limit = snapshot.effectiveLimitMillisAtClose.toDouble().coerceAtLeast(1.0)
                    if (snapshot.periodUsageMillisAtClose <= snapshot.effectiveLimitMillisAtClose) {
                        1.0
                    } else {
                        val exceededRatio = (snapshot.periodUsageMillisAtClose - snapshot.effectiveLimitMillisAtClose) / limit
                        (1.0 - exceededRatio).coerceIn(0.0, 1.0)
                    }
                }
            }
    val encourageProgressValues =
        groupSnapshots
            .filter { it.groupType == GroupType.ENCOURAGE && it.limitMinutes > 0 }
            .map { snapshot ->
                val targetMillis = (snapshot.limitMinutes * 60_000L).coerceAtLeast(1L)
                if (snapshot.completed && snapshot.periodUsageMillisAtClose <= 0L) {
                    1.0
                } else {
                    (snapshot.periodUsageMillisAtClose.toDouble() / targetMillis.toDouble()).coerceAtLeast(0.0)
                }
            }
    val growthTargetPoints =
        groupSnapshots
            .filter { it.groupType == GroupType.ENCOURAGE && it.limitMinutes > 0 }
            .sumOf { snapshot ->
                val targetUsagePoints = snapshot.limitMinutes * snapshot.pointsPerMinute
                targetUsagePoints.coerceAtLeast(0.0)
            }
    val growthProgress =
        if (growthTargetPoints > 0.0) {
            (pointsEarned / growthTargetPoints).coerceAtLeast(0.0)
        } else {
            0.0
        }

    return ActivityRingProgressSnapshot(
        controlProgress = controlProgressValues.averageOrZero().coerceIn(0.0, 1.0),
        encourageProgress = encourageProgressValues.averageOrZero(),
        growthProgress = growthProgress,
        controlAvailable = controlProgressValues.isNotEmpty(),
        encourageAvailable = encourageProgressValues.isNotEmpty(),
        growthAvailable = growthTargetPoints > 0.0,
        growthTargetPoints = growthTargetPoints,
    )
}

private fun List<Double>.averageOrZero(): Double =
    if (isEmpty()) 0.0 else average()
