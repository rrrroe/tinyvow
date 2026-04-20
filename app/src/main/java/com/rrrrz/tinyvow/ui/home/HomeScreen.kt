package com.rrrrz.tinyvow.ui.home

import android.Manifest
import android.content.Intent
import android.provider.Settings
import android.os.PowerManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.data.accessibility.AccessibilityServiceStateChecker
import com.rrrrz.tinyvow.data.apps.InstalledAppRepository
import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.notification.NotificationPermissionChecker
import com.rrrrz.tinyvow.data.repository.AppGroupWithApps
import com.rrrrz.tinyvow.data.repository.AppLimitRepository
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.usage.UsageAccessStateChecker
import com.rrrrz.tinyvow.data.usage.UsageAccessStatus
import com.rrrrz.tinyvow.service.block.AppLimitAccessibilityService
import com.rrrrz.tinyvow.ui.theme.TinyVowTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import androidx.compose.animation.*
import java.time.LocalDate
import com.rrrrz.tinyvow.data.usage.UsageStatsUsageRepository
import com.rrrrz.tinyvow.data.usage.UsageRepository

import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.db.AchievementEntity
import com.rrrrz.tinyvow.data.db.AchievementTier
import com.rrrrz.tinyvow.data.db.RedemptionEntity
import com.rrrrz.tinyvow.data.db.RedemptionHistoryEntity
import com.rrrrz.tinyvow.ui.rewards.RedeemScreen
import com.rrrrz.tinyvow.ui.rewards.AchievementScreen

enum class Screen { HOME, REWARDS, STATS, ME, LABORATORY }

@Composable
fun RewardsHome(
    userPoints: Double,
    achievements: List<AchievementEntity>,
    rewards: List<RedemptionEntity>,
    groups: List<AppGroupWithApps>,
    redemptionHistory: List<RedemptionHistoryEntity>,
    onRedeem: (RedemptionEntity, String?) -> Unit,
    onAddReward: (String, Int, Int, String) -> Unit,
    onUpdateReward: (RedemptionEntity) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("成就殿堂", "积分商城")
    
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }
        
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> AchievementScreen(
                    achievements = achievements,
                    currentPoints = userPoints,
                    onBack = onBack
                )
                1 -> RedeemScreen(
                    userPoints = userPoints,
                    rewards = rewards,
                    groups = groups,
                    redemptionHistory = redemptionHistory,
                    onRedeem = onRedeem,
                    onAddReward = onAddReward,
                    onUpdateReward = onUpdateReward,
                    onBack = onBack
                )
            }
        }
    }
}

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    
    val accessibilityServiceStateChecker = remember(context) { AccessibilityServiceStateChecker(context) }
    val checker = remember(context) { UsageAccessStateChecker(context) }
    val appRepository = remember(context) { InstalledAppRepository(context) }
    val preferences = remember(context) { ManagedAppPreferences(context) }
    val notificationPermissionChecker = remember(context) { NotificationPermissionChecker(context) }
    val powerManager = remember(context) { context.getSystemService(android.content.Context.POWER_SERVICE) as PowerManager }
    var isIgnoringBattery by remember { mutableStateOf(powerManager.isIgnoringBatteryOptimizations(context.packageName)) }
    
    val database = remember(context) { AppDatabase.getDatabase(context) }
    val appLimitRepository = remember(database) { AppLimitRepository(database) }
    val usageRepository = remember(context) { UsageStatsUsageRepository(context) }
    
    val groupsWithApps by appLimitRepository.getAllGroupsWithApps().collectAsState(initial = emptyList())
    val userPoints by preferences.userPoints.collectAsState(initial = 0.0)
    val todayPoints by preferences.todayPoints.collectAsState(initial = 0.0)
    val selectedTheme by preferences.selectedTheme.collectAsState(initial = 0)
    val rewards by appLimitRepository.getAllRewards().collectAsState(initial = emptyList())
    val achievements by appLimitRepository.getAllAchievements().collectAsState(initial = emptyList())
    val redemptionHistory by appLimitRepository.getRedemptionHistory().collectAsState(initial = emptyList())

    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }
    var usageAccessStatus by remember { mutableStateOf(checker.getStatus()) }
    var accessibilityServiceEnabled by remember {
        mutableStateOf(accessibilityServiceStateChecker.isEnabled(AppLimitAccessibilityService::class.java))
    }
    var notificationPermissionGranted by remember {
        mutableStateOf(notificationPermissionChecker.isGranted())
    }
    
    var installedApps by remember { mutableStateOf<List<ManagedApp>>(emptyList()) }
    var isLoadingApps by remember { mutableStateOf(false) }

    var showYesterdaySummary by remember { mutableStateOf(false) }
    var yesterdaySavedMinutes by remember { mutableIntStateOf(0) }

    val isAutoStartDismissed by preferences.isAutoStartDismissed.collectAsState(initial = false)

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        notificationPermissionGranted = notificationPermissionChecker.isGranted()
    }

    DisposableEffect(lifecycleOwner, checker) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                usageAccessStatus = checker.getStatus()
                accessibilityServiceEnabled =
                    accessibilityServiceStateChecker.isEnabled(AppLimitAccessibilityService::class.java)
                notificationPermissionGranted = notificationPermissionChecker.isGranted()
                isIgnoringBattery = powerManager.isIgnoringBatteryOptimizations(context.packageName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        appLimitRepository.clearExpiredBonusTime(System.currentTimeMillis())
        
        // 每日总结逻辑
        val today = LocalDate.now().toString()
        val lastShownFlow = preferences.lastSummaryShownDate
        val lastShown = lastShownFlow.first()
        if (lastShown != today) {
            val groups = appLimitRepository.getAllGroupsWithApps().first()
            var totalSavedMillis = 0L
            for (groupWithApps in groups) {
                if (groupWithApps.group.type == GroupType.CONTROL) {
                    var groupUsage = 0L
                    for (pkg in groupWithApps.packageNames) {
                        groupUsage += usageRepository.getYesterdayUsageMillis(pkg)
                    }
                    val limitMillis = groupWithApps.group.limitMinutes * 60_000L
                    if (groupUsage < limitMillis) {
                        totalSavedMillis += (limitMillis - groupUsage)
                    }
                }
            }
            if (totalSavedMillis > 0) {
                yesterdaySavedMinutes = (totalSavedMillis / 60_000).toInt()
                showYesterdaySummary = true
            }
            preferences.setLastSummaryShownDate(today)
        }
    }

    LaunchedEffect(usageAccessStatus) {
        if (usageAccessStatus == UsageAccessStatus.GRANTED) {
            isLoadingApps = true
            installedApps = appRepository.getAllInstalledApps()
            isLoadingApps = false
        } else {
            installedApps = emptyList()
            isLoadingApps = false
        }
    }

    LaunchedEffect(rewards) {
        if (rewards.isEmpty()) {
            appLimitRepository.seedInitialData()
        }
    }

    // 仅在应用首次启动时检查一次成就，避免每次积分变化都触发高代价 DB 扫描
    LaunchedEffect(Unit) {
        appLimitRepository.checkAchievements(preferences.userPoints.first())
    }

    var newlyUnlockedAchievement by remember { mutableStateOf<AchievementEntity?>(null) }
    LaunchedEffect(Unit) {
        appLimitRepository.newAchievementsAction.collectLatest { achievement ->
            newlyUnlockedAchievement = achievement
            kotlinx.coroutines.delay(5000) // 显示 5 秒
            newlyUnlockedAchievement = null
        }
    }

    LaunchedEffect(Unit) {
        appLimitRepository.redemptionEvents.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    if (currentScreen != Screen.HOME) {
        BackHandler { currentScreen = Screen.HOME }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            if (currentScreen != Screen.LABORATORY) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val screens = listOf(
                        Triple(Screen.HOME, "首页", Icons.Default.Home),
                        Triple(Screen.REWARDS, "奖励", Icons.Default.CardGiftcard),
                        Triple(Screen.STATS, "战报", Icons.Default.BarChart),
                        Triple(Screen.ME, "我的", Icons.Default.Person)
                    )
                    screens.forEach { (screen, label, icon) ->
                        NavigationBarItem(
                            selected = currentScreen == screen || (screen == Screen.REWARDS && (currentScreen == Screen.REWARDS)),
                            onClick = { currentScreen = screen },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                Screen.HOME -> {
                    HomeScreen(
                        usageAccessStatus = usageAccessStatus,
                        accessibilityServiceEnabled = accessibilityServiceEnabled,
                        notificationPermissionGranted = notificationPermissionGranted,
                        isIgnoringBattery = isIgnoringBattery,
                        installedApps = installedApps,
                        groupsWithApps = groupsWithApps,
                        userPoints = userPoints,
                        todayPoints = todayPoints,
                        isLoadingApps = isLoadingApps,
                        onNavigateToRedeem = { currentScreen = Screen.REWARDS }, // Placeholder
                        onNavigateToAchievements = { currentScreen = Screen.REWARDS },
                        onOpenUsageAccessSettings = {
                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        },
                        onOpenAccessibilitySettings = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        },
                        onRequestNotificationPermission = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onOpenAutoStartSettings = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = android.net.Uri.fromParts("package", context.packageName, null)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        },
                        onRequestBatteryOptimization = {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = android.net.Uri.parse("package:${context.packageName}")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        },
                        isAutoStartDismissed = isAutoStartDismissed,
                        onSetAutoStartDismissed = {
                            coroutineScope.launch { preferences.setAutoStartDismissed(true) }
                        },
                        onSaveGroup = { id, name, limit, type, period, pts, pkgs ->
                            coroutineScope.launch {
                                val groupId = appLimitRepository.createOrUpdateGroup(id, name, limit, type, period, pts)
                                appLimitRepository.updateGroupApps(groupId, pkgs)
                            }
                        },
                        onDeleteGroup = { id ->
                            coroutineScope.launch { appLimitRepository.deleteGroup(id) }
                        },
                        appLimitRepository = appLimitRepository,
                        modifier = modifier,
                    )
                }
                Screen.REWARDS -> {
                    RewardsHome(
                        userPoints = userPoints,
                        achievements = achievements,
                        rewards = rewards,
                        groups = groupsWithApps,
                        redemptionHistory = redemptionHistory,
                        onRedeem = { reward, gId -> 
                            coroutineScope.launch {
                                val result = appLimitRepository.redeemReward(reward, gId)
                                if (result != null) {
                                    preferences.addUserPoints(-result.pointCost.toDouble())
                                }
                            }
                        },
                        onAddReward = { name, cost, stock, desc ->
                            coroutineScope.launch { appLimitRepository.addReward(name, cost, com.rrrrz.tinyvow.data.db.RewardType.CUSTOM, stock, desc) }
                        },
                        onUpdateReward = { reward ->
                            coroutineScope.launch { appLimitRepository.updateReward(reward) }
                        },
                        onBack = { currentScreen = Screen.HOME }
                    )
                }
                Screen.STATS -> {
                    StatsRoute(
                        usageAccessStatus = usageAccessStatus,
                        groupsWithApps = groupsWithApps,
                        userPoints = userPoints,
                        todayPoints = todayPoints,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Screen.ME -> {
                    MeScreen(
                        userPoints = userPoints,
                        currentTheme = selectedTheme,
                        onSetTheme = { i -> coroutineScope.launch { preferences.setSelectedTheme(i) } },
                        onNavigateToLaboratory = { currentScreen = Screen.LABORATORY },
                        onNavigateToAchievements = { currentScreen = Screen.REWARDS },
                        onNavigateToRedeem = { currentScreen = Screen.REWARDS }
                    )
                }
                Screen.LABORATORY -> {
                    LaboratoryScreen(
                        onAddPoints = { pts -> coroutineScope.launch { preferences.addUserPoints(pts); appLimitRepository.checkAchievements(preferences.userPoints.first()) } },
                        onResetSummary = { coroutineScope.launch { preferences.setLastSummaryShownDate("reset") } },
                        onTriggerSummary = { showYesterdaySummary = true },
                        onBack = { currentScreen = Screen.ME }
                    )
                }
            }
        }
    }

    if (showYesterdaySummary) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showYesterdaySummary = false },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        androidx.compose.material.icons.Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("昨日战报", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("太棒了！昨天你凭借强大的意志力，在受控应用中省下了：")
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(12.dp)
                        ).padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (yesterdaySavedMinutes >= 60) {
                                "${yesterdaySavedMinutes / 60} 小时 ${yesterdaySavedMinutes % 60} 分钟"
                            } else {
                                "$yesterdaySavedMinutes 分钟"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("继续保持，自律即自由！", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(onClick = { showYesterdaySummary = false }) {
                    Text("我知道了")
                }
            }
        )
    }

    // ──────── 成就解锁通知横幅 ────────
    AnimatedVisibility(
        visible = newlyUnlockedAchievement != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier.fillMaxWidth()
    ) {
        newlyUnlockedAchievement?.let { achievement ->
            AchievementNotificationBanner(achievement)
        }
    }
}

@Composable
fun AchievementNotificationBanner(achievement: AchievementEntity) {
    val tierGradient = when (achievement.tier) {
        AchievementTier.LEGENDARY -> listOf(
            Color(0xFFFF6B6B), Color(0xFFFECA57), Color(0xFF48DBFB), Color(0xFFFF9FF3), Color(0xFFFF6B6B)
        )
        AchievementTier.DIAMOND -> listOf(
            Color(0xFF64B5F6), Color(0xFF90CAF9), Color(0xFFE3F2FD), Color(0xFF64B5F6)
        )
        AchievementTier.GOLD -> listOf(
            Color(0xFFFFD700), Color(0xFFFFF59D), Color(0xFFFFA500), Color(0xFFFFD700)
        )
        AchievementTier.SILVER -> listOf(
            Color(0xFFB8C5D6), Color(0xFFE2E8F0), Color(0xFF8A9BB0), Color(0xFFB8C5D6)
        )
        else -> listOf(
            Color(0xFFCD9B6B), Color(0xFFDEB887), Color(0xFF8B6F47), Color(0xFFCD9B6B)
        )
    }

    val tierLabel = when (achievement.tier) {
        AchievementTier.LEGENDARY -> "💎 传奇成就解锁"
        AchievementTier.DIAMOND -> "💫 钻石成就解锁"
        AchievementTier.GOLD -> "🥇 金阶成就解锁"
        AchievementTier.SILVER -> "🥈 银阶成就解锁"
        else -> "🥉 铜阶成就解锁"
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "banner_shine")
    val shineOffset by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shine_offset"
    )
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    var isReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isReady = true }
    
    val bounceScale by animateFloatAsState(
        targetValue = if (isReady) 1f else 0.5f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "banner_bounce"
    )

    Surface(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .padding(top = 24.dp)
            .graphicsLayer { 
                scaleX = bounceScale * (if (achievement.tier >= AchievementTier.GOLD) pulse else 1f)
                scaleY = bounceScale * (if (achievement.tier >= AchievementTier.GOLD) pulse else 1f)
            },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 12.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))) {
            // 光泽扫光效果
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (achievement.tier >= AchievementTier.GOLD) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = tierGradient,
                            start = Offset(shineOffset, 0f),
                            end = Offset(shineOffset + 150f, 150f)
                        ),
                        alpha = 0.15f
                    )
                }
            }
            
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 等级徽章
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Brush.sweepGradient(tierGradient),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // 内圈
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape),
                    )
                    Text(
                        text = achievement.iconEmoji,
                        fontSize = 28.sp,
                        modifier = Modifier.graphicsLayer {
                            scaleX = pulse
                            scaleY = pulse
                        }
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        tierLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Brush.linearGradient(tierGradient).let { Color.Unspecified } // 占位
                    )
                    Text(
                        achievement.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        achievement.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    usageAccessStatus: UsageAccessStatus,
    accessibilityServiceEnabled: Boolean,
    notificationPermissionGranted: Boolean,
    isIgnoringBattery: Boolean,
    installedApps: List<ManagedApp>,
    groupsWithApps: List<AppGroupWithApps>,
    userPoints: Double,
    todayPoints: Double,
    isLoadingApps: Boolean,
    onNavigateToRedeem: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onOpenUsageAccessSettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onOpenAutoStartSettings: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    isAutoStartDismissed: Boolean,
    onSetAutoStartDismissed: () -> Unit,
    onSaveGroup: (id: String?, name: String, limit: Int, type: GroupType, period: LimitPeriod, pts: Double, pkgs: List<String>) -> Unit,
    onDeleteGroup: (id: String) -> Unit,
    appLimitRepository: AppLimitRepository? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var usageMap by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    
    // 定时刷新各分组用量：批量查询一次 UsageStats，过滤分组汇总。这样可将 N 次 IPC 降为 1 次
    LaunchedEffect(groupsWithApps) {
        val usageRepo = UsageStatsUsageRepository(context)
        while (true) {
            val todayStart = java.time.LocalDate.now(java.time.ZoneId.systemDefault())
                .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            // 一次性获得所有包名的用量 Map
            val allUsage = usageRepo.getUsageStats(todayStart, System.currentTimeMillis())
            val newMap = mutableMapOf<String, Long>()
            groupsWithApps.forEach { groupWithApps ->
                newMap[groupWithApps.group.id] =
                    groupWithApps.packageNames.sumOf { allUsage[it] ?: 0L }
            }
            usageMap = newMap
            kotlinx.coroutines.delay(5000L) // 5秒刷新一次
        }
    }
    val usageAccessGranted = usageAccessStatus == UsageAccessStatus.GRANTED
    val statusColor = if (usageAccessGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    var showDiagnosticMenu by remember { mutableStateOf(false) }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val showCardsOnHome = !usageAccessGranted || !accessibilityServiceEnabled || !isAutoStartDismissed || !isIgnoringBattery || !notificationPermissionGranted

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 计算今日进度
                val controlGroups = groupsWithApps.filter { it.group.type == GroupType.CONTROL }
                val encourageGroups = groupsWithApps.filter { it.group.type == GroupType.ENCOURAGE }
                
                val safeVows = controlGroups.count { g -> 
                    val usage = (usageMap[g.group.id] ?: 0L) / 60_000L
                    usage <= g.group.limitMinutes
                }
                val doneEncs = encourageGroups.count { g ->
                    val usage = (usageMap[g.group.id] ?: 0L) / 60_000L
                    usage >= g.group.limitMinutes
                }
                val totalUsageMinutes = groupsWithApps.sumOf { (usageMap[it.group.id] ?: 0L) / 60_000L }

                // 积分与今日概览
                if (usageAccessGranted) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            val currentDate = remember {
                                val date = java.time.LocalDate.now()
                                val formatter = java.time.format.DateTimeFormatter.ofPattern("M月d日 EEEE", java.util.Locale.CHINESE)
                                date.format(formatter)
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = currentDate,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "自律即自由",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "当前累计",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "%.1f".format(userPoints),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.alignByBaseline()
                                        )
                                        Text(
                                            text = " 积分",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.alignByBaseline()
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("小约定", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                                        Text(
                                            text = "$safeVows / ${controlGroups.size}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                                
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("小鼓励", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f))
                                        Text(
                                            text = "$doneEncs / ${encourageGroups.size}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("今日收益", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                        Text(
                                            text = "+%.1f".format(todayPoints),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (showCardsOnHome) {
                    Text(
                        text = stringResource(R.string.home_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    PermissionProcessList(
                        isMenuMode = false,
                        usageAccessGranted = usageAccessGranted,
                        accessibilityServiceEnabled = accessibilityServiceEnabled,
                        isAutoStartDismissed = isAutoStartDismissed,
                        isIgnoringBattery = isIgnoringBattery,
                        notificationPermissionGranted = notificationPermissionGranted,
                        statusColor = statusColor,
                        onOpenUsageAccessSettings = onOpenUsageAccessSettings,
                        onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                        onOpenAutoStartSettings = onOpenAutoStartSettings,
                        onSetAutoStartDismissed = onSetAutoStartDismissed,
                        onRequestBatteryOptimization = onRequestBatteryOptimization,
                        onRequestNotificationPermission = onRequestNotificationPermission,
                    )
                }
            }

            if (usageAccessGranted) {
                GroupDashboard(
                    groupsWithApps = groupsWithApps,
                    usageMap = usageMap,
                    installedApps = installedApps,
                    isLoadingApps = isLoadingApps,
                    onSaveGroup = onSaveGroup,
                    onDeleteGroup = onDeleteGroup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GuidanceCard(
                        title = stringResource(R.string.permission_steps_title),
                        body = stringResource(R.string.permission_steps_body),
                    )
                    GuidanceCard(
                        title = stringResource(R.string.mvp_scope_title),
                        body = stringResource(R.string.mvp_scope_body),
                    )
                }
            }
        }

        if (showDiagnosticMenu) {
            @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { showDiagnosticMenu = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.action_diagnostic_settings),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.settings_menu_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    PermissionProcessList(
                        isMenuMode = true,
                        usageAccessGranted = usageAccessGranted,
                        accessibilityServiceEnabled = accessibilityServiceEnabled,
                        isAutoStartDismissed = isAutoStartDismissed,
                        isIgnoringBattery = isIgnoringBattery,
                        notificationPermissionGranted = notificationPermissionGranted,
                        statusColor = statusColor,
                        onOpenUsageAccessSettings = onOpenUsageAccessSettings,
                        onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                        onOpenAutoStartSettings = onOpenAutoStartSettings,
                        onSetAutoStartDismissed = onSetAutoStartDismissed,
                        onRequestBatteryOptimization = onRequestBatteryOptimization,
                        onRequestNotificationPermission = onRequestNotificationPermission,
                    )

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
private fun PermissionProcessList(
    isMenuMode: Boolean,
    usageAccessGranted: Boolean,
    accessibilityServiceEnabled: Boolean,
    isAutoStartDismissed: Boolean,
    isIgnoringBattery: Boolean,
    notificationPermissionGranted: Boolean,
    statusColor: androidx.compose.ui.graphics.Color,
    onOpenUsageAccessSettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenAutoStartSettings: () -> Unit,
    onSetAutoStartDismissed: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
) {
    if (isMenuMode || !usageAccessGranted) {
        PermissionCard(
            usageAccessGranted = usageAccessGranted,
            statusColor = statusColor,
            onOpenUsageAccessSettings = onOpenUsageAccessSettings,
        )
    }

    if (usageAccessGranted || isMenuMode) {
        if (isMenuMode || !accessibilityServiceEnabled) {
            AccessibilityStatusCard(
                accessibilityServiceEnabled = accessibilityServiceEnabled,
                isMenuMode = isMenuMode,
                onOpenAccessibilitySettings = onOpenAccessibilitySettings,
            )
        }

        if (isMenuMode || !isAutoStartDismissed) {
            AutoStartCard(
                onOpenAutoStartSettings = onOpenAutoStartSettings,
                onSetAutoStartDismissed = onSetAutoStartDismissed,
            )
        }

        if (isMenuMode || !isIgnoringBattery) {
            BatteryOptimizationCard(
                isIgnoringBattery = isIgnoringBattery,
                isMenuMode = isMenuMode,
                onRequestBatteryOptimization = onRequestBatteryOptimization,
            )
        }

        if (isMenuMode || !notificationPermissionGranted) {
            ReminderStatusCard(
                notificationPermissionGranted = notificationPermissionGranted,
                isMenuMode = isMenuMode,
                onRequestNotificationPermission = onRequestNotificationPermission,
            )
        }
    }
}

@Composable
private fun AccessibilityStatusCard(
    accessibilityServiceEnabled: Boolean,
    isMenuMode: Boolean = false,
    onOpenAccessibilitySettings: () -> Unit,
) {
    val statusColor = if (accessibilityServiceEnabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.accessibility_card_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (accessibilityServiceEnabled) {
                    stringResource(R.string.accessibility_card_enabled)
                } else {
                    stringResource(R.string.accessibility_card_disabled)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor,
                fontWeight = FontWeight.Medium,
            )
            if (isMenuMode || !accessibilityServiceEnabled) {
                Button(
                    onClick = onOpenAccessibilitySettings,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.accessibility_card_action))
                }
            }
        }
    }
}

@Composable
private fun ReminderStatusCard(
    notificationPermissionGranted: Boolean,
    isMenuMode: Boolean = false,
    onRequestNotificationPermission: () -> Unit,
) {
    val statusColor = if (notificationPermissionGranted) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.reminder_card_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (notificationPermissionGranted) {
                    stringResource(R.string.reminder_card_enabled)
                } else {
                    stringResource(R.string.reminder_card_disabled)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor,
                fontWeight = FontWeight.Medium,
            )
            if (isMenuMode || !notificationPermissionGranted) {
                Button(
                    onClick = onRequestNotificationPermission,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.reminder_card_action))
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(
    usageAccessGranted: Boolean,
    statusColor: androidx.compose.ui.graphics.Color,
    onOpenUsageAccessSettings: () -> Unit,
) {
    ElevatedCard(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(statusColor, CircleShape),
                )
                Text(
                    text = if (usageAccessGranted) {
                        stringResource(R.string.permission_status_granted)
                    } else {
                        stringResource(R.string.permission_status_denied)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor,
                )
            }

            Text(
                text = if (usageAccessGranted) {
                    stringResource(R.string.permission_status_granted_desc)
                } else {
                    stringResource(R.string.permission_status_denied_desc)
                },
                style = MaterialTheme.typography.bodyMedium,
            )

            Button(
                onClick = onOpenUsageAccessSettings,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (usageAccessGranted) {
                        stringResource(R.string.permission_manage_action)
                    } else {
                        stringResource(R.string.permission_primary_action)
                    },
                )
            }
        }
    }
}

@Composable
private fun AutoStartCard(
    onOpenAutoStartSettings: () -> Unit,
    onSetAutoStartDismissed: () -> Unit,
) {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.autostart_card_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.autostart_card_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenAutoStartSettings,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(R.string.autostart_card_action))
                }
                androidx.compose.material3.OutlinedButton(
                    onClick = onSetAutoStartDismissed,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(R.string.autostart_card_action_done))
                }
            }
        }
    }
}

@Composable
private fun BatteryOptimizationCard(
    isIgnoringBattery: Boolean,
    isMenuMode: Boolean = false,
    onRequestBatteryOptimization: () -> Unit,
) {
    val statusColor = if (isIgnoringBattery) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.battery_card_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (isIgnoringBattery) {
                    stringResource(R.string.battery_card_enabled)
                } else {
                    stringResource(R.string.battery_card_disabled)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor,
                fontWeight = FontWeight.Medium,
            )
            if (isMenuMode || !isIgnoringBattery) {
                Button(
                    onClick = onRequestBatteryOptimization,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.battery_card_action))
                }
            }
        }
    }
}

@Composable
private fun GuidanceCard(
    title: String,
    body: String,
) {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun DashboardProgressItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreviewDenied() {
    TinyVowTheme {
        HomeScreen(
            usageAccessStatus = UsageAccessStatus.DENIED,
            accessibilityServiceEnabled = false,
            installedApps = emptyList(),
            groupsWithApps = emptyList(),
            userPoints = 120.5,
            todayPoints = 10.0,
            isLoadingApps = false,
            notificationPermissionGranted = false,
            isIgnoringBattery = false,
            isAutoStartDismissed = false,
            onNavigateToRedeem = {},
            onNavigateToAchievements = {},
            onOpenUsageAccessSettings = {},
            onOpenAccessibilitySettings = {},
            onRequestNotificationPermission = {},
            onOpenAutoStartSettings = {},
            onRequestBatteryOptimization = {},
            onSetAutoStartDismissed = {},
            onSaveGroup = { _, _, _, _, _, _, _ -> },
            onDeleteGroup = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreviewGranted() {
    TinyVowTheme {
        HomeScreen(
            usageAccessStatus = UsageAccessStatus.GRANTED,
            accessibilityServiceEnabled = true,
            installedApps = listOf(
                ManagedApp(
                    packageName = "com.example.video",
                    appName = "Video App",
                ),
            ),
            groupsWithApps = emptyList(),
            userPoints = 450.0,
            todayPoints = 25.0,
            isLoadingApps = false,
            notificationPermissionGranted = true,
            isIgnoringBattery = true,
            isAutoStartDismissed = false,
            onNavigateToRedeem = {},
            onNavigateToAchievements = {},
            onOpenUsageAccessSettings = {},
            onOpenAccessibilitySettings = {},
            onRequestNotificationPermission = {},
            onOpenAutoStartSettings = {},
            onRequestBatteryOptimization = {},
            onSetAutoStartDismissed = {},
            onSaveGroup = { _, _, _, _, _, _, _ -> },
            onDeleteGroup = {}
        )
    }
}
