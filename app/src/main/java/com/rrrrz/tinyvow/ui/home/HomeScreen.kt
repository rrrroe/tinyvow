package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.i18n.AppText

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import androidx.core.content.FileProvider
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.data.auth.LocalAuthRepository
import com.rrrrz.tinyvow.data.billing.PlayBillingSubscriptionRepository
import com.rrrrz.tinyvow.data.accessibility.AccessibilityServiceStateChecker
import com.rrrrz.tinyvow.data.apps.InstalledAppRepository
import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.notification.NotificationPermissionChecker
import com.rrrrz.tinyvow.data.privacy.LocalDataManager
import com.rrrrz.tinyvow.data.repository.AppGroupWithApps
import com.rrrrz.tinyvow.data.repository.AppLimitRepository
import com.rrrrz.tinyvow.data.repository.DailyArchiveRepository
import com.rrrrz.tinyvow.data.repository.PointsRepository
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.usage.UsageAccessStateChecker
import com.rrrrz.tinyvow.data.usage.UsageAccessStatus
import com.rrrrz.tinyvow.service.block.AppLimitAccessibilityService
import com.rrrrz.tinyvow.ui.theme.TinyVowTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import com.rrrrz.tinyvow.ui.theme.DefaultThemeSeed
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors

enum class Screen { HOME, REWARDS, STATS, ME, LABORATORY, HISTORY, THEME, HELP_FEEDBACK, CONTACT_US }

private const val CONTACT_EMAIL = "rrrr.zhao@gmail.com"

private object PermissionPromptIds {
    const val USAGE_ACCESS = "usage_access"
    const val ACCESSIBILITY = "accessibility"
    const val BACKGROUND_START = "background_start"
    const val BATTERY = "battery"
    const val NOTIFICATION = "notification"
}

private enum class SensitivePermissionDisclosure {
    USAGE_ACCESS,
    ACCESSIBILITY,
    NOTIFICATION,
    BATTERY_OPTIMIZATION,
    BACKGROUND_START,
}

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
    val tabs = listOf(AppText.t("home_achievements"), AppText.t("home_rewards_store"))
    
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
    val authRepository = remember(context) { LocalAuthRepository(context) }
    val subscriptionRepository = remember(context) { PlayBillingSubscriptionRepository(context) }
    
    val database = remember(context) { AppDatabase.getDatabase(context) }
    val appLimitRepository = remember(database, context) { AppLimitRepository(context, database) }
    val usageRepository = remember(context) { UsageStatsUsageRepository(context) }
    val pointsRepository = remember(database, context) { PointsRepository(context, database) }
    val dailyArchiveRepository = remember(database, context) { DailyArchiveRepository(context, database) }
    val localDataManager = remember(database, context, preferences) {
        LocalDataManager(context, database, preferences)
    }
    
    val groupsWithApps by appLimitRepository.getAllGroupsWithApps().collectAsState(initial = emptyList())
    val userPoints by preferences.userPoints.collectAsState(initial = 0.0)
    val todayPoints by preferences.todayPoints.collectAsState(initial = 0.0)
    val selectedThemeId by preferences.selectedThemeId.collectAsState(initial = DefaultThemeSeed.id)
    val customThemes by preferences.customThemes.collectAsState(initial = emptyList())
    val selectedAppLanguage by preferences.selectedAppLanguage.collectAsState(initial = com.rrrrz.tinyvow.i18n.AppLanguage.SYSTEM)
    val rewards by appLimitRepository.getAllRewards().collectAsState(initial = emptyList())
    val achievements by appLimitRepository.getAllAchievements().collectAsState(initial = emptyList())
    val redemptionHistory by appLimitRepository.getRedemptionHistory().collectAsState(initial = emptyList())
    val dismissedPermissionPrompts by preferences.dismissedPermissionPrompts.collectAsState(initial = emptySet())
    val usageAccessDisclosureAccepted by preferences.usageAccessDisclosureAccepted.collectAsState(initial = false)
    val accessibilityDisclosureAccepted by preferences.accessibilityDisclosureAccepted.collectAsState(initial = false)
    val userSession by authRepository.session.collectAsState(initial = null)
    val proEntitlement by subscriptionRepository.entitlement.collectAsState()
    val subscriptionOffers by subscriptionRepository.offers.collectAsState()

    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingSensitiveDisclosure by remember { mutableStateOf<SensitivePermissionDisclosure?>(null) }
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
        dailyArchiveRepository.ensureArchivesUpToYesterday()
        
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

    val effectiveUsageAccessStatus =
        if (usageAccessStatus == UsageAccessStatus.GRANTED && usageAccessDisclosureAccepted) {
            UsageAccessStatus.GRANTED
        } else {
            UsageAccessStatus.DENIED
        }
    val effectiveAccessibilityServiceEnabled =
        accessibilityServiceEnabled && accessibilityDisclosureAccepted

    LaunchedEffect(usageAccessStatus, usageAccessDisclosureAccepted) {
        if (effectiveUsageAccessStatus == UsageAccessStatus.GRANTED) {
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
        BackHandler {
            currentScreen = when (currentScreen) {
                Screen.LABORATORY, Screen.HISTORY, Screen.THEME, Screen.HELP_FEEDBACK, Screen.CONTACT_US -> Screen.ME
                else -> Screen.HOME
            }
        }
    }

    LaunchedEffect(Unit) {
        subscriptionRepository.refresh()
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            if (currentScreen == Screen.HOME || currentScreen == Screen.REWARDS || currentScreen == Screen.STATS || currentScreen == Screen.ME) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val screens = listOf(
                        Triple(Screen.HOME, AppText.t("home_home"), Icons.Default.Home),
                        Triple(Screen.STATS, AppText.t("home_report"), Icons.Default.BarChart),
                        Triple(Screen.REWARDS, AppText.t("home_rewards"), Icons.Default.CardGiftcard),
                        Triple(Screen.ME, AppText.t("home_me"), Icons.Default.Person)
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
                        usageAccessStatus = effectiveUsageAccessStatus,
                        accessibilityServiceEnabled = effectiveAccessibilityServiceEnabled,
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
                            if (usageAccessDisclosureAccepted) {
                                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } else {
                                pendingSensitiveDisclosure = SensitivePermissionDisclosure.USAGE_ACCESS
                            }
                        },
                        onOpenAccessibilitySettings = {
                            if (accessibilityDisclosureAccepted) {
                                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            } else {
                                pendingSensitiveDisclosure = SensitivePermissionDisclosure.ACCESSIBILITY
                            }
                        },
                        onRequestNotificationPermission = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                pendingSensitiveDisclosure = SensitivePermissionDisclosure.NOTIFICATION
                            }
                        },
                        onOpenAutoStartSettings = {
                            pendingSensitiveDisclosure = SensitivePermissionDisclosure.BACKGROUND_START
                        },
                        onRequestBatteryOptimization = {
                            pendingSensitiveDisclosure = SensitivePermissionDisclosure.BATTERY_OPTIMIZATION
                        },
                        isAutoStartDismissed = isAutoStartDismissed,
                        dismissedPermissionPrompts = dismissedPermissionPrompts,
                        onSetAutoStartDismissed = {
                            coroutineScope.launch { preferences.setAutoStartDismissed(true) }
                        },
                        onDismissPermissionPrompts = { ids ->
                            coroutineScope.launch {
                                ids.forEach { id ->
                                    preferences.setPermissionPromptDismissed(id, true)
                                    if (id == PermissionPromptIds.BACKGROUND_START) {
                                        preferences.setAutoStartDismissed(true)
                                    }
                                }
                            }
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
                        archiveRepository = dailyArchiveRepository,
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
                                appLimitRepository.redeemReward(reward, gId)
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
                        archiveRepository = dailyArchiveRepository,
                        onRequestUsageAccess = {
                            if (usageAccessDisclosureAccepted) {
                                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } else {
                                pendingSensitiveDisclosure = SensitivePermissionDisclosure.USAGE_ACCESS
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Screen.ME -> {
                    MeScreen(
                        userSession = userSession,
                        isGoogleSignInConfigured = authRepository.isGoogleSignInConfigured,
                        proEntitlement = proEntitlement,
                        subscriptionOffers = subscriptionOffers,
                        userPoints = userPoints,
                        selectedThemeId = selectedThemeId,
                        customThemes = customThemes,
                        selectedAppLanguage = selectedAppLanguage,
                        usageAccessGranted = effectiveUsageAccessStatus == UsageAccessStatus.GRANTED,
                        accessibilityServiceEnabled = effectiveAccessibilityServiceEnabled,
                        isAutoStartDismissed = isAutoStartDismissed,
                        isIgnoringBattery = isIgnoringBattery,
                        notificationPermissionGranted = notificationPermissionGranted,
                        dismissedPermissionPrompts = dismissedPermissionPrompts,
                        onSelectAppLanguage = { language ->
                            coroutineScope.launch {
                                preferences.setSelectedAppLanguage(language)
                                AppText.setLanguage(language, context)
                            }
                        },
                        onSelectTheme = { themeId ->
                            coroutineScope.launch {
                                preferences.setSelectedThemeId(themeId)
                            }
                        },
                        onSaveCustomTheme = { theme ->
                            coroutineScope.launch {
                                preferences.upsertCustomTheme(theme)
                            }
                        },
                        onDeleteCustomTheme = { themeId ->
                            coroutineScope.launch {
                                preferences.deleteCustomTheme(themeId)
                            }
                        },
                        onOpenUsageAccessSettings = {
                            if (usageAccessDisclosureAccepted) {
                                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } else {
                                pendingSensitiveDisclosure = SensitivePermissionDisclosure.USAGE_ACCESS
                            }
                        },
                        onOpenAccessibilitySettings = {
                            if (accessibilityDisclosureAccepted) {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } else {
                                pendingSensitiveDisclosure = SensitivePermissionDisclosure.ACCESSIBILITY
                            }
                        },
                        onRequestNotificationPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                pendingSensitiveDisclosure = SensitivePermissionDisclosure.NOTIFICATION
                            }
                        },
                        onOpenAutoStartSettings = {
                            pendingSensitiveDisclosure = SensitivePermissionDisclosure.BACKGROUND_START
                        },
                        onSetAutoStartDismissed = {
                            coroutineScope.launch { preferences.setAutoStartDismissed(true) }
                        },
                        onRequestBatteryOptimization = {
                            pendingSensitiveDisclosure = SensitivePermissionDisclosure.BATTERY_OPTIMIZATION
                        },
                        onClearDismissedPermissionPrompts = {
                            coroutineScope.launch {
                                dismissedPermissionPrompts.forEach { id ->
                                    preferences.setPermissionPromptDismissed(id, false)
                                }
                                if (PermissionPromptIds.BACKGROUND_START in dismissedPermissionPrompts) {
                                    preferences.setAutoStartDismissed(false)
                                }
                            }
                        },
                        onNavigateToLaboratory = { currentScreen = Screen.LABORATORY },
                        onNavigateToHistory = { currentScreen = Screen.HISTORY },
                        onNavigateToThemeSettings = { currentScreen = Screen.THEME },
                        onNavigateToHelpFeedback = { currentScreen = Screen.HELP_FEEDBACK },
                        onNavigateToContactUs = { currentScreen = Screen.CONTACT_US },
                        onExportLocalData = {
                            coroutineScope.launch {
                                runCatching {
                                    localDataManager.exportPrivacyReport()
                                }.onSuccess { file ->
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file,
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(Intent.EXTRA_TITLE, AppText.t("home_tiny_vow_local_data_export"))
                                        clipData = ClipData.newUri(
                                            context.contentResolver,
                                            AppText.t("home_tiny_vow_local_data_export"),
                                            uri,
                                        )
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, AppText.t("home_export_local_data")))
                                }.onFailure {
                                    snackbarHostState.showSnackbar(AppText.t("home_export_local_data_failed"))
                                }
                            }
                        },
                        onClearLocalData = {
                            coroutineScope.launch {
                                runCatching {
                                    localDataManager.clearLocalData()
                                }.onSuccess {
                                    snackbarHostState.showSnackbar(AppText.t("home_local_data_cleared"))
                                }.onFailure {
                                    snackbarHostState.showSnackbar(AppText.t("home_clear_local_data_failed"))
                                }
                            }
                        },
                        onOpenPrivacyPolicy = {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(PRIVACY_POLICY_URL),
                                )
                            )
                        },
                        onSignInWithGoogle = {
                            val activity = context as? androidx.activity.ComponentActivity
                            if (activity == null) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(AppText.t("home_google_sign_in_cannot_start_from_this_screen"))
                                }
                            } else {
                                coroutineScope.launch {
                                    authRepository.signInWithGoogle(activity)
                                        .onSuccess {
                                            snackbarHostState.showSnackbar(AppText.t("home_signed_in_with_google"))
                                        }
                                        .onFailure {
                                            snackbarHostState.showSnackbar(it.message ?: AppText.t("home_google_sign_in_failed"))
                                        }
                                }
                            }
                        },
                        onSignOut = {
                            coroutineScope.launch {
                                authRepository.signOut()
                                snackbarHostState.showSnackbar(AppText.t("home_signed_out"))
                            }
                        },
                        onDeleteAccount = { clearLocalData ->
                            coroutineScope.launch {
                                authRepository.deleteAccount()
                                if (clearLocalData) {
                                    localDataManager.clearLocalData()
                                }
                                snackbarHostState.showSnackbar(
                                    if (clearLocalData) AppText.t("home_account_and_local_data_deleted") else AppText.t("home_account_deleted")
                                )
                            }
                        },
                        onPurchasePro = { offer ->
                            val activity = context as? android.app.Activity
                            if (activity == null) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(AppText.t("home_google_play_purchase_cannot_start_from_this_screen"))
                                }
                            } else {
                                coroutineScope.launch {
                                    subscriptionRepository.purchase(activity, offer)
                                        .onFailure {
                                            snackbarHostState.showSnackbar(it.message ?: AppText.t("home_failed_to_start_pro_subscription"))
                                        }
                                }
                            }
                        },
                        onRestorePurchases = {
                            coroutineScope.launch {
                                subscriptionRepository.refresh()
                                    .onSuccess {
                                        snackbarHostState.showSnackbar(AppText.t("home_subscription_status_refreshed"))
                                    }
                                    .onFailure {
                                        snackbarHostState.showSnackbar(it.message ?: AppText.t("home_failed_to_restore_purchases"))
                                    }
                            }
                        },
                        onManageSubscription = {
                            subscriptionRepository.openManageSubscription(context)
                        },
                    )
                }
                Screen.THEME -> {
                    ThemeSettingsScreen(
                        selectedThemeId = selectedThemeId,
                        customThemes = customThemes,
                        onSelectTheme = { themeId ->
                            coroutineScope.launch {
                                preferences.setSelectedThemeId(themeId)
                            }
                        },
                        onSaveCustomTheme = { theme ->
                            coroutineScope.launch {
                                preferences.upsertCustomTheme(theme)
                            }
                        },
                        onDeleteCustomTheme = { themeId ->
                            coroutineScope.launch {
                                preferences.deleteCustomTheme(themeId)
                            }
                        },
                        onBack = { currentScreen = Screen.ME },
                    )
                }
                Screen.HELP_FEEDBACK -> {
                    HelpFeedbackScreen(
                        onBack = { currentScreen = Screen.ME },
                        onSendFeedback = {
                            if (!context.openSupportEmail(AppText.t("home_feedback_subject"))) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(AppText.t("home_no_mail_app_was_found_email_address_copied"))
                                }
                                context.copyContactEmail()
                            }
                        },
                    )
                }
                Screen.CONTACT_US -> {
                    ContactUsScreen(
                        onBack = { currentScreen = Screen.ME },
                        onSendEmail = {
                            if (!context.openSupportEmail(AppText.t("home_contact_tiny_vow"))) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(AppText.t("home_no_mail_app_was_found_email_address_copied"))
                                }
                                context.copyContactEmail()
                            }
                        },
                        onCopyEmail = {
                            context.copyContactEmail()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(AppText.t("home_email_copied"))
                            }
                        },
                    )
                }
                Screen.HISTORY -> {
                    HistoryRoute(
                        archiveRepository = dailyArchiveRepository,
                        onBack = { currentScreen = Screen.ME },
                    )
                }
                Screen.LABORATORY -> {
                    LaboratoryScreen(
                        onAddPoints = {
                            pts ->
                            coroutineScope.launch {
                                pointsRepository.recordManualAdjustment(pts, "Laboratory adjustment")
                                appLimitRepository.checkAchievements(preferences.userPoints.first())
                            }
                        },
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
                    Text(AppText.t("home_yesterday_s_report"), fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(AppText.t("home_well_done_yesterday_your_strong_will_saved_this"))
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
                                AppText.t("home_value_h_value_min", yesterdaySavedMinutes / 60, yesterdaySavedMinutes % 60)
                            } else {
                                AppText.t("home_value_minutes", yesterdaySavedMinutes)
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(AppText.t("home_keep_going_discipline_is_freedom"), style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(onClick = { showYesterdaySummary = false }) {
                    Text(AppText.t("home_got_it"))
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

    pendingSensitiveDisclosure?.let { disclosure ->
        SensitivePermissionDisclosureDialog(
            disclosure = disclosure,
            onDismiss = { pendingSensitiveDisclosure = null },
            onAccept = {
                coroutineScope.launch {
                    when (disclosure) {
                        SensitivePermissionDisclosure.USAGE_ACCESS -> {
                            preferences.setUsageAccessDisclosureAccepted(true)
                            pendingSensitiveDisclosure = null
                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                        SensitivePermissionDisclosure.ACCESSIBILITY -> {
                            preferences.setAccessibilityDisclosureAccepted(true)
                            pendingSensitiveDisclosure = null
                            context.startActivity(
                                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                        SensitivePermissionDisclosure.NOTIFICATION -> {
                            pendingSensitiveDisclosure = null
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                        SensitivePermissionDisclosure.BATTERY_OPTIMIZATION -> {
                            pendingSensitiveDisclosure = null
                            runCatching {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }.onFailure {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                        }
                        SensitivePermissionDisclosure.BACKGROUND_START -> {
                            pendingSensitiveDisclosure = null
                            runCatching {
                                val intent = Intent().apply {
                                    component = android.content.ComponentName(
                                        "com.miui.securitycenter",
                                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                                    )
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }.onFailure {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }
                        }
                    }
                }
            },
        )
    }
}

private const val PRIVACY_POLICY_URL = "https://rrrroe.github.io/tinyvow/privacy.html"

private fun android.content.Context.openSupportEmail(subject: String): Boolean {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:$CONTACT_EMAIL")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(CONTACT_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return runCatching {
        startActivity(intent)
        true
    }.getOrDefault(false)
}

private fun android.content.Context.copyContactEmail() {
    val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(AppText.t("home_tiny_vow_contact_email"), CONTACT_EMAIL))
}

@Composable
fun AchievementNotificationBanner(achievement: AchievementEntity) {
    val themeColors = LocalThemeColors.current
    val tierGradient = when (achievement.tier) {
        AchievementTier.LEGENDARY -> listOf(
            themeColors.control, themeColors.base, themeColors.encourage, themeColors.control
        )
        AchievementTier.DIAMOND -> listOf(
            themeColors.base, themeColors.baseContainer, themeColors.encourageContainer, themeColors.base
        )
        AchievementTier.GOLD -> listOf(
            themeColors.encourage, themeColors.encourageContainer, themeColors.base, themeColors.encourage
        )
        AchievementTier.SILVER -> listOf(
            themeColors.base, themeColors.baseContainer, themeColors.base
        )
        else -> listOf(
            themeColors.control, themeColors.controlContainer, themeColors.control
        )
    }

    val tierLabel = when (achievement.tier) {
        AchievementTier.LEGENDARY -> AppText.t("home_legendary_achievement_unlocked")
        AchievementTier.DIAMOND -> AppText.t("home_diamond_achievement_unlocked")
        AchievementTier.GOLD -> AppText.t("home_gold_achievement_unlocked")
        AchievementTier.SILVER -> AppText.t("home_silver_achievement_unlocked")
        else -> AppText.t("home_bronze_achievement_unlocked")
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
private fun SensitivePermissionDisclosureDialog(
    disclosure: SensitivePermissionDisclosure,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
) {
    val (title, body) = when (disclosure) {
        SensitivePermissionDisclosure.USAGE_ACCESS -> {
            AppText.t("home_usage_access_disclosure") to
                AppText.t("home_tiny_vow_reads_local_app_usage_data_including")
        }
        SensitivePermissionDisclosure.ACCESSIBILITY -> {
            AppText.t("home_accessibility_service_disclosure") to
                AppText.t("home_tiny_vow_s_accessibility_service_only_listens_for")
        }
        SensitivePermissionDisclosure.NOTIFICATION -> {
            AppText.t("home_notification_permission_disclosure") to
                AppText.t("home_tiny_vow_uses_notification_permission_to_send_local")
        }
        SensitivePermissionDisclosure.BATTERY_OPTIMIZATION -> {
            AppText.t("home_battery_allowlist_disclosure") to
                AppText.t("home_the_battery_allowlist_is_an_optional_reliability_setting")
        }
        SensitivePermissionDisclosure.BACKGROUND_START -> {
            AppText.t("home_background_start_disclosure") to
                AppText.t("home_some_phone_vendors_restrict_background_work_background_start")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text(AppText.t("home_agree_and_open_settings"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppText.t("group_cancel"))
            }
        },
    )
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
    dismissedPermissionPrompts: Set<String>,
    onSetAutoStartDismissed: () -> Unit,
    onDismissPermissionPrompts: (List<String>) -> Unit,
    onSaveGroup: (id: String?, name: String, limit: Int, type: GroupType, period: LimitPeriod, pts: Double, pkgs: List<String>) -> Unit,
    onDeleteGroup: (id: String) -> Unit,
    appLimitRepository: AppLimitRepository? = null,
    archiveRepository: DailyArchiveRepository? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var usageMap by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    
    // 定时刷新各分组用量：批量查询一次 UsageStats，过滤分组汇总。这样可将 N 次 IPC 降为 1 次
    LaunchedEffect(groupsWithApps, usageAccessStatus) {
        if (usageAccessStatus != UsageAccessStatus.GRANTED) {
            usageMap = emptyMap()
            return@LaunchedEffect
        }
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
            val pendingPermissionPrompts =
                listOfNotNull(
                    if (!usageAccessGranted && PermissionPromptIds.USAGE_ACCESS !in dismissedPermissionPrompts) {
                        PermissionPromptIds.USAGE_ACCESS to AppText.t("home_usage_access")
                    } else {
                        null
                    },
                    if (!accessibilityServiceEnabled && PermissionPromptIds.ACCESSIBILITY !in dismissedPermissionPrompts) {
                        PermissionPromptIds.ACCESSIBILITY to AppText.t("home_accessibility_blocking")
                    } else {
                        null
                    },
                    if (!isAutoStartDismissed && PermissionPromptIds.BACKGROUND_START !in dismissedPermissionPrompts) {
                        PermissionPromptIds.BACKGROUND_START to AppText.t("home_background_start")
                    } else {
                        null
                    },
                    if (!isIgnoringBattery && PermissionPromptIds.BATTERY !in dismissedPermissionPrompts) {
                        PermissionPromptIds.BATTERY to AppText.t("home_battery_allowlist")
                    } else {
                        null
                    },
                    if (!notificationPermissionGranted && PermissionPromptIds.NOTIFICATION !in dismissedPermissionPrompts) {
                        PermissionPromptIds.NOTIFICATION to AppText.t("home_notifications_permission")
                    } else {
                        null
                    },
                )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                val controlUsageMinutes = controlGroups.sumOf { (usageMap[it.group.id] ?: 0L) / 60_000L }
                val liveTodayPoints = encourageGroups.sumOf { group ->
                    val usageMillis = usageMap[group.group.id] ?: 0L
                    val usagePoints = usageMillis / 60_000.0 * group.group.pointsPerMinute
                    val targetBonus = if (usageMillis >= group.group.limitMinutes * 60_000L) {
                        group.group.limitMinutes * group.group.pointsPerMinute
                    } else {
                        0.0
                    }
                    usagePoints + targetBonus
                }
                val displayTodayPoints = liveTodayPoints

                // 积分与今日概览
                if (usageAccessGranted) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            val currentDate = remember {
                                val date = java.time.LocalDate.now()
                                val formatter = java.time.format.DateTimeFormatter.ofPattern(AppText.t("home_mmm_d_eeee"), java.util.Locale.CHINESE)
                                date.format(formatter)
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = currentDate,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                    )
                                    Text(
                                        text = AppText.t("home_discipline_is_freedom"),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = AppText.t("home_current_total"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "%.1f".format(userPoints),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.alignByBaseline()
                                        )
                                        Text(
                                            text = AppText.t("home_label"),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.alignByBaseline()
                                        )
                                    }
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    OverviewStatTile(
                                        label = AppText.t("group_commitment"),
                                        value = "$safeVows/${controlGroups.size}",
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.weight(1f),
                                    )
                                    OverviewStatTile(
                                        label = AppText.t("group_small_encouragement"),
                                        value = "$doneEncs/${encourageGroups.size}",
                                        color = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    OverviewStatTile(
                                        label = AppText.t("home_today_usage_time"),
                                        value = AppText.t("home_value_min", controlUsageMinutes),
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.weight(1f),
                                    )
                                    OverviewStatTile(
                                        label = AppText.t("home_available_today"),
                                        value = AppText.t("home_value_pts").format(displayTodayPoints),
                                        color = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }
                }
                
                if (pendingPermissionPrompts.isNotEmpty()) {
                    CompactPermissionBanner(
                        prompts = pendingPermissionPrompts,
                        onOpen = { showDiagnosticMenu = true },
                        onDismiss = {
                            onDismissPermissionPrompts(pendingPermissionPrompts.map { it.first })
                        },
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
                    onReorderGroups = { type, ids ->
                        coroutineScope.launch { appLimitRepository?.reorderGroups(type, ids) }
                    },
                    archiveRepository = archiveRepository,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = AppText.t("home_enable_usage_access_to_show_groups"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
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
private fun CompactPermissionBanner(
    prompts: List<Pair<String, String>>,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape),
            )
            Text(
                text = AppText.t("home_permission_suggestions_count", prompts.size),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            TextButton(onClick = onOpen, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                Text(AppText.t("home_review"), maxLines = 1)
            }
            TextButton(onClick = onDismiss, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                Text(AppText.t("home_dismiss"), maxLines = 1)
            }
        }
    }
}

@Composable
private fun OverviewStatTile(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(58.dp),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun PermissionProcessList(
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
    val showUsageAccess = isMenuMode || !usageAccessGranted
    val showAccessibility = usageAccessGranted || isMenuMode
    val showAutoStart = showAccessibility && (isMenuMode || !isAutoStartDismissed)
    val showBattery = showAccessibility && (isMenuMode || !isIgnoringBattery)
    val showNotification = showAccessibility && (isMenuMode || !notificationPermissionGranted)

    if (showUsageAccess || showAccessibility) {
        Text(
            text = AppText.t("home_core_permissions"),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp),
        )
    }

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

        if (showAutoStart || showBattery || showNotification) {
            Text(
                text = AppText.t("home_improve_reliability_optional"),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp),
            )
        }

        if (isMenuMode || !isAutoStartDismissed) {
            AutoStartCard(
                isAutoStartDismissed = isAutoStartDismissed,
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
                color = MaterialTheme.colorScheme.onSurface,
            )
            PermissionStatusLine(
                text = if (accessibilityServiceEnabled) AppText.t("home_service_running") else AppText.t("home_not_enabled"),
                color = statusColor,
            )
            Text(
                text = if (accessibilityServiceEnabled) {
                    stringResource(R.string.accessibility_card_enabled)
                } else {
                    stringResource(R.string.accessibility_card_disabled)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (accessibilityServiceEnabled) statusColor else MaterialTheme.colorScheme.onSurfaceVariant,
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
                color = MaterialTheme.colorScheme.onSurface,
            )
            PermissionStatusLine(
                text = if (notificationPermissionGranted) AppText.t("home_notifications_enabled") else AppText.t("home_not_enabled"),
                color = statusColor,
            )
            Text(
                text = if (notificationPermissionGranted) {
                    stringResource(R.string.reminder_card_enabled)
                } else {
                    stringResource(R.string.reminder_card_disabled)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (notificationPermissionGranted) statusColor else MaterialTheme.colorScheme.onSurfaceVariant,
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
            Text(
                text = AppText.t("home_usage_access_step_title"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            PermissionStatusLine(
                text = if (usageAccessGranted) AppText.t("home_enabled") else AppText.t("home_not_enabled"),
                color = statusColor,
            )

            Text(
                text = if (usageAccessGranted) {
                    stringResource(R.string.permission_status_granted_desc)
                } else {
                    stringResource(R.string.permission_status_denied_desc)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (usageAccessGranted) statusColor else MaterialTheme.colorScheme.onSurfaceVariant,
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
    isAutoStartDismissed: Boolean,
    onOpenAutoStartSettings: () -> Unit,
    onSetAutoStartDismissed: () -> Unit,
) {
    val statusColor = if (isAutoStartDismissed) {
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
                text = stringResource(R.string.autostart_card_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            PermissionStatusLine(
                text = if (isAutoStartDismissed) AppText.t("home_confirmed_enabled") else AppText.t("home_manual_setup_recommended"),
                color = statusColor,
            )
            Text(
                text = stringResource(R.string.autostart_card_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isAutoStartDismissed) statusColor else MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun PermissionStatusLine(
    text: String,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .background(color, CircleShape),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
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
                color = MaterialTheme.colorScheme.onSurface,
            )
            PermissionStatusLine(
                text = if (isIgnoringBattery) AppText.t("home_allowlist_enabled") else AppText.t("home_not_enabled"),
                color = statusColor,
            )
            Text(
                text = if (isIgnoringBattery) {
                    stringResource(R.string.battery_card_enabled)
                } else {
                    stringResource(R.string.battery_card_disabled)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (isIgnoringBattery) statusColor else MaterialTheme.colorScheme.onSurfaceVariant,
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
            dismissedPermissionPrompts = emptySet(),
            onNavigateToRedeem = {},
            onNavigateToAchievements = {},
            onOpenUsageAccessSettings = {},
            onOpenAccessibilitySettings = {},
            onRequestNotificationPermission = {},
            onOpenAutoStartSettings = {},
            onRequestBatteryOptimization = {},
            onSetAutoStartDismissed = {},
            onDismissPermissionPrompts = {},
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
            dismissedPermissionPrompts = emptySet(),
            onNavigateToRedeem = {},
            onNavigateToAchievements = {},
            onOpenUsageAccessSettings = {},
            onOpenAccessibilitySettings = {},
            onRequestNotificationPermission = {},
            onOpenAutoStartSettings = {},
            onRequestBatteryOptimization = {},
            onSetAutoStartDismissed = {},
            onDismissPermissionPrompts = {},
            onSaveGroup = { _, _, _, _, _, _, _ -> },
            onDeleteGroup = {}
        )
    }
}
