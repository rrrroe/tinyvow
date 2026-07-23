package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.i18n.AppText

import com.rrrrz.tinyvow.BuildConfig
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.data.auth.UserSession
import com.rrrrz.tinyvow.data.account.BackendAccount
import com.rrrrz.tinyvow.data.billing.ProEntitlementState
import com.rrrrz.tinyvow.data.billing.ProEntitlementStatus
import com.rrrrz.tinyvow.data.billing.SubscriptionOffer
import com.rrrrz.tinyvow.data.pro.ProFeatureGate
import com.rrrrz.tinyvow.data.repository.DailyCheckInDayState
import com.rrrrz.tinyvow.data.repository.DailyCheckInMonthState
import com.rrrrz.tinyvow.data.settings.AppTextSize
import com.rrrrz.tinyvow.data.settings.HomeActivityRingColorPreference
import com.rrrrz.tinyvow.data.settings.HomeActivityRingColorPreferences
import com.rrrrz.tinyvow.data.settings.HomeActivityRingColorSource
import com.rrrrz.tinyvow.data.settings.HomeActivityRingMetric
import com.rrrrz.tinyvow.data.settings.HomeActivityRingPreferences
import com.rrrrz.tinyvow.data.settings.HomeActivityRingSlot
import com.rrrrz.tinyvow.data.time.BusinessDay
import com.rrrrz.tinyvow.data.supermode.SuperModeStatus
import com.rrrrz.tinyvow.i18n.AppLanguage
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.ThemeSeed
import com.rrrrz.tinyvow.ui.theme.TinyVowButton
import com.rrrrz.tinyvow.ui.theme.TinyVowButtonTone
import com.rrrrz.tinyvow.ui.theme.TinyVowCard
import com.rrrrz.tinyvow.ui.theme.TinyVowDetailScaffold
import com.rrrrz.tinyvow.ui.theme.TinyVowElevation
import com.rrrrz.tinyvow.ui.theme.TinyVowPageBackground
import com.rrrrz.tinyvow.ui.theme.TinyVowRadius
import com.rrrrz.tinyvow.ui.theme.TinyVowSectionHeader
import com.rrrrz.tinyvow.ui.theme.TinyVowSettingsDivider
import com.rrrrz.tinyvow.ui.theme.TinyVowSettingsGroup
import com.rrrrz.tinyvow.ui.theme.TinyVowSettingsItem
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import com.rrrrz.tinyvow.ui.theme.selectedThemeDisplayName
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToLong

@Composable
fun MeScreen(
    userSession: UserSession?,
    backendAccount: BackendAccount?,
    isBackendAccountEnabled: Boolean,
    isGoogleSignInEnabled: Boolean,
    isGoogleSignInConfigured: Boolean,
    isPlayBillingEnabled: Boolean,
    isLocalActivationEnabled: Boolean,
    appVersionName: String,
    appVersionCode: Int,
    proEntitlement: ProEntitlementState,
    subscriptionOffers: List<SubscriptionOffer>,
    totalSavedMinutes: Long,
    totalEarnedPoints: Double,
    profileDisplayName: String?,
    profileAvatarUri: String?,
    selectedThemeId: String,
    customThemes: List<ThemeSeed>,
    isProActive: Boolean,
    superModeStatus: SuperModeStatus,
    isDebugBuild: Boolean,
    selectedAppLanguage: AppLanguage,
    selectedAppTextSize: AppTextSize,
    dayBoundaryHour: Int,
    notificationRemindersEnabled: Boolean,
    controlRemainingReminderMinutes: Int,
    encourageReminderTimesMinutes: List<Int>,
    openBenefitsDialog: Boolean,
    onBenefitsDialogOpened: () -> Unit,
    onNavigateToProMembership: () -> Unit,
    onNavigateToAccount: () -> Unit,
    usageAccessGranted: Boolean,
    accessibilityServiceEnabled: Boolean,
    isAutoStartDismissed: Boolean,
    isIgnoringBattery: Boolean,
    notificationPermissionGranted: Boolean,
    dismissedPermissionPrompts: Set<String>,
    onSelectTheme: (String) -> Unit,
    onSaveCustomTheme: (ThemeSeed) -> Unit,
    onDeleteCustomTheme: (String) -> Unit,
    onShowProUpsell: (ProUpsellSource) -> Unit,
    onOpenSuperModeSettings: () -> Unit,
    onOpenUsageAccessSettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenAutoStartSettings: () -> Unit,
    onSetAutoStartDismissed: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onClearDismissedPermissionPrompts: () -> Unit,
    onNavigateToPermissionSettings: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToLaboratory: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToCheckInOverview: () -> Unit,
    onNavigateToSavedProgressStats: () -> Unit,
    onNavigateToPointsProgressStats: () -> Unit,
    onNavigateToThemeSettings: () -> Unit,
    onNavigateToAppearanceSettings: () -> Unit,
    onNavigateToLanguageSettings: () -> Unit,
    onNavigateToDayBoundarySettings: () -> Unit,
    onNavigateToOfflineFocusSettings: () -> Unit,
    onNavigateToHelpFeedback: () -> Unit,
    onNavigateToContactUs: () -> Unit,
    onNavigateToSpecialAppSettings: () -> Unit,
    onNavigateToDataPrivacy: () -> Unit,
    onNavigateToVersionInfo: () -> Unit,
    onSignInWithGoogle: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: (clearLocalData: Boolean) -> Unit,
    onPurchasePro: (SubscriptionOffer) -> Unit,
    onRestorePurchases: () -> Unit,
    onManageSubscription: () -> Unit,
    onActivateProCode: (String) -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val themeColors = LocalThemeColors.current
    var showDeleteAccountConfirm by remember { mutableStateOf(false) }

    val effectiveAvatar = backendAccount?.avatarUrl ?: profileAvatarUri ?: userSession?.avatarUrl
    val canTapToSignIn = isGoogleSignInEnabled && userSession == null && isGoogleSignInConfigured
    val isProMember = isProActive
    val appUsageDays = remember(context) { calculateInstalledDays(context) }
    val currentThemeName = selectedThemeDisplayName(selectedThemeId, customThemes)
    val displayName =
        backendAccount?.displayName
            ?: profileDisplayName
            ?: userSession?.displayName
            ?: if (isLocalActivationEnabled) {
                AppText.t("me_local_account_default_name")
            } else {
                AppText.t("me_not_signed_in")
            }
    val subtitle: String? =
        when {
            backendAccount?.isRegistered == true -> backendAccount.email
            !userSession?.email.isNullOrBlank() -> userSession?.email.orEmpty()
            isLocalActivationEnabled -> AppText.t("me_local_user")
            isGoogleSignInEnabled -> null
            else -> AppText.t("me_china_local_mode_subtitle")
        }
    val showInlineProMark = isLocalActivationEnabled && userSession?.email.isNullOrBlank()
    val displayAppVersion = userFacingVersionName(appVersionName)
    LaunchedEffect(openBenefitsDialog) {
        if (openBenefitsDialog) {
            onBenefitsDialogOpened()
            onNavigateToProMembership()
        }
    }

    TinyVowPageBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(themeColors.base)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(
                            enabled = isBackendAccountEnabled || canTapToSignIn,
                            onClick = {
                                if (isBackendAccountEnabled) {
                                    onNavigateToAccount()
                                } else {
                                    onSignInWithGoogle()
                                }
                            },
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ProfileAvatar(
                        avatar = effectiveAvatar,
                        contentDescription = AppText.t("me_profile_avatar"),
                        size = 72.dp,
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleLarge,
                                color = themeColors.onBase,
                            )
                            if (showInlineProMark) {
                                ProMemberBadge(isActive = isProMember)
                            }
                        }
                        subtitle?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = themeColors.onBase.copy(alpha = 0.78f),
                            )
                        }
                    }
                }
                when {
                    isGoogleSignInEnabled && userSession != null -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TinyVowButton(
                                text = AppText.t("me_sign_out"),
                                onClick = onSignOut,
                            )
                            TinyVowButton(
                                text = AppText.t("me_delete_account"),
                                onClick = { showDeleteAccountConfirm = true },
                                tone = TinyVowButtonTone.Danger,
                            )
                        }
                    }
                }
                if (!isLocalActivationEnabled && !isGoogleSignInConfigured && userSession == null) {
                    Text(
                        text = AppText.t("me_google_sign_in_is_not_configured"),
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.onBase.copy(alpha = 0.78f),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = TinyVowSpacing.PageHorizontal)
                .offset(y = (-40).dp),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.SectionGap),
        ) {
            TinyVowCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TinyVowRadius.Card),
                shadowElevation = TinyVowElevation.FeaturedCard,
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = TinyVowSpacing.CardHorizontal,
                        vertical = TinyVowSpacing.CardVertical,
                    ),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MeStatItem(
                        value = formatMetricNumber(totalSavedMinutes),
                        label = AppText.t("me_total_saved_minutes"),
                        color = themeColors.control,
                        onClick = onNavigateToSavedProgressStats,
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    MeStatItem(
                        value = formatMetricNumber(totalEarnedPoints.roundToLong()),
                        label = AppText.t("me_total_earned_points"),
                        color = themeColors.encourage,
                        onClick = onNavigateToPointsProgressStats,
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    MeStatItem(
                        value = formatMetricNumber(appUsageDays),
                        label = AppText.t("me_app_usage_days"),
                        color = themeColors.base,
                        onClick = onNavigateToCheckInOverview,
                    )
                }
            }

            TinyVowCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TinyVowRadius.Card),
                shadowElevation = TinyVowElevation.Card,
            ) {
                SubscriptionStatusPanel(
                    entitlement = proEntitlement,
                    offers = subscriptionOffers,
                    isLocalActivationEnabled = isLocalActivationEnabled,
                    localUserId = userSession?.userId,
                    onOpenMembershipPage = onNavigateToProMembership,
                    onPurchasePro = onPurchasePro,
                    onRestorePurchases = onRestorePurchases,
                    onActivateProCode = onActivateProCode,
                )
            }

            MeMenuSection(title = AppText.t("me_feature_settings_section")) {
                if (BuildConfig.DEBUG) {
                    MeMenuItem(
                        icon = Icons.Default.AccessTime,
                        title = AppText.t("day_boundary_settings_title"),
                        trailingText = AppText.t("day_boundary_hour_value", dayBoundaryHour),
                        onClick = onNavigateToDayBoundarySettings,
                    )
                    SettingsDivider()
                }
                MeMenuItem(
                    icon = Icons.Default.Notifications,
                    title = AppText.t("notification_settings_title"),
                    trailingText = if (notificationRemindersEnabled) {
                        AppText.t("notification_settings_enabled")
                    } else {
                        AppText.t("notification_settings_disabled")
                    },
                    onClick = onNavigateToNotificationSettings,
                )
                SettingsDivider()
                MeMenuItem(
                    icon = Icons.Default.Security,
                    title = AppText.t("me_permission_settings"),
                    onClick = onNavigateToPermissionSettings,
                )
                SettingsDivider()
                MeMenuItem(
                    icon = Icons.Default.VerifiedUser,
                    title = AppText.t("super_mode_title"),
                    trailingText = describeSuperModeStatus(superModeStatus),
                    onClick = onOpenSuperModeSettings,
                )
                SettingsDivider()
                MeMenuItem(
                    icon = Icons.Default.Apps,
                    title = AppText.t("special_app_settings_title"),
                    onClick = onNavigateToSpecialAppSettings,
                )
                SettingsDivider()
                MeMenuItem(
                    icon = Icons.Default.Timer,
                    title = AppText.t("offline_focus_settings_title"),
                    trailingText = AppText.t("offline_focus_settings_badge"),
                    titleTrailingContent = {
                        ProMemberBadge(isActive = isProActive)
                    },
                    onClick = onNavigateToOfflineFocusSettings,
                )
            }

            MeMenuSection(title = AppText.t("me_preferences_section")) {
                MeMenuItem(
                    icon = Icons.Default.Palette,
                    title = AppText.t("me_theme_management"),
                    trailingText = currentThemeName,
                    onClick = onNavigateToThemeSettings,
                )
                SettingsDivider()
                MeMenuItem(
                    icon = Icons.Default.Settings,
                    title = AppText.t("appearance_settings_title"),
                    trailingText = selectedAppTextSize.displayName(),
                    onClick = onNavigateToAppearanceSettings,
                )
                SettingsDivider()
                MeMenuItem(
                    icon = Icons.Default.Language,
                    title = AppText.t("selected_language_title"),
                    trailingText = selectedAppLanguage.displayName(),
                    onClick = onNavigateToLanguageSettings,
                )
            }

            MeMenuSection(title = AppText.t("me_data_and_privacy")) {
                MeMenuItem(
                    icon = Icons.Default.History,
                    title = AppText.t("me_usage_history"),
                    onClick = onNavigateToHistory,
                )
                SettingsDivider()
                MeMenuItem(
                    icon = Icons.Default.Storage,
                    title = AppText.t("me_local_data_management"),
                    onClick = onNavigateToDataPrivacy,
                )
            }

            MeMenuSection(title = AppText.t("me_help_and_contact")) {
                MeMenuItem(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = AppText.t("me_help_and_feedback"),
                    onClick = onNavigateToHelpFeedback,
                )
                SettingsDivider()
                MeMenuItem(
                    icon = Icons.Default.Email,
                    title = AppText.t("me_contact_us"),
                    onClick = onNavigateToContactUs,
                )
                SettingsDivider()
                MeMenuItem(
                    icon = Icons.Default.Info,
                    title = AppText.t("me_app_version"),
                    trailingText = displayAppVersion,
                    onClick = onNavigateToVersionInfo,
                )
            }

            if (isDebugBuild) {
                MeMenuSection(title = AppText.t("me_advanced_center")) {
                    MeMenuItem(
                        icon = Icons.Default.Science,
                        title = AppText.t("me_advanced_center"),
                        onClick = onNavigateToLaboratory,
                        color = themeColors.base,
                    )
                }
            }
        }
        }
    }

    if (showDeleteAccountConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountConfirm = false },
            title = { Text(AppText.t("me_delete_account")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(AppText.t("me_the_first_version_deletes_the_sign_in_session"))
                    TextButton(
                        onClick = {
                            showDeleteAccountConfirm = false
                            onDeleteAccount(true)
                        },
                    ) {
                        Text(AppText.t("me_delete_account_and_clear_local_data"), color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountConfirm = false
                        onDeleteAccount(false)
                    },
                ) {
                    Text(AppText.t("me_delete_account_only"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountConfirm = false }) {
                    Text(AppText.t("group_cancel"))
                }
            },
        )
    }

}

private fun AppLanguage.displayName(): String =
    when (this) {
        AppLanguage.SYSTEM -> AppText.t("selected_language_system")
        AppLanguage.ZH_CN -> AppText.t("selected_language_zh_cn")
        AppLanguage.EN -> AppText.t("selected_language_en")
    }

private fun AppTextSize.displayName(): String =
    when (this) {
        AppTextSize.EXTRA_SMALL -> AppText.t("appearance_text_size_extra_small")
        AppTextSize.SMALL -> AppText.t("appearance_text_size_small")
        AppTextSize.STANDARD -> AppText.t("appearance_text_size_standard")
        AppTextSize.LARGE -> AppText.t("appearance_text_size_large")
    }

@Composable
fun AppearanceSettingsScreen(
    isProActive: Boolean,
    selectedAppTextSize: AppTextSize,
    onBack: () -> Unit,
    onSelectAppTextSize: (AppTextSize) -> Unit,
    onOpenRingSettings: () -> Unit,
    onShowProUpsell: (ProUpsellSource) -> Unit,
) {
    MeDetailPageScaffold(
        title = AppText.t("appearance_settings_title"),
        description = AppText.t("appearance_settings_description"),
        onBack = onBack,
    ) {
        MeSettingsCard(title = AppText.t("appearance_text_size_title")) {
            Text(
                text = AppText.t("appearance_text_size_description"),
                style = MaterialTheme.typography.bodySmall,
                color = LocalThemeColors.current.inkMuted,
            )
            AppTextSize.entries.forEachIndexed { index, textSize ->
                val supportingText =
                    when (textSize) {
                        AppTextSize.EXTRA_SMALL -> AppText.t("appearance_text_size_extra_small_hint")
                        AppTextSize.STANDARD -> AppText.t("appearance_text_size_default")
                        else -> null
                    }
                if (index > 0) {
                    SettingsDivider()
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectAppTextSize(textSize) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = textSize.displayName(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight =
                                if (textSize == selectedAppTextSize) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.Normal
                                },
                            color = LocalThemeColors.current.inkStrong,
                        )
                        supportingText?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = LocalThemeColors.current.inkMuted,
                            )
                        }
                    }
                    RadioButton(
                        selected = textSize == selectedAppTextSize,
                        onClick = { onSelectAppTextSize(textSize) },
                    )
                }
            }
        }

        MeSettingsCard {
            MeMenuItem(
                icon = Icons.Default.Settings,
                title = AppText.t("ring_settings_title"),
                trailingContent = {
                    ProMemberBadge(isActive = isProActive)
                },
                onClick = {
                    if (isProActive) {
                        onOpenRingSettings()
                    } else {
                        onShowProUpsell(ProUpsellSource.RING_SETTINGS)
                    }
                },
            )
        }
    }
}

@Composable
fun RingSettingsScreen(
    ringPreferences: HomeActivityRingPreferences,
    ringColorPreferences: HomeActivityRingColorPreferences,
    offlineFocusDailyTargetMinutes: Int,
    onBack: () -> Unit,
    onSelectRingMetric: (HomeActivityRingSlot, HomeActivityRingMetric) -> Unit,
    onSelectRingMetricColor: (HomeActivityRingMetric, HomeActivityRingColorSource, Int?) -> Unit,
    onSaveOfflineFocusDailyTarget: (Int) -> Unit,
) {
    var focusTargetText by remember(offlineFocusDailyTargetMinutes) { mutableStateOf(offlineFocusDailyTargetMinutes.toString()) }
    val parsedFocusTarget = focusTargetText.toIntOrNull()?.coerceAtLeast(0)

    MeDetailPageScaffold(
        title = AppText.t("ring_settings_title"),
        description = AppText.t("ring_settings_description"),
        onBack = onBack,
    ) {
        MeSettingsCard(title = AppText.t("ring_settings_layout_section")) {
            RingSlotSelector(
                title = AppText.t("ring_settings_outer_ring"),
                selected = ringPreferences.outer,
                onSelect = { onSelectRingMetric(HomeActivityRingSlot.OUTER, it) },
            )
            SettingsDivider()
            RingSlotSelector(
                title = AppText.t("ring_settings_middle_ring"),
                selected = ringPreferences.middle,
                onSelect = { onSelectRingMetric(HomeActivityRingSlot.MIDDLE, it) },
            )
            SettingsDivider()
            RingSlotSelector(
                title = AppText.t("ring_settings_inner_ring"),
                selected = ringPreferences.inner,
                onSelect = { onSelectRingMetric(HomeActivityRingSlot.INNER, it) },
            )
        }

        MeSettingsCard(title = AppText.t("ring_settings_metric_section")) {
            RingMetricColorSelector(
                metric = HomeActivityRingMetric.CONTROL,
                title = ringMetricLabel(HomeActivityRingMetric.CONTROL),
                body = AppText.t("ring_settings_control_explanation"),
                preference = ringColorPreferences.control,
                onSelectColor = onSelectRingMetricColor,
            )
            SettingsDivider()
            RingMetricColorSelector(
                metric = HomeActivityRingMetric.ENCOURAGE,
                title = ringMetricLabel(HomeActivityRingMetric.ENCOURAGE),
                body = AppText.t("ring_settings_encourage_explanation"),
                preference = ringColorPreferences.encourage,
                onSelectColor = onSelectRingMetricColor,
            )
            SettingsDivider()
            RingMetricColorSelector(
                metric = HomeActivityRingMetric.GROWTH,
                title = ringMetricLabel(HomeActivityRingMetric.GROWTH),
                body = AppText.t("ring_settings_growth_explanation"),
                preference = ringColorPreferences.growth,
                onSelectColor = onSelectRingMetricColor,
            )
            RingMetricColorSelector(
                metric = HomeActivityRingMetric.FOCUS,
                title = ringMetricLabel(HomeActivityRingMetric.FOCUS),
                body = AppText.t("ring_settings_focus_explanation"),
                preference = ringColorPreferences.focus,
                onSelectColor = onSelectRingMetricColor,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = focusTargetText,
                    onValueChange = { focusTargetText = sanitizePreferenceIntegerInput(it) },
                    label = { Text(AppText.t("ring_settings_focus_daily_target_label")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                TinyVowButton(
                    text = AppText.t("ring_settings_save_focus"),
                    onClick = {
                        parsedFocusTarget?.let(onSaveOfflineFocusDailyTarget)
                    },
                    enabled = parsedFocusTarget != null,
                    tone = TinyVowButtonTone.Primary,
                )
            }
        }
    }
}

@Composable
private fun RingSlotSelector(
    title: String,
    selected: HomeActivityRingMetric,
    onSelect: (HomeActivityRingMetric) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = LocalThemeColors.current.inkStrong,
            )
            Text(
                text = ringMetricShortDescription(selected),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(ringMetricLabel(selected))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                HomeActivityRingMetric.entries.filterNot { it == HomeActivityRingMetric.STEPS }.forEach { metric ->
                    DropdownMenuItem(
                        text = { Text(ringMetricLabel(metric)) },
                        onClick = {
                            expanded = false
                            onSelect(metric)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RingMetricColorSelector(
    metric: HomeActivityRingMetric,
    title: String,
    body: String,
    preference: HomeActivityRingColorPreference,
    onSelectColor: (HomeActivityRingMetric, HomeActivityRingColorSource, Int?) -> Unit,
) {
    val themeColors = LocalThemeColors.current
    var customColorText by remember(metric, preference.customArgb) {
        mutableStateOf(formatRingCustomColor(preference.customArgb))
    }
    val parsedCustomColor = parseRingCustomColor(customColorText)
    val selectedColor =
        when (preference.source) {
            HomeActivityRingColorSource.CONTROL -> themeColors.control
            HomeActivityRingColorSource.ENCOURAGE -> themeColors.encourage
            HomeActivityRingColorSource.THEME -> themeColors.base
            HomeActivityRingColorSource.CUSTOM -> Color(preference.customArgb ?: DEFAULT_RING_CUSTOM_COLOR)
        }
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.inkStrong,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    RingColorSwatch(color = selectedColor)
                    Spacer(Modifier.width(8.dp))
                    Text(ringColorSourceLabel(preference.source))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    HomeActivityRingColorSource.entries.forEach { source ->
                        val sourceColor =
                            when (source) {
                                HomeActivityRingColorSource.CONTROL -> themeColors.control
                                HomeActivityRingColorSource.ENCOURAGE -> themeColors.encourage
                                HomeActivityRingColorSource.THEME -> themeColors.base
                                HomeActivityRingColorSource.CUSTOM -> Color(parsedCustomColor ?: preference.customArgb ?: DEFAULT_RING_CUSTOM_COLOR)
                            }
                        val enabled = source != HomeActivityRingColorSource.CUSTOM || parsedCustomColor != null
                        DropdownMenuItem(
                            text = { Text(ringColorSourceLabel(source)) },
                            leadingIcon = { RingColorSwatch(color = sourceColor) },
                            enabled = enabled,
                            onClick = {
                                expanded = false
                                onSelectColor(
                                    metric,
                                    source,
                                    if (source == HomeActivityRingColorSource.CUSTOM) parsedCustomColor else preference.customArgb,
                                )
                            },
                        )
                    }
                }
            }
        }
        if (preference.source == HomeActivityRingColorSource.CUSTOM) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = customColorText,
                    onValueChange = { customColorText = sanitizeRingCustomColorInput(it) },
                    label = { Text(AppText.t("ring_settings_custom_color_label")) },
                    singleLine = true,
                    isError = parsedCustomColor == null,
                    modifier = Modifier.weight(1f),
                )
                TinyVowButton(
                    text = AppText.t("ring_settings_save_color"),
                    onClick = {
                        parsedCustomColor?.let {
                            onSelectColor(metric, HomeActivityRingColorSource.CUSTOM, it)
                        }
                    },
                    enabled = parsedCustomColor != null,
                    tone = TinyVowButtonTone.Neutral,
                )
            }
        }
    }
}

@Composable
private fun RingColorSwatch(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(16.dp),
        shape = CircleShape,
        color = color,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        content = {},
    )
}

private fun ringMetricLabel(metric: HomeActivityRingMetric): String =
    AppText.t(
        when (metric) {
            HomeActivityRingMetric.CONTROL -> "home_activity_ring_control_label"
            HomeActivityRingMetric.ENCOURAGE -> "home_activity_ring_encourage_label"
            HomeActivityRingMetric.GROWTH -> "home_activity_ring_growth_label"
            HomeActivityRingMetric.STEPS -> "home_activity_ring_steps_label"
            HomeActivityRingMetric.FOCUS -> "home_activity_ring_focus_label"
        },
    )

private fun ringMetricShortDescription(metric: HomeActivityRingMetric): String =
    AppText.t(
        when (metric) {
            HomeActivityRingMetric.CONTROL -> "ring_settings_control_short"
            HomeActivityRingMetric.ENCOURAGE -> "ring_settings_encourage_short"
            HomeActivityRingMetric.GROWTH -> "ring_settings_growth_short"
            HomeActivityRingMetric.STEPS -> "ring_settings_steps_short"
            HomeActivityRingMetric.FOCUS -> "ring_settings_focus_short"
        },
    )

private fun ringColorSourceLabel(source: HomeActivityRingColorSource): String =
    AppText.t(
        when (source) {
            HomeActivityRingColorSource.CONTROL -> "ring_settings_color_control"
            HomeActivityRingColorSource.ENCOURAGE -> "ring_settings_color_encourage"
            HomeActivityRingColorSource.THEME -> "ring_settings_color_theme"
            HomeActivityRingColorSource.CUSTOM -> "ring_settings_color_custom"
        },
    )

private fun sanitizePreferenceIntegerInput(value: String): String =
    buildString {
        value.forEach { char ->
            if (char.isDigit()) append(char)
        }
    }.take(6)

private fun sanitizePreferenceDecimalInput(value: String): String {
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

private fun formatPreferenceDecimal(value: Double): String =
    java.math.BigDecimal.valueOf(value.coerceAtLeast(0.0)).stripTrailingZeros().toPlainString()

private fun sanitizeRingCustomColorInput(value: String): String {
    val raw = value.trim().removePrefix("#")
    val hex = raw
        .uppercase(Locale.US)
        .filter { it in '0'..'9' || it in 'A'..'F' }
        .take(6)
    return "#$hex"
}

private fun parseRingCustomColor(value: String): Int? {
    val hex = value.trim().removePrefix("#")
    if (hex.length != 6) return null
    return runCatching {
        (0xFF000000L or hex.toLong(16)).toInt()
    }.getOrNull()
}

private fun formatRingCustomColor(argb: Int?): String =
    String.format(Locale.US, "#%06X", (argb ?: DEFAULT_RING_CUSTOM_COLOR) and 0x00FFFFFF)

private const val DEFAULT_RING_CUSTOM_COLOR: Int = 0xFF6B8EF2.toInt()

@Composable
private fun SettingsDivider() {
    TinyVowSettingsDivider()
}

@Composable
internal fun ProfileAvatar(
    avatar: String?,
    contentDescription: String,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        if (avatar.isNullOrBlank()) {
            Image(
                painter = painterResource(R.mipmap.ic_launcher_foreground),
                contentDescription = contentDescription,
                modifier = Modifier
                    .padding(4.dp)
                    .clip(CircleShape),
            )
        } else {
            AsyncImage(
                model = avatar,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                fallback = painterResource(R.mipmap.ic_launcher_foreground),
                error = painterResource(R.mipmap.ic_launcher_foreground),
            )
        }
    }
}

@Composable
fun ProMemberBadge(
    isActive: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(
            if (isActive) {
                R.drawable.tinyvow_pro_mark_gold
            } else {
                R.drawable.tinyvow_pro_mark_gray
            },
        ),
        contentDescription = null,
        modifier = modifier.size(20.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DataPrivacyPage(
    onBack: () -> Unit,
    onSaveLocalBackup: () -> Unit,
    onShareLocalBackup: () -> Unit,
    onImportLocalBackup: () -> Unit,
    onClearLocalData: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
) {
    var showClearConfirm by remember { mutableStateOf(false) }
    var showExportChoice by remember { mutableStateOf(false) }

    MeDetailPageScaffold(
        title = AppText.t("me_local_data_management"),
        description = AppText.t("me_tiny_vow_stores_the_apps_you_manage_usage"),
        onBack = onBack,
    ) {
        MeSettingsCard(title = AppText.t("me_local_data_management")) {
            Text(
                text = AppText.t("me_export_files_are_created_only_in_local_cache"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = { showExportChoice = true }, modifier = Modifier.fillMaxWidth()) {
                Text(AppText.t("me_export_recoverable_backup"))
            }
            OutlinedButton(onClick = onImportLocalBackup, modifier = Modifier.fillMaxWidth()) {
                Text(AppText.t("me_import_local_backup"))
            }
            TextButton(onClick = onOpenPrivacyPolicy, modifier = Modifier.fillMaxWidth()) {
                Text(AppText.t("me_view_privacy_policy"))
            }
            TextButton(onClick = { showClearConfirm = true }, modifier = Modifier.fillMaxWidth()) {
                Text(AppText.t("me_clear_local_data"), color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showExportChoice) {
        AlertDialog(
            onDismissRequest = { showExportChoice = false },
            title = { Text(AppText.t("me_export_backup_choose_method")) },
            text = { Text(AppText.t("me_export_backup_choose_method_body")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExportChoice = false
                        onSaveLocalBackup()
                    },
                ) {
                    Text(AppText.t("me_export_backup_save_to_local"))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExportChoice = false
                        onShareLocalBackup()
                    },
                ) {
                    Text(AppText.t("me_export_backup_share"))
                }
            },
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(AppText.t("me_clear_local_data")) },
            text = { Text(AppText.t("me_this_deletes_tiny_vow_local_records_on_this")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirm = false
                        onClearLocalData()
                        onBack()
                    },
                ) {
                    Text(AppText.t("me_clear"), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(AppText.t("group_cancel"))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PermissionSettingsPage(
    usageAccessGranted: Boolean,
    accessibilityServiceEnabled: Boolean,
    isAutoStartDismissed: Boolean,
    isIgnoringBattery: Boolean,
    notificationPermissionGranted: Boolean,
    dismissedPermissionPrompts: Set<String>,
    onBack: () -> Unit,
    onOpenUsageAccessSettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenAutoStartSettings: () -> Unit,
    onSetAutoStartDismissed: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onClearDismissedPermissionPrompts: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val statusColor = if (usageAccessGranted) themeColors.encourage else themeColors.control

    MeDetailPageScaffold(
        title = AppText.t("me_permission_settings"),
        description = AppText.t("me_check_permission_status_or_restore_permission_prompts_dismiss"),
        onBack = onBack,
    ) {
        MeSettingsCard(title = AppText.t("me_permission_settings")) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = AppText.t("me_check_permission_status_or_restore_permission_prompts_dismiss"),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (dismissedPermissionPrompts.isNotEmpty()) {
                    Button(onClick = onClearDismissedPermissionPrompts) {
                        Text(AppText.t("me_undismiss"))
                    }
                }
            }

            if (dismissedPermissionPrompts.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Text(
                        text = AppText.t("me_value_prompts_dismissed_after_you_undismiss_them_home", dismissedPermissionPrompts.size),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotificationReminderSettingsPage(
    remindersEnabled: Boolean,
    notificationPermissionGranted: Boolean,
    isProActive: Boolean,
    controlRemainingReminderMinutes: Int,
    encourageReminderTimesMinutes: List<Int>,
    onBack: () -> Unit,
    onSetEnabled: (Boolean) -> Unit,
    onSetControlRemainingMinutes: (Int) -> Unit,
    onSetEncourageTimes: (List<Int>) -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onShowProUpsell: () -> Unit,
) {
    var controlText by remember(controlRemainingReminderMinutes) {
        mutableStateOf(controlRemainingReminderMinutes.toString())
    }
    var draftEncourageTimes by remember(encourageReminderTimesMinutes) {
        mutableStateOf(encourageReminderTimesMinutes)
    }
    var editingTimeIndex by remember { mutableStateOf<Int?>(null) }
    var showAddTimeDialog by remember { mutableStateOf(false) }
    val parsedControl = controlText.toIntOrNull()

    MeDetailPageScaffold(
        title = AppText.t("notification_settings_title"),
        description = AppText.t("notification_settings_description"),
        onBack = onBack,
    ) {
        MeSettingsCard(title = AppText.t("notification_settings_general")) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = AppText.t("notification_settings_enabled_title"),
                        style = MaterialTheme.typography.bodyLarge,
                        color = LocalThemeColors.current.ink,
                    )
                    Text(
                        text = AppText.t("notification_settings_enabled_body"),
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalThemeColors.current.inkMuted,
                    )
                }
                Switch(checked = remindersEnabled, onCheckedChange = onSetEnabled)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = AppText.t("notification_settings_permission_title"),
                        style = MaterialTheme.typography.bodyLarge,
                        color = LocalThemeColors.current.ink,
                    )
                    Text(
                        text = if (notificationPermissionGranted) {
                            AppText.t("home_notifications_enabled")
                        } else {
                            AppText.t("notification_settings_permission_needed")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalThemeColors.current.inkMuted,
                    )
                }
                if (!notificationPermissionGranted) {
                    Button(onClick = onRequestNotificationPermission) {
                        Text(AppText.t("reminder_card_action"))
                    }
                }
            }
        }

        MeSettingsCard(title = AppText.t("notification_settings_schedule")) {
            OutlinedTextField(
                value = controlText,
                onValueChange = { value ->
                    if (isProActive) {
                        controlText = value.filter(Char::isDigit).take(3)
                    } else {
                        onShowProUpsell()
                    }
                },
                enabled = isProActive,
                label = { Text(AppText.t("notification_settings_control_threshold")) },
                supportingText = {
                    Text(
                        if (isProActive) {
                            AppText.t("notification_settings_control_threshold_hint")
                        } else {
                            AppText.t("notification_settings_pro_locked")
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Button(
                onClick = {
                    val minutes = parsedControl ?: return@Button
                    onSetControlRemainingMinutes(minutes)
                },
                enabled = isProActive &&
                    parsedControl != null &&
                    parsedControl in 1..120 &&
                    parsedControl != controlRemainingReminderMinutes,
            ) {
                Text(AppText.t("notification_settings_save_control_threshold"))
            }
            if (!isProActive) {
                OutlinedButton(onClick = onShowProUpsell) {
                    Text(AppText.t("pro_view_benefits"))
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ReminderTimeSelector(
                times = draftEncourageTimes,
                enabled = isProActive,
                onAdd = {
                    if (isProActive) {
                        showAddTimeDialog = true
                    } else {
                        onShowProUpsell()
                    }
                },
                onEdit = { index ->
                    if (isProActive) {
                        editingTimeIndex = index
                    } else {
                        onShowProUpsell()
                    }
                },
                onDelete = { index ->
                    if (!isProActive) {
                        onShowProUpsell()
                    } else if (draftEncourageTimes.size > 1) {
                        draftEncourageTimes = draftEncourageTimes.filterIndexed { itemIndex, _ -> itemIndex != index }
                    }
                },
                onShowProUpsell = onShowProUpsell,
            )
            Button(
                onClick = { onSetEncourageTimes(draftEncourageTimes) },
                enabled = isProActive && draftEncourageTimes.isNotEmpty() && draftEncourageTimes != encourageReminderTimesMinutes,
            ) {
                Text(AppText.t("notification_settings_save_encourage_times"))
            }
        }
    }

    val editingIndex = editingTimeIndex
    if (showAddTimeDialog) {
        ReminderTimePickerDialog(
            initialMinutes = draftEncourageTimes.lastOrNull() ?: 8 * 60,
            onDismiss = { showAddTimeDialog = false },
            onConfirm = { minutes ->
                draftEncourageTimes = (draftEncourageTimes + minutes).distinct().sorted()
                showAddTimeDialog = false
            },
        )
    }
    if (editingIndex != null) {
        ReminderTimePickerDialog(
            initialMinutes = draftEncourageTimes.getOrNull(editingIndex) ?: 8 * 60,
            onDismiss = { editingTimeIndex = null },
            onConfirm = { minutes ->
                draftEncourageTimes =
                    draftEncourageTimes
                        .mapIndexed { index, existing -> if (index == editingIndex) minutes else existing }
                        .distinct()
                        .sorted()
                editingTimeIndex = null
            },
        )
    }
}

@Composable
internal fun DayBoundarySettingsPage(
    currentHour: Int,
    isProActive: Boolean,
    onBack: () -> Unit,
    onSave: (Int) -> Unit,
    onShowProUpsell: () -> Unit,
) {
    var selectedHour by remember(currentHour) { mutableIntStateOf(currentHour) }
    val canEditBoundary = isProActive || currentHour == BusinessDay.DEFAULT_START_HOUR

    MeDetailPageScaffold(
        title = AppText.t("day_boundary_settings_title"),
        description = AppText.t("day_boundary_settings_description"),
        onBack = onBack,
    ) {
        MeSettingsCard(title = AppText.t("day_boundary_settings_section")) {
            Text(
                text = AppText.t("day_boundary_settings_default_hint"),
                style = MaterialTheme.typography.bodySmall,
                color = LocalThemeColors.current.inkMuted,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                (BusinessDay.MIN_START_HOUR..BusinessDay.MAX_START_HOUR).forEach { hour ->
                    val enabled = isProActive || (currentHour == BusinessDay.DEFAULT_START_HOUR && hour == BusinessDay.DEFAULT_START_HOUR)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                if (enabled) {
                                    selectedHour = hour
                                } else {
                                    onShowProUpsell()
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        color =
                            if (selectedHour == hour) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            RadioButton(
                                selected = selectedHour == hour,
                                onClick = {
                                    if (enabled) {
                                        selectedHour = hour
                                    } else {
                                        onShowProUpsell()
                                    }
                                },
                                enabled = enabled,
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = AppText.t("day_boundary_hour_value", hour),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = LocalThemeColors.current.ink,
                                )
                                Text(
                                    text =
                                        if (hour == BusinessDay.DEFAULT_START_HOUR) {
                                            AppText.t("day_boundary_settings_default_value_hint")
                                        } else {
                                            AppText.t("day_boundary_settings_hour_detail", hour)
                                        },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LocalThemeColors.current.inkMuted,
                                )
                            }
                            if (!enabled) {
                                ProMemberBadge(isActive = false)
                            }
                        }
                    }
                }
            }
            Button(
                onClick = {
                    if (isProActive || (currentHour == BusinessDay.DEFAULT_START_HOUR && selectedHour == BusinessDay.DEFAULT_START_HOUR)) {
                        onSave(selectedHour)
                    } else {
                        onShowProUpsell()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedHour != currentHour && (isProActive || currentHour == BusinessDay.DEFAULT_START_HOUR),
            ) {
                Text(AppText.t("day_boundary_settings_save"))
            }
        }
    }
}

@Composable
private fun ReminderTimeSelector(
    times: List<Int>,
    enabled: Boolean,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onShowProUpsell: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = AppText.t("notification_settings_encourage_times"),
            style = MaterialTheme.typography.bodyLarge,
            color = themeColors.ink,
        )
        Text(
            text = if (enabled) {
                AppText.t("notification_settings_encourage_times_hint")
            } else {
                AppText.t("notification_settings_pro_locked")
            },
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.inkMuted,
        )
        times.forEachIndexed { index, minutes ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (enabled) onEdit(index) else onShowProUpsell() },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = themeColors.base,
                    )
                    Text(
                        text = formatReminderTime(minutes),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.inkStrong,
                    )
                    IconButton(
                        onClick = { if (enabled) onDelete(index) else onShowProUpsell() },
                        enabled = enabled && times.size > 1,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = AppText.t("notification_settings_delete_time"),
                            tint = if (enabled && times.size > 1) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                        )
                    }
                }
            }
        }
        OutlinedButton(
            onClick = onAdd,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(AppText.t("notification_settings_add_encourage_time"))
        }
        if (!enabled) {
            OutlinedButton(onClick = onShowProUpsell, modifier = Modifier.fillMaxWidth()) {
                Text(AppText.t("pro_view_benefits"))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimePickerDialog(
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = (initialMinutes / 60).coerceIn(0, 23),
        initialMinute = (initialMinutes % 60).coerceIn(0, 59),
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppText.t("notification_settings_time_picker_title")) },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = state)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(state.hour * 60 + state.minute) }) {
                Text(AppText.t("group_save"))
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
fun LanguageSettingsScreen(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onBack: () -> Unit,
) {
    TinyVowDetailScaffold(
        title = AppText.t("selected_language_title"),
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
                Column(
                    modifier = Modifier.padding(
                        horizontal = TinyVowSpacing.CardHorizontal,
                        vertical = TinyVowSpacing.CardVertical,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = AppText.t("me_language_settings_description"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                    )
                }
            }

            MeSettingsCard(title = AppText.t("selected_language_title")) {
                AppLanguage.entries.forEachIndexed { index, language ->
                    if (index > 0) {
                        SettingsDivider()
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(language) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = language.displayName(),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (language == selected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                        if (language == selected) {
                            Text(
                                text = AppText.t("home_enabled"),
                                style = MaterialTheme.typography.labelSmall,
                                color = LocalThemeColors.current.encourage,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VersionInfoPage(
    versionName: String,
    onBack: () -> Unit,
) {
    MeDetailPageScaffold(
        title = AppText.t("me_app_version"),
        description = AppText.t("me_version_page_description"),
        onBack = onBack,
    ) {
        MeSettingsCard(title = AppText.t("me_app_version")) {
            MetricInfoRow(
                label = AppText.t("me_current_version"),
                value = versionName,
            )
        }
        versionReleaseNotes().forEach { release ->
            MeSettingsCard(
                title = AppText.t(
                    "me_changelog_version_title",
                    release.version,
                    release.publishedAt,
                ),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    release.notes.forEach { note ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyMedium,
                                color = LocalThemeColors.current.base,
                            )
                            Text(
                                text = note,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class VersionReleaseNotes(
    val version: String,
    val publishedAt: String,
    val notes: List<String>,
)

private fun versionReleaseNotes(): List<VersionReleaseNotes> =
    listOf(
        "1.3.1" to ("2026-07-23" to "me_changelog_131_notes"),
        "1.3.0" to ("2026-07-19" to "me_changelog_130_notes"),
        "1.2.0" to ("2026-07-13" to "me_changelog_120_notes"),
        "1.1.1" to ("2026-07-11" to "me_changelog_111_notes"),
        "1.1.0" to ("2026-07-09" to "me_changelog_110_notes"),
        "1.0.4" to ("2026-06-28" to "me_changelog_104_notes"),
        "1.0.3" to ("2026-06-27" to "me_changelog_103_notes"),
        "1.0.2" to ("2026-06-24" to "me_changelog_102_notes"),
        "1.0.1" to ("2026-05-30" to "me_changelog_101_notes"),
        "1.0.0" to ("2026-05-18" to "me_changelog_100_notes"),
    ).map { (version, details) ->
        VersionReleaseNotes(
            version = version,
            publishedAt = details.first,
            notes = AppText.t(details.second).lines().filter(String::isNotBlank),
        )
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeDetailPageScaffold(
    title: String,
    description: String?,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    TinyVowDetailScaffold(
        title = title,
        onBack = onBack,
        navigationContentDescription = AppText.t("group_back"),
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
            if (description != null) {
                TinyVowCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    borderAlpha = 0.18f,
                ) {
                    Text(
                        text = description,
                        modifier = Modifier.padding(
                            horizontal = TinyVowSpacing.CardHorizontal,
                            vertical = TinyVowSpacing.CardVertical,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun MeSettingsCard(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val themeColors = LocalThemeColors.current
    TinyVowSettingsGroup {
        Column(
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CardHorizontal,
                vertical = TinyVowSpacing.CardVertical,
            ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
        ) {
            title?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.inkStrong,
                )
            }
            content()
        }
    }
}

@Composable
private fun MetricInfoRow(label: String, value: String) {
    val themeColors = LocalThemeColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = themeColors.inkMuted,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = themeColors.inkStrong,
        )
    }
}

@Composable
private fun SubscriptionStatusPanel(
    entitlement: ProEntitlementState,
    offers: List<SubscriptionOffer>,
    isLocalActivationEnabled: Boolean,
    localUserId: String?,
    onOpenMembershipPage: () -> Unit,
    onPurchasePro: (SubscriptionOffer) -> Unit,
    onRestorePurchases: () -> Unit,
    onActivateProCode: (String) -> Unit,
) {
    val isActive = entitlement.status == ProEntitlementStatus.ACTIVE
    val isPending = entitlement.status == ProEntitlementStatus.PENDING
    val clipboard = LocalClipboardManager.current
    var showActivationDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onOpenMembershipPage)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = AppText.t("pro_membership_title"),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    ProMemberBadge(isActive = isActive)
                }
                Text(
                    text = entitlementStatusText(entitlement),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = subscriptionPriceSummary(
                        offers = offers,
                        isActive = isActive,
                        isLocalActivationEnabled = isLocalActivationEnabled,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isActive) LocalThemeColors.current.encourage else MaterialTheme.colorScheme.primary,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = AppText.t("pro_view_benefits"),
                    tint = MaterialTheme.colorScheme.outline,
                )
            }
        }

        if (isLocalActivationEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        localUserId?.let {
                            clipboard.setText(AnnotatedString(it))
                        }
                    },
                    enabled = !localUserId.isNullOrBlank(),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(AppText.t("activation_copy_user_id"))
                }
                OutlinedButton(
                    onClick = { showActivationDialog = true },
                    enabled = !localUserId.isNullOrBlank(),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(AppText.t("activation_enter_code"))
                }
            }
            if (showActivationDialog) {
                ActivationCodeDialog(
                    userId = localUserId.orEmpty(),
                    onDismiss = { showActivationDialog = false },
                    onActivate = {
                        showActivationDialog = false
                        onActivateProCode(it)
                    },
                )
            }
            return@Column
        }

        if (offers.isEmpty()) {
            Button(
                onClick = onRestorePurchases,
                enabled = !isActive && !isPending,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(AppText.t("me_loading_subscription_info"))
            }
        } else {
            offers.forEach { offer ->
                Button(
                    onClick = { onPurchasePro(offer) },
                    enabled = !isActive && !isPending,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(AppText.t("me_buy_pro_with_price", offer.price))
                }
            }
        }
    }
}

private fun subscriptionPriceSummary(
    offers: List<SubscriptionOffer>,
    isActive: Boolean,
    isLocalActivationEnabled: Boolean,
): String =
    when {
        isActive -> AppText.t("me_unlocked")
        isLocalActivationEnabled -> AppText.t("pro_price_china_summary")
        offers.size > 1 -> AppText.t("me_subscription_options_count", offers.size)
        offers.size == 1 -> offers.first().price
        else -> AppText.t("me_loading")
    }

private fun entitlementStatusText(entitlement: ProEntitlementState): String =
    when (entitlement.status) {
        ProEntitlementStatus.ACTIVE -> entitlement.expiresAtMillis?.let {
            AppText.t("activation_pro_active_until", DateFormat.getDateInstance().format(Date(it)))
        } ?: AppText.t("me_pro_active")
        ProEntitlementStatus.PENDING -> entitlement.message ?: AppText.t("me_payment_pending")
        ProEntitlementStatus.UNAVAILABLE -> entitlement.message ?: AppText.t("billing_play_billing_is_temporarily_unavailable")
        ProEntitlementStatus.FREE -> AppText.t("me_free_version")
    }

private data class ProPricePlan(
    val title: String,
    val price: String,
    val note: String,
    val offer: SubscriptionOffer? = null,
    val highlighted: Boolean = false,
)

private const val PRO_PURCHASE_EMAIL = "rrrr.zhao@qq.com"
private const val PRO_PURCHASE_WECHAT = "rourourenren222"
private const val PRO_PURCHASE_XIAOHONGSHU = "rourourenren222"
private const val PRO_COMPARE_FEATURE_WEIGHT = 0.72f
private const val PRO_COMPARE_FREE_WEIGHT = 1.02f
private const val PRO_COMPARE_PRO_WEIGHT = 1.26f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProMembershipPage(
    entitlement: ProEntitlementState,
    offers: List<SubscriptionOffer>,
    isPlayBillingEnabled: Boolean,
    isLocalActivationEnabled: Boolean,
    localUserId: String?,
    onBack: () -> Unit,
    onPurchasePro: (SubscriptionOffer) -> Unit,
    onRestorePurchases: () -> Unit,
    onManageSubscription: () -> Unit,
    onActivateProCode: (String) -> Unit,
) {
    val isActive = entitlement.status == ProEntitlementStatus.ACTIVE
    val showSubscriptionActions = isPlayBillingEnabled && !isLocalActivationEnabled
    val plans = proPricePlans(offers, isLocalActivationEnabled)
    val directChinaPaymentAvailable = isLocalActivationEnabled && plans.any { it.offer != null }
    var showPurchaseContactDialog by remember { mutableStateOf(false) }

    MeDetailPageScaffold(
        title = AppText.t("pro_membership_title"),
        description = AppText.t("pro_membership_description"),
        onBack = onBack,
    ) {
        MeSettingsCard(title = AppText.t("pro_compare_title")) {
            ProCompareHeaderRow()
            proComparisonRows().forEach { (feature, freeValue, proValue) ->
                ProCompareValueRow(
                    feature = feature,
                    freeValue = freeValue,
                    proValue = proValue,
                )
            }
        }

        MeSettingsCard(title = AppText.t("pro_membership_status")) {
            MetricInfoRow(
                label = AppText.t("me_subscription"),
                value = entitlementStatusText(entitlement),
            )
            if (isActive && showSubscriptionActions) {
                TextButton(onClick = onManageSubscription, modifier = Modifier.fillMaxWidth()) {
                    Text(AppText.t("me_manage_subscription"))
                }
            }
        }

        MeSettingsCard(title = AppText.t("pro_price_title")) {
            Text(
                text = if (isLocalActivationEnabled) {
                    AppText.t("pro_price_china_description")
                } else {
                    AppText.t("pro_price_global_description")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            plans.forEach { plan ->
                ProPricePlanCard(
                    plan = plan,
                    showPurchaseButton = directChinaPaymentAvailable,
                    onPurchase = {
                        if (isLocalActivationEnabled) {
                            showPurchaseContactDialog = true
                        } else {
                            plan.offer?.let(onPurchasePro)
                        }
                    },
                )
            }
            if (!directChinaPaymentAvailable) {
                Button(
                    onClick = { showPurchaseContactDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        when {
                            isLocalActivationEnabled && isActive -> AppText.t("pro_renew_early_bird_membership")
                            isLocalActivationEnabled -> AppText.t("pro_buy_early_bird_membership")
                            isActive -> AppText.t("pro_renew_membership")
                            else -> AppText.t("pro_buy_membership")
                        },
                    )
                }
            } else {
                TextButton(
                    onClick = { showPurchaseContactDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(AppText.t("pro_purchase_help"))
                }
            }
            if (showSubscriptionActions) {
                TextButton(onClick = onRestorePurchases, modifier = Modifier.fillMaxWidth()) {
                    Text(AppText.t("me_restore_purchases"))
                }
            }
        }
    }

    if (showPurchaseContactDialog) {
        ProPurchaseContactDialog(
            onDismiss = { showPurchaseContactDialog = false },
        )
    }
}

@Composable
private fun ProPricePlanCard(
    plan: ProPricePlan,
    showPurchaseButton: Boolean,
    onPurchase: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (plan.highlighted) {
            themeColors.baseContainer.copy(alpha = 0.58f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (plan.highlighted) themeColors.base.copy(alpha = 0.24f) else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = plan.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.inkStrong,
                )
                Text(
                    text = plan.price,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.base,
                )
            }
            Text(
                text = plan.note,
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.inkMuted,
            )
            if (showPurchaseButton) {
                Button(
                    onClick = onPurchase,
                    enabled = plan.offer != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (plan.offer == null) {
                            AppText.t("me_loading_subscription_info")
                        } else {
                            AppText.t("me_buy_pro_with_price", plan.price)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProPurchaseContactDialog(
    onDismiss: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    var copiedLabel by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppText.t("pro_purchase_contact_title")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = AppText.t("pro_purchase_contact_body"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ProPurchaseContactRow(
                    label = AppText.t("pro_purchase_email_label"),
                    value = PRO_PURCHASE_EMAIL,
                    onCopy = {
                        clipboard.setText(AnnotatedString(PRO_PURCHASE_EMAIL))
                        copiedLabel = AppText.t("pro_purchase_email_label")
                    },
                )
                ProPurchaseContactRow(
                    label = AppText.t("pro_purchase_wechat_label"),
                    value = PRO_PURCHASE_WECHAT,
                    onCopy = {
                        clipboard.setText(AnnotatedString(PRO_PURCHASE_WECHAT))
                        copiedLabel = AppText.t("pro_purchase_wechat_label")
                    },
                )
                ProPurchaseContactRow(
                    label = AppText.t("pro_purchase_xiaohongshu_label"),
                    value = PRO_PURCHASE_XIAOHONGSHU,
                    onCopy = {
                        clipboard.setText(AnnotatedString(PRO_PURCHASE_XIAOHONGSHU))
                        copiedLabel = AppText.t("pro_purchase_xiaohongshu_label")
                    },
                )
                copiedLabel?.let { label ->
                    Text(
                        text = AppText.t("pro_purchase_contact_copied", label),
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalThemeColors.current.encourage,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(AppText.t("group_close"))
            }
        },
    )
}

@Composable
private fun ProPurchaseContactRow(
    label: String,
    value: String,
    onCopy: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = AppText.t("pro_purchase_copy_contact", label),
                )
            }
        }
    }
}

private fun proPricePlans(offers: List<SubscriptionOffer>, isLocalActivationEnabled: Boolean): List<ProPricePlan> {
    if (isLocalActivationEnabled) {
        val monthlyOffer = offers.firstOrNull { it.productId == "tinyvow_pro_monthly" }
        val yearlyOffer = offers.firstOrNull { it.productId == "tinyvow_pro_yearly" }
        val lifetimeOffer = offers.firstOrNull { it.productId == "tinyvow_pro_lifetime" }
        return listOf(
            ProPricePlan(
                title = AppText.t("pro_price_monthly"),
                price = monthlyOffer?.price ?: AppText.t("pro_price_china_monthly"),
                note = AppText.t("pro_price_china_monthly_note"),
                offer = monthlyOffer,
            ),
            ProPricePlan(
                title = AppText.t("pro_price_yearly"),
                price = yearlyOffer?.price ?: AppText.t("pro_price_china_yearly"),
                note = AppText.t("pro_price_china_yearly_note"),
                offer = yearlyOffer,
                highlighted = true,
            ),
            ProPricePlan(
                title = AppText.t("pro_price_lifetime"),
                price = lifetimeOffer?.price ?: AppText.t("pro_price_china_lifetime"),
                note = AppText.t("pro_price_china_lifetime_note"),
                offer = lifetimeOffer,
            ),
        )
    }

    val yearlyOffer = offers.firstOrNull { it.billingPeriod.equals("P1Y", ignoreCase = true) }
    val monthlyOffer = offers.firstOrNull { it.billingPeriod.equals("P1M", ignoreCase = true) }
    val periodOffers = listOfNotNull(yearlyOffer, monthlyOffer)
    return if (periodOffers.isNotEmpty()) {
        listOf(
            ProPricePlan(
                title = AppText.t("pro_price_yearly"),
                price = yearlyOffer?.price ?: AppText.t("pro_price_global_yearly"),
                note = AppText.t("pro_price_global_yearly_note"),
                offer = yearlyOffer,
                highlighted = true,
            ),
            ProPricePlan(
                title = AppText.t("pro_price_monthly"),
                price = monthlyOffer?.price ?: AppText.t("pro_price_global_monthly"),
                note = AppText.t("pro_price_global_monthly_note"),
                offer = monthlyOffer,
            ),
        )
    } else if (offers.isNotEmpty()) {
        offers.mapIndexed { index, offer ->
            ProPricePlan(
                title = billingPeriodTitle(offer.billingPeriod),
                price = offer.price,
                note = AppText.t("pro_price_store_offer_note"),
                offer = offer,
                highlighted = index == 0,
            )
        }
    } else {
        listOf(
            ProPricePlan(
                title = AppText.t("pro_price_yearly"),
                price = AppText.t("pro_price_global_yearly"),
                note = AppText.t("pro_price_global_yearly_note"),
                highlighted = true,
            ),
            ProPricePlan(
                title = AppText.t("pro_price_monthly"),
                price = AppText.t("pro_price_global_monthly"),
                note = AppText.t("pro_price_global_monthly_note"),
            ),
        )
    }
}

private fun billingPeriodTitle(billingPeriod: String): String =
    when (billingPeriod.uppercase()) {
        "P1Y" -> AppText.t("pro_price_yearly")
        "P1M" -> AppText.t("pro_price_monthly")
        else -> AppText.t("me_subscription")
    }

private fun proComparisonRows(): List<Triple<String, String, String>> {
    val freeLimits = ProFeatureGate.limits(false)
    val proLimits = ProFeatureGate.limits(true)
    return listOf(
        Triple(
            AppText.t("pro_compare_control_groups"),
            freeLimits.controlGroupLimit.toString(),
            AppText.t("pro_compare_unlimited"),
        ),
        Triple(
            AppText.t("pro_compare_encourage_groups"),
            freeLimits.encourageGroupLimit.toString(),
            AppText.t("pro_compare_unlimited"),
        ),
        Triple(
            AppText.t("pro_compare_apps_per_group"),
            freeLimits.appsPerGroupLimit.toString(),
            proLimits.appsPerGroupLimit.toString(),
        ),
        Triple(
            AppText.t("pro_compare_custom_rewards"),
            freeLimits.customRewardLimit.toString(),
            AppText.t("pro_compare_unlimited"),
        ),
        Triple(
            AppText.t("pro_compare_custom_themes"),
            freeLimits.customThemeLimit.toString(),
            proLimits.customThemeLimit.toString(),
        ),
        Triple(
            AppText.t("pro_compare_advanced_reports"),
            AppText.t("pro_compare_basic_reports"),
            AppText.t("pro_compare_full_reports"),
        ),
        Triple(
            AppText.t("pro_compare_super_mode_window"),
            AppText.t("pro_compare_fixed_window"),
            AppText.t("pro_compare_custom_window"),
        ),
    )
}

@Composable
private fun ProBenefitsComparisonDialog(
    entitlement: ProEntitlementState,
    offers: List<SubscriptionOffer>,
    showSubscriptionActions: Boolean,
    isLocalActivationEnabled: Boolean,
    localUserId: String?,
    onPurchasePro: (SubscriptionOffer) -> Unit,
    onRestorePurchases: () -> Unit,
    onManageSubscription: () -> Unit,
    onActivateProCode: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val defaultOffer = offers.firstOrNull()
    val purchaseButtonEnabled =
        if (isLocalActivationEnabled) {
            !localUserId.isNullOrBlank()
        } else {
            entitlement.status == ProEntitlementStatus.FREE && defaultOffer != null
        }
    val purchaseButtonLabel =
        when {
            isLocalActivationEnabled -> AppText.t("pro_activate_membership")
            entitlement.status == ProEntitlementStatus.ACTIVE -> AppText.t("me_unlocked")
            entitlement.status == ProEntitlementStatus.PENDING -> AppText.t("me_payment_pending")
            defaultOffer != null -> AppText.t("me_buy_pro_with_price", defaultOffer.price)
            else -> AppText.t("me_loading_subscription_info")
        }
    var showActivationDialog by remember { mutableStateOf(false) }
    val rows = proComparisonRows()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppText.t("pro_compare_title")) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ProCompareHeaderRow()
                rows.forEach { (feature, freeValue, proValue) ->
                    ProCompareValueRow(
                        feature = feature,
                        freeValue = freeValue,
                        proValue = proValue,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        if (isLocalActivationEnabled) {
                            showActivationDialog = true
                        } else {
                            defaultOffer?.let(onPurchasePro)
                        }
                    },
                    enabled = purchaseButtonEnabled,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(purchaseButtonLabel)
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showSubscriptionActions) {
                    TextButton(onClick = onRestorePurchases) {
                        Text(AppText.t("me_restore_purchases"))
                    }
                    TextButton(onClick = onManageSubscription) {
                        Text(AppText.t("me_manage_subscription"))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(AppText.t("group_close"))
                }
            }
        },
    )

    if (showActivationDialog) {
        ActivationCodeDialog(
            userId = localUserId.orEmpty(),
            onDismiss = { showActivationDialog = false },
            onActivate = {
                showActivationDialog = false
                onActivateProCode(it)
            },
        )
    }
}

@Composable
private fun ProCompareHeaderRow() {
    val themeColors = LocalThemeColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = themeColors.baseContainer.copy(alpha = 0.22f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = AppText.t("pro_compare_feature"),
                modifier = Modifier.weight(PRO_COMPARE_FEATURE_WEIGHT),
                style = MaterialTheme.typography.labelSmall,
                color = themeColors.inkMuted,
            )
            Text(
                text = AppText.t("pro_compare_free"),
                modifier = Modifier.weight(PRO_COMPARE_FREE_WEIGHT),
                style = MaterialTheme.typography.labelSmall,
                color = themeColors.inkMuted,
            )
            Text(
                text = AppText.t("pro_compare_pro"),
                modifier = Modifier.weight(PRO_COMPARE_PRO_WEIGHT),
                style = MaterialTheme.typography.labelSmall,
                color = themeColors.base,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ProCompareValueRow(
    feature: String,
    freeValue: String,
    proValue: String,
) {
    val themeColors = LocalThemeColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = themeColors.baseContainer.copy(alpha = 0.34f),
        border = BorderStroke(1.dp, themeColors.base.copy(alpha = 0.08f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = feature,
                modifier = Modifier.weight(PRO_COMPARE_FEATURE_WEIGHT),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.inkStrong,
            )
            Text(
                text = freeValue,
                modifier = Modifier.weight(PRO_COMPARE_FREE_WEIGHT),
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.inkMuted,
            )
            Text(
                text = proValue,
                modifier = Modifier.weight(PRO_COMPARE_PRO_WEIGHT),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.base,
            )
        }
    }
}

@Composable
private fun ActivationCodeDialog(
    userId: String,
    onDismiss: () -> Unit,
    onActivate: (String) -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val clipboard = LocalClipboardManager.current
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppText.t("activation_title")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = AppText.t("activation_send_user_id_to_developer"),
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.inkMuted,
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = themeColors.baseContainer.copy(alpha = 0.54f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.base.copy(alpha = 0.10f)),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = userId,
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.ink,
                        )
                        TextButton(
                            onClick = { clipboard.setText(AnnotatedString(userId)) },
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = AppText.t("activation_copy_user_id"), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(AppText.t("activation_copy_user_id"))
                        }
                    }
                }
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.trim() },
                    label = { Text(AppText.t("activation_code_label")) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onActivate(code) },
                enabled = code.isNotBlank(),
            ) {
                Text(AppText.t("activation_activate"))
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
internal fun CheckInOverviewPage(
    state: DailyCheckInMonthState,
    onBack: () -> Unit,
    onMonthChange: (YearMonth) -> Unit,
) {
    val themeColors = LocalThemeColors.current
    MeDetailPageScaffold(
        title = AppText.t("checkin_overview_title"),
        description = null,
        onBack = onBack,
    ) {
        TinyVowCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
            shadowElevation = TinyVowElevation.FeaturedCard,
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            listOf(
                                themeColors.base.copy(alpha = 0.16f),
                                MaterialTheme.colorScheme.surface,
                                themeColors.control.copy(alpha = 0.10f),
                            ),
                        ),
                    )
                    .padding(
                        horizontal = TinyVowSpacing.CardHorizontal,
                        vertical = TinyVowSpacing.CardVertical,
                    ),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                CheckInCalendarHeader(month = state.month)
                CheckInYearSelector(month = state.month, onMonthChange = onMonthChange)
                CheckInMonthSelector(month = state.month, onMonthChange = onMonthChange)
                CheckInLegend()
                CheckInCalendar(state = state)
            }
        }
    }
}

@Composable
private fun CheckInCalendarHeader(
    month: YearMonth,
) {
    val themeColors = LocalThemeColors.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = AppText.t("checkin_calendar_heading"),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = themeColors.base,
        )
        Text(
            text = AppText.t("checkin_month_title", month.monthValue, month.year),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = themeColors.inkStrong,
        )
    }
}

@Composable
private fun CheckInYearSelector(
    month: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
) {
    val years = remember(month.year) { ((month.year - 6)..(month.year + 6)).toList() }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 4)

    LaunchedEffect(month.year) {
        listState.animateScrollToItem(4)
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(years) { year ->
            CheckInSelectorChip(
                label = AppText.t("checkin_year_label", year),
                selected = year == month.year,
                width = 76.dp,
                onClick = { onMonthChange(YearMonth.of(year, month.monthValue)) },
            )
        }
    }
}

@Composable
private fun CheckInMonthSelector(
    month: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (month.monthValue - 2).coerceAtLeast(0))

    LaunchedEffect(month.monthValue) {
        listState.animateScrollToItem((month.monthValue - 2).coerceIn(0, 11))
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items((1..12).toList()) { monthNumber ->
            CheckInSelectorChip(
                label = AppText.t("checkin_month_chip", monthNumber),
                selected = month.monthValue == monthNumber,
                width = 58.dp,
                onClick = { onMonthChange(YearMonth.of(month.year, monthNumber)) },
            )
        }
    }
}

@Composable
private fun CheckInSelectorChip(
    label: String,
    selected: Boolean,
    width: Dp,
    onClick: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    Surface(
        modifier = Modifier
            .width(width)
            .height(36.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) themeColors.base.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.62f),
        border = BorderStroke(
            1.dp,
            if (selected) themeColors.base.copy(alpha = 0.48f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f),
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) themeColors.base else themeColors.inkMuted,
            )
        }
    }
}

@Composable
private fun CheckInLegend() {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item {
            CheckInLegendItem(label = AppText.t("checkin_badge_signed")) {
                CheckInSignedSwatch()
            }
        }
        item {
            CheckInLegendItem(label = AppText.t("checkin_badge_control")) {
                CheckInStatusRing(
                    controlProgress = 1f,
                    encourageProgress = 0f,
                    growthProgress = 0f,
                    controlAvailable = true,
                    encourageAvailable = false,
                    growthAvailable = false,
                    markSize = 24.dp,
                )
            }
        }
        item {
            CheckInLegendItem(label = AppText.t("checkin_badge_encourage")) {
                CheckInStatusRing(
                    controlProgress = 0f,
                    encourageProgress = 1f,
                    growthProgress = 0f,
                    controlAvailable = false,
                    encourageAvailable = true,
                    growthAvailable = false,
                    markSize = 24.dp,
                )
            }
        }
        item {
            CheckInLegendItem(label = AppText.t("checkin_badge_growth")) {
                CheckInStatusRing(
                    controlProgress = 0f,
                    encourageProgress = 0f,
                    growthProgress = 1f,
                    controlAvailable = false,
                    encourageAvailable = false,
                    growthAvailable = true,
                    markSize = 24.dp,
                )
            }
        }
    }
}

@Composable
private fun CheckInLegendItem(
    label: String,
    mark: @Composable () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        mark()
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = LocalThemeColors.current.inkMuted,
        )
    }
}

@Composable
private fun CheckInCalendar(state: DailyCheckInMonthState) {
    var selectedDay by remember { mutableStateOf<DailyCheckInDayState?>(null) }
    val firstOffset = state.month.atDay(1).dayOfWeek.value - 1
    val cells: List<DailyCheckInDayState?> = List(firstOffset) { null } + state.days
    val rows = cells.chunked(7)
    selectedDay?.let { day ->
        CheckInRingDetailDialog(
            day = day,
            onDismiss = { selectedDay = null },
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            checkInWeekdayLabels().forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalThemeColors.current.inkMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                (0 until 7).forEach { index ->
                    CheckInDayCell(
                        day = row.getOrNull(index),
                        modifier = Modifier.weight(1f),
                        onClick = { selectedDay = it },
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckInDayCell(
    day: DailyCheckInDayState?,
    modifier: Modifier = Modifier,
    onClick: (DailyCheckInDayState) -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val checkedIn = day?.checkedIn == true
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(enabled = day != null) {
                day?.let(onClick)
            },
        shape = RoundedCornerShape(18.dp),
        color =
            if (checkedIn) {
                lerp(MaterialTheme.colorScheme.surface, themeColors.base, 0.14f).copy(alpha = 0.96f)
            } else if (day?.isToday == true) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f)
            },
        border =
            if (day?.isToday == true) {
                BorderStroke(1.dp, themeColors.base.copy(alpha = 0.62f))
            } else if (checkedIn) {
                BorderStroke(1.dp, themeColors.base.copy(alpha = 0.18f))
            } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.16f))
            },
    ) {
        if (day != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp),
                contentAlignment = Alignment.Center,
            ) {
                CheckInStatusRing(
                    controlProgress = day.activityControlProgress,
                    encourageProgress = day.activityEncourageProgress,
                    growthProgress = day.activityGrowthProgress,
                    controlAvailable = day.activityControlAvailable,
                    encourageAvailable = day.activityEncourageAvailable,
                    growthAvailable = day.activityGrowthAvailable,
                    markSize = 34.dp,
                )
                Text(
                    text = day.date.dayOfMonth.toString(),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color =
                        if (day.hasArchivedSignals || checkedIn || day.isToday) {
                            themeColors.inkStrong.copy(alpha = 0.20f)
                        } else {
                            themeColors.inkMuted.copy(alpha = 0.13f)
                        },
                    minLines = 1,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun CheckInRingDetailDialog(
    day: DailyCheckInDayState,
    onDismiss: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = AppText.t("checkin_day_detail_title", day.date.monthValue, day.date.dayOfMonth),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CheckInStatusRing(
                        controlProgress = day.activityControlProgress,
                        encourageProgress = day.activityEncourageProgress,
                        growthProgress = day.activityGrowthProgress,
                        controlAvailable = day.activityControlAvailable,
                        encourageAvailable = day.activityEncourageAvailable,
                        growthAvailable = day.activityGrowthAvailable,
                        markSize = 72.dp,
                    )
                }
                if (!day.hasArchivedSignals) {
                    Text(
                        text = AppText.t("checkin_day_detail_no_archive"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeColors.inkMuted,
                    )
                } else {
                    CheckInRingDetailRow(
                        label = AppText.t("checkin_badge_control"),
                        progress = day.activityControlProgress,
                        available = day.activityControlAvailable,
                        color = themeColors.control,
                    )
                    CheckInRingDetailRow(
                        label = AppText.t("checkin_badge_encourage"),
                        progress = day.activityEncourageProgress,
                        available = day.activityEncourageAvailable,
                        color = themeColors.encourage,
                    )
                    CheckInRingDetailRow(
                        label = AppText.t("checkin_badge_growth"),
                        progress = day.activityGrowthProgress,
                        available = day.activityGrowthAvailable,
                        color = themeColors.base,
                    )
                    if (day.activityRingsCompleted) {
                        Text(
                            text = AppText.t("checkin_day_detail_all_closed"),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = themeColors.base,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(AppText.t("checkin_day_detail_close"))
            }
        },
    )
}

@Composable
private fun CheckInRingDetailRow(
    label: String,
    progress: Float,
    available: Boolean,
    color: Color,
) {
    val themeColors = LocalThemeColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(8.dp),
            shape = CircleShape,
            color = color,
        ) {}
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = themeColors.ink,
        )
        Text(
            text = formatCheckInRingProgress(progress, available),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (available) themeColors.inkStrong else themeColors.inkFaint,
        )
    }
}

private fun formatCheckInRingProgress(progress: Float, available: Boolean): String {
    if (!available) return AppText.t("home_activity_ring_empty_value")
    return NumberFormat.getPercentInstance()
        .apply { maximumFractionDigits = 0 }
        .format(progress.coerceAtLeast(0f).toDouble())
}

@Composable
private fun CheckInSignedSwatch() {
    val themeColors = LocalThemeColors.current
    Surface(
        modifier = Modifier.size(18.dp),
        shape = RoundedCornerShape(6.dp),
        color = lerp(MaterialTheme.colorScheme.surface, themeColors.base, 0.18f).copy(alpha = 0.96f),
        border = BorderStroke(1.dp, themeColors.base.copy(alpha = 0.28f)),
    ) {}
}

@Composable
private fun CheckInStatusRing(
    controlProgress: Float,
    encourageProgress: Float,
    growthProgress: Float,
    controlAvailable: Boolean,
    encourageAvailable: Boolean,
    growthAvailable: Boolean,
    markSize: Dp = 30.dp,
) {
    val themeColors = LocalThemeColors.current
    Canvas(
        modifier = Modifier.size(markSize),
    ) {
        val shortestSide = min(size.width, size.height)
        val strokeWidth = (shortestSide * 0.12f).coerceAtLeast(1.4.dp.toPx())
        val ringGap = 1.dp.toPx()
        val outerInset = strokeWidth / 2f + 1.dp.toPx()
        val maxInset = outerInset + (strokeWidth + ringGap) * 2f
        if (shortestSide <= maxInset * 2f) return@Canvas

        val center = Offset(size.width / 2f, size.height / 2f)
        val style = Stroke(width = strokeWidth, cap = StrokeCap.Round)

        fun drawRing(
            progress: Float,
            color: Color,
            available: Boolean,
            inset: Float,
        ) {
            if (!available) return
            val diameter = shortestSide - inset * 2f
            if (diameter <= 0f) return
            val topLeft = Offset(center.x - diameter / 2f, center.y - diameter / 2f)
            val arcSize = Size(diameter, diameter)
            drawArc(
                color = color.copy(alpha = 0.18f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = style,
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = progress.coerceIn(0f, 1f) * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = style,
            )
        }

        drawRing(controlProgress, themeColors.control, controlAvailable, outerInset)
        drawRing(encourageProgress, themeColors.encourage, encourageAvailable, outerInset + strokeWidth + ringGap)
        drawRing(growthProgress, themeColors.base, growthAvailable, outerInset + (strokeWidth + ringGap) * 2f)
    }
}

private fun checkInWeekdayLabels(): List<String> =
    listOf(
        AppText.t("checkin_weekday_mon"),
        AppText.t("checkin_weekday_tue"),
        AppText.t("checkin_weekday_wed"),
        AppText.t("checkin_weekday_thu"),
        AppText.t("checkin_weekday_fri"),
        AppText.t("checkin_weekday_sat"),
        AppText.t("checkin_weekday_sun"),
    )

@Composable
fun MeStatItem(
    value: String,
    label: String,
    color: Color,
    onClick: (() -> Unit)? = null,
) {
    val themeColors = LocalThemeColors.current
    Column(
        modifier =
            if (onClick != null) {
                Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onClick)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            } else {
                Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = color)
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = themeColors.inkMuted)
            if (onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = themeColors.inkMuted,
                )
            }
        }
    }
}

private fun formatMetricNumber(value: Long): String = NumberFormat.getIntegerInstance().format(value)

private fun userFacingVersionName(versionName: String): String =
    versionName.removeSuffix("-cn")

private fun calculateInstalledDays(context: android.content.Context): Long {
    val firstInstallTime =
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
        }.getOrElse { return 1L }
    val installedDate = Instant.ofEpochMilli(firstInstallTime).atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now(ZoneId.systemDefault())
    return ChronoUnit.DAYS.between(installedDate, today).plus(1L).coerceAtLeast(1L)
}

private fun formatReminderTime(minutes: Int): String =
    "%02d:%02d".format(minutes / 60, minutes % 60)

@Composable
fun MeMenuSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TinyVowSectionHeader(title = title)
        TinyVowSettingsGroup {
            Column(content = content)
        }
    }
}

@Composable
private fun MeInfoItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val themeColors = LocalThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = color)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = themeColors.ink)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.inkMuted,
            )
        }
    }
}

@Composable
fun MeMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface,
    trailingText: String? = null,
    titleTrailingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val themeColors = LocalThemeColors.current
    val resolvedIconColor = if (color == MaterialTheme.colorScheme.onSurface) themeColors.base else color
    TinyVowSettingsItem(
        icon = icon,
        title = title,
        iconContainerColor = resolvedIconColor.copy(alpha = 0.12f),
        iconContentColor = resolvedIconColor,
        onClick = onClick,
        titleTrailing = titleTrailingContent?.let { content ->
            { content() }
        },
        trailing = {
            trailingText?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = themeColors.inkFaint,
                    maxLines = 1,
                )
                Spacer(Modifier.width(8.dp))
            }
            trailingContent?.let {
                it()
                Spacer(Modifier.width(8.dp))
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.outlineVariant,
            )
        },
    )
}
