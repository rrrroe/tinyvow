package com.rrrrz.tinyvow.domain.limit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ControlGroupLimitPolicyTest {
    @Test
    fun exactEffectiveLimitDoesNotBlock() {
        val decision =
            ControlGroupLimitPolicy.evaluate(
                totalUsedMillis = 75 * MINUTE_MILLIS,
                baseLimitMillis = 60 * MINUTE_MILLIS,
                bonusMillis = 15 * MINUTE_MILLIS,
            )

        assertNull(decision)
    }

    @Test
    fun firstMillisecondOverEffectiveLimitBlocks() {
        val decision =
            ControlGroupLimitPolicy.evaluate(
                totalUsedMillis = 75 * MINUTE_MILLIS + 1L,
                baseLimitMillis = 60 * MINUTE_MILLIS,
                bonusMillis = 15 * MINUTE_MILLIS,
            )

        assertNotNull(decision)
        assertEquals(75 * MINUTE_MILLIS, decision?.totalLimitMillis)
        assertEquals(1L, decision?.exceededMillis)
    }

    @Test
    fun periodPassBypassesLimitEvaluation() {
        assertTrue(ControlGroupLimitPolicy.shouldBypass(hasPeriodPass = true))
    }

    private companion object {
        const val MINUTE_MILLIS = 60_000L
    }
}
