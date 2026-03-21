package com.rrrrz.tinyvow.service.block

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.rrrrz.tinyvow.data.usage.UsageAccessStateChecker
import com.rrrrz.tinyvow.data.usage.UsageAccessStatus
import com.rrrrz.tinyvow.domain.limit.GroupLimitEnforcer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt

@android.annotation.SuppressLint("all")
@Suppress("all")
class AppLimitAccessibilityService : AccessibilityService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var enforcer: GroupLimitEnforcer

    override fun onServiceConnected() {
        super.onServiceConnected()
        enforcer = GroupLimitEnforcer(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return

        android.util.Log.v("AppLimitService", "Event from: $packageName, type: ${event.eventType}")

        // 忽略自身
        if (packageName == this.packageName) return

        // 只关注窗口切换事件
        if (
            event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) return

        // 高频事件防抖：和上一次评估的包名+时间对比
        val now = SystemClock.elapsedRealtime()
        if (packageName == lastCheckedPackage && now - lastCheckElapsedRealtime < CHECK_DEBOUNCE_MS) {
            return
        }
        lastCheckedPackage = packageName
        lastCheckElapsedRealtime = now

        serviceScope.launch(Dispatchers.IO) {
            // 前置检查：使用量权限
            val usagePermission = UsageAccessStateChecker(applicationContext).getStatus()
            if (usagePermission != UsageAccessStatus.GRANTED) {
                android.util.Log.w("AppLimitService", "Usage access not granted, skipping check")
                return@launch
            }

            // ★ 核心：多分组短板效应评估
            val result = enforcer.evaluate(packageName)
            if (result == null) {
                // 没超标，如果有 overlay 就移除
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    removeBlockOverlay()
                }
                return@launch
            }

            // 阻断防抖：同一个包名短时间内不重复弹窗
            val blockNow = SystemClock.elapsedRealtime()
            if (packageName == lastBlockedPackage && blockNow - lastBlockElapsedRealtime < BLOCK_DEBOUNCE_MS) {
                android.util.Log.d("AppLimitService", "Debouncing block for $packageName")
                return@launch
            }
            lastBlockedPackage = packageName
            lastBlockElapsedRealtime = blockNow

            android.util.Log.i(
                "AppLimitService",
                "Group [${result.groupName}] exceeded! Used ${result.totalUsedMillis / 60000}m / limit ${result.limitMinutes}m. Blocking $packageName."
            )

            kotlinx.coroutines.withContext(Dispatchers.Main) {
                showBlockOverlay(packageName, result.groupName, result.exceededMillis)
            }
        }
    }

    // ──────── Overlay ────────

    private var blockView: android.view.View? = null

    private fun showBlockOverlay(packageName: String, groupName: String, exceededMillis: Long) {
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
            setBackgroundColor("#E6000000".toColorInt()) // 90% black
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
            text = getString(com.rrrrz.tinyvow.R.string.block_body_group, groupName, exceededText)
            textSize = 16f
            setTextColor(android.graphics.Color.LTGRAY)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 80)
        }

        val btnOpenApp = android.widget.Button(this).apply {
            text = getString(com.rrrrz.tinyvow.R.string.block_overlay_open_app)
            setBackgroundColor("#FF6200EE".toColorInt())
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
            text = getString(com.rrrrz.tinyvow.R.string.block_overlay_go_home)
            setBackgroundColor("#FF444444".toColorInt())
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
            } catch (_: Exception) {
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
        /** 评估防抖窗口：同一个包名 3 秒内不重复查询 */
        private const val CHECK_DEBOUNCE_MS = 3_000L
        /** 阻断弹窗防抖窗口：同一个包名 5 秒内不重复弹 overlay */
        private const val BLOCK_DEBOUNCE_MS = 5_000L
        private var lastCheckedPackage: String? = null
        private var lastCheckElapsedRealtime: Long = 0L
        private var lastBlockedPackage: String? = null
        private var lastBlockElapsedRealtime: Long = 0L
    }
}
