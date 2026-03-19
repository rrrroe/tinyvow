package com.rrrrz.tinyvow.data.usage

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.os.Process

class UsageAccessStateChecker(
    private val context: Context,
) {
    @Suppress("DEPRECATION")
    fun getStatus(): UsageAccessStatus {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOpsManager.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        } else {
            @Suppress("DEPRECATION")
            appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        }

        return if (mode == AppOpsManager.MODE_ALLOWED) {
            UsageAccessStatus.GRANTED
        } else {
            UsageAccessStatus.DENIED
        }
    }
}
