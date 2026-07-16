package com.rrrrz.tinyvow.data.payment

enum class PendingPaymentAction {
    KEEP,
    CLEAR_SUCCESS,
    CLEAR_TERMINAL,
}

fun pendingPaymentAction(status: String): PendingPaymentAction =
    when (status.uppercase()) {
        "PAID" -> PendingPaymentAction.CLEAR_SUCCESS
        "CLOSED", "FAILED", "REFUNDED" -> PendingPaymentAction.CLEAR_TERMINAL
        else -> PendingPaymentAction.KEEP
    }
