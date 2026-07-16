package com.rrrrz.tinyvow.data.repository

internal object OfflineFocusSettlementPolicy {
    private const val EARLY_COMPLETE_THRESHOLD = 0.8f

    fun normalizeDurationMinutes(
        durationMinutes: Int,
        unlimitedDurationMinutes: Int,
        minDurationMinutes: Int,
        maxDurationMinutes: Int,
    ): Int =
        if (durationMinutes == unlimitedDurationMinutes) {
            unlimitedDurationMinutes
        } else {
            durationMinutes.coerceIn(minDurationMinutes, maxDurationMinutes)
        }

    fun shouldAward(
        plannedDurationMillis: Long,
        actualDurationMillis: Long,
        forceComplete: Boolean,
    ): Boolean {
        if (forceComplete || plannedDurationMillis <= 0L) return true
        return actualDurationMillis.toFloat() / plannedDurationMillis.toFloat() >= EARLY_COMPLETE_THRESHOLD
    }

    fun actualDurationForPoints(
        plannedDurationMillis: Long,
        actualDurationMillis: Long,
        forceComplete: Boolean,
    ): Long =
        when {
            plannedDurationMillis <= 0L -> actualDurationMillis
            forceComplete -> plannedDurationMillis
            else -> actualDurationMillis
        }
}
