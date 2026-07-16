package com.rrrrz.tinyvow.data.payment

import android.app.Activity

interface AlipayPaymentLauncher {
    suspend fun launch(activity: Activity, orderString: String): AlipayLaunchResult
}

data class AlipayLaunchResult(
    val resultStatus: String,
) {
    val shouldConfirmWithServer: Boolean
        get() = resultStatus in setOf("9000", "8000", "6004")

    val wasCancelled: Boolean
        get() = resultStatus == "6001"
}
