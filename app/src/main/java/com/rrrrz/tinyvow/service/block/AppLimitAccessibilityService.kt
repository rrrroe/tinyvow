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
        
        // Very broad log to see if the service is alive and catching anything
        android.util.Log.v("AppLimitService", "Event from: $packageName, type: ${event.eventType}")
        
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
            val selectedPackage = preferences.getSelectedPackageNameOnce()
            
            android.util.Log.d("AppLimitService", "Comparing event package: $packageName with selected: $selectedPackage")
            
            if (selectedPackage == null || packageName != selectedPackage) return@launch

            val usagePermission = UsageAccessStateChecker(applicationContext).getStatus()
            if (usagePermission != UsageAccessStatus.GRANTED) {
                android.util.Log.w("AppLimitService", "Usage access not granted, skipping check")
                return@launch
            }

            val dailyLimitMinutes = preferences.getDailyLimitMinutesOnce(selectedPackage) ?: return@launch
            val usageMillis =
                UsageStatsUsageRepository(applicationContext).getTodayUsageMillis(selectedPackage)
            
            android.util.Log.i("AppLimitService", "Checking $selectedPackage: used ${usageMillis / 60000}m / limit ${dailyLimitMinutes}m")
            
            val evaluation = DailyTimeLimitPolicy().evaluate(
                usageMillis = usageMillis,
                limitMillis = dailyLimitMinutes * 60_000L,
            )
            if (!evaluation.isExceeded) return@launch

            val now = SystemClock.elapsedRealtime()
            if (selectedPackage == lastBlockedPackage && now - lastBlockElapsedRealtime < BLOCK_DEBOUNCE_MS) {
                android.util.Log.d("AppLimitService", "Debouncing block for $selectedPackage")
                return@launch
            }
            lastBlockedPackage = selectedPackage
            lastBlockElapsedRealtime = now

            android.util.Log.i("AppLimitService", "Limit exceeded! Showing TYPE_ACCESSIBILITY_OVERLAY for $selectedPackage")
            
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                showBlockOverlay(selectedPackage, evaluation.exceededMillis)
            }
        }
    }

    private var blockView: android.view.View? = null

    private fun showBlockOverlay(packageName: String, exceededMillis: Long) {
        if (blockView != null) return

        val windowManager = getSystemService(WINDOW_SERVICE) as android.view.WindowManager
        val params = android.view.WindowManager.LayoutParams(
            android.view.WindowManager.LayoutParams.MATCH_PARENT,
            android.view.WindowManager.LayoutParams.MATCH_PARENT,
            android.view.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            android.graphics.PixelFormat.TRANSLUCENT
        )

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.parseColor("#E6000000")) // 90% black
            gravity = android.view.Gravity.CENTER
            setPadding(80, 80, 80, 80)
        }

        val totalMinutes = exceededMillis / 60_000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val exceededText = if (hours > 0) "${hours}小时 ${minutes}分钟" else "${minutes}分钟"

        val title = android.widget.TextView(this).apply {
            text = getString(com.rrrrz.tinyvow.R.string.block_title)
            textSize = 28f
            setTextColor(android.graphics.Color.WHITE)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 48)
        }
        
        val body = android.widget.TextView(this).apply {
            text = getString(com.rrrrz.tinyvow.R.string.block_body, packageName, exceededText)
            textSize = 16f
            setTextColor(android.graphics.Color.LTGRAY)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 80)
        }

        val btnOpenApp = android.widget.Button(this).apply {
            text = getString(com.rrrrz.tinyvow.R.string.block_open_app)
            setBackgroundColor(android.graphics.Color.parseColor("#FF6200EE"))
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener {
                removeBlockOverlay()
                val intent = Intent(this@AppLimitAccessibilityService, com.rrrrz.tinyvow.MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(intent)
            }
        }
        
        val space = android.view.View(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(1, 40)
        }

        val btnGoHome = android.widget.Button(this).apply {
            text = getString(com.rrrrz.tinyvow.R.string.block_go_home)
            setBackgroundColor(android.graphics.Color.parseColor("#FF444444"))
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener {
                removeBlockOverlay()
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            }
        }

        layout.addView(title)
        layout.addView(body)
        layout.addView(btnOpenApp)
        layout.addView(space)
        layout.addView(btnGoHome)

        try {
            windowManager.addView(layout, params)
            blockView = layout
            android.util.Log.i("AppLimitService", "Overlay added successfully!")
        } catch (e: Exception) {
            android.util.Log.e("AppLimitService", "Failed to add overlay", e)
        }
    }

    private fun removeBlockOverlay() {
        blockView?.let {
            val windowManager = getSystemService(WINDOW_SERVICE) as android.view.WindowManager
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                // Ignore
            }
            blockView = null
        }
    }

    override fun onInterrupt() {
        removeBlockOverlay()
    }

    override fun onDestroy() {
        removeBlockOverlay()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val BLOCK_DEBOUNCE_MS = 2_000L
        private var lastBlockedPackage: String? = null
        private var lastBlockElapsedRealtime: Long = 0L
    }
}
