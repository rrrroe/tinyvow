package com.rrrrz.tinyvow.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class EncouragePointsCalculatorTest {
    @Test
    fun calculateUsageEarnedPoints_usesActualEncourageMinutesTimesRate() {
        val points = calculateUsageEarnedPoints(
            usageMillis = 12L * 60L * 1000L + 30L * 1000L,
            pointsPerMinute = 2.0,
        )

        assertEquals(25.0, points, 0.0001)
    }

    @Test
    fun calculateTargetBonusPoints_usesTargetMinutesTimesRate() {
        val points = calculateTargetBonusPoints(
            targetMinutes = 30,
            pointsPerMinute = 1.5,
        )

        assertEquals(45.0, points, 0.0001)
    }

    @Test
    fun calculateEncourageTargetPoints_includesUsagePointsAndOneTimeBonus() {
        val points = calculateEncourageTargetPoints(
            targetMinutes = 30,
            pointsPerMinute = 1.5,
        )

        assertEquals(90.0, points, 0.0001)
    }

    @Test
    fun calculateEncourageEarnedPoints_addsTargetBonusOnceWhenReached() {
        val points = calculateEncourageEarnedPoints(
            usageMillis = 20L * 60L * 1000L,
            targetMinutes = 30,
            pointsPerMinute = 1.5,
            targetReachedDuringWindow = true,
        )

        assertEquals(75.0, points, 0.0001)
    }
}
