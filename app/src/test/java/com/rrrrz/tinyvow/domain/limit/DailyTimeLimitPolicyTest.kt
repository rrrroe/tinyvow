package com.rrrrz.tinyvow.domain.limit

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyTimeLimitPolicyTest {
    @Test
    fun evaluate_doesNotTreatTenMinuteOverrunAsExceeded() {
        val evaluation = DailyTimeLimitPolicy().evaluate(
            usageMillis = 70L * 60L * 1000L,
            limitMillis = 60L * 60L * 1000L,
        )

        assertFalse(evaluation.isExceeded)
    }

    @Test
    fun evaluate_treatsMoreThanTenMinuteOverrunAsExceeded() {
        val evaluation = DailyTimeLimitPolicy().evaluate(
            usageMillis = 70L * 60L * 1000L + 1L,
            limitMillis = 60L * 60L * 1000L,
        )

        assertTrue(evaluation.isExceeded)
    }
}
