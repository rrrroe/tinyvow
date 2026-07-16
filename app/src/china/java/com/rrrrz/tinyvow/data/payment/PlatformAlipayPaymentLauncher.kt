package com.rrrrz.tinyvow.data.payment

import android.app.Activity
import com.alipay.sdk.app.PayTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlatformAlipayPaymentLauncher : AlipayPaymentLauncher {
    override suspend fun launch(activity: Activity, orderString: String): AlipayLaunchResult =
        withContext(Dispatchers.IO) {
            val result = PayTask(activity).payV2(orderString, true)
            AlipayLaunchResult(resultStatus = result["resultStatus"].orEmpty())
        }
}
