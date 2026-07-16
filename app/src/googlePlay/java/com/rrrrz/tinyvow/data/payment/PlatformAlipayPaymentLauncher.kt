package com.rrrrz.tinyvow.data.payment

import android.app.Activity

class PlatformAlipayPaymentLauncher : AlipayPaymentLauncher {
    override suspend fun launch(activity: Activity, orderString: String): AlipayLaunchResult =
        AlipayLaunchResult(resultStatus = "unsupported")
}
