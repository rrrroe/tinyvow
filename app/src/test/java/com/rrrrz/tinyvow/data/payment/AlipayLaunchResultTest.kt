package com.rrrrz.tinyvow.data.payment

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlipayLaunchResultTest {
    @Test
    fun `success processing and unknown results all require server confirmation`() {
        listOf("9000", "8000", "6004").forEach { status ->
            assertTrue(AlipayLaunchResult(status).shouldConfirmWithServer)
        }
    }

    @Test
    fun `user cancellation is not treated as a payment to confirm`() {
        val result = AlipayLaunchResult("6001")
        assertTrue(result.wasCancelled)
        assertFalse(result.shouldConfirmWithServer)
    }

    @Test
    fun `definite payment failure is not treated as a payment to confirm`() {
        val result = AlipayLaunchResult("4000")
        assertFalse(result.wasCancelled)
        assertFalse(result.shouldConfirmWithServer)
    }
}
