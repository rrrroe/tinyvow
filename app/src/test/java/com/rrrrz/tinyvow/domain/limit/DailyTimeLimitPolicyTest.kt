package com.rrrrz.tinyvow.domain.limit

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyTimeLimitPolicyTest {
    @Test
    fun evaluate_treatsAnyOverrunAsExceeded() {
        val evaluation = DailyTimeLimitPolicy().evaluate(
            usageMillis = 60L * 60L * 1000L + 1L,
            limitMillis = 60L * 60L * 1000L,
        )

        assertTrue(evaluation.isExceeded)
    }

    @Test
    fun statsTimeout_allowsFiveMinuteGrace() {
        assertFalse(isControlTimeoutForStats(5L * 60L * 1000L))
        assertTrue(isControlTimeoutForStats(5L * 60L * 1000L + 1L))
    }

    @Test
    fun evaluate_doesNotTreatExactLimitAsExceeded() {
        val evaluation = DailyTimeLimitPolicy().evaluate(
            usageMillis = 60L * 60L * 1000L,
            limitMillis = 60L * 60L * 1000L,
        )

        assertFalse(evaluation.isExceeded)
    }
}
