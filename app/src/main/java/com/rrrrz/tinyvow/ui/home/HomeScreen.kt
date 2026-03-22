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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.rrrrz.tinyvow.data.db.RedemptionEntity
import com.rrrrz.tinyvow.ui.rewards.RedeemScreen
import com.rrrrz.tinyvow.ui.rewards.AchievementScreen

enum class Screen { HOME, REDEEM, ACHIEVEMENT }

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
    val rewards by appLimitRepository.getAllRewards().collectAsState(initial = emptyList())
    val achievements by appLimitRepository.getAllAchievements().collectAsState(initial = emptyList())

    var currentScreen by remember { mutableStateOf(Screen.HOME) }
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

    LaunchedEffect(userPoints) {
        appLimitRepository.checkAchievements(userPoints)
    }

    var newlyUnlockedAchievement by remember { mutableStateOf<AchievementEntity?>(null) }
    LaunchedEffect(Unit) {
        appLimitRepository.newAchievementsAction.collectLatest { achievement ->
            newlyUnlockedAchievement = achievement
            kotlinx.coroutines.delay(5000) // 显示 5 秒
            newlyUnlockedAchievement = null
        }
    }

    if (currentScreen != Screen.HOME) {
        BackHandler { currentScreen = Screen.HOME }
    }

    when (currentScreen) {
        Screen.REDEEM -> {
            RedeemScreen(
                userPoints = userPoints,
                rewards = rewards,
                groups = groupsWithApps,
                onRedeem = { reward, gId -> 
                    coroutineScope.launch {
                        if (reward.rewardType == com.rrrrz.tinyvow.data.db.RewardType.TIME_PACK && gId != null) {
                            appLimitRepository.redeemTimePack(gId, reward.bonusMinutes)
                        }
                        preferences.addUserPoints(-reward.pointCost.toDouble())
                    }
                },
                onAddCustomReward = { name, cost ->
                    coroutineScope.launch { appLimitRepository.addReward(name, cost, com.rrrrz.tinyvow.data.db.RewardType.CUSTOM) }
                },
                onBack = { currentScreen = Screen.HOME }
            )
        }
        Screen.ACHIEVEMENT -> {
            AchievementScreen(
                achievements = achievements,
                onBack = { currentScreen = Screen.HOME }
            )
        }
        Screen.HOME -> {
            HomeScreen(
                usageAccessStatus = usageAccessStatus,
                accessibilityServiceEnabled = accessibilityServiceEnabled,
                notificationPermissionGranted = notificationPermissionGranted,
                isIgnoringBattery = isIgnoringBattery,
                installedApps = installedApps,
                groupsWithApps = groupsWithApps,
                userPoints = userPoints,
                isLoadingApps = isLoadingApps,
                onNavigateToRedeem = { currentScreen = Screen.REDEEM },
                onNavigateToAchievements = { currentScreen = Screen.ACHIEVEMENT },
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
                onTestAddPoints = { points ->
                    coroutineScope.launch {
                        preferences.addUserPoints(points)
                        appLimitRepository.checkAchievements(preferences.userPoints.first())
                    }
                },
                onTestResetSummaryDate = {
                    coroutineScope.launch {
                        preferences.setLastSummaryShownDate("reset")
                    }
                },
                onTestTriggerAchievement = {
                    coroutineScope.launch {
                        val achievement = appLimitRepository.getAllAchievements().first().find { it.id == "FIRST_10_POINTS" }
                        if (achievement != null) {
                            // Using a private flow/method isn't possible, but checkAchievements will trigger it anyway.
                            // We can just simulate it by adding points.
                            preferences.addUserPoints(10.0)
                            appLimitRepository.checkAchievements(preferences.userPoints.first())
                        }
                    }
                },
                modifier = modifier,
            )
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
                        tint = androidx.compose.ui.graphics.Color(0xFFFFD700),
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
    Surface(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .padding(top = 24.dp), // 避开状态栏
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 8.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(androidx.compose.ui.graphics.Brush.sweepGradient(
                        listOf(
                            androidx.compose.ui.graphics.Color(0xFFFFD700),
                            androidx.compose.ui.graphics.Color(0xFFFFA500),
                            androidx.compose.ui.graphics.Color(0xFFFFD700)
                        )
                    ), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.Default.Star,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    "成就解锁！",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    achievement.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
    onTestAddPoints: (Double) -> Unit,
    onTestResetSummaryDate: () -> Unit,
    onTestTriggerAchievement: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showCardsOnHome) stringResource(R.string.home_title) else stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    androidx.compose.material3.IconButton(onClick = { showDiagnosticMenu = true }) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                            contentDescription = stringResource(R.string.action_diagnostic_settings)
                        )
                    }
                }

                // 积分与奖励入口
                if (usageAccessGranted) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("当前积分", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    "%.1f PT".format(userPoints),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = onNavigateToRedeem,
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("商城", style = MaterialTheme.typography.labelLarge)
                                }
                                OutlinedButton(
                                    onClick = onNavigateToAchievements,
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("成就", style = MaterialTheme.typography.labelLarge)
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
                    installedApps = installedApps,
                    isLoadingApps = isLoadingApps,
                    onSaveGroup = onSaveGroup,
                    onDeleteGroup = onDeleteGroup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
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

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("调试工具 (Testing)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onTestAddPoints(10.0) }, modifier = Modifier.weight(1f)) { Text("+10") }
                        Button(onClick = { onTestAddPoints(100.0) }, modifier = Modifier.weight(1f)) { Text("+100") }
                    }
                    Button(onClick = { onTestResetSummaryDate() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) { Text("重置昨日战报日期") }
                    OutlinedButton(onClick = { onTestTriggerAchievement() }, modifier = Modifier.fillMaxWidth()) { Text("模拟/触发成就 banner") }
                    
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
            onDeleteGroup = {},
            onTestAddPoints = {},
            onTestResetSummaryDate = {},
            onTestTriggerAchievement = {}
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
            onDeleteGroup = {},
            onTestAddPoints = {},
            onTestResetSummaryDate = {},
            onTestTriggerAchievement = {}
        )
    }
}
