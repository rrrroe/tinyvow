package com.rrrrz.tinyvow.service.block

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.usage.UsageAccessStateChecker
import com.rrrrz.tinyvow.data.usage.UsageAccessStatus
import com.rrrrz.tinyvow.data.usage.UsageStatsUsageRepository
import com.rrrrz.tinyvow.domain.limit.DailyTimeLimitPolicy
import com.rrrrz.tinyvow.ui.block.BlockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AppLimitAccessibilityService : AccessibilityService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        if (packageName == this.packageName || packageName == BlockActivity.BLOCK_ACTIVITY_PACKAGE) {
            return
        }
        if (
            event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) {
            return
        }

        serviceScope.launch {
            val preferences = ManagedAppPreferences(applicationContext)
            val selectedPackage = preferences.getSelectedPackageNameOnce() ?: return@launch
            if (packageName != selectedPackage) return@launch

            val usagePermission = UsageAccessStateChecker(applicationContext).getStatus()
            if (usagePermission != UsageAccessStatus.GRANTED) return@launch

            val dailyLimitMinutes = preferences.getDailyLimitMinutesOnce(selectedPackage) ?: return@launch
            val usageMillis =
                UsageStatsUsageRepository(applicationContext).getTodayUsageMillis(selectedPackage)
            val evaluation = DailyTimeLimitPolicy().evaluate(
                usageMillis = usageMillis,
                limitMillis = dailyLimitMinutes * 60_000L,
            )
            if (!evaluation.isExceeded) return@launch

            val now = SystemClock.elapsedRealtime()
            if (selectedPackage == lastBlockedPackage && now - lastBlockElapsedRealtime < BLOCK_DEBOUNCE_MS) {
                return@launch
            }
            lastBlockedPackage = selectedPackage
            lastBlockElapsedRealtime = now

            val intent = Intent(applicationContext, BlockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(BlockActivity.EXTRA_PACKAGE_NAME, selectedPackage)
                putExtra(BlockActivity.EXTRA_EXCEEDED_MILLIS, evaluation.exceededMillis)
            }
            startActivity(intent)
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val BLOCK_DEBOUNCE_MS = 2_000L
        private var lastBlockedPackage: String? = null
        private var lastBlockElapsedRealtime: Long = 0L
    }
}
