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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.offset
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import androidx.compose.ui.window.Popup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.content.FileProvider
import com.rrrrz.tinyvow.BuildConfig
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.data.activation.LocalActivationSubscriptionRepository
import com.rrrrz.tinyvow.data.auth.LocalAuthRepository
import com.rrrrz.tinyvow.data.billing.NoopSubscriptionRepository
import com.rrrrz.tinyvow.data.billing.PlayBillingSubscriptionRepository
import com.rrrrz.tinyvow.data.billing.ProEntitlementState
import com.rrrrz.tinyvow.data.billing.SubscriptionRepository
import com.rrrrz.tinyvow.data.accessibility.AccessibilityServiceStateChecker
import com.rrrrz.tinyvow.data.apps.InstalledAppRepository
import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.notification.NotificationPermissionChecker
import com.rrrrz.tinyvow.data.privacy.LocalDataManager
import com.rrrrz.tinyvow.data.pro.ProFeatureGate
import com.rrrrz.tinyvow.data.repository.AppGroupWithApps
import com.rrrrz.tinyvow.data.repository.AppLimitRepository
import com.rrrrz.tinyvow.data.repository.AchievementProgress
import com.rrrrz.tinyvow.data.repository.DailyArchiveRepository
import com.rrrrz.tinyvow.data.repository.CustomRewardDraft
import com.rrrrz.tinyvow.data.repository.PointsRepository
import com.rrrrz.tinyvow.data.repository.InventoryRewardItem
import com.rrrrz.tinyvow.data.repository.PendingStreakShieldItem
import com.rrrrz.tinyvow.data.repository.PurchaseRewardResult
import com.rrrrz.tinyvow.data.repository.RewardStoreItem
import com.rrrrz.tinyvow.data.repository.RewardSaveResult
import com.rrrrz.tinyvow.data.repository.RewardSaveValidationError
import com.rrrrz.tinyvow.data.repository.UseRewardResult
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.supermode.GuardedAction
import com.rrrrz.tinyvow.data.supermode.SuperModeController
import com.rrrrz.tinyvow.data.supermode.SuperModeEnterResult
import com.rrrrz.tinyvow.data.supermode.SuperModeExitReason
import com.rrrrz.tinyvow.data.supermode.SuperModeRecoveryResult
import com.rrrrz.tinyvow.data.supermode.SuperModeStatus
import com.rrrrz.tinyvow.data.supermode.SuperModeStoredState
import com.rrrrz.tinyvow.data.supermode.SuperModeWindowUpdateResult
import com.rrrrz.tinyvow.data.usage.UsageAccessStateChecker
import com.rrrrz.tinyvow.data.usage.UsageAccessStatus
import com.rrrrz.tinyvow.service.block.AppLimitAccessibilityService
import com.rrrrz.tinyvow.ui.theme.TinyVowTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToLong
import kotlin.math.sin
import com.rrrrz.tinyvow.data.usage.UsageStatsUsageRepository
import com.rrrrz.tinyvow.data.usage.UsageRepository

import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.db.AchievementEntity
import com.rrrrz.tinyvow.data.db.AchievementTier
import com.rrrrz.tinyvow.data.db.RedemptionEntity
import com.rrrrz.tinyvow.data.db.RedemptionHistoryEntity
import com.rrrrz.tinyvow.data.db.RewardUseHistoryEntity
import com.rrrrz.tinyvow.ui.rewards.RedeemScreen
import com.rrrrz.tinyvow.ui.rewards.AchievementScreen
import com.rrrrz.tinyvow.ui.rewards.AchievementBadge
import com.rrrrz.tinyvow.ui.rewards.RewardInventoryScreen
import com.rrrrz.tinyvow.ui.theme.DefaultThemeSeed
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors

enum class Screen { HOME, REWARDS, STATS, ME, LABORATORY, HISTORY, THEME, HELP_FEEDBACK, CONTACT_US }
enum class RewardsSection { STORE, INVENTORY, ACHIEVEMENTS }

private const val CONTACT_EMAIL = "rrrr.zhao@gmail.com"

private data class PendingSuperModeRequest(
    val message: String,
    val onAllowed: (() -> Unit)?,
)

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

private data class HomeOverviewUiState(
    val dateLabel: String,
    val tagline: String,
    val control: HomeControlOverviewUiState,
    val encourage: HomeEncourageOverviewUiState,
    val history: HomeHistoryOverviewUiState,
)

private data class HomeControlOverviewUiState(
    val todaySavedMinutes: Int,
    val completedGroups: Int,
    val totalGroups: Int,
    val streakDays: Int,
)

private data class HomeEncourageOverviewUiState(
    val todayEarnedPoints: Double,
    val completedGroups: Int,
    val totalGroups: Int,
    val streakDays: Int,
)

private data class HomeHistoryOverviewUiState(
    val totalSavedMinutes: Long,
    val extendedLifeMinutes: Long,
    val totalEarnedPoints: Double,
    val currentPoints: Double,
)

@Composable
fun RewardsHome(
    userPoints: Double,
    achievements: List<AchievementEntity>,
    achievementProgress: AchievementProgress,
    storeItems: List<RewardStoreItem>,
    inventoryItems: List<InventoryRewardItem>,
    pendingShieldItems: List<PendingStreakShieldItem>,
    groups: List<AppGroupWithApps>,
    redemptionHistory: List<RedemptionHistoryEntity>,
    rewardUseHistory: List<RewardUseHistoryEntity>,
    onPurchaseReward: (RedemptionEntity) -> Unit,
    onUseInventoryReward: (RedemptionEntity, String?) -> Unit,
    onResolvePendingShield: (String, Boolean) -> Unit,
    onAddReward: (CustomRewardDraft) -> Unit,
    onUpdateReward: (RedemptionEntity) -> Unit,
    onArchiveReward: (RedemptionEntity) -> Unit,
    isProActive: Boolean,
    onShowProUpsell: (ProUpsellSource) -> Unit,
    onGuardAction: (GuardedAction, () -> Unit) -> Unit,
    currentSection: RewardsSection,
    onSectionChange: (RewardsSection) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        RewardsPrimarySwitcher(
            currentSection = currentSection,
            onSectionChange = onSectionChange,
        )
        when (currentSection) {
            RewardsSection.STORE -> {
                RedeemScreen(
                    userPoints = userPoints,
                    storeItems = storeItems,
                    groups = groups,
                    onPurchase = onPurchaseReward,
                    onAddReward = onAddReward,
                    onUpdateReward = onUpdateReward,
                    onArchiveReward = onArchiveReward,
                    isProActive = isProActive,
                    onShowProUpsell = onShowProUpsell,
                    onGuardAction = onGuardAction,
                )
            }
            RewardsSection.INVENTORY -> {
                RewardInventoryScreen(
                    inventoryItems = inventoryItems,
                    pendingItems = pendingShieldItems,
                    groups = groups,
                    redemptionHistory = redemptionHistory,
                    rewardUseHistory = rewardUseHistory,
                    onUseReward = onUseInventoryReward,
                    onResolvePending = onResolvePendingShield,
                )
            }
            RewardsSection.ACHIEVEMENTS -> {
                AchievementScreen(
                    achievements = achievements,
                    achievementProgress = achievementProgress,
                    onBack = {},
                )
            }
        }
    }
}

@Composable
private fun RewardsPrimarySwitcher(
    currentSection: RewardsSection,
    onSectionChange: (RewardsSection) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RewardsSwitchButton(
            selected = currentSection == RewardsSection.STORE,
            title = AppText.t("redeem_store_title"),
            onClick = { onSectionChange(RewardsSection.STORE) },
            modifier = Modifier.weight(1f),
        )
        RewardsSwitchButton(
            selected = currentSection == RewardsSection.INVENTORY,
            title = AppText.t("redeem_inventory_title"),
            onClick = { onSectionChange(RewardsSection.INVENTORY) },
            modifier = Modifier.weight(1f),
        )
        RewardsSwitchButton(
            selected = currentSection == RewardsSection.ACHIEVEMENTS,
            title = AppText.t("home_achievements"),
            onClick = { onSectionChange(RewardsSection.ACHIEVEMENTS) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RewardsSwitchButton(
    selected: Boolean,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color =
            if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color =
                    if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun purchaseRewardResultMessage(result: PurchaseRewardResult): String =
    when (result) {
        is PurchaseRewardResult.Success ->
            AppText.t("redeem_purchase_success", result.rewardTitle, result.pointCost)
        PurchaseRewardResult.InsufficientPoints -> AppText.t("redeem_error_insufficient_points")
        PurchaseRewardResult.OutOfStock -> AppText.t("redeem_error_out_of_stock")
        PurchaseRewardResult.DailyLimitReached -> AppText.t("redeem_error_daily_limit_reached")
        PurchaseRewardResult.InvalidReward -> AppText.t("redeem_error_invalid_reward")
    }

private fun useRewardResultMessage(result: UseRewardResult): String =
    when (result) {
        is UseRewardResult.Success ->
            if (result.messageArgs.isEmpty()) {
                AppText.t(result.messageKey)
            } else {
                AppText.t(result.messageKey, *result.messageArgs.toTypedArray())
            }
        UseRewardResult.NotOwned -> AppText.t("redeem_error_not_owned")
        UseRewardResult.InvalidTargetGroup -> AppText.t("redeem_error_missing_target_group")
        UseRewardResult.AlreadyActive -> AppText.t("redeem_error_already_active")
        UseRewardResult.AlreadyCompleted -> AppText.t("redeem_error_already_completed")
        UseRewardResult.InvalidReward -> AppText.t("redeem_error_invalid_reward")
    }

private fun rewardSaveResultMessage(result: RewardSaveResult): String? =
    when (result) {
        RewardSaveResult.Success -> null
        is RewardSaveResult.Invalid ->
            when (result.error) {
                RewardSaveValidationError.TITLE_REQUIRED -> AppText.t("redeem_error_title_required")
                RewardSaveValidationError.POINT_COST_INVALID -> AppText.t("redeem_error_point_cost_invalid")
                RewardSaveValidationError.STOCK_INVALID -> AppText.t("redeem_error_stock_invalid")
                RewardSaveValidationError.ICON_INVALID -> AppText.t("redeem_error_icon_invalid")
                RewardSaveValidationError.REWARD_NOT_EDITABLE -> AppText.t("redeem_error_reward_not_editable")
            }
    }

private fun guardedActionLabel(action: GuardedAction): String =
    when (action) {
        GuardedAction.EDIT_GROUP -> AppText.t("super_mode_action_edit_group")
        GuardedAction.DELETE_GROUP -> AppText.t("super_mode_action_delete_group")
        GuardedAction.ADD_CUSTOM_REWARD -> AppText.t("super_mode_action_add_custom_reward")
        GuardedAction.EDIT_CUSTOM_REWARD -> AppText.t("super_mode_action_edit_custom_reward")
        GuardedAction.EDIT_REWARD_PRICE -> AppText.t("super_mode_action_edit_reward_price")
    }

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = lifecycleOwner.lifecycle
    val coroutineScope = rememberCoroutineScope()
    
    val accessibilityServiceStateChecker = remember(context) { AccessibilityServiceStateChecker(context) }
    val checker = remember(context) { UsageAccessStateChecker(context) }
    val appRepository = remember(context) { InstalledAppRepository(context) }
    val preferences = remember(context) { ManagedAppPreferences(context) }
    val notificationPermissionChecker = remember(context) { NotificationPermissionChecker(context) }
    val powerManager = remember(context) { context.getSystemService(android.content.Context.POWER_SERVICE) as PowerManager }
    var isIgnoringBattery by remember { mutableStateOf(powerManager.isIgnoringBatteryOptimizations(context.packageName)) }
    val authRepository = remember(context) { LocalAuthRepository(context) }
    val localActivationRepository = remember(context) {
        if (BuildConfig.ENABLE_LOCAL_ACTIVATION) {
            LocalActivationSubscriptionRepository(context, BuildConfig.ACTIVATION_PUBLIC_KEY_BASE64)
        } else {
            null
        }
    }
    val subscriptionRepository: SubscriptionRepository = remember(context) {
        if (BuildConfig.ENABLE_PLAY_BILLING) {
            PlayBillingSubscriptionRepository(context)
        } else {
            localActivationRepository ?: NoopSubscriptionRepository()
        }
    }
    
    val database = remember(context) { AppDatabase.getDatabase(context) }
    val appLimitRepository = remember(database, context) { AppLimitRepository(context, database) }
    val usageRepository = remember(context) { UsageStatsUsageRepository(context) }
    val pointsRepository = remember(database, context) { PointsRepository(context, database) }
    val dailyArchiveRepository = remember(database, context) { DailyArchiveRepository(context, database) }
    val localDataManager = remember(database, context, preferences) {
        LocalDataManager(context, database, preferences)
    }
    val superModeController = remember(preferences) { SuperModeController(preferences) }
    val currentTimeMillis by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1000L)
        }
    }
    
    val groupsWithApps by appLimitRepository.getAllGroupsWithApps().collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val userPoints by preferences.userPoints.collectAsStateWithLifecycle(initialValue = 0.0, lifecycle = lifecycle)
    val todayPoints by preferences.todayPoints.collectAsStateWithLifecycle(initialValue = 0.0, lifecycle = lifecycle)
    val selectedThemeId by preferences.selectedThemeId.collectAsStateWithLifecycle(initialValue = DefaultThemeSeed.id, lifecycle = lifecycle)
    val customThemes by preferences.customThemes.collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val selectedAppLanguage by preferences.selectedAppLanguage.collectAsStateWithLifecycle(initialValue = com.rrrrz.tinyvow.i18n.AppLanguage.SYSTEM, lifecycle = lifecycle)
    val profileDisplayName by preferences.profileDisplayName.collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    val profileAvatarUri by preferences.profileAvatarUri.collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    val storeRewardItems by appLimitRepository.observeStoreRewardsWithInventory().collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val inventoryRewardItems by appLimitRepository.observeInventoryRewards().collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val pendingShieldItems by appLimitRepository.observePendingStreakShields().collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val achievements by appLimitRepository.getAllAchievements().collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val achievementProgress by appLimitRepository.observeAchievementProgress().collectAsStateWithLifecycle(initialValue = AchievementProgress(), lifecycle = lifecycle)
    val redemptionHistory by appLimitRepository.getRedemptionHistory().collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val rewardUseHistory by appLimitRepository.observeRewardUseHistory().collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val dismissedPermissionPrompts by preferences.dismissedPermissionPrompts.collectAsStateWithLifecycle(initialValue = emptySet(), lifecycle = lifecycle)
    val usageAccessDisclosureAccepted by preferences.usageAccessDisclosureAccepted.collectAsStateWithLifecycle(initialValue = false, lifecycle = lifecycle)
    val accessibilityDisclosureAccepted by preferences.accessibilityDisclosureAccepted.collectAsStateWithLifecycle(initialValue = false, lifecycle = lifecycle)
    val superModeStoredState by preferences.superModeState.collectAsStateWithLifecycle(initialValue = SuperModeStoredState(), lifecycle = lifecycle)
    val userSession by authRepository.session.collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    val subscriptionEntitlement by subscriptionRepository.entitlement.collectAsStateWithLifecycle(lifecycle = lifecycle)
    val subscriptionOffers by subscriptionRepository.offers.collectAsStateWithLifecycle(lifecycle = lifecycle)
    val debugProExpiresAtMillis by preferences.debugProExpiresAtMillis.collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    val proEntitlement = remember(subscriptionEntitlement, debugProExpiresAtMillis) {
        val now = System.currentTimeMillis()
        val debugExpiresAt = debugProExpiresAtMillis
        if (BuildConfig.DEBUG && debugExpiresAt != null && now <= debugExpiresAt) {
            ProEntitlementState.active(
                purchaseToken = "debug:lab",
                expiresAtMillis = debugExpiresAt,
                source = "debug_lab",
            )
        } else {
            subscriptionEntitlement
        }
    }
    val superModeStatus = remember(superModeStoredState, proEntitlement.isProActive, currentTimeMillis) {
        superModeController.buildStatus(
            storedState = superModeStoredState,
            isProActive = proEntitlement.isProActive,
            nowMillis = currentTimeMillis,
        )
    }
    val currentTimeLabel = remember(currentTimeMillis) {
        val localTime = java.time.Instant.ofEpochMilli(currentTimeMillis).atZone(ZoneId.systemDefault()).toLocalTime()
        superModeController.formatTime(localTime.hour * 60 + localTime.minute)
    }

    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var rewardsSection by remember { mutableStateOf(RewardsSection.STORE) }
    val snackbarHostState = remember { SnackbarHostState() }
    var proUpsellSource by remember { mutableStateOf<ProUpsellSource?>(null) }
    var openMeProBenefitsDialog by remember { mutableStateOf(false) }
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
    val meHistoricalArchives by dailyArchiveRepository.getRecentArchives(limit = 3650)
        .collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val meTotalSavedMinutes = remember(meHistoricalArchives) {
        meHistoricalArchives.sumOf { it.savedMillis } / 60_000L
    }

    var showYesterdaySummary by remember { mutableStateOf(false) }
    var yesterdaySavedMinutes by remember { mutableIntStateOf(0) }
    var showSuperModeSettings by remember { mutableStateOf(false) }
    var showSuperModeCredentialDialog by remember { mutableStateOf(false) }
    var isEditingSuperModeCredentials by remember { mutableStateOf(false) }
    var showSuperModePasswordDialog by remember { mutableStateOf(false) }
    var showSuperModeUnavailableDialog by remember { mutableStateOf(false) }
    var showSuperModeSetupDialog by remember { mutableStateOf(false) }
    var showSuperModeRecoveryDialog by remember { mutableStateOf(false) }
    var showSuperModeWindowDialog by remember { mutableStateOf(false) }
    var showSuperModeDisableDialog by remember { mutableStateOf(false) }
    var superModePasswordError by remember { mutableStateOf<String?>(null) }
    var superModeRecoveryError by remember { mutableStateOf<String?>(null) }
    var superModeWindowError by remember { mutableStateOf<String?>(null) }
    var setupRequiredActionLabel by remember { mutableStateOf(AppText.t("super_mode_title")) }
    var pendingSuperModeRequest by remember { mutableStateOf<PendingSuperModeRequest?>(null) }

    val isAutoStartDismissed by preferences.isAutoStartDismissed.collectAsStateWithLifecycle(initialValue = false, lifecycle = lifecycle)

    fun openUsageAccessSettingsNow() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openAccessibilitySettingsNow() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun requestUsageAccessSettings() {
        if (usageAccessDisclosureAccepted) {
            openUsageAccessSettingsNow()
        } else {
            pendingSensitiveDisclosure = SensitivePermissionDisclosure.USAGE_ACCESS
        }
    }

    fun requestAccessibilitySettings() {
        if (accessibilityDisclosureAccepted) {
            openAccessibilitySettingsNow()
        } else {
            pendingSensitiveDisclosure = SensitivePermissionDisclosure.ACCESSIBILITY
        }
    }

    fun clearPendingSuperModeRequest() {
        pendingSuperModeRequest = null
        superModePasswordError = null
    }

    fun requestSuperModeSession(
        message: String,
        onAllowed: (() -> Unit)? = null,
    ) {
        if (!superModeStatus.isConfigured) {
            clearPendingSuperModeRequest()
            setupRequiredActionLabel = AppText.t("super_mode_title")
            showSuperModeSetupDialog = true
            return
        }
        if (!superModeStatus.isEnabled) {
            clearPendingSuperModeRequest()
            onAllowed?.invoke()
            return
        }
        if (superModeStatus.isActive) {
            onAllowed?.invoke()
            return
        }
        if (!superModeStatus.isAvailableNow) {
            clearPendingSuperModeRequest()
            showSuperModeUnavailableDialog = true
            return
        }
        pendingSuperModeRequest = PendingSuperModeRequest(message = message, onAllowed = onAllowed)
        superModePasswordError = null
        showSuperModePasswordDialog = true
    }

    fun runWithSuperModeGuard(
        action: GuardedAction,
        onAllowed: () -> Unit,
    ) {
        if (!superModeStatus.isConfigured) {
            clearPendingSuperModeRequest()
            setupRequiredActionLabel = guardedActionLabel(action)
            showSuperModeSetupDialog = true
            return
        }
        if (!superModeStatus.isEnabled) {
            clearPendingSuperModeRequest()
            onAllowed()
            return
        }
        if (superModeStatus.isActive) {
            onAllowed()
            return
        }
        if (!superModeStatus.isAvailableNow) {
            clearPendingSuperModeRequest()
            showSuperModeUnavailableDialog = true
            return
        }
        pendingSuperModeRequest =
            PendingSuperModeRequest(
                message = AppText.t("super_mode_enter_for_action", guardedActionLabel(action)),
                onAllowed = onAllowed,
            )
        superModePasswordError = null
        showSuperModePasswordDialog = true
    }

    fun openSuperModeSettings(configureImmediately: Boolean = false) {
        currentScreen = Screen.ME
        showSuperModeSettings = true
        if (configureImmediately) {
            isEditingSuperModeCredentials = false
            showSuperModeCredentialDialog = true
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        notificationPermissionGranted = notificationPermissionChecker.isGranted()
    }

    DisposableEffect(lifecycleOwner, checker, superModeStoredState.isActive) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                usageAccessStatus = checker.getStatus()
                accessibilityServiceEnabled =
                    accessibilityServiceStateChecker.isEnabled(AppLimitAccessibilityService::class.java)
                notificationPermissionGranted = notificationPermissionChecker.isGranted()
                isIgnoringBattery = powerManager.isIgnoringBatteryOptimizations(context.packageName)
                coroutineScope.launch {
                    subscriptionRepository.refresh()
                }
            } else if (event == Lifecycle.Event.ON_STOP && superModeStoredState.isActive) {
                coroutineScope.launch {
                    superModeController.exit(SuperModeExitReason.BACKGROUND)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(superModeStoredState.isActive, superModeStatus.isActive, superModeStatus.isAvailableNow) {
        if (superModeStoredState.isActive && !superModeStatus.isActive) {
            val reason =
                if (!superModeStatus.isAvailableNow) {
                    SuperModeExitReason.OUTSIDE_ALLOWED_WINDOW
                } else {
                    SuperModeExitReason.IDLE_TIMEOUT
                }
            superModeController.exit(reason)
        }
    }

    LaunchedEffect(Unit) {
        appLimitRepository.clearExpiredBonusTime(System.currentTimeMillis())
        dailyArchiveRepository.ensureArchivesUpToYesterday()
        
        // Daily summary logic.
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

    // Check achievements once at startup to avoid expensive DB scans on every point update.
    LaunchedEffect(Unit) {
        appLimitRepository.syncBuiltinRewardsV2()
        appLimitRepository.syncAchievementDefinitions()
        appLimitRepository.checkAchievements()
    }

    var newlyUnlockedAchievement by remember { mutableStateOf<AchievementEntity?>(null) }
    val presentAchievementBanner: (AchievementEntity) -> Unit = { achievement ->
        coroutineScope.launch {
            newlyUnlockedAchievement = achievement
            kotlinx.coroutines.delay(5000)
            if (newlyUnlockedAchievement?.id == achievement.id) {
                newlyUnlockedAchievement = null
            }
        }
    }
    LaunchedEffect(Unit) {
        appLimitRepository.newAchievementsAction.collectLatest { achievement ->
            presentAchievementBanner(achievement)
        }
    }

    if (currentScreen != Screen.HOME) {
        BackHandler {
            if (currentScreen == Screen.REWARDS && rewardsSection != RewardsSection.STORE) {
                rewardsSection = RewardsSection.STORE
            } else {
                if (currentScreen == Screen.REWARDS) {
                    rewardsSection = RewardsSection.STORE
                }
                currentScreen = when (currentScreen) {
                    Screen.LABORATORY, Screen.HISTORY, Screen.THEME, Screen.HELP_FEEDBACK, Screen.CONTACT_US -> Screen.ME
                    else -> Screen.HOME
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        subscriptionRepository.refresh()
    }

    LaunchedEffect(BuildConfig.ENABLE_LOCAL_ACTIVATION) {
        if (BuildConfig.ENABLE_LOCAL_ACTIVATION) {
            authRepository.ensureLocalSession()
        }
    }

    LaunchedEffect(BuildConfig.ENABLE_LOCAL_ACTIVATION, userSession?.userId) {
        if (BuildConfig.ENABLE_LOCAL_ACTIVATION) {
            localActivationRepository?.bindUser(userSession?.userId)
        }
    }

    LaunchedEffect(proEntitlement.isProActive, selectedThemeId, customThemes) {
        if (!proEntitlement.isProActive) {
            val customIndex = customThemes.indexOfFirst { it.id == selectedThemeId }
            val selectedThemeLocked =
                ProFeatureGate.isMemberTheme(selectedThemeId) ||
                    (customIndex >= ProFeatureGate.limits(false).customThemeLimit)
            if (selectedThemeLocked) {
                preferences.setSelectedThemeId(DefaultThemeSeed.id)
            }
        }
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
                            onClick = {
                                if (screen == Screen.REWARDS) {
                                    rewardsSection = RewardsSection.STORE
                                }
                                currentScreen = screen
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
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
                        superModeStatus = superModeStatus,
                        onNavigateToRedeem = {
                            rewardsSection = RewardsSection.STORE
                            currentScreen = Screen.REWARDS
                        },
                        onNavigateToAchievements = {
                            rewardsSection = RewardsSection.ACHIEVEMENTS
                            currentScreen = Screen.REWARDS
                        },
                        onOpenSuperModeEntry = {
                            when {
                                !superModeStatus.isConfigured -> openSuperModeSettings(configureImmediately = true)
                                superModeStatus.isActive -> showSuperModeSettings = true
                                else -> requestSuperModeSession(AppText.t("super_mode_enter_from_home"))
                            }
                        },
                        onOpenUsageAccessSettings = { requestUsageAccessSettings() },
                        onOpenAccessibilitySettings = { requestAccessibilitySettings() },
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
                                if (id != null) {
                                    superModeController.touch(proEntitlement.isProActive)
                                }
                            }
                        },
                        onDeleteGroup = { id ->
                            coroutineScope.launch {
                                appLimitRepository.deleteGroup(id)
                                superModeController.touch(proEntitlement.isProActive)
                            }
                        },
                        onGuardAction = ::runWithSuperModeGuard,
                        achievementProgress = achievementProgress,
                        appLimitRepository = appLimitRepository,
                        archiveRepository = dailyArchiveRepository,
                        isProActive = proEntitlement.isProActive,
                        onShowProUpsell = { proUpsellSource = it },
                        modifier = modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                    )
                }
                Screen.REWARDS -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    ) {
                        RewardsHome(
                            userPoints = userPoints,
                            achievements = achievements,
                            achievementProgress = achievementProgress,
                            storeItems = storeRewardItems,
                            inventoryItems = inventoryRewardItems,
                            pendingShieldItems = pendingShieldItems,
                            groups = groupsWithApps,
                            redemptionHistory = redemptionHistory,
                            rewardUseHistory = rewardUseHistory,
                            onPurchaseReward = { reward ->
                                coroutineScope.launch {
                                    val result = appLimitRepository.purchaseReward(reward.id)
                                    snackbarHostState.showSnackbar(
                                        purchaseRewardResultMessage(result)
                                    )
                                }
                            },
                        onUseInventoryReward = { reward, groupId ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    useRewardResultMessage(appLimitRepository.useInventoryReward(reward.id, groupId))
                                )
                            }
                        },
                        onResolvePendingShield = { pendingId, useShield ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    useRewardResultMessage(appLimitRepository.resolvePendingStreakShield(pendingId, useShield))
                                )
                            }
                        },
                        onAddReward = { draft ->
                            coroutineScope.launch {
                                val result = appLimitRepository.addCustomReward(draft)
                                if (result == RewardSaveResult.Success) {
                                    superModeController.touch(proEntitlement.isProActive)
                                }
                                rewardSaveResultMessage(result)?.let { snackbarHostState.showSnackbar(it) }
                            }
                        },
                        onUpdateReward = { reward ->
                            coroutineScope.launch {
                                val result = appLimitRepository.updateReward(reward)
                                if (result == RewardSaveResult.Success) {
                                    superModeController.touch(proEntitlement.isProActive)
                                }
                                rewardSaveResultMessage(result)
                                    ?.let { snackbarHostState.showSnackbar(it) }
                            }
                        },
                        onArchiveReward = { reward ->
                            coroutineScope.launch {
                                appLimitRepository.archiveReward(reward.id)
                                snackbarHostState.showSnackbar(AppText.t("redeem_archived_reward"))
                            }
                        },
                            isProActive = proEntitlement.isProActive,
                            onShowProUpsell = { proUpsellSource = it },
                            onGuardAction = ::runWithSuperModeGuard,
                            currentSection = rewardsSection,
                            onSectionChange = { rewardsSection = it },
                        )
                    }
                }
                Screen.STATS -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    ) {
                        StatsRoute(
                            usageAccessStatus = usageAccessStatus,
                            groupsWithApps = groupsWithApps,
                            userPoints = userPoints,
                            todayPoints = todayPoints,
                            archiveRepository = dailyArchiveRepository,
                            isProActive = proEntitlement.isProActive,
                            onShowProUpsell = { proUpsellSource = it },
                            onRequestUsageAccess = { requestUsageAccessSettings() },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                Screen.ME -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    ) {
                        MeScreen(
                        userSession = userSession,
                        isGoogleSignInEnabled = BuildConfig.ENABLE_GOOGLE_LOGIN,
                        isGoogleSignInConfigured = authRepository.isGoogleSignInConfigured,
                        isPlayBillingEnabled = BuildConfig.ENABLE_PLAY_BILLING,
                        isLocalActivationEnabled = BuildConfig.ENABLE_LOCAL_ACTIVATION,
                        appVersionName = BuildConfig.VERSION_NAME,
                        appVersionCode = BuildConfig.VERSION_CODE,
                        proEntitlement = proEntitlement,
                        subscriptionOffers = subscriptionOffers,
                        totalSavedMinutes = meTotalSavedMinutes,
                        totalEarnedPoints = achievementProgress.earnedPointsTotal,
                        profileDisplayName = profileDisplayName,
                        profileAvatarUri = profileAvatarUri,
                        selectedThemeId = selectedThemeId,
                        customThemes = customThemes,
                        isProActive = proEntitlement.isProActive,
                        superModeStatus = superModeStatus,
                        isDebugBuild = BuildConfig.DEBUG,
                        selectedAppLanguage = selectedAppLanguage,
                        openBenefitsDialog = openMeProBenefitsDialog,
                        onBenefitsDialogOpened = { openMeProBenefitsDialog = false },
                        usageAccessGranted = effectiveUsageAccessStatus == UsageAccessStatus.GRANTED,
                        accessibilityServiceEnabled = effectiveAccessibilityServiceEnabled,
                        isAutoStartDismissed = isAutoStartDismissed,
                        isIgnoringBattery = isIgnoringBattery,
                        notificationPermissionGranted = notificationPermissionGranted,
                        dismissedPermissionPrompts = dismissedPermissionPrompts,
                        onSelectAppLanguage = { language ->
                            coroutineScope.launch {
                                preferences.setSelectedAppLanguage(language)
                            }
                        },
                        onUpdateProfileName = { displayName ->
                            coroutineScope.launch {
                                preferences.setProfileDisplayName(displayName)
                            }
                        },
                        onUpdateProfileAvatar = { avatarUri ->
                            coroutineScope.launch {
                                preferences.setProfileAvatarUri(avatarUri)
                            }
                        },
                        onClearProfileAvatar = {
                            coroutineScope.launch {
                                preferences.setProfileAvatarUri(null)
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
                        onShowProUpsell = { proUpsellSource = it },
                        onOpenSuperModeSettings = { showSuperModeSettings = true },
                        onOpenUsageAccessSettings = { requestUsageAccessSettings() },
                        onOpenAccessibilitySettings = { requestAccessibilitySettings() },
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
                                    if (BuildConfig.ENABLE_LOCAL_ACTIVATION) {
                                        authRepository.deleteAccount()
                                        localActivationRepository?.clearActivationData()
                                    }
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
                                if (BuildConfig.ENABLE_LOCAL_ACTIVATION) {
                                    localActivationRepository?.clearActivationData()
                                }
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
                                    subscriptionRepository.purchase(activity, offer, userSession?.userId)
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
                        onActivateProCode = { code ->
                            coroutineScope.launch {
                                val localUserId = userSession?.userId ?: authRepository.ensureLocalSession().userId
                                localActivationRepository
                                    ?.activate(localUserId, code)
                                    ?.onSuccess {
                                        snackbarHostState.showSnackbar(AppText.t("activation_pro_activated"))
                                    }
                                    ?.onFailure {
                                        snackbarHostState.showSnackbar(AppText.t("activation_code_invalid"))
                                    }
                            }
                        },
                        )
                    }
                }
                Screen.THEME -> {
                     ThemeSettingsScreen(
                         selectedThemeId = selectedThemeId,
                         customThemes = customThemes,
                         isProActive = proEntitlement.isProActive,
                         isLocalActivationEnabled = BuildConfig.ENABLE_LOCAL_ACTIVATION,
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
                        onShowProUpsell = { proUpsellSource = it },
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
                            }
                        },
                        onTriggerAchievementPopupTest = {
                            coroutineScope.launch {
                                val sampleAchievement =
                                    achievements.firstOrNull { it.id == "DIAMOND_POINTS" }
                                        ?: achievements.firstOrNull { it.tier == AchievementTier.DIAMOND }
                                        ?: achievements.firstOrNull()
                                if (sampleAchievement == null) {
                                    snackbarHostState.showSnackbar(AppText.t("lab_achievement_test_unavailable"))
                                    return@launch
                                }
                                presentAchievementBanner(
                                    sampleAchievement.copy(
                                        isUnlocked = true,
                                        unlockedAt = System.currentTimeMillis(),
                                    )
                                )
                            }
                        },
                        onResetSummary = { coroutineScope.launch { preferences.setLastSummaryShownDate("reset") } },
                        onTriggerSummary = { showYesterdaySummary = true },
                        showDebugProControls = BuildConfig.DEBUG,
                        onExtendDebugPro = { days ->
                            coroutineScope.launch {
                                preferences.extendDebugPro(days, proEntitlement.expiresAtMillis)
                                snackbarHostState.showSnackbar(AppText.t("lab_debug_pro_extended", days))
                            }
                        },
                        onClearDebugPro = {
                            coroutineScope.launch {
                                preferences.clearDebugPro()
                                snackbarHostState.showSnackbar(AppText.t("lab_debug_pro_cleared"))
                            }
                        },
                        showDebugSuperModeControls = BuildConfig.DEBUG,
                        onEnterSuperMode = {
                            coroutineScope.launch {
                                superModeController.enterForDebug(proEntitlement.isProActive)
                                snackbarHostState.showSnackbar(AppText.t("super_mode_enter_success"))
                            }
                        },
                        onBack = { currentScreen = Screen.ME }
                    )
                }
            }
        }
    }

    if (showSuperModeSettings) {
        SuperModeSettingsSheet(
            status = superModeStatus,
            isProActive = proEntitlement.isProActive,
            currentTimeLabel = currentTimeLabel,
            recoveryQuestion = superModeStoredState.recoveryQuestion,
            onDismiss = { showSuperModeSettings = false },
            onConfigure = {
                isEditingSuperModeCredentials = false
                showSuperModeCredentialDialog = true
            },
            onSetEnabled = { enabled ->
                coroutineScope.launch {
                    preferences.setSuperModeEnabled(enabled)
                    snackbarHostState.showSnackbar(
                        if (enabled) {
                            AppText.t("super_mode_enabled_success")
                        } else {
                            AppText.t("super_mode_disabled_toggle_success")
                        }
                    )
                }
            },
            onEnter = {
                requestSuperModeSession(AppText.t("super_mode_enter_for_settings"))
            },
            onExit = {
                coroutineScope.launch {
                    superModeController.exit(SuperModeExitReason.MANUAL)
                    snackbarHostState.showSnackbar(AppText.t("super_mode_exit_success"))
                }
            },
            onEditCredentials = {
                requestSuperModeSession(AppText.t("super_mode_enter_to_edit_credentials")) {
                    isEditingSuperModeCredentials = true
                    showSuperModeCredentialDialog = true
                }
            },
            onEditWindow = {
                if (!proEntitlement.isProActive) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(AppText.t("super_mode_window_pro_only"))
                    }
                } else {
                    requestSuperModeSession(AppText.t("super_mode_enter_to_edit_window")) {
                        superModeWindowError = null
                        showSuperModeWindowDialog = true
                    }
                }
            },
            onRecoveryReset = {
                superModeRecoveryError = null
                showSuperModeRecoveryDialog = true
            },
            onDisable = {
                requestSuperModeSession(AppText.t("super_mode_enter_to_disable")) {
                    showSuperModeDisableDialog = true
                }
            },
        )
    }

    if (showSuperModeCredentialDialog) {
        SuperModeCredentialDialog(
            initialQuestion = superModeStoredState.recoveryQuestion.orEmpty(),
            isEditing = isEditingSuperModeCredentials,
            onDismiss = { showSuperModeCredentialDialog = false },
            onConfirm = { password, question, answer ->
                coroutineScope.launch {
                    superModeController.updateCredentials(password, question, answer)
                    showSuperModeCredentialDialog = false
                    snackbarHostState.showSnackbar(
                        if (isEditingSuperModeCredentials) {
                            AppText.t("super_mode_edit_credentials_success")
                        } else {
                            AppText.t("super_mode_configured_success")
                        }
                    )
                }
            },
        )
    }

    if (showSuperModePasswordDialog) {
        SuperModePasswordDialog(
            title = AppText.t("super_mode_enter_title"),
            message = pendingSuperModeRequest?.message ?: AppText.t("super_mode_enter_hint"),
            errorMessage = superModePasswordError,
            onDismiss = {
                showSuperModePasswordDialog = false
                clearPendingSuperModeRequest()
            },
            onConfirm = { password ->
                coroutineScope.launch {
                    when (val result = superModeController.enter(password, proEntitlement.isProActive)) {
                        is SuperModeEnterResult.Success -> {
                            showSuperModePasswordDialog = false
                            val nextAction = pendingSuperModeRequest?.onAllowed
                            clearPendingSuperModeRequest()
                            snackbarHostState.showSnackbar(AppText.t("super_mode_enter_success"))
                            nextAction?.invoke()
                        }
                        SuperModeEnterResult.IncorrectPassword -> {
                            superModePasswordError = AppText.t("super_mode_password_incorrect")
                        }
                        SuperModeEnterResult.OutsideAllowedWindow -> {
                            showSuperModePasswordDialog = false
                            clearPendingSuperModeRequest()
                            showSuperModeUnavailableDialog = true
                        }
                        SuperModeEnterResult.NotConfigured -> {
                            showSuperModePasswordDialog = false
                            clearPendingSuperModeRequest()
                            openSuperModeSettings(configureImmediately = true)
                        }
                    }
                }
            },
        )
    }

    if (showSuperModeUnavailableDialog) {
        SuperModeUnavailableDialog(
            currentTimeLabel = currentTimeLabel,
            windowLabel = superModeStatus.windowLabel,
            onDismiss = { showSuperModeUnavailableDialog = false },
        )
    }

    if (showSuperModeSetupDialog) {
        SuperModeSetupRequiredDialog(
            actionLabel = setupRequiredActionLabel,
            onDismiss = { showSuperModeSetupDialog = false },
            onOpenSettings = {
                showSuperModeSetupDialog = false
                openSuperModeSettings(configureImmediately = true)
            },
        )
    }

    if (showSuperModeRecoveryDialog) {
        SuperModeRecoveryResetDialog(
            question = superModeStoredState.recoveryQuestion.orEmpty(),
            errorMessage = superModeRecoveryError,
            onDismiss = { showSuperModeRecoveryDialog = false },
            onConfirm = { answer ->
                coroutineScope.launch {
                    when (superModeController.resetWithRecovery(answer)) {
                        SuperModeRecoveryResult.Success -> {
                            showSuperModeRecoveryDialog = false
                            showSuperModeSettings = false
                            snackbarHostState.showSnackbar(AppText.t("super_mode_reset_success"))
                        }
                        SuperModeRecoveryResult.IncorrectAnswer -> {
                            superModeRecoveryError = AppText.t("super_mode_recovery_answer_incorrect")
                        }
                        SuperModeRecoveryResult.NotConfigured -> {
                            showSuperModeRecoveryDialog = false
                        }
                    }
                }
            },
        )
    }

    if (showSuperModeWindowDialog) {
        SuperModeWindowDialog(
            initialStartMinutes = superModeStatus.windowStartMinutes,
            initialEndMinutes = superModeStatus.windowEndMinutes,
            errorMessage = superModeWindowError,
            onDismiss = { showSuperModeWindowDialog = false },
            onConfirm = { startMinutes, endMinutes ->
                coroutineScope.launch {
                    when (superModeController.updateWindow(startMinutes, endMinutes, proEntitlement.isProActive)) {
                        SuperModeWindowUpdateResult.Success -> {
                            showSuperModeWindowDialog = false
                            snackbarHostState.showSnackbar(AppText.t("super_mode_window_saved"))
                        }
                        SuperModeWindowUpdateResult.InvalidWindow -> {
                            superModeWindowError = AppText.t("super_mode_window_invalid")
                        }
                        SuperModeWindowUpdateResult.ProRequired -> {
                            superModeWindowError = AppText.t("super_mode_window_pro_only")
                        }
                    }
                }
            },
        )
    }

    if (showSuperModeDisableDialog) {
        AlertDialog(
            onDismissRequest = { showSuperModeDisableDialog = false },
            title = { Text(AppText.t("super_mode_disable_confirm_title")) },
            text = { Text(AppText.t("super_mode_disable_confirm_body")) },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            superModeController.clearConfiguration()
                            showSuperModeDisableDialog = false
                            showSuperModeSettings = false
                            snackbarHostState.showSnackbar(AppText.t("super_mode_disabled_success"))
                        }
                    },
                ) {
                    Text(AppText.t("super_mode_disable_action"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuperModeDisableDialog = false }) {
                    Text(AppText.t("group_cancel"))
                }
            },
        )
    }

    proUpsellSource?.let { source ->
        ProUpsellDialog(
            source = source,
            onViewBenefits = {
                proUpsellSource = null
                currentScreen = Screen.ME
                openMeProBenefitsDialog = true
            },
            onDismiss = { proUpsellSource = null },
        )
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
                    Text(
                        text = AppText.t("home_yesterday_s_report"),
                        style = MaterialTheme.typography.titleLarge,
                    )
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
                            style = MaterialTheme.typography.headlineMedium,
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

        // Achievement unlock banner.
        AnimatedVisibility(
            visible = newlyUnlockedAchievement != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.fillMaxWidth()
        ) {
            newlyUnlockedAchievement?.let { achievement ->
                Popup(alignment = Alignment.TopCenter) {
                    AchievementNotificationBanner(achievement)
                }
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
                            openUsageAccessSettingsNow()
                        }
                        SensitivePermissionDisclosure.ACCESSIBILITY -> {
                            preferences.setAccessibilityDisclosureAccepted(true)
                            pendingSensitiveDisclosure = null
                            openAccessibilitySettingsNow()
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
            .fillMaxWidth(0.94f)
            .wrapContentHeight()
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(20.dp))
        ) {
            // 鍏夋辰鎵厜鏁堟灉
            Canvas(modifier = Modifier.matchParentSize()) {
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
                AchievementBadge(
                    achievement = achievement,
                    modifier = Modifier.size(56.dp),
                    animated = true,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        tierLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = tierGradient.first()
                    )
                    Text(
                        achievement.localizedAchievementTitle(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        achievement.localizedAchievementDescription(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

}

private fun AchievementEntity.localizedAchievementTitle(): String {
    val key = "achievement_${id.lowercase()}_title"
    val value = AppText.t(key)
    return if (value == key) title else value
}

private fun AchievementEntity.localizedAchievementDescription(): String {
    val key = "achievement_${id.lowercase()}_desc"
    val value = AppText.t(key)
    return if (value == key) description else value
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
    superModeStatus: com.rrrrz.tinyvow.data.supermode.SuperModeStatus,
    onNavigateToRedeem: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onOpenSuperModeEntry: () -> Unit,
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
    onGuardAction: (GuardedAction, () -> Unit) -> Unit,
    achievementProgress: AchievementProgress = AchievementProgress(),
    appLimitRepository: AppLimitRepository? = null,
    archiveRepository: DailyArchiveRepository? = null,
    isProActive: Boolean,
    onShowProUpsell: (ProUpsellSource) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val coroutineScope = rememberCoroutineScope()
    val homeScrollState = rememberScrollState()
    var usageMap by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    val historicalArchives =
        archiveRepository?.let { repository ->
            val archives by repository.getRecentArchives(limit = 3650).collectAsStateWithLifecycle(
                initialValue = emptyList(),
                lifecycle = lifecycle,
            )
            archives
        } ?: emptyList()
    
    // Periodically refresh group usage by querying UsageStats once and aggregating packages in memory.
    LaunchedEffect(groupsWithApps, usageAccessStatus) {
        if (usageAccessStatus != UsageAccessStatus.GRANTED) {
            usageMap = emptyMap()
            return@LaunchedEffect
        }
        val usageRepo = UsageStatsUsageRepository(context)
        while (true) {
            val todayStart = java.time.LocalDate.now(java.time.ZoneId.systemDefault())
                .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            // 涓€娆℃€ц幏寰楁墍鏈夊寘鍚嶇殑鐢ㄩ噺 Map
            val allUsage = usageRepo.getUsageStats(todayStart, System.currentTimeMillis())
            val newMap = mutableMapOf<String, Long>()
            groupsWithApps.forEach { groupWithApps ->
                newMap[groupWithApps.group.id] =
                    groupWithApps.packageNames.sumOf { allUsage[it] ?: 0L }
            }
            usageMap = newMap
            kotlinx.coroutines.delay(5000L) // 5绉掑埛鏂颁竴娆?
        }
    }
    val usageAccessGranted = usageAccessStatus == UsageAccessStatus.GRANTED
    val statusColor = if (usageAccessGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    var showDiagnosticMenu by remember { mutableStateOf(false) }
    val overviewState =
        remember(
            context,
            groupsWithApps,
            usageMap,
            historicalArchives,
            userPoints,
            todayPoints,
            achievementProgress,
        ) {
            buildHomeOverviewUiState(
                context = context,
                groupsWithApps = groupsWithApps,
                usageMap = usageMap,
                historicalArchives = historicalArchives,
                userPoints = userPoints,
                todayPoints = todayPoints,
                achievementProgress = achievementProgress,
            )
        }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(homeScrollState)
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
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (usageAccessGranted) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = Color.Transparent,
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                    ) {
                        HomeOverviewCard(
                            state = overviewState,
                            modifier = Modifier.fillMaxWidth(),
                        )
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
                val dashboardTopSpacing = if (pendingPermissionPrompts.isEmpty()) 16.dp else 8.dp
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = dashboardTopSpacing),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
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
                        isProActive = isProActive,
                        onShowProUpsell = onShowProUpsell,
                        onGuardAction = onGuardAction,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        text =
                            AppText.t(
                                "home_surprise_footer_format",
                                AppText.t("group_commitment"),
                                overviewState.tagline,
                            ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.84f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier =
                            Modifier
                                .fillMaxWidth(),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp),
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

            Spacer(modifier = Modifier.height(20.dp))
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
private fun HomeOverviewCard(
    state: HomeOverviewUiState,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(32.dp))
                .background(
                    brush =
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    lerp(MaterialTheme.colorScheme.surface, themeColors.controlContainer, 0.28f),
                                    lerp(MaterialTheme.colorScheme.surface, themeColors.baseContainer, 0.34f),
                                    lerp(MaterialTheme.colorScheme.surface, themeColors.encourageContainer, 0.30f),
                                ),
                            start = Offset(0f, 0f),
                            end = Offset(920f, 720f),
                        )
                )
    ) {
        HomeOverviewBackdrop(modifier = Modifier.matchParentSize())

        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HomeOverviewHeader(
                dateLabel = state.dateLabel,
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(170.dp),
            ) {
                HomeOverviewClockWatermark(
                    modifier =
                        Modifier
                            .size(176.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = 4.dp),
                )

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(top = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    HomeOverviewSideMetric(
                        title = AppText.t("home_commitment_panel"),
                        headlineLabel = AppText.t("home_saved_today"),
                        headlineValue = state.control.todaySavedMinutes.toString(),
                        headlineUnit = AppText.t("group_minutes"),
                        accent = MaterialTheme.colorScheme.secondary,
                        tags =
                            listOf(
                                AppText.t("home_commitment_progress_value", state.control.completedGroups, state.control.totalGroups),
                                AppText.t("home_control_streak_value", state.control.streakDays),
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    HomeOverviewSideMetric(
                        title = AppText.t("home_encouragement_panel"),
                        headlineLabel = AppText.t("home_earned_today"),
                        headlineValue = formatHomePointWholeValue(state.encourage.todayEarnedPoints),
                        headlineUnit = AppText.t("group_points"),
                        accent = MaterialTheme.colorScheme.tertiary,
                        tags =
                            listOf(
                                AppText.t("home_encouragement_progress_value", state.encourage.completedGroups, state.encourage.totalGroups),
                                AppText.t("home_encourage_streak_value", state.encourage.streakDays),
                            ),
                        alignEnd = true,
                        unitBefore = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.50f),
                border = BorderStroke(1.dp, themeColors.base.copy(alpha = 0.12f)),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.spacedBy(42.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        HomeHistoryMetric(
                            label = AppText.t("home_total_saved"),
                            value = state.history.totalSavedMinutes.toString(),
                            unit = AppText.t("group_minutes"),
                            accent = MaterialTheme.colorScheme.secondary,
                        )
                        HomeHistoryMetric(
                            label = AppText.t("home_equivalent_live_more"),
                            value = roundedDaysValue(state.history.extendedLifeMinutes).toString(),
                            unit = AppText.t("home_day_unit"),
                            accent = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        HomeHistoryMetric(
                            label = AppText.t("home_total_earned"),
                            value = formatHomePointValue(state.history.totalEarnedPoints),
                            unit = AppText.t("group_points"),
                            accent = MaterialTheme.colorScheme.tertiary,
                            alignEnd = true,
                            unitBefore = true,
                        )
                        HomeHistoryMetric(
                            label = AppText.t("home_current_remaining"),
                            value = formatHomePointValue(state.history.currentPoints),
                            unit = AppText.t("group_points"),
                            accent = MaterialTheme.colorScheme.tertiary,
                            alignEnd = true,
                            unitBefore = true,
                        )
                    }
                }
            }

        }
    }
}

@Composable
private fun HomeOverviewBackdrop(
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    Canvas(modifier = modifier) {
        drawCircle(
            brush =
                Brush.radialGradient(
                    colors = listOf(themeColors.controlContainer.copy(alpha = 0.72f), Color.Transparent),
                    center = Offset(size.width * 0.10f, size.height * 0.42f),
                    radius = size.width * 0.58f,
                ),
            radius = size.width * 0.58f,
            center = Offset(size.width * 0.10f, size.height * 0.42f),
        )
        drawCircle(
            brush =
                Brush.radialGradient(
                    colors = listOf(themeColors.encourageContainer.copy(alpha = 0.74f), Color.Transparent),
                    center = Offset(size.width * 0.92f, size.height * 0.46f),
                    radius = size.width * 0.56f,
                ),
            radius = size.width * 0.56f,
            center = Offset(size.width * 0.92f, size.height * 0.46f),
        )
        drawCircle(
            brush =
                Brush.radialGradient(
                    colors = listOf(themeColors.baseContainer.copy(alpha = 0.58f), Color.Transparent),
                    center = Offset(size.width * 0.52f, size.height * 0.74f),
                    radius = size.width * 0.62f,
                ),
            radius = size.width * 0.62f,
            center = Offset(size.width * 0.52f, size.height * 0.74f),
        )
    }
}

@Composable
private fun HomeOverviewClockWatermark(
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val time by produceState(initialValue = LocalTime.now()) {
        while (true) {
            val current = LocalTime.now()
            value = current
            delay(((60 - current.second).coerceAtLeast(1) * 1000).toLong())
        }
    }

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = min(size.width, size.height) * 0.36f
        val outerRadius = radius * 1.10f
        val ringGradient =
            Brush.horizontalGradient(
                colors =
                    listOf(
                        themeColors.control.copy(alpha = 0.58f),
                        themeColors.base.copy(alpha = 0.22f),
                        themeColors.encourage.copy(alpha = 0.58f),
                    ),
                startX = center.x - outerRadius,
                endX = center.x + outerRadius,
            )

        drawCircle(
            brush =
                Brush.radialGradient(
                    colors =
                        listOf(
                            surfaceColor.copy(alpha = 0.18f),
                            themeColors.baseContainer.copy(alpha = 0.06f),
                            Color.Transparent,
                        ),
                    center = center,
                    radius = outerRadius * 1.12f,
                ),
            radius = outerRadius * 1.12f,
            center = center,
        )
        drawCircle(
            brush = ringGradient,
            radius = radius,
            center = center,
            style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round),
        )
        drawCircle(
            color = surfaceColor.copy(alpha = 0.34f),
            radius = radius * 0.90f,
            center = center,
            style = Stroke(width = 1.dp.toPx()),
        )

        val minuteAngle = (time.minute / 60.0) * 2.0 * PI - PI / 2.0
        val hourAngle = (((time.hour % 12) + time.minute / 60.0) / 12.0) * 2.0 * PI - PI / 2.0
        val handColor = lerp(themeColors.base, themeColors.encourage, 0.30f)
        drawLine(
            color = handColor.copy(alpha = 0.46f),
            start = center,
            end = Offset(
                x = center.x + cos(hourAngle).toFloat() * radius * 0.48f,
                y = center.y + sin(hourAngle).toFloat() * radius * 0.48f,
            ),
            strokeWidth = 5.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = handColor.copy(alpha = 0.38f),
            start = center,
            end = Offset(
                x = center.x + cos(minuteAngle).toFloat() * radius * 0.68f,
                y = center.y + sin(minuteAngle).toFloat() * radius * 0.68f,
            ),
            strokeWidth = 3.4.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = surfaceColor.copy(alpha = 0.56f),
            radius = 5.8.dp.toPx(),
            center = center,
        )
        drawCircle(
            color = outlineColor.copy(alpha = 0.34f),
            radius = 3.2.dp.toPx(),
            center = center,
        )
    }
}

@Composable
private fun HomeOverviewSideMetric(
    title: String,
    headlineLabel: String,
    headlineValue: String,
    headlineUnit: String,
    accent: Color,
    tags: List<String>,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
    unitBefore: Boolean = false,
) {
    val textAlign =
        if (alignEnd) {
            androidx.compose.ui.text.style.TextAlign.End
        } else {
            androidx.compose.ui.text.style.TextAlign.Start
        }
    val horizontalAlignment =
        if (alignEnd) {
            Alignment.End
        } else {
            Alignment.Start
        }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(7.dp),
        horizontalAlignment = horizontalAlignment,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = accent,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            horizontalAlignment = horizontalAlignment,
        ) {
            Text(
                text = headlineLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = textAlign,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    if (alignEnd) {
                        Arrangement.End
                    } else {
                        Arrangement.Start
                    },
                verticalAlignment = Alignment.Bottom,
            ) {
                if (unitBefore) {
                    HomeOverviewHeadlineUnit(text = headlineUnit, accent = accent)
                    Spacer(modifier = Modifier.width(4.dp))
                    HomeOverviewHeadlineValue(text = headlineValue, accent = accent)
                } else {
                    HomeOverviewHeadlineValue(text = headlineValue, accent = accent)
                    Spacer(modifier = Modifier.width(4.dp))
                    HomeOverviewHeadlineUnit(text = headlineUnit, accent = accent)
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = horizontalAlignment,
        ) {
            tags.forEach { tag ->
                HomeOverviewPill(
                    text = tag,
                    accent = accent,
                    alignEnd = alignEnd,
                )
            }
        }
    }
}

@Composable
private fun HomeOverviewHeadlineValue(
    text: String,
    accent: Color,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.displaySmall,
        fontSize = 34.sp,
        fontWeight = FontWeight.SemiBold,
        color = accent,
        maxLines = 1,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
    )
}

@Composable
private fun HomeOverviewHeadlineUnit(
    text: String,
    accent: Color,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Normal,
        color = accent,
        modifier = Modifier.padding(bottom = 4.dp),
        maxLines = 1,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
    )
}

@Composable
private fun HomeOverviewPill(
    text: String,
    accent: Color,
    alignEnd: Boolean,
) {
    if (text.isBlank()) return
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.46f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.16f)),
        modifier = Modifier.widthIn(max = 132.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            textAlign =
                if (alignEnd) {
                    androidx.compose.ui.text.style.TextAlign.End
                } else {
                    androidx.compose.ui.text.style.TextAlign.Start
                },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HomeOverviewHeader(
    dateLabel: String,
) {
    Text(
        text = dateLabel,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth(),
        maxLines = 1,
        softWrap = false,
        overflow = androidx.compose.ui.text.style.TextOverflow.Clip,
    )
}

@Composable
private fun HomeEnginePanel(
    title: String,
    headlineLabel: String,
    headlineValue: String,
    accent: Color,
    containerColor: Color,
    onContainer: Color,
    trailingAlignment: Boolean,
    tags: List<String>,
    modifier: Modifier = Modifier,
) {
    val textAlign =
        if (trailingAlignment) {
            androidx.compose.ui.text.style.TextAlign.End
        } else {
            androidx.compose.ui.text.style.TextAlign.Start
        }
    val horizontalAlignment =
        if (trailingAlignment) {
            Alignment.End
        } else {
            Alignment.Start
        }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = horizontalAlignment,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = accent,
                textAlign = textAlign,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = horizontalAlignment,
            ) {
                Text(
                    text = headlineLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = onContainer.copy(alpha = 0.78f),
                    textAlign = textAlign,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = headlineValue,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = onContainer,
                    textAlign = textAlign,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = horizontalAlignment,
            ) {
                HomeOverviewTagLine(
                    text = tags.firstOrNull().orEmpty(),
                    accent = accent,
                    trailingAlignment = trailingAlignment,
                )
                HomeOverviewTagLine(
                    text = tags.getOrNull(1).orEmpty(),
                    accent = accent,
                    trailingAlignment = trailingAlignment,
                )
            }
        }
    }
}

@Composable
private fun HomeOverviewTagLine(
    text: String,
    accent: Color,
    trailingAlignment: Boolean,
) {
    if (text.isBlank()) return
    val textAlign =
        if (trailingAlignment) {
            androidx.compose.ui.text.style.TextAlign.End
        } else {
            androidx.compose.ui.text.style.TextAlign.Start
        }
    val horizontalAlignment =
        if (trailingAlignment) {
            Alignment.End
        } else {
            Alignment.Start
        }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HomeHistoryMetric(
    label: String,
    value: String,
    unit: String,
    accent: Color,
    alignEnd: Boolean = false,
    unitBefore: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val textAlign =
        if (alignEnd) {
            androidx.compose.ui.text.style.TextAlign.End
        } else {
            androidx.compose.ui.text.style.TextAlign.Start
        }
    val horizontalAlignment =
        if (alignEnd) {
            Alignment.End
        } else {
            Alignment.Start
        }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = horizontalAlignment,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Normal,
            color = accent,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                if (alignEnd) {
                    Arrangement.End
                } else {
                    Arrangement.Start
                },
            verticalAlignment = Alignment.Bottom,
        ) {
            if (unitBefore) {
                HomeHistoryMetricUnit(text = unit, accent = accent)
                Spacer(modifier = Modifier.width(4.dp))
                HomeHistoryMetricValue(text = value, accent = accent)
            } else {
                HomeHistoryMetricValue(text = value, accent = accent)
                Spacer(modifier = Modifier.width(4.dp))
                HomeHistoryMetricUnit(text = unit, accent = accent)
            }
        }
    }
}

@Composable
private fun HomeHistoryMetricValue(
    text: String,
    accent: Color,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = accent,
        maxLines = 1,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
    )
}

@Composable
private fun HomeHistoryMetricUnit(
    text: String,
    accent: Color,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Normal,
        color = accent,
        modifier = Modifier.padding(bottom = 3.dp),
        maxLines = 1,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
    )
}

private fun roundedDaysValue(totalMinutes: Long): Long =
    (totalMinutes.toDouble() / (24.0 * 60.0)).roundToLong().coerceAtLeast(0L)

private fun formatHomePointValue(points: Double): String =
    java.lang.String.format(java.util.Locale.getDefault(), "%.1f", points.coerceAtLeast(0.0))

private fun formatHomePointWholeValue(points: Double): String =
    points.roundToLong().coerceAtLeast(0L).toString()

private const val HOME_SURPRISE_COUNT = 100

private fun homeSurpriseKeyForDate(date: LocalDate): String {
    val index = (((date.toEpochDay() * 37L) + 17L).floorMod(HOME_SURPRISE_COUNT.toLong()) + 1L).toInt()
    return "home_surprise_%03d".format(java.util.Locale.US, index)
}

private fun Long.floorMod(modulus: Long): Long = ((this % modulus) + modulus) % modulus

private fun buildHomeOverviewUiState(
    context: android.content.Context,
    groupsWithApps: List<AppGroupWithApps>,
    usageMap: Map<String, Long>,
    historicalArchives: List<com.rrrrz.tinyvow.data.db.DailyArchiveEntity>,
    userPoints: Double,
    todayPoints: Double,
    achievementProgress: AchievementProgress,
): HomeOverviewUiState {
    val controlGroups = groupsWithApps.filter { it.group.type == GroupType.CONTROL }
    val encourageGroups = groupsWithApps.filter { it.group.type == GroupType.ENCOURAGE }

    val controlCompletedGroups =
        controlGroups.count { group ->
            val usageMinutes = ((usageMap[group.group.id] ?: 0L) / 60_000L).toInt()
            usageMinutes <= group.group.limitMinutes
        }
    val encourageCompletedGroups =
        encourageGroups.count { group ->
            val usageMinutes = ((usageMap[group.group.id] ?: 0L) / 60_000L).toInt()
            usageMinutes >= group.group.limitMinutes
        }
    val controlTodaySavedMinutes =
        controlGroups.sumOf { group ->
            val usageMinutes = ((usageMap[group.group.id] ?: 0L) / 60_000L).toInt()
            (group.group.limitMinutes - usageMinutes).coerceAtLeast(0)
        }
    val encourageTodayEarnedPoints =
        encourageGroups.sumOf { group ->
            val usageMillis = usageMap[group.group.id] ?: 0L
            val usagePoints = usageMillis / 60_000.0 * group.group.pointsPerMinute
            val targetBonus =
                if (usageMillis >= group.group.limitMinutes * 60_000L) {
                    group.group.limitMinutes * group.group.pointsPerMinute
                } else {
                    0.0
                }
            usagePoints + targetBonus
        }
    val totalSavedMinutes = historicalArchives.sumOf { it.savedMillis } / 60_000L
    val extendedLifeMinutes = totalSavedMinutes * 3L
    val totalEarnedPoints = historicalArchives.sumOf { it.pointsEarned } + todayPoints
    val locale = context.resources.configuration.locales[0] ?: java.util.Locale.getDefault()
    val today = LocalDate.now()
    val currentDate =
        today.format(
            java.time.format.DateTimeFormatter.ofPattern(AppText.t("home_mmm_d_eeee"), locale),
        )

    return HomeOverviewUiState(
        dateLabel = currentDate,
        tagline = AppText.t(homeSurpriseKeyForDate(today)),
        control =
            HomeControlOverviewUiState(
                todaySavedMinutes = controlTodaySavedMinutes,
                completedGroups = controlCompletedGroups,
                totalGroups = controlGroups.size,
                streakDays = achievementProgress.controlStreak,
            ),
        encourage =
            HomeEncourageOverviewUiState(
                todayEarnedPoints = encourageTodayEarnedPoints,
                completedGroups = encourageCompletedGroups,
                totalGroups = encourageGroups.size,
                streakDays = achievementProgress.encourageStreak,
            ),
        history =
            HomeHistoryOverviewUiState(
                totalSavedMinutes = totalSavedMinutes,
                extendedLifeMinutes = extendedLifeMinutes,
                totalEarnedPoints = totalEarnedPoints,
                currentPoints = userPoints,
            ),
    )
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
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = color,
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
            todayPoints = 10.0,
            isLoadingApps = false,
            superModeStatus = SuperModeStatus(false, false, false, false, "06:00 - 10:00", 360, 600, null, 0L),
            notificationPermissionGranted = false,
            isIgnoringBattery = false,
            isAutoStartDismissed = false,
            dismissedPermissionPrompts = emptySet(),
            onNavigateToRedeem = {},
            onNavigateToAchievements = {},
            onOpenSuperModeEntry = {},
            onOpenUsageAccessSettings = {},
            onOpenAccessibilitySettings = {},
            onRequestNotificationPermission = {},
            onOpenAutoStartSettings = {},
            onRequestBatteryOptimization = {},
            onSetAutoStartDismissed = {},
            onDismissPermissionPrompts = {},
            onSaveGroup = { _, _, _, _, _, _, _ -> },
            onDeleteGroup = {},
            onGuardAction = { _, block -> block() },
            isProActive = false,
            onShowProUpsell = {},
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
            superModeStatus = SuperModeStatus(true, true, true, true, "06:00 - 10:00", 360, 600, System.currentTimeMillis() + 300_000L, 300_000L),
            notificationPermissionGranted = true,
            isIgnoringBattery = true,
            isAutoStartDismissed = false,
            dismissedPermissionPrompts = emptySet(),
            onNavigateToRedeem = {},
            onNavigateToAchievements = {},
            onOpenSuperModeEntry = {},
            onOpenUsageAccessSettings = {},
            onOpenAccessibilitySettings = {},
            onRequestNotificationPermission = {},
            onOpenAutoStartSettings = {},
            onRequestBatteryOptimization = {},
            onSetAutoStartDismissed = {},
            onDismissPermissionPrompts = {},
            onSaveGroup = { _, _, _, _, _, _, _ -> },
            onDeleteGroup = {},
            onGuardAction = { _, block -> block() },
            isProActive = true,
            onShowProUpsell = {},
        )
    }
}
