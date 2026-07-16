package com.rrrrz.tinyvow.data.payment

import org.junit.Assert.assertEquals
import org.junit.Test

class PendingPaymentPolicyTest {
    @Test
    fun `paid order clears pending state as success`() {
        assertEquals(PendingPaymentAction.CLEAR_SUCCESS, pendingPaymentAction("PAID"))
    }

    @Test
    fun `terminal orders clear pending state without success`() {
        listOf("CLOSED", "FAILED", "REFUNDED").forEach { status ->
            assertEquals(PendingPaymentAction.CLEAR_TERMINAL, pendingPaymentAction(status))
        }
    }

    @Test
    fun `created and paying orders survive restart for later recovery`() {
        listOf("CREATED", "PAYING", "unknown").forEach { status ->
            assertEquals(PendingPaymentAction.KEEP, pendingPaymentAction(status))
        }
    }
}
