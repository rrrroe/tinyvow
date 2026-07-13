package com.rrrrz.tinyvow.service.block

import com.rrrrz.tinyvow.i18n.AppText

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.app.KeyguardManager
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.graphics.toColorInt
import com.rrrrz.tinyvow.BuildConfig
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.db.AppGroupEntity
import com.rrrrz.tinyvow.data.db.BlockEventEntity
import com.rrrrz.tinyvow.data.db.EncourageMetric
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LockScreenTimerAppStatus
import com.rrrrz.tinyvow.data.db.RewardType
import com.rrrrz.tinyvow.data.lockscreen.LockScreenTimerAppRepository
import com.rrrrz.tinyvow.data.notification.TinyVowNotifier
import com.rrrrz.tinyvow.data.reminder.ReminderPolicy
import com.rrrrz.tinyvow.data.repository.AppLimitRepository
import com.rrrrz.tinyvow.data.repository.ArchiveDateUtils
import com.rrrrz.tinyvow.data.repository.OfflineFocusRepository
import com.rrrrz.tinyvow.data.repository.PointsRepository
import com.rrrrz.tinyvow.data.repository.UseRewardResult
import com.rrrrz.tinyvow.data.repository.calculateTargetBonusPoints
import com.rrrrz.tinyvow.data.repository.calculateUsageEarnedPoints
import com.rrrrz.tinyvow.data.repository.parseRewardPayload
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.time.BusinessDay
import com.rrrrz.tinyvow.data.usage.UsageAccessStateChecker
import com.rrrrz.tinyvow.data.usage.UsageAccessStatus
import com.rrrrz.tinyvow.data.special.SpecialAppUsageRepository
import com.rrrrz.tinyvow.data.special.WEREAD_PACKAGE_NAME
import com.rrrrz.tinyvow.data.usage.MergedUsageRepository
import com.rrrrz.tinyvow.domain.limit.GroupExceededResult
import com.rrrrz.tinyvow.domain.limit.GroupLimitEnforcer
import com.rrrrz.tinyvow.ui.theme.DefaultThemeSeed
import com.rrrrz.tinyvow.ui.theme.resolveThemeSeed
import com.rrrrz.tinyvow.ui.theme.themeTokensFromSeed
import androidx.compose.ui.graphics.toArgb
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class AppLimitAccessibilityService : AccessibilityService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var enforcer: GroupLimitEnforcer
    private lateinit var pointsRepository: PointsRepository
    private lateinit var appLimitRepository: AppLimitRepository
    private val database by lazy { AppDatabase.getDatabase(applicationContext) }
    private val usageAccessStateChecker by lazy { UsageAccessStateChecker(applicationContext) }
    private val usageRepository by lazy { MergedUsageRepository(applicationContext) }
    private val lockScreenTimerRepository by lazy { LockScreenTimerAppRepository(applicationContext, database) }
    private val specialAppUsageRepository by lazy { SpecialAppUsageRepository(applicationContext) }
    private val preferences by lazy { ManagedAppPreferences(applicationContext) }
    private val offlineFocusRepository by lazy { OfflineFocusRepository(applicationContext, database) }
    private val notifier by lazy { TinyVowNotifier(applicationContext) }
    @Volatile private var overlayPalette: OverlayPalette = overlayPaletteForSeed(DefaultThemeSeed)
    @Volatile private var accessibilityDisclosureAccepted: Boolean = false

    // 积分积累状态
    private var lastPackageForPoints: String? = null
    private var lastUpdateElapsedRealtime: Long = 0L
    private var lastCheckedPackage: String? = null
    private var lastCheckElapsedRealtime: Long = 0L
    private var lastBlockedPackage: String? = null
    private var lastBlockElapsedRealtime: Long = 0L
    private val fastBlockCache = java.util.concurrent.ConcurrentHashMap<String, Pair<com.rrrrz.tinyvow.domain.limit.GroupExceededResult, Long>>()
    
    data class EncourageGroupCache(
        val groupName: String,
        val limitMinutes: Int,
        val pointsPerMinute: Double,
        val usageMs: Long,
        val packages: List<String>
    )
    private var encourageAppsCache: List<EncourageGroupCache> = emptyList()
    private var overlayPackageName: String? = null
    private var emergencyUnlockConsumedPackage: String? = null
    private var debugReceiverRegistered: Boolean = false
    private var lockScreenReceiverRegistered: Boolean = false
    @Volatile private var lastLockScreenTimerCandidatePackage: String? = null
    @Volatile private var lastLockScreenTimerCandidateAtMillis: Long = 0L
    @Volatile private var activeLockScreenTimerPackage: String? = null
    private val debugOverlayReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action != ACTION_DEBUG_SHOW_TEST_OVERLAY || !BuildConfig.DEBUG) return
                if (!accessibilityDisclosureAccepted) return
                serviceScope.launch(Dispatchers.Main) {
                    removeBlockOverlay()
                    showBlockOverlay(
                        packageName = DEBUG_OVERLAY_PACKAGE_NAME,
                        groupId = DEBUG_OVERLAY_GROUP_ID,
                        groupName = AppText.t("lab_block_overlay_preview_group_name"),
                        exceededMillis = DEBUG_OVERLAY_EXCEEDED_MILLIS,
                        encourageGroups = emptyList(),
                        canUseEmergencyUnlock = false,
                    )
                }
            }
        }
    private val lockScreenStateReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_ON,
                    Intent.ACTION_SCREEN_OFF,
                    Intent.ACTION_USER_PRESENT,
                    -> serviceScope.launch(Dispatchers.IO) {
                        syncLockScreenTimerState(foregroundPackageName = null)
                    }
                }
            }
        }

    override fun onServiceConnected() {
        super.onServiceConnected()
        AppText.attach(this)
        enforcer = GroupLimitEnforcer(applicationContext)
        pointsRepository = PointsRepository(applicationContext, database)
        appLimitRepository = AppLimitRepository(applicationContext, database)

        serviceScope.launch {
            BusinessDay.updateCachedStartHour(preferences.getDayBoundaryHourOnce())
            AppText.setLanguage(preferences.getSelectedAppLanguageOnce(), this@AppLimitAccessibilityService)
        }

        // 启动定时结算协程
        startPeriodicPointsTicker()
        // 启动事件消费协程
        startEventConsumer()
        startThemeWatcher()
        startDisclosureWatcher()
        startServiceHeartbeat()
        registerDebugReceiverIfNeeded()
        registerLockScreenReceiverIfNeeded()
    }

    private fun registerDebugReceiverIfNeeded() {
        if (!BuildConfig.DEBUG || debugReceiverRegistered) return
        registerReceiver(
            debugOverlayReceiver,
            IntentFilter(ACTION_DEBUG_SHOW_TEST_OVERLAY),
            Context.RECEIVER_NOT_EXPORTED,
        )
        debugReceiverRegistered = true
    }

    private fun registerLockScreenReceiverIfNeeded() {
        if (lockScreenReceiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(lockScreenStateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        lockScreenReceiverRegistered = true
    }

    private fun startServiceHeartbeat() {
        serviceScope.launch(Dispatchers.IO) {
            while (this.isActive) {
                runCatching {
                    preferences.touchAccessibilityServiceHeartbeat()
                }.onFailure {
                    if (BuildConfig.DEBUG) {
                        Log.w(TAG, "Failed to update accessibility heartbeat", it)
                    }
                }
                kotlinx.coroutines.delay(SERVICE_HEARTBEAT_INTERVAL_MS)
            }
        }
    }

    private fun startDisclosureWatcher() {
        serviceScope.launch(Dispatchers.Default) {
            preferences.accessibilityDisclosureAccepted.collect { accepted ->
                accessibilityDisclosureAccepted = accepted
                if (!accepted) {
                    withContext(Dispatchers.Main) {
                        removeBlockOverlay()
                    }
                }
            }
        }
    }

    private fun startThemeWatcher() {
        serviceScope.launch(Dispatchers.Default) {
            combine(
                preferences.selectedThemeId,
                preferences.customThemes,
            ) { selectedThemeId, customThemes ->
                overlayPaletteForSeed(resolveThemeSeed(selectedThemeId, customThemes))
            }.collect { palette ->
                overlayPalette = palette
            }
        }
    }

    private fun startPeriodicPointsTicker() {
        serviceScope.launch(Dispatchers.IO) {
            while (this.isActive) {
                kotlinx.coroutines.delay(POINTS_TICK_INTERVAL_MS)
                val currentPackage = currentPackageForPoints()
                if (currentPackage != null) {
                    try {
                        handlePointsAccumulation(currentPackage, SystemClock.elapsedRealtime())
                    } catch (e: Exception) {
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Error in periodic points ticker", e)
                        }
                    }
                }
            }
        }
    }

    // CONFLATED Channel：只保留最新的事件对象
    private data class EventPayload(val packageName: String, val eventType: Int)
    
    private val eventChannel = kotlinx.coroutines.channels.Channel<EventPayload>(
        kotlinx.coroutines.channels.Channel.CONFLATED
    )

    private fun startEventConsumer() {
        serviceScope.launch(Dispatchers.IO) {
            for (payload in eventChannel) {
                try {
                    evaluateAndBlock(payload.packageName, payload.eventType)
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "Error evaluating package limit: ${payload.packageName}", e)
                    }
                }
            }
        }
    }

    private suspend fun evaluateAndBlock(packageName: String, eventType: Int) {
        val usagePermission = usageAccessStateChecker.getStatus()

        val result = enforcer.evaluate(packageName)
        if (result == null || result.groupType == GroupType.ENCOURAGE) {
            fastBlockCache.remove(packageName)
            // 仅仅在真正的窗口状态改变时才解除阻断，防止后台应用的边界更新导致误删除前台阻断页
            if (eventType == android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                withContext(Dispatchers.Main) {
                    // 如果在异步评估期间，用户已经切去了另一个应用(比如切回了黑名单应用)，则忽略这次瞬态移除
                    if (lastCheckedPackage == packageName) {
                        removeBlockOverlay()
                    }
                }
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

        val encourageGroupsForOverlay = loadEncourageGroupsForOverlay()
        encourageAppsCache = encourageGroupsForOverlay
        fastBlockCache[packageName] = Pair(result, blockNow)
        val hasEmergencyUnlock =
            database.rewardInventoryDao().sumQuantityByRewardType(RewardType.EMERGENCY_UNLOCK) > 0 &&
                emergencyUnlockConsumedPackage != packageName

        withContext(Dispatchers.Main) {
            // 防止瞬态切换导致错误弹窗
            if (lastCheckedPackage == packageName) {
                showBlockOverlay(
                    packageName = packageName,
                    groupId = result.groupId,
                    groupName = result.groupName,
                    exceededMillis = result.exceededMillis,
                    encourageGroups = encourageGroupsForOverlay,
                    canUseEmergencyUnlock = hasEmergencyUnlock,
                )
            }
        }
    }

    private suspend fun loadEncourageGroupsForOverlay(): List<EncourageGroupCache> =
        try {
            database.appGroupDao()
                .getAllGroupsSync()
                .filter { it.type == GroupType.ENCOURAGE && it.encourageMetric == EncourageMetric.APP_USAGE }
                .mapNotNull { group ->
                    val packages = database.crossRefDao().getPackageNamesForGroupSync(group.id)
                    if (packages.isEmpty()) {
                        null
                    } else {
                        val usage = packages.sumOf { packageName ->
                            usageRepository.getUsageInPeriod(packageName, group.limitPeriod, GroupType.ENCOURAGE)
                        }
                        EncourageGroupCache(
                            groupName = group.name,
                            limitMinutes = group.limitMinutes,
                            pointsPerMinute = group.pointsPerMinute,
                            usageMs = usage,
                            packages = packages,
                        )
                    }
                }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Failed to list ENCOURAGE apps", e)
            }
            encourageAppsCache
        }

    private suspend fun recordBlockEvent(
        packageName: String,
        result: GroupExceededResult,
    ) {
        val nowMillis = System.currentTimeMillis()
        val zoneId = ZoneId.systemDefault()
        val dayStartHour = preferences.getDayBoundaryHourOnce()
        database.blockEventDao().insert(
            BlockEventEntity(
                id = UUID.randomUUID().toString(),
                eventDate = ArchiveDateUtils.formatDate(ArchiveDateUtils.localDateAt(nowMillis, zoneId, dayStartHour)),
                occurredAt = nowMillis,
                packageName = packageName,
                groupId = result.groupId,
                groupNameSnapshot = result.groupName,
                exceededMillis = result.exceededMillis,
                createdAt = nowMillis,
            )
        )
    }

    private fun handleLockScreenTimerForegroundPackage(packageName: String, isOwnPackage: Boolean) {
        serviceScope.launch(Dispatchers.IO) {
            val enabledTimerApp =
                !isOwnPackage &&
                    !isSystemLockScreenPackage(packageName) &&
                    lockScreenTimerRepository.isPackageEnabled(packageName)
            if (enabledTimerApp) {
                lastLockScreenTimerCandidatePackage = packageName
                lastLockScreenTimerCandidateAtMillis = System.currentTimeMillis()
            }
            syncLockScreenTimerState(foregroundPackageName = packageName.takeUnless { isOwnPackage })
        }
    }

    private suspend fun syncLockScreenTimerState(foregroundPackageName: String?) {
        val nowMillis = System.currentTimeMillis()
        if (!isDeviceInteractive()) {
            stopActiveLockScreenTimer(LockScreenTimerAppStatus.SCREEN_OFF, nowMillis)
            return
        }
        if (!isDeviceLocked()) {
            stopActiveLockScreenTimer(LockScreenTimerAppStatus.UNLOCKED, nowMillis)
            return
        }

        val foregroundTimerPackage =
            foregroundPackageName
                ?.takeUnless { isSystemLockScreenPackage(it) }
                ?.takeIf { lockScreenTimerRepository.isPackageEnabled(it) }
        val recentCandidate =
            lastLockScreenTimerCandidatePackage
                ?.takeIf { nowMillis - lastLockScreenTimerCandidateAtMillis <= LOCK_SCREEN_TIMER_CANDIDATE_WINDOW_MS }
                ?.takeIf { lockScreenTimerRepository.isPackageEnabled(it) }
        val targetPackage =
            foregroundTimerPackage
                ?: activeLockScreenTimerPackage
                ?: recentCandidate

        if (targetPackage == null) {
            stopActiveLockScreenTimer(LockScreenTimerAppStatus.IDLE, nowMillis)
            return
        }

        val currentActive = activeLockScreenTimerPackage
        if (currentActive == targetPackage) {
            lockScreenTimerRepository.startLockScreenTimer(targetPackage, nowMillis)
            return
        }
        if (currentActive != null) {
            lockScreenTimerRepository.stopLockScreenTimer(
                packageName = currentActive,
                status = LockScreenTimerAppStatus.IDLE,
                nowMillis = nowMillis,
            )
        }
        activeLockScreenTimerPackage = targetPackage
        lockScreenTimerRepository.startLockScreenTimer(targetPackage, nowMillis)
    }

    private suspend fun stopActiveLockScreenTimer(
        status: LockScreenTimerAppStatus,
        nowMillis: Long = System.currentTimeMillis(),
    ) {
        val packageName = activeLockScreenTimerPackage ?: return
        lockScreenTimerRepository.stopLockScreenTimer(
            packageName = packageName,
            status = status,
            nowMillis = nowMillis,
        )
        activeLockScreenTimerPackage = null
    }

    private fun isDeviceLocked(): Boolean =
        runCatching {
            getSystemService(KeyguardManager::class.java)?.isKeyguardLocked == true
        }.getOrDefault(false)

    private fun isDeviceInteractive(): Boolean =
        runCatching {
            getSystemService(PowerManager::class.java)?.isInteractive == true
        }.getOrDefault(true)

    private fun isSystemLockScreenPackage(packageName: String): Boolean =
        packageName == "android" ||
            packageName == "com.android.systemui" ||
            packageName == "com.google.android.systemui"

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        if (overlayPackageName != null && overlayPackageName != packageName) {
            emergencyUnlockConsumedPackage = null
        }

        // Tiny Vow 自身不赚积分，但需要触发上一个应用结算
        val isOwnPackage = packageName == this.packageName

        val isWindowSwitchEvent =
            event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED
        val isFocusForegroundEvent = event.eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED

        // 限额和积分只走窗口切换事件；专注恢复额外接收焦点事件兜底。
        if (!isWindowSwitchEvent && !isFocusForegroundEvent) return

        if (!accessibilityDisclosureAccepted) {
            removeBlockOverlay()
            return
        }

        if (isWindowSwitchEvent) {
            handleLockScreenTimerForegroundPackage(packageName, isOwnPackage)
        }

        val now = SystemClock.elapsedRealtime()

        if (isDeviceInteractive() && !isDeviceLocked()) {
            serviceScope.launch(Dispatchers.IO) {
                offlineFocusRepository.handleForegroundPackageForFocus(
                    packageName = packageName,
                    ownPackageName = this@AppLimitAccessibilityService.packageName,
                )
            }
        }

        if (!isWindowSwitchEvent) {
            return
        }

        // 0. 零延迟（Fast-Path）阻断拦截，消除后台热启动时的白屏/闪透
        val fastBlock = fastBlockCache[packageName]
        if (fastBlock != null && now - fastBlock.second < 15_000L) {
            showBlockOverlay(
                packageName = packageName,
                groupId = fastBlock.first.groupId,
                groupName = fastBlock.first.groupName,
                exceededMillis = fastBlock.first.exceededMillis,
                encourageGroups = encourageAppsCache,
                canUseEmergencyUnlock = emergencyUnlockConsumedPackage != packageName,
            )
        }

        // 1. 积分累加逻辑：当包切换时，结算上个包的积分
        handlePointsAccumulation(
            packageName = packageName.takeUnless { isOwnPackage },
            now = now,
        )

        // 只跳过 Tiny Vow 的限额检查；上一个应用已在上面结算
        if (isOwnPackage) {
            return
        }

        // 2. 限额评估逻辑
        // 高频事件防抖
        if (packageName == lastCheckedPackage && now - lastCheckElapsedRealtime < CHECK_DEBOUNCE_MS) {
            return
        }
        lastCheckedPackage = packageName
        lastCheckElapsedRealtime = now

        // 发送到 CONFLATED Channel
        eventChannel.trySend(EventPayload(packageName, event.eventType))
    }

    @Synchronized
    private fun handlePointsAccumulation(packageName: String?, now: Long) {
        val oldPackage = lastPackageForPoints
        
        if (oldPackage == null) {
            // 第一次记录
            if (packageName != null) {
                lastPackageForPoints = packageName
                lastUpdateElapsedRealtime = now
            }
            return
        }

        if (oldPackage != packageName) {
            // 包名切换：结算旧包的最后一段 deltas
            val durationMs = now - lastUpdateElapsedRealtime
            if (durationMs > MIN_CREDIT_DURATION_MS) {
                creditPoints(oldPackage, durationMs)
            }
            // 重置状态
            lastPackageForPoints = packageName
            lastUpdateElapsedRealtime = if (packageName != null) now else 0L
        } else {
            // 同一个包：每隔 1 分钟结算一次
            val durationMs = now - lastUpdateElapsedRealtime
            if (durationMs >= POINTS_TICK_INTERVAL_MS) {
                creditPoints(oldPackage, durationMs)
                lastUpdateElapsedRealtime = now
            }
        }
    }

    @Synchronized
    private fun currentPackageForPoints(): String? = lastPackageForPoints

    private fun creditPoints(packageName: String, durationMs: Long) {
        serviceScope.launch(Dispatchers.IO) {
            val groupIds = database.crossRefDao().getGroupIdsForPackageSync(packageName)
            val nowMillis = System.currentTimeMillis()
            if (packageName == WEREAD_PACKAGE_NAME &&
                specialAppUsageRepository.shouldUseSyncBasedEncouragePoints()
            ) {
                return@launch
            }
            for (gid in groupIds) {
                val group = database.appGroupDao().getGroupByIdSync(gid) ?: continue
                if (group.type == GroupType.ENCOURAGE &&
                    group.encourageMetric == EncourageMetric.APP_USAGE &&
                    group.pointsPerMinute > 0
                ) {
                    val pointsMultiplier = currentEncouragePointsMultiplier(group.id, nowMillis)
                    // 基础每分钟积分
                    val pointsEarned = calculateUsageEarnedPoints(durationMs, group.pointsPerMinute) * pointsMultiplier
                    pointsRepository.recordUsageEarn(group, pointsEarned)
                    
                    // 检查是否达成今日目标大奖
                    checkAndGrantBonus(group, pointsMultiplier)
                }
            }
        }
    }

    private suspend fun checkAndGrantBonus(group: AppGroupEntity, pointsMultiplier: Double) {
        val nowMillis = System.currentTimeMillis()
        val todayStart = getStartOfDay(nowMillis)
        
        if (group.lastBonusAt >= todayStart) {
            return // 今天已经领过了
        }

        // 获取该分组今日总用量
        val packages = database.crossRefDao().getPackageNamesForGroupSync(group.id)

        var totalTodayUsageMs = 0L
        for (pkg in packages) {
            totalTodayUsageMs +=
                if (pkg == WEREAD_PACKAGE_NAME &&
                    specialAppUsageRepository.shouldUseSyncBasedEncouragePoints() &&
                    specialAppUsageRepository.getWeReadConfig().lastSuccessAt <= 0L
                ) {
                    0L
                } else {
                    usageRepository.getUsageInPeriod(pkg, com.rrrrz.tinyvow.data.db.LimitPeriod.DAILY, GroupType.ENCOURAGE)
                }
        }

        val targetMs = group.limitMinutes * 60_000L
        if (totalTodayUsageMs >= targetMs) {
            // 达成目标！发放奖励：目标分钟 * 鼓励金比例
            val bonusPoints = calculateTargetBonusPoints(group.limitMinutes, group.pointsPerMinute) * pointsMultiplier
            pointsRepository.recordTargetBonusEarn(group, bonusPoints)
            
            // 更新数据库标记
            database.appGroupDao().insertGroup(group.copy(lastBonusAt = nowMillis))
            notifyEncourageTargetCompleted(group, nowMillis)
            
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "Group ${group.name} reached daily target! Bonus $bonusPoints PT granted.")
            }
        }
    }

    private suspend fun notifyEncourageTargetCompleted(group: AppGroupEntity, nowMillis: Long) {
        if (!preferences.getNotificationRemindersEnabledOnce()) return
        val today = ArchiveDateUtils.localDateAt(nowMillis, ZoneId.systemDefault(), preferences.getDayBoundaryHourOnce())
        val reminderKey = ReminderPolicy.encourageCompletedReminderKey(today, group.id)
        if (reminderKey in preferences.getSentReminderKeysOnce()) return
        notifier.notifyEncourageCompleted(groupId = group.id, groupName = group.name)
        preferences.addSentReminderKey(reminderKey)
    }

    private suspend fun currentEncouragePointsMultiplier(groupId: String, nowMillis: Long): Double {
        val effect =
            database
                .activeRewardEffectDao()
                .getActiveForGroup(groupId, nowMillis)
                .firstOrNull { it.effectType == RewardType.DOUBLE_POINTS_DAY }
                ?: return 1.0
        return parseRewardPayload(effect.payloadJson).pointsMultiplier.coerceAtLeast(1.0)
    }

    private suspend fun getStartOfDay(millis: Long): Long {
        val zoneId = ZoneId.systemDefault()
        val dayStartHour = preferences.getDayBoundaryHourOnce()
        val businessDate = ArchiveDateUtils.localDateAt(millis, zoneId, dayStartHour)
        return ArchiveDateUtils.startOfDayMillis(businessDate, zoneId, dayStartHour)
    }

    // ──────── Overlay ────────

    private var blockView: android.view.View? = null

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density + 0.5f).toInt()

    private fun roundedBackground(
        color: String,
        radiusDp: Int,
        strokeColor: String? = null,
        strokeDp: Int = 0,
    ): android.graphics.drawable.GradientDrawable =
        android.graphics.drawable.GradientDrawable().apply {
            setColor(color.toColorInt())
            cornerRadius = dp(radiusDp).toFloat()
            if (strokeColor != null && strokeDp > 0) {
                setStroke(dp(strokeDp), strokeColor.toColorInt())
            }
        }

    private fun roundedBackground(
        color: Int,
        radiusDp: Int,
        strokeColor: Int? = null,
        strokeDp: Int = 0,
    ): android.graphics.drawable.GradientDrawable =
        android.graphics.drawable.GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radiusDp).toFloat()
            if (strokeColor != null && strokeDp > 0) {
                setStroke(dp(strokeDp), strokeColor)
            }
        }

    private data class OverlayPalette(
        val primary: Int,
        val secondary: Int,
        val tertiary: Int,
        val background: Int,
        val surface: Int,
        val surfaceContainer: Int,
        val onSurface: Int,
        val onSurfaceVariant: Int,
        val outline: Int,
    )

    private fun overlayPaletteForSeed(seed: com.rrrrz.tinyvow.ui.theme.ThemeSeed): OverlayPalette {
        val tokens = themeTokensFromSeed(seed)
        val scheme = tokens.colorScheme
        return OverlayPalette(
            primary = tokens.base.toArgb(),
            secondary = tokens.control.toArgb(),
            tertiary = tokens.encourage.toArgb(),
            background = scheme.background.toArgb(),
            surface = scheme.surface.toArgb(),
            surfaceContainer = scheme.surfaceContainerHigh.toArgb(),
            onSurface = scheme.onSurface.toArgb(),
            onSurfaceVariant = scheme.onSurfaceVariant.toArgb(),
            outline = scheme.outlineVariant.toArgb(),
        )
    }

    private fun withAlpha(color: Int, alpha: Float): Int =
        (color and 0x00FFFFFF) or ((alpha.coerceIn(0f, 1f) * 255).toInt() shl 24)

    private fun blendColor(foreground: Int, background: Int, ratio: Float): Int {
        val inverse = 1f - ratio.coerceIn(0f, 1f)
        return android.graphics.Color.rgb(
            (android.graphics.Color.red(foreground) * ratio + android.graphics.Color.red(background) * inverse).toInt(),
            (android.graphics.Color.green(foreground) * ratio + android.graphics.Color.green(background) * inverse).toInt(),
            (android.graphics.Color.blue(foreground) * ratio + android.graphics.Color.blue(background) * inverse).toInt(),
        )
    }

    private fun readableAccent(color: Int): Int {
        val luminance = (0.299 * android.graphics.Color.red(color) +
            0.587 * android.graphics.Color.green(color) +
            0.114 * android.graphics.Color.blue(color)) / 255.0
        return if (luminance > 0.72) blendColor(color, android.graphics.Color.BLACK, 0.62f) else color
    }

    @SuppressLint("SetTextI18n")
    private fun showBlockOverlay(
        packageName: String,
        groupId: String,
        groupName: String,
        exceededMillis: Long,
        encourageGroups: List<EncourageGroupCache>,
        canUseEmergencyUnlock: Boolean,
    ) {
        if (blockView != null) return
        overlayPackageName = packageName

        val palette = overlayPalette
        val windowManager = getSystemService(WINDOW_SERVICE) as android.view.WindowManager
        val params = android.view.WindowManager.LayoutParams(
            android.view.WindowManager.LayoutParams.MATCH_PARENT,
            android.view.WindowManager.LayoutParams.MATCH_PARENT,
            android.view.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            android.graphics.PixelFormat.TRANSLUCENT
        )

        val layout = android.widget.FrameLayout(this).apply {
            setBackgroundColor(palette.background)
            isClickable = true
            isFocusable = true
        }

        val scrollView = android.widget.ScrollView(this).apply {
            isFillViewport = true
            isVerticalScrollBarEnabled = false
            overScrollMode = android.view.View.OVER_SCROLL_NEVER
            setPadding(dp(20), dp(18), dp(20), dp(92))
            clipToPadding = false
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val content = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            setPadding(0, 0, 0, dp(12))
            minimumHeight = resources.displayMetrics.heightPixels - dp(102)
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val iconShell = android.widget.FrameLayout(this).apply {
            background = roundedBackground(palette.surface, 26, palette.outline, 1)
            elevation = 0f
            layoutParams = android.widget.LinearLayout.LayoutParams(dp(84), dp(84)).apply {
                bottomMargin = dp(14)
            }
        }

        val iconView = android.widget.ImageView(this).apply {
            setImageResource(com.rrrrz.tinyvow.R.mipmap.ic_launcher)
            scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            layoutParams = android.widget.FrameLayout.LayoutParams(dp(54), dp(54), android.view.Gravity.CENTER)
        }
        iconShell.addView(iconView)

        val totalMinutes = exceededMillis / 60_000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val exceededText = if (hours > 0) AppText.t("duration_value_h_value_min", hours, minutes) else AppText.t("duration_value_min", minutes)

        val title = android.widget.TextView(this).apply {
            text = AppText.t("accessibility_take_a_deep_breath_right_now")
            textSize = 22f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(palette.onSurface)
            gravity = android.view.Gravity.CENTER
            includeFontPadding = false
            setPadding(0, 0, 0, dp(8))
        }

        val body = android.widget.TextView(this).apply {
            text = AppText.t("accessibility_you_have_used_value_today_over_by_value", groupName, exceededText)
            textSize = 15f
            setLineSpacing(dp(3).toFloat(), 1.15f)
            setTextColor(palette.onSurfaceVariant)
            gravity = android.view.Gravity.CENTER
            includeFontPadding = true
            setPadding(dp(8), 0, dp(8), dp(18))
        }

        val btnPrimaryBg = roundedBackground(palette.secondary, 18)
        val btnUnlock = android.widget.Button(this).apply {
            text = AppText.t("redeem_overlay_use_emergency_unlock")
            background = roundedBackground(palette.surface, 18, palette.outline, 1)
            setTextColor(palette.onSurface)
            isAllCaps = false
            textSize = 15f
            minHeight = 0
            minWidth = 0
            visibility = if (canUseEmergencyUnlock) android.view.View.VISIBLE else android.view.View.GONE
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                dp(48)
            ).apply {
                gravity = android.view.Gravity.BOTTOM
                leftMargin = dp(20)
                rightMargin = dp(20)
                bottomMargin = dp(84)
            }
            setOnClickListener {
                isEnabled = false
                serviceScope.launch {
                    when (appLimitRepository.consumeEmergencyUnlockForBlockedGroup(groupId)) {
                        is UseRewardResult.Success -> {
                            emergencyUnlockConsumedPackage = packageName
                            val reevaluated = enforcer.evaluate(packageName)
                            withContext(Dispatchers.Main) {
                                removeBlockOverlay()
                                if (reevaluated != null) {
                                    showBlockOverlay(
                                        packageName = packageName,
                                        groupId = reevaluated.groupId,
                                        groupName = reevaluated.groupName,
                                        exceededMillis = reevaluated.exceededMillis,
                                        encourageGroups = encourageAppsCache,
                                        canUseEmergencyUnlock = false,
                                    )
                                }
                            }
                        }
                        else -> {
                            withContext(Dispatchers.Main) {
                                isEnabled = true
                            }
                        }
                    }
                }
            }
        }
        val btnGoHome = android.widget.Button(this).apply {
            text = getString(com.rrrrz.tinyvow.R.string.block_overlay_go_home)
            background = btnPrimaryBg
            setTextColor(android.graphics.Color.WHITE)
            isAllCaps = false
            textSize = 16f
            minHeight = 0
            minWidth = 0
            elevation = dp(3).toFloat()
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            ).apply {
                gravity = android.view.Gravity.BOTTOM
                leftMargin = dp(20)
                rightMargin = dp(20)
                bottomMargin = dp(24)
            }
            setOnClickListener {
                removeBlockOverlay()
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            }
        }

        content.addView(iconShell)
        content.addView(title)
        content.addView(body)

        if (encourageGroups.isNotEmpty()) {
            val groupsContainer = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(16)
                }
            }
            
            encourageGroups.chunked(2).forEach { rowGroups ->
                val row = android.widget.LinearLayout(this).apply {
                    orientation = android.widget.LinearLayout.HORIZONTAL
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = dp(10)
                    }
                }

                rowGroups.forEachIndexed { index, group ->
                    val card = createGroupCardView(group).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            0,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        ).apply {
                            marginEnd = if (index == 0) dp(8) else 0
                            marginStart = if (index == 1) dp(8) else 0
                        }
                    }
                    row.addView(card)
                }

                if (rowGroups.size == 1) {
                    row.addView(android.widget.Space(this).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            0,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        ).apply {
                            marginStart = dp(8)
                        }
                    })
                }

                groupsContainer.addView(row)
            }
            content.addView(groupsContainer)
        }

        scrollView.addView(content)
        layout.addView(scrollView)
        layout.addView(btnUnlock)
        layout.addView(btnGoHome)

        try {
            windowManager.addView(layout, params)
            blockView = layout
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "Overlay added successfully!")
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Failed to add overlay", e)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createGroupCardView(group: EncourageGroupCache): android.view.View {
        val palette = overlayPalette
        val encourageAccent = readableAccent(palette.tertiary)
        val encourageContainer = blendColor(palette.tertiary, palette.surface, 0.16f)
        val iconContainer = blendColor(palette.tertiary, palette.surface, 0.10f)
        val card = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            background = roundedBackground(palette.surface, 18, palette.outline, 1)
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(dp(10), dp(10), dp(10), dp(10))
            elevation = 0f
        }

        // --- Header (Name + Rate) ---
        val header = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(8)
            }
            
            val titleText = android.widget.TextView(this@AppLimitAccessibilityService).apply {
                text = group.groupName
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(palette.onSurface)
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                )
            }
            
            val formattedRate = if (group.pointsPerMinute % 1.0 == 0.0) group.pointsPerMinute.toInt().toString() else group.pointsPerMinute.toString()
            val rateText = android.widget.TextView(this@AppLimitAccessibilityService).apply {
                text = AppText.t("accessibility_value_pts_min", formattedRate)
                textSize = 10f
                setTextColor(encourageAccent)
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = android.view.Gravity.CENTER_VERTICAL or android.view.Gravity.END
                background = roundedBackground(encourageContainer, 10)
                setPadding(dp(6), dp(3), dp(6), dp(3))
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            
            addView(titleText)
            addView(rateText)
        }

        // --- Progress Info ---
        val usedMins = group.usageMs / 60_000
        val targetMins = group.limitMinutes
        val progressText = android.widget.TextView(this).apply {
            text = AppText.t("accessibility_value_minutes_done_value_minute_target", usedMins, targetMins)
            textSize = 10f
            setTextColor(palette.onSurfaceVariant)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(8)
            }
        }
        
        val progressBar = android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = if (targetMins > 0) targetMins else 100
            progress = usedMins.toInt()
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, dp(5)
            ).apply {
                bottomMargin = dp(10)
            }
            progressTintList = android.content.res.ColorStateList.valueOf(encourageAccent)
            progressBackgroundTintList = android.content.res.ColorStateList.valueOf(encourageContainer)
        }

        // --- App Row ---
        val scroller = android.widget.HorizontalScrollView(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            isHorizontalScrollBarEnabled = false
            overScrollMode = android.view.View.OVER_SCROLL_NEVER
            clipToPadding = false
        }
        
        val appsLayout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val pm = packageManager
        for (pkg in group.packages) {
            try {
                val icon = pm.getApplicationIcon(pkg)
                val label = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0))
                
                val appItem = android.widget.LinearLayout(this).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                    gravity = android.view.Gravity.CENTER
                    background = roundedBackground(iconContainer, 14, palette.surface, 1)
                    setPadding(dp(6), dp(6), dp(6), dp(6))
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        dp(46), dp(46)
                    ).apply {
                        marginEnd = dp(8)
                    }
                    
                    val iv = android.widget.ImageView(this@AppLimitAccessibilityService).apply {
                        setImageDrawable(icon)
                        contentDescription = label
                        scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                        layoutParams = android.widget.LinearLayout.LayoutParams(dp(34), dp(34))
                    }
                    
                    addView(iv)
                    
                    setOnClickListener {
                        removeBlockOverlay()
                        val intent = pm.getLaunchIntentForPackage(pkg)?.apply {
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        if (intent != null) {
                            try {
                                startActivity(intent)
                            } catch (e: Exception) {
                                if (BuildConfig.DEBUG) {
                                    Log.e(TAG, "Failed to launch encourage app: $pkg", e)
                                }
                            }
                        }
                    }
                }
                appsLayout.addView(appItem)
            } catch (e: Exception) {
                // Ignore missing apps
            }
        }
        
        scroller.addView(appsLayout)
        
        card.addView(header)
        card.addView(progressText)
        card.addView(progressBar)
        card.addView(scroller)

        return card
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
            overlayPackageName = null
        }
    }

    override fun onInterrupt() {
        removeBlockOverlay()
    }

    override fun onDestroy() {
        removeBlockOverlay()
        runCatching {
            runBlocking(Dispatchers.IO) {
                stopActiveLockScreenTimer(LockScreenTimerAppStatus.IDLE)
            }
        }
        if (debugReceiverRegistered) {
            runCatching { unregisterReceiver(debugOverlayReceiver) }
            debugReceiverRegistered = false
        }
        if (lockScreenReceiverRegistered) {
            runCatching { unregisterReceiver(lockScreenStateReceiver) }
            lockScreenReceiverRegistered = false
        }
        eventChannel.close()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "AppLimitService"
        const val ACTION_DEBUG_SHOW_TEST_OVERLAY = "com.rrrrz.tinyvow.action.DEBUG_SHOW_TEST_OVERLAY"
        /** 评估防抖窗口：缩短以提升响应速度 */
        private const val CHECK_DEBOUNCE_MS = 300L
        /** 阻断弹窗防抖窗口：缩短以确保点击关闭后能较快再次生效 */
        private const val BLOCK_DEBOUNCE_MS = 500L
        private const val POINTS_TICK_INTERVAL_MS = 60_000L
        private const val MIN_CREDIT_DURATION_MS = 1_000L
        private const val SERVICE_HEARTBEAT_INTERVAL_MS = 60_000L
        private const val LOCK_SCREEN_TIMER_CANDIDATE_WINDOW_MS = 2L * 60L * 60_000L
        private const val DEBUG_OVERLAY_GROUP_ID = "debug-overlay-group"
        private const val DEBUG_OVERLAY_PACKAGE_NAME = "debug.overlay.package"
        private const val DEBUG_OVERLAY_EXCEEDED_MILLIS = 27L * 60_000L

        fun debugShowTestOverlayIntent(context: Context): Intent =
            Intent(ACTION_DEBUG_SHOW_TEST_OVERLAY).setPackage(context.packageName)
    }
}
