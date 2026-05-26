package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.i18n.AppText

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.os.PowerManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
import com.rrrrz.tinyvow.data.billing.ProEntitlementStatus
import com.rrrrz.tinyvow.data.billing.SubscriptionRepository
import com.rrrrz.tinyvow.data.accessibility.AccessibilityServiceStateChecker
import com.rrrrz.tinyvow.data.apps.InstalledAppRepository
import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.notification.NotificationPermissionChecker
import com.rrrrz.tinyvow.data.privacy.LocalDataManager
import com.rrrrz.tinyvow.data.pro.ProFeatureGate
import com.rrrrz.tinyvow.data.reminder.ReminderPolicy
import com.rrrrz.tinyvow.data.reminder.ReminderScheduler
import com.rrrrz.tinyvow.data.reliability.PermissionReliabilitySnapshot
import com.rrrrz.tinyvow.data.reliability.StartupReliabilityStep
import com.rrrrz.tinyvow.data.repository.AppGroupWithApps
import com.rrrrz.tinyvow.data.repository.AppLimitRepository
import com.rrrrz.tinyvow.data.repository.AchievementProgress
import com.rrrrz.tinyvow.data.repository.DailyArchiveRepository
import com.rrrrz.tinyvow.data.repository.CustomRewardDraft
import com.rrrrz.tinyvow.data.repository.PointsRepository
import com.rrrrz.tinyvow.data.repository.InventoryRewardItem
import com.rrrrz.tinyvow.data.repository.PendingStreakShieldItem
import com.rrrrz.tinyvow.data.repository.ProtectionEventRepository
import com.rrrrz.tinyvow.data.repository.PurchaseRewardResult
import com.rrrrz.tinyvow.data.repository.RewardStoreItem
import com.rrrrz.tinyvow.data.repository.RewardSaveResult
import com.rrrrz.tinyvow.data.repository.RewardSaveValidationError
import com.rrrrz.tinyvow.data.repository.UseRewardResult
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.special.SpecialAppUsageRepository
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
import com.rrrrz.tinyvow.data.usage.AppSession
import com.rrrrz.tinyvow.service.block.AppLimitAccessibilityService
import com.rrrrz.tinyvow.ui.theme.TinyVowTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.json.JSONObject
import kotlin.math.atan2
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt
import com.rrrrz.tinyvow.data.usage.MergedUsageRepository
import com.rrrrz.tinyvow.data.usage.UsageRepository

import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.db.AchievementEntity
import com.rrrrz.tinyvow.data.db.AchievementTier
import com.rrrrz.tinyvow.data.db.ActiveRewardEffectEntity
import com.rrrrz.tinyvow.data.db.ActiveRewardEffectStatus
import com.rrrrz.tinyvow.data.db.DailyAppArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyGroupArchiveEntity
import com.rrrrz.tinyvow.data.db.RedemptionEntity
import com.rrrrz.tinyvow.data.db.RedemptionHistoryEntity
import com.rrrrz.tinyvow.data.db.ProtectionEventType
import com.rrrrz.tinyvow.data.db.RewardEffectBenefitEntity
import com.rrrrz.tinyvow.data.db.RewardType
import com.rrrrz.tinyvow.data.db.RewardUseHistoryEntity
import com.rrrrz.tinyvow.data.repository.parseRewardPayload
import com.rrrrz.tinyvow.ui.rewards.RedeemScreen
import com.rrrrz.tinyvow.ui.rewards.AchievementScreen
import com.rrrrz.tinyvow.ui.rewards.AchievementBadge
import com.rrrrz.tinyvow.ui.rewards.RewardInventoryScreen
import com.rrrrz.tinyvow.ui.theme.DefaultThemeSeed
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.TinyVowButton
import com.rrrrz.tinyvow.ui.theme.TinyVowButtonTone
import com.rrrrz.tinyvow.ui.theme.TinyVowCard
import com.rrrrz.tinyvow.ui.theme.TinyVowElevation
import com.rrrrz.tinyvow.ui.theme.TinyVowRadius
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import com.rrrrz.tinyvow.ui.theme.TinyVowSnackbarHost

enum class Screen { HOME, REWARDS, STATS, ME, ME_PRO, ME_PERMISSIONS, ME_NOTIFICATIONS, ME_DATA_PRIVACY, ME_VERSION, SUPER_MODE, LABORATORY, HISTORY, THEME, LANGUAGE, HELP_FEEDBACK, CONTACT_US, SPECIAL_APPS, WEREAD_SPECIAL_APP, PERMISSION_DIAGNOSTICS }
enum class RewardsSection { STORE, INVENTORY, ACHIEVEMENTS }

private const val CONTACT_EMAIL = "rrrr.zhao@gmail.com"
private const val WEREAD_AUTO_SYNC_DEBOUNCE_MS = 60_000L
private const val HOME_CONTROL_TOLERANCE_MINUTES = 5L

private data class PendingSuperModeRequest(
    val message: String,
    val onAllowed: (() -> Unit)?,
)

private data class BottomNavDestination(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
)

private enum class CoachmarkBubbleAlignment {
    Top,
    Center,
    Bottom,
}

private data class FirstRunCoachmarkStep(
    val screen: Screen,
    val icon: ImageVector,
    val titleKey: String,
    val bodyKey: String,
    val alignment: CoachmarkBubbleAlignment,
)

@Composable
private fun QuietBottomNavigation(
    currentScreen: Screen,
    onSelect: (Screen) -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val destinations =
        listOf(
            BottomNavDestination(Screen.HOME, AppText.t("home_home"), Icons.Default.Home),
            BottomNavDestination(Screen.STATS, AppText.t("home_report"), Icons.Default.BarChart),
            BottomNavDestination(Screen.REWARDS, AppText.t("home_rewards"), Icons.Default.CardGiftcard),
            BottomNavDestination(Screen.ME, AppText.t("home_me"), Icons.Default.Person),
        )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f),
                thickness = 0.5.dp,
            )
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(76.dp)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                destinations.forEach { destination ->
                    val selected = currentScreen == destination.screen
                    QuietBottomNavigationItem(
                        destination = destination,
                        selected = selected,
                        selectedContainer = themeColors.navSelectedContainer,
                        selectedColor = themeColors.base,
                        unselectedColor = themeColors.navUnselected,
                        onClick = { onSelect(destination.screen) },
                    )
                }
            }
        }
    }
}

@Composable
private fun QuietBottomNavigationItem(
    destination: BottomNavDestination,
    selected: Boolean,
    selectedContainer: Color,
    selectedColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .width(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(width = 38.dp, height = 32.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selected) selectedContainer else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.label,
                modifier = Modifier.size(22.dp),
                tint = if (selected) selectedColor else unselectedColor,
            )
        }
        Text(
            text = destination.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) selectedColor else unselectedColor,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
    }
}

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
    val behaviorScoreMetrics: List<DailyBehaviorScoreMetric>,
    val behaviorComparisonMetrics: List<DailyBehaviorScoreMetric>,
    val battleActions: List<HomeBattleAction>,
)

private enum class HomeBattleActionType {
    CONTROL,
    ENCOURAGE,
    REWARD,
    CREATE,
    PERMISSION_USAGE,
    PERMISSION_ACCESSIBILITY,
}

private data class HomeBattleAction(
    val type: HomeBattleActionType,
    val title: String,
    val subtitle: String,
    val value: String,
    val progress: Float,
    val group: AppGroupWithApps? = null,
    val subtitleGroupName: String? = null,
)

private data class HomeControlOverviewUiState(
    val todaySavedMinutes: Int,
    val completedGroups: Int,
    val totalGroups: Int,
    val scoreRatio: Float,
    val streakDays: Int,
    val streakLabel: String,
)

private data class HomeEncourageOverviewUiState(
    val todayEarnedPoints: Double,
    val completedGroups: Int,
    val totalGroups: Int,
    val scoreRatio: Float,
    val streakDays: Int,
    val streakLabel: String,
    val pointsMultiplierLabel: String?,
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
    activeRewardEffects: List<ActiveRewardEffectEntity>,
    rewardEffectBenefits: List<RewardEffectBenefitEntity>,
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
                    activeRewardEffects = activeRewardEffects,
                    rewardEffectBenefits = rewardEffectBenefits,
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
    TinyVowButton(
        text = title,
        onClick = onClick,
        selected = selected,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    )
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
        UseRewardResult.PeriodPassAlreadyActive -> AppText.t("redeem_error_period_pass_already_active")
        UseRewardResult.DoublePointsAlreadyActive -> AppText.t("redeem_error_double_points_already_active")
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
        GuardedAction.PURCHASE_TIME_ADD -> AppText.t("super_mode_action_purchase_time_add")
        GuardedAction.PURCHASE_PERIOD_PASS -> AppText.t("super_mode_action_purchase_period_pass")
        GuardedAction.PURCHASE_EMERGENCY_UNLOCK -> AppText.t("super_mode_action_purchase_emergency_unlock")
    }

private fun groupProtectionSnapshot(group: AppGroupWithApps): String =
    groupProtectionSnapshot(
        name = group.group.name,
        type = group.group.type,
        period = group.group.limitPeriod,
        limitMinutes = group.group.limitMinutes,
        pointsPerMinute = group.group.pointsPerMinute,
        packageCount = group.packageNames.size,
    )

private fun groupProtectionSnapshot(
    name: String,
    type: GroupType,
    period: LimitPeriod,
    limitMinutes: Int,
    pointsPerMinute: Double,
    packageCount: Int,
): String =
    JSONObject()
        .put("name", name)
        .put("type", type.name)
        .put("period", period.name)
        .put("limitMinutes", limitMinutes)
        .put("pointsPerMinute", pointsPerMinute)
        .put("packageCount", packageCount)
        .toString()

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
    val usageRepository = remember(context) { MergedUsageRepository(context) }
    val specialAppUsageRepository = remember(context) { SpecialAppUsageRepository(context) }
    val pointsRepository = remember(database, context) { PointsRepository(context, database) }
    val dailyArchiveRepository = remember(database, context) { DailyArchiveRepository(context, database) }
    val statsReportMemoryCache = remember { StatsReportMemoryCache() }
    val protectionEventRepository = remember(database) { ProtectionEventRepository(database) }
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
    val allRewardEffects by database.activeRewardEffectDao().observeAll().collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val homeOverviewGroupsWithAppsLoaded by appLimitRepository.getAllGroupsWithApps()
        .map<List<AppGroupWithApps>, List<AppGroupWithApps>?> { it }
        .collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    val homeOverviewUserPointsLoaded by preferences.userPoints
        .map<Double, Double?> { it }
        .collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    val homeOverviewTodayPointsLoaded by preferences.todayPoints
        .map<Double, Double?> { it }
        .collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    val homeOverviewAchievementProgressLoaded by appLimitRepository.observeAchievementProgress()
        .map<AchievementProgress, AchievementProgress?> { it }
        .collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    val homeOverviewAllRewardEffectsLoaded by database.activeRewardEffectDao().observeAll()
        .map<List<ActiveRewardEffectEntity>, List<ActiveRewardEffectEntity>?> { it }
        .collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    val homeOverviewHistoricalArchivesLoaded =
        dailyArchiveRepository.getRecentArchives(limit = 3650)
            .map<List<com.rrrrz.tinyvow.data.db.DailyArchiveEntity>, List<com.rrrrz.tinyvow.data.db.DailyArchiveEntity>?> { it }
            .collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    val homeOverviewRecentGroupArchivesLoaded =
        dailyArchiveRepository.getGroupArchivesByRange(
            LocalDate.now().minusDays(7).toString(),
            LocalDate.now().minusDays(1).toString(),
        ).map<List<DailyGroupArchiveEntity>, List<DailyGroupArchiveEntity>?> { it }
            .collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    val homeOverviewInputsReady =
        homeOverviewGroupsWithAppsLoaded != null &&
            homeOverviewUserPointsLoaded != null &&
            homeOverviewTodayPointsLoaded != null &&
            homeOverviewAchievementProgressLoaded != null &&
            homeOverviewAllRewardEffectsLoaded != null &&
            homeOverviewHistoricalArchivesLoaded != null &&
            homeOverviewRecentGroupArchivesLoaded != null
    val activeRewardEffects = remember(allRewardEffects, currentTimeMillis) {
        allRewardEffects.filter {
            it.status == ActiveRewardEffectStatus.ACTIVE &&
                it.startAt <= currentTimeMillis &&
                it.expireAt > currentTimeMillis
        }
    }
    val achievements by appLimitRepository.getAllAchievements().collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val achievementProgress by appLimitRepository.observeAchievementProgress().collectAsStateWithLifecycle(initialValue = AchievementProgress(), lifecycle = lifecycle)
    val redemptionHistory by appLimitRepository.getRedemptionHistory().collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val rewardUseHistory by appLimitRepository.observeRewardUseHistory().collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val rewardEffectBenefits by appLimitRepository.observeRewardEffectBenefits().collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val dismissedPermissionPrompts by preferences.dismissedPermissionPrompts.collectAsStateWithLifecycle(initialValue = emptySet(), lifecycle = lifecycle)
    val usageAccessDisclosureAccepted by preferences.usageAccessDisclosureAccepted.collectAsStateWithLifecycle(initialValue = false, lifecycle = lifecycle)
    val accessibilityDisclosureAccepted by preferences.accessibilityDisclosureAccepted.collectAsStateWithLifecycle(initialValue = false, lifecycle = lifecycle)
    val accessibilityServiceHeartbeatAtMillis by preferences.accessibilityServiceHeartbeatAtMillis.collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    val welcomeIntroCompleted by preferences.welcomeIntroCompleted.collectAsStateWithLifecycle(initialValue = true, lifecycle = lifecycle)
    val firstRunCoachmarkCompleted by preferences.firstRunCoachmarkCompleted.collectAsStateWithLifecycle(initialValue = true, lifecycle = lifecycle)
    val notificationRemindersEnabled by preferences.notificationRemindersEnabled.collectAsStateWithLifecycle(initialValue = true, lifecycle = lifecycle)
    val controlRemainingReminderMinutes by preferences.controlRemainingReminderMinutes.collectAsStateWithLifecycle(
        initialValue = ManagedAppPreferences.DEFAULT_CONTROL_REMAINING_REMINDER_MINUTES,
        lifecycle = lifecycle,
    )
    val encourageReminderTimesMinutes by preferences.encourageReminderTimesMinutes.collectAsStateWithLifecycle(
        initialValue = ManagedAppPreferences.DEFAULT_ENCOURAGE_REMINDER_TIMES_MINUTES,
        lifecycle = lifecycle,
    )
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
    val screenStateHolder = rememberSaveableStateHolder()
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
    var showWelcomeIntro by remember { mutableStateOf(false) }
    var showFirstRunCoachmark by remember { mutableStateOf(false) }
    var yesterdaySavedMinutes by remember { mutableIntStateOf(0) }
    var showSuperModeCredentialDialog by remember { mutableStateOf(false) }
    var isEditingSuperModeCredentials by remember { mutableStateOf(false) }
    var showSuperModePasswordDialog by remember { mutableStateOf(false) }
    var showSuperModeUnavailableDialog by remember { mutableStateOf(false) }
    var showSuperModeSetupDialog by remember { mutableStateOf(false) }
    var showSuperModeRecoveryDialog by remember { mutableStateOf(false) }
    var showSuperModeWindowDialog by remember { mutableStateOf(false) }
    var showSuperModeDisableDialog by remember { mutableStateOf(false) }
    var showSuperModeInfoDialog by remember { mutableStateOf(false) }
    var superModePasswordError by remember { mutableStateOf<String?>(null) }
    var superModeRecoveryError by remember { mutableStateOf<String?>(null) }
    var superModeWindowError by remember { mutableStateOf<String?>(null) }
    var setupRequiredActionLabel by remember { mutableStateOf(AppText.t("super_mode_title")) }
    var pendingSuperModeRequest by remember { mutableStateOf<PendingSuperModeRequest?>(null) }
    var isWeReadAutoSyncing by remember { mutableStateOf(false) }
    var lastWeReadAutoSyncAt by remember { mutableLongStateOf(0L) }

    val isAutoStartDismissed by preferences.isAutoStartDismissed.collectAsStateWithLifecycle(initialValue = false, lifecycle = lifecycle)

    fun openUsageAccessSettingsNow() {
        context.startActivityKeepingCurrentTask(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    fun openAccessibilitySettingsNow() {
        context.startActivityKeepingCurrentTask(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
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

    suspend fun recordProtectionEvent(
        eventType: ProtectionEventType,
        titleKey: String,
        messageKey: String,
        messageArgs: List<String> = emptyList(),
        targetId: String? = null,
        targetLabel: String? = null,
        beforeJson: String? = null,
        afterJson: String? = null,
        withinWindow: Boolean? = superModeStatus.isAvailableNow,
        protectionEnabled: Boolean = superModeStatus.isEnabled,
    ) {
        protectionEventRepository.record(
            eventType = eventType,
            titleKey = titleKey,
            messageKey = messageKey,
            messageArgs = messageArgs,
            targetId = targetId,
            targetLabel = targetLabel,
            beforeJson = beforeJson,
            afterJson = afterJson,
            withinWindow = withinWindow,
            protectionEnabled = protectionEnabled,
        )
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
            coroutineScope.launch {
                recordProtectionEvent(
                    eventType = ProtectionEventType.GUARDED_ACTION_BLOCKED_OUTSIDE_WINDOW,
                    titleKey = "protection_event_title_guarded_action_blocked",
                    messageKey = "protection_event_message_guarded_action_blocked",
                    messageArgs = listOf(guardedActionLabel(action)),
                    targetLabel = guardedActionLabel(action),
                    withinWindow = false,
                    protectionEnabled = true,
                )
            }
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
        currentScreen = Screen.SUPER_MODE
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

    fun triggerWeReadAutoSync() {
        val now = System.currentTimeMillis()
        if (isWeReadAutoSyncing || now - lastWeReadAutoSyncAt < WEREAD_AUTO_SYNC_DEBOUNCE_MS) {
            return
        }
        isWeReadAutoSyncing = true
        lastWeReadAutoSyncAt = now
        coroutineScope.launch {
            try {
                if (specialAppUsageRepository.buildSettingsState().hasApiKey) {
                    specialAppUsageRepository.syncWeReadNow()
                }
            } finally {
                isWeReadAutoSyncing = false
            }
        }
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
                triggerWeReadAutoSync()
            } else if (event == Lifecycle.Event.ON_STOP && superModeStoredState.isActive) {
                coroutineScope.launch {
                    superModeController.exit(SuperModeExitReason.BACKGROUND)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        triggerWeReadAutoSync()
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
        appLimitRepository.confirmExpiredPendingRewardEffects()
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

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000L)
            appLimitRepository.confirmExpiredPendingRewardEffects()
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
    val permissionReliabilitySnapshot =
        remember(
            groupsWithApps,
            effectiveUsageAccessStatus,
            accessibilityDisclosureAccepted,
            accessibilityServiceEnabled,
            notificationPermissionGranted,
            isIgnoringBattery,
            isAutoStartDismissed,
            accessibilityServiceHeartbeatAtMillis,
            currentTimeMillis,
        ) {
            PermissionReliabilitySnapshot.build(
                groups = groupsWithApps,
                usageAccessGranted = effectiveUsageAccessStatus == UsageAccessStatus.GRANTED,
                accessibilityDisclosureAccepted = accessibilityDisclosureAccepted,
                accessibilityServiceEnabled = accessibilityServiceEnabled,
                notificationPermissionGranted = notificationPermissionGranted,
                isIgnoringBatteryOptimizations = isIgnoringBattery,
                isAutoStartDismissed = isAutoStartDismissed,
                lastAccessibilityHeartbeatAtMillis = accessibilityServiceHeartbeatAtMillis,
                nowMillis = currentTimeMillis,
            )
        }

    LaunchedEffect(usageAccessStatus, usageAccessDisclosureAccepted) {
        isLoadingApps = true
        installedApps =
            if (effectiveUsageAccessStatus == UsageAccessStatus.GRANTED) {
                appRepository.getAllInstalledApps()
            } else {
                appRepository.getLaunchableApps()
            }
        isLoadingApps = false
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

    LaunchedEffect(welcomeIntroCompleted) {
        if (!welcomeIntroCompleted) {
            showWelcomeIntro = true
        }
    }

    LaunchedEffect(welcomeIntroCompleted, firstRunCoachmarkCompleted, showWelcomeIntro) {
        if (welcomeIntroCompleted && !firstRunCoachmarkCompleted && !showWelcomeIntro) {
            showFirstRunCoachmark = true
        }
    }

    if (showWelcomeIntro) {
        BackHandler {
            coroutineScope.launch {
                preferences.setWelcomeIntroCompleted(true)
                showWelcomeIntro = false
            }
        }
    } else if (showFirstRunCoachmark) {
        BackHandler {
            coroutineScope.launch {
                preferences.setFirstRunCoachmarkCompleted(true)
                showFirstRunCoachmark = false
            }
        }
    } else if (currentScreen != Screen.HOME) {
        BackHandler {
            if (currentScreen == Screen.REWARDS && rewardsSection != RewardsSection.STORE) {
                rewardsSection = RewardsSection.STORE
            } else {
                if (currentScreen == Screen.REWARDS) {
                    rewardsSection = RewardsSection.STORE
                }
                currentScreen = when (currentScreen) {
                    Screen.WEREAD_SPECIAL_APP -> Screen.SPECIAL_APPS
                    Screen.PERMISSION_DIAGNOSTICS -> Screen.HOME
                    Screen.ME_PRO, Screen.ME_PERMISSIONS, Screen.ME_NOTIFICATIONS, Screen.ME_DATA_PRIVACY, Screen.ME_VERSION, Screen.SUPER_MODE, Screen.LABORATORY, Screen.HISTORY, Screen.THEME, Screen.LANGUAGE, Screen.HELP_FEEDBACK, Screen.CONTACT_US, Screen.SPECIAL_APPS -> Screen.ME
                    else -> Screen.HOME
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!BuildConfig.ENABLE_LOCAL_ACTIVATION) {
            subscriptionRepository.refresh()
        }
    }

    LaunchedEffect(BuildConfig.ENABLE_LOCAL_ACTIVATION) {
        if (BuildConfig.ENABLE_LOCAL_ACTIVATION) {
            val session = authRepository.ensureLocalSession()
            localActivationRepository?.bindUser(session.userId)
        }
    }

    LaunchedEffect(BuildConfig.ENABLE_LOCAL_ACTIVATION, userSession?.userId) {
        if (BuildConfig.ENABLE_LOCAL_ACTIVATION && userSession?.userId != null) {
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
            TinyVowSnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            if (currentScreen == Screen.HOME || currentScreen == Screen.REWARDS || currentScreen == Screen.STATS || currentScreen == Screen.ME) {
                QuietBottomNavigation(
                    currentScreen = currentScreen,
                    onSelect = { screen ->
                        if (screen == Screen.REWARDS) {
                            rewardsSection = RewardsSection.STORE
                        }
                        currentScreen = screen
                    },
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            screenStateHolder.SaveableStateProvider(currentScreen.name) {
                when (currentScreen) {
                Screen.HOME -> {
                    HomeScreen(
                        usageAccessStatus = effectiveUsageAccessStatus,
                        accessibilityServiceEnabled = effectiveAccessibilityServiceEnabled,
                        notificationPermissionGranted = notificationPermissionGranted,
                        isIgnoringBattery = isIgnoringBattery,
                        permissionReliabilitySnapshot = permissionReliabilitySnapshot,
                        installedApps = installedApps,
                        groupsWithApps = groupsWithApps,
                        activeRewardEffects = activeRewardEffects,
                        userPoints = userPoints,
                        todayPoints = todayPoints,
                        overviewInputsReady = homeOverviewInputsReady,
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
                        onOpenSuperModeEntry = { showSuperModeInfoDialog = true },
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
                        onOpenPermissionDiagnostics = { currentScreen = Screen.PERMISSION_DIAGNOSTICS },
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
                                val previousGroup = id?.let { groupId ->
                                    groupsWithApps.firstOrNull { it.group.id == groupId }
                                }
                                val groupId = appLimitRepository.createOrUpdateGroup(id, name, limit, type, period, pts)
                                appLimitRepository.updateGroupApps(groupId, pkgs)
                                if (id != null) {
                                    recordProtectionEvent(
                                        eventType = ProtectionEventType.GROUP_UPDATED,
                                        titleKey = "protection_event_title_group_updated",
                                        messageKey = "protection_event_message_group_updated",
                                        messageArgs = listOf(name),
                                        targetId = groupId,
                                        targetLabel = name,
                                        beforeJson = previousGroup?.let(::groupProtectionSnapshot),
                                        afterJson = groupProtectionSnapshot(name, type, period, limit, pts, pkgs.size),
                                    )
                                    superModeController.touch(proEntitlement.isProActive)
                                }
                            }
                        },
                        onDeleteGroup = { id ->
                            coroutineScope.launch {
                                val previousGroup = groupsWithApps.firstOrNull { it.group.id == id }
                                appLimitRepository.deleteGroup(id)
                                recordProtectionEvent(
                                    eventType = ProtectionEventType.GROUP_DELETED,
                                    titleKey = "protection_event_title_group_deleted",
                                    messageKey = "protection_event_message_group_deleted",
                                    messageArgs = listOf(previousGroup?.group?.name ?: id),
                                    targetId = id,
                                    targetLabel = previousGroup?.group?.name,
                                    beforeJson = previousGroup?.let(::groupProtectionSnapshot),
                                )
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
                            activeRewardEffects = activeRewardEffects,
                            rewardEffectBenefits = rewardEffectBenefits,
                            groups = groupsWithApps,
                            redemptionHistory = redemptionHistory,
                            rewardUseHistory = rewardUseHistory,
                            onPurchaseReward = { reward ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        runCatching {
                                            val result = appLimitRepository.purchaseReward(reward.id)
                                            if (
                                                result is PurchaseRewardResult.Success &&
                                                GuardedAction.fromRewardType(reward.rewardType) != null
                                            ) {
                                                recordProtectionEvent(
                                                    eventType = ProtectionEventType.TIME_REWARD_PURCHASED,
                                                    titleKey = "protection_event_title_time_reward_purchased",
                                                    messageKey = "protection_event_message_time_reward_purchased",
                                                    messageArgs = listOf(result.rewardTitle),
                                                    targetId = reward.id,
                                                    targetLabel = result.rewardTitle,
                                                )
                                            }
                                            purchaseRewardResultMessage(result)
                                        }.getOrElse {
                                            AppText.t("redeem_purchase_failed")
                                        }
                                    )
                                }
                            },
                        onUseInventoryReward = { reward, groupId ->
                            coroutineScope.launch {
                                runCatching {
                                    appLimitRepository.useInventoryReward(reward.id, groupId)
                                }.onSuccess { result ->
                                    val message = useRewardResultMessage(result)
                                    val pendingEffectId = (result as? UseRewardResult.Success)?.pendingEffectId
                                    if (pendingEffectId == null) {
                                        snackbarHostState.showSnackbar(message)
                                    } else {
                                        val snackbarResult =
                                            snackbarHostState.showSnackbar(
                                                message = message,
                                                actionLabel = AppText.t("redeem_effect_undo"),
                                                withDismissAction = true,
                                            )
                                        if (snackbarResult == SnackbarResult.ActionPerformed) {
                                            snackbarHostState.showSnackbar(
                                                useRewardResultMessage(appLimitRepository.cancelPendingRewardEffect(pendingEffectId))
                                            )
                                        } else {
                                            appLimitRepository.confirmRewardEffect(pendingEffectId)
                                        }
                                    }
                                }.onFailure {
                                    snackbarHostState.showSnackbar(AppText.t("redeem_purchase_failed"))
                                }
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
                            reportMemoryCache = statsReportMemoryCache,
                            isProActive = proEntitlement.isProActive,
                            onShowProUpsell = { proUpsellSource = it },
                            onRequestUsageAccess = { requestUsageAccessSettings() },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                Screen.PERMISSION_DIAGNOSTICS -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding()),
                    ) {
                        DiagnosticSettingsPage(
                            usageAccessGranted = effectiveUsageAccessStatus == UsageAccessStatus.GRANTED,
                            accessibilityServiceEnabled = effectiveAccessibilityServiceEnabled,
                            isAutoStartDismissed = isAutoStartDismissed,
                            isIgnoringBattery = isIgnoringBattery,
                            notificationPermissionGranted = notificationPermissionGranted,
                            runtimeDiagnostics =
                                buildRuntimeDiagnostics(
                                    generatedAtMillis = currentTimeMillis,
                                    usageAccessGranted = effectiveUsageAccessStatus == UsageAccessStatus.GRANTED,
                                    accessibilityServiceEnabled = effectiveAccessibilityServiceEnabled,
                                    accessibilityHeartbeatHealthy = permissionReliabilitySnapshot.accessibilityHeartbeatHealthy,
                                    lastAccessibilityHeartbeatAtMillis = accessibilityServiceHeartbeatAtMillis,
                                    notificationPermissionGranted = notificationPermissionGranted,
                                    isIgnoringBattery = isIgnoringBattery,
                                    groupsWithApps = groupsWithApps,
                                    archiveCount = meHistoricalArchives.size,
                                    latestArchiveDate = meHistoricalArchives.maxByOrNull { it.archiveDate }?.archiveDate,
                                    proEntitlement = proEntitlement,
                                    superModeStatus = superModeStatus,
                                ),
                            statusColor = if (effectiveUsageAccessStatus == UsageAccessStatus.GRANTED) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            onBack = { currentScreen = Screen.HOME },
                            onOpenUsageAccessSettings = { requestUsageAccessSettings() },
                            onOpenAccessibilitySettings = { requestAccessibilitySettings() },
                            onOpenAutoStartSettings = {
                                pendingSensitiveDisclosure = SensitivePermissionDisclosure.BACKGROUND_START
                            },
                            onSetAutoStartDismissed = {
                                coroutineScope.launch { preferences.setAutoStartDismissed(true) }
                            },
                            onRequestBatteryOptimization = {
                                pendingSensitiveDisclosure = SensitivePermissionDisclosure.BATTERY_OPTIMIZATION
                            },
                            onRequestNotificationPermission = {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    pendingSensitiveDisclosure = SensitivePermissionDisclosure.NOTIFICATION
                                }
                            },
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
                        notificationRemindersEnabled = notificationRemindersEnabled,
                        controlRemainingReminderMinutes = ReminderPolicy.effectiveSettings(
                            enabled = notificationRemindersEnabled,
                            controlRemainingReminderMinutes = controlRemainingReminderMinutes,
                            encourageReminderTimesMinutes = encourageReminderTimesMinutes,
                            isProActive = proEntitlement.isProActive,
                        ).controlRemainingReminderMinutes,
                        encourageReminderTimesMinutes = ReminderPolicy.effectiveSettings(
                            enabled = notificationRemindersEnabled,
                            controlRemainingReminderMinutes = controlRemainingReminderMinutes,
                            encourageReminderTimesMinutes = encourageReminderTimesMinutes,
                            isProActive = proEntitlement.isProActive,
                        ).encourageReminderTimesMinutes,
                        openBenefitsDialog = openMeProBenefitsDialog,
                        onBenefitsDialogOpened = { openMeProBenefitsDialog = false },
                        onNavigateToProMembership = { currentScreen = Screen.ME_PRO },
                        usageAccessGranted = effectiveUsageAccessStatus == UsageAccessStatus.GRANTED,
                        accessibilityServiceEnabled = effectiveAccessibilityServiceEnabled,
                        isAutoStartDismissed = isAutoStartDismissed,
                        isIgnoringBattery = isIgnoringBattery,
                        notificationPermissionGranted = notificationPermissionGranted,
                        dismissedPermissionPrompts = dismissedPermissionPrompts,
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
                        onOpenSuperModeSettings = { openSuperModeSettings() },
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
                        onNavigateToPermissionSettings = { currentScreen = Screen.ME_PERMISSIONS },
                        onNavigateToNotificationSettings = { currentScreen = Screen.ME_NOTIFICATIONS },
                        onNavigateToLaboratory = { currentScreen = Screen.LABORATORY },
                        onNavigateToHistory = { currentScreen = Screen.HISTORY },
                        onNavigateToThemeSettings = { currentScreen = Screen.THEME },
                        onNavigateToLanguageSettings = { currentScreen = Screen.LANGUAGE },
                        onNavigateToHelpFeedback = { currentScreen = Screen.HELP_FEEDBACK },
                        onNavigateToContactUs = { currentScreen = Screen.CONTACT_US },
                        onNavigateToSpecialAppSettings = { currentScreen = Screen.SPECIAL_APPS },
                        onNavigateToDataPrivacy = { currentScreen = Screen.ME_DATA_PRIVACY },
                        onNavigateToVersionInfo = { currentScreen = Screen.ME_VERSION },
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
                Screen.ME_PRO -> {
                    ProMembershipPage(
                        entitlement = proEntitlement,
                        offers = subscriptionOffers,
                        isPlayBillingEnabled = BuildConfig.ENABLE_PLAY_BILLING,
                        isLocalActivationEnabled = BuildConfig.ENABLE_LOCAL_ACTIVATION,
                        localUserId = userSession?.userId,
                        onBack = { currentScreen = Screen.ME },
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
                Screen.ME_PERMISSIONS -> {
                    PermissionSettingsPage(
                        usageAccessGranted = effectiveUsageAccessStatus == UsageAccessStatus.GRANTED,
                        accessibilityServiceEnabled = effectiveAccessibilityServiceEnabled,
                        isAutoStartDismissed = isAutoStartDismissed,
                        isIgnoringBattery = isIgnoringBattery,
                        notificationPermissionGranted = notificationPermissionGranted,
                        dismissedPermissionPrompts = dismissedPermissionPrompts,
                        onBack = { currentScreen = Screen.ME },
                        onOpenUsageAccessSettings = { requestUsageAccessSettings() },
                        onOpenAccessibilitySettings = { requestAccessibilitySettings() },
                        onOpenAutoStartSettings = {
                            pendingSensitiveDisclosure = SensitivePermissionDisclosure.BACKGROUND_START
                        },
                        onSetAutoStartDismissed = {
                            coroutineScope.launch { preferences.setAutoStartDismissed(true) }
                        },
                        onRequestBatteryOptimization = {
                            pendingSensitiveDisclosure = SensitivePermissionDisclosure.BATTERY_OPTIMIZATION
                        },
                        onRequestNotificationPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                pendingSensitiveDisclosure = SensitivePermissionDisclosure.NOTIFICATION
                            }
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
                    )
                }
                Screen.ME_NOTIFICATIONS -> {
                    val effectiveNotificationSettings = ReminderPolicy.effectiveSettings(
                        enabled = notificationRemindersEnabled,
                        controlRemainingReminderMinutes = controlRemainingReminderMinutes,
                        encourageReminderTimesMinutes = encourageReminderTimesMinutes,
                        isProActive = proEntitlement.isProActive,
                    )
                    NotificationReminderSettingsPage(
                        remindersEnabled = effectiveNotificationSettings.enabled,
                        notificationPermissionGranted = notificationPermissionGranted,
                        isProActive = proEntitlement.isProActive,
                        controlRemainingReminderMinutes = effectiveNotificationSettings.controlRemainingReminderMinutes,
                        encourageReminderTimesMinutes = effectiveNotificationSettings.encourageReminderTimesMinutes,
                        onBack = { currentScreen = Screen.ME },
                        onSetEnabled = { enabled ->
                            coroutineScope.launch {
                                preferences.setNotificationRemindersEnabled(enabled)
                                ReminderScheduler(context).schedule()
                            }
                        },
                        onSetControlRemainingMinutes = { minutes ->
                            coroutineScope.launch {
                                preferences.setControlRemainingReminderMinutes(minutes)
                                ReminderScheduler(context).scheduleControlRemainingReminder()
                                snackbarHostState.showSnackbar(AppText.t("notification_settings_saved"))
                            }
                        },
                        onSetEncourageTimes = { times ->
                            coroutineScope.launch {
                                preferences.setEncourageReminderTimesMinutes(times)
                                ReminderScheduler(context).scheduleNextEncourageReminder(
                                    ReminderPolicy.effectiveSettings(
                                        enabled = notificationRemindersEnabled,
                                        controlRemainingReminderMinutes = controlRemainingReminderMinutes,
                                        encourageReminderTimesMinutes = times,
                                        isProActive = proEntitlement.isProActive,
                                    )
                                )
                                snackbarHostState.showSnackbar(AppText.t("notification_settings_saved"))
                            }
                        },
                        onRequestNotificationPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                pendingSensitiveDisclosure = SensitivePermissionDisclosure.NOTIFICATION
                            }
                        },
                        onShowProUpsell = {
                            proUpsellSource = ProUpsellSource.NOTIFICATION_CUSTOMIZATION
                        },
                    )
                }
                Screen.ME_DATA_PRIVACY -> {
                    DataPrivacyPage(
                        onBack = { currentScreen = Screen.ME },
                        onExportLocalData = {
                            coroutineScope.launch {
                                runCatching {
                                    val diagnostics =
                                        buildRuntimeDiagnostics(
                                            generatedAtMillis = currentTimeMillis,
                                            usageAccessGranted = effectiveUsageAccessStatus == UsageAccessStatus.GRANTED,
                                            accessibilityServiceEnabled = effectiveAccessibilityServiceEnabled,
                                            accessibilityHeartbeatHealthy = permissionReliabilitySnapshot.accessibilityHeartbeatHealthy,
                                            lastAccessibilityHeartbeatAtMillis = accessibilityServiceHeartbeatAtMillis,
                                            notificationPermissionGranted = notificationPermissionGranted,
                                            isIgnoringBattery = isIgnoringBattery,
                                            groupsWithApps = groupsWithApps,
                                            archiveCount = meHistoricalArchives.size,
                                            latestArchiveDate = meHistoricalArchives.maxByOrNull { it.archiveDate }?.archiveDate,
                                            proEntitlement = proEntitlement,
                                            superModeStatus = superModeStatus,
                                        ).asPlainText(AppText.t("diagnostics_runtime_summary"))
                                    val file = localDataManager.exportPrivacyReport(diagnostics)
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
                                }.onSuccess {
                                    // The share sheet is now open; no snackbar needed.
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
                            context.startActivityKeepingCurrentTask(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(PRIVACY_POLICY_URL),
                                )
                            )
                        },
                    )
                }
                Screen.ME_VERSION -> {
                    VersionInfoPage(
                        versionName = BuildConfig.VERSION_NAME.removeSuffix("-cn"),
                        onBack = { currentScreen = Screen.ME },
                    )
                }
                Screen.SUPER_MODE -> {
                    SuperModeSettingsSheet(
                        status = superModeStatus,
                        isProActive = proEntitlement.isProActive,
                        currentTimeLabel = currentTimeLabel,
                        recoveryQuestion = superModeStoredState.recoveryQuestion,
                        onDismiss = { currentScreen = Screen.ME },
                        onConfigure = {
                            isEditingSuperModeCredentials = false
                            showSuperModeCredentialDialog = true
                        },
                        onSetEnabled = { enabled ->
                            coroutineScope.launch {
                                preferences.setSuperModeEnabled(enabled)
                                recordProtectionEvent(
                                    eventType = if (enabled) {
                                        ProtectionEventType.SUPER_MODE_ENABLED
                                    } else {
                                        ProtectionEventType.SUPER_MODE_DISABLED
                                    },
                                    titleKey = if (enabled) {
                                        "protection_event_title_super_mode_enabled"
                                    } else {
                                        "protection_event_title_super_mode_disabled"
                                    },
                                    messageKey = if (enabled) {
                                        "protection_event_message_super_mode_enabled"
                                    } else {
                                        "protection_event_message_super_mode_disabled"
                                    },
                                    withinWindow = superModeStatus.isAvailableNow,
                                    protectionEnabled = enabled,
                                )
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
                Screen.LANGUAGE -> {
                    LanguageSettingsScreen(
                        selected = selectedAppLanguage,
                        onSelect = { language ->
                            coroutineScope.launch {
                                preferences.setSelectedAppLanguage(language)
                            }
                        },
                        onBack = { currentScreen = Screen.ME },
                    )
                }
                Screen.SPECIAL_APPS -> {
                    SpecialAppsScreen(
                        onBack = { currentScreen = Screen.ME },
                        onOpenWeRead = { currentScreen = Screen.WEREAD_SPECIAL_APP },
                    )
                }
                Screen.WEREAD_SPECIAL_APP -> {
                    SpecialAppSettingsScreen(
                        onBack = { currentScreen = Screen.SPECIAL_APPS },
                    )
                }
                Screen.HELP_FEEDBACK -> {
                    HelpFeedbackScreen(
                        onBack = { currentScreen = Screen.ME },
                        onSendFeedback = {
                            val diagnostics =
                                buildRuntimeDiagnostics(
                                    generatedAtMillis = currentTimeMillis,
                                    usageAccessGranted = effectiveUsageAccessStatus == UsageAccessStatus.GRANTED,
                                    accessibilityServiceEnabled = effectiveAccessibilityServiceEnabled,
                                    accessibilityHeartbeatHealthy = permissionReliabilitySnapshot.accessibilityHeartbeatHealthy,
                                    lastAccessibilityHeartbeatAtMillis = accessibilityServiceHeartbeatAtMillis,
                                    notificationPermissionGranted = notificationPermissionGranted,
                                    isIgnoringBattery = isIgnoringBattery,
                                    groupsWithApps = groupsWithApps,
                                    archiveCount = meHistoricalArchives.size,
                                    latestArchiveDate = meHistoricalArchives.maxByOrNull { it.archiveDate }?.archiveDate,
                                    proEntitlement = proEntitlement,
                                    superModeStatus = superModeStatus,
                                ).asPlainText(AppText.t("diagnostics_runtime_summary"))
                            if (!context.openSupportEmail(
                                    subject = AppText.t("home_feedback_subject"),
                                    body = AppText.t("support_feedback_email_body", diagnostics),
                                )
                            ) {
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
                        protectionEventRepository = protectionEventRepository,
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
                        onTriggerWelcomeIntro = { showWelcomeIntro = true },
                        onTriggerCoachmarkTutorial = {
                            currentScreen = Screen.HOME
                            showFirstRunCoachmark = true
                        },
                        onTriggerAdvancedCenterTest = {
                            coroutineScope.launch {
                                if (!accessibilityDisclosureAccepted || !accessibilityServiceEnabled) {
                                    snackbarHostState.showSnackbar(AppText.t("lab_advanced_center_test_requires_accessibility"))
                                    return@launch
                                }
                                context.sendBroadcast(AppLimitAccessibilityService.debugShowTestOverlayIntent(context))
                                snackbarHostState.showSnackbar(AppText.t("lab_advanced_center_test_triggered"))
                            }
                        },
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
    }

    if (showWelcomeIntro) {
        WelcomeIntroScreen(
            onComplete = {
                coroutineScope.launch {
                    preferences.setWelcomeIntroCompleted(true)
                    showWelcomeIntro = false
                }
            },
            onDismiss = {
                coroutineScope.launch {
                    preferences.setWelcomeIntroCompleted(true)
                    showWelcomeIntro = false
                }
            },
        )
    }

    if (showFirstRunCoachmark && !showWelcomeIntro) {
        FirstRunCoachmarkOverlay(
            onTargetScreenChange = { screen ->
                if (screen == Screen.REWARDS) {
                    rewardsSection = RewardsSection.STORE
                }
                currentScreen = screen
            },
            onComplete = {
                coroutineScope.launch {
                    preferences.setFirstRunCoachmarkCompleted(true)
                    showFirstRunCoachmark = false
                }
            },
            onDismiss = {
                coroutineScope.launch {
                    preferences.setFirstRunCoachmarkCompleted(true)
                    showFirstRunCoachmark = false
                }
            },
        )
    }

    if (showSuperModeInfoDialog) {
        SuperModeInfoDialog(
            status = superModeStatus,
            currentTimeLabel = currentTimeLabel,
            onDismiss = { showSuperModeInfoDialog = false },
            onOpenSettings = {
                showSuperModeInfoDialog = false
                openSuperModeSettings()
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
                    val wasConfigured = superModeStatus.isConfigured
                    superModeController.updateCredentials(password, question, answer)
                    showSuperModeCredentialDialog = false
                    recordProtectionEvent(
                        eventType = if (wasConfigured) {
                            ProtectionEventType.SUPER_MODE_CREDENTIALS_CHANGED
                        } else {
                            ProtectionEventType.SUPER_MODE_CONFIGURED
                        },
                        titleKey = if (wasConfigured) {
                            "protection_event_title_super_mode_credentials_changed"
                        } else {
                            "protection_event_title_super_mode_configured"
                        },
                        messageKey = if (wasConfigured) {
                            "protection_event_message_super_mode_credentials_changed"
                        } else {
                            "protection_event_message_super_mode_configured"
                        },
                    )
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
                            recordProtectionEvent(
                                eventType = ProtectionEventType.SUPER_MODE_CLEARED,
                                titleKey = "protection_event_title_super_mode_cleared",
                                messageKey = "protection_event_message_super_mode_cleared",
                            )
                            currentScreen = Screen.ME
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
                    val previousWindow = superModeStatus.windowLabel
                    when (superModeController.updateWindow(startMinutes, endMinutes, proEntitlement.isProActive)) {
                        SuperModeWindowUpdateResult.Success -> {
                            showSuperModeWindowDialog = false
                            recordProtectionEvent(
                                eventType = ProtectionEventType.SUPER_MODE_WINDOW_CHANGED,
                                titleKey = "protection_event_title_super_mode_window_changed",
                                messageKey = "protection_event_message_super_mode_window_changed",
                                messageArgs = listOf(previousWindow, superModeController.formatWindowLabel(startMinutes, endMinutes)),
                            )
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
                            recordProtectionEvent(
                                eventType = ProtectionEventType.SUPER_MODE_CLEARED,
                                titleKey = "protection_event_title_super_mode_cleared",
                                messageKey = "protection_event_message_super_mode_cleared",
                            )
                            showSuperModeDisableDialog = false
                            currentScreen = Screen.ME
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
                currentScreen = Screen.ME_PRO
            },
            onDismiss = { proUpsellSource = null },
        )
    }

    if (showYesterdaySummary) {
        val themeColors = LocalThemeColors.current
        AlertDialog(
            onDismissRequest = { showYesterdaySummary = false },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        androidx.compose.material.icons.Icons.Default.Star,
                        contentDescription = null,
                        tint = themeColors.encourage,
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
                            themeColors.baseContainer,
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
                            color = themeColors.base
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
                                }
                                context.startActivityKeepingCurrentTask(intent)
                            }.onFailure {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivityKeepingCurrentTask(intent)
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
                                }
                                context.startActivityKeepingCurrentTask(intent)
                            }.onFailure {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivityKeepingCurrentTask(intent)
                            }
                        }
                    }
                }
            },
        )
    }
}

private const val PRIVACY_POLICY_URL = "https://rrrroe.github.io/tinyvow/privacy.html"

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

private fun Context.startActivityKeepingCurrentTask(intent: Intent) {
    if (findActivity() == null) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}

private fun android.content.Context.openSupportEmail(
    subject: String,
    body: String? = null,
): Boolean {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:$CONTACT_EMAIL")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(CONTACT_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        if (body != null) {
            putExtra(Intent.EXTRA_TEXT, body)
        }
    }
    return runCatching {
        startActivityKeepingCurrentTask(intent)
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
    permissionReliabilitySnapshot: PermissionReliabilitySnapshot,
    installedApps: List<ManagedApp>,
    groupsWithApps: List<AppGroupWithApps>,
    activeRewardEffects: List<ActiveRewardEffectEntity>,
    userPoints: Double,
    todayPoints: Double,
    overviewInputsReady: Boolean = true,
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
    onOpenPermissionDiagnostics: () -> Unit,
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
    var periodUsageMap by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var todayAppUsageMap by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var todayAppOpenCountMap by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var todaySessions by remember { mutableStateOf<List<AppSession>>(emptyList()) }
    var isOverviewUsageReady by remember { mutableStateOf(false) }
    var createFirstVowRequest by remember { mutableIntStateOf(0) }
    var openBattleGroupDetailRequest by remember { mutableIntStateOf(0) }
    var openBattleGroupDetailGroup by remember { mutableStateOf<AppGroupWithApps?>(null) }
    var showHomeBehaviorRadarDialog by remember { mutableStateOf(false) }
    val historicalArchives =
        archiveRepository?.let { repository ->
            val archives by repository.getRecentArchives(limit = 3650).collectAsStateWithLifecycle(
                initialValue = emptyList(),
                lifecycle = lifecycle,
            )
            archives
        } ?: emptyList()
    val recentGroupArchives =
        archiveRepository?.let { repository ->
            val today = LocalDate.now()
            val from = today.minusDays(7).toString()
            val to = today.minusDays(1).toString()
            val archives by repository.getGroupArchivesByRange(from, to).collectAsStateWithLifecycle(
                initialValue = emptyList(),
                lifecycle = lifecycle,
            )
            archives
        } ?: emptyList()
    val yesterdayArchiveDate = remember { LocalDate.now().minusDays(1).toString() }
    val yesterdayGroupArchives =
        archiveRepository?.let { repository ->
            val archives by repository.getGroupArchivesByDate(yesterdayArchiveDate).collectAsStateWithLifecycle(
                initialValue = emptyList(),
                lifecycle = lifecycle,
            )
            archives
        } ?: emptyList()
    val yesterdayAppArchives =
        archiveRepository?.let { repository ->
            val archives by repository.getAppArchivesByDate(yesterdayArchiveDate).collectAsStateWithLifecycle(
                initialValue = emptyList(),
                lifecycle = lifecycle,
            )
            archives
        } ?: emptyList()
    // Periodically refresh group usage by querying UsageStats once and aggregating packages in memory.
    LaunchedEffect(groupsWithApps, usageAccessStatus) {
        if (usageAccessStatus != UsageAccessStatus.GRANTED) {
            isOverviewUsageReady = false
            usageMap = emptyMap()
            periodUsageMap = emptyMap()
            todayAppUsageMap = emptyMap()
            todayAppOpenCountMap = emptyMap()
            todaySessions = emptyList()
            return@LaunchedEffect
        }
        isOverviewUsageReady = false
        val usageRepo = MergedUsageRepository(context)
        while (true) {
            runCatching {
                val zoneId = ZoneId.systemDefault()
                val today = LocalDate.now(zoneId)
                val todayStart = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
                val now = System.currentTimeMillis()
                val newTodayAppUsageMap = usageRepo.getUsageStats(todayStart, now, null)
                val newTodayAppOpenCountMap = usageRepo.getAppOpenCount(todayStart, now)
                val newTodaySessions = usageRepo.getUsageSessions(todayStart, now)
                val todayUsageByGroupType = mutableMapOf<GroupType, Map<String, Long>>()
                groupsWithApps
                    .map { it.group.type }
                    .distinct()
                    .forEach { groupType ->
                        todayUsageByGroupType[groupType] = usageRepo.getUsageStats(todayStart, now, groupType)
                    }
                val periodUsageByGroupConfig = mutableMapOf<Pair<GroupType, LimitPeriod>, Map<String, Long>>()
                groupsWithApps
                    .map { it.group.type to it.group.limitPeriod }
                    .distinct()
                    .forEach { (groupType, limitPeriod) ->
                        periodUsageByGroupConfig[groupType to limitPeriod] =
                            usageRepo.getUsageStatsInPeriod(limitPeriod, groupType)
                    }
                val newUsageMap =
                    groupsWithApps.associate { groupWithApps ->
                        val groupUsage = todayUsageByGroupType[groupWithApps.group.type].orEmpty()
                        groupWithApps.group.id to
                            groupWithApps.packageNames.sumOf { packageName ->
                                groupUsage[packageName] ?: 0L
                            }
                    }
                val newPeriodUsageMap =
                    groupsWithApps.associate { groupWithApps ->
                        val groupUsage =
                            periodUsageByGroupConfig[
                                groupWithApps.group.type to groupWithApps.group.limitPeriod
                            ].orEmpty()
                        groupWithApps.group.id to
                            groupWithApps.packageNames.sumOf { packageName ->
                                groupUsage[packageName] ?: 0L
                            }
                    }
                usageMap = newUsageMap
                periodUsageMap = newPeriodUsageMap
                todayAppUsageMap = newTodayAppUsageMap
                todayAppOpenCountMap = newTodayAppOpenCountMap
                todaySessions = newTodaySessions
                isOverviewUsageReady = true
            }
            kotlinx.coroutines.delay(5000L)
        }
    }
    val usageAccessGranted = usageAccessStatus == UsageAccessStatus.GRANTED
    val statusColor = if (usageAccessGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val isOverviewReady = overviewInputsReady && isOverviewUsageReady

    val overviewState =
        remember(
            context,
            groupsWithApps,
            usageMap,
            periodUsageMap,
            todayAppUsageMap,
            todayAppOpenCountMap,
            todaySessions,
            activeRewardEffects,
            recentGroupArchives,
            yesterdayGroupArchives,
            yesterdayAppArchives,
            historicalArchives,
            userPoints,
            todayPoints,
            achievementProgress,
        ) {
            buildHomeOverviewUiState(
                context = context,
                groupsWithApps = groupsWithApps,
                usageMap = usageMap,
                periodUsageMap = periodUsageMap,
                todayAppUsageMap = todayAppUsageMap,
                todayAppOpenCountMap = todayAppOpenCountMap,
                todaySessions = todaySessions,
                activeRewardEffects = activeRewardEffects,
                recentGroupArchives = recentGroupArchives,
                yesterdayGroupArchives = yesterdayGroupArchives,
                yesterdayAppArchives = yesterdayAppArchives,
                historicalArchives = historicalArchives,
                userPoints = userPoints,
                todayPoints = todayPoints,
                achievementProgress = achievementProgress,
            )
        }
    val battleActions =
        remember(
            overviewState.battleActions,
            usageAccessGranted,
            accessibilityServiceEnabled,
        ) {
            buildList {
                if (!usageAccessGranted) {
                    add(
                        HomeBattleAction(
                            type = HomeBattleActionType.PERMISSION_USAGE,
                            title = AppText.t("home_battle_permission_usage_title"),
                            subtitle = AppText.t("home_battle_permission_usage_body"),
                            value = AppText.t("home_battle_permission_fix"),
                            progress = 0f,
                        ),
                    )
                } else if (!accessibilityServiceEnabled) {
                    add(
                        HomeBattleAction(
                            type = HomeBattleActionType.PERMISSION_ACCESSIBILITY,
                            title = AppText.t("home_battle_permission_accessibility_title"),
                            subtitle = AppText.t("home_battle_permission_accessibility_body"),
                            value = AppText.t("home_battle_permission_fix"),
                            progress = 0f,
                        ),
                    )
                }
                addAll(overviewState.battleActions)
            }.take(2)
        }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(homeScrollState)
        ) {
            val startupDismissIds =
                startupReliabilityDismissIds(
                    snapshot = permissionReliabilitySnapshot,
                    dismissedPermissionPrompts = dismissedPermissionPrompts,
                )
            val showStartupReliabilityCard =
                when (permissionReliabilitySnapshot.primaryStep) {
                    StartupReliabilityStep.CREATE_FIRST_VOW -> true
                    StartupReliabilityStep.READY -> startupDismissIds.isNotEmpty()
                    else -> startupDismissIds.isNotEmpty()
                }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = TinyVowSpacing.PageHorizontal,
                        end = TinyVowSpacing.PageHorizontal,
                        top = TinyVowSpacing.PageTop,
                    ),
                verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap)
            ) {
                if (usageAccessGranted) {
                    HomeOverviewHeader(
                        dateLabel = overviewState.dateLabel,
                        superModeStatus = superModeStatus,
                        onOpenSuperModeInfo = onOpenSuperModeEntry,
                    )
                    if (isOverviewReady) {
                        HomeOverviewPaperCard(
                            state = overviewState,
                            onOpenBehaviorRadar = { showHomeBehaviorRadarDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        HomeOverviewLoadingCard(modifier = Modifier.fillMaxWidth())
                    }
                }

                HomeBattleStation(
                    actions = battleActions,
                    onActionClick = { action ->
                        when (action.type) {
                            HomeBattleActionType.CONTROL,
                            HomeBattleActionType.ENCOURAGE -> {
                                action.group?.let { group ->
                                    openBattleGroupDetailGroup = group
                                    openBattleGroupDetailRequest += 1
                                }
                            }
                            HomeBattleActionType.REWARD -> onNavigateToRedeem()
                            HomeBattleActionType.CREATE -> createFirstVowRequest += 1
                            HomeBattleActionType.PERMISSION_USAGE -> onOpenUsageAccessSettings()
                            HomeBattleActionType.PERMISSION_ACCESSIBILITY -> onOpenAccessibilitySettings()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                if (usageAccessGranted && activeRewardEffects.isNotEmpty()) {
                    HomeActiveEffectsCard(
                        effects = activeRewardEffects,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (showStartupReliabilityCard) {
                    StartupReliabilityCard(
                        snapshot = permissionReliabilitySnapshot,
                        onCreateFirstVow = {
                            createFirstVowRequest += 1
                        },
                        onOpenUsageAccessSettings = onOpenUsageAccessSettings,
                        onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                        onOpenPermissionDiagnostics = onOpenPermissionDiagnostics,
                        onDismiss =
                            startupDismissIds
                                .takeIf { it.isNotEmpty() }
                                ?.let { ids -> { onDismissPermissionPrompts(ids) } },
                    )
                }
            }

            if (showHomeBehaviorRadarDialog) {
                HomeBehaviorRadarDialog(
                    metrics = homeOverviewScoreMetrics(overviewState),
                    comparisonMetrics = overviewState.behaviorComparisonMetrics,
                    onDismiss = { showHomeBehaviorRadarDialog = false },
                )
            }

            if (installedApps.isNotEmpty() || groupsWithApps.isNotEmpty() || isLoadingApps) {
                val dashboardTopSpacing = if (showStartupReliabilityCard) 8.dp else 16.dp
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                start = TinyVowSpacing.PageHorizontal,
                                end = TinyVowSpacing.PageHorizontal,
                                top = dashboardTopSpacing,
                            ),
                    verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
                ) {
                    GroupDashboard(
                        groupsWithApps = groupsWithApps,
                        usageMap = usageMap,
                        activeRewardEffects = activeRewardEffects,
                        installedApps = installedApps,
                        isLoadingApps = isLoadingApps,
                        onSaveGroup = onSaveGroup,
                        onDeleteGroup = onDeleteGroup,
                        onReorderGroups = { type, ids ->
                            coroutineScope.launch { appLimitRepository?.reorderGroups(type, ids) }
                        },
                        createGroupRequest = createFirstVowRequest,
                        archiveRepository = archiveRepository,
                        isProActive = isProActive,
                        onShowProUpsell = onShowProUpsell,
                        onGuardAction = onGuardAction,
                        openGroupDetailRequest = openBattleGroupDetailRequest,
                        openGroupDetailGroup = openBattleGroupDetailGroup,
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
    }
}

@Composable
private fun StartupReliabilityCard(
    snapshot: PermissionReliabilitySnapshot,
    onCreateFirstVow: () -> Unit,
    onOpenUsageAccessSettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenPermissionDiagnostics: () -> Unit,
    onDismiss: (() -> Unit)?,
) {
    val themeColors = LocalThemeColors.current
    val (titleKey, bodyKey, actionKey, action) =
        when (snapshot.primaryStep) {
            StartupReliabilityStep.CREATE_FIRST_VOW ->
                Quadruple(
                    "startup_reliability_create_vow_title",
                    "startup_reliability_create_vow_body",
                    "startup_reliability_create_vow_action",
                    onCreateFirstVow,
                )
            StartupReliabilityStep.ENABLE_USAGE_ACCESS ->
                Quadruple(
                    "startup_reliability_usage_access_title",
                    "startup_reliability_usage_access_body",
                    "permission_primary_action",
                    onOpenUsageAccessSettings,
                )
            StartupReliabilityStep.ACCEPT_ACCESSIBILITY_DISCLOSURE ->
                Quadruple(
                    "startup_reliability_accessibility_disclosure_title",
                    "startup_reliability_accessibility_disclosure_body",
                    "startup_reliability_accessibility_action",
                    onOpenAccessibilitySettings,
                )
            StartupReliabilityStep.ENABLE_ACCESSIBILITY_SERVICE ->
                Quadruple(
                    "startup_reliability_accessibility_service_title",
                    "startup_reliability_accessibility_service_body",
                    "accessibility_card_action",
                    onOpenAccessibilitySettings,
                )
            StartupReliabilityStep.CHECK_ACCESSIBILITY_HEALTH ->
                Quadruple(
                    "startup_reliability_accessibility_health_title",
                    "startup_reliability_accessibility_health_body",
                    "startup_reliability_review_action",
                    onOpenPermissionDiagnostics,
                )
            StartupReliabilityStep.READY ->
                Quadruple(
                    if (snapshot.optionalSuggestionCount > 0) {
                        "startup_reliability_optional_title"
                    } else {
                        "startup_reliability_ready_title"
                    },
                    if (snapshot.optionalSuggestionCount > 0) {
                        "startup_reliability_optional_body"
                    } else {
                        "startup_reliability_ready_body"
                    },
                    "startup_reliability_review_action",
                    onOpenPermissionDiagnostics,
                )
        }

    TinyVowCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        borderAlpha = 0.22f,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = TinyVowSpacing.CardHorizontal,
                        vertical = TinyVowSpacing.CardVertical,
                    ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (snapshot.coreReady) themeColors.encourageContainer else themeColors.controlContainer,
                    tonalElevation = 0.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (snapshot.coreReady) Icons.Default.CheckCircle else Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (snapshot.coreReady) themeColors.encourage else themeColors.control,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = AppText.t(titleKey),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.inkStrong,
                    )
                }
            }

            Text(
                text =
                    if (bodyKey == "startup_reliability_optional_body") {
                        AppText.t(bodyKey, snapshot.optionalSuggestionCount)
                    } else {
                        AppText.t(bodyKey)
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = themeColors.ink,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (onDismiss != null) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(AppText.t("home_dismiss"), maxLines = 1)
                    }
                }
                Button(
                    onClick = action,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(AppText.t(actionKey), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)

private fun startupReliabilityDismissIds(
    snapshot: PermissionReliabilitySnapshot,
    dismissedPermissionPrompts: Set<String>,
): List<String> =
    when (snapshot.primaryStep) {
        StartupReliabilityStep.CREATE_FIRST_VOW -> emptyList()
        StartupReliabilityStep.ENABLE_USAGE_ACCESS ->
            listOf(PermissionPromptIds.USAGE_ACCESS).filterNot { it in dismissedPermissionPrompts }
        StartupReliabilityStep.ACCEPT_ACCESSIBILITY_DISCLOSURE,
        StartupReliabilityStep.ENABLE_ACCESSIBILITY_SERVICE,
        StartupReliabilityStep.CHECK_ACCESSIBILITY_HEALTH ->
            listOf(PermissionPromptIds.ACCESSIBILITY).filterNot { it in dismissedPermissionPrompts }
        StartupReliabilityStep.READY ->
            buildList {
                if (!snapshot.notificationPermissionGranted) add(PermissionPromptIds.NOTIFICATION)
                if (!snapshot.isIgnoringBatteryOptimizations) add(PermissionPromptIds.BATTERY)
                if (!snapshot.isAutoStartDismissed) add(PermissionPromptIds.BACKGROUND_START)
            }.filterNot { it in dismissedPermissionPrompts }
    }

@Composable
private fun CompactPermissionBanner(
    prompts: List<Pair<String, String>>,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
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
                color = themeColors.ink,
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
private fun FirstRunCoachmarkOverlay(
    onTargetScreenChange: (Screen) -> Unit,
    onComplete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val steps =
        remember {
            listOf(
                FirstRunCoachmarkStep(
                    screen = Screen.HOME,
                    icon = Icons.Default.Home,
                    titleKey = "tutorial_bubble_home_title",
                    bodyKey = "tutorial_bubble_home_body",
                    alignment = CoachmarkBubbleAlignment.Top,
                ),
                FirstRunCoachmarkStep(
                    screen = Screen.HOME,
                    icon = Icons.Default.AddCircle,
                    titleKey = "tutorial_bubble_groups_title",
                    bodyKey = "tutorial_bubble_groups_body",
                    alignment = CoachmarkBubbleAlignment.Center,
                ),
                FirstRunCoachmarkStep(
                    screen = Screen.STATS,
                    icon = Icons.Default.BarChart,
                    titleKey = "tutorial_bubble_stats_title",
                    bodyKey = "tutorial_bubble_stats_body",
                    alignment = CoachmarkBubbleAlignment.Bottom,
                ),
                FirstRunCoachmarkStep(
                    screen = Screen.REWARDS,
                    icon = Icons.Default.CardGiftcard,
                    titleKey = "tutorial_bubble_rewards_title",
                    bodyKey = "tutorial_bubble_rewards_body",
                    alignment = CoachmarkBubbleAlignment.Bottom,
                ),
                FirstRunCoachmarkStep(
                    screen = Screen.ME,
                    icon = Icons.Default.Person,
                    titleKey = "tutorial_bubble_me_title",
                    bodyKey = "tutorial_bubble_me_body",
                    alignment = CoachmarkBubbleAlignment.Bottom,
                ),
            )
        }
    var stepIndex by remember { mutableIntStateOf(0) }
    val currentStep = steps[stepIndex]
    val isLastStep = stepIndex == steps.lastIndex

    LaunchedEffect(currentStep.screen) {
        onTargetScreenChange(currentStep.screen)
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.38f))
                .padding(horizontal = 18.dp, vertical = 24.dp),
        contentAlignment =
            when (currentStep.alignment) {
                CoachmarkBubbleAlignment.Top -> Alignment.TopCenter
                CoachmarkBubbleAlignment.Center -> Alignment.Center
                CoachmarkBubbleAlignment.Bottom -> Alignment.BottomCenter
            },
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
                    .padding(bottom = if (currentStep.alignment == CoachmarkBubbleAlignment.Bottom) 88.dp else 0.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = themeColors.baseContainer,
                        tonalElevation = 0.dp,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = currentStep.icon,
                                contentDescription = null,
                                tint = themeColors.base,
                                modifier = Modifier.size(23.dp),
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppText.t(currentStep.titleKey),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = themeColors.inkStrong,
                        )
                        Text(
                            text = AppText.t("tutorial_bubble_step_count", stepIndex + 1, steps.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = themeColors.inkMuted,
                        )
                    }
                }

                Text(
                    text = AppText.t(currentStep.bodyKey),
                    style = MaterialTheme.typography.bodyMedium,
                    color = themeColors.ink,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(AppText.t("tutorial_bubble_skip"))
                    }
                    Button(
                        onClick = {
                            if (isLastStep) {
                                onComplete()
                            } else {
                                stepIndex += 1
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            if (isLastStep) {
                                AppText.t("tutorial_bubble_finish")
                            } else {
                                AppText.t("welcome_intro_next")
                            },
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeOverviewPaperCard(
    state: HomeOverviewUiState,
    onOpenBehaviorRadar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    val score = homeOverviewScore(state)
    val scoreMetrics = homeOverviewScoreMetrics(state)
    val ringTrackColor = themeColors.inkFaint.copy(alpha = 0.30f)
    val revealProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        revealProgress.snapTo(0f)
        revealProgress.animateTo(
            targetValue = 1f,
            animationSpec =
                tween(
                    durationMillis = 720,
                    easing = FastOutSlowInEasing,
                ),
        )
    }

    TinyVowCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
        borderAlpha = 0.30f,
        shadowElevation = TinyVowElevation.FeaturedCard,
    ) {
        BoxWithConstraints(
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CardHorizontal,
                vertical = TinyVowSpacing.CardVertical,
            ),
        ) {
            val centerSize = if (maxWidth < 360.dp) 126.dp else 134.dp
            val wingHeight = if (maxWidth < 360.dp) 232.dp else 242.dp
            val centerGap = centerSize - 38.dp
            val density = LocalDensity.current
            val centerGapPx = with(density) { centerGap.toPx() }
            val notchRadiusPx = with(density) { (centerSize / 2 + 26.dp).toPx() }
            val compact = maxWidth < 380.dp
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(wingHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(wingHeight),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        HomeOverviewWingPanel(
                            isLeft = true,
                            title = AppText.t("home_commitment_panel"),
                            label = AppText.t("home_saved_today"),
                            value = state.control.todaySavedMinutes.toString(),
                            unit = AppText.t("group_minutes"),
                            progress = AppText.t("home_commitment_progress_value", state.control.completedGroups, state.control.totalGroups),
                            streak = state.control.streakLabel,
                            primaryMetricLabel = AppText.t("home_total_saved"),
                            primaryMetricValue = state.history.totalSavedMinutes.toString(),
                            primaryMetricUnit = AppText.t("group_minutes"),
                            secondaryMetricLabel = AppText.t("home_equivalent_live_more"),
                            secondaryMetricValue = roundedDaysValue(state.history.extendedLifeMinutes).toString(),
                            secondaryMetricUnit = AppText.t("home_day_unit"),
                            color = themeColors.controlContainer,
                            contentColor = themeColors.onControlContainer,
                            accent = themeColors.control,
                            compact = compact,
                            revealProgress = revealProgress,
                            centerGapPx = centerGapPx,
                            notchRadiusPx = notchRadiusPx,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(centerGap))
                        HomeOverviewWingPanel(
                            isLeft = false,
                            title = AppText.t("home_encouragement_panel"),
                            label = state.encourage.pointsMultiplierLabel?.let {
                                AppText.t("home_earned_today_with_badge", it)
                            } ?: AppText.t("home_earned_today"),
                            value = formatHomePointWholeValue(state.encourage.todayEarnedPoints),
                            unit = AppText.t("group_points"),
                            progress = AppText.t("home_encouragement_progress_value", state.encourage.completedGroups, state.encourage.totalGroups),
                            streak = state.encourage.streakLabel,
                            primaryMetricLabel = AppText.t("home_total_earned"),
                            primaryMetricValue = formatHomePointValue(state.history.totalEarnedPoints),
                            primaryMetricUnit = AppText.t("group_points"),
                            secondaryMetricLabel = AppText.t("home_current_remaining"),
                            secondaryMetricValue = formatHomePointValue(state.history.currentPoints),
                            secondaryMetricUnit = AppText.t("group_points"),
                            color = themeColors.encourageContainer,
                            contentColor = themeColors.onEncourageContainer,
                            accent = themeColors.encourage,
                            compact = compact,
                            revealProgress = revealProgress,
                            centerGapPx = centerGapPx,
                            notchRadiusPx = notchRadiusPx,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    HomeOverviewScoreDial(
                        score = score,
                        metrics = scoreMetrics,
                        ringTrackColor = ringTrackColor,
                        scoreColor = themeColors.inkStrong,
                        revealProgress = revealProgress,
                        onClick = onOpenBehaviorRadar,
                        modifier = Modifier.size(centerSize),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeOverviewLoadingCard(
    modifier: Modifier = Modifier,
) {
    TinyVowCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
        borderAlpha = 0.24f,
        shadowElevation = TinyVowElevation.FeaturedCard,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(242.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(strokeWidth = 2.5.dp)
        }
    }
}

@Composable
private fun HomeBehaviorRadarDialog(
    metrics: List<DailyBehaviorScoreMetric>,
    comparisonMetrics: List<DailyBehaviorScoreMetric>,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = AppText.t("home_behavior_radar_title"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = AppText.t("home_behavior_radar_subtitle"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                BehaviorRadarPanel(
                    metrics = metrics,
                    comparisonMetrics = comparisonMetrics,
                )
                TinyVowButton(
                    text = AppText.t("stats_score_info_close"),
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    tone = TinyVowButtonTone.Primary,
                )
            }
        }
    }
}

@Composable
private fun HomeOverviewScoreDial(
    score: Int,
    metrics: List<DailyBehaviorScoreMetric>,
    ringTrackColor: Color,
    scoreColor: Color,
    revealProgress: Animatable<Float, AnimationVector1D>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val displaySegments = metrics.take(5)
    val boundedRevealProgress = revealProgress.value.coerceIn(0f, 1f)

    Box(
        modifier =
            modifier
                .clip(CircleShape)
                .clickable(
                    onClickLabel = AppText.t("home_behavior_radar_action"),
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()
            val arcInset = strokeWidth / 2f
            val arcTopLeft = Offset(arcInset, arcInset)
            val arcSize =
                androidx.compose.ui.geometry.Size(
                    width = size.width - strokeWidth,
                    height = size.height - strokeWidth,
                )
            drawArc(
                color = ringTrackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            val segmentCapacity = 360f / displaySegments.size.coerceAtLeast(1)
            val targetSweeps =
                displaySegments.map { segment ->
                    segmentCapacity * (segment.score.coerceIn(0, 100) / 100f)
                }
            var remainingRevealSweep = targetSweeps.sum() * boundedRevealProgress
            var startAngle = -90f
            val center = Offset(size.width / 2f, size.height / 2f)
            val radiusX = arcSize.width / 2f
            val radiusY = arcSize.height / 2f
            val forwardCaps = mutableListOf<Pair<Color, Float>>()
            targetSweeps.forEachIndexed { index, sweep ->
                val color = displaySegments.getOrNull(index)?.let { behaviorScoreAccentColor(it.accentIndex) } ?: scoreColor
                val visibleSweep = min(sweep, remainingRevealSweep)
                if (visibleSweep <= 0f) return@forEachIndexed
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = visibleSweep,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                )
                forwardCaps += color to (startAngle + visibleSweep)
                remainingRevealSweep -= visibleSweep
                startAngle += sweep
            }
            forwardCaps.asReversed().forEach { (color, capAngle) ->
                drawHomeOverviewForwardHalfCap(
                    color = color,
                    arcCenter = center,
                    radiusX = radiusX,
                    radiusY = radiusY,
                    capRadius = strokeWidth / 2f,
                    angleDegrees = capAngle,
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val animatedScore = homeAnimatedNumberText(
                targetText = score.toString(),
                progress = boundedRevealProgress,
            )
            Text(
                text = animatedScore,
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 44.sp),
                fontWeight = FontWeight.ExtraBold,
                color = scoreColor,
            )
            Text(
                text = AppText.t(homeOverviewScoreStatusKey(score, LocalDate.now())),
                modifier = Modifier.padding(top = 0.dp),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                fontWeight = FontWeight.Bold,
                color = scoreColor,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
        }
    }
}

private fun homeOverviewArcPoint(
    arcCenter: Offset,
    radiusX: Float,
    radiusY: Float,
    angleDegrees: Float,
): Offset {
    val radians = Math.toRadians(angleDegrees.toDouble())
    val cosValue = kotlin.math.cos(radians).toFloat()
    val sinValue = kotlin.math.sin(radians).toFloat()
    return Offset(
        x = arcCenter.x + cosValue * radiusX,
        y = arcCenter.y + sinValue * radiusY,
    )
}

private fun DrawScope.drawHomeOverviewForwardHalfCap(
    color: Color,
    arcCenter: Offset,
    radiusX: Float,
    radiusY: Float,
    capRadius: Float,
    angleDegrees: Float,
) {
    val radians = Math.toRadians(angleDegrees.toDouble())
    val cosValue = kotlin.math.cos(radians).toFloat()
    val sinValue = kotlin.math.sin(radians).toFloat()
    val capCenter =
        Offset(
            x = arcCenter.x + cosValue * radiusX,
            y = arcCenter.y + sinValue * radiusY,
        )
    val rawTangentX = -radiusX * sinValue
    val rawTangentY = radiusY * cosValue
    val tangentLength =
        kotlin.math.sqrt(rawTangentX * rawTangentX + rawTangentY * rawTangentY)
            .coerceAtLeast(0.001f)
    val tangentX = rawTangentX / tangentLength
    val tangentY = rawTangentY / tangentLength
    val normalX = -tangentY
    val normalY = tangentX
    val reach = size.maxDimension * 2f + capRadius
    val clip =
        Path().apply {
            moveTo(capCenter.x + normalX * reach, capCenter.y + normalY * reach)
            lineTo(
                capCenter.x + tangentX * reach + normalX * reach,
                capCenter.y + tangentY * reach + normalY * reach,
            )
            lineTo(
                capCenter.x + tangentX * reach - normalX * reach,
                capCenter.y + tangentY * reach - normalY * reach,
            )
            lineTo(capCenter.x - normalX * reach, capCenter.y - normalY * reach)
            close()
        }
    clipPath(clip) {
        drawCircle(color = color, radius = capRadius, center = capCenter)
    }
}

private const val HOME_SCORE_STATUS_VARIANTS_PER_STAGE = 20

private fun homeOverviewScoreStatusKey(score: Int, date: LocalDate): String {
    if (score >= 100) return "home_score_status_perfect"
    val stage =
        when (score.coerceIn(0, 100)) {
            in 0..19 -> 1
            in 20..39 -> 2
            in 40..59 -> 3
            in 60..79 -> 4
            else -> 5
        }
    val variant =
        (((date.toEpochDay() * 31L) + (stage * 17L)).floorMod(HOME_SCORE_STATUS_VARIANTS_PER_STAGE.toLong()) + 1L).toInt()
    return "home_score_status_stage%d_%02d".format(java.util.Locale.US, stage, variant)
}

@Composable
private fun HomeOverviewWingPanel(
    isLeft: Boolean,
    title: String,
    label: String,
    value: String,
    unit: String,
    progress: String,
    streak: String,
    primaryMetricLabel: String,
    primaryMetricValue: String,
    primaryMetricUnit: String,
    secondaryMetricLabel: String,
    secondaryMetricValue: String,
    secondaryMetricUnit: String,
    color: Color,
    contentColor: Color,
    accent: Color,
    compact: Boolean,
    revealProgress: Animatable<Float, AnimationVector1D>,
    centerGapPx: Float,
    notchRadiusPx: Float,
    modifier: Modifier = Modifier,
) {
    val panelShape = remember(isLeft, centerGapPx, notchRadiusPx) {
        homeOverviewWingShape(
            isLeft = isLeft,
            centerGapPx = centerGapPx,
            notchRadiusPx = notchRadiusPx,
        )
    }
    val horizontalAlignment = if (isLeft) Alignment.Start else Alignment.End
    val textAlign =
        if (isLeft) {
            androidx.compose.ui.text.style.TextAlign.Start
        } else {
            androidx.compose.ui.text.style.TextAlign.End
        }
    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = panelShape,
        color = color,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.10f)),
    ) {
        Column(
            modifier = Modifier.padding(
                start = if (isLeft) 14.dp else if (compact) 18.dp else 22.dp,
                end = if (isLeft) if (compact) 18.dp else 22.dp else 14.dp,
                top = 14.dp,
                bottom = 14.dp,
            ),
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = Arrangement.spacedBy(if (compact) 7.dp else 8.dp),
        ) {
            Column(horizontalAlignment = horizontalAlignment, verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = if (compact) 17.sp else 18.sp,
                        lineHeight = if (compact) 20.sp else 21.sp,
                    ),
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    textAlign = textAlign,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = if (compact) 11.sp else 12.sp,
                        lineHeight = 15.sp,
                    ),
                    color = contentColor.copy(alpha = 0.66f),
                    textAlign = textAlign,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Column(horizontalAlignment = horizontalAlignment, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                HomeOverviewWingMainMetric(
                    value = value,
                    unit = unit,
                    contentColor = contentColor,
                    compact = compact,
                    alignEnd = !isLeft,
                    revealProgress = revealProgress,
                )
                HomeOverviewWingPillRow(
                    first = progress,
                    second = streak,
                    contentColor = contentColor,
                    alignEnd = !isLeft,
                )
            }

            Column(horizontalAlignment = horizontalAlignment, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                HomeOverviewWingMiniMetric(
                    label = primaryMetricLabel,
                    value = primaryMetricValue,
                    unit = primaryMetricUnit,
                    contentColor = contentColor,
                    alignEnd = !isLeft,
                )
                HomeOverviewWingMiniMetric(
                    label = secondaryMetricLabel,
                    value = secondaryMetricValue,
                    unit = secondaryMetricUnit,
                    contentColor = contentColor,
                    alignEnd = !isLeft,
                )
            }
        }
    }
}

@Composable
private fun HomeOverviewWingMainMetric(
    value: String,
    unit: String,
    contentColor: Color,
    compact: Boolean,
    alignEnd: Boolean,
    revealProgress: Animatable<Float, AnimationVector1D>,
) {
    val animatedValue = homeAnimatedNumberText(value, revealProgress.value.coerceIn(0f, 1f))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = animatedValue,
            style =
                if (compact) {
                    MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp, lineHeight = 34.sp)
                } else {
                    MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp, lineHeight = 38.sp)
                },
            fontWeight = FontWeight.ExtraBold,
            color = contentColor,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = unit,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = if (compact) 11.5.sp else 12.5.sp,
                lineHeight = 15.sp,
            ),
            fontWeight = FontWeight.SemiBold,
            color = contentColor.copy(alpha = 0.72f),
            modifier = Modifier.padding(bottom = 5.dp),
            maxLines = 1,
        )
    }
}

@Composable
private fun HomeOverviewWingPillRow(
    first: String,
    second: String,
    contentColor: Color,
    alignEnd: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        HomeOverviewWingPill(text = first, contentColor = contentColor, alignEnd = alignEnd)
        HomeOverviewWingPill(text = second, contentColor = contentColor, alignEnd = alignEnd)
    }
}

@Composable
private fun HomeOverviewWingPill(
    text: String,
    contentColor: Color,
    alignEnd: Boolean,
) {
    if (text.isBlank()) return
    Text(
        text = homeOverviewEmphasizedNumberText(text, contentColor),
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.labelMedium.copy(
            fontSize = 11.5.sp,
            lineHeight = 16.sp,
        ),
        fontWeight = FontWeight.Medium,
        color = contentColor.copy(alpha = 0.68f),
        textAlign =
            if (alignEnd) {
                androidx.compose.ui.text.style.TextAlign.End
            } else {
                androidx.compose.ui.text.style.TextAlign.Start
            },
        maxLines = 1,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
    )
}

private fun homeAnimatedNumberText(
    targetText: String,
    progress: Float,
): String {
    val target = targetText.replace(",", "").toFloatOrNull() ?: return targetText
    val decimalCount = targetText.substringAfter('.', "").takeIf { targetText.contains('.') }?.length ?: 0
    val animatedValue = target * progress.coerceIn(0f, 1f)

    return if (decimalCount > 0) {
        java.lang.String.format(
            java.util.Locale.getDefault(),
            "%.${decimalCount}f",
            animatedValue,
        )
    } else {
        animatedValue.roundToInt().toString()
    }
}

private fun homeOverviewEmphasizedNumberText(
    text: String,
    contentColor: Color,
) = buildAnnotatedString {
    var cursor = 0
    Regex("""[\d.,/]+""").findAll(text).forEach { match ->
        append(text.substring(cursor, match.range.first))
        withStyle(
            SpanStyle(
                fontWeight = FontWeight.Bold,
                color = contentColor.copy(alpha = 0.88f),
            ),
        ) {
            append(match.value)
        }
        cursor = match.range.last + 1
    }
    append(text.substring(cursor))
}

@Composable
private fun HomeOverviewWingMiniMetric(
    label: String,
    value: String,
    unit: String,
    contentColor: Color,
    alignEnd: Boolean,
) {
    val textAlign =
        if (alignEnd) {
            androidx.compose.ui.text.style.TextAlign.End
        } else {
            androidx.compose.ui.text.style.TextAlign.Start
        }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.5.sp,
                lineHeight = 14.sp,
            ),
            color = contentColor.copy(alpha = 0.58f),
            textAlign = textAlign,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "$value $unit",
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 13.sp,
                lineHeight = 17.sp,
            ),
            fontWeight = FontWeight.Bold,
            color = contentColor.copy(alpha = 0.88f),
            textAlign = textAlign,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun homeOverviewWingShape(
    isLeft: Boolean,
    centerGapPx: Float,
    notchRadiusPx: Float,
) =
    GenericShape { size, _ ->
        val outerRadius = min(size.width * 0.18f, size.height * 0.11f)
        val circleCenterOffset = centerGapPx / 2f
        val radius = notchRadiusPx.coerceAtLeast(circleCenterOffset + 1f)
        val halfArcHeight = sqrt((radius * radius - circleCenterOffset * circleCenterOffset).coerceAtLeast(0f))
            .coerceAtMost(size.height / 2f - outerRadius)
        val centerY = size.height / 2f
        val notchTop = centerY - halfArcHeight
        val notchBottom = centerY + halfArcHeight
        if (isLeft) {
            val circleCenterX = size.width + circleCenterOffset
            val startAngle = homeOverviewAngleDegrees(
                x = size.width - circleCenterX,
                y = notchTop - centerY,
            )
            val endAngle = homeOverviewAngleDegrees(
                x = size.width - circleCenterX,
                y = notchBottom - centerY,
            )
            val sweepAngle = homeOverviewCounterClockwiseSweep(startAngle, endAngle)
            val circleBounds = Rect(
                left = circleCenterX - radius,
                top = centerY - radius,
                right = circleCenterX + radius,
                bottom = centerY + radius,
            )
            moveTo(0f, 0f)
            lineTo(size.width - outerRadius, 0f)
            quadraticTo(size.width, 0f, size.width, outerRadius)
            lineTo(size.width, notchTop)
            arcTo(circleBounds, startAngle, sweepAngle, false)
            lineTo(size.width, size.height - outerRadius)
            quadraticTo(size.width, size.height, size.width - outerRadius, size.height)
            lineTo(outerRadius, size.height)
            quadraticTo(0f, size.height, 0f, size.height - outerRadius)
            lineTo(0f, outerRadius)
            quadraticTo(0f, 0f, outerRadius, 0f)
            close()
        } else {
            val circleCenterX = -circleCenterOffset
            val startAngle = homeOverviewAngleDegrees(
                x = -circleCenterX,
                y = notchBottom - centerY,
            )
            val endAngle = homeOverviewAngleDegrees(
                x = -circleCenterX,
                y = notchTop - centerY,
            )
            val sweepAngle = homeOverviewCounterClockwiseSweep(startAngle, endAngle)
            val circleBounds = Rect(
                left = circleCenterX - radius,
                top = centerY - radius,
                right = circleCenterX + radius,
                bottom = centerY + radius,
            )
            moveTo(outerRadius, 0f)
            lineTo(size.width - outerRadius, 0f)
            quadraticTo(size.width, 0f, size.width, outerRadius)
            lineTo(size.width, size.height - outerRadius)
            quadraticTo(size.width, size.height, size.width - outerRadius, size.height)
            lineTo(outerRadius, size.height)
            quadraticTo(0f, size.height, 0f, size.height - outerRadius)
            lineTo(0f, notchBottom)
            arcTo(circleBounds, startAngle, sweepAngle, false)
            lineTo(0f, outerRadius)
            quadraticTo(0f, 0f, outerRadius, 0f)
            close()
        }
    }

private fun homeOverviewAngleDegrees(x: Float, y: Float): Float =
    (atan2(y, x) * 180f / PI.toFloat())

private fun homeOverviewCounterClockwiseSweep(startAngle: Float, endAngle: Float): Float {
    var sweep = endAngle - startAngle
    while (sweep >= 0f) sweep -= 360f
    return sweep
}

@Composable
private fun HomeOverviewPaperMetric(
    label: String,
    value: String,
    unit: String,
    supporting: String,
    color: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = color,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.72f),
                    modifier = Modifier.padding(bottom = 3.dp),
                    maxLines = 1,
                )
            }
            Text(
                text = supporting,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.68f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HomeOverviewSplitCards(
    state: HomeOverviewUiState,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(236.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left Card (CONTROL)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(32.dp))
                    .background(themeColors.control)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppText.t("home_commitment_panel"),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color.White
                        )
                    }

                    // Today Metric
                    Column(
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = AppText.t("home_saved_today"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.White.copy(alpha = 0.70f)
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = state.control.todaySavedMinutes.toString(),
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = AppText.t("group_minutes"),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White.copy(alpha = 0.80f),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    // Progress Pills
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HomeOverviewSplitPill(text = AppText.t("home_commitment_progress_value", state.control.completedGroups, state.control.totalGroups))
                        HomeOverviewSplitPill(text = state.control.streakLabel)
                    }

                    // Divider
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.15f),
                        thickness = 0.5.dp
                    )

                    // Stacked history complications
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        HomeHistoryMetricSplit(
                            label = AppText.t("home_total_saved"),
                            value = state.history.totalSavedMinutes.toString(),
                            unit = AppText.t("group_minutes")
                        )
                        HomeHistoryMetricSplit(
                            label = AppText.t("home_equivalent_live_more"),
                            value = roundedDaysValue(state.history.extendedLifeMinutes).toString(),
                            unit = AppText.t("home_day_unit")
                        )
                    }
                }
            }

            // Right Card (ENCOURAGE)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(32.dp))
                    .background(themeColors.encourage)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppText.t("home_encouragement_panel"),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color.White
                        )
                    }

                    // Today Metric
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = state.encourage.pointsMultiplierLabel?.let {
                                AppText.t("home_earned_today_with_badge", it)
                            } ?: AppText.t("home_earned_today"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.White.copy(alpha = 0.70f)
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = AppText.t("group_points"),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White.copy(alpha = 0.80f),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = formatHomePointWholeValue(state.encourage.todayEarnedPoints),
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.sp
                                ),
                                color = Color.White
                            )
                        }
                    }

                    // Progress Pills
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HomeOverviewSplitPill(text = AppText.t("home_encouragement_progress_value", state.encourage.completedGroups, state.encourage.totalGroups))
                        HomeOverviewSplitPill(text = state.encourage.streakLabel)
                    }

                    // Divider
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.15f),
                        thickness = 0.5.dp
                    )

                    // Stacked history complications
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        HomeHistoryMetricSplit(
                            label = AppText.t("home_total_earned"),
                            value = formatHomePointValue(state.history.totalEarnedPoints),
                            unit = AppText.t("group_points"),
                            alignEnd = true,
                            unitBefore = true
                        )
                        HomeHistoryMetricSplit(
                            label = AppText.t("home_current_remaining"),
                            value = formatHomePointValue(state.history.currentPoints),
                            unit = AppText.t("group_points"),
                            alignEnd = true,
                            unitBefore = true
                        )
                    }
                }
            }
        }

        // Center White Clock Watermark
        HomeOverviewClockWatermark(
            modifier = Modifier
                .size(136.dp)
                .align(Alignment.Center)
                .offset(y = 4.dp),
        )
    }
}

@Composable
private fun HomeOverviewSplitPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    if (text.isBlank()) return
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.10f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White.copy(alpha = 0.90f),
            maxLines = 1
        )
    }
}

@Composable
private fun HomeHistoryMetricSplit(
    label: String,
    value: String,
    unit: String,
    alignEnd: Boolean = false,
    unitBefore: Boolean = false,
) {
    val horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    val textAlign = if (alignEnd) androidx.compose.ui.text.style.TextAlign.End else androidx.compose.ui.text.style.TextAlign.Start
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.2.sp
            ),
            color = Color.White.copy(alpha = 0.65f),
            textAlign = textAlign,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start
        ) {
            if (unitBefore) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 1.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = Color.White,
                    maxLines = 1
                )
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = Color.White,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 1.dp)
                )
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
        
        // Outer ring (alpha = 0.18f)
        drawCircle(
            color = Color.White.copy(alpha = 0.18f),
            radius = radius,
            center = center,
            style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round),
        )
        
        // Inner thin ring (alpha = 0.08f)
        drawCircle(
            color = Color.White.copy(alpha = 0.08f),
            radius = radius * 0.90f,
            center = center,
            style = Stroke(width = 0.8.dp.toPx()),
        )

        // Hands
        val minuteAngle = (time.minute / 60.0) * 2.0 * PI - PI / 2.0
        val hourAngle = (((time.hour % 12) + time.minute / 60.0) / 12.0) * 2.0 * PI - PI / 2.0
        
        // Hour hand
        drawLine(
            color = Color.White.copy(alpha = 0.40f),
            start = center,
            end = Offset(
                x = center.x + cos(hourAngle).toFloat() * radius * 0.48f,
                y = center.y + sin(hourAngle).toFloat() * radius * 0.48f,
            ),
            strokeWidth = 4.2.dp.toPx(),
            cap = StrokeCap.Round,
        )
        
        // Minute hand
        drawLine(
            color = Color.White.copy(alpha = 0.28f),
            start = center,
            end = Offset(
                x = center.x + cos(minuteAngle).toFloat() * radius * 0.68f,
                y = center.y + sin(minuteAngle).toFloat() * radius * 0.68f,
            ),
            strokeWidth = 2.6.dp.toPx(),
            cap = StrokeCap.Round,
        )
        
        // Center cap
        drawCircle(
            color = Color.White.copy(alpha = 0.50f),
            radius = 5.dp.toPx(),
            center = center,
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.70f),
            radius = 2.5.dp.toPx(),
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
    superModeStatus: SuperModeStatus,
    onOpenSuperModeInfo: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            softWrap = false,
            overflow = androidx.compose.ui.text.style.TextOverflow.Clip,
        )
        if (superModeStatus.isConfigured && superModeStatus.isEnabled) {
            Surface(
                modifier = Modifier.clickable(onClick = onOpenSuperModeInfo),
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = AppText.t("super_mode_title"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeBattleStation(
    actions: List<HomeBattleAction>,
    onActionClick: (HomeBattleAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (actions.isEmpty()) return
    TinyVowCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
        borderAlpha = 0.30f,
        shadowElevation = TinyVowElevation.FeaturedCard,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            actions.forEach { action ->
                HomeBattleActionTile(
                    action = action,
                    onClick = { onActionClick(action) },
                    modifier =
                        Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp),
                )
            }
        }
    }
}

@Composable
private fun HomeBattleActionTile(
    action: HomeBattleAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    val accent =
        when (action.type) {
            HomeBattleActionType.CONTROL -> themeColors.control
            HomeBattleActionType.ENCOURAGE -> themeColors.encourage
            HomeBattleActionType.REWARD -> MaterialTheme.colorScheme.primary
            HomeBattleActionType.CREATE -> MaterialTheme.colorScheme.primary
            HomeBattleActionType.PERMISSION_USAGE,
            HomeBattleActionType.PERMISSION_ACCESSIBILITY -> MaterialTheme.colorScheme.error
        }
    val icon =
        when (action.type) {
            HomeBattleActionType.CONTROL -> Icons.Default.Shield
            HomeBattleActionType.ENCOURAGE -> Icons.Default.Star
            HomeBattleActionType.REWARD -> Icons.Default.CardGiftcard
            HomeBattleActionType.CREATE -> Icons.Default.AddCircle
            HomeBattleActionType.PERMISSION_USAGE,
            HomeBattleActionType.PERMISSION_ACCESSIBILITY -> Icons.Default.VerifiedUser
        }

    Surface(
        color = Color.Transparent,
        modifier =
            modifier
                .clip(RoundedCornerShape(TinyVowRadius.Control))
                .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(14.dp),
                    )
                }
                Text(
                    text = action.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, lineHeight = 15.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.inkStrong,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }
            Text(
                text =
                    homeBattleHintText(
                        value = action.value,
                        subtitle = action.subtitle,
                        subtitleGroupName = action.subtitleGroupName,
                        valueColor = accent,
                        restColor = themeColors.inkMuted,
                    ),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.8.sp, lineHeight = 14.sp),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
        }
    }
}

private fun homeBattleHintText(
    value: String,
    subtitle: String,
    subtitleGroupName: String? = null,
    valueColor: Color,
    restColor: Color,
) = buildAnnotatedString {
    withStyle(SpanStyle(color = valueColor, fontWeight = FontWeight.SemiBold)) {
        append(value)
    }
    withStyle(SpanStyle(color = restColor)) {
        append(AppText.t("home_battle_action_hint_separator"))
    }
    val name = subtitleGroupName
    if (name != null && name.isNotEmpty()) {
        val idx = subtitle.indexOf(name)
        if (idx >= 0) {
            withStyle(SpanStyle(color = restColor)) { append(subtitle.substring(0, idx)) }
            withStyle(SpanStyle(color = valueColor, fontWeight = FontWeight.SemiBold)) { append(name) }
            withStyle(SpanStyle(color = restColor)) { append(subtitle.substring(idx + name.length)) }
        } else {
            withStyle(SpanStyle(color = restColor)) { append(subtitle) }
        }
    } else {
        withStyle(SpanStyle(color = restColor)) { append(subtitle) }
    }
}

@Composable
private fun HomeActiveEffectsCard(
    effects: List<ActiveRewardEffectEntity>,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    TinyVowCard(
        modifier = modifier,
        shape = RoundedCornerShape(TinyVowRadius.Card),
        borderAlpha = 0.30f,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CardHorizontal,
                vertical = TinyVowSpacing.CardVertical,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = AppText.t("redeem_effects_active_title"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.inkStrong,
                )
                Text(
                    text = AppText.t("redeem_effects_active_count", effects.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            effects.take(3).forEach { effect ->
                HomeActiveEffectRow(effect = effect)
            }
        }
    }
}

@Composable
private fun HomeActiveEffectRow(effect: ActiveRewardEffectEntity) {
    val payload = remember(effect.payloadJson) { parseRewardPayload(effect.payloadJson) }
    val target = effect.targetGroupNameSnapshot ?: AppText.t("generic_target_group")
    val title =
        when (effect.effectType) {
            RewardType.TIME_ADD -> AppText.t("redeem_effects_active_time_add", target, payload.minutes)
            RewardType.PERIOD_PASS -> AppText.t("redeem_effects_active_period_pass", target)
            RewardType.EMERGENCY_UNLOCK -> AppText.t("redeem_effects_active_emergency_unlock", target, payload.minutes)
            RewardType.DOUBLE_POINTS_DAY -> AppText.t("redeem_effects_active_double_points", target, trimHomeMultiplier(payload.pointsMultiplier))
            RewardType.STREAK_SHIELD -> AppText.t("redeem_effects_active_streak_shield")
            RewardType.CUSTOM -> AppText.t("redeem_rule_keep_in_inventory")
        }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = LocalThemeColors.current.ink,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
        Text(
            text = AppText.t("redeem_effects_remaining", formatHomeEffectDuration(effect.expireAt - System.currentTimeMillis())),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatHomeEffectDuration(durationMillis: Long): String {
    val totalMinutes = durationMillis.coerceAtLeast(0L) / 60_000L
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> AppText.t("duration_value_h_value_min", hours, minutes)
        hours > 0 -> AppText.t("duration_value_h", hours)
        else -> AppText.t("duration_value_min", minutes)
    }
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
        shape = RoundedCornerShape(TinyVowRadius.ItemCard),
        color = containerColor,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CompactCardHorizontal,
                vertical = TinyVowSpacing.CompactCardVertical,
            ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
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

private fun homeStreakLabel(
    archivedStreak: Int,
    todayCompleted: Boolean,
): String {
    return when {
        todayCompleted -> AppText.t("home_streak_value", archivedStreak + 1)
        archivedStreak > 0 -> AppText.t("home_streak_pending_value", archivedStreak, archivedStreak + 1)
        else -> AppText.t("home_streak_value", archivedStreak)
    }
}

private fun trimHomeMultiplier(value: Double): String =
    if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        java.lang.String.format(java.util.Locale.getDefault(), "%.1f", value)
            .trimEnd('0')
            .trimEnd('.')
    }

private const val HOME_SURPRISE_COUNT = 100

private fun homeSurpriseKeyForDate(date: LocalDate): String {
    val index = (((date.toEpochDay() * 37L) + 17L).floorMod(HOME_SURPRISE_COUNT.toLong()) + 1L).toInt()
    return "home_surprise_%03d".format(java.util.Locale.US, index)
}

private fun Long.floorMod(modulus: Long): Long = ((this % modulus) + modulus) % modulus

private const val HOME_CONTROL_SCORE_TOTAL = 60f
private const val HOME_ENCOURAGE_SCORE_TOTAL = 40f

private fun homeOverviewScore(state: HomeOverviewUiState): Int {
    if (state.behaviorScoreMetrics.isNotEmpty()) {
        return state.behaviorScoreMetrics.map { it.score }.average().roundToInt().coerceIn(0, 100)
    }
    val controlPoints = if (state.control.totalGroups > 0) {
        state.control.scoreRatio.coerceAtLeast(0f) * HOME_CONTROL_SCORE_TOTAL
    } else {
        0f
    }
    val encouragePoints = if (state.encourage.totalGroups > 0) {
        state.encourage.scoreRatio.coerceAtLeast(0f) * HOME_ENCOURAGE_SCORE_TOTAL
    } else {
        0f
    }
    return (controlPoints + encouragePoints).roundToInt().coerceIn(0, 100)
}

private fun homeOverviewScoreMetrics(
    state: HomeOverviewUiState,
): List<DailyBehaviorScoreMetric> =
    state.behaviorScoreMetrics.ifEmpty {
        listOf(
            DailyBehaviorScoreMetric(
                label = AppText.t("stats_score_kept_vow"),
                score = (state.control.scoreRatio.coerceIn(0f, 1f) * 100f).roundToInt(),
                detail = AppText.t("stats_score_value", (state.control.scoreRatio.coerceIn(0f, 1f) * 100f).roundToInt()),
                accentIndex = 0,
            ),
            DailyBehaviorScoreMetric(
                label = AppText.t("stats_score_gains"),
                score = (state.encourage.scoreRatio.coerceIn(0f, 1f) * 100f).roundToInt(),
                detail = AppText.t("stats_score_value", (state.encourage.scoreRatio.coerceIn(0f, 1f) * 100f).roundToInt()),
                accentIndex = 1,
            ),
            DailyBehaviorScoreMetric(label = AppText.t("stats_score_focus"), score = 60, detail = AppText.t("stats_score_value", 60), accentIndex = 2),
            DailyBehaviorScoreMetric(label = AppText.t("stats_score_rhythm"), score = 60, detail = AppText.t("stats_score_value", 60), accentIndex = 3),
            DailyBehaviorScoreMetric(label = AppText.t("stats_score_restraint"), score = 60, detail = AppText.t("stats_score_value", 60), accentIndex = 4),
        )
    }

private fun buildRealtimeHomeBehaviorScoreMetrics(
    controlGroups: List<AppGroupWithApps>,
    encourageGroups: List<AppGroupWithApps>,
    periodUsageMap: Map<String, Long>,
    activeRewardEffects: List<ActiveRewardEffectEntity>,
    analysis: BehaviorScoreAnalysis,
    yesterdayGroupArchives: List<DailyGroupArchiveEntity>,
    yesterdayAppArchives: List<DailyAppArchiveEntity>,
): List<DailyBehaviorScoreMetric> {
    val yesterdaySnapshots = mergeArchivedAppSnapshots(yesterdayAppArchives)
    val hasYesterdayData = yesterdayGroupArchives.isNotEmpty() || yesterdayAppArchives.isNotEmpty()
    val yesterdayControlPackageNames =
        yesterdayAppArchives
            .filter { it.groupType == GroupType.CONTROL }
            .mapTo(linkedSetOf()) { it.packageName }
    val yesterdayEncouragePackageNames =
        yesterdayAppArchives
            .filter { it.groupType == GroupType.ENCOURAGE }
            .mapTo(linkedSetOf()) { it.packageName }
    val yesterdayAnalysis =
        analyzeBehaviorScores(
            buildArchivedBehaviorScoreInputs(
                items = yesterdaySnapshots,
                groupArchives = yesterdayGroupArchives,
                controlPackageNames = yesterdayControlPackageNames,
                encouragePackageNames = yesterdayEncouragePackageNames,
            ),
        )
    val yesterdayControlByName =
        yesterdayGroupArchives
            .filter { it.groupType == GroupType.CONTROL }
            .associateBy { it.groupName }
    val yesterdayEncourageByName =
        yesterdayGroupArchives
            .filter { it.groupType == GroupType.ENCOURAGE }
            .associateBy { it.groupName }
    return listOf(
        DailyBehaviorScoreMetric(
            label = AppText.t("stats_score_kept_vow"),
            score = analysis.breakdown.guardScore,
            detail = AppText.t("stats_score_value", analysis.breakdown.guardScore),
            accentIndex = 0,
            explanation =
                BehaviorScoreMetricDetail(
                    title = AppText.t("stats_score_kept_vow"),
                    score = analysis.breakdown.guardScore,
                    formulaLines =
                        listOf(
                            AppText.t("stats_score_metric_guard_formula_1"),
                            AppText.t("stats_score_metric_guard_formula_2"),
                        ),
                    comparisonRows =
                        buildList {
                            add(
                                BehaviorScoreMetricComparisonRow(
                                    label = AppText.t("stats_score_metric_completion_groups_label"),
                                    todayValue = "${analysis.guard.completedGroups} / ${analysis.guard.totalGroups}",
                                    yesterdayValue =
                                        if (hasYesterdayData) {
                                            buildCompletedGroupsValue(
                                                yesterdayAnalysis.guard.completedGroups,
                                                yesterdayAnalysis.guard.totalGroups,
                                            )
                                        } else {
                                            AppText.t("stats_score_metric_empty_value")
                                        },
                                ),
                            )
                            add(
                                BehaviorScoreMetricComparisonRow(
                                    label = AppText.t("stats_score_metric_remaining_limit_label"),
                                    todayValue = buildUsageSlashValue(analysis.guard.remainingMillis, analysis.guard.totalLimitMillis),
                                    yesterdayValue =
                                        if (hasYesterdayData) {
                                            buildUsageSlashValue(
                                                yesterdayAnalysis.guard.remainingMillis,
                                                yesterdayAnalysis.guard.totalLimitMillis,
                                            )
                                        } else {
                                            AppText.t("stats_score_metric_empty_value")
                                        },
                                ),
                            )
                            controlGroups.forEach { group ->
                                val usedMillis = periodUsageMap[group.group.id] ?: 0L
                                add(
                                    BehaviorScoreMetricComparisonRow(
                                        label = group.group.name,
                                        todayValue =
                                            buildUsageSlashValue(
                                                usedMillis,
                                                homeEffectiveControlLimitMillis(
                                                    activeRewardEffects,
                                                    group.group.id,
                                                    group.group.limitMinutes,
                                                ),
                                            ),
                                        yesterdayValue =
                                            if (!hasYesterdayData) {
                                                AppText.t("stats_score_metric_empty_value")
                                            } else {
                                                yesterdayControlByName[group.group.name]?.let {
                                                    buildUsageSlashValue(
                                                        it.periodUsageMillisAtClose,
                                                        it.effectiveLimitMillisAtClose,
                                                    )
                                                } ?: AppText.t("stats_score_metric_empty_value")
                                            },
                                    ),
                                )
                            }
                        },
                ),
        ),
        DailyBehaviorScoreMetric(
            label = AppText.t("stats_score_gains"),
            score = analysis.breakdown.gainScore,
            detail = AppText.t("stats_score_value", analysis.breakdown.gainScore),
            accentIndex = 1,
            explanation =
                BehaviorScoreMetricDetail(
                    title = AppText.t("stats_score_gains"),
                    score = analysis.breakdown.gainScore,
                    formulaLines =
                        listOf(
                            AppText.t("stats_score_metric_gain_formula_1"),
                            AppText.t("stats_score_metric_gain_formula_2"),
                        ),
                    comparisonRows =
                        buildList {
                            add(
                                BehaviorScoreMetricComparisonRow(
                                    label = AppText.t("stats_score_metric_completion_groups_label"),
                                    todayValue = "${analysis.gain.completedGroups} / ${analysis.gain.totalGroups}",
                                    yesterdayValue =
                                        if (hasYesterdayData) {
                                            buildCompletedGroupsValue(
                                                yesterdayAnalysis.gain.completedGroups,
                                                yesterdayAnalysis.gain.totalGroups,
                                            )
                                        } else {
                                            AppText.t("stats_score_metric_empty_value")
                                        },
                                ),
                            )
                            encourageGroups.forEachIndexed { index, group ->
                                val groupAnalysis = analysis.gain.groups.getOrNull(index) ?: return@forEachIndexed
                                add(
                                    BehaviorScoreMetricComparisonRow(
                                        label = group.group.name,
                                        todayValue =
                                            buildProgressSlashValue(
                                                groupAnalysis.usedMillis,
                                                groupAnalysis.targetMillis,
                                                groupAnalysis.progress,
                                            ),
                                        yesterdayValue =
                                            if (!hasYesterdayData) {
                                                AppText.t("stats_score_metric_empty_value")
                                            } else {
                                                yesterdayEncourageByName[group.group.name]?.let {
                                                    buildProgressSlashValue(
                                                        it.periodUsageMillisAtClose,
                                                        it.effectiveLimitMillisAtClose.coerceAtLeast(1L),
                                                        it.periodUsageMillisAtClose.toFloat() /
                                                            it.effectiveLimitMillisAtClose.coerceAtLeast(1L).toFloat(),
                                                    )
                                                } ?: AppText.t("stats_score_metric_empty_value")
                                            },
                                    ),
                                )
                            }
                        },
                ),
        ),
        DailyBehaviorScoreMetric(
            label = AppText.t("stats_score_focus"),
            score = analysis.breakdown.focusScore,
            detail = AppText.t("stats_score_value", analysis.breakdown.focusScore),
            accentIndex = 2,
            explanation =
                buildHomeRatioScoreDetail(
                    title = AppText.t("stats_score_focus"),
                    score = analysis.breakdown.focusScore,
                    formula = AppText.t("stats_score_metric_focus_formula"),
                    numeratorLabel = AppText.t("stats_score_metric_encourage_usage_label"),
                    numeratorToday = formatDuration(analysis.focus.numerator),
                    numeratorYesterday = if (hasYesterdayData) formatDuration(yesterdayAnalysis.focus.numerator) else AppText.t("stats_score_metric_empty_value"),
                    denominatorLabel = AppText.t("stats_score_metric_control_usage_label"),
                    denominatorToday = formatDuration(analysis.focus.denominator),
                    denominatorYesterday = if (hasYesterdayData) formatDuration(yesterdayAnalysis.focus.denominator) else AppText.t("stats_score_metric_empty_value"),
                    ratioToday = analysis.focus.ratio,
                    ratioYesterday = yesterdayAnalysis.focus.ratio.takeIf { hasYesterdayData },
                ),
        ),
        DailyBehaviorScoreMetric(
            label = AppText.t("stats_score_rhythm"),
            score = analysis.breakdown.rhythmScore,
            detail = AppText.t("stats_score_value", analysis.breakdown.rhythmScore),
            accentIndex = 3,
            explanation =
                BehaviorScoreMetricDetail(
                    title = AppText.t("stats_score_rhythm"),
                    score = analysis.breakdown.rhythmScore,
                    formulaLines = listOf(AppText.t("stats_score_metric_rhythm_formula")),
                    comparisonRows =
                        listOf(
                            BehaviorScoreMetricComparisonRow(
                                label = AppText.t("stats_score_metric_night_outside_label"),
                                todayValue = formatDuration(analysis.rhythm.nightOutsideEncourageMillis),
                                yesterdayValue =
                                    if (hasYesterdayData) {
                                        formatDuration(yesterdayAnalysis.rhythm.nightOutsideEncourageMillis)
                                    } else {
                                        AppText.t("stats_score_metric_empty_value")
                                    },
                            ),
                        ),
                ),
        ),
        DailyBehaviorScoreMetric(
            label = AppText.t("stats_score_restraint"),
            score = analysis.breakdown.restraintScore,
            detail = AppText.t("stats_score_value", analysis.breakdown.restraintScore),
            accentIndex = 4,
            explanation =
                buildHomeRatioScoreDetail(
                    title = AppText.t("stats_score_restraint"),
                    score = analysis.breakdown.restraintScore,
                    formula = AppText.t("stats_score_metric_restraint_formula"),
                    numeratorLabel = AppText.t("stats_score_metric_encourage_launches_label"),
                    numeratorToday = analysis.restraint.numerator.toString(),
                    numeratorYesterday = if (hasYesterdayData) yesterdayAnalysis.restraint.numerator.toString() else AppText.t("stats_score_metric_empty_value"),
                    denominatorLabel = AppText.t("stats_score_metric_control_launches_label"),
                    denominatorToday = analysis.restraint.denominator.toString(),
                    denominatorYesterday = if (hasYesterdayData) yesterdayAnalysis.restraint.denominator.toString() else AppText.t("stats_score_metric_empty_value"),
                    ratioToday = analysis.restraint.ratio,
                    ratioYesterday = yesterdayAnalysis.restraint.ratio.takeIf { hasYesterdayData },
                ),
        ),
    )
}

private fun buildHomeRatioScoreDetail(
    title: String,
    score: Int,
    formula: String,
    numeratorLabel: String,
    numeratorToday: String,
    numeratorYesterday: String,
    denominatorLabel: String,
    denominatorToday: String,
    denominatorYesterday: String,
    ratioToday: Float?,
    ratioYesterday: Float?,
): BehaviorScoreMetricDetail =
    BehaviorScoreMetricDetail(
        title = title,
        score = score,
        formulaLines = listOf(formula),
        comparisonRows =
            listOf(
                BehaviorScoreMetricComparisonRow(
                    label = numeratorLabel,
                    todayValue = numeratorToday,
                    yesterdayValue = numeratorYesterday,
                ),
                BehaviorScoreMetricComparisonRow(
                    label = denominatorLabel,
                    todayValue = denominatorToday,
                    yesterdayValue = denominatorYesterday,
                ),
                BehaviorScoreMetricComparisonRow(
                    label = AppText.t("stats_score_metric_ratio_label"),
                    todayValue = formatBehaviorRatioValue(ratioToday),
                    yesterdayValue = formatBehaviorRatioValue(ratioYesterday),
                ),
            ),
    )

private fun formatBehaviorMultiplier(value: Float): String {
    val rounded = kotlin.math.round(value * 10f) / 10f
    return if (rounded % 1f == 0f) {
        "${rounded.toInt()}x"
    } else {
        "${rounded}x"
    }
}

private fun buildCompletedGroupsValue(
    completed: Int,
    total: Int,
): String = "$completed / $total"

private fun buildUsageSlashValue(
    usedMillis: Long,
    limitMillis: Long,
): String = "${formatDuration(usedMillis)} / ${formatDuration(limitMillis)}"

private fun buildProgressSlashValue(
    usedMillis: Long,
    targetMillis: Long,
    ratio: Float,
): String = "${formatDuration(usedMillis)} / ${formatDuration(targetMillis)} (${formatBehaviorMultiplier(ratio)})"

private fun formatBehaviorRatioValue(ratio: Float?): String =
    ratio?.let(::formatBehaviorMultiplier) ?: AppText.t("stats_score_metric_empty_value")

private enum class HomeControlRiskLevel {
    HALF,
    NEAR_LIMIT,
    DEPLETED,
    OVER_LIMIT,
}

private data class HomeControlRisk(
    val group: AppGroupWithApps,
    val level: HomeControlRiskLevel,
    val usedMillis: Long,
    val effectiveLimitMillis: Long,
)

private data class HomeEncouragePromptCandidate(
    val group: AppGroupWithApps,
    val usedMillis: Long,
    val targetMillis: Long,
    val remainingMillis: Long,
    val earnablePoints: Double,
)

private fun buildHomeBattleActions(
    controlGroups: List<AppGroupWithApps>,
    encourageGroups: List<AppGroupWithApps>,
    usageMap: Map<String, Long>,
    activeRewardEffects: List<ActiveRewardEffectEntity>,
    recentGroupArchives: List<DailyGroupArchiveEntity>,
    achievementProgress: AchievementProgress,
): List<HomeBattleAction> {
    val anomalyAction =
        controlGroups
            .mapNotNull { group ->
                if (hasActivePeriodPass(activeRewardEffects, group.group.id)) return@mapNotNull null
                val usedMillis = usageMap[group.group.id] ?: 0L
                val effectiveLimitMillis =
                    homeEffectiveControlLimitMillis(
                        activeRewardEffects = activeRewardEffects,
                        groupId = group.group.id,
                        limitMinutes = group.group.limitMinutes,
                    )
                val level =
                    homeControlRiskLevel(
                        usedMillis = usedMillis,
                        effectiveLimitMillis = effectiveLimitMillis,
                    ) ?: return@mapNotNull null
                HomeControlRisk(
                    group = group,
                    level = level,
                    usedMillis = usedMillis,
                    effectiveLimitMillis = effectiveLimitMillis,
                )
            }
            .groupBy { it.level }
            .maxByOrNull { (level, _) -> level.ordinal }
            ?.let { (level, risks) ->
                val worstRisk =
                    risks.maxByOrNull { risk ->
                        risk.usedMillis.toDouble() / risk.effectiveLimitMillis.coerceAtLeast(1L).toDouble()
                    } ?: return@let null
                val groupNames = risks.joinToString(AppText.t("home_battle_group_name_separator")) { it.group.group.name }
                val detail =
                    when (level) {
                        HomeControlRiskLevel.HALF -> AppText.t("home_battle_control_groups_half", groupNames, achievementProgress.controlStreak + 1)
                        HomeControlRiskLevel.NEAR_LIMIT -> AppText.t("home_battle_control_groups_near_limit", groupNames, achievementProgress.controlStreak + 1)
                        HomeControlRiskLevel.DEPLETED -> AppText.t("home_battle_control_groups_depleted", groupNames, achievementProgress.controlStreak + 1)
                        HomeControlRiskLevel.OVER_LIMIT -> AppText.t("home_battle_control_groups_over_limit", groupNames)
                    }
                val title =
                    when (level) {
                        HomeControlRiskLevel.HALF -> AppText.t("home_battle_control_half_title")
                        HomeControlRiskLevel.NEAR_LIMIT -> AppText.t("home_battle_control_near_limit_title")
                        HomeControlRiskLevel.DEPLETED -> AppText.t("home_battle_control_depleted_title")
                        HomeControlRiskLevel.OVER_LIMIT -> AppText.t("home_battle_control_over_limit_title")
                    }
                val exceededMillis = (worstRisk.usedMillis - worstRisk.effectiveLimitMillis).coerceAtLeast(0L)
                val remainingMillis = (worstRisk.effectiveLimitMillis - worstRisk.usedMillis).coerceAtLeast(0L)
                val value =
                    if (level == HomeControlRiskLevel.OVER_LIMIT) {
                        AppText.t("home_battle_control_over_value", ceilHomeMinutes(exceededMillis))
                    } else {
                        AppText.t("home_battle_control_left_value", ceilHomeMinutes(remainingMillis))
                    }
                HomeBattleAction(
                    type = HomeBattleActionType.CONTROL,
                    title = title,
                    subtitle = detail,
                    subtitleGroupName = groupNames,
                    value = value,
                    progress =
                        (worstRisk.usedMillis.toFloat() / worstRisk.effectiveLimitMillis.coerceAtLeast(1L).toFloat())
                            .coerceIn(0f, 1f),
                    group = worstRisk.group,
                )
            }
            ?: controlGroups
                .takeIf { it.isNotEmpty() }
                ?.let {
                    HomeBattleAction(
                        type = HomeBattleActionType.CONTROL,
                        title = AppText.t("home_battle_control_steady_title"),
                        subtitle = AppText.t("home_battle_anomaly_clear_subtitle"),
                        value = AppText.t("home_battle_anomaly_clear_value"),
                        progress = 0f,
                    )
                }

    val zoneId = ZoneId.systemDefault()
    val todayStartMillis = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant().toEpochMilli()
    val encouragePromptCandidates =
        encourageGroups
            .mapNotNull { group ->
                val usedMillis = usageMap[group.group.id] ?: 0L
                val targetMillis = group.group.limitMinutes.coerceAtLeast(1) * 60_000L
                val remainingMillis = (targetMillis - usedMillis).coerceAtLeast(0L)
                if (remainingMillis <= 0L) return@mapNotNull null
                val pointsMultiplier = activeEncouragePointsMultiplier(activeRewardEffects, group.group.id)
                val usagePoints = remainingMillis / 60_000.0 * group.group.pointsPerMinute * pointsMultiplier
                val targetBonus =
                    if (group.group.lastBonusAt < todayStartMillis) {
                        group.group.limitMinutes.coerceAtLeast(0) * group.group.pointsPerMinute * pointsMultiplier
                    } else {
                        0.0
                    }
                HomeEncouragePromptCandidate(
                    group = group,
                    usedMillis = usedMillis,
                    targetMillis = targetMillis,
                    remainingMillis = remainingMillis,
                    earnablePoints = usagePoints + targetBonus,
                )
            }
    val shortcutAction =
        when {
            encourageGroups.isEmpty() -> null
            encouragePromptCandidates.isEmpty() ->
                HomeBattleAction(
                    type = HomeBattleActionType.ENCOURAGE,
                    title = AppText.t("home_battle_encourage_perfect_title"),
                    subtitle = AppText.t("home_battle_encourage_perfect_subtitle"),
                    value = AppText.t("home_battle_encourage_done_value"),
                    progress = 1f,
                )
            encouragePromptCandidates.size == encourageGroups.size -> {
                val candidate =
                    encouragePromptCandidates.minByOrNull { it.remainingMillis }
                        ?: error("Expected at least one unfinished encourage group")
                HomeBattleAction(
                    type = HomeBattleActionType.ENCOURAGE,
                    title = AppText.t("home_battle_shortcut_streak_title"),
                    subtitle = AppText.t("home_battle_shortcut_complete_group_subtitle", candidate.group.group.name, achievementProgress.encourageStreak + 1),
                    subtitleGroupName = candidate.group.group.name,
                    value = AppText.t("home_battle_encourage_left_value", ceilHomeMinutes(candidate.remainingMillis)),
                    progress = (candidate.usedMillis.toFloat() / candidate.targetMillis.toFloat()).coerceIn(0f, 1f),
                    group = candidate.group,
                )
            }
            else -> {
                val candidate =
                    encouragePromptCandidates.maxByOrNull { it.earnablePoints }
                        ?: error("Expected at least one unfinished encourage group")
                HomeBattleAction(
                    type = HomeBattleActionType.ENCOURAGE,
                    title = AppText.t("home_battle_shortcut_points_title"),
                    subtitle =
                        AppText.t(
                            "home_battle_shortcut_points_subtitle",
                            candidate.group.group.name,
                            formatHomePointWholeValue(candidate.earnablePoints),
                        ),
                    subtitleGroupName = candidate.group.group.name,
                    value = AppText.t("home_battle_encourage_left_value", ceilHomeMinutes(candidate.remainingMillis)),
                    progress = (candidate.usedMillis.toFloat() / candidate.targetMillis.toFloat()).coerceIn(0f, 1f),
                    group = candidate.group,
                )
            }
        }

    val createAction =
        if (controlGroups.isEmpty() && encourageGroups.isEmpty()) {
            HomeBattleAction(
                type = HomeBattleActionType.CREATE,
                title = AppText.t("home_battle_create_title"),
                subtitle = AppText.t("home_battle_create_subtitle"),
                value = AppText.t("home_battle_create_value"),
                progress = 0f,
            )
        } else {
            null
        }

    return listOfNotNull(createAction, anomalyAction, shortcutAction).take(2)
}

private fun homeControlRiskLevel(
    usedMillis: Long,
    effectiveLimitMillis: Long,
): HomeControlRiskLevel? {
    val limit = effectiveLimitMillis.coerceAtLeast(1L)
    val exceededMillis = usedMillis - limit
    return when {
        exceededMillis > HOME_CONTROL_TOLERANCE_MINUTES * 60_000L -> HomeControlRiskLevel.OVER_LIMIT
        usedMillis >= limit -> HomeControlRiskLevel.DEPLETED
        usedMillis.toDouble() / limit.toDouble() > 0.8 -> HomeControlRiskLevel.NEAR_LIMIT
        usedMillis.toDouble() / limit.toDouble() > 0.6 -> HomeControlRiskLevel.HALF
        else -> null
    }
}

private fun ceilHomeMinutes(millis: Long): Int =
    if (millis <= 0L) {
        0
    } else {
        ((millis + 59_999L) / 60_000L).toInt()
    }

private fun activeRewardExtraMinutes(
    activeRewardEffects: List<ActiveRewardEffectEntity>,
    groupId: String,
): Int =
    activeRewardEffects
        .filter {
            it.targetGroupId == groupId &&
                (it.effectType == RewardType.TIME_ADD || it.effectType == RewardType.EMERGENCY_UNLOCK)
        }
        .sumOf { parseRewardPayload(it.payloadJson).minutes }

private fun hasActivePeriodPass(
    activeRewardEffects: List<ActiveRewardEffectEntity>,
    groupId: String,
): Boolean =
    activeRewardEffects.any {
        it.targetGroupId == groupId &&
            it.effectType == RewardType.PERIOD_PASS
    }

private fun homeEffectiveControlLimitMillis(
    activeRewardEffects: List<ActiveRewardEffectEntity>,
    groupId: String,
    limitMinutes: Int,
): Long = (limitMinutes + activeRewardExtraMinutes(activeRewardEffects, groupId)).coerceAtLeast(1) * 60_000L

private fun activeEncouragePointsMultiplier(
    activeRewardEffects: List<ActiveRewardEffectEntity>,
    groupId: String,
): Double =
    activeRewardEffects
        .firstOrNull {
            it.targetGroupId == groupId &&
                it.effectType == RewardType.DOUBLE_POINTS_DAY
        }
        ?.let { parseRewardPayload(it.payloadJson).pointsMultiplier.coerceAtLeast(1.0) }
        ?: 1.0

private fun homeControlGroupCompleted(
    activeRewardEffects: List<ActiveRewardEffectEntity>,
    groupId: String,
    usedMillis: Long,
    limitMinutes: Int,
): Boolean =
    hasActivePeriodPass(activeRewardEffects, groupId) ||
        usedMillis <= homeEffectiveControlLimitMillis(activeRewardEffects, groupId, limitMinutes)

private fun calculateRealtimeNightOutsideEncourageMillis(
    sessions: List<AppSession>,
    encouragePackageNames: Set<String>,
): Long {
    if (sessions.isEmpty()) return 0L

    val zoneId = ZoneId.systemDefault()
    return sessions.sumOf { session ->
        if (session.packageName in encouragePackageNames) {
            0L
        } else {
            var total = 0L
            var cursor = session.startTime
            val end = max(session.startTime, session.endTime)
            while (cursor < end) {
                val dateTime = java.time.Instant.ofEpochMilli(cursor).atZone(zoneId)
                val nextHour =
                    dateTime
                        .truncatedTo(java.time.temporal.ChronoUnit.HOURS)
                        .plusHours(1)
                        .toInstant()
                        .toEpochMilli()
                val sliceEnd = min(end, nextHour)
                if (dateTime.hour >= 22 || dateTime.hour < 4) {
                    total += (sliceEnd - cursor).coerceAtLeast(0L)
                }
                cursor = sliceEnd
            }
            total
        }
    }
}

private fun buildHomeBehaviorScoreInputs(
    controlGroups: List<AppGroupWithApps>,
    encourageGroups: List<AppGroupWithApps>,
    periodUsageMap: Map<String, Long>,
    todayAppUsageMap: Map<String, Long>,
    todayAppOpenCountMap: Map<String, Int>,
    todaySessions: List<AppSession>,
    activeRewardEffects: List<ActiveRewardEffectEntity>,
): BehaviorScoreInputs {
    val controlPackageNames = controlGroups.flatMapTo(linkedSetOf()) { it.packageNames }
    val encouragePackageNames = encourageGroups.flatMapTo(linkedSetOf()) { it.packageNames }

    return BehaviorScoreInputs(
        controlGroups =
            controlGroups.map { group ->
                val usedMillis = periodUsageMap[group.group.id] ?: 0L
                BehaviorControlScoreInput(
                    usedMillis = usedMillis,
                    effectiveLimitMillis = homeEffectiveControlLimitMillis(activeRewardEffects, group.group.id, group.group.limitMinutes),
                    completed = homeControlGroupCompleted(activeRewardEffects, group.group.id, usedMillis, group.group.limitMinutes),
                )
            },
        encourageGroups =
            encourageGroups.map { group ->
                val targetMillis = group.group.limitMinutes.coerceAtLeast(1) * 60_000L
                val usedMillis = periodUsageMap[group.group.id] ?: 0L
                BehaviorEncourageScoreInput(
                    usedMillis = usedMillis,
                    targetMillis = targetMillis,
                    completed = usedMillis >= targetMillis,
                )
            },
        packageStats = buildHomeBehaviorPackageStats(todayAppUsageMap, todayAppOpenCountMap),
        controlPackageNames = controlPackageNames,
        encouragePackageNames = encouragePackageNames,
        nightOutsideEncourageMillis = calculateRealtimeNightOutsideEncourageMillis(todaySessions, encouragePackageNames),
    )
}

private fun buildHomeBehaviorPackageStats(
    todayAppUsageMap: Map<String, Long>,
    todayAppOpenCountMap: Map<String, Int>,
): List<BehaviorPackageScoreInput> =
    (todayAppUsageMap.keys + todayAppOpenCountMap.keys)
        .distinct()
        .map { packageName ->
            BehaviorPackageScoreInput(
                packageName = packageName,
                usageMillis = todayAppUsageMap[packageName] ?: 0L,
                openCount = todayAppOpenCountMap[packageName] ?: 0,
            )
        }

private fun buildHomeOverviewUiState(
    context: android.content.Context,
    groupsWithApps: List<AppGroupWithApps>,
    usageMap: Map<String, Long>,
    periodUsageMap: Map<String, Long>,
    todayAppUsageMap: Map<String, Long>,
    todayAppOpenCountMap: Map<String, Int>,
    todaySessions: List<AppSession>,
    activeRewardEffects: List<ActiveRewardEffectEntity>,
    recentGroupArchives: List<DailyGroupArchiveEntity>,
    yesterdayGroupArchives: List<DailyGroupArchiveEntity>,
    yesterdayAppArchives: List<DailyAppArchiveEntity>,
    historicalArchives: List<com.rrrrz.tinyvow.data.db.DailyArchiveEntity>,
    userPoints: Double,
    todayPoints: Double,
    achievementProgress: AchievementProgress,
): HomeOverviewUiState {
    val controlGroups = groupsWithApps.filter { it.group.type == GroupType.CONTROL }
    val encourageGroups = groupsWithApps.filter { it.group.type == GroupType.ENCOURAGE }
    val controlPeriodUsageMinutesByGroup =
        controlGroups.associate { group ->
            group.group.id to ((periodUsageMap[group.group.id] ?: 0L) / 60_000L).toInt()
        }
    val encouragePeriodUsageMinutesByGroup =
        encourageGroups.associate { group ->
            group.group.id to ((periodUsageMap[group.group.id] ?: 0L) / 60_000L).toInt()
        }
    val controlCompletedGroups =
        controlGroups.count { group ->
            homeControlGroupCompleted(
                activeRewardEffects = activeRewardEffects,
                groupId = group.group.id,
                usedMillis = periodUsageMap[group.group.id] ?: 0L,
                limitMinutes = group.group.limitMinutes,
            )
        }
    val encourageCompletedGroups =
        encourageGroups.count { group ->
            val usageMinutes = encouragePeriodUsageMinutesByGroup[group.group.id] ?: 0
            usageMinutes >= group.group.limitMinutes
        }
    val behaviorScoreInputs =
        buildHomeBehaviorScoreInputs(
            controlGroups = controlGroups,
            encourageGroups = encourageGroups,
            periodUsageMap = periodUsageMap,
            todayAppUsageMap = todayAppUsageMap,
            todayAppOpenCountMap = todayAppOpenCountMap,
            todaySessions = todaySessions,
            activeRewardEffects = activeRewardEffects,
        )
    val behaviorScoreAnalysis = analyzeBehaviorScores(behaviorScoreInputs)
    val behaviorScoreBreakdown = behaviorScoreAnalysis.breakdown
    val controlScoreRatio = if (controlGroups.isNotEmpty()) behaviorScoreBreakdown.guardScore / 100f else 0f
    val encourageScoreRatio = if (encourageGroups.isNotEmpty()) behaviorScoreBreakdown.gainScore / 100f else 0f
    val controlTodaySavedMinutes =
        controlGroups.sumOf { group ->
            val todayUsageMinutes = ((usageMap[group.group.id] ?: 0L) / 60_000L).toInt()
            val effectiveLimitMinutes = (homeEffectiveControlLimitMillis(activeRewardEffects, group.group.id, group.group.limitMinutes) / 60_000L).toInt()
            (effectiveLimitMinutes - todayUsageMinutes).coerceAtLeast(0)
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
    val encouragePointsMultiplier =
        activeRewardEffects
            .filter { it.effectType == RewardType.DOUBLE_POINTS_DAY && it.targetGroupType == GroupType.ENCOURAGE }
            .maxOfOrNull { parseRewardPayload(it.payloadJson).pointsMultiplier.coerceAtLeast(1.0) }
            ?: 1.0
    val encouragePointsMultiplierLabel =
        if (encouragePointsMultiplier > 1.0) {
            AppText.t("home_points_multiplier_badge", trimHomeMultiplier(encouragePointsMultiplier))
        } else {
            null
        }
    val totalSavedMinutes = historicalArchives.sumOf { it.savedMillis } / 60_000L
    val extendedLifeMinutes = totalSavedMinutes * 3L
    val totalEarnedPoints = historicalArchives.sumOf { it.pointsEarned } + todayPoints
    val behaviorScoreMetrics =
        buildRealtimeHomeBehaviorScoreMetrics(
            controlGroups = controlGroups,
            encourageGroups = encourageGroups,
            periodUsageMap = periodUsageMap,
            activeRewardEffects = activeRewardEffects,
            analysis = behaviorScoreAnalysis,
            yesterdayGroupArchives = yesterdayGroupArchives,
            yesterdayAppArchives = yesterdayAppArchives,
        )
    val behaviorComparisonMetrics =
        buildDailyBehaviorScoreMetrics(
            items = mergeArchivedAppSnapshots(yesterdayAppArchives),
            groupArchives = yesterdayGroupArchives,
            controlPackageNames =
                yesterdayAppArchives
                    .filter { it.groupType == GroupType.CONTROL }
                    .mapTo(linkedSetOf()) { it.packageName },
            encouragePackageNames =
                yesterdayAppArchives
                    .filter { it.groupType == GroupType.ENCOURAGE }
                    .mapTo(linkedSetOf()) { it.packageName },
        )
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
                scoreRatio = controlScoreRatio,
                streakDays = achievementProgress.controlStreak,
                streakLabel =
                    homeStreakLabel(
                        archivedStreak = achievementProgress.controlStreak,
                        todayCompleted = controlGroups.isNotEmpty() && controlCompletedGroups >= controlGroups.size,
                    ),
            ),
        encourage =
            HomeEncourageOverviewUiState(
                todayEarnedPoints = encourageTodayEarnedPoints,
                completedGroups = encourageCompletedGroups,
                totalGroups = encourageGroups.size,
                scoreRatio = encourageScoreRatio,
                streakDays = achievementProgress.encourageStreak,
                streakLabel =
                    homeStreakLabel(
                        archivedStreak = achievementProgress.encourageStreak,
                        todayCompleted = encourageCompletedGroups > 0,
                    ),
                pointsMultiplierLabel = encouragePointsMultiplierLabel,
            ),
        history =
            HomeHistoryOverviewUiState(
                totalSavedMinutes = totalSavedMinutes,
                extendedLifeMinutes = extendedLifeMinutes,
                totalEarnedPoints = totalEarnedPoints,
                currentPoints = userPoints,
            ),
        behaviorScoreMetrics = behaviorScoreMetrics,
        behaviorComparisonMetrics = behaviorComparisonMetrics,
        battleActions =
            buildHomeBattleActions(
                controlGroups = controlGroups,
                encourageGroups = encourageGroups,
                usageMap = periodUsageMap,
                activeRewardEffects = activeRewardEffects,
                recentGroupArchives = recentGroupArchives,
                achievementProgress = achievementProgress,
            ),
    )
}

private fun buildRuntimeDiagnostics(
    generatedAtMillis: Long,
    usageAccessGranted: Boolean,
    accessibilityServiceEnabled: Boolean,
    accessibilityHeartbeatHealthy: Boolean,
    lastAccessibilityHeartbeatAtMillis: Long?,
    notificationPermissionGranted: Boolean,
    isIgnoringBattery: Boolean,
    groupsWithApps: List<AppGroupWithApps>,
    archiveCount: Int,
    latestArchiveDate: String?,
    proEntitlement: ProEntitlementState,
    superModeStatus: SuperModeStatus,
): RuntimeDiagnostics {
    val controlCount = groupsWithApps.count { it.group.type == GroupType.CONTROL }
    val encourageCount = groupsWithApps.count { it.group.type == GroupType.ENCOURAGE }
    val generatedAt =
        java.text.DateFormat
            .getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT)
            .format(java.util.Date(generatedAtMillis))
    val archiveValue =
        if (archiveCount > 0 && latestArchiveDate != null) {
            AppText.t("diagnostics_archive_count_latest", archiveCount, latestArchiveDate)
        } else {
            AppText.t("diagnostics_archive_count_empty")
        }
    val proValue = proDiagnosticValue(proEntitlement)
    val superModeValue =
        when {
            !superModeStatus.isConfigured -> AppText.t("super_mode_not_configured")
            !superModeStatus.isEnabled -> AppText.t("super_mode_disabled_status")
            superModeStatus.isActive -> AppText.t("super_mode_active_status", formatDiagnosticsRemaining(superModeStatus.remainingMillis))
            superModeStatus.isAvailableNow -> AppText.t("super_mode_ready_status")
            else -> AppText.t("super_mode_locked_until_status")
        }
    return RuntimeDiagnostics(
        generatedAt = generatedAt,
        items =
            listOf(
                RuntimeDiagnosticItem(
                    label = AppText.t("diagnostics_label_app_version"),
                    value = AppText.t("diagnostics_version_value", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
                ),
                RuntimeDiagnosticItem(
                    label = AppText.t("diagnostics_label_channel"),
                    value = BuildConfig.STORE_CHANNEL,
                ),
                RuntimeDiagnosticItem(
                    label = AppText.t("diagnostics_label_usage_access"),
                    value = permissionStatusText(usageAccessGranted),
                    isHealthy = usageAccessGranted,
                ),
                RuntimeDiagnosticItem(
                    label = AppText.t("diagnostics_label_accessibility"),
                    value = enabledStatusText(accessibilityServiceEnabled),
                    isHealthy = accessibilityServiceEnabled,
                ),
                RuntimeDiagnosticItem(
                    label = AppText.t("diagnostics_label_accessibility_heartbeat"),
                    value = accessibilityHeartbeatStatusText(generatedAtMillis, lastAccessibilityHeartbeatAtMillis),
                    isHealthy = accessibilityHeartbeatHealthy,
                ),
                RuntimeDiagnosticItem(
                    label = AppText.t("diagnostics_label_notifications"),
                    value = permissionStatusText(notificationPermissionGranted),
                    isHealthy = notificationPermissionGranted,
                ),
                RuntimeDiagnosticItem(
                    label = AppText.t("diagnostics_label_battery"),
                    value = if (isIgnoringBattery) AppText.t("diagnostics_battery_unrestricted") else AppText.t("diagnostics_battery_restricted"),
                    isHealthy = isIgnoringBattery,
                ),
                RuntimeDiagnosticItem(
                    label = AppText.t("diagnostics_label_groups"),
                    value = AppText.t("diagnostics_group_count_value", controlCount, encourageCount),
                ),
                RuntimeDiagnosticItem(
                    label = AppText.t("diagnostics_label_archives"),
                    value = archiveValue,
                ),
                RuntimeDiagnosticItem(
                    label = AppText.t("diagnostics_label_pro"),
                    value = proValue,
                    isHealthy = proEntitlement.status != ProEntitlementStatus.UNAVAILABLE,
                ),
                RuntimeDiagnosticItem(
                    label = AppText.t("diagnostics_label_super_mode"),
                    value = superModeValue,
                ),
            ),
    )
}

private fun permissionStatusText(granted: Boolean): String =
    if (granted) AppText.t("diagnostics_status_granted") else AppText.t("diagnostics_status_denied")

private fun enabledStatusText(enabled: Boolean): String =
    if (enabled) AppText.t("diagnostics_status_enabled") else AppText.t("diagnostics_status_disabled")

private fun accessibilityHeartbeatStatusText(nowMillis: Long, lastHeartbeatAtMillis: Long?): String {
    if (lastHeartbeatAtMillis == null) {
        return AppText.t("diagnostics_accessibility_heartbeat_never")
    }
    val ageMinutes = ((nowMillis - lastHeartbeatAtMillis).coerceAtLeast(0L) + 59_999L) / 60_000L
    return if (ageMinutes <= 1L) {
        AppText.t("diagnostics_accessibility_heartbeat_recent")
    } else {
        AppText.t("diagnostics_accessibility_heartbeat_minutes_ago", ageMinutes)
    }
}

private fun formatDiagnosticsRemaining(millis: Long): String {
    val minutes = ((millis + 59_999L) / 60_000L).coerceAtLeast(1L).toInt()
    return AppText.t("super_mode_minutes_value", minutes)
}

private fun proDiagnosticValue(entitlement: ProEntitlementState): String {
    val sourceLabel =
        when (entitlement.source) {
            "google_play" -> AppText.t("diagnostics_pro_source_google_play")
            "local_activation" -> AppText.t("diagnostics_pro_source_local_activation")
            "debug_lab" -> AppText.t("diagnostics_pro_source_debug_lab")
            null, "" -> AppText.t("diagnostics_pro_source_none")
            else -> entitlement.source
        }
    val expiresAt =
        entitlement.expiresAtMillis?.let {
            java.text.DateFormat.getDateInstance().format(java.util.Date(it))
        }
    return when (entitlement.status) {
        ProEntitlementStatus.ACTIVE ->
            if (expiresAt == null) {
                AppText.t("diagnostics_pro_active_source", sourceLabel)
            } else {
                AppText.t("diagnostics_pro_active_source_until", sourceLabel, expiresAt)
            }
        ProEntitlementStatus.PENDING -> AppText.t("diagnostics_pro_pending")
        ProEntitlementStatus.UNAVAILABLE -> AppText.t("diagnostics_pro_unavailable")
        ProEntitlementStatus.FREE -> AppText.t("diagnostics_pro_free")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiagnosticSettingsPage(
    usageAccessGranted: Boolean,
    accessibilityServiceEnabled: Boolean,
    isAutoStartDismissed: Boolean,
    isIgnoringBattery: Boolean,
    notificationPermissionGranted: Boolean,
    runtimeDiagnostics: RuntimeDiagnostics,
    statusColor: androidx.compose.ui.graphics.Color,
    onBack: () -> Unit,
    onOpenUsageAccessSettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenAutoStartSettings: () -> Unit,
    onSetAutoStartDismissed: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val context = LocalContext.current
    BackHandler(onBack = onBack)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.action_diagnostic_settings),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.inkStrong,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = AppText.t("group_back"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = TinyVowSpacing.PageHorizontal,
                    end = TinyVowSpacing.PageHorizontal,
                    top = TinyVowSpacing.PageTop,
                    bottom = TinyVowSpacing.PageTop,
                ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.SectionGap),
        ) {
            TinyVowCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
                color = MaterialTheme.colorScheme.primaryContainer,
                borderAlpha = 0.18f,
            ) {
                Text(
                    text = stringResource(R.string.settings_menu_subtitle),
                    modifier = Modifier.padding(
                        horizontal = TinyVowSpacing.CardHorizontal,
                        vertical = TinyVowSpacing.CardVertical,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = themeColors.ink.copy(alpha = 0.78f),
                )
            }
            RuntimeDiagnosticsCard(
                diagnostics = runtimeDiagnostics,
                onCopy = {
                    val title = AppText.t("diagnostics_runtime_summary")
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(
                        ClipData.newPlainText(
                            title,
                            runtimeDiagnostics.asPlainText(title),
                        ),
                    )
                    android.widget.Toast
                        .makeText(context, AppText.t("diagnostics_copied"), android.widget.Toast.LENGTH_SHORT)
                        .show()
                },
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
        }
    }
}

@Composable
private fun RuntimeDiagnosticsCard(
    diagnostics: RuntimeDiagnostics,
    onCopy: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    TinyVowCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.Card),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CardHorizontal,
                vertical = TinyVowSpacing.CardVertical,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = AppText.t("diagnostics_runtime_summary"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.inkStrong,
                    )
                    Text(
                        text = AppText.t("diagnostics_generated_at_value", diagnostics.generatedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.inkMuted,
                    )
                }
                TextButton(onClick = onCopy) {
                    Text(AppText.t("diagnostics_copy"))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                diagnostics.items.forEach { item ->
                    RuntimeDiagnosticRow(item)
                }
            }
        }
    }
}

@Composable
private fun RuntimeDiagnosticRow(item: RuntimeDiagnosticItem) {
    val valueColor =
        when (item.isHealthy) {
            true -> LocalThemeColors.current.encourage
            false -> MaterialTheme.colorScheme.error
            null -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = item.value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
        )
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

    TinyVowCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = TinyVowSpacing.CardHorizontal,
                    vertical = TinyVowSpacing.CardVertical,
                ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
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

    TinyVowCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = TinyVowSpacing.CardHorizontal,
                    vertical = TinyVowSpacing.CardVertical,
                ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
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
    TinyVowCard(
        shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = TinyVowSpacing.CardHorizontal,
                    vertical = TinyVowSpacing.CardVertical,
                ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.SectionGap),
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

    TinyVowCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = TinyVowSpacing.CardHorizontal,
                    vertical = TinyVowSpacing.CardVertical,
                ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
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
                OutlinedButton(
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

    TinyVowCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = TinyVowSpacing.CardHorizontal,
                    vertical = TinyVowSpacing.CardVertical,
                ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
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
    TinyVowCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = TinyVowSpacing.CardHorizontal,
                    vertical = TinyVowSpacing.CardVertical,
                ),
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
            activeRewardEffects = emptyList(),
            userPoints = 120.5,
            todayPoints = 10.0,
            isLoadingApps = false,
            superModeStatus = SuperModeStatus(false, false, false, false, "06:00 - 10:00", 360, 600, null, 0L),
            notificationPermissionGranted = false,
            isIgnoringBattery = false,
            permissionReliabilitySnapshot =
                PermissionReliabilitySnapshot.build(
                    groups = emptyList(),
                    usageAccessGranted = false,
                    accessibilityDisclosureAccepted = false,
                    accessibilityServiceEnabled = false,
                    notificationPermissionGranted = false,
                    isIgnoringBatteryOptimizations = false,
                    isAutoStartDismissed = false,
                    lastAccessibilityHeartbeatAtMillis = null,
                    nowMillis = System.currentTimeMillis(),
                ),
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
            onOpenPermissionDiagnostics = {},
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
            activeRewardEffects = emptyList(),
            userPoints = 450.0,
            todayPoints = 25.0,
            isLoadingApps = false,
            superModeStatus = SuperModeStatus(true, true, true, true, "06:00 - 10:00", 360, 600, System.currentTimeMillis() + 300_000L, 300_000L),
            notificationPermissionGranted = true,
            isIgnoringBattery = true,
            permissionReliabilitySnapshot =
                PermissionReliabilitySnapshot.build(
                    groups = emptyList(),
                    usageAccessGranted = true,
                    accessibilityDisclosureAccepted = true,
                    accessibilityServiceEnabled = true,
                    notificationPermissionGranted = true,
                    isIgnoringBatteryOptimizations = true,
                    isAutoStartDismissed = false,
                    lastAccessibilityHeartbeatAtMillis = System.currentTimeMillis(),
                    nowMillis = System.currentTimeMillis(),
                ),
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
            onOpenPermissionDiagnostics = {},
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
