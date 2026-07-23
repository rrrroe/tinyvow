package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.i18n.AppText

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.os.PowerManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.health.connect.client.PermissionController
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.core.content.FileProvider
import com.rrrrz.tinyvow.BuildConfig
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.data.activation.ChinaSubscriptionRepository
import com.rrrrz.tinyvow.data.activation.LocalActivationSubscriptionRepository
import com.rrrrz.tinyvow.data.account.BackendAccountRepository
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
import com.rrrrz.tinyvow.data.db.OfflineFocusAbandonReason
import com.rrrrz.tinyvow.data.db.OfflineFocusMode
import com.rrrrz.tinyvow.data.db.OfflineFocusSessionStatus
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
import com.rrrrz.tinyvow.data.repository.DailyCheckInMonthState
import com.rrrrz.tinyvow.data.repository.DailyCheckInRepository
import com.rrrrz.tinyvow.data.repository.DailyCheckInResult
import com.rrrrz.tinyvow.data.repository.DailyCheckInTodayState
import com.rrrrz.tinyvow.data.repository.DailyArchiveRepository
import com.rrrrz.tinyvow.data.repository.CustomRewardDraft
import com.rrrrz.tinyvow.data.repository.calculateEncourageTargetPoints
import com.rrrrz.tinyvow.data.repository.PointsRepository
import com.rrrrz.tinyvow.data.repository.InventoryRewardItem
import com.rrrrz.tinyvow.data.repository.OfflineFocusCategory
import com.rrrrz.tinyvow.data.repository.OfflineFocusRepository
import com.rrrrz.tinyvow.data.repository.OfflineFocusSession
import com.rrrrz.tinyvow.data.repository.OfflineFocusTodaySummary
import com.rrrrz.tinyvow.data.repository.PendingStreakShieldItem
import com.rrrrz.tinyvow.data.repository.ProtectionEventRepository
import com.rrrrz.tinyvow.data.repository.PurchaseRewardResult
import com.rrrrz.tinyvow.data.repository.RewardStoreItem
import com.rrrrz.tinyvow.data.repository.RewardSaveResult
import com.rrrrz.tinyvow.data.repository.RewardSaveValidationError
import com.rrrrz.tinyvow.data.repository.UseRewardResult
import com.rrrrz.tinyvow.data.settings.AppTextSize
import com.rrrrz.tinyvow.data.settings.HomeActivityRingColorPreference
import com.rrrrz.tinyvow.data.settings.HomeActivityRingColorPreferences
import com.rrrrz.tinyvow.data.settings.HomeActivityRingColorSource
import com.rrrrz.tinyvow.data.settings.HomeActivityRingMetric
import com.rrrrz.tinyvow.data.settings.HomeActivityRingPreferences
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.server.BackendSubscriptionStore
import com.rrrrz.tinyvow.data.server.HttpTinyVowBackendApi
import com.rrrrz.tinyvow.data.server.TinyVowBackendException
import com.rrrrz.tinyvow.data.settings.OfflineFocusCategoryDefaults
import com.rrrrz.tinyvow.data.special.SpecialAppUsageRepository
import com.rrrrz.tinyvow.data.steps.HealthConnectStepDataSource
import com.rrrrz.tinyvow.data.steps.StepTrackingRepository
import com.rrrrz.tinyvow.data.time.BusinessDay
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
import com.rrrrz.tinyvow.service.offline.OfflineFocusTimerService
import com.rrrrz.tinyvow.ui.theme.ThemeTokens
import com.rrrrz.tinyvow.ui.theme.TinyVowTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.io.FileInputStream
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
import kotlin.system.exitProcess
import com.rrrrz.tinyvow.data.usage.MergedUsageRepository
import com.rrrrz.tinyvow.data.usage.UsageRepository

import com.rrrrz.tinyvow.data.db.EncourageMetric
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.db.PointLedgerEntity
import com.rrrrz.tinyvow.data.db.AchievementEntity
import com.rrrrz.tinyvow.data.db.AchievementTier
import com.rrrrz.tinyvow.data.db.ActiveRewardEffectEntity
import com.rrrrz.tinyvow.data.db.ActiveRewardEffectStatus
import com.rrrrz.tinyvow.data.db.DailyArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyArchiveStateEntity
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
import com.rrrrz.tinyvow.ui.theme.DailyRandomThemeId
import com.rrrrz.tinyvow.ui.theme.DefaultThemeSeed
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.ThemePresets
import com.rrrrz.tinyvow.ui.theme.TinyVowButton
import com.rrrrz.tinyvow.ui.theme.TinyVowButtonTone
import com.rrrrz.tinyvow.ui.theme.TinyVowCard
import com.rrrrz.tinyvow.ui.theme.TinyVowDetailScaffold
import com.rrrrz.tinyvow.ui.theme.TinyVowElevation
import com.rrrrz.tinyvow.ui.theme.TinyVowEmptyState
import com.rrrrz.tinyvow.ui.theme.TinyVowIconSurface
import com.rrrrz.tinyvow.ui.theme.TinyVowMetricTile
import com.rrrrz.tinyvow.ui.theme.TinyVowPageBackground
import com.rrrrz.tinyvow.ui.theme.TinyVowRadius
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import com.rrrrz.tinyvow.ui.theme.TinyVowSnackbarHost
import com.rrrrz.tinyvow.ui.theme.TinyVowStatusPill
import com.rrrrz.tinyvow.ui.theme.selectedThemeDisplayName

enum class Screen { HOME, REWARDS, STATS, ME, HOME_STEP_PROGRESS_STATS, CHECK_IN_OVERVIEW, ME_ACCOUNT, ME_SAVED_PROGRESS_STATS, ME_POINTS_PROGRESS_STATS, ME_PRO, ME_PERMISSIONS, ME_NOTIFICATIONS, ME_DAY_BOUNDARY, ME_OFFLINE_FOCUS, ME_APPEARANCE, ME_RING_SETTINGS, ME_DATA_PRIVACY, ME_VERSION, SUPER_MODE, LABORATORY, LAB_FOCUS_HISTORY_EDITOR, HISTORY, THEME, LANGUAGE, HELP_FEEDBACK, CONTACT_US, SPECIAL_APPS, WEREAD_SPECIAL_APP, MEDIA_APPS, LOCK_SCREEN_TIMER_APPS, APP_COLOR_DEBUG, PERMISSION_DIAGNOSTICS }
enum class RewardsSection { STORE, INVENTORY, ACHIEVEMENTS }

private const val CONTACT_EMAIL = "rrrr.zhao@qq.com"
private const val WEREAD_AUTO_SYNC_DEBOUNCE_MS = 60_000L

private fun accountErrorMessage(error: Throwable): String {
    val code =
        when (error) {
            is TinyVowBackendException -> error.errorCode
            else -> error.message
        }
    return when (code) {
        "email_already_registered" -> AppText.t("account_error_email_registered")
        "email_invalid" -> AppText.t("account_error_email_invalid")
        "credentials_invalid" -> AppText.t("account_error_credentials_invalid")
        "login_rate_limited" -> AppText.t("account_error_rate_limited")
        "device_limit_reached" -> AppText.t("account_error_device_limit")
        "account_switch_requires_sign_out" -> AppText.t("account_error_switch_requires_sign_out")
        "avatar_too_large" -> AppText.t("account_error_avatar_too_large")
        "avatar_type_unsupported" -> AppText.t("account_error_avatar_type")
        "avatar_content_invalid" -> AppText.t("account_error_avatar_content")
        "account_registration_required" -> AppText.t("account_error_registration_required")
        "email_delivery_unavailable" -> AppText.t("account_error_email_delivery_unavailable")
        "email_delivery_failed" -> AppText.t("account_error_email_delivery_failed")
        "email_code_send_too_frequent" -> AppText.t("account_error_code_too_frequent")
        "email_code_invalid" -> AppText.t("account_error_code_invalid")
        "email_code_expired" -> AppText.t("account_error_code_expired")
        else -> error.message ?: AppText.t("account_error_backend_unavailable")
    }
}
private const val HOME_STEP_REFRESH_INTERVAL_MS = 60_000L
private const val HOME_CONTROL_TOLERANCE_MINUTES = 5L
private const val HOME_OVERVIEW_DATA_REVEAL_MILLIS = 1_440
private const val HOME_CONTROL_RING_CLOSE_HOUR = 20
private val HomeCompactCardHeight = 64.dp

private data class PendingSuperModeRequest(
    val message: String,
    val onAllowed: (() -> Unit)?,
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
    ACTIVITY_RECOGNITION,
    BATTERY_OPTIMIZATION,
    BACKGROUND_START,
}

private data class HomeOverviewUiState(
    val dateLabel: String,
    val tagline: String,
    val activityRings: HomeActivityRingsUiState,
    val control: HomeControlOverviewUiState,
    val encourage: HomeEncourageOverviewUiState,
    val history: HomeHistoryOverviewUiState,
    val totalUsageMillis: Long,
    val controlUsageMillis: Long,
    val encourageUsageMillis: Long,
    val savedMillis: Long,
    val behaviorScoreMetrics: List<DailyBehaviorScoreMetric>,
    val behaviorComparisonMetrics: List<DailyBehaviorScoreMetric>,
    val battleActions: List<HomeBattleAction>,
)

data class HomeActivityRingsUiState(
    val controlProgress: Float,
    val encourageProgress: Float,
    val growthProgress: Float,
    val stepProgress: Float = 0f,
    val focusProgress: Float = 0f,
    val controlAvailable: Boolean,
    val encourageAvailable: Boolean,
    val growthAvailable: Boolean,
    val stepAvailable: Boolean = false,
    val focusAvailable: Boolean = false,
    val growthTargetPoints: Double,
    val stepCount: Int = 0,
    val stepTarget: Int = 0,
    val focusMillis: Long = 0L,
    val focusTargetMillis: Long = 0L,
    val controlDetail: HomeActivityRingDetailUiState = HomeActivityRingDetailUiState(),
    val encourageDetail: HomeActivityRingDetailUiState = HomeActivityRingDetailUiState(),
    val growthDetail: HomeActivityRingDetailUiState = HomeActivityRingDetailUiState(),
    val outer: HomeActivityRingSlotUiState = HomeActivityRingSlotUiState(HomeActivityRingMetric.CONTROL, controlProgress, controlAvailable),
    val middle: HomeActivityRingSlotUiState = HomeActivityRingSlotUiState(HomeActivityRingMetric.ENCOURAGE, encourageProgress, encourageAvailable),
    val inner: HomeActivityRingSlotUiState = HomeActivityRingSlotUiState(HomeActivityRingMetric.GROWTH, growthProgress, growthAvailable),
)

data class HomeActivityRingSlotUiState(
    val metric: HomeActivityRingMetric,
    val progress: Float = 0f,
    val available: Boolean = false,
)

data class HomeActivityRingDetailUiState(
    val groupCount: Int = 0,
    val usedMillis: Long = 0L,
    val targetMillis: Long = 0L,
    val expectedMillis: Long = 0L,
    val periodElapsedProgress: Float = 0f,
    val healthProgress: Float = 0f,
    val earnedPoints: Double = 0.0,
    val targetPoints: Double = 0.0,
)

private data class YesterdayReportUiState(
    val archiveDate: String,
    val dateLabel: String,
    val footerLine: String,
    val message: String,
    val totalUsageLabel: String,
    val savedLabel: String,
    val pointsNetLabel: String,
    val blockCountLabel: String,
    val redemptionCountLabel: String,
    val controlCompletedLabel: String,
    val controlExceededLabel: String,
    val encourageCompletedLabel: String,
    val nightUsageLabel: String,
    val peakPeriodLabel: String,
    val topApps: List<AppDisplayItem>,
    val totalUsageMillis: Long,
    val controlUsageMillis: Long,
    val encourageUsageMillis: Long,
    val savedMillis: Long,
    val scoreMetrics: List<DailyBehaviorScoreMetric>,
    val comparisonScoreMetrics: List<DailyBehaviorScoreMetric>,
)

data class HomeOverviewRuntimeState(
    val usageMap: Map<String, Long> = emptyMap(),
    val periodUsageMap: Map<String, Long> = emptyMap(),
    val todayAppUsageMap: Map<String, Long> = emptyMap(),
    val todayAppOpenCountMap: Map<String, Int> = emptyMap(),
    val todaySessions: List<AppSession> = emptyList(),
    val isUsageReady: Boolean = false,
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
    val subtitleStrikethroughText: String? = null,
)

private enum class HomeOverviewDetailKind {
    CONTROL,
    ENCOURAGE,
}

private data class HomeOverviewGroupDetailUiState(
    val groupData: AppGroupWithApps,
    val usedMillis: Long,
    val targetMillis: Long,
    val extraMinutes: Int = 0,
    val hasPeriodPass: Boolean = false,
    val pointsMultiplier: Double = 1.0,
)

private data class HomeControlOverviewUiState(
    val todaySavedMinutes: Int,
    val completedGroups: Int,
    val totalGroups: Int,
    val scoreRatio: Float,
    val streakDays: Int,
    val streakLabel: String,
    val groups: List<HomeOverviewGroupDetailUiState> = emptyList(),
)

private data class HomeEncourageOverviewUiState(
    val todayEarnedPoints: Double,
    val completedGroups: Int,
    val totalGroups: Int,
    val scoreRatio: Float,
    val streakDays: Int,
    val streakLabel: String,
    val pointsMultiplierLabel: String?,
    val groups: List<HomeOverviewGroupDetailUiState> = emptyList(),
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
    pointLedgerEntries: List<PointLedgerEntity>,
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
                    pointLedgerEntries = pointLedgerEntries,
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
        onClick = onClick,
        selected = selected,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1,
        )
    }
}

private fun purchaseRewardResultMessage(result: PurchaseRewardResult): String =
    when (result) {
        is PurchaseRewardResult.Success ->
            AppText.t("redeem_purchase_success", result.rewardTitle, result.pointCost)
        PurchaseRewardResult.InsufficientPoints -> AppText.t("redeem_error_insufficient_points")
        PurchaseRewardResult.OutOfStock -> AppText.t("redeem_error_out_of_stock")
        PurchaseRewardResult.DailyLimitReached -> AppText.t("redeem_error_daily_limit_reached")
        PurchaseRewardResult.MonthlyLimitReached -> AppText.t("redeem_error_monthly_limit_reached")
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
    hostActivity: androidx.activity.ComponentActivity? = null,
    completedOfflineFocusSessionId: String? = null,
    offlineFocusDetailRequestToken: Int = 0,
    onCompletedOfflineFocusConsumed: () -> Unit = {},
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
    val backendStore = remember(context) {
        BuildConfig.TINYVOW_BACKEND_BASE_URL
            .takeIf { it.isNotBlank() }
            ?.let { BackendSubscriptionStore(context) }
    }
    val backendApi = remember(context) {
        BuildConfig.TINYVOW_BACKEND_BASE_URL
            .takeIf { it.isNotBlank() }
            ?.let { baseUrl ->
                HttpTinyVowBackendApi(
                    baseUrl = baseUrl,
                    deviceName = listOf(Build.MANUFACTURER, Build.MODEL)
                        .filter { it.isNotBlank() }
                        .joinToString(" ")
                        .takeIf { it.isNotBlank() },
                    appVersion = BuildConfig.VERSION_NAME,
                    channel = BuildConfig.STORE_CHANNEL,
                )
            }
    }
    val backendAccountRepository = remember(backendApi, backendStore) {
        if (backendApi != null && backendStore != null) {
            BackendAccountRepository(backendApi, backendStore)
        } else {
            null
        }
    }
    val localActivationRepository = remember(context) {
        if (BuildConfig.ENABLE_LOCAL_ACTIVATION) {
            LocalActivationSubscriptionRepository(context, BuildConfig.ACTIVATION_PUBLIC_KEY_BASE64)
        } else {
            null
        }
    }
    val chinaSubscriptionRepository = remember(context, localActivationRepository, backendApi, backendStore) {
        if (localActivationRepository != null && backendApi != null && backendStore != null) {
            ChinaSubscriptionRepository(
                context = context,
                localRepository = localActivationRepository,
                backendBaseUrl = BuildConfig.TINYVOW_BACKEND_BASE_URL,
                store = backendStore,
                api = backendApi,
            )
        } else {
            null
        }
    }
    val subscriptionRepository: SubscriptionRepository = remember(context, chinaSubscriptionRepository) {
        if (BuildConfig.ENABLE_PLAY_BILLING) {
            PlayBillingSubscriptionRepository(context)
        } else {
            chinaSubscriptionRepository ?: NoopSubscriptionRepository()
        }
    }
    
    val database = remember(context) { AppDatabase.getDatabase(context) }
    val appLimitRepository = remember(database, context) { AppLimitRepository(context, database) }
    val usageRepository = remember(context) { MergedUsageRepository(context) }
    val specialAppUsageRepository = remember(context) { SpecialAppUsageRepository(context) }
    val pointsRepository = remember(database, context) { PointsRepository(context, database) }
    val dailyArchiveRepository = remember(database, context) { DailyArchiveRepository(context, database) }
    val dailyCheckInRepository = remember(database, context) { DailyCheckInRepository(context, database) }
    val stepTrackingRepository = remember(database, context) { StepTrackingRepository(context, database) }
    val offlineFocusRepository = remember(database, context) { OfflineFocusRepository(context, database) }
    val statsReportMemoryCache = remember { StatsReportMemoryCache() }
    val protectionEventRepository = remember(database, preferences) {
        ProtectionEventRepository(database) { preferences.getDayBoundaryHourOnce() }
    }
    val localDataManager = remember(database, context, preferences) {
        LocalDataManager(context, database, preferences)
    }
    val superModeController = remember(preferences) { SuperModeController(preferences) }
    val currentTimeMillis by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            val now = System.currentTimeMillis()
            value = now
            delay((60_000L - now % 60_000L).coerceIn(1_000L, 60_000L))
        }
    }
    val businessToday = remember(currentTimeMillis) {
        BusinessDay.today(ZoneId.systemDefault(), BusinessDay.cachedStartHour(), currentTimeMillis)
    }
    val offlineFocusDayStartMillis = remember(businessToday) {
        BusinessDay.startOfDayMillis(businessToday, ZoneId.systemDefault(), BusinessDay.cachedStartHour())
    }
    val offlineFocusDayEndMillis = remember(businessToday) {
        BusinessDay.nextDayStartMillis(businessToday, ZoneId.systemDefault(), BusinessDay.cachedStartHour())
    }
    val offlineFocusCategories by offlineFocusRepository.observeCategories()
        .collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val offlineFocusAllCategories by offlineFocusRepository.observeCategories(includeArchived = true)
        .collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val offlineFocusActiveSession by offlineFocusRepository.observeActiveSession()
        .collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    val offlineFocusTodaySummary by offlineFocusRepository
        .observeSummaryForDay(offlineFocusDayStartMillis, offlineFocusDayEndMillis)
        .collectAsStateWithLifecycle(initialValue = OfflineFocusTodaySummary(), lifecycle = lifecycle)
    val offlineFocusDefaultCategoryId by preferences.offlineFocusDefaultCategoryId
        .collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    val offlineFocusDefaultDurationMinutes by preferences.offlineFocusDefaultDurationMinutes
        .collectAsStateWithLifecycle(
            initialValue = ManagedAppPreferences.DEFAULT_OFFLINE_FOCUS_DURATION_MINUTES,
            lifecycle = lifecycle,
        )
    val offlineFocusDefaultMode by preferences.offlineFocusDefaultMode
        .collectAsStateWithLifecycle(initialValue = OfflineFocusMode.NORMAL, lifecycle = lifecycle)
    val offlineFocusWhitelistPackages by preferences.offlineFocusWhitelistPackages
        .collectAsStateWithLifecycle(initialValue = emptySet(), lifecycle = lifecycle)
    val offlineFocusContinueOnLock by preferences.offlineFocusContinueOnLock
        .collectAsStateWithLifecycle(initialValue = true, lifecycle = lifecycle)
    val offlineFocusDailyPointCap by preferences.offlineFocusDailyPointCap
        .collectAsStateWithLifecycle(
            initialValue = ManagedAppPreferences.DEFAULT_OFFLINE_FOCUS_DAILY_POINT_CAP,
            lifecycle = lifecycle,
        )
    val offlineFocusRestReminderEnabled by preferences.offlineFocusRestReminderEnabled
        .collectAsStateWithLifecycle(initialValue = true, lifecycle = lifecycle)
    val offlineFocusRestReminderMinutes by preferences.offlineFocusRestReminderMinutes
        .collectAsStateWithLifecycle(initialValue = ManagedAppPreferences.DEFAULT_OFFLINE_FOCUS_REST_REMINDER_MINUTES, lifecycle = lifecycle)
    val offlineFocusRestReminderSoundEnabled by preferences.offlineFocusRestReminderSoundEnabled
        .collectAsStateWithLifecycle(initialValue = true, lifecycle = lifecycle)
    val offlineFocusRestReminderVibrationEnabled by preferences.offlineFocusRestReminderVibrationEnabled
        .collectAsStateWithLifecycle(initialValue = true, lifecycle = lifecycle)
    val offlineFocusRestReminderRingtoneUri by preferences.offlineFocusRestReminderRingtoneUri
        .collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    val offlineFocusCategoryDefaults by preferences.offlineFocusCategoryDefaults
        .collectAsStateWithLifecycle(initialValue = emptyMap(), lifecycle = lifecycle)
    val todayStepState by stepTrackingRepository.observeToday(businessToday.toString()).collectAsStateWithLifecycle(
        initialValue =
            com.rrrrz.tinyvow.data.steps.TodayStepState(
                date = businessToday.toString(),
                steps = 0,
                available = stepTrackingRepository.hasStepCounter(),
                permissionGranted = stepTrackingRepository.hasActivityRecognitionPermission(),
        ),
        lifecycle = lifecycle,
    )
    val historicalStepDays by database.stepDayDao().observeAll()
        .collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    
    val loadedGroupsWithApps by collectNullableAsStateWithLifecycle(appLimitRepository.getAllGroupsWithApps(), lifecycle)
    val groupsWithAppsLoaded = loadedGroupsWithApps != null
    val groupsWithApps = loadedGroupsWithApps.orEmpty()
    val userPoints by preferences.userPoints.collectAsStateWithLifecycle(initialValue = 0.0, lifecycle = lifecycle)
    val todayPoints by preferences.todayPoints.collectAsStateWithLifecycle(initialValue = 0.0, lifecycle = lifecycle)
    val todayEarnedPoints by database.pointLedgerDao().observeEarnedByDate(businessToday.toString())
        .collectAsStateWithLifecycle(initialValue = 0.0, lifecycle = lifecycle)
    val stepPointsPerStep by preferences.stepPointsPerStep.collectAsStateWithLifecycle(
        initialValue = StepTrackingRepository.DEFAULT_POINTS_PER_STEP,
        lifecycle = lifecycle,
    )
    val stepPointsRewardThreshold by preferences.stepPointsRewardThreshold.collectAsStateWithLifecycle(
        initialValue = StepTrackingRepository.DEFAULT_REWARD_THRESHOLD,
        lifecycle = lifecycle,
    )
    val homeActivityRingPreferences by preferences.homeActivityRingPreferences.collectAsStateWithLifecycle(
        initialValue = HomeActivityRingPreferences(),
        lifecycle = lifecycle,
    )
    val homeActivityRingColorPreferences by preferences.homeActivityRingColorPreferences.collectAsStateWithLifecycle(
        initialValue = HomeActivityRingColorPreferences(),
        lifecycle = lifecycle,
    )
    val offlineFocusDailyTargetMinutes by preferences.offlineFocusDailyTargetMinutes.collectAsStateWithLifecycle(
        initialValue = ManagedAppPreferences.DEFAULT_OFFLINE_FOCUS_DAILY_TARGET_MINUTES,
        lifecycle = lifecycle,
    )
    val selectedThemeId by preferences.selectedThemeId.collectAsStateWithLifecycle(initialValue = DefaultThemeSeed.id, lifecycle = lifecycle)
    val customThemes by preferences.customThemes.collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val selectedAppLanguage by preferences.selectedAppLanguage.collectAsStateWithLifecycle(initialValue = com.rrrrz.tinyvow.i18n.AppLanguage.SYSTEM, lifecycle = lifecycle)
    val selectedAppTextSize by preferences.selectedAppTextSize.collectAsStateWithLifecycle(
        initialValue = AppTextSize.STANDARD,
        lifecycle = lifecycle,
    )
    val dayBoundaryHour by preferences.dayBoundaryHour.collectAsStateWithLifecycle(
        initialValue = BusinessDay.DEFAULT_START_HOUR,
        lifecycle = lifecycle,
    )
    val profileDisplayName by preferences.profileDisplayName.collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    val profileAvatarUri by preferences.profileAvatarUri.collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    val storeRewardItems by appLimitRepository.observeStoreRewardsWithInventory().collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val inventoryRewardItems by appLimitRepository.observeInventoryRewards().collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val checkInTodayState by dailyCheckInRepository.observeTodayState().collectAsStateWithLifecycle(
        initialValue = DailyCheckInTodayState(businessToday, checkedIn = false, bufferCardCount = 0),
        lifecycle = lifecycle,
    )
    var selectedCheckInMonthKey by rememberSaveable {
        mutableStateOf(
            YearMonth.from(Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault())).toString(),
        )
    }
    val checkInMonth = remember(selectedCheckInMonthKey) {
        runCatching { YearMonth.parse(selectedCheckInMonthKey) }
            .getOrElse { YearMonth.from(Instant.ofEpochMilli(currentTimeMillis).atZone(ZoneId.systemDefault())) }
    }
    val checkInMonthState by dailyCheckInRepository.observeMonth(checkInMonth).collectAsStateWithLifecycle(
        initialValue = DailyCheckInMonthState(
            month = checkInMonth,
            days = emptyList(),
            checkedInDays = 0,
            bufferCardCount = 0,
            allControlKeptDays = 0,
            encourageCompletedDays = 0,
        ),
        lifecycle = lifecycle,
    )
    val pendingShieldItems by appLimitRepository.observePendingStreakShields().collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val allRewardEffects by database.activeRewardEffectDao().observeAll().collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val activeBonusTimes by database.bonusTimeDao().observeActive(currentTimeMillis)
        .collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val homeOverviewGroupsWithAppsLoaded by collectNullableAsStateWithLifecycle(appLimitRepository.getAllGroupsWithApps(), lifecycle)
    val homeOverviewUserPointsLoaded by collectNullableAsStateWithLifecycle(preferences.userPoints, lifecycle)
    val homeOverviewTodayPointsLoaded by collectNullableAsStateWithLifecycle(preferences.todayPoints, lifecycle)
    val homeOverviewTodayEarnedPointsLoaded by collectNullableAsStateWithLifecycle(
        database.pointLedgerDao().observeEarnedByDate(businessToday.toString()),
        lifecycle,
    )
    val homeOverviewAchievementProgressLoaded by collectNullableAsStateWithLifecycle(
        appLimitRepository.observeAchievementProgress(),
        lifecycle,
    )
    val homeOverviewAllRewardEffectsLoaded by collectNullableAsStateWithLifecycle(database.activeRewardEffectDao().observeAll(), lifecycle)
    val historicalArchivesLoaded by
        collectNullableAsStateWithLifecycle(dailyArchiveRepository.getRecentArchives(limit = 3650), lifecycle)
    val historicalArchives = historicalArchivesLoaded.orEmpty()
    val pointDailyStats by database.pointLedgerDao().observeDailyStats()
        .collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val pointLedgerEntries by database.pointLedgerDao().observeAllEntries()
        .collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val pointSpendRecords by database.pointLedgerDao().observeSpendRecords()
        .collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
    val homeOverviewRecentGroupArchivesLoaded by collectNullableAsStateWithLifecycle(
        dailyArchiveRepository.getGroupArchivesByRange(
            businessToday.minusDays(7).toString(),
            businessToday.minusDays(1).toString(),
        ),
        lifecycle,
    )
    val homeOverviewInputsReady =
        homeOverviewGroupsWithAppsLoaded != null &&
            homeOverviewUserPointsLoaded != null &&
            homeOverviewTodayPointsLoaded != null &&
            homeOverviewTodayEarnedPointsLoaded != null &&
            homeOverviewAchievementProgressLoaded != null &&
            homeOverviewAllRewardEffectsLoaded != null &&
            historicalArchivesLoaded != null &&
            homeOverviewRecentGroupArchivesLoaded != null
    val activeRewardEffects = remember(allRewardEffects, currentTimeMillis) {
        allRewardEffects.filter {
            it.status == ActiveRewardEffectStatus.ACTIVE &&
                it.startAt <= currentTimeMillis &&
                it.expireAt > currentTimeMillis
        }
    }
    val activeBonusMinutesByGroup = remember(activeBonusTimes) {
        activeBonusTimes
            .groupBy { it.targetGroupId }
            .mapValues { (_, bonusTimes) -> bonusTimes.sumOf { it.extraMinutes.coerceAtLeast(0) } }
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
    val backendAccountFlow = remember(backendAccountRepository) {
        backendAccountRepository?.account ?: flowOf(null)
    }
    val backendAccount by backendAccountFlow.collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    val subscriptionEntitlement by subscriptionRepository.entitlement.collectAsStateWithLifecycle(lifecycle = lifecycle)
    val subscriptionOffers by subscriptionRepository.offers.collectAsStateWithLifecycle(lifecycle = lifecycle)
    val debugProExpiresAtMillis by preferences.debugProExpiresAtMillis.collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
    var localCompletedOfflineFocusSessionId by remember { mutableStateOf<String?>(null) }
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
    val visibleCompletedOfflineFocusSessionId = completedOfflineFocusSessionId ?: localCompletedOfflineFocusSessionId
    val completedOfflineFocusSessionFlow =
        remember(visibleCompletedOfflineFocusSessionId, offlineFocusRepository) {
            visibleCompletedOfflineFocusSessionId
                ?.let { offlineFocusRepository.observeSession(it) }
                ?: flowOf(null)
        }
    val completedOfflineFocusSession by completedOfflineFocusSessionFlow.collectAsStateWithLifecycle(
        initialValue = null,
        lifecycle = lifecycle,
    )
    val superModeStatus = remember(superModeStoredState, proEntitlement.isProActive, currentTimeMillis) {
        superModeController.buildStatus(
            storedState = superModeStoredState,
            isProActive = proEntitlement.isProActive,
            nowMillis = currentTimeMillis,
        )
    }
    val currentTimeLabel = remember(currentTimeMillis) {
        val localTime = Instant.ofEpochMilli(currentTimeMillis).atZone(ZoneId.systemDefault()).toLocalTime()
        superModeController.formatTime(localTime.hour * 60 + localTime.minute)
    }

    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var accountOperationInProgress by remember { mutableStateOf(false) }
    var lastOfflineFocusActiveSessionId by remember { mutableStateOf<String?>(null) }
    var homeOverviewAnimationReplayToken by remember { mutableIntStateOf(0) }
    var statsAnimationReplayToken by remember { mutableIntStateOf(0) }
    var rewardsSection by remember { mutableStateOf(RewardsSection.STORE) }
    var hasPlayedHomeOverviewDataReveal by rememberSaveable { mutableStateOf(false) }
    var homeOverviewRuntimeState by remember { mutableStateOf(HomeOverviewRuntimeState()) }
    val homeAppIconCache = remember { mutableStateMapOf<String, Drawable>() }
    LaunchedEffect(offlineFocusDetailRequestToken) {
        if (offlineFocusDetailRequestToken > 0) {
            currentScreen = Screen.HOME
        }
    }
    LaunchedEffect(offlineFocusActiveSession?.id, visibleCompletedOfflineFocusSessionId) {
        val activeSession = offlineFocusActiveSession
        if (activeSession != null) {
            lastOfflineFocusActiveSessionId = activeSession.id
            return@LaunchedEffect
        }
        if (visibleCompletedOfflineFocusSessionId != null) {
            lastOfflineFocusActiveSessionId = null
            return@LaunchedEffect
        }
        val endedSessionId = lastOfflineFocusActiveSessionId ?: return@LaunchedEffect
        lastOfflineFocusActiveSessionId = null
        val endedSession = offlineFocusRepository.getSessionOnce(endedSessionId) ?: return@LaunchedEffect
        val shouldShowRestScreen =
            endedSession.status == OfflineFocusSessionStatus.COMPLETED ||
                endedSession.status == OfflineFocusSessionStatus.SETTLED ||
                (
                    endedSession.status == OfflineFocusSessionStatus.ABANDONED &&
                        endedSession.abandonedReason == OfflineFocusAbandonReason.BELOW_THRESHOLD
                )
        if (shouldShowRestScreen) {
            localCompletedOfflineFocusSessionId = endedSession.id
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val performTodayCheckIn: () -> Unit = {
        coroutineScope.launch {
            val result = dailyCheckInRepository.checkInToday()
            val message =
                when (result) {
                    is DailyCheckInResult.Success -> AppText.t("checkin_success_buffer_card")
                    DailyCheckInResult.AlreadyCheckedIn -> AppText.t("checkin_already_done")
                    DailyCheckInResult.RewardUnavailable -> AppText.t("checkin_reward_unavailable")
                }
            snackbarHostState.showSnackbar(message)
        }
    }
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
    var activityRecognitionPermissionGranted by remember {
        mutableStateOf(stepTrackingRepository.hasActivityRecognitionPermission())
    }
    var healthConnectStepsAvailable by remember {
        mutableStateOf(stepTrackingRepository.isHealthConnectAvailable())
    }
    var healthConnectStepsPermissionGranted by remember {
        mutableStateOf(false)
    }
    
    var installedApps by remember { mutableStateOf<List<ManagedApp>>(emptyList()) }
    var isLoadingApps by remember { mutableStateOf(false) }
    val meTotalSavedMinutes = remember(historicalArchives) {
        historicalArchives.sumOf { it.savedMillis } / 60_000L
    }

    var yesterdayReportState by remember { mutableStateOf<YesterdayReportUiState?>(null) }
    var showWelcomeIntro by remember { mutableStateOf(false) }
    var showFirstRunCoachmark by remember { mutableStateOf(false) }
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
        if (!superModeStatus.isEnabled) {
            clearPendingSuperModeRequest()
            onAllowed()
            return
        }
        if (!superModeStatus.isConfigured) {
            clearPendingSuperModeRequest()
            setupRequiredActionLabel = guardedActionLabel(action)
            showSuperModeSetupDialog = true
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
    val activityRecognitionPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        activityRecognitionPermissionGranted = stepTrackingRepository.hasActivityRecognitionPermission()
    }
    val healthConnectPermissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(),
    ) {
        coroutineScope.launch {
            healthConnectStepsAvailable = stepTrackingRepository.isHealthConnectAvailable()
            healthConnectStepsPermissionGranted = stepTrackingRepository.hasHealthConnectStepPermission()
            stepTrackingRepository.refreshTodayFromHealthConnect()
        }
    }
    val backupImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        coroutineScope.launch {
            runCatching {
                val result = localDataManager.restoreLocalBackup(uri)
                if (result.requiresRestart) {
                    val warning = if (result.warnings.contains("weread_key_material_not_restored")) {
                        "\n" + AppText.t("me_backup_restore_weread_key_note")
                    } else {
                        ""
                    }
                    snackbarHostState.showSnackbar(AppText.t("me_import_local_backup_success_restart") + warning)
                    context.restartAppAfterRestore()
                }
            }.onFailure {
                snackbarHostState.showSnackbar(AppText.t("me_import_local_backup_failed"))
            }
        }
    }
    val backupSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        coroutineScope.launch {
            runCatching {
                val file = localDataManager.exportLocalBackup()
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    FileInputStream(file).use { input -> input.copyTo(output) }
                } ?: error("Unable to open backup output file.")
            }.onSuccess {
                snackbarHostState.showSnackbar(AppText.t("me_export_backup_saved"))
            }.onFailure {
                snackbarHostState.showSnackbar(AppText.t("home_export_local_data_failed"))
            }
        }
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
                activityRecognitionPermissionGranted = stepTrackingRepository.hasActivityRecognitionPermission()
                healthConnectStepsAvailable = stepTrackingRepository.isHealthConnectAvailable()
                isIgnoringBattery = powerManager.isIgnoringBatteryOptimizations(context.packageName)
                coroutineScope.launch {
                    healthConnectStepsPermissionGranted = stepTrackingRepository.hasHealthConnectStepPermission()
                    stepTrackingRepository.refreshTodayFromHealthConnect()
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

    LaunchedEffect(stepTrackingRepository) {
        healthConnectStepsAvailable = stepTrackingRepository.isHealthConnectAvailable()
        healthConnectStepsPermissionGranted = stepTrackingRepository.hasHealthConnectStepPermission()
        stepTrackingRepository.refreshTodayFromHealthConnect()
    }

    DisposableEffect(activityRecognitionPermissionGranted, todayStepState.available, todayStepState.source) {
        if (
            activityRecognitionPermissionGranted &&
                todayStepState.available &&
                todayStepState.source != com.rrrrz.tinyvow.data.db.STEP_DAY_SOURCE_HEALTH_CONNECT
        ) {
            stepTrackingRepository.start()
        }
        onDispose { stepTrackingRepository.stop() }
    }

    LaunchedEffect(
        activityRecognitionPermissionGranted,
        healthConnectStepsPermissionGranted,
        todayStepState.available,
        todayStepState.source,
    ) {
        if (healthConnectStepsPermissionGranted) {
            while (true) {
                stepTrackingRepository.refreshTodayFromHealthConnect()
                delay(HOME_STEP_REFRESH_INTERVAL_MS)
            }
        } else if (
            activityRecognitionPermissionGranted &&
                todayStepState.available &&
                todayStepState.source != com.rrrrz.tinyvow.data.db.STEP_DAY_SOURCE_HEALTH_CONNECT
        ) {
            while (true) {
                delay(HOME_STEP_REFRESH_INTERVAL_MS)
                stepTrackingRepository.restart()
            }
        }
    }

    LaunchedEffect(proEntitlement.isProActive, todayStepState.date, todayStepState.steps, stepPointsPerStep) {
        stepTrackingRepository.creditTodayStepsIfEligible(
            steps = todayStepState.steps,
            date = todayStepState.date,
            pointsPerStep = stepPointsPerStep,
            allowNewCredit = proEntitlement.isProActive,
        )
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
        appLimitRepository.deleteDeprecatedStepGroups()
        appLimitRepository.confirmExpiredPendingRewardEffects()
        dailyArchiveRepository.ensureArchivesUpToYesterday()
        
        // Daily morning report logic.
        val todayDate = BusinessDay.today(ZoneId.systemDefault(), BusinessDay.cachedStartHour())
        val today = todayDate.toString()
        val lastShownFlow = preferences.lastSummaryShownDate
        val lastShown = lastShownFlow.first()
        if (lastShown != today) {
            val report =
                buildYesterdayReportUiState(
                    archiveRepository = dailyArchiveRepository,
                    reportDate = todayDate.minusDays(1),
                    context = context,
                )
            if (report != null) {
                yesterdayReportState = report
                preferences.setLastSummaryShownDate(today)
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
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
    LaunchedEffect(Unit) {
        appLimitRepository.newAchievementsAction.collectLatest { achievement ->
            newlyUnlockedAchievement = achievement
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
                    Screen.WEREAD_SPECIAL_APP, Screen.MEDIA_APPS, Screen.LOCK_SCREEN_TIMER_APPS, Screen.APP_COLOR_DEBUG -> Screen.SPECIAL_APPS
                    Screen.HOME_STEP_PROGRESS_STATS, Screen.PERMISSION_DIAGNOSTICS -> Screen.HOME
                    Screen.CHECK_IN_OVERVIEW, Screen.ME_ACCOUNT, Screen.ME_SAVED_PROGRESS_STATS, Screen.ME_POINTS_PROGRESS_STATS, Screen.ME_PRO, Screen.ME_PERMISSIONS, Screen.ME_NOTIFICATIONS, Screen.ME_DAY_BOUNDARY, Screen.ME_OFFLINE_FOCUS, Screen.ME_APPEARANCE, Screen.ME_RING_SETTINGS, Screen.ME_DATA_PRIVACY, Screen.ME_VERSION, Screen.SUPER_MODE, Screen.LABORATORY, Screen.HISTORY, Screen.THEME, Screen.LANGUAGE, Screen.HELP_FEEDBACK, Screen.CONTACT_US, Screen.SPECIAL_APPS -> Screen.ME
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

    LaunchedEffect(
        BuildConfig.ENABLE_LOCAL_ACTIVATION,
        authRepository,
        backendAccountRepository,
        chinaSubscriptionRepository,
    ) {
        if (BuildConfig.ENABLE_LOCAL_ACTIVATION) {
            val session = authRepository.ensureLocalSession(
                preferredUserId = chinaSubscriptionRepository?.restorableUserId(),
            )
            backendAccountRepository?.initialize(session.userId)
            chinaSubscriptionRepository?.bindUser(session.userId)
        }
    }

    LaunchedEffect(
        BuildConfig.ENABLE_LOCAL_ACTIVATION,
        userSession?.userId,
        chinaSubscriptionRepository,
    ) {
        if (BuildConfig.ENABLE_LOCAL_ACTIVATION && userSession?.userId != null) {
            chinaSubscriptionRepository?.bindUser(userSession?.userId)
        }
    }

    LaunchedEffect(currentScreen) {
        when (currentScreen) {
            Screen.HOME -> homeOverviewAnimationReplayToken += 1
            Screen.STATS -> statsAnimationReplayToken += 1
            else -> Unit
        }
    }

    LaunchedEffect(proEntitlement.isProActive, selectedThemeId, customThemes) {
        val selectedRemovedBuiltInTheme =
            selectedThemeId == DailyRandomThemeId ||
                (selectedThemeId.startsWith("preset_") && ThemePresets.none { it.id == selectedThemeId }) ||
                ProFeatureGate.isMemberTheme(selectedThemeId)
        if (selectedRemovedBuiltInTheme) {
            preferences.setSelectedThemeId(DefaultThemeSeed.id)
            return@LaunchedEffect
        }
        if (!proEntitlement.isProActive) {
            val customIndex = customThemes.indexOfFirst { it.id == selectedThemeId }
            val selectedThemeLocked =
                customIndex >= ProFeatureGate.limits(false).customThemeLimit
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
                        groupsWithAppsLoaded = groupsWithAppsLoaded,
                        dayBoundaryHour = dayBoundaryHour,
                        activeRewardEffects = activeRewardEffects,
                        activeBonusMinutesByGroup = activeBonusMinutesByGroup,
                        appIconCache = homeAppIconCache,
                        onAppIconLoaded = { packageName, icon ->
                            if (!homeAppIconCache.containsKey(packageName)) {
                                homeAppIconCache[packageName] = icon
                            }
                        },
                        userPoints = userPoints,
                        todayPoints = todayPoints,
                        todayEarnedPoints = todayEarnedPoints,
                        todayStepCount = todayStepState.steps,
                        todayStepPoints = todayStepState.steps * stepPointsPerStep,
                        stepPointsPerStep = stepPointsPerStep,
                        stepPointsRewardThreshold = stepPointsRewardThreshold,
                        homeActivityRingPreferences = homeActivityRingPreferences,
                        homeActivityRingColorPreferences = homeActivityRingColorPreferences,
                        isStepCounterAvailable = todayStepState.available || healthConnectStepsAvailable,
                        isActivityRecognitionPermissionGranted =
                            todayStepState.permissionGranted || healthConnectStepsPermissionGranted,
                        offlineFocusCategories = offlineFocusCategories,
                        offlineFocusActiveSession = offlineFocusActiveSession,
                        offlineFocusDetailRequestToken = offlineFocusDetailRequestToken,
                        offlineFocusTodaySummary = offlineFocusTodaySummary,
                        offlineFocusDefaultCategoryId = offlineFocusDefaultCategoryId,
                        offlineFocusDefaultDurationMinutes = offlineFocusDefaultDurationMinutes,
                        offlineFocusDefaultMode = offlineFocusDefaultMode,
                        offlineFocusRestReminderEnabled = offlineFocusRestReminderEnabled,
                        offlineFocusRestReminderMinutes = offlineFocusRestReminderMinutes,
                        offlineFocusCategoryDefaults = offlineFocusCategoryDefaults,
                        offlineFocusDailyTargetMinutes = offlineFocusDailyTargetMinutes,
                        offlineFocusRulesAvailable = effectiveAccessibilityServiceEnabled,
                        onSetOfflineFocusRestReminderEnabled = { enabled ->
                            coroutineScope.launch {
                                preferences.setOfflineFocusRestReminderEnabled(enabled)
                            }
                        },
                        onSetOfflineFocusRestReminderMinutes = { minutes ->
                            coroutineScope.launch {
                                preferences.setOfflineFocusRestReminderMinutes(minutes)
                            }
                        },
                        onStartOfflineFocus = { categoryId, minutes, mode ->
                            coroutineScope.launch {
                                if (!proEntitlement.isProActive) {
                                    proUpsellSource = ProUpsellSource.FOCUS_MODE
                                    return@launch
                                }
                                if (mode == OfflineFocusMode.STRICT && !effectiveAccessibilityServiceEnabled) {
                                    requestAccessibilitySettings()
                                    return@launch
                                }
                                val session = offlineFocusRepository.startSession(categoryId, minutes, mode)
                                    ?: run {
                                        snackbarHostState.showSnackbar(AppText.t("offline_focus_category_empty_start_hint"))
                                        return@launch
                                    }
                                preferences.setOfflineFocusDefaultMode(mode)
                                OfflineFocusTimerService.start(context, session.id)
                            }
                        },
                        onOpenOfflineFocusRulesSettings = { requestAccessibilitySettings() },
                        onAllowOfflineFocusViolationPackage = { packageName ->
                            coroutineScope.launch {
                                preferences.setOfflineFocusWhitelistPackages(offlineFocusWhitelistPackages + packageName)
                                offlineFocusActiveSession?.id?.let { sessionId ->
                                    offlineFocusRepository.resumeSession(sessionId)
                                    OfflineFocusTimerService.resume(context, sessionId)
                                }
                                snackbarHostState.showSnackbar(AppText.t("offline_focus_whitelist_added_and_resumed"))
                            }
                        },
                        onUpsertOfflineFocusCategory = { categoryId, name, iconKey, customIconPath, colorArgb, pointsPerMinute ->
                            coroutineScope.launch {
                                offlineFocusRepository.upsertCategory(
                                    categoryId = categoryId,
                                    name = name,
                                    iconKey = iconKey,
                                    customIconPath = customIconPath,
                                    colorArgb = colorArgb,
                                    pointsPerMinute = pointsPerMinute,
                                )
                                snackbarHostState.showSnackbar(AppText.t("offline_focus_settings_saved"))
                            }
                        },
                        onFinishOfflineFocusEarly = { sessionId ->
                            coroutineScope.launch {
                                val completedSession = offlineFocusRepository.stopSessionEarly(sessionId)
                                localCompletedOfflineFocusSessionId = completedSession?.id
                            }
                            OfflineFocusTimerService.stopEarly(context, sessionId, showCompletionAlert = false)
                        },
                        onAbandonOfflineFocus = { sessionId ->
                            coroutineScope.launch {
                                offlineFocusRepository.abandonSession(sessionId)
                            }
                            OfflineFocusTimerService.abandon(context, sessionId)
                        },
                        overviewInputsReady = homeOverviewInputsReady,
                        overviewRuntimeState = homeOverviewRuntimeState,
                        onOverviewRuntimeStateChange = { homeOverviewRuntimeState = it },
                        overviewAnimationReplayToken = homeOverviewAnimationReplayToken,
                        shouldPlayOverviewDataReveal = !hasPlayedHomeOverviewDataReveal,
                        onOverviewDataRevealStarted = {
                            hasPlayedHomeOverviewDataReveal = true
                        },
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
                        onRequestActivityRecognitionPermission = {
                            pendingSensitiveDisclosure = SensitivePermissionDisclosure.ACTIVITY_RECOGNITION
                        },
                        onOpenStepProComparison = {
                            currentScreen = Screen.ME_PRO
                        },
                        onOpenStepStats = {
                            currentScreen = Screen.HOME_STEP_PROGRESS_STATS
                        },
                        onRefreshStepData = {
                            coroutineScope.launch {
                                healthConnectStepsAvailable = stepTrackingRepository.isHealthConnectAvailable()
                                healthConnectStepsPermissionGranted = stepTrackingRepository.hasHealthConnectStepPermission()
                                val refreshedFromHealth = stepTrackingRepository.refreshTodayFromHealthConnect()
                                when {
                                    refreshedFromHealth -> {
                                        snackbarHostState.showSnackbar(AppText.t("home_step_refresh_requested"))
                                    }
                                    !stepTrackingRepository.hasStepCounter() -> {
                                        snackbarHostState.showSnackbar(AppText.t("home_step_counter_unavailable"))
                                    }
                                    !stepTrackingRepository.hasActivityRecognitionPermission() -> {
                                        pendingSensitiveDisclosure = SensitivePermissionDisclosure.ACTIVITY_RECOGNITION
                                    }
                                    else -> {
                                        stepTrackingRepository.restart()
                                        activityRecognitionPermissionGranted = stepTrackingRepository.hasActivityRecognitionPermission()
                                        snackbarHostState.showSnackbar(AppText.t("home_step_refresh_requested"))
                                    }
                                }
                            }
                        },
                        onSaveStepPointsPerStep = { pointsPerStep ->
                            coroutineScope.launch {
                                preferences.setStepPointsPerStep(pointsPerStep)
                            }
                        },
                        onSaveStepPointsRewardThreshold = { threshold ->
                            coroutineScope.launch {
                                preferences.setStepPointsRewardThreshold(threshold)
                            }
                        },
                        onSaveGroup = { id, name, limit, type, period, pts, pkgs ->
                            coroutineScope.launch {
                                val existingGroup = id?.let { groupId ->
                                    groupsWithApps.firstOrNull { it.group.id == groupId }
                                }
                                val previousGroup = existingGroup
                                val groupId =
                                    appLimitRepository.createOrUpdateGroup(
                                        id = id,
                                        name = name,
                                        limitMinutes = limit,
                                        type = type,
                                        limitPeriod = period,
                                        pointsPerMinute = pts,
                                    )
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
                        historicalArchives = historicalArchives,
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
                            pointLedgerEntries = pointLedgerEntries,
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
                            profileDisplayName = profileDisplayName ?: userSession?.displayName,
                            profileAvatarUri = profileAvatarUri ?: userSession?.avatarUrl,
                            archiveRepository = dailyArchiveRepository,
                            reportMemoryCache = statsReportMemoryCache,
                            screenEnterReplayToken = statsAnimationReplayToken,
                            isProActive = proEntitlement.isProActive,
                            offlineFocusEnabled = true,
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
                                    archiveCount = historicalArchives.size,
                                    latestArchiveDate = historicalArchives.maxByOrNull { it.archiveDate }?.archiveDate,
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
                        backendAccount = backendAccount,
                        isBackendAccountEnabled = backendAccountRepository != null,
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
                        selectedAppTextSize = selectedAppTextSize,
                        dayBoundaryHour = dayBoundaryHour,
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
                        onNavigateToAccount = { currentScreen = Screen.ME_ACCOUNT },
                        usageAccessGranted = effectiveUsageAccessStatus == UsageAccessStatus.GRANTED,
                        accessibilityServiceEnabled = effectiveAccessibilityServiceEnabled,
                        isAutoStartDismissed = isAutoStartDismissed,
                        isIgnoringBattery = isIgnoringBattery,
                        notificationPermissionGranted = notificationPermissionGranted,
                        dismissedPermissionPrompts = dismissedPermissionPrompts,
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
                        onNavigateToCheckInOverview = { currentScreen = Screen.CHECK_IN_OVERVIEW },
                        onNavigateToSavedProgressStats = { currentScreen = Screen.ME_SAVED_PROGRESS_STATS },
                        onNavigateToPointsProgressStats = { currentScreen = Screen.ME_POINTS_PROGRESS_STATS },
                        onNavigateToThemeSettings = { currentScreen = Screen.THEME },
                        onNavigateToAppearanceSettings = { currentScreen = Screen.ME_APPEARANCE },
                        onNavigateToLanguageSettings = { currentScreen = Screen.LANGUAGE },
                        onNavigateToDayBoundarySettings = { currentScreen = Screen.ME_DAY_BOUNDARY },
                        onNavigateToOfflineFocusSettings = {
                            if (proEntitlement.isProActive) {
                                currentScreen = Screen.ME_OFFLINE_FOCUS
                            } else {
                                proUpsellSource = ProUpsellSource.FOCUS_MODE
                            }
                        },
                        onNavigateToHelpFeedback = { currentScreen = Screen.HELP_FEEDBACK },
                        onNavigateToContactUs = { currentScreen = Screen.CONTACT_US },
                        onNavigateToSpecialAppSettings = { currentScreen = Screen.SPECIAL_APPS },
                        onNavigateToDataPrivacy = { currentScreen = Screen.ME_DATA_PRIVACY },
                        onNavigateToVersionInfo = { currentScreen = Screen.ME_VERSION },
                        onSignInWithGoogle = {
                            val activity = hostActivity
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
                                if (backendAccount?.isRegistered == true) {
                                    backendAccountRepository
                                        ?.signOut()
                                        ?.onSuccess {
                                            preferences.setProfileDisplayName(null)
                                            preferences.setProfileAvatarUri(null)
                                            snackbarHostState.showSnackbar(AppText.t("home_signed_out"))
                                        }
                                        ?.onFailure { error ->
                                            snackbarHostState.showSnackbar(accountErrorMessage(error))
                                        }
                                } else {
                                    authRepository.signOut()
                                    snackbarHostState.showSnackbar(AppText.t("home_signed_out"))
                                }
                            }
                        },
                        onDeleteAccount = { clearLocalData ->
                            coroutineScope.launch {
                                val backendDeletion =
                                    backendAccountRepository?.deleteAccount()
                                        ?: chinaSubscriptionRepository?.deleteAccount()
                                if (backendDeletion?.isFailure == true) {
                                    snackbarHostState.showSnackbar(AppText.t("home_account_deletion_failed"))
                                    return@launch
                                }
                                chinaSubscriptionRepository?.clearLocalState()
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
                            val activity = hostActivity
                            if (activity == null) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(AppText.t("home_purchase_cannot_start_from_this_screen"))
                                }
                            } else {
                                coroutineScope.launch {
                                    subscriptionRepository.purchase(activity, offer, userSession?.userId)
                                        .onSuccess {
                                            snackbarHostState.showSnackbar(AppText.t("payment_pro_activated"))
                                        }
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
                                chinaSubscriptionRepository
                                    ?.activate(localUserId, code)
                                    ?.onSuccess {
                                        snackbarHostState.showSnackbar(AppText.t("activation_pro_activated"))
                                    }
                                    ?.onFailure {
                                        snackbarHostState.showSnackbar(
                                            it.message ?: AppText.t("activation_code_invalid"),
                                        )
                                    }
                            }
                        },
                        )
                    }
                }
                Screen.ME_ACCOUNT -> {
                    AccountCenterScreen(
                        account = backendAccount,
                        profileDisplayName = profileDisplayName,
                        profileAvatarUri = profileAvatarUri,
                        isBusy = accountOperationInProgress,
                        onBack = { currentScreen = Screen.ME },
                        onRegister = { email, password, displayName ->
                            val repository = backendAccountRepository
                            if (repository != null) {
                                coroutineScope.launch {
                                    accountOperationInProgress = true
                                    repository.register(email, password, displayName)
                                        .onSuccess { account ->
                                            preferences.setProfileDisplayName(account.displayName)
                                            preferences.setProfileAvatarUri(account.avatarUrl)
                                            subscriptionRepository.refresh()
                                            snackbarHostState.showSnackbar(AppText.t("account_register_success"))
                                        }
                                        .onFailure { error ->
                                            snackbarHostState.showSnackbar(accountErrorMessage(error))
                                        }
                                    accountOperationInProgress = false
                                }
                            }
                        },
                        onLogin = { email, password ->
                            val repository = backendAccountRepository
                            if (repository != null) {
                                coroutineScope.launch {
                                    accountOperationInProgress = true
                                    repository.login(email, password)
                                        .onSuccess { account ->
                                            preferences.setProfileDisplayName(account.displayName)
                                            preferences.setProfileAvatarUri(account.avatarUrl)
                                            subscriptionRepository.refresh()
                                            snackbarHostState.showSnackbar(AppText.t("account_login_success"))
                                        }
                                        .onFailure { error ->
                                            snackbarHostState.showSnackbar(accountErrorMessage(error))
                                        }
                                    accountOperationInProgress = false
                                }
                            }
                        },
                        onRequestEmailVerification = {
                            val repository = backendAccountRepository
                            if (repository != null) {
                                coroutineScope.launch {
                                    accountOperationInProgress = true
                                    repository.requestEmailVerification()
                                        .onSuccess {
                                            snackbarHostState.showSnackbar(
                                                AppText.t("account_verification_code_sent"),
                                            )
                                        }
                                        .onFailure { error ->
                                            snackbarHostState.showSnackbar(accountErrorMessage(error))
                                        }
                                    accountOperationInProgress = false
                                }
                            }
                        },
                        onConfirmEmailVerification = { code ->
                            val repository = backendAccountRepository
                            if (repository != null) {
                                coroutineScope.launch {
                                    accountOperationInProgress = true
                                    repository.confirmEmailVerification(code)
                                        .onSuccess {
                                            snackbarHostState.showSnackbar(
                                                AppText.t("account_email_verified_success"),
                                            )
                                        }
                                        .onFailure { error ->
                                            snackbarHostState.showSnackbar(accountErrorMessage(error))
                                        }
                                    accountOperationInProgress = false
                                }
                            }
                        },
                        onRequestPasswordReset = { email ->
                            val repository = backendAccountRepository
                            if (repository != null) {
                                coroutineScope.launch {
                                    accountOperationInProgress = true
                                    repository.requestPasswordReset(email)
                                        .onSuccess {
                                            snackbarHostState.showSnackbar(
                                                AppText.t("account_reset_request_accepted"),
                                            )
                                        }
                                        .onFailure { error ->
                                            snackbarHostState.showSnackbar(accountErrorMessage(error))
                                        }
                                    accountOperationInProgress = false
                                }
                            }
                        },
                        onConfirmPasswordReset = { email, code, newPassword ->
                            val repository = backendAccountRepository
                            if (repository != null) {
                                coroutineScope.launch {
                                    accountOperationInProgress = true
                                    repository.confirmPasswordReset(email, code, newPassword)
                                        .onSuccess {
                                            snackbarHostState.showSnackbar(
                                                AppText.t("account_password_reset_success"),
                                            )
                                        }
                                        .onFailure { error ->
                                            snackbarHostState.showSnackbar(accountErrorMessage(error))
                                        }
                                    accountOperationInProgress = false
                                }
                            }
                        },
                        onUpdateProfileName = { displayName ->
                            coroutineScope.launch {
                                if (backendAccount?.isRegistered == true) {
                                    val repository = backendAccountRepository ?: return@launch
                                    accountOperationInProgress = true
                                    repository.updateDisplayName(displayName)
                                        .onSuccess { account ->
                                            preferences.setProfileDisplayName(account.displayName)
                                        }
                                        .onFailure { error ->
                                            snackbarHostState.showSnackbar(accountErrorMessage(error))
                                        }
                                    accountOperationInProgress = false
                                } else {
                                    preferences.setProfileDisplayName(displayName.trim())
                                }
                            }
                        },
                        onUpdateProfileAvatar = { avatarUri ->
                            coroutineScope.launch {
                                if (backendAccount?.isRegistered == true) {
                                    val repository = backendAccountRepository ?: return@launch
                                    accountOperationInProgress = true
                                    repository.uploadAvatar(context.contentResolver, Uri.parse(avatarUri))
                                        .onSuccess { account ->
                                            preferences.setProfileAvatarUri(account.avatarUrl)
                                        }
                                        .onFailure { error ->
                                            snackbarHostState.showSnackbar(accountErrorMessage(error))
                                        }
                                    accountOperationInProgress = false
                                } else {
                                    preferences.setProfileAvatarUri(avatarUri)
                                }
                            }
                        },
                        onSignOut = {
                            val repository = backendAccountRepository
                            if (repository != null) {
                                coroutineScope.launch {
                                    accountOperationInProgress = true
                                    repository.signOut()
                                        .onSuccess {
                                            preferences.setProfileDisplayName(null)
                                            preferences.setProfileAvatarUri(null)
                                            subscriptionRepository.refresh()
                                            snackbarHostState.showSnackbar(AppText.t("home_signed_out"))
                                        }
                                        .onFailure { error ->
                                            snackbarHostState.showSnackbar(accountErrorMessage(error))
                                        }
                                    accountOperationInProgress = false
                                }
                            }
                        },
                        onDeleteAccount = {
                            val repository = backendAccountRepository
                            if (repository != null) {
                                coroutineScope.launch {
                                    accountOperationInProgress = true
                                    repository.deleteAccount()
                                        .onSuccess {
                                            chinaSubscriptionRepository?.clearLocalState()
                                            authRepository.deleteAccount()
                                            val replacementSession = authRepository.ensureLocalSession()
                                            repository.initialize(replacementSession.userId)
                                                .onFailure { error ->
                                                    snackbarHostState.showSnackbar(accountErrorMessage(error))
                                                }
                                            chinaSubscriptionRepository?.bindUser(replacementSession.userId)
                                            preferences.setProfileDisplayName(null)
                                            preferences.setProfileAvatarUri(null)
                                            currentScreen = Screen.ME
                                            snackbarHostState.showSnackbar(AppText.t("home_account_deleted"))
                                        }
                                        .onFailure { error ->
                                            snackbarHostState.showSnackbar(accountErrorMessage(error))
                                        }
                                    accountOperationInProgress = false
                                }
                            }
                        },
                    )
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
                            val activity = hostActivity
                            if (activity == null) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(AppText.t("home_purchase_cannot_start_from_this_screen"))
                                }
                            } else {
                                coroutineScope.launch {
                                    subscriptionRepository.purchase(activity, offer, userSession?.userId)
                                        .onSuccess {
                                            snackbarHostState.showSnackbar(AppText.t("payment_pro_activated"))
                                        }
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
                                chinaSubscriptionRepository
                                    ?.activate(localUserId, code)
                                    ?.onSuccess {
                                        snackbarHostState.showSnackbar(AppText.t("activation_pro_activated"))
                                    }
                                    ?.onFailure {
                                        snackbarHostState.showSnackbar(
                                            it.message ?: AppText.t("activation_code_invalid"),
                                        )
                                    }
                            }
                        },
                    )
                }
                Screen.CHECK_IN_OVERVIEW -> {
                    CheckInOverviewPage(
                        state = checkInMonthState,
                        onBack = { currentScreen = Screen.ME },
                        onMonthChange = { selectedCheckInMonthKey = it.toString() },
                    )
                }
                Screen.HOME_STEP_PROGRESS_STATS -> {
                    MeProgressStatsScreen(
                        metric = MeProgressMetric.STEPS,
                        archives = historicalArchives,
                        pointDailyStats = pointDailyStats,
                        pointSpendRecords = pointSpendRecords,
                        currentPointsBalance = userPoints,
                        stepDays = historicalStepDays,
                        stepTarget = stepPointsRewardThreshold,
                        today = businessToday,
                        onBack = { currentScreen = Screen.HOME },
                    )
                }
                Screen.ME_SAVED_PROGRESS_STATS -> {
                    MeProgressStatsScreen(
                        metric = MeProgressMetric.SAVED_MINUTES,
                        archives = historicalArchives,
                        pointDailyStats = pointDailyStats,
                        pointSpendRecords = pointSpendRecords,
                        currentPointsBalance = userPoints,
                        today = businessToday,
                        onBack = { currentScreen = Screen.ME },
                    )
                }
                Screen.ME_POINTS_PROGRESS_STATS -> {
                    MeProgressStatsScreen(
                        metric = MeProgressMetric.EARNED_POINTS,
                        archives = historicalArchives,
                        pointDailyStats = pointDailyStats,
                        pointSpendRecords = pointSpendRecords,
                        currentPointsBalance = userPoints,
                        today = businessToday,
                        onBack = { currentScreen = Screen.ME },
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
                Screen.ME_DAY_BOUNDARY -> {
                    if (BuildConfig.DEBUG) {
                        DayBoundarySettingsPage(
                            currentHour = dayBoundaryHour,
                            isProActive = proEntitlement.isProActive,
                            onBack = { currentScreen = Screen.ME },
                            onSave = { hour ->
                                coroutineScope.launch {
                                    preferences.setDayBoundaryHour(hour)
                                    snackbarHostState.showSnackbar(AppText.t("day_boundary_settings_saved"))
                                }
                            },
                            onShowProUpsell = {
                                proUpsellSource = ProUpsellSource.DAY_BOUNDARY_CUSTOMIZATION
                            },
                        )
                    } else {
                        LaunchedEffect(Unit) { currentScreen = Screen.ME }
                    }
                }
                Screen.ME_OFFLINE_FOCUS -> {
                    if (!proEntitlement.isProActive) {
                        LaunchedEffect(Unit) {
                            currentScreen = Screen.ME
                            proUpsellSource = ProUpsellSource.FOCUS_MODE
                        }
                    } else {
                    OfflineFocusSettingsScreen(
                        categories = offlineFocusAllCategories,
                        installedApps = installedApps,
                        defaultCategoryId = offlineFocusDefaultCategoryId,
                        defaultDurationMinutes = offlineFocusDefaultDurationMinutes,
                        defaultMode = offlineFocusDefaultMode,
                        categoryDefaults = offlineFocusCategoryDefaults,
                        whitelistPackages = offlineFocusWhitelistPackages,
                        continueOnLock = offlineFocusContinueOnLock,
                        dailyPointCap = offlineFocusDailyPointCap,
                        restReminderEnabled = offlineFocusRestReminderEnabled,
                        restReminderMinutes = offlineFocusRestReminderMinutes,
                        restReminderSoundEnabled = offlineFocusRestReminderSoundEnabled,
                        restReminderVibrationEnabled = offlineFocusRestReminderVibrationEnabled,
                        restReminderRingtoneUri = offlineFocusRestReminderRingtoneUri,
                        onBack = { currentScreen = Screen.ME },
                        onSelectDefaultCategory = { categoryId ->
                            coroutineScope.launch {
                                preferences.setOfflineFocusDefaultCategoryId(categoryId)
                                snackbarHostState.showSnackbar(AppText.t("offline_focus_settings_saved"))
                            }
                        },
                        onSelectDefaultDuration = { minutes ->
                            coroutineScope.launch {
                                preferences.setOfflineFocusDefaultDurationMinutes(minutes)
                                snackbarHostState.showSnackbar(AppText.t("offline_focus_settings_saved"))
                            }
                        },
                        onSelectDefaultMode = { mode ->
                            coroutineScope.launch {
                                preferences.setOfflineFocusDefaultMode(mode)
                                snackbarHostState.showSnackbar(AppText.t("offline_focus_settings_saved"))
                            }
                        },
                        onSetWhitelistPackages = { packages ->
                            coroutineScope.launch {
                                preferences.setOfflineFocusWhitelistPackages(packages)
                                snackbarHostState.showSnackbar(AppText.t("offline_focus_settings_saved"))
                            }
                        },
                        onSetContinueOnLock = { enabled ->
                            coroutineScope.launch {
                                preferences.setOfflineFocusContinueOnLock(enabled)
                                snackbarHostState.showSnackbar(AppText.t("offline_focus_settings_saved"))
                            }
                        },
                        onSetDailyPointCap = { points ->
                            coroutineScope.launch {
                                preferences.setOfflineFocusDailyPointCap(points)
                                snackbarHostState.showSnackbar(AppText.t("offline_focus_settings_saved"))
                            }
                        },
                        onSetRestReminderEnabled = { enabled ->
                            coroutineScope.launch {
                                preferences.setOfflineFocusRestReminderEnabled(enabled)
                                snackbarHostState.showSnackbar(AppText.t("offline_focus_settings_saved"))
                            }
                        },
                        onSetRestReminderMinutes = { minutes ->
                            coroutineScope.launch {
                                preferences.setOfflineFocusRestReminderMinutes(minutes)
                                snackbarHostState.showSnackbar(AppText.t("offline_focus_settings_saved"))
                            }
                        },
                        onSetRestReminderSoundEnabled = { enabled ->
                            coroutineScope.launch {
                                preferences.setOfflineFocusRestReminderSoundEnabled(enabled)
                                snackbarHostState.showSnackbar(AppText.t("offline_focus_settings_saved"))
                            }
                        },
                        onSetRestReminderVibrationEnabled = { enabled ->
                            coroutineScope.launch {
                                preferences.setOfflineFocusRestReminderVibrationEnabled(enabled)
                                snackbarHostState.showSnackbar(AppText.t("offline_focus_settings_saved"))
                            }
                        },
                        onSetRestReminderRingtoneUri = { uri ->
                            coroutineScope.launch {
                                preferences.setOfflineFocusRestReminderRingtoneUri(uri)
                                snackbarHostState.showSnackbar(AppText.t("offline_focus_settings_saved"))
                            }
                        },
                        onUpsertCategory = { categoryId, name, iconKey, customIconPath, colorArgb, pointsPerMinute ->
                            coroutineScope.launch {
                                offlineFocusRepository.upsertCategory(
                                    categoryId = categoryId,
                                    name = name,
                                    iconKey = iconKey,
                                    customIconPath = customIconPath,
                                    colorArgb = colorArgb,
                                    pointsPerMinute = pointsPerMinute,
                                )
                                snackbarHostState.showSnackbar(AppText.t("offline_focus_settings_saved"))
                            }
                        },
                        onImportCategoryIcon = { categoryId, uri ->
                            coroutineScope.launch {
                                offlineFocusRepository.importCategoryIcon(categoryId, uri)
                                snackbarHostState.showSnackbar(AppText.t("offline_focus_settings_saved"))
                            }
                        },
                        onMoveCategory = { categoryId, direction ->
                            coroutineScope.launch {
                                offlineFocusRepository.moveCategory(categoryId, direction)
                                snackbarHostState.showSnackbar(AppText.t("offline_focus_settings_saved"))
                            }
                        },
                        onSetCategoryArchived = { categoryId, archived ->
                            coroutineScope.launch {
                                offlineFocusRepository.setCategoryArchived(categoryId, archived)
                                snackbarHostState.showSnackbar(AppText.t("offline_focus_settings_saved"))
                            }
                        },
                        onDeleteCategory = { categoryId ->
                            coroutineScope.launch {
                                offlineFocusRepository.deleteCategory(categoryId)
                                snackbarHostState.showSnackbar(AppText.t("offline_focus_settings_saved"))
                            }
                        },
                    )
                    }
                }
                Screen.ME_APPEARANCE -> {
                    AppearanceSettingsScreen(
                        isProActive = proEntitlement.isProActive,
                        selectedAppTextSize = selectedAppTextSize,
                        onBack = { currentScreen = Screen.ME },
                        onSelectAppTextSize = { textSize ->
                            coroutineScope.launch {
                                preferences.setSelectedAppTextSize(textSize)
                            }
                        },
                        onOpenRingSettings = { currentScreen = Screen.ME_RING_SETTINGS },
                        onShowProUpsell = { proUpsellSource = it },
                    )
                }
                Screen.ME_RING_SETTINGS -> {
                    if (!proEntitlement.isProActive) {
                        LaunchedEffect(Unit) {
                            currentScreen = Screen.ME_APPEARANCE
                            proUpsellSource = ProUpsellSource.RING_SETTINGS
                        }
                    } else {
                        RingSettingsScreen(
                            ringPreferences = homeActivityRingPreferences,
                            ringColorPreferences = homeActivityRingColorPreferences,
                            offlineFocusDailyTargetMinutes = offlineFocusDailyTargetMinutes,
                            onBack = { currentScreen = Screen.ME_APPEARANCE },
                            onSelectRingMetric = { slot, metric ->
                                coroutineScope.launch {
                                    preferences.setHomeActivityRingMetric(slot, metric)
                                    snackbarHostState.showSnackbar(AppText.t("ring_settings_saved"))
                                }
                            },
                            onSelectRingMetricColor = { metric, source, customArgb ->
                                coroutineScope.launch {
                                    preferences.setHomeActivityRingMetricColor(metric, source, customArgb)
                                    snackbarHostState.showSnackbar(AppText.t("ring_settings_saved"))
                                }
                            },
                            onSaveOfflineFocusDailyTarget = { minutes ->
                                coroutineScope.launch {
                                    preferences.setOfflineFocusDailyTargetMinutes(minutes)
                                    snackbarHostState.showSnackbar(AppText.t("ring_settings_saved"))
                                }
                            },
                        )
                    }
                }
                Screen.ME_DATA_PRIVACY -> {
                    DataPrivacyPage(
                        onBack = { currentScreen = Screen.ME },
                        onSaveLocalBackup = {
                            runCatching {
                                backupSaveLauncher.launch(defaultBackupFileName())
                            }.onFailure {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(AppText.t("home_export_local_data_failed"))
                                }
                            }
                        },
                        onShareLocalBackup = {
                            coroutineScope.launch {
                                runCatching {
                                    val file = localDataManager.exportLocalBackup()
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file,
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/zip"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(Intent.EXTRA_TITLE, AppText.t("me_export_recoverable_backup"))
                                        clipData = ClipData.newUri(
                                            context.contentResolver,
                                            AppText.t("me_export_recoverable_backup"),
                                            uri,
                                        )
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivityKeepingCurrentTask(
                                        Intent.createChooser(intent, AppText.t("me_export_recoverable_backup")),
                                    )
                                }.onSuccess {
                                    // The share sheet is now open; no snackbar needed.
                                }.onFailure {
                                    snackbarHostState.showSnackbar(AppText.t("home_export_local_data_failed"))
                                }
                            }
                        },
                        onImportLocalBackup = {
                            backupImportLauncher.launch(arrayOf("application/zip", "*/*"))
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
                        onOpenMediaApps = { currentScreen = Screen.MEDIA_APPS },
                        onOpenLockScreenTimerApps = { currentScreen = Screen.LOCK_SCREEN_TIMER_APPS },
                        onOpenAppColors = { currentScreen = Screen.APP_COLOR_DEBUG },
                        isProActive = proEntitlement.isProActive,
                        onOpenProMembership = { currentScreen = Screen.ME_PRO },
                    )
                }
                Screen.WEREAD_SPECIAL_APP -> {
                    SpecialAppSettingsScreen(
                        onBack = { currentScreen = Screen.SPECIAL_APPS },
                    )
                }
                Screen.MEDIA_APPS -> {
                    MediaAppSettingsScreen(
                        onBack = { currentScreen = Screen.SPECIAL_APPS },
                    )
                }
                Screen.LOCK_SCREEN_TIMER_APPS -> {
                    LockScreenTimerAppSettingsScreen(
                        onBack = { currentScreen = Screen.SPECIAL_APPS },
                    )
                }
                Screen.APP_COLOR_DEBUG -> {
                    AppColorDebugScreen(
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
                                    archiveCount = historicalArchives.size,
                                    latestArchiveDate = historicalArchives.maxByOrNull { it.archiveDate }?.archiveDate,
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
                        onReplayTutorial = {
                            currentScreen = Screen.HOME
                            showFirstRunCoachmark = true
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
                                newlyUnlockedAchievement = sampleAchievement.copy(
                                    isUnlocked = true,
                                    unlockedAt = System.currentTimeMillis(),
                                )
                            }
                        },
                        onResetSummary = { coroutineScope.launch { preferences.setLastSummaryShownDate("reset") } },
                        onTriggerSummary = {
                            coroutineScope.launch {
                                yesterdayReportState =
                                    buildYesterdayReportUiState(
                                        archiveRepository = dailyArchiveRepository,
                                        reportDate = BusinessDay.today(ZoneId.systemDefault(), BusinessDay.cachedStartHour()).minusDays(1),
                                        context = context,
                                    )
                                if (yesterdayReportState == null) {
                                    snackbarHostState.showSnackbar(AppText.t("home_yesterday_report_unavailable"))
                                }
                            }
                        },
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
                        onOpenFocusHistoryEditor = { currentScreen = Screen.LAB_FOCUS_HISTORY_EDITOR },
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
                Screen.LAB_FOCUS_HISTORY_EDITOR -> {
                    OfflineFocusHistoryEditorScreen(
                        categories = offlineFocusAllCategories.filterNot { it.isDeleted },
                        onCreate = { input ->
                            coroutineScope.launch {
                                offlineFocusRepository.createDebugSession(input)
                                snackbarHostState.showSnackbar(AppText.t("lab_focus_history_created"))
                                currentScreen = Screen.LABORATORY
                            }
                        },
                        onBack = { currentScreen = Screen.LABORATORY },
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

    completedOfflineFocusSession?.let { session ->
        OfflineFocusCompletionDialog(
            session = session,
            restReminderEnabled = offlineFocusRestReminderEnabled,
            restReminderMinutes = offlineFocusRestReminderMinutes,
            restReminderSoundEnabled = offlineFocusRestReminderSoundEnabled,
            restReminderVibrationEnabled = offlineFocusRestReminderVibrationEnabled,
            restReminderRingtoneUri = offlineFocusRestReminderRingtoneUri,
            onDismiss = {
                OfflineFocusTimerService.dismissCompletionAlert(context)
                localCompletedOfflineFocusSessionId = null
                onCompletedOfflineFocusConsumed()
            },
            onSetRestReminderEnabled = { enabled ->
                coroutineScope.launch { preferences.setOfflineFocusRestReminderEnabled(enabled) }
            },
            onSetRestReminderSoundEnabled = { enabled ->
                coroutineScope.launch { preferences.setOfflineFocusRestReminderSoundEnabled(enabled) }
            },
            onSetRestReminderVibrationEnabled = { enabled ->
                coroutineScope.launch { preferences.setOfflineFocusRestReminderVibrationEnabled(enabled) }
            },
            onSetRestReminderRingtoneUri = { uri ->
                coroutineScope.launch { preferences.setOfflineFocusRestReminderRingtoneUri(uri) }
            },
            onAdjustEndEarlier = {
                Unit
            },
            onUserInteraction = {
                OfflineFocusTimerService.stopCompletionSignal(context)
            },
            onStartAgain = {
                coroutineScope.launch {
                    if (!proEntitlement.isProActive) {
                        proUpsellSource = ProUpsellSource.FOCUS_MODE
                        return@launch
                    }
                    val nextSession =
                        offlineFocusRepository.startSession(
                            categoryId = session.categoryId,
                            durationMinutes =
                                if (session.plannedDurationMillis <= 0L) {
                                    ManagedAppPreferences.UNLIMITED_OFFLINE_FOCUS_DURATION_MINUTES
                                } else {
                                    (session.plannedDurationMillis / 60_000L).toInt().coerceAtLeast(1)
                                },
                            focusMode = session.focusMode,
                        ) ?: run {
                            snackbarHostState.showSnackbar(AppText.t("offline_focus_category_empty_start_hint"))
                            return@launch
                        }
                    preferences.setOfflineFocusDefaultMode(session.focusMode)
                    OfflineFocusTimerService.dismissCompletionAlert(context)
                    localCompletedOfflineFocusSessionId = null
                    onCompletedOfflineFocusConsumed()
                    OfflineFocusTimerService.start(context, nextSession.id)
                }
            },
        )
    }

    yesterdayReportState?.takeIf { !showWelcomeIntro && !showFirstRunCoachmark }?.let { report ->
        YesterdayReportDialog(
            state = report,
            todayState = checkInTodayState,
            onDismiss = { yesterdayReportState = null },
            onCheckInToday = performTodayCheckIn,
            onOpenCheckInOverview = {
                yesterdayReportState = null
                currentScreen = Screen.CHECK_IN_OVERVIEW
            },
            onViewDailyReport = {
                yesterdayReportState = null
                currentScreen = Screen.STATS
            },
        )
    }

    newlyUnlockedAchievement?.let { achievement ->
        AchievementUnlockDialog(
            achievement = achievement,
            onDismiss = { newlyUnlockedAchievement = null },
            onViewAchievements = {
                newlyUnlockedAchievement = null
                rewardsSection = RewardsSection.ACHIEVEMENTS
                currentScreen = Screen.REWARDS
            },
        )
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
                        SensitivePermissionDisclosure.ACTIVITY_RECOGNITION -> {
                            pendingSensitiveDisclosure = null
                            healthConnectStepsAvailable = stepTrackingRepository.isHealthConnectAvailable()
                            healthConnectStepsPermissionGranted = stepTrackingRepository.hasHealthConnectStepPermission()
                            if (healthConnectStepsAvailable && !healthConnectStepsPermissionGranted) {
                                healthConnectPermissionLauncher.launch(HealthConnectStepDataSource.STEP_PERMISSIONS)
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                activityRecognitionPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                            } else {
                                activityRecognitionPermissionGranted = true
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

private const val PRIVACY_POLICY_URL = "https://tinyvow.rorolo.com/privacy/"

private fun defaultBackupFileName(): String =
    "tinyvow-local-backup-${System.currentTimeMillis()}.zip"

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

private fun Context.restartAppAfterRestore() {
    val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
    if (launchIntent != null) {
        val restartIntent = launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent = PendingIntent.getActivity(
            this,
            9086,
            restartIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        getSystemService(AlarmManager::class.java)
            ?.set(AlarmManager.RTC, System.currentTimeMillis() + 200L, pendingIntent)
    }
    findActivity()?.finishAffinity()
    exitProcess(0)
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
private fun AchievementUnlockDialog(
    achievement: AchievementEntity,
    onDismiss: () -> Unit,
    onViewAchievements: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val tierLabel = when (achievement.tier) {
        AchievementTier.LEGENDARY -> AppText.t("home_legendary_achievement_unlocked")
        AchievementTier.DIAMOND -> AppText.t("home_diamond_achievement_unlocked")
        AchievementTier.GOLD -> AppText.t("home_gold_achievement_unlocked")
        AchievementTier.SILVER -> AppText.t("home_silver_achievement_unlocked")
        else -> AppText.t("home_bronze_achievement_unlocked")
    }
    
    Dialog(onDismissRequest = onDismiss) {
        TinyVowCard(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = TinyVowElevation.FeaturedCard,
            tonalElevation = TinyVowElevation.FeaturedCard,
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = TinyVowSpacing.CardHorizontal,
                    vertical = TinyVowSpacing.CardVertical,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AchievementBadge(
                    achievement = achievement,
                    modifier = Modifier.size(72.dp),
                    animated = true,
                )
                Spacer(modifier = Modifier.height(TinyVowSpacing.CardGap))
                Text(
                    tierLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = themeColors.encourage,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    achievement.localizedAchievementTitle(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    achievement.localizedAchievementDescription(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(TinyVowSpacing.SectionGap))
                TinyVowButton(
                    text = AppText.t("achievement_unlock_view_action"),
                    onClick = onViewAchievements,
                    modifier = Modifier.fillMaxWidth(),
                    tone = TinyVowButtonTone.Primary,
                )
                Spacer(modifier = Modifier.height(TinyVowSpacing.CardGap))
                TinyVowButton(
                    text = AppText.t("achievement_unlock_dismiss_action"),
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                )
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
        SensitivePermissionDisclosure.ACTIVITY_RECOGNITION -> {
            AppText.t("home_step_health_permission_disclosure") to
                AppText.t("home_tiny_vow_uses_health_steps_first")
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
    groupsWithAppsLoaded: Boolean = true,
    dayBoundaryHour: Int = BusinessDay.DEFAULT_START_HOUR,
    activeRewardEffects: List<ActiveRewardEffectEntity>,
    activeBonusMinutesByGroup: Map<String, Int> = emptyMap(),
    appIconCache: Map<String, Drawable> = emptyMap(),
    onAppIconLoaded: (String, Drawable) -> Unit = { _, _ -> },
    userPoints: Double,
    todayPoints: Double,
    todayEarnedPoints: Double = todayPoints,
    todayStepCount: Int = 0,
    todayStepPoints: Double = 0.0,
    stepPointsPerStep: Double = StepTrackingRepository.DEFAULT_POINTS_PER_STEP,
    stepPointsRewardThreshold: Int = StepTrackingRepository.DEFAULT_REWARD_THRESHOLD,
    homeActivityRingPreferences: HomeActivityRingPreferences = HomeActivityRingPreferences(),
    homeActivityRingColorPreferences: HomeActivityRingColorPreferences = HomeActivityRingColorPreferences(),
    isStepCounterAvailable: Boolean = false,
    isActivityRecognitionPermissionGranted: Boolean = false,
    offlineFocusCategories: List<OfflineFocusCategory> = emptyList(),
    offlineFocusActiveSession: OfflineFocusSession? = null,
    offlineFocusDetailRequestToken: Int = 0,
    offlineFocusTodaySummary: OfflineFocusTodaySummary = OfflineFocusTodaySummary(),
    offlineFocusDefaultCategoryId: String? = null,
    offlineFocusDefaultDurationMinutes: Int = ManagedAppPreferences.DEFAULT_OFFLINE_FOCUS_DURATION_MINUTES,
    offlineFocusDefaultMode: OfflineFocusMode = OfflineFocusMode.NORMAL,
    offlineFocusRestReminderEnabled: Boolean = true,
    offlineFocusRestReminderMinutes: Int = ManagedAppPreferences.DEFAULT_OFFLINE_FOCUS_REST_REMINDER_MINUTES,
    offlineFocusCategoryDefaults: Map<String, OfflineFocusCategoryDefaults> = emptyMap(),
    offlineFocusDailyTargetMinutes: Int = ManagedAppPreferences.DEFAULT_OFFLINE_FOCUS_DAILY_TARGET_MINUTES,
    offlineFocusRulesAvailable: Boolean = false,
    onSetOfflineFocusRestReminderEnabled: (Boolean) -> Unit = {},
    onSetOfflineFocusRestReminderMinutes: (Int) -> Unit = {},
    onStartOfflineFocus: (String, Int, OfflineFocusMode) -> Unit = { _, _, _ -> },
    onOpenOfflineFocusRulesSettings: () -> Unit = {},
    onAllowOfflineFocusViolationPackage: (String) -> Unit = {},
    onUpsertOfflineFocusCategory: (String?, String, String, String?, Int, Double) -> Unit = { _, _, _, _, _, _ -> },
    onFinishOfflineFocusEarly: (String) -> Unit = {},
    onAbandonOfflineFocus: (String) -> Unit = {},
    overviewInputsReady: Boolean = true,
    overviewRuntimeState: HomeOverviewRuntimeState = HomeOverviewRuntimeState(),
    onOverviewRuntimeStateChange: (HomeOverviewRuntimeState) -> Unit = {},
    overviewAnimationReplayToken: Int = 0,
    shouldPlayOverviewDataReveal: Boolean = false,
    onOverviewDataRevealStarted: () -> Unit = {},
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
    onRequestActivityRecognitionPermission: () -> Unit = {},
    onOpenStepProComparison: () -> Unit = {},
    onOpenStepStats: () -> Unit = {},
    onRefreshStepData: () -> Unit = {},
    onSaveStepPointsPerStep: (Double) -> Unit = {},
    onSaveStepPointsRewardThreshold: (Int) -> Unit = {},
    onSaveGroup: (
        id: String?,
        name: String,
        limit: Int,
        type: GroupType,
        period: LimitPeriod,
        pts: Double,
        pkgs: List<String>,
    ) -> Unit,
    onDeleteGroup: (id: String) -> Unit,
    onGuardAction: (GuardedAction, () -> Unit) -> Unit,
    achievementProgress: AchievementProgress = AchievementProgress(),
    appLimitRepository: AppLimitRepository? = null,
    archiveRepository: DailyArchiveRepository? = null,
    historicalArchives: List<com.rrrrz.tinyvow.data.db.DailyArchiveEntity> = emptyList(),
    isProActive: Boolean,
    onShowProUpsell: (ProUpsellSource) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val coroutineScope = rememberCoroutineScope()
    val businessToday by
        produceState(initialValue = currentBusinessDay()) {
            while (true) {
                val latestToday = currentBusinessDay()
                if (value != latestToday) {
                    value = latestToday
                }
                delay(60_000L)
            }
        }
    val homeScrollState = rememberScrollState()
    val usageMap = overviewRuntimeState.usageMap
    val periodUsageMap = overviewRuntimeState.periodUsageMap
    var showStepPointsDialog by remember { mutableStateOf(false) }
    val todayAppUsageMap = overviewRuntimeState.todayAppUsageMap
    val todayAppOpenCountMap = overviewRuntimeState.todayAppOpenCountMap
    val todaySessions = overviewRuntimeState.todaySessions
    val isOverviewUsageReady = overviewRuntimeState.isUsageReady
    var createFirstVowRequest by remember { mutableIntStateOf(0) }
    var openBattleGroupDetailRequest by remember { mutableIntStateOf(0) }
    var openBattleGroupDetailGroup by remember { mutableStateOf<AppGroupWithApps?>(null) }
    var homeOverviewDetailKind by remember { mutableStateOf<HomeOverviewDetailKind?>(null) }
    var showHomeBehaviorRadarDialog by remember { mutableStateOf(false) }
    val recentGroupArchives =
        archiveRepository?.let { repository ->
            val today = businessToday
            val from = today.minusDays(7).toString()
            val to = today.minusDays(1).toString()
            val archives by repository.getGroupArchivesByRange(from, to).collectAsStateWithLifecycle(
                initialValue = emptyList(),
                lifecycle = lifecycle,
            )
            archives
        } ?: emptyList()
    val yesterdayArchiveDate = remember(businessToday) { businessToday.minusDays(1).toString() }
    val archiveState =
        archiveRepository?.let { repository ->
            val state by repository.observeArchiveState().collectAsStateWithLifecycle(
                initialValue = null,
                lifecycle = lifecycle,
            )
            state
        }
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
    val isYesterdayArchivePending =
        remember(
            archiveState,
            businessToday,
            yesterdayArchiveDate,
            yesterdayGroupArchives,
            yesterdayAppArchives,
            usageAccessStatus,
        ) {
            isHomeYesterdayArchivePending(
                archiveState = archiveState,
                today = businessToday,
                yesterdayArchiveDate = yesterdayArchiveDate,
                hasYesterdayData = yesterdayGroupArchives.isNotEmpty() || yesterdayAppArchives.isNotEmpty(),
                usageAccessStatus = usageAccessStatus,
            )
        }
    // Periodically refresh group usage by querying UsageStats once and aggregating packages in memory.
    LaunchedEffect(groupsWithApps, usageAccessStatus, dayBoundaryHour) {
        if (usageAccessStatus != UsageAccessStatus.GRANTED) {
            onOverviewRuntimeStateChange(HomeOverviewRuntimeState())
            return@LaunchedEffect
        }
        val usageRepo = MergedUsageRepository(context)
        while (true) {
            runCatching {
                val zoneId = ZoneId.systemDefault()
                val today = BusinessDay.today(zoneId, dayBoundaryHour)
                val todayStart = BusinessDay.startOfDayMillis(today, zoneId, dayBoundaryHour)
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
                onOverviewRuntimeStateChange(
                    HomeOverviewRuntimeState(
                        usageMap = newUsageMap,
                        periodUsageMap = newPeriodUsageMap,
                        todayAppUsageMap = newTodayAppUsageMap,
                        todayAppOpenCountMap = newTodayAppOpenCountMap,
                        todaySessions = newTodaySessions,
                        isUsageReady = true,
                    ),
                )
            }
            kotlinx.coroutines.delay(5000L)
        }
    }
    val usageAccessGranted = usageAccessStatus == UsageAccessStatus.GRANTED
    val statusColor = if (usageAccessGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val isOverviewReady = overviewInputsReady && isOverviewUsageReady
    val effectiveHomeActivityRingPreferences =
        if (isProActive) homeActivityRingPreferences else HomeActivityRingPreferences()

    val overviewState =
        remember(
            context,
            businessToday,
            groupsWithApps,
            usageMap,
            periodUsageMap,
            todayAppUsageMap,
            todayAppOpenCountMap,
            todaySessions,
            activeRewardEffects,
            activeBonusMinutesByGroup,
            recentGroupArchives,
            yesterdayGroupArchives,
            yesterdayAppArchives,
            historicalArchives,
            userPoints,
            todayPoints,
            todayEarnedPoints,
            todayStepCount,
            stepPointsRewardThreshold,
            effectiveHomeActivityRingPreferences,
            offlineFocusTodaySummary,
            offlineFocusDailyTargetMinutes,
            achievementProgress,
            isYesterdayArchivePending,
        ) {
            buildHomeOverviewUiState(
                context = context,
                today = businessToday,
                groupsWithApps = groupsWithApps,
                usageMap = usageMap,
                periodUsageMap = periodUsageMap,
                todayAppUsageMap = todayAppUsageMap,
                todayAppOpenCountMap = todayAppOpenCountMap,
                todaySessions = todaySessions,
                activeRewardEffects = activeRewardEffects,
                activeBonusMinutesByGroup = activeBonusMinutesByGroup,
                recentGroupArchives = recentGroupArchives,
                yesterdayGroupArchives = yesterdayGroupArchives,
                yesterdayAppArchives = yesterdayAppArchives,
                historicalArchives = historicalArchives,
                userPoints = userPoints,
                todayPoints = todayPoints,
                todayEarnedPoints = todayEarnedPoints,
                todayStepCount = todayStepCount,
                stepPointsRewardThreshold = stepPointsRewardThreshold,
                ringPreferences = effectiveHomeActivityRingPreferences,
                offlineFocusTodaySummary = offlineFocusTodaySummary,
                offlineFocusDailyTargetMinutes = offlineFocusDailyTargetMinutes,
                achievementProgress = achievementProgress,
                isYesterdayArchivePending = isYesterdayArchivePending,
            )
        }
    val battleActions =
        remember(
            overviewState.battleActions,
            usageAccessGranted,
            accessibilityServiceEnabled,
            dismissedPermissionPrompts,
        ) {
            buildList {
                if (!usageAccessGranted && PermissionPromptIds.USAGE_ACCESS !in dismissedPermissionPrompts) {
                    add(
                        HomeBattleAction(
                            type = HomeBattleActionType.PERMISSION_USAGE,
                            title = AppText.t("home_battle_permission_usage_title"),
                            subtitle = AppText.t("home_battle_permission_usage_body"),
                            value = AppText.t("home_battle_permission_fix"),
                            progress = 0f,
                        ),
                    )
                } else if (
                    !accessibilityServiceEnabled &&
                    PermissionPromptIds.ACCESSIBILITY !in dismissedPermissionPrompts
                ) {
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

    TinyVowPageBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(homeScrollState)
        ) {
            val startupDismissIds =
                startupReliabilityDismissIds(
                    snapshot = permissionReliabilitySnapshot,
                    dismissedPermissionPrompts = dismissedPermissionPrompts,
                )
            val showStartupReliabilityCard =
                groupsWithAppsLoaded && when (permissionReliabilitySnapshot.primaryStep) {
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
                    HomeOverviewPaperCard(
                        state = overviewState,
                        ringColorPreferences = homeActivityRingColorPreferences,
                        isDataReady = isOverviewReady,
                        overviewAnimationReplayToken = overviewAnimationReplayToken,
                        shouldPlayDataReveal = shouldPlayOverviewDataReveal,
                        onDataRevealStarted = onOverviewDataRevealStarted,
                        onOpenBehaviorRadar = { showHomeBehaviorRadarDialog = true },
                        onOpenControlDetail = { homeOverviewDetailKind = HomeOverviewDetailKind.CONTROL },
                        onOpenEncourageDetail = { homeOverviewDetailKind = HomeOverviewDetailKind.ENCOURAGE },
                        modifier = Modifier.fillMaxWidth(),
                    )
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

                if (usageAccessGranted && isProActive) {
                    val focusSessionForHome = offlineFocusActiveSession.takeIf { isProActive }
                    OfflineFocusHomeCard(
                        categories = offlineFocusCategories,
                        activeSession = focusSessionForHome,
                        detailRequestToken = offlineFocusDetailRequestToken,
                        todaySummary = offlineFocusTodaySummary,
                        dailyTargetMinutes = offlineFocusDailyTargetMinutes,
                        defaultCategoryId = offlineFocusDefaultCategoryId,
                        defaultDurationMinutes = offlineFocusDefaultDurationMinutes,
                        defaultMode = offlineFocusDefaultMode,
                        restReminderEnabled = offlineFocusRestReminderEnabled,
                        restReminderMinutes = offlineFocusRestReminderMinutes,
                        categoryDefaults = offlineFocusCategoryDefaults,
                        focusRulesAvailable = offlineFocusRulesAvailable,
                        isProActive = isProActive,
                        onStart = onStartOfflineFocus,
                        onSetRestReminderEnabled = onSetOfflineFocusRestReminderEnabled,
                        onSetRestReminderMinutes = { minutes ->
                            onSetOfflineFocusRestReminderMinutes(minutes)
                        },
                        onOpenFocusRulesSettings = onOpenOfflineFocusRulesSettings,
                        onLocked = { onShowProUpsell(ProUpsellSource.FOCUS_MODE) },
                        onUpsertCategory = onUpsertOfflineFocusCategory,
                        onFinishEarly = onFinishOfflineFocusEarly,
                        onPause = { sessionId ->
                            OfflineFocusTimerService.pause(context, sessionId)
                        },
                        onResume = { sessionId ->
                            OfflineFocusTimerService.resume(context, sessionId)
                        },
                        onAbandon = onAbandonOfflineFocus,
                        onAllowViolationPackage = onAllowOfflineFocusViolationPackage,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(HomeCompactCardHeight),
                    )
                }

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
                    state = overviewState,
                    onDismiss = { showHomeBehaviorRadarDialog = false },
                )
            }

            homeOverviewDetailKind?.let { kind ->
                HomeOverviewDetailSheet(
                    kind = kind,
                    state = overviewState,
                    onOpenGroup = { group ->
                        homeOverviewDetailKind = null
                        openBattleGroupDetailGroup = group
                        openBattleGroupDetailRequest += 1
                    },
                    onDismiss = { homeOverviewDetailKind = null },
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
                        periodUsageMap = periodUsageMap,
                        todayAppUsageMap = todayAppUsageMap,
                        todayAppOpenCountMap = todayAppOpenCountMap,
                        todaySessions = todaySessions,
                        todayStepCount = todayStepCount,
                        activeRewardEffects = activeRewardEffects,
                        appIconCache = appIconCache,
                        onAppIconLoaded = onAppIconLoaded,
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
private fun YesterdayReportDialog(
    state: YesterdayReportUiState,
    todayState: DailyCheckInTodayState,
    onDismiss: () -> Unit,
    onCheckInToday: () -> Unit,
    onOpenCheckInOverview: () -> Unit,
    onViewDailyReport: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .widthIn(max = 460.dp),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = AppText.t("home_yesterday_report_title"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.inkStrong,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = AppText.t("group_close"))
                    }
                }

                Text(
                    text = state.dateLabel,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                HomeBehaviorOverviewPanel(
                    metrics = state.scoreMetrics,
                    comparisonMetrics = state.comparisonScoreMetrics,
                    totalUsageMillis = state.totalUsageMillis,
                    controlUsageMillis = state.controlUsageMillis,
                    encourageUsageMillis = state.encourageUsageMillis,
                    savedMillis = state.savedMillis,
                    scaleToFit = true,
                )
                YesterdayReportCheckInAction(
                    checkedIn = todayState.checkedIn,
                    onCheckInToday = onCheckInToday,
                    onOpenCheckInOverview = onOpenCheckInOverview,
                )
                OutlinedButton(
                    onClick = onViewDailyReport,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(AppText.t("home_yesterday_report_view_daily"))
                }
            }
        }
    }
}

@Composable
private fun YesterdayReportCheckInAction(
    checkedIn: Boolean,
    onCheckInToday: () -> Unit,
    onOpenCheckInOverview: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = themeColors.baseContainer.copy(alpha = 0.54f),
        border = BorderStroke(1.dp, themeColors.base.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = AppText.t("checkin_report_card_title"),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.inkStrong,
            )
            Text(
                text = AppText.t("checkin_report_card_description"),
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.inkMuted,
            )
            Button(
                onClick = if (checkedIn) onOpenCheckInOverview else onCheckInToday,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (checkedIn) {
                        AppText.t("checkin_view")
                    } else {
                        AppText.t("checkin_report_action")
                    },
                )
            }
        }
    }
}

@Composable
private fun YesterdayReportPentagonCard(
    metrics: List<DailyBehaviorScoreMetric>,
    comparisonMetrics: List<DailyBehaviorScoreMetric>,
) {
    val themeColors = LocalThemeColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = AppText.t("home_yesterday_report_five_star_title"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.inkStrong,
                    )
                    Text(
                        text = AppText.t("home_yesterday_report_five_star_subtitle"),
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.inkMuted,
                    )
                }
                YesterdayReportOverallChip(metrics)
            }
            YesterdayReportPentagonChart(
                metrics = metrics,
                comparisonMetrics = comparisonMetrics,
                modifier = Modifier.fillMaxWidth().height(260.dp),
            )
        }
    }
}

@Composable
private fun YesterdayReportOverallChip(metrics: List<DailyBehaviorScoreMetric>) {
    val score =
        metrics
            .takeIf { it.isNotEmpty() }
            ?.map { it.score }
            ?.average()
            ?.roundToInt()
            ?: 0
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = AppText.t("stats_score_overall"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun YesterdayReportPentagonChart(
    metrics: List<DailyBehaviorScoreMetric>,
    comparisonMetrics: List<DailyBehaviorScoreMetric>,
    modifier: Modifier = Modifier,
) {
    val displayMetrics = metrics.take(5)
    val comparisonByLabel = comparisonMetrics.associateBy { it.label }
    val displayComparisonMetrics = displayMetrics.mapNotNull { comparisonByLabel[it.label] }
    val primary = MaterialTheme.colorScheme.primary
    val radarLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.46f)
    val radarAxisColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.34f)
    val previousColor = LocalThemeColors.current.inkMuted
    var selectedMetric by remember { mutableStateOf<DailyBehaviorScoreMetric?>(null) }
    val overall =
        displayMetrics
            .takeIf { it.isNotEmpty() }
            ?.map { it.score }
            ?.average()
            ?.roundToInt()
            ?: 0

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(218.dp)) {
            if (displayMetrics.size < 3) return@Canvas
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension * 0.42f
            val axisCount = displayMetrics.size
            fun pointFor(index: Int, ratio: Float): Offset {
                val angle = -PI.toFloat() / 2f + (2f * PI.toFloat() * index / axisCount)
                return Offset(
                    x = center.x + cos(angle) * radius * ratio,
                    y = center.y + sin(angle) * radius * ratio,
                )
            }
            fun pathFor(scoreMetrics: List<DailyBehaviorScoreMetric>): Path =
                Path().apply {
                    scoreMetrics.forEachIndexed { index, metric ->
                        val point = pointFor(index, (metric.score / 100f).coerceIn(0.08f, 1f))
                        if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                    }
                    close()
                }
            repeat(4) { ringIndex ->
                val ringPath = Path()
                val ratio = (ringIndex + 1) / 4f
                repeat(axisCount) { index ->
                    val point = pointFor(index, ratio)
                    if (index == 0) ringPath.moveTo(point.x, point.y) else ringPath.lineTo(point.x, point.y)
                }
                ringPath.close()
                drawPath(
                    path = ringPath,
                    color = radarLineColor,
                    style = Stroke(width = 1.8f),
                )
            }
            repeat(axisCount) { index ->
                drawLine(
                    color = radarAxisColor,
                    start = center,
                    end = pointFor(index, 1f),
                    strokeWidth = 1.4f,
                )
            }
            if (displayComparisonMetrics.size == displayMetrics.size) {
                val previousPath = pathFor(displayComparisonMetrics)
                drawPath(path = previousPath, color = previousColor.copy(alpha = 0.12f))
                drawPath(path = previousPath, color = previousColor.copy(alpha = 0.42f), style = Stroke(width = 3f))
            }
            val currentPath = pathFor(displayMetrics)
            drawPath(path = currentPath, color = primary.copy(alpha = 0.20f))
            drawPath(path = currentPath, color = primary.copy(alpha = 0.58f), style = Stroke(width = 3.5f))
            displayMetrics.forEachIndexed { index, metric ->
                val point = pointFor(index, (metric.score / 100f).coerceIn(0.08f, 1f))
                drawCircle(
                    color = behaviorScoreAccentColor(metric.accentIndex),
                    radius = 7.5f,
                    center = point,
                )
            }
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, primary.copy(alpha = 0.26f)),
        ) {
            Column(
                modifier = Modifier.size(76.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = overall.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = primary,
                )
                Text(
                    text = AppText.t("stats_score_overall"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        YesterdayReportVertexLabel(displayMetrics.getOrNull(0), Alignment.TopCenter, 0.dp, (-2).dp) { selectedMetric = it }
        YesterdayReportVertexLabel(displayMetrics.getOrNull(1), Alignment.CenterEnd, 0.dp, (-44).dp) { selectedMetric = it }
        YesterdayReportVertexLabel(displayMetrics.getOrNull(2), Alignment.BottomEnd, (-18).dp, (-6).dp) { selectedMetric = it }
        YesterdayReportVertexLabel(displayMetrics.getOrNull(3), Alignment.BottomStart, 18.dp, (-6).dp) { selectedMetric = it }
        YesterdayReportVertexLabel(displayMetrics.getOrNull(4), Alignment.CenterStart, 0.dp, (-44).dp) { selectedMetric = it }

        selectedMetric?.explanation?.let { explanation ->
            YesterdayReportScoreDetailDialog(
                title = explanation.title,
                score = explanation.score,
                accentColor = behaviorScoreAccentColor(selectedMetric?.accentIndex ?: 0),
                formulaLines = explanation.formulaLines,
                comparisonRows = explanation.comparisonRows,
                onDismiss = { selectedMetric = null },
            )
        }
    }
}

@Composable
private fun BoxScope.YesterdayReportVertexLabel(
    metric: DailyBehaviorScoreMetric?,
    alignment: Alignment,
    xOffset: Dp,
    yOffset: Dp,
    onSelect: (DailyBehaviorScoreMetric) -> Unit,
) {
    metric ?: return
    val accent = behaviorScoreAccentColor(metric.accentIndex)
    Surface(
        modifier =
            Modifier
                .align(alignment)
                .offset(x = xOffset, y = yOffset)
                .width(72.dp)
                .clickable(
                    onClickLabel = AppText.t("stats_score_metric_open_detail", metric.label),
                    onClick = { onSelect(metric) },
                ),
        shape = RoundedCornerShape(15.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                text = metric.label,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            Text(
                text = metric.score.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = accent,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun YesterdayReportMetricGrid(state: YesterdayReportUiState) {
    AdaptiveRowGrid(
        itemCount = 3,
        compactColumns = 3,
        expandedColumns = 3,
        horizontalSpacing = 10.dp,
        verticalSpacing = 10.dp,
    ) { modifier, index ->
        when (index) {
            0 -> YesterdayReportMetricCard(Icons.Default.PhoneAndroid, AppText.t("home_yesterday_report_total_usage"), state.totalUsageLabel, modifier)
            1 -> YesterdayReportMetricCard(Icons.Default.Shield, AppText.t("home_yesterday_report_saved"), state.savedLabel, modifier)
            else -> YesterdayReportMetricCard(Icons.Default.EmojiEvents, AppText.t("home_yesterday_report_points_net"), state.pointsNetLabel, modifier)
        }
    }
}

@Composable
private fun YesterdayReportMetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(17.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun YesterdayReportGroupSummary(state: YesterdayReportUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = AppText.t("home_yesterday_report_key_stats"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            AdaptiveRowGrid(
                itemCount = 5,
                compactColumns = 2,
                expandedColumns = 3,
                horizontalSpacing = 8.dp,
                verticalSpacing = 8.dp,
            ) { modifier, index ->
                val pair =
                    when (index) {
                        0 -> AppText.t("home_yesterday_report_control_done") to state.controlCompletedLabel
                        1 -> AppText.t("home_yesterday_report_control_exceeded") to state.controlExceededLabel
                        2 -> AppText.t("home_yesterday_report_encourage_done") to state.encourageCompletedLabel
                        3 -> AppText.t("home_yesterday_report_night_use") to state.nightUsageLabel
                        else -> AppText.t("home_yesterday_report_peak_period") to state.peakPeriodLabel
                    }
                YesterdayReportMiniStat(pair.first, pair.second, modifier)
            }
            YesterdayReportMiniStat(
                label = AppText.t("home_yesterday_report_redemptions"),
                value = state.redemptionCountLabel,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun YesterdayReportMiniStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.72f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun YesterdayReportTopApps(topApps: List<AppDisplayItem>) {
    if (topApps.isEmpty()) return
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = AppText.t("home_yesterday_report_top_apps"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            topApps.forEachIndexed { index, app ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = (index + 1).toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(22.dp),
                    )
                    AppIconCircle(pkg = app.packageName, size = 34.dp)
                    Text(
                        text = app.label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                    Text(
                        text = formatDuration(app.value),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun YesterdayReportScoreDetailDialog(
    title: String,
    score: Int,
    accentColor: Color,
    formulaLines: List<String>,
    comparisonRows: List<BehaviorScoreMetricComparisonRow>,
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
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accentColor)
                Text(text = AppText.t("stats_score_value", score), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = accentColor)
                Text(
                    text = AppText.t("stats_score_metric_formula_title"),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                )
                formulaLines.forEach { line ->
                    Text(
                        text = "- $line",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.18f)),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("", modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = AppText.t("home_yesterday_report_yesterday_column"),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                            )
                            Text(
                                text = AppText.t("home_yesterday_report_previous_column"),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                            )
                        }
                        comparisonRows.forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = row.label,
                                    modifier = Modifier.weight(1.2f),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(text = row.todayValue, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                Text(text = row.yesterdayValue, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
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
    ringColorPreferences: HomeActivityRingColorPreferences,
    isDataReady: Boolean,
    overviewAnimationReplayToken: Int,
    shouldPlayDataReveal: Boolean,
    onDataRevealStarted: () -> Unit,
    onOpenBehaviorRadar: () -> Unit,
    onOpenControlDetail: () -> Unit,
    onOpenEncourageDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    val dataRevealProgress = remember { Animatable(if (shouldPlayDataReveal && !isDataReady) 0f else 1f) }
    LaunchedEffect(isDataReady) {
        when {
            !isDataReady && shouldPlayDataReveal -> dataRevealProgress.snapTo(0f)
            !isDataReady -> dataRevealProgress.snapTo(1f)
            shouldPlayDataReveal && dataRevealProgress.value < 1f -> {
                onDataRevealStarted()
                dataRevealProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = HOME_OVERVIEW_DATA_REVEAL_MILLIS,
                        easing = FastOutSlowInEasing,
                    ),
                )
            }
            else -> dataRevealProgress.snapTo(1f)
        }
    }
    val boundedDataRevealProgress = dataRevealProgress.value.coerceIn(0f, 1f)
    val displayControlTodaySavedMinutes = (state.control.todaySavedMinutes * boundedDataRevealProgress).roundToInt()
    val displayControlCompletedGroups = (state.control.completedGroups * boundedDataRevealProgress).roundToInt()
    val displayControlTotalGroups = (state.control.totalGroups * boundedDataRevealProgress).roundToInt()
    val displayControlStreakLabel =
        if (boundedDataRevealProgress >= 1f) state.control.streakLabel else homeStreakLabel(archivedStreak = 0, todayCompleted = false)
    val displayEncourageTodayEarnedPoints = state.encourage.todayEarnedPoints * boundedDataRevealProgress
    val displayEncourageCompletedGroups = (state.encourage.completedGroups * boundedDataRevealProgress).roundToInt()
    val displayEncourageTotalGroups = (state.encourage.totalGroups * boundedDataRevealProgress).roundToInt()
    val displayEncourageStreakLabel =
        if (boundedDataRevealProgress >= 1f) state.encourage.streakLabel else homeStreakLabel(archivedStreak = 0, todayCompleted = false)
    val displayHistoryTotalSavedMinutes = (state.history.totalSavedMinutes * boundedDataRevealProgress).roundToLong()
    val displayHistoryExtendedLifeMinutes = (state.history.extendedLifeMinutes * boundedDataRevealProgress).roundToLong()
    val displayHistoryTotalEarnedPoints = state.history.totalEarnedPoints * boundedDataRevealProgress
    val displayHistoryCurrentPoints = state.history.currentPoints * boundedDataRevealProgress
    val outerRingColor = homeActivityRingMetricColor(state.activityRings.outer.metric, ringColorPreferences, themeColors)
    val middleRingColor = homeActivityRingMetricColor(state.activityRings.middle.metric, ringColorPreferences, themeColors)
    val innerRingColor = homeActivityRingMetricColor(state.activityRings.inner.metric, ringColorPreferences, themeColors)

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
            val notchRadiusPx = with(density) { (centerSize / 2 + 21.dp).toPx() }
            val compact = maxWidth < 380.dp
            val ringCenterOffsetY = 0.dp
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
                            value = displayControlTodaySavedMinutes.toString(),
                            unit = AppText.t("group_minutes"),
                            progress = AppText.t(
                                "home_commitment_progress_value",
                                displayControlCompletedGroups,
                                displayControlTotalGroups,
                            ),
                            streak = displayControlStreakLabel,
                            primaryMetricLabel = AppText.t("home_total_saved"),
                            primaryMetricValue = displayHistoryTotalSavedMinutes.toString(),
                            primaryMetricUnit = AppText.t("group_minutes"),
                            secondaryMetricLabel = AppText.t("home_equivalent_live_more"),
                            secondaryMetricValue = roundedDaysValue(displayHistoryExtendedLifeMinutes).toString(),
                            secondaryMetricUnit = AppText.t("home_day_unit"),
                            color = themeColors.controlContainer.copy(alpha = 0.28f),
                            contentColor = themeColors.onControlContainer,
                            accent = themeColors.control,
                            compact = compact,
                            centerGapPx = centerGapPx,
                            notchRadiusPx = notchRadiusPx,
                            onClick = onOpenControlDetail,
                            modifier = Modifier
                                .weight(1f)
                                .offset(x = 5.dp),
                        )
                        Spacer(modifier = Modifier.width(centerGap))
                        HomeOverviewWingPanel(
                            isLeft = false,
                            title = AppText.t("home_encouragement_panel"),
                            label = state.encourage.pointsMultiplierLabel?.let {
                                AppText.t("home_earned_today_with_badge", it)
                            } ?: AppText.t("home_earned_today"),
                            value = formatHomePointWholeValue(displayEncourageTodayEarnedPoints),
                            unit = AppText.t("group_points"),
                            progress = AppText.t(
                                "home_encouragement_progress_value",
                                displayEncourageCompletedGroups,
                                displayEncourageTotalGroups,
                            ),
                            streak = displayEncourageStreakLabel,
                            primaryMetricLabel = AppText.t("home_total_earned"),
                            primaryMetricValue = formatHomePointValue(displayHistoryTotalEarnedPoints),
                            primaryMetricUnit = AppText.t("group_points"),
                            secondaryMetricLabel = AppText.t("home_current_remaining"),
                            secondaryMetricValue = formatHomePointValue(displayHistoryCurrentPoints),
                            secondaryMetricUnit = AppText.t("group_points"),
                            color = themeColors.encourageContainer.copy(alpha = 0.28f),
                            contentColor = themeColors.onEncourageContainer,
                            accent = themeColors.encourage,
                            compact = compact,
                            centerGapPx = centerGapPx,
                            notchRadiusPx = notchRadiusPx,
                            onClick = onOpenEncourageDetail,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    HomeActivityRingsDial(
                        rings = state.activityRings,
                        outerColor = outerRingColor,
                        middleColor = middleRingColor,
                        innerColor = innerRingColor,
                        replayToken = overviewAnimationReplayToken,
                        revealProgress = boundedDataRevealProgress,
                        onClick = onOpenBehaviorRadar,
                        modifier = Modifier
                            .size(centerSize)
                            .offset(y = ringCenterOffsetY),
                    )
                    HomeActivityRingsProgressLabels(
                        rings = state.activityRings,
                        outerColor = outerRingColor,
                        middleColor = middleRingColor,
                        innerColor = innerRingColor,
                        contentColor = themeColors.inkStrong,
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .offset(x = 5.dp, y = centerSize / 2 + 31.dp + ringCenterOffsetY)
                                .width(centerSize),
                    )
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeOverviewDetailSheet(
    kind: HomeOverviewDetailKind,
    state: HomeOverviewUiState,
    onOpenGroup: (AppGroupWithApps) -> Unit,
    onDismiss: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isControl = kind == HomeOverviewDetailKind.CONTROL
    val accent = if (isControl) themeColors.control else themeColors.encourage
    val accentContainer = if (isControl) themeColors.controlContainer else themeColors.encourageContainer
    val groups = if (isControl) state.control.groups else state.encourage.groups
    val completedGroups = if (isControl) state.control.completedGroups else state.encourage.completedGroups
    val totalGroups = if (isControl) state.control.totalGroups else state.encourage.totalGroups
    val title =
        AppText.t(
            if (isControl) {
                "home_overview_detail_control_title"
            } else {
                "home_overview_detail_encourage_title"
            },
        )
    val heroLabel =
        AppText.t(
            if (isControl) {
                "home_overview_detail_saved_today"
            } else {
                "home_overview_detail_earned_today"
            },
        )
    val heroValue =
        if (isControl) {
            AppText.t("home_value_minutes", state.control.todaySavedMinutes.toString())
        } else {
            AppText.t("home_overview_detail_points_value", formatHomePointWholeValue(state.encourage.todayEarnedPoints))
        }
    val heroBody =
        when {
            totalGroups == 0 ->
                AppText.t(
                    if (isControl) {
                        "home_overview_detail_control_empty_body"
                    } else {
                        "home_overview_detail_encourage_empty_body"
                    },
                )
            completedGroups >= totalGroups ->
                AppText.t(
                    if (isControl) {
                        "home_overview_detail_control_all_met_body"
                    } else {
                        "home_overview_detail_encourage_all_met_body"
                    },
                    totalGroups,
                )
            else ->
                AppText.t(
                    if (isControl) {
                        "home_overview_detail_control_progress_body"
                    } else {
                        "home_overview_detail_encourage_progress_body"
                    },
                    completedGroups,
                    totalGroups,
                )
        }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = TinyVowRadius.FeaturedCard,
            topEnd = TinyVowRadius.FeaturedCard,
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 640.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(
                        start = TinyVowSpacing.PageHorizontal,
                        end = TinyVowSpacing.PageHorizontal,
                        bottom = 24.dp,
                    ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TinyVowIconSurface(
                    icon = if (isControl) Icons.Default.VerifiedUser else Icons.Default.Star,
                    contentDescription = null,
                    size = 42.dp,
                    iconSize = 22.dp,
                    containerColor = accentContainer.copy(alpha = 0.82f),
                    contentColor = accent,
                )
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.inkStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                TinyVowStatusPill(
                    text = AppText.t("home_overview_detail_status", completedGroups, totalGroups),
                    color = accent,
                    leadingDot = false,
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = AppText.t("group_close"),
                        tint = themeColors.inkMuted,
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = heroLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = themeColors.inkMuted,
                )
                Text(
                    text = heroValue,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.inkStrong,
                )
                Text(
                    text = heroBody,
                    style = MaterialTheme.typography.bodyMedium,
                    color = themeColors.inkMuted,
                    textAlign = TextAlign.Center,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
            ) {
                if (isControl) {
                    TinyVowMetricTile(
                        label = AppText.t("home_overview_detail_control_streak"),
                        value = AppText.t("home_overview_detail_days_value", state.control.streakDays),
                        color = accent,
                        compact = true,
                        modifier = Modifier.weight(1f),
                    )
                    TinyVowMetricTile(
                        label = AppText.t("home_overview_detail_total_saved"),
                        value = AppText.t("home_value_minutes", state.history.totalSavedMinutes.toString()),
                        color = accent,
                        compact = true,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    TinyVowMetricTile(
                        label = AppText.t("home_overview_detail_encourage_streak"),
                        value = AppText.t("home_overview_detail_days_value", state.encourage.streakDays),
                        color = accent,
                        compact = true,
                        modifier = Modifier.weight(1f),
                    )
                    TinyVowMetricTile(
                        label = AppText.t("home_overview_detail_current_balance"),
                        value = AppText.t("home_overview_detail_points_value", formatHomePointValue(state.history.currentPoints)),
                        color = accent,
                        compact = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Text(
                text =
                    AppText.t(
                        if (isControl) {
                            "home_overview_detail_period_control"
                        } else {
                            "home_overview_detail_period_encourage"
                        },
                    ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = themeColors.inkStrong,
            )

            if (groups.isEmpty()) {
                TinyVowEmptyState(
                    title =
                        AppText.t(
                            if (isControl) {
                                "home_overview_detail_control_empty_title"
                            } else {
                                "home_overview_detail_encourage_empty_title"
                            },
                        ),
                    body = heroBody,
                    icon = if (isControl) Icons.Default.VerifiedUser else Icons.Default.Star,
                )
            } else {
                groups.forEach { group ->
                    HomeOverviewDetailGroupRow(
                        item = group,
                        kind = kind,
                        accent = accent,
                        onClick = { onOpenGroup(group.groupData) },
                    )
                }
                if (!isControl) {
                    Text(
                        text =
                            AppText.t(
                                "home_overview_detail_total_earned_value",
                                formatHomePointValue(state.history.totalEarnedPoints),
                            ),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeColors.inkMuted,
                        textAlign = TextAlign.Center,
                    )
                }
                Text(
                    text = AppText.t("home_overview_detail_group_hint"),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.inkMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun HomeOverviewDetailGroupRow(
    item: HomeOverviewGroupDetailUiState,
    kind: HomeOverviewDetailKind,
    accent: Color,
    onClick: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val isControl = kind == HomeOverviewDetailKind.CONTROL
    val usedMinutes = ceilHomeMinutes(item.usedMillis)
    val targetMinutes = ceilHomeMinutes(item.targetMillis)
    val remainingMillis = (item.targetMillis - item.usedMillis).coerceAtLeast(0L)
    val exceededMillis = (item.usedMillis - item.targetMillis).coerceAtLeast(0L)
    val statusText =
        when {
            isControl && exceededMillis > 0L -> AppText.t("home_overview_detail_over_value", ceilHomeMinutes(exceededMillis))
            isControl && remainingMillis > 0L -> AppText.t("home_overview_detail_remaining_value", ceilHomeMinutes(remainingMillis))
            isControl -> AppText.t("home_overview_detail_limit_used")
            !isControl && remainingMillis > 0L -> AppText.t("home_overview_detail_more_needed_value", ceilHomeMinutes(remainingMillis))
            else -> AppText.t("home_overview_detail_met")
        }
    val metadata =
        buildList {
            if (isControl && item.extraMinutes > 0) {
                add(AppText.t("home_overview_detail_extra_minutes", item.extraMinutes))
            }
            if (isControl && item.hasPeriodPass) {
                add(AppText.t("home_group_effect_period_pass"))
            }
            if (!isControl) {
                add(
                    AppText.t(
                        "home_overview_detail_points_rate",
                        trimHomeMultiplier(item.groupData.group.pointsPerMinute),
                    ),
                )
                if (item.pointsMultiplier > 1.0) {
                    add(AppText.t("home_points_multiplier_badge", trimHomeMultiplier(item.pointsMultiplier)))
                }
            }
        }.joinToString(AppText.t("home_overview_detail_metadata_separator"))
    val progress =
        item.usedMillis.toFloat() /
            item.targetMillis.coerceAtLeast(1L).toFloat()

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(TinyVowRadius.ItemCard),
        color = themeColors.surfaceSoft,
        border = BorderStroke(1.dp, themeColors.borderSoft.copy(alpha = 0.72f)),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CompactCardHorizontal,
                vertical = TinyVowSpacing.CompactCardVertical,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = item.groupData.group.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.inkStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = homeOverviewDetailPeriodLabel(item.groupData.group.limitPeriod),
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.inkMuted,
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = AppText.t("home_overview_detail_usage_value", usedMinutes, targetMinutes),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.ink,
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = accent,
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = themeColors.inkMuted,
                )
            }
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = accent,
                trackColor = accent.copy(alpha = 0.12f),
            )
            if (metadata.isNotBlank()) {
                Text(
                    text = metadata,
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.inkMuted,
                )
            }
        }
    }
}

private fun homeOverviewDetailPeriodLabel(period: LimitPeriod): String =
    when (period) {
        LimitPeriod.DAILY -> AppText.t("group_daily_short")
        LimitPeriod.WEEKLY -> AppText.t("group_weekly_short")
        LimitPeriod.MONTHLY -> AppText.t("group_monthly_short")
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeStepSummaryCard(
    steps: Int,
    earnedPoints: Double,
    rewardThreshold: Int,
    isProActive: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    val earnedPointsFloor = kotlin.math.floor(earnedPoints.coerceAtLeast(0.0)).toLong()
    val stepTitleValue =
        if (rewardThreshold > 0) {
            "${formatStepCount(steps)}/${formatStepCount(rewardThreshold)}"
        } else {
            formatStepCount(steps)
        }
    TinyVowCard(
        modifier =
            modifier
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
        shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
        borderAlpha = 0.30f,
        shadowElevation = TinyVowElevation.FeaturedCard,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(30.dp),
                shape = RoundedCornerShape(10.dp),
                color = themeColors.encourage.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, themeColors.encourage.copy(alpha = 0.20f)),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = null,
                        tint = themeColors.encourage,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text =
                        homeStepTitleText(
                            title = AppText.t("home_steps_title"),
                            value = stepTitleValue,
                            valueColor = themeColors.encourage,
                            titleColor = themeColors.inkStrong,
                        ),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, lineHeight = 15.sp),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
                if (isProActive) {
                    Text(
                        text = AppText.t("home_step_points_plus_value", earnedPointsFloor),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.8.sp, lineHeight = 14.sp),
                        fontWeight = FontWeight.Medium,
                        color = themeColors.inkMuted,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeStepCornerSummary(
    steps: Int,
    earnedPoints: Double,
    rewardThreshold: Int,
    isProActive: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeColors = LocalThemeColors.current
    val textStyle = MaterialTheme.typography.labelMedium
    val earnedPointsFloor = kotlin.math.floor(earnedPoints.coerceAtLeast(0.0)).toLong()
    val stepSummaryText =
        if (rewardThreshold > 0 && steps < rewardThreshold) {
            AppText.t(
                "home_step_points_reward_progress_summary",
                formatStepCount(steps),
                formatStepCount(rewardThreshold),
            )
        } else {
            AppText.t("home_today_steps_value", formatStepCount(steps))
        }
    Column(
        modifier =
            modifier
                .widthIn(max = 150.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .padding(start = 8.dp, top = 2.dp, bottom = 2.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(
            text = stepSummaryText,
            style = textStyle,
            fontWeight = FontWeight.SemiBold,
            color = themeColors.encourage,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
        if (isProActive) {
            Text(
                text = AppText.t("home_step_points_plus_value", earnedPointsFloor),
                style = textStyle,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.encourage,
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun StepPointsSettingsDialog(
    todaySteps: Int,
    currentPointsPerStep: Double,
    currentRewardThreshold: Int,
    isStepCounterAvailable: Boolean,
    isActivityRecognitionPermissionGranted: Boolean,
    onRequestActivityRecognitionPermission: () -> Unit,
    onSave: (Double, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var rateText by remember(currentPointsPerStep) {
        mutableStateOf(formatStepRate(currentPointsPerStep))
    }
    var rewardThresholdText by remember(currentRewardThreshold) {
        mutableStateOf(currentRewardThreshold.toString())
    }
    val parsedRate = rateText.toDoubleOrNull()?.coerceAtLeast(0.0)
    val parsedRewardThreshold = rewardThresholdText.toIntOrNull()?.coerceAtLeast(0)
    val estimatedPoints = todaySteps * (parsedRate ?: 0.0)
    val rewardProgressValue =
        when {
            parsedRewardThreshold == null -> null
            parsedRewardThreshold <= 0 || todaySteps >= parsedRewardThreshold -> AppText.t("home_step_points_reward_reached")
            else -> AppText.t(
                "home_step_points_reward_progress_value",
                formatStepCount(todaySteps),
                formatStepCount(parsedRewardThreshold),
            )
        }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppText.t("home_step_points_settings_title")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeStepSettingsMetricRow(
                    label = AppText.t("home_today_steps"),
                    value = AppText.t("home_steps_plain_value", formatStepCount(todaySteps)),
                )
                HomeStepSettingsMetricRow(
                    label = AppText.t("home_step_points_estimate"),
                    value = AppText.t("group_points_value", formatHomePointValue(estimatedPoints)),
                )
                rewardProgressValue?.let { progress ->
                    HomeStepSettingsMetricRow(
                        label = AppText.t("home_step_points_reward_progress_label"),
                        value = progress,
                    )
                }
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = sanitizeHomeDecimalInput(it) },
                    label = { Text(AppText.t("home_step_points_rate_label")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = rewardThresholdText,
                    onValueChange = { rewardThresholdText = sanitizeHomeIntegerInput(it) },
                    label = { Text(AppText.t("home_step_points_reward_threshold_label")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = AppText.t("home_step_points_reward_threshold_hint"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!isStepCounterAvailable) {
                    Text(
                        text = AppText.t("home_step_counter_unavailable"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else if (!isActivityRecognitionPermissionGranted) {
                    Text(
                        text = AppText.t("home_step_permission_required"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TinyVowButton(
                        text = AppText.t("home_step_permission_action"),
                        onClick = onRequestActivityRecognitionPermission,
                        modifier = Modifier.fillMaxWidth(),
                        tone = TinyVowButtonTone.Primary,
                    )
                }
            }
        },
        confirmButton = {
            TinyVowButton(
                text = AppText.t("group_save"),
                onClick = {
                    if (parsedRate != null && parsedRewardThreshold != null) {
                        onSave(parsedRate, parsedRewardThreshold)
                    }
                },
                enabled = parsedRate != null && parsedRewardThreshold != null,
                tone = TinyVowButtonTone.Primary,
            )
        },
        dismissButton = {
            TinyVowButton(
                text = AppText.t("group_cancel"),
                onClick = onDismiss,
            )
        },
    )
}

@Composable
private fun HomeStepSettingsMetricRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun HomeBehaviorRadarDialog(
    state: HomeOverviewUiState,
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
                    text = AppText.t("home_today_overview"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = AppText.t("home_behavior_radar_subtitle"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                HomeBehaviorOverviewPanel(
                    metrics = homeOverviewScoreMetrics(state),
                    comparisonMetrics = state.behaviorComparisonMetrics,
                    totalUsageMillis = state.totalUsageMillis,
                    controlUsageMillis = state.controlUsageMillis,
                    encourageUsageMillis = state.encourageUsageMillis,
                    savedMillis = state.savedMillis,
                    modifier = Modifier.padding(top = 24.dp),
                    scaleToFit = true,
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
private fun HomeBehaviorOverviewPanel(
    metrics: List<DailyBehaviorScoreMetric>,
    comparisonMetrics: List<DailyBehaviorScoreMetric>,
    totalUsageMillis: Long,
    controlUsageMillis: Long,
    encourageUsageMillis: Long,
    savedMillis: Long,
    modifier: Modifier = Modifier,
    scaleToFit: Boolean = false,
) {
    val themeColors = LocalThemeColors.current
    val savedAccent = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
    val totalScore =
        metrics
            .takeIf { it.isNotEmpty() }
            ?.map { it.score }
            ?.average()
            ?.roundToInt()
            ?: 0
    if (scaleToFit) {
        BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
            val designWidth = 360.dp
            val designHeight = 332.dp
            val widthScale = maxWidth / designWidth
            val heightScale = if (maxHeight != Dp.Infinity) maxHeight / designHeight else 1f
            val scale = minOf(widthScale, heightScale, 1f)
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(designHeight * scale),
                contentAlignment = Alignment.TopCenter,
            ) {
                HomeBehaviorOverviewPanelContent(
                    metrics = metrics,
                    comparisonMetrics = comparisonMetrics,
                    totalUsageMillis = totalUsageMillis,
                    controlUsageMillis = controlUsageMillis,
                    encourageUsageMillis = encourageUsageMillis,
                    savedMillis = savedMillis,
                    totalScore = totalScore,
                    savedAccent = savedAccent,
                    modifier =
                        Modifier
                            .requiredWidth(designWidth)
                            .requiredHeight(designHeight)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                transformOrigin = TransformOrigin(0.5f, 0f)
                            },
                )
            }
        }
        return
    }
    HomeBehaviorOverviewPanelContent(
        metrics = metrics,
        comparisonMetrics = comparisonMetrics,
        totalUsageMillis = totalUsageMillis,
        controlUsageMillis = controlUsageMillis,
        encourageUsageMillis = encourageUsageMillis,
        savedMillis = savedMillis,
        totalScore = totalScore,
        savedAccent = savedAccent,
        modifier = modifier,
    )
}

@Composable
private fun HomeBehaviorOverviewPanelContent(
    metrics: List<DailyBehaviorScoreMetric>,
    comparisonMetrics: List<DailyBehaviorScoreMetric>,
    totalUsageMillis: Long,
    controlUsageMillis: Long,
    encourageUsageMillis: Long,
    savedMillis: Long,
    totalScore: Int,
    savedAccent: Color,
    modifier: Modifier,
) {
    val themeColors = LocalThemeColors.current
    BehaviorRadarPanel(
        metrics = metrics,
        comparisonMetrics = comparisonMetrics,
        cornerMetrics =
            listOf(
                BehaviorCornerMetric(
                    label = AppText.t("stats_behavior_corner_usage"),
                    value = formatHomeBehaviorMetricMinutes(totalUsageMillis),
                    unit = AppText.t("stats_behavior_unit_minutes_short"),
                    accent = themeColors.base,
                    rawMillis = totalUsageMillis,
                    align = Alignment.TopStart,
                ),
                BehaviorCornerMetric(
                    label = AppText.t("stats_behavior_corner_investment"),
                    value = formatHomeBehaviorMetricMinutes(encourageUsageMillis),
                    unit = AppText.t("stats_behavior_unit_minutes_short"),
                    accent = themeColors.encourage,
                    rawMillis = encourageUsageMillis,
                    align = Alignment.TopEnd,
                ),
                BehaviorCornerMetric(
                    label = AppText.t("stats_behavior_corner_control"),
                    value = formatHomeBehaviorMetricMinutes(controlUsageMillis),
                    unit = AppText.t("stats_behavior_unit_minutes_short"),
                    accent = themeColors.control,
                    rawMillis = controlUsageMillis,
                    align = Alignment.BottomStart,
                ),
                BehaviorCornerMetric(
                    label = AppText.t("stats_behavior_corner_savings"),
                    value = formatHomeBehaviorMetricMinutes(savedMillis),
                    unit = AppText.t("stats_behavior_unit_minutes_short"),
                    accent = savedAccent,
                    rawMillis = savedMillis,
                    align = Alignment.BottomEnd,
                ),
            ),
        totalMetric =
            BehaviorTotalMetric(
                label = AppText.t("stats_behavior_total_score"),
                value = totalScore.toString(),
                unit = "",
                accent = themeColors.base,
            ),
        modifier = modifier,
    )
}

private fun formatHomeBehaviorMetricMinutes(durationMillis: Long): String {
    if (durationMillis <= 0L) return "0"
    return ((durationMillis + 59_999L) / 60_000L).toString()
}

private fun homeActivityRingMetricColor(
    metric: HomeActivityRingMetric,
    preferences: HomeActivityRingColorPreferences,
    themeColors: ThemeTokens,
): Color =
    homeActivityRingColor(preferences.preferenceFor(metric), themeColors)

private fun homeActivityRingColor(
    preference: HomeActivityRingColorPreference,
    themeColors: ThemeTokens,
): Color =
    when (preference.source) {
        HomeActivityRingColorSource.CONTROL -> themeColors.control
        HomeActivityRingColorSource.ENCOURAGE -> themeColors.encourage
        HomeActivityRingColorSource.THEME -> themeColors.base
        HomeActivityRingColorSource.CUSTOM -> preference.customArgb?.let(::Color) ?: themeColors.base
    }

@Composable
fun HomeActivityRingsDial(
    rings: HomeActivityRingsUiState,
    outerColor: Color,
    middleColor: Color,
    innerColor: Color,
    replayToken: Int,
    revealProgress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val replayProgress =
        animateReplayFractionValue(
            targetValue = 1f,
            replayKey = replayToken,
            durationMillis = 860,
        )
    val boundedRevealProgress = (revealProgress.coerceIn(0f, 1f) * replayProgress).coerceIn(0f, 1f)

    Box(
        modifier =
            modifier
                .clip(CircleShape)
                .clickable(
                    onClickLabel = AppText.t("home_activity_rings_action"),
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = (12.dp.toPx() * 1.2f).roundToInt().toFloat()
            val ringGap = 2.dp.toPx().roundToInt().toFloat()
            val outerInset = strokeWidth / 2f
            drawHomeActivityRing(
                progress = if (rings.outer.available) rings.outer.progress * boundedRevealProgress else 0f,
                color = outerColor,
                trackColor = homeActivityRingTrackColor(outerColor),
                inset = outerInset,
                strokeWidth = strokeWidth,
            )
            drawHomeActivityRing(
                progress = if (rings.middle.available) rings.middle.progress * boundedRevealProgress else 0f,
                color = middleColor,
                trackColor = homeActivityRingTrackColor(middleColor),
                inset = outerInset + strokeWidth + ringGap,
                strokeWidth = strokeWidth,
            )
            drawHomeActivityRing(
                progress = if (rings.inner.available) rings.inner.progress * boundedRevealProgress else 0f,
                color = innerColor,
                trackColor = homeActivityRingTrackColor(innerColor),
                inset = outerInset + (strokeWidth + ringGap) * 2f,
                strokeWidth = strokeWidth,
            )
        }
    }
}

@Composable
private fun HomeActivityRingsProgressLabels(
    rings: HomeActivityRingsUiState,
    outerColor: Color,
    middleColor: Color,
    innerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    var selectedRing by remember { mutableStateOf<HomeActivityRingMetric?>(null) }

    selectedRing?.let { kind ->
        HomeActivityRingExplanationDialog(
            kind = kind,
            rings = rings,
            onDismiss = { selectedRing = null },
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        HomeActivityRingProgressLabel(
            label = homeActivityRingMetricLabel(rings.outer.metric),
            value = formatHomeActivityRingProgress(rings.outer.progress, rings.outer.available),
            color = outerColor,
            contentColor = contentColor,
            onClick = { selectedRing = rings.outer.metric },
        )
        HomeActivityRingProgressLabel(
            label = homeActivityRingMetricLabel(rings.middle.metric),
            value = formatHomeActivityRingProgress(rings.middle.progress, rings.middle.available),
            color = middleColor,
            contentColor = contentColor,
            onClick = { selectedRing = rings.middle.metric },
        )
        HomeActivityRingProgressLabel(
            label = homeActivityRingMetricLabel(rings.inner.metric),
            value = formatHomeActivityRingProgress(rings.inner.progress, rings.inner.available),
            color = innerColor,
            contentColor = contentColor,
            onClick = { selectedRing = rings.inner.metric },
        )
    }
}

@Composable
private fun HomeActivityRingProgressLabel(
    label: String,
    value: String,
    color: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.width(10.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
        Text(
            text = label,
            modifier = Modifier.width(42.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.SemiBold,
            color = contentColor.copy(alpha = 0.78f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Start,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
        Box(
            modifier = Modifier
                .width(31.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.SemiBold,
                color = contentColor.copy(alpha = 0.78f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HomeActivityRingExplanationDialog(
    kind: HomeActivityRingMetric,
    rings: HomeActivityRingsUiState,
    onDismiss: () -> Unit,
) {
    val title = homeActivityRingMetricLabel(kind)
    val progress =
        when (kind) {
            HomeActivityRingMetric.CONTROL -> rings.controlProgress
            HomeActivityRingMetric.ENCOURAGE -> rings.encourageProgress
            HomeActivityRingMetric.GROWTH -> rings.growthProgress
            HomeActivityRingMetric.STEPS -> rings.stepProgress
            HomeActivityRingMetric.FOCUS -> rings.focusProgress
        }
    val available =
        when (kind) {
            HomeActivityRingMetric.CONTROL -> rings.controlAvailable
            HomeActivityRingMetric.ENCOURAGE -> rings.encourageAvailable
            HomeActivityRingMetric.GROWTH -> rings.growthAvailable
            HomeActivityRingMetric.STEPS -> rings.stepAvailable
            HomeActivityRingMetric.FOCUS -> rings.focusAvailable
        }
    val detail =
        when (kind) {
            HomeActivityRingMetric.CONTROL -> rings.controlDetail
            HomeActivityRingMetric.ENCOURAGE -> rings.encourageDetail
            HomeActivityRingMetric.GROWTH -> rings.growthDetail
            HomeActivityRingMetric.STEPS -> HomeActivityRingDetailUiState()
            HomeActivityRingMetric.FOCUS -> HomeActivityRingDetailUiState()
        }
    val formula =
        when (kind) {
            HomeActivityRingMetric.CONTROL -> AppText.t("home_activity_ring_control_formula")
            HomeActivityRingMetric.ENCOURAGE -> AppText.t("home_activity_ring_encourage_formula")
            HomeActivityRingMetric.GROWTH -> AppText.t("home_activity_ring_growth_formula")
            HomeActivityRingMetric.STEPS -> AppText.t("home_activity_ring_steps_formula")
            HomeActivityRingMetric.FOCUS -> AppText.t("home_activity_ring_focus_formula")
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppText.t("home_activity_ring_detail_title", title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = formula,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                HomeActivityRingDetailRow(
                    label = AppText.t("home_activity_ring_detail_progress"),
                    value = formatHomeActivityRingProgress(progress, available),
                )
                if (available) {
                    when (kind) {
                        HomeActivityRingMetric.CONTROL -> {
                            HomeActivityRingDetailRow(
                                label = AppText.t("home_activity_ring_detail_period_elapsed"),
                                value = formatHomeActivityRingPercent(detail.periodElapsedProgress),
                            )
                            HomeActivityRingDetailRow(
                                label = AppText.t("home_activity_ring_detail_control_health"),
                                value = formatHomeActivityRingPercent(detail.healthProgress),
                            )
                            HomeActivityRingDetailRow(
                                label = AppText.t("home_activity_ring_detail_usage_limit"),
                                value =
                                    AppText.t(
                                        "home_activity_ring_detail_duration_pair",
                                        formatDuration(detail.usedMillis),
                                        formatDuration(detail.targetMillis),
                                    ),
                            )
                            HomeActivityRingDetailRow(
                                label = AppText.t("home_activity_ring_detail_expected_usage"),
                                value = formatDuration(detail.expectedMillis),
                            )
                        }
                        HomeActivityRingMetric.ENCOURAGE -> {
                            HomeActivityRingDetailRow(
                                label = AppText.t("home_activity_ring_detail_usage_target"),
                                value =
                                    AppText.t(
                                        "home_activity_ring_detail_duration_pair",
                                        formatDuration(detail.usedMillis),
                                        formatDuration(detail.targetMillis),
                                    ),
                            )
                        }
                        HomeActivityRingMetric.GROWTH -> {
                            HomeActivityRingDetailRow(
                                label = AppText.t("home_activity_ring_detail_points_target"),
                                value =
                                    AppText.t(
                                        "home_activity_ring_detail_points_pair",
                                        formatHomePointValue(detail.earnedPoints),
                                        formatHomePointValue(detail.targetPoints),
                                    ),
                            )
                        }
                        HomeActivityRingMetric.STEPS -> {
                            HomeActivityRingDetailRow(
                                label = AppText.t("home_activity_ring_detail_steps_target"),
                                value = AppText.t("home_activity_ring_detail_steps_pair", formatStepCount(rings.stepCount), formatStepCount(rings.stepTarget)),
                            )
                        }
                        HomeActivityRingMetric.FOCUS -> {
                            HomeActivityRingDetailRow(
                                label = AppText.t("home_activity_ring_detail_focus_target"),
                                value =
                                    AppText.t(
                                        "home_activity_ring_detail_duration_pair",
                                        formatDuration(rings.focusMillis),
                                        formatDuration(rings.focusTargetMillis),
                                    ),
                            )
                        }
                    }
                    if (kind == HomeActivityRingMetric.CONTROL || kind == HomeActivityRingMetric.ENCOURAGE || kind == HomeActivityRingMetric.GROWTH) {
                        HomeActivityRingDetailRow(
                            label = AppText.t("home_activity_ring_detail_groups"),
                            value = AppText.t("home_activity_ring_detail_group_count", detail.groupCount),
                        )
                    }
                } else {
                    Text(
                        text = AppText.t("home_activity_ring_detail_no_data"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(AppText.t("stats_score_info_close"))
            }
        },
    )
}

@Composable
private fun HomeActivityRingDetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.46f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.54f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
        )
    }
}

private fun formatHomeActivityRingProgress(
    progress: Float,
    available: Boolean,
): String =
    if (available) {
        formatHomeActivityRingPercent(progress)
    } else {
        AppText.t("home_activity_ring_empty_value")
    }

private fun formatHomeActivityRingPercent(progress: Float): String =
    "${(progress.coerceAtLeast(0f) * 100f).roundToInt()}%"

private fun homeActivityRingMetricLabel(metric: HomeActivityRingMetric): String =
    when (metric) {
        HomeActivityRingMetric.CONTROL -> AppText.t("home_activity_ring_control_label")
        HomeActivityRingMetric.ENCOURAGE -> AppText.t("home_activity_ring_encourage_label")
        HomeActivityRingMetric.GROWTH -> AppText.t("home_activity_ring_growth_label")
        HomeActivityRingMetric.STEPS -> AppText.t("home_activity_ring_steps_label")
        HomeActivityRingMetric.FOCUS -> AppText.t("home_activity_ring_focus_label")
    }

private fun homeActivityRingTrackColor(color: Color): Color =
    color.copy(alpha = 0.18f)

private fun DrawScope.drawHomeActivityRing(
    progress: Float,
    color: Color,
    trackColor: Color,
    inset: Float,
    strokeWidth: Float,
) {
    val arcTopLeft = Offset(inset, inset)
    val arcSize =
        androidx.compose.ui.geometry.Size(
            width = size.width - inset * 2f,
            height = size.height - inset * 2f,
        )
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = arcSize.width.coerceAtMost(arcSize.height) / 2f
    drawCircle(
        color = trackColor,
        radius = radius,
        center = center,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
    )
    val boundedProgress = progress.coerceAtLeast(0f)
    if (boundedProgress <= 0f) return

    val extraProgress = boundedProgress - kotlin.math.floor(boundedProgress)
    val hasCompletedLoop = boundedProgress >= 0.995f
    if (hasCompletedLoop) {
        drawCircle(
            color = color,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }

    val visibleSweep =
        when {
            boundedProgress < 0.995f -> boundedProgress * 360f
            extraProgress > 0.01f -> extraProgress * 360f
            else -> 0f
        }
    if (visibleSweep <= 0f) {
        val completedHeadAngle = 270f
        drawHomeActivityRingCapShadow(
            center = center,
            radius = radius,
            strokeWidth = strokeWidth,
            angleDegrees = completedHeadAngle,
        )
        drawHomeActivityRingCap(
            center = center,
            radius = radius,
            strokeWidth = strokeWidth,
            angleDegrees = completedHeadAngle,
            color = color,
        )
        return
    }

    val endAngle = -90f + visibleSweep
    drawArc(
        color = color,
        startAngle = -90f,
        sweepAngle = visibleSweep,
        useCenter = false,
        topLeft = arcTopLeft,
        size = arcSize,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
    )
    if (!hasCompletedLoop) {
        drawHomeActivityRingCap(
            center = center,
            radius = radius,
            strokeWidth = strokeWidth,
            angleDegrees = -90f,
            color = color,
        )
    }
    drawHomeActivityRingCapShadow(
        center = center,
        radius = radius,
        strokeWidth = strokeWidth,
        angleDegrees = endAngle,
    )
    drawHomeActivityRingCap(
        center = center,
        radius = radius,
        strokeWidth = strokeWidth,
        angleDegrees = endAngle,
        color = color,
    )
}

private fun DrawScope.drawHomeActivityRingCap(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    angleDegrees: Float,
    color: Color,
) {
    drawCircle(
        color = color,
        radius = strokeWidth / 2f,
        center = center + homeActivityRingUnitVector(angleDegrees) * radius,
    )
}

private fun DrawScope.drawHomeActivityRingCapShadow(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    angleDegrees: Float,
) {
    val direction = homeActivityRingUnitVector(angleDegrees)
    val tangent = homeActivityRingTangentVector(angleDegrees)
    val capCenter = center + direction * radius
    val shadowCenter = capCenter + tangent * (strokeWidth * 0.16f)
    val radialRadius = strokeWidth / 2f
    val tangentRadius = strokeWidth * 0.44f
    val gradientRadius = strokeWidth * 0.68f
    val ringClipPath =
        Path().apply {
            fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
            val outerRadius = radius + strokeWidth / 2f
            val innerRadius = (radius - strokeWidth / 2f).coerceAtLeast(0f)
            addOval(
                Rect(
                    left = center.x - outerRadius,
                    top = center.y - outerRadius,
                    right = center.x + outerRadius,
                    bottom = center.y + outerRadius,
                ),
            )
            addOval(
                Rect(
                    left = center.x - innerRadius,
                    top = center.y - innerRadius,
                    right = center.x + innerRadius,
                    bottom = center.y + innerRadius,
                ),
            )
    }

    clipPath(ringClipPath) {
        rotate(degrees = angleDegrees + 90f, pivot = shadowCenter) {
            drawOval(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                Color.Black.copy(alpha = 0.48f),
                                Color.Black.copy(alpha = 0.22f),
                                Color.Transparent,
                            ),
                        center = shadowCenter,
                        radius = gradientRadius,
                    ),
                topLeft = Offset(shadowCenter.x - tangentRadius, shadowCenter.y - radialRadius),
                size =
                    androidx.compose.ui.geometry.Size(
                        width = tangentRadius * 2f,
                        height = radialRadius * 2f,
                    ),
            )
        }
    }
}

private fun homeActivityRingUnitVector(angleDegrees: Float): Offset {
    val radians = Math.toRadians(angleDegrees.toDouble())
    return Offset(cos(radians).toFloat(), sin(radians).toFloat())
}

private fun homeActivityRingTangentVector(angleDegrees: Float): Offset {
    val radians = Math.toRadians(angleDegrees.toDouble())
    return Offset(-sin(radians).toFloat(), cos(radians).toFloat())
}

@Composable
private fun HomeOverviewScoreDial(
    score: Int,
    metrics: List<DailyBehaviorScoreMetric>,
    ringTrackColor: Color,
    scoreColor: Color,
    replayToken: Int,
    revealProgress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val displaySegments = metrics.take(5)
    val replayProgress =
        animateReplayFractionValue(
            targetValue = 1f,
            replayKey = replayToken,
            durationMillis = 860,
        )
    val boundedRevealProgress = (revealProgress.coerceIn(0f, 1f) * replayProgress).coerceIn(0f, 1f)

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
            val displayScore = (score * boundedRevealProgress).roundToInt()
            Text(
                text = displayScore.toString(),
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 44.sp),
                fontWeight = FontWeight.ExtraBold,
                color = scoreColor,
            )
            Text(
                text = AppText.t(homeOverviewScoreStatusKey(displayScore, BusinessDay.today(ZoneId.systemDefault(), BusinessDay.cachedStartHour()))),
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
    centerGapPx: Float,
    notchRadiusPx: Float,
    onClick: () -> Unit,
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
    BoxWithConstraints(modifier = modifier.fillMaxHeight()) {
        val outwardExpansion = 5.dp
        Surface(
            modifier =
                Modifier
                    .offset(x = if (isLeft) -outwardExpansion else 0.dp)
                    .requiredWidth(maxWidth + outwardExpansion)
                    .fillMaxHeight()
                    .clickable(onClick = onClick),
            shape = panelShape,
            color = color,
            border = BorderStroke(3.dp, accent),
        ) {
            Column(
                modifier = Modifier.padding(
                    start = if (isLeft) 14.dp else if (compact) 13.dp else 17.dp,
                    end = if (isLeft) if (compact) 13.dp else 17.dp else 14.dp,
                    top = 14.dp,
                    bottom = 14.dp,
                ),
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = Arrangement.spacedBy(if (compact) 7.dp else 8.dp),
            ) {
            Column(horizontalAlignment = horizontalAlignment, verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (isLeft) Arrangement.Start else Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (isLeft) {
                        HomeOverviewWingAccentDot(accent = accent)
                        Spacer(modifier = Modifier.width(7.dp))
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = if (compact) 15.sp else 16.sp,
                            lineHeight = if (compact) 18.sp else 19.sp,
                        ),
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        textAlign = textAlign,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (!isLeft) {
                        Spacer(modifier = Modifier.width(7.dp))
                        HomeOverviewWingAccentDot(accent = accent)
                    }
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = if (compact) 10.5.sp else 11.sp,
                        lineHeight = 14.sp,
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
}

@Composable
private fun HomeOverviewWingAccentDot(
    accent: Color,
) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(accent),
    )
}

@Composable
private fun HomeOverviewWingMainMetric(
    value: String,
    unit: String,
    contentColor: Color,
    compact: Boolean,
    alignEnd: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = value,
            style =
                if (compact) {
                    MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp, lineHeight = 32.sp)
                } else {
                    MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp, lineHeight = 34.sp)
                },
            fontWeight = FontWeight.ExtraBold,
            color = contentColor,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = unit,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = if (compact) 10.5.sp else 11.5.sp,
                lineHeight = 14.sp,
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
            fontSize = 10.5.sp,
            lineHeight = 15.sp,
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
                fontSize = 10.sp,
                lineHeight = 13.sp,
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
                fontSize = 12.sp,
                lineHeight = 16.sp,
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
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.White.copy(alpha = 0.70f)
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = state.control.todaySavedMinutes.toString(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = AppText.t("group_minutes"),
                                style = MaterialTheme.typography.labelSmall.copy(
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
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White.copy(alpha = 0.80f),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = formatHomePointWholeValue(state.encourage.todayEarnedPoints),
                                style = MaterialTheme.typography.headlineMedium.copy(
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
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 1.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = Color.White,
                    maxLines = 1
                )
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = Color.White,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall.copy(
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
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
    ) {
        actions.forEach { action ->
            HomeBattleActionTile(
                action = action,
                onClick = { onActionClick(action) },
                modifier =
                    Modifier
                        .weight(1f)
                        .height(HomeCompactCardHeight),
            )
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
    val isStatusOnly =
        action.type == HomeBattleActionType.CONTROL ||
            action.type == HomeBattleActionType.ENCOURAGE
    val roleIconBorderAlpha = if (isStatusOnly) 0.45f else 0.24f

    TinyVowCard(
        shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
        borderAlpha = 0.30f,
        shadowElevation = TinyVowElevation.FeaturedCard,
        modifier =
            modifier
                .then(if (isStatusOnly) Modifier else Modifier.clickable(onClick = onClick)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(accent.copy(alpha = 0.12f))
                        .border(
                            width = 1.dp,
                            color = accent.copy(alpha = roleIconBorderAlpha),
                            shape = RoundedCornerShape(11.dp),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = homeBattleTitleText(action.title, action.value, accent, themeColors.inkStrong),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, lineHeight = 15.sp),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
                Text(
                    text =
                        homeBattleSubtitleText(
                            subtitle = action.subtitle,
                            subtitleGroupName = action.subtitleGroupName,
                            strikethroughText = action.subtitleStrikethroughText,
                            accent = accent,
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
}

private fun homeBattleTitleText(
    title: String,
    value: String,
    valueColor: Color,
    titleColor: Color,
) = buildAnnotatedString {
    withStyle(SpanStyle(color = titleColor, fontWeight = FontWeight.SemiBold)) {
        append(title)
    }
    if (value.isNotBlank()) {
        withStyle(SpanStyle(color = titleColor.copy(alpha = 0.42f))) {
            append(" · ")
        }
        withStyle(SpanStyle(color = valueColor, fontWeight = FontWeight.SemiBold)) {
            append(value)
        }
    }
}

private fun homeStepTitleText(
    title: String,
    value: String,
    valueColor: Color,
    titleColor: Color,
) = buildAnnotatedString {
    withStyle(SpanStyle(color = titleColor, fontWeight = FontWeight.SemiBold)) {
        append(title)
    }
    if (value.isNotBlank()) {
        append(" ")
        withStyle(SpanStyle(color = valueColor, fontWeight = FontWeight.SemiBold)) {
            append(value)
        }
    }
}

private fun homeBattleSubtitleText(
    subtitle: String,
    subtitleGroupName: String? = null,
    strikethroughText: String? = null,
    accent: Color,
    restColor: Color,
) = buildAnnotatedString {
    withStyle(SpanStyle(color = restColor)) { append(subtitle) }
    subtitleGroupName
        ?.takeIf { it.isNotEmpty() }
        ?.let { name ->
            val index = subtitle.indexOf(name)
            if (index >= 0) {
                addStyle(
                    style = SpanStyle(color = accent, fontWeight = FontWeight.SemiBold),
                    start = index,
                    end = index + name.length,
                )
            }
        }
    strikethroughText
        ?.takeIf { it.isNotEmpty() }
        ?.let { text ->
            val index = subtitle.lastIndexOf(text)
            if (index >= 0) {
                addStyle(
                    style = SpanStyle(textDecoration = TextDecoration.LineThrough),
                    start = index,
                    end = index + text.length,
                )
            }
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

private fun formatStepCount(steps: Int): String =
    java.lang.String.format(java.util.Locale.getDefault(), "%,d", steps.coerceAtLeast(0))

private fun formatStepRate(value: Double): String =
    java.math.BigDecimal.valueOf(value.coerceAtLeast(0.0)).stripTrailingZeros().toPlainString()

private fun sanitizeHomeDecimalInput(value: String): String {
    val builder = StringBuilder()
    var seenDot = false
    value.forEach { char ->
        when {
            char.isDigit() -> builder.append(char)
            char == '.' && !seenDot -> {
                builder.append(char)
                seenDot = true
            }
        }
    }
    return builder.toString().take(10)
}

private fun sanitizeHomeIntegerInput(value: String): String =
    buildString {
        value.forEach { char ->
            if (char.isDigit()) append(char)
        }
    }.take(6)

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
    activeBonusMinutesByGroup: Map<String, Int>,
    analysis: BehaviorScoreAnalysis,
    yesterdayGroupArchives: List<DailyGroupArchiveEntity>,
    yesterdayAppArchives: List<DailyAppArchiveEntity>,
    isYesterdayArchivePending: Boolean,
): List<DailyBehaviorScoreMetric> {
    val yesterdaySnapshots = mergeArchivedAppSnapshots(yesterdayAppArchives)
    val hasYesterdayData = yesterdayGroupArchives.isNotEmpty() || yesterdayAppArchives.isNotEmpty()
    val yesterdayFallbackValue =
        if (isYesterdayArchivePending) {
            AppText.t("stats_score_metric_pending_value")
        } else {
            AppText.t("stats_score_metric_empty_value")
        }
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
                                            yesterdayFallbackValue
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
                                            yesterdayFallbackValue
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
                                                    activeRewardEffects = activeRewardEffects,
                                                    activeBonusMinutesByGroup = activeBonusMinutesByGroup,
                                                    groupId = group.group.id,
                                                    limitMinutes = group.group.limitMinutes,
                                                ),
                                            ),
                                        yesterdayValue =
                                            if (!hasYesterdayData) {
                                                yesterdayFallbackValue
                                            } else {
                                                yesterdayControlByName[group.group.name]?.let {
                                                    buildUsageSlashValue(
                                                        it.periodUsageMillisAtClose,
                                                        it.effectiveLimitMillisAtClose,
                                                    )
                                                } ?: yesterdayFallbackValue
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
                                            yesterdayFallbackValue
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
                                                yesterdayFallbackValue
                                            } else {
                                                yesterdayEncourageByName[group.group.name]?.let {
                                                    buildProgressSlashValue(
                                                        it.periodUsageMillisAtClose,
                                                        it.effectiveLimitMillisAtClose.coerceAtLeast(1L),
                                                        it.periodUsageMillisAtClose.toFloat() /
                                                            it.effectiveLimitMillisAtClose.coerceAtLeast(1L).toFloat(),
                                                    )
                                                } ?: yesterdayFallbackValue
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
                    numeratorYesterday = if (hasYesterdayData) formatDuration(yesterdayAnalysis.focus.numerator) else yesterdayFallbackValue,
                    denominatorLabel = AppText.t("stats_score_metric_control_usage_label"),
                    denominatorToday = formatDuration(analysis.focus.denominator),
                    denominatorYesterday = if (hasYesterdayData) formatDuration(yesterdayAnalysis.focus.denominator) else yesterdayFallbackValue,
                    ratioToday = analysis.focus.ratio,
                    ratioYesterday = yesterdayAnalysis.focus.ratio.takeIf { hasYesterdayData },
                    ratioYesterdayOverride = yesterdayFallbackValue.takeUnless { hasYesterdayData },
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
                                        yesterdayFallbackValue
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
                    numeratorYesterday = if (hasYesterdayData) yesterdayAnalysis.restraint.numerator.toString() else yesterdayFallbackValue,
                    denominatorLabel = AppText.t("stats_score_metric_control_launches_label"),
                    denominatorToday = analysis.restraint.denominator.toString(),
                    denominatorYesterday = if (hasYesterdayData) yesterdayAnalysis.restraint.denominator.toString() else yesterdayFallbackValue,
                    ratioToday = analysis.restraint.ratio,
                    ratioYesterday = yesterdayAnalysis.restraint.ratio.takeIf { hasYesterdayData },
                    ratioYesterdayOverride = yesterdayFallbackValue.takeUnless { hasYesterdayData },
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
    ratioYesterdayOverride: String? = null,
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
                    yesterdayValue = ratioYesterdayOverride ?: formatBehaviorRatioValue(ratioYesterday),
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

private fun buildHomeActivityRings(
    controlGroups: List<AppGroupWithApps>,
    encourageGroups: List<AppGroupWithApps>,
    periodUsageMap: Map<String, Long>,
    activeRewardEffects: List<ActiveRewardEffectEntity>,
    activeBonusMinutesByGroup: Map<String, Int>,
    todayEarnedPoints: Double,
    todayStepCount: Int,
    stepPointsRewardThreshold: Int,
    offlineFocusTodaySummary: OfflineFocusTodaySummary,
    offlineFocusDailyTargetMinutes: Int,
    ringPreferences: HomeActivityRingPreferences,
): HomeActivityRingsUiState {
    val validControlGroups = controlGroups.filter { it.group.limitMinutes > 0 }
    val nowMillis = System.currentTimeMillis()
    val controlGroupProgress =
        validControlGroups.map { group ->
            homeControlRingGroupProgress(
                usedMillis = periodUsageMap[group.group.id] ?: 0L,
                limitMillis = group.group.limitMinutes * 60_000L,
                limitPeriod = group.group.limitPeriod,
                nowMillis = nowMillis,
            )
        }
    val controlProgress =
        controlGroupProgress
            .map { it.progress }
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?.toFloat()
            ?: 0f
    val controlDetail =
        HomeActivityRingDetailUiState(
            groupCount = validControlGroups.size,
            usedMillis = controlGroupProgress.sumOf { it.usedMillis },
            targetMillis = controlGroupProgress.sumOf { it.limitMillis },
            expectedMillis = controlGroupProgress.sumOf { it.expectedMillis },
            periodElapsedProgress =
                controlGroupProgress
                    .map { it.periodElapsedProgress }
                    .takeIf { it.isNotEmpty() }
                    ?.average()
                    ?.toFloat()
                    ?: 0f,
            healthProgress =
                controlGroupProgress
                    .map { it.healthProgress }
                    .takeIf { it.isNotEmpty() }
                    ?.average()
                    ?.toFloat()
                    ?: 0f,
        )

    val validEncourageGroups = encourageGroups.filter { it.group.limitMinutes > 0 }
    val encourageUsedMillis =
        validEncourageGroups.sumOf { group -> periodUsageMap[group.group.id] ?: 0L }
    val encourageTargetMillis =
        validEncourageGroups.sumOf { group -> group.group.limitMinutes * 60_000L }
    val encourageProgress =
        validEncourageGroups
            .map { group ->
                val targetMillis = group.group.limitMinutes * 60_000L
                (periodUsageMap[group.group.id] ?: 0L).toFloat() / targetMillis.toFloat()
            }
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?.toFloat()
            ?.coerceAtLeast(0f)
            ?: 0f

    val growthTargetPoints =
        validEncourageGroups.sumOf { group ->
            calculateEncourageTargetPoints(
                targetMinutes = group.group.limitMinutes,
                pointsPerMinute = group.group.pointsPerMinute,
            )
        }
    val growthProgress =
        if (growthTargetPoints > 0.0) {
            (todayEarnedPoints / growthTargetPoints).toFloat().coerceAtLeast(0f)
        } else {
            0f
        }
    val stepTarget = stepPointsRewardThreshold.coerceAtLeast(0)
    val stepProgress =
        if (stepTarget > 0) {
            todayStepCount.toFloat() / stepTarget.toFloat()
        } else {
            0f
        }
    val focusTargetMillis = offlineFocusDailyTargetMinutes.coerceAtLeast(0) * 60_000L
    val focusProgress =
        if (focusTargetMillis > 0L) {
            offlineFocusTodaySummary.totalMillis.toFloat() / focusTargetMillis.toFloat()
        } else {
            0f
        }
    val controlAvailable = validControlGroups.isNotEmpty()
    val encourageAvailable = validEncourageGroups.isNotEmpty()
    val growthAvailable = growthTargetPoints > 0.0
    val stepAvailable = stepTarget > 0
    val focusAvailable = focusTargetMillis > 0L

    return HomeActivityRingsUiState(
        controlProgress = controlProgress.coerceIn(0f, 1f),
        encourageProgress = encourageProgress,
        growthProgress = growthProgress,
        stepProgress = stepProgress,
        focusProgress = focusProgress,
        controlAvailable = controlAvailable,
        encourageAvailable = encourageAvailable,
        growthAvailable = growthAvailable,
        stepAvailable = stepAvailable,
        focusAvailable = focusAvailable,
        growthTargetPoints = growthTargetPoints,
        stepCount = todayStepCount,
        stepTarget = stepTarget,
        focusMillis = offlineFocusTodaySummary.totalMillis,
        focusTargetMillis = focusTargetMillis,
        controlDetail = controlDetail,
        encourageDetail =
            HomeActivityRingDetailUiState(
                groupCount = validEncourageGroups.size,
                usedMillis = encourageUsedMillis,
                targetMillis = encourageTargetMillis,
            ),
        growthDetail =
            HomeActivityRingDetailUiState(
                groupCount = validEncourageGroups.size,
                earnedPoints = todayEarnedPoints,
                targetPoints = growthTargetPoints,
            ),
        outer = homeActivityRingSlot(ringPreferences.outer, controlProgress, encourageProgress, growthProgress, stepProgress, focusProgress, controlAvailable, encourageAvailable, growthAvailable, stepAvailable, focusAvailable),
        middle = homeActivityRingSlot(ringPreferences.middle, controlProgress, encourageProgress, growthProgress, stepProgress, focusProgress, controlAvailable, encourageAvailable, growthAvailable, stepAvailable, focusAvailable),
        inner = homeActivityRingSlot(ringPreferences.inner, controlProgress, encourageProgress, growthProgress, stepProgress, focusProgress, controlAvailable, encourageAvailable, growthAvailable, stepAvailable, focusAvailable),
    )
}

private fun homeActivityRingSlot(
    metric: HomeActivityRingMetric,
    controlProgress: Float,
    encourageProgress: Float,
    growthProgress: Float,
    stepProgress: Float,
    focusProgress: Float,
    controlAvailable: Boolean,
    encourageAvailable: Boolean,
    growthAvailable: Boolean,
    stepAvailable: Boolean,
    focusAvailable: Boolean,
): HomeActivityRingSlotUiState =
    when (metric) {
        HomeActivityRingMetric.CONTROL -> HomeActivityRingSlotUiState(metric, controlProgress.coerceIn(0f, 1f), controlAvailable)
        HomeActivityRingMetric.ENCOURAGE -> HomeActivityRingSlotUiState(metric, encourageProgress, encourageAvailable)
        HomeActivityRingMetric.GROWTH -> HomeActivityRingSlotUiState(metric, growthProgress, growthAvailable)
        HomeActivityRingMetric.STEPS -> HomeActivityRingSlotUiState(metric, stepProgress, stepAvailable)
        HomeActivityRingMetric.FOCUS -> HomeActivityRingSlotUiState(metric, focusProgress, focusAvailable)
    }

private data class HomeControlRingGroupProgress(
    val progress: Float,
    val usedMillis: Long,
    val limitMillis: Long,
    val expectedMillis: Long,
    val periodElapsedProgress: Float,
    val healthProgress: Float,
)

private fun homeControlRingGroupProgress(
    usedMillis: Long,
    limitMillis: Long,
    limitPeriod: LimitPeriod,
    nowMillis: Long,
): HomeControlRingGroupProgress {
    val limit = limitMillis.coerceAtLeast(1L)
    val bounds = homeActivityRingPeriodBounds(limitPeriod, nowMillis)
    val targetEndMillis = bounds.second
    val periodElapsedProgress =
        if (nowMillis >= targetEndMillis) {
            1f
        } else {
            ((nowMillis - bounds.first).toFloat() / (targetEndMillis - bounds.first).coerceAtLeast(1L).toFloat())
                .coerceIn(0f, 1f)
        }
    val expectedMillis = (limit * periodElapsedProgress).roundToLong().coerceIn(0L, limit)
    val healthProgress =
        when {
            usedMillis <= 0L -> 1f
            nowMillis >= targetEndMillis -> (limit.toFloat() / usedMillis.toFloat()).coerceIn(0f, 1f)
            usedMillis <= expectedMillis -> 1f
            else -> (expectedMillis.toFloat() / usedMillis.toFloat()).coerceIn(0f, 1f)
        }

    return HomeControlRingGroupProgress(
        progress = (periodElapsedProgress * healthProgress).coerceIn(0f, 1f),
        usedMillis = usedMillis,
        limitMillis = limitMillis,
        expectedMillis = expectedMillis,
        periodElapsedProgress = periodElapsedProgress,
        healthProgress = healthProgress,
    )
}

private fun homeActivityRingPeriodBounds(
    period: LimitPeriod,
    nowMillis: Long,
): Pair<Long, Long> {
    val zoneId = ZoneId.systemDefault()
    val dayStartHour = BusinessDay.cachedStartHour()
    val currentDate = BusinessDay.today(zoneId, dayStartHour, nowMillis)
    val startDate =
        when (period) {
            LimitPeriod.DAILY -> currentDate
            LimitPeriod.WEEKLY -> currentDate.minusDays(6)
            LimitPeriod.MONTHLY -> currentDate.withDayOfMonth(1)
        }
    val closeDate =
        when (period) {
            LimitPeriod.DAILY,
            LimitPeriod.WEEKLY -> currentDate
            LimitPeriod.MONTHLY -> currentDate.withDayOfMonth(currentDate.lengthOfMonth())
        }
    val closeMillis =
        closeDate
            .atTime(HOME_CONTROL_RING_CLOSE_HOUR, 0)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
    return BusinessDay.startOfDayMillis(startDate, zoneId, dayStartHour) to
        closeMillis
}

private fun buildHomeBattleActions(
    controlGroups: List<AppGroupWithApps>,
    encourageGroups: List<AppGroupWithApps>,
    usageMap: Map<String, Long>,
    activeRewardEffects: List<ActiveRewardEffectEntity>,
    activeBonusMinutesByGroup: Map<String, Int>,
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
                        activeBonusMinutesByGroup = activeBonusMinutesByGroup,
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
                val groupNames = homeBattleGroupSummary(risks.map { it.group.group.name })
                val streakText = AppText.t("home_battle_streak_compact", achievementProgress.controlStreak + 1)
                val detail =
                    when (level) {
                        HomeControlRiskLevel.HALF -> AppText.t("home_battle_control_groups_half", groupNames, achievementProgress.controlStreak + 1)
                        HomeControlRiskLevel.NEAR_LIMIT -> AppText.t("home_battle_control_groups_near_limit", groupNames, achievementProgress.controlStreak + 1)
                        HomeControlRiskLevel.DEPLETED -> AppText.t("home_battle_control_groups_depleted", groupNames, achievementProgress.controlStreak + 1)
                        HomeControlRiskLevel.OVER_LIMIT -> AppText.t("home_battle_control_groups_over_limit", groupNames, achievementProgress.controlStreak + 1)
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
                    subtitleStrikethroughText =
                        streakText.takeIf { level == HomeControlRiskLevel.OVER_LIMIT },
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
    val dayStartHour = BusinessDay.cachedStartHour()
    val todayStartMillis =
        BusinessDay.startOfDayMillis(
            BusinessDay.today(zoneId, dayStartHour),
            zoneId,
            dayStartHour,
        )
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

private fun homeBattleGroupSummary(groupNames: List<String>): String {
    val firstName = groupNames.firstOrNull().orEmpty()
    if (firstName.isEmpty()) return ""
    val extraCount = (groupNames.size - 1).coerceAtLeast(0)
    return if (extraCount == 0) {
        firstName
    } else {
        AppText.t("home_battle_group_name_more", firstName, extraCount)
    }
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
    activeBonusMinutesByGroup: Map<String, Int>,
    groupId: String,
    limitMinutes: Int,
): Long =
    (
        limitMinutes +
            (activeBonusMinutesByGroup[groupId] ?: 0) +
            activeRewardExtraMinutes(activeRewardEffects, groupId)
    ).coerceAtLeast(1) * 60_000L

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
    activeBonusMinutesByGroup: Map<String, Int>,
    groupId: String,
    usedMillis: Long,
    limitMinutes: Int,
): Boolean =
    hasActivePeriodPass(activeRewardEffects, groupId) ||
        usedMillis <= homeEffectiveControlLimitMillis(
            activeRewardEffects = activeRewardEffects,
            activeBonusMinutesByGroup = activeBonusMinutesByGroup,
            groupId = groupId,
            limitMinutes = limitMinutes,
        )

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
    activeBonusMinutesByGroup: Map<String, Int>,
): BehaviorScoreInputs {
    val controlPackageNames = controlGroups.flatMapTo(linkedSetOf()) { it.packageNames }
    val encouragePackageNames = encourageGroups.flatMapTo(linkedSetOf()) { it.packageNames }

    return BehaviorScoreInputs(
        controlGroups =
            controlGroups.map { group ->
                val usedMillis = periodUsageMap[group.group.id] ?: 0L
                BehaviorControlScoreInput(
                    usedMillis = usedMillis,
                    effectiveLimitMillis =
                        homeEffectiveControlLimitMillis(
                            activeRewardEffects = activeRewardEffects,
                            activeBonusMinutesByGroup = activeBonusMinutesByGroup,
                            groupId = group.group.id,
                            limitMinutes = group.group.limitMinutes,
                        ),
                    completed =
                        homeControlGroupCompleted(
                            activeRewardEffects = activeRewardEffects,
                            activeBonusMinutesByGroup = activeBonusMinutesByGroup,
                            groupId = group.group.id,
                            usedMillis = usedMillis,
                            limitMinutes = group.group.limitMinutes,
                        ),
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

private suspend fun buildYesterdayReportUiState(
    archiveRepository: DailyArchiveRepository,
    reportDate: LocalDate,
    context: Context,
): YesterdayReportUiState? {
    val archiveDate = reportDate.toString()
    val archive = archiveRepository.getArchiveByDate(archiveDate).first() ?: return null
    val groupArchives = archiveRepository.getGroupArchivesByDate(archiveDate).first()
    val appArchives = archiveRepository.getAppArchivesByDate(archiveDate).first()
    val snapshots = mergeArchivedAppSnapshots(appArchives)
    if (archive.totalUsageMillis <= 0L && snapshots.isEmpty() && groupArchives.isEmpty()) {
        return null
    }
    val previousArchive =
        archiveRepository
            .getRecentArchives(limit = 3650)
            .first()
            .filter { it.archiveDate < archive.archiveDate }
            .maxByOrNull { it.archiveDate }
    val previousGroupArchives =
        previousArchive?.let {
            archiveRepository.getGroupArchivesByDate(it.archiveDate).first()
        }.orEmpty()
    val previousAppArchives =
        previousArchive?.let {
            archiveRepository.getAppArchivesByDate(it.archiveDate).first()
        }.orEmpty()
    val previousSnapshots = mergeArchivedAppSnapshots(previousAppArchives)
    val controlPackageNames =
        appArchives
            .filter { it.groupType == GroupType.CONTROL }
            .mapTo(linkedSetOf()) { it.packageName }
    val encouragePackageNames =
        appArchives
            .filter { it.groupType == GroupType.ENCOURAGE }
            .mapTo(linkedSetOf()) { it.packageName }
    val previousControlPackageNames =
        previousAppArchives
            .filter { it.groupType == GroupType.CONTROL }
            .mapTo(linkedSetOf()) { it.packageName }
    val previousEncouragePackageNames =
        previousAppArchives
            .filter { it.groupType == GroupType.ENCOURAGE }
            .mapTo(linkedSetOf()) { it.packageName }
    val scoreMetrics =
        buildDailyBehaviorScoreMetrics(
            items = snapshots,
            groupArchives = groupArchives,
            controlPackageNames = controlPackageNames,
            encouragePackageNames = encouragePackageNames,
            previousItems = previousSnapshots,
            previousGroupArchives = previousGroupArchives,
            previousControlPackageNames = previousControlPackageNames,
            previousEncouragePackageNames = previousEncouragePackageNames,
        )
    val comparisonScoreMetrics =
        if (previousArchive == null) {
            emptyList()
        } else {
            buildDailyBehaviorScoreMetrics(
                items = previousSnapshots,
                groupArchives = previousGroupArchives,
                controlPackageNames = previousControlPackageNames,
                encouragePackageNames = previousEncouragePackageNames,
            )
        }
    val timelineBuckets = buildArchivedDayTimelineBuckets(snapshots)
    val peakBucket = timelineBuckets.maxByOrNull { it.deviceMillis }
    val topApps =
        snapshots
            .filter { it.usageMillis > 0L }
            .sortedByDescending { it.usageMillis }
            .take(3)
            .map {
                AppDisplayItem(
                    packageName = it.packageName,
                    label = it.label,
                    value = it.usageMillis,
                )
            }
    val locale = context.resources.configuration.locales[0] ?: java.util.Locale.getDefault()
    val reportLocalDate = runCatching { LocalDate.parse(archive.archiveDate) }.getOrElse { reportDate }
    val dateLabel =
        runCatching {
            reportLocalDate.format(java.time.format.DateTimeFormatter.ofPattern(AppText.t("home_mmm_d_eeee"), locale))
        }.getOrElse { archive.archiveDate }
    val averageScore =
        scoreMetrics
            .takeIf { it.isNotEmpty() }
            ?.map { it.score }
            ?.average()
            ?.roundToInt()
            ?: 0
    val messageKey =
        when {
            averageScore >= 85 -> "home_yesterday_report_message_great"
            averageScore >= 70 -> "home_yesterday_report_message_good"
            averageScore >= 55 -> "home_yesterday_report_message_steady"
            else -> "home_yesterday_report_message_reset"
        }
    val controlGroupCount = groupArchives.count { it.groupType == GroupType.CONTROL }
    val encourageGroupCount = groupArchives.count { it.groupType == GroupType.ENCOURAGE }

    return YesterdayReportUiState(
        archiveDate = archive.archiveDate,
        dateLabel = dateLabel,
        footerLine =
            AppText.t(
                "home_surprise_footer_format",
                context.getString(R.string.app_name),
                AppText.t(homeSurpriseKeyForDate(reportLocalDate)),
            ),
        message = AppText.t(messageKey),
        totalUsageLabel = formatDuration(archive.totalUsageMillis),
        savedLabel = formatDuration(archive.savedMillis),
        pointsNetLabel = formatSignedPointsLocal(archive.pointsNet),
        blockCountLabel = AppText.t("home_yesterday_report_count_times", archive.controlBlockEventCount),
        redemptionCountLabel = AppText.t("home_yesterday_report_count_times", archive.redemptionCount),
        controlCompletedLabel = AppText.t("home_yesterday_report_group_count", archive.controlCompletedGroupCount, controlGroupCount),
        controlExceededLabel = AppText.t("home_yesterday_report_group_count", archive.controlExceededGroupCount, controlGroupCount),
        encourageCompletedLabel = AppText.t("home_yesterday_report_group_count", archive.encourageCompletedGroupCount, encourageGroupCount),
        nightUsageLabel = formatDuration(snapshots.sumOf { it.nightUsageMillis }),
        peakPeriodLabel = peakBucket?.takeIf { it.deviceMillis > 0L }?.let { "${it.label} · ${formatDuration(it.deviceMillis)}" }
            ?: AppText.t("stats_no_records_yet"),
        topApps = topApps,
        totalUsageMillis = archive.totalUsageMillis,
        controlUsageMillis = archive.controlUsageMillis,
        encourageUsageMillis = archive.encourageUsageMillis,
        savedMillis = archive.savedMillis,
        scoreMetrics = scoreMetrics,
        comparisonScoreMetrics = comparisonScoreMetrics,
    )
}

private fun buildHomeOverviewUiState(
    context: android.content.Context,
    today: LocalDate,
    groupsWithApps: List<AppGroupWithApps>,
    usageMap: Map<String, Long>,
    periodUsageMap: Map<String, Long>,
    todayAppUsageMap: Map<String, Long>,
    todayAppOpenCountMap: Map<String, Int>,
    todaySessions: List<AppSession>,
    activeRewardEffects: List<ActiveRewardEffectEntity>,
    activeBonusMinutesByGroup: Map<String, Int>,
    recentGroupArchives: List<DailyGroupArchiveEntity>,
    yesterdayGroupArchives: List<DailyGroupArchiveEntity>,
    yesterdayAppArchives: List<DailyAppArchiveEntity>,
    historicalArchives: List<com.rrrrz.tinyvow.data.db.DailyArchiveEntity>,
    userPoints: Double,
    todayPoints: Double,
    todayEarnedPoints: Double,
    todayStepCount: Int,
    stepPointsRewardThreshold: Int,
    ringPreferences: HomeActivityRingPreferences,
    offlineFocusTodaySummary: OfflineFocusTodaySummary,
    offlineFocusDailyTargetMinutes: Int,
    achievementProgress: AchievementProgress,
    isYesterdayArchivePending: Boolean,
): HomeOverviewUiState {
    val controlGroups = groupsWithApps.filter { it.group.type == GroupType.CONTROL }
    val encourageGroups = groupsWithApps.filter { it.group.type == GroupType.ENCOURAGE && it.group.encourageMetric != EncourageMetric.STEPS }
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
                activeBonusMinutesByGroup = activeBonusMinutesByGroup,
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
            activeBonusMinutesByGroup = activeBonusMinutesByGroup,
        )
    val behaviorScoreAnalysis = analyzeBehaviorScores(behaviorScoreInputs)
    val behaviorScoreBreakdown = behaviorScoreAnalysis.breakdown
    val controlScoreRatio = if (controlGroups.isNotEmpty()) behaviorScoreBreakdown.guardScore / 100f else 0f
    val encourageScoreRatio = if (encourageGroups.isNotEmpty()) behaviorScoreBreakdown.gainScore / 100f else 0f
    val controlTodaySavedMinutes =
        controlGroups.sumOf { group ->
            val todayUsageMinutes = ((usageMap[group.group.id] ?: 0L) / 60_000L).toInt()
            val effectiveLimitMinutes =
                (homeEffectiveControlLimitMillis(
                    activeRewardEffects = activeRewardEffects,
                    activeBonusMinutesByGroup = activeBonusMinutesByGroup,
                    groupId = group.group.id,
                    limitMinutes = group.group.limitMinutes,
                ) / 60_000L).toInt()
            (effectiveLimitMinutes - todayUsageMinutes).coerceAtLeast(0)
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
    val controlOverviewGroups =
        controlGroups.map { group ->
            val extraMinutes =
                (activeBonusMinutesByGroup[group.group.id] ?: 0) +
                    activeRewardExtraMinutes(activeRewardEffects, group.group.id)
            HomeOverviewGroupDetailUiState(
                groupData = group,
                usedMillis = periodUsageMap[group.group.id] ?: 0L,
                targetMillis =
                    homeEffectiveControlLimitMillis(
                        activeRewardEffects = activeRewardEffects,
                        activeBonusMinutesByGroup = activeBonusMinutesByGroup,
                        groupId = group.group.id,
                        limitMinutes = group.group.limitMinutes,
                    ),
                extraMinutes = extraMinutes,
                hasPeriodPass = hasActivePeriodPass(activeRewardEffects, group.group.id),
            )
        }
    val encourageOverviewGroups =
        encourageGroups.map { group ->
            HomeOverviewGroupDetailUiState(
                groupData = group,
                usedMillis = periodUsageMap[group.group.id] ?: 0L,
                targetMillis = group.group.limitMinutes.coerceAtLeast(1) * 60_000L,
                pointsMultiplier = activeEncouragePointsMultiplier(activeRewardEffects, group.group.id),
            )
        }
    val activityRings =
        buildHomeActivityRings(
            controlGroups = controlGroups,
            encourageGroups = encourageGroups,
            periodUsageMap = periodUsageMap,
            activeRewardEffects = activeRewardEffects,
            activeBonusMinutesByGroup = activeBonusMinutesByGroup,
            todayEarnedPoints = todayEarnedPoints,
            todayStepCount = todayStepCount,
            stepPointsRewardThreshold = stepPointsRewardThreshold,
            offlineFocusTodaySummary = offlineFocusTodaySummary,
            offlineFocusDailyTargetMinutes = offlineFocusDailyTargetMinutes,
            ringPreferences = ringPreferences,
        )
    val totalSavedMinutes = historicalArchives.sumOf { it.savedMillis } / 60_000L
    val extendedLifeMinutes = totalSavedMinutes * 3L
    val totalEarnedPoints = historicalArchives.sumOf { it.pointsEarned } + todayEarnedPoints
    val behaviorScoreMetrics =
        buildRealtimeHomeBehaviorScoreMetrics(
            controlGroups = controlGroups,
            encourageGroups = encourageGroups,
            periodUsageMap = periodUsageMap,
            activeRewardEffects = activeRewardEffects,
            activeBonusMinutesByGroup = activeBonusMinutesByGroup,
            analysis = behaviorScoreAnalysis,
            yesterdayGroupArchives = yesterdayGroupArchives,
            yesterdayAppArchives = yesterdayAppArchives,
            isYesterdayArchivePending = isYesterdayArchivePending,
        )
    val hasYesterdayComparisonData = yesterdayGroupArchives.isNotEmpty() || yesterdayAppArchives.isNotEmpty()
    val behaviorComparisonMetrics =
        if (hasYesterdayComparisonData) {
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
        } else {
            emptyList()
        }
    val locale = context.resources.configuration.locales[0] ?: java.util.Locale.getDefault()
    val currentDate =
        today.format(
            java.time.format.DateTimeFormatter.ofPattern(AppText.t("home_mmm_d_eeee"), locale),
        )
    val totalUsageMillis = todayAppUsageMap.values.sum()
    val controlUsageMillis = controlGroups.sumOf { group -> periodUsageMap[group.group.id] ?: 0L }
    val encourageUsageMillis = encourageGroups.sumOf { group -> periodUsageMap[group.group.id] ?: 0L }
    val savedMillis = controlTodaySavedMinutes * 60_000L

    return HomeOverviewUiState(
        dateLabel = currentDate,
        tagline = AppText.t(homeSurpriseKeyForDate(today)),
        activityRings = activityRings,
        control =
            HomeControlOverviewUiState(
                todaySavedMinutes = controlTodaySavedMinutes,
                completedGroups = controlCompletedGroups,
                totalGroups = controlGroups.size,
                scoreRatio = controlScoreRatio,
                streakDays =
                    achievementProgress.controlStreak +
                        if (controlGroups.isNotEmpty() && controlCompletedGroups >= controlGroups.size) 1 else 0,
                streakLabel =
                    homeStreakLabel(
                        archivedStreak = achievementProgress.controlStreak,
                        todayCompleted = controlGroups.isNotEmpty() && controlCompletedGroups >= controlGroups.size,
                    ),
                groups = controlOverviewGroups,
            ),
        encourage =
            HomeEncourageOverviewUiState(
                todayEarnedPoints = todayEarnedPoints,
                completedGroups = encourageCompletedGroups,
                totalGroups = encourageGroups.size,
                scoreRatio = encourageScoreRatio,
                streakDays = achievementProgress.encourageStreak + if (encourageCompletedGroups > 0) 1 else 0,
                streakLabel =
                    homeStreakLabel(
                        archivedStreak = achievementProgress.encourageStreak,
                        todayCompleted = encourageCompletedGroups > 0,
                    ),
                pointsMultiplierLabel = encouragePointsMultiplierLabel,
                groups = encourageOverviewGroups,
            ),
        history =
            HomeHistoryOverviewUiState(
                totalSavedMinutes = totalSavedMinutes,
                extendedLifeMinutes = extendedLifeMinutes,
                totalEarnedPoints = totalEarnedPoints,
                currentPoints = userPoints,
            ),
        totalUsageMillis = totalUsageMillis,
        controlUsageMillis = controlUsageMillis,
        encourageUsageMillis = encourageUsageMillis,
        savedMillis = savedMillis,
        behaviorScoreMetrics = behaviorScoreMetrics,
        behaviorComparisonMetrics = behaviorComparisonMetrics,
        battleActions =
            buildHomeBattleActions(
                controlGroups = controlGroups,
                encourageGroups = encourageGroups,
                usageMap = periodUsageMap,
                activeRewardEffects = activeRewardEffects,
                activeBonusMinutesByGroup = activeBonusMinutesByGroup,
                recentGroupArchives = recentGroupArchives,
                achievementProgress = achievementProgress,
            ),
    )
}

private fun currentBusinessDay(): LocalDate = BusinessDay.today(ZoneId.systemDefault(), BusinessDay.cachedStartHour())

private fun isHomeYesterdayArchivePending(
    archiveState: DailyArchiveStateEntity?,
    today: LocalDate,
    yesterdayArchiveDate: String,
    hasYesterdayData: Boolean,
    usageAccessStatus: UsageAccessStatus,
): Boolean {
    if (usageAccessStatus != UsageAccessStatus.GRANTED || hasYesterdayData) {
        return false
    }
    val yesterday = LocalDate.parse(yesterdayArchiveDate)
    if (!today.isAfter(yesterday)) {
        return false
    }
    if (archiveState == null) {
        return true
    }
    val archiveStartDate = LocalDate.parse(archiveState.archiveStartDate)
    if (yesterday.isBefore(archiveStartDate)) {
        return false
    }
    val lastArchivedDate = archiveState.lastArchivedDate?.let(LocalDate::parse)
    return lastArchivedDate == null || lastArchivedDate.isBefore(yesterday)
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
    TinyVowDetailScaffold(
        title = stringResource(R.string.action_diagnostic_settings),
        onBack = onBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                TinyVowButton(
                    onClick = onOpenAccessibilitySettings,
                    tone = TinyVowButtonTone.Primary,
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
                TinyVowButton(
                    onClick = onRequestNotificationPermission,
                    tone = TinyVowButtonTone.Primary,
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

            TinyVowButton(
                onClick = onOpenUsageAccessSettings,
                tone = if (usageAccessGranted) TinyVowButtonTone.Neutral else TinyVowButtonTone.Primary,
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
                TinyVowButton(
                    onClick = onOpenAutoStartSettings,
                    tone = TinyVowButtonTone.Primary,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = stringResource(R.string.autostart_card_action))
                }
                TinyVowButton(
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
    TinyVowStatusPill(
        text = text,
        color = color,
        containerColor = color.copy(alpha = 0.12f),
    )
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
                TinyVowButton(
                    onClick = onRequestBatteryOptimization,
                    tone = TinyVowButtonTone.Primary,
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

@Composable
private fun <T> collectNullableAsStateWithLifecycle(
    flow: Flow<T>,
    lifecycle: Lifecycle,
): State<T?> =
    produceState<T?>(initialValue = null, flow, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { value = it }
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
