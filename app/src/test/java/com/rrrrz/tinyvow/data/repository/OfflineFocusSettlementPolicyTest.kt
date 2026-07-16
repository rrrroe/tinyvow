package com.rrrrz.tinyvow.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineFocusSettlementPolicyTest {
    @Test
    fun unlimitedDurationRemainsUnlimitedDuringNormalization() {
        assertEquals(
            0,
            OfflineFocusSettlementPolicy.normalizeDurationMinutes(
                durationMinutes = 0,
                unlimitedDurationMinutes = 0,
                minDurationMinutes = 5,
                maxDurationMinutes = 180,
            ),
        )
    }

    @Test
    fun finiteSessionAwardsAtEightyPercentButNotBelow() {
        val planned = 10 * MINUTE_MILLIS

        assertFalse(
            OfflineFocusSettlementPolicy.shouldAward(
                plannedDurationMillis = planned,
                actualDurationMillis = 8 * MINUTE_MILLIS - 1L,
                forceComplete = false,
            ),
        )
        assertTrue(
            OfflineFocusSettlementPolicy.shouldAward(
                plannedDurationMillis = planned,
                actualDurationMillis = 8 * MINUTE_MILLIS,
                forceComplete = false,
            ),
        )
    }

    @Test
    fun unlimitedSessionAlwaysSettlesUsingActualElapsedTime() {
        val elapsed = 37 * MINUTE_MILLIS

        assertTrue(
            OfflineFocusSettlementPolicy.shouldAward(
                plannedDurationMillis = 0L,
                actualDurationMillis = elapsed,
                forceComplete = false,
            ),
        )
        assertEquals(
            elapsed,
            OfflineFocusSettlementPolicy.actualDurationForPoints(
                plannedDurationMillis = 0L,
                actualDurationMillis = elapsed,
                forceComplete = false,
            ),
        )
    }

    @Test
    fun forcedFiniteCompletionUsesPlannedDurationForPoints() {
        assertEquals(
            25 * MINUTE_MILLIS,
            OfflineFocusSettlementPolicy.actualDurationForPoints(
                plannedDurationMillis = 25 * MINUTE_MILLIS,
                actualDurationMillis = 5 * MINUTE_MILLIS,
                forceComplete = true,
            ),
        )
    }

    private companion object {
        const val MINUTE_MILLIS = 60_000L
    }
}
