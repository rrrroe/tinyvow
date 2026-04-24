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
import kotlinx.coroutines.isActive
import androidx.core.graphics.toColorInt

import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.db.BlockEventEntity
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.repository.ArchiveDateUtils
import com.rrrrz.tinyvow.data.repository.PointsRepository
import java.time.ZoneId
import java.util.UUID

@android.annotation.SuppressLint("all")
@Suppress("all")
class AppLimitAccessibilityService : AccessibilityService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var enforcer: GroupLimitEnforcer
    private lateinit var preferences: ManagedAppPreferences
    private lateinit var pointsRepository: PointsRepository
    private val database by lazy { com.rrrrz.tinyvow.data.db.AppDatabase.getDatabase(applicationContext) }

    // 积分积累状态
    private var lastPackageForPoints: String? = null
    private var startTimeForPoints: Long = 0L
    private var lastUpdateElapsedRealtime: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        enforcer = GroupLimitEnforcer(applicationContext)
        preferences = ManagedAppPreferences(applicationContext)
        pointsRepository = PointsRepository(applicationContext, database)

        // 启动定时结算协程
        startPeriodicPointsTicker()
        // 启动事件消费协程
        startEventConsumer()
    }

    private fun startPeriodicPointsTicker() {
        serviceScope.launch(Dispatchers.IO) {
            while (this.isActive) {
                kotlinx.coroutines.delay(60_000L) // 每分钟检查一次
                val currentPackage = lastPackageForPoints
                if (currentPackage != null) {
                    handlePointsAccumulation(currentPackage, SystemClock.elapsedRealtime())
                }
            }
        }
    }

    // CONFLATED Channel：只保留最新的包名，高频窗口切换事件自动合并，防止协程爆炸
    private val eventChannel = kotlinx.coroutines.channels.Channel<String>(
        kotlinx.coroutines.channels.Channel.CONFLATED
    )

    private fun startEventConsumer() {
        serviceScope.launch(Dispatchers.IO) {
            for (packageName in eventChannel) {
                evaluateAndBlock(packageName)
            }
        }
    }

    private suspend fun evaluateAndBlock(packageName: String) {
        val usagePermission = UsageAccessStateChecker(applicationContext).getStatus()
        if (usagePermission != UsageAccessStatus.GRANTED) return

        val result = enforcer.evaluate(packageName)
        if (result == null || result.groupType == GroupType.ENCOURAGE) {
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                removeBlockOverlay()
            }
            return
        }

        // 阻断防抖
        val blockNow = SystemClock.elapsedRealtime()
        if (packageName == lastBlockedPackage && blockNow - lastBlockElapsedRealtime < BLOCK_DEBOUNCE_MS) {
            return
        }
        lastBlockedPackage = packageName
        lastBlockElapsedRealtime = blockNow
        recordBlockEvent(packageName, result)

        kotlinx.coroutines.withContext(Dispatchers.Main) {
            showBlockOverlay(packageName, result.groupName, result.exceededMillis)
        }
    }

    private suspend fun recordBlockEvent(
        packageName: String,
        result: com.rrrrz.tinyvow.domain.limit.GroupExceededResult,
    ) {
        val nowMillis = System.currentTimeMillis()
        val zoneId = ZoneId.systemDefault()
        database.blockEventDao().insert(
            BlockEventEntity(
                id = UUID.randomUUID().toString(),
                eventDate = ArchiveDateUtils.formatDate(ArchiveDateUtils.localDateAt(nowMillis, zoneId)),
                occurredAt = nowMillis,
                packageName = packageName,
                groupId = result.groupId,
                groupNameSnapshot = result.groupName,
                exceededMillis = result.exceededMillis,
                createdAt = nowMillis,
            )
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return

        // 忽略自身
        if (packageName == this.packageName) return

        // 只关注窗口切换事件
        if (
            event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) return

        val now = SystemClock.elapsedRealtime()

        // 1. 积分累加逻辑：当包切换时，结算上个包的积分
        handlePointsAccumulation(packageName, now)

        // 2. 限额评估逻辑
        // 高频事件防抖
        if (packageName == lastCheckedPackage && now - lastCheckElapsedRealtime < CHECK_DEBOUNCE_MS) {
            return
        }
        lastCheckedPackage = packageName
        lastCheckElapsedRealtime = now

        // 发送到 CONFLATED Channel（非阻塞，自动丢弃旧值，只保留最新包名）
        eventChannel.trySend(packageName)
    }

    private fun handlePointsAccumulation(packageName: String, now: Long) {
        val oldPackage = lastPackageForPoints
        
        if (oldPackage == null) {
            // 第一次记录
            lastPackageForPoints = packageName
            startTimeForPoints = now
            lastUpdateElapsedRealtime = now
            return
        }

        if (oldPackage != packageName) {
            // 包名切换：结算旧包的最后一段 deltas
            val durationMs = now - lastUpdateElapsedRealtime
            if (durationMs > 1000) {
                creditPoints(oldPackage, durationMs)
            }
            // 重置状态
            lastPackageForPoints = packageName
            startTimeForPoints = now
            lastUpdateElapsedRealtime = now
        } else {
            // 同一个包：每隔 1 分钟结算一次
            val durationMs = now - lastUpdateElapsedRealtime
            if (durationMs >= 60_000L) {
                creditPoints(packageName, durationMs)
                lastUpdateElapsedRealtime = now
            }
        }
    }

    private fun creditPoints(packageName: String, durationMs: Long) {
        serviceScope.launch(Dispatchers.IO) {
            val groupIds = database.crossRefDao().getGroupIdsForPackageSync(packageName)
            for (gid in groupIds) {
                val group = database.appGroupDao().getGroupByIdSync(gid) ?: continue
                if (group.type == GroupType.ENCOURAGE && group.pointsPerMinute > 0) {
                    // 基础每分钟积分
                    val pointsEarned = (durationMs / 60000.0) * group.pointsPerMinute
                    pointsRepository.recordUsageEarn(group, pointsEarned)
                    
                    // 检查是否达成今日目标大奖
                    checkAndGrantBonus(group)
                }
            }
        }
    }

    private suspend fun checkAndGrantBonus(group: com.rrrrz.tinyvow.data.db.AppGroupEntity) {
        val nowMillis = System.currentTimeMillis()
        val todayStart = getStartOfDay(nowMillis)
        
        if (group.lastBonusAt >= todayStart) {
            return // 今天已经领过了
        }

        // 获取该分组今日总用量
        val packages = database.crossRefDao().getPackageNamesForGroupSync(group.id)
        val usageRepo = com.rrrrz.tinyvow.data.usage.UsageStatsUsageRepository(applicationContext)
        var totalTodayUsageMs = 0L
        for (pkg in packages) {
            totalTodayUsageMs += usageRepo.getTodayUsageMillis(pkg)
        }

        val targetMs = group.limitMinutes * 60_000L
        if (totalTodayUsageMs >= targetMs) {
            // 达成目标！发放奖励：目标分钟 * 鼓励金比例
            val bonusPoints = group.limitMinutes * group.pointsPerMinute
            pointsRepository.recordTargetBonusEarn(group, bonusPoints)
            
            // 更新数据库标记
            database.appGroupDao().insertGroup(group.copy(lastBonusAt = nowMillis))
            
            android.util.Log.i("AppLimitService", "Group ${group.name} reached daily target! Bonus $bonusPoints pts granted.")
        }
    }

    private fun getStartOfDay(millis: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = millis
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private suspend fun getEncouragementPointsPerMinute(packageName: String): Double {
        val groupIds = database.crossRefDao().getGroupIdsForPackageSync(packageName)
        var maxRate = 0.0
        for (gid in groupIds) {
            val group = database.appGroupDao().getGroupByIdSync(gid)
            if (group?.type == GroupType.ENCOURAGE) {
                maxRate = maxOf(maxRate, group.pointsPerMinute)
            }
        }
        return maxRate
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
            setBackgroundColor("#F8FAFB".toColorInt()) // 清新的亮色背景，更积极
            gravity = android.view.Gravity.CENTER
            setPadding(100, 100, 100, 100)
        }

        val iconView = android.widget.ImageView(this).apply {
            setImageResource(com.rrrrz.tinyvow.R.mipmap.ic_launcher)
            layoutParams = android.widget.LinearLayout.LayoutParams(180, 180).apply {
                bottomMargin = 60
            }
        }

        val totalMinutes = exceededMillis / 60_000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val exceededText = if (hours > 0) "${hours}小时 ${minutes}分钟" else "${minutes}分钟"

        val title = android.widget.TextView(this).apply {
            text = "此刻，给自己一个深呼吸"
            textSize = 26f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor("#2F3133".toColorInt())
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }

        val body = android.widget.TextView(this).apply {
            text = "你今日已使用 $groupName 超过 $exceededText。\n自律不是限制，而是为了遇见更好的自己。"
            textSize = 15f
            setLineSpacing(2f, 1.2f)
            setTextColor("#5F6266".toColorInt())
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 80)
        }

        // 主按钮 (回到首页)
        val btnPrimaryBg = android.graphics.drawable.GradientDrawable().apply {
            setColor("#8FB9C5".toColorInt()) 
            cornerRadius = 50f
        }
        val btnGoHome = android.widget.Button(this).apply {
            text = getString(com.rrrrz.tinyvow.R.string.block_overlay_go_home)
            background = btnPrimaryBg
            setTextColor(android.graphics.Color.WHITE)
            isAllCaps = false
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 
                140
            )
            setOnClickListener {
                removeBlockOverlay()
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            }
        }

        layout.addView(iconView)
        layout.addView(title)
        layout.addView(body)
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
        /** 评估防抖窗口：缩短以提升响应速度 */
        private const val CHECK_DEBOUNCE_MS = 300L
        /** 阻断弹窗防抖窗口：缩短以确保点击关闭后能较快再次生效 */
        private const val BLOCK_DEBOUNCE_MS = 500L
        private var lastCheckedPackage: String? = null
        private var lastCheckElapsedRealtime: Long = 0L
        private var lastBlockedPackage: String? = null
        private var lastBlockElapsedRealtime: Long = 0L
    }
}
