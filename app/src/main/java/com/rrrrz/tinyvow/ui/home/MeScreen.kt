package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.i18n.AppText

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.data.auth.UserSession
import com.rrrrz.tinyvow.data.billing.ProEntitlementState
import com.rrrrz.tinyvow.data.billing.ProEntitlementStatus
import com.rrrrz.tinyvow.data.billing.SubscriptionOffer
import com.rrrrz.tinyvow.data.pro.ProFeatureGate
import com.rrrrz.tinyvow.data.supermode.SuperModeStatus
import com.rrrrz.tinyvow.i18n.AppLanguage
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.ThemeSeed
import com.rrrrz.tinyvow.ui.theme.TinyVowButton
import com.rrrrz.tinyvow.ui.theme.TinyVowButtonTone
import com.rrrrz.tinyvow.ui.theme.TinyVowCard
import com.rrrrz.tinyvow.ui.theme.TinyVowElevation
import com.rrrrz.tinyvow.ui.theme.TinyVowRadius
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.roundToLong

@Composable
fun MeScreen(
    userSession: UserSession?,
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
    openBenefitsDialog: Boolean,
    onBenefitsDialogOpened: () -> Unit,
    usageAccessGranted: Boolean,
    accessibilityServiceEnabled: Boolean,
    isAutoStartDismissed: Boolean,
    isIgnoringBattery: Boolean,
    notificationPermissionGranted: Boolean,
    dismissedPermissionPrompts: Set<String>,
    onUpdateProfileName: (String?) -> Unit,
    onUpdateProfileAvatar: (String) -> Unit,
    onClearProfileAvatar: () -> Unit,
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
    onNavigateToLaboratory: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToThemeSettings: () -> Unit,
    onNavigateToLanguageSettings: () -> Unit,
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
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
        onUpdateProfileAvatar(uri.toString())
    }

    var showDeleteAccountConfirm by remember { mutableStateOf(false) }
    var showProfileEditor by remember { mutableStateOf(false) }

    val effectiveAvatar = profileAvatarUri ?: userSession?.avatarUrl
    val canTapToSignIn = isGoogleSignInEnabled && userSession == null && isGoogleSignInConfigured
    val isProMember = isProActive
    val appUsageDays = remember(context) { calculateInstalledDays(context) }
    val hasCustomDisplayName = !profileDisplayName.isNullOrBlank()
    val displayName =
        profileDisplayName
            ?: userSession?.displayName
            ?: if (isLocalActivationEnabled) {
                AppText.t("me_local_account_default_name")
            } else {
                AppText.t("me_not_signed_in")
            }
    val subtitle: String? =
        when {
            !userSession?.email.isNullOrBlank() -> userSession?.email.orEmpty()
            isLocalActivationEnabled && !hasCustomDisplayName -> AppText.t("me_local_account_subtitle")
            isLocalActivationEnabled -> null
            isGoogleSignInEnabled -> null
            else -> AppText.t("me_china_local_mode_subtitle")
        }
    val displayAppVersion = userFacingVersionName(appVersionName)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            themeColors.base,
                            lerp(themeColors.base, themeColors.encourage, 0.22f),
                            themeColors.base.copy(alpha = 0.88f),
                        )
                    )
                )
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
                        .clickable(enabled = canTapToSignIn, onClick = onSignInWithGoogle)
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
                            if (isProMember) {
                                ProMemberBadge()
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
                    IconButton(onClick = { showProfileEditor = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = AppText.t("me_edit_profile"),
                            tint = themeColors.onBase,
                        )
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
                    isPlayBillingEnabled = isPlayBillingEnabled,
                    isLocalActivationEnabled = isLocalActivationEnabled,
                    localUserId = userSession?.userId,
                    openBenefitsDialog = openBenefitsDialog,
                    onBenefitsDialogOpened = onBenefitsDialogOpened,
                    onPurchasePro = onPurchasePro,
                    onRestorePurchases = onRestorePurchases,
                    onManageSubscription = onManageSubscription,
                    onActivateProCode = onActivateProCode,
                )
            }

            TinyVowCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TinyVowRadius.Card),
                shadowElevation = TinyVowElevation.Card,
            ) {
                Column {
                    MeMenuItem(
                        icon = Icons.Default.Settings,
                        title = AppText.t("me_permission_settings"),
                        onClick = onNavigateToPermissionSettings,
                    )
                    SettingsDivider()
                    MeMenuItem(
                        icon = Icons.Default.Person,
                        title = AppText.t("selected_language_title"),
                        trailingText = selectedAppLanguage.displayName(),
                        onClick = onNavigateToLanguageSettings,
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
                        icon = Icons.Default.Settings,
                        title = AppText.t("special_app_settings_title"),
                        onClick = onNavigateToSpecialAppSettings,
                    )
                    SettingsDivider()
                    MeMenuItem(
                        icon = Icons.Default.Palette,
                        title = AppText.t("me_theme_management"),
                        onClick = onNavigateToThemeSettings,
                    )
                    SettingsDivider()
                    MeMenuItem(
                        icon = Icons.Default.History,
                        title = AppText.t("me_usage_history"),
                        onClick = onNavigateToHistory,
                    )
                    SettingsDivider()
                    MeMenuItem(
                        icon = Icons.Default.Settings,
                        title = AppText.t("me_local_data_management"),
                        onClick = onNavigateToDataPrivacy,
                    )
                    SettingsDivider()
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
                    if (isDebugBuild) {
                        SettingsDivider()
                        MeMenuItem(
                            icon = Icons.Default.Science,
                            title = AppText.t("me_advanced_center"),
                            onClick = onNavigateToLaboratory,
                            color = themeColors.base,
                        )
                    }
                    SettingsDivider()
                    MeMenuItem(
                        icon = Icons.Default.Info,
                        title = AppText.t("me_app_version"),
                        trailingText = displayAppVersion,
                        onClick = onNavigateToVersionInfo,
                    )
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

    if (showProfileEditor) {
        ProfileEditorDialog(
            initialDisplayName = profileDisplayName ?: userSession?.displayName.orEmpty(),
            avatar = effectiveAvatar,
            onPickAvatar = { avatarPickerLauncher.launch(arrayOf("image/*")) },
            onClearAvatar = onClearProfileAvatar,
            onSave = {
                onUpdateProfileName(it)
                showProfileEditor = false
            },
            onDismiss = { showProfileEditor = false },
        )
    }
}

private fun AppLanguage.displayName(): String =
    when (this) {
        AppLanguage.SYSTEM -> AppText.t("selected_language_system")
        AppLanguage.ZH_CN -> AppText.t("selected_language_zh_cn")
        AppLanguage.EN -> AppText.t("selected_language_en")
    }

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
private fun ProfileAvatar(
    avatar: String?,
    contentDescription: String,
    size: androidx.compose.ui.unit.Dp,
) {
    Surface(
        modifier = Modifier.size(size),
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
private fun ProMemberBadge() {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = ProBadgeGold,
    ) {
        Box(modifier = Modifier.padding(2.dp)) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ProBadgeBackground,
            ) {
                Text(
                    text = AppText.t("me_pro_badge"),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = ProBadgeGold,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

private val ProBadgeBackground = Color(0xFF141414)
private val ProBadgeGold = Color(0xFFE0B84F)

@Composable
private fun ProfileEditorDialog(
    initialDisplayName: String,
    avatar: String?,
    onPickAvatar: () -> Unit,
    onClearAvatar: () -> Unit,
    onSave: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var displayName by remember(initialDisplayName) { mutableStateOf(initialDisplayName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppText.t("me_edit_profile"), style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ProfileAvatar(
                        avatar = avatar,
                        contentDescription = AppText.t("me_profile_avatar"),
                        size = 72.dp,
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = onPickAvatar,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(AppText.t("me_profile_choose_avatar"))
                        }
                        if (!avatar.isNullOrBlank()) {
                            TextButton(
                                onClick = onClearAvatar,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    AppText.t("me_profile_remove_avatar"),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(AppText.t("me_profile_name")) },
                    placeholder = { Text(AppText.t("me_profile_name_placeholder")) },
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(displayName) }) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DataPrivacyPage(
    onBack: () -> Unit,
    onExportLocalData: () -> Unit,
    onClearLocalData: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
) {
    var showClearConfirm by remember { mutableStateOf(false) }

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
            Button(onClick = onExportLocalData, modifier = Modifier.fillMaxWidth()) {
                Text(AppText.t("home_export_local_data"))
            }
            TextButton(onClick = onOpenPrivacyPolicy, modifier = Modifier.fillMaxWidth()) {
                Text(AppText.t("me_view_privacy_policy"))
            }
            TextButton(onClick = { showClearConfirm = true }, modifier = Modifier.fillMaxWidth()) {
                Text(AppText.t("me_clear_local_data"), color = MaterialTheme.colorScheme.error)
            }
        }
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
fun LanguageSettingsScreen(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(AppText.t("selected_language_title")) },
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
        MeSettingsCard(title = AppText.t("me_changelog")) {
            Text(
                text = AppText.t("me_changelog_empty"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeDetailPageScaffold(
    title: String,
    description: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = LocalThemeColors.current.inkStrong,
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
                    text = description,
                    modifier = Modifier.padding(
                        horizontal = TinyVowSpacing.CardHorizontal,
                        vertical = TinyVowSpacing.CardVertical,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                )
            }
            content()
        }
    }
}

@Composable
private fun MeSettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val themeColors = LocalThemeColors.current
    TinyVowCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(TinyVowRadius.Card),
        color = MaterialTheme.colorScheme.surface,
        borderAlpha = 0.26f,
        shadowElevation = TinyVowElevation.Card,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = TinyVowSpacing.CardHorizontal,
                vertical = TinyVowSpacing.CardVertical,
            ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.inkStrong,
            )
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
    isPlayBillingEnabled: Boolean,
    isLocalActivationEnabled: Boolean,
    localUserId: String?,
    openBenefitsDialog: Boolean,
    onBenefitsDialogOpened: () -> Unit,
    onPurchasePro: (SubscriptionOffer) -> Unit,
    onRestorePurchases: () -> Unit,
    onManageSubscription: () -> Unit,
    onActivateProCode: (String) -> Unit,
) {
    val isActive = entitlement.status == ProEntitlementStatus.ACTIVE
    val isPending = entitlement.status == ProEntitlementStatus.PENDING
    var showActivationDialog by remember { mutableStateOf(false) }
    var showBenefitsDialog by remember { mutableStateOf(false) }
    LaunchedEffect(openBenefitsDialog) {
        if (openBenefitsDialog) {
            showBenefitsDialog = true
            onBenefitsDialogOpened()
        }
    }

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
                .clickable(onClick = { showBenefitsDialog = true })
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Tiny Vow Pro",
                    style = MaterialTheme.typography.titleMedium,
                )
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
                    text = subscriptionPriceSummary(offers, isActive),
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
            OutlinedButton(
                onClick = { showActivationDialog = true },
                enabled = !localUserId.isNullOrBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(AppText.t("activation_enter_code"))
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

    if (showBenefitsDialog) {
        ProBenefitsComparisonDialog(
            entitlement = entitlement,
            offers = offers,
            showSubscriptionActions = isPlayBillingEnabled && !isLocalActivationEnabled,
            isLocalActivationEnabled = isLocalActivationEnabled,
            localUserId = localUserId,
            onPurchasePro = onPurchasePro,
            onRestorePurchases = onRestorePurchases,
            onManageSubscription = onManageSubscription,
            onActivateProCode = onActivateProCode,
            onDismiss = { showBenefitsDialog = false },
        )
    }
}

private fun subscriptionPriceSummary(offers: List<SubscriptionOffer>, isActive: Boolean): String =
    when {
        isActive -> AppText.t("me_unlocked")
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
    val freeLimits = ProFeatureGate.limits(false)
    val proLimits = ProFeatureGate.limits(true)
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
    val rows = listOf(
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
            AppText.t("pro_compare_member_themes"),
            AppText.t("pro_compare_not_included"),
            AppText.t("pro_compare_included"),
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = AppText.t("pro_compare_feature"),
            modifier = Modifier.weight(1.4f),
            style = MaterialTheme.typography.labelMedium,
            color = themeColors.inkMuted,
        )
        Text(
            text = AppText.t("pro_compare_free"),
            modifier = Modifier.weight(0.8f),
            style = MaterialTheme.typography.labelMedium,
            color = themeColors.inkMuted,
        )
        Text(
            text = AppText.t("pro_compare_pro"),
            modifier = Modifier.weight(0.8f),
            style = MaterialTheme.typography.labelMedium,
            color = themeColors.base,
        )
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
        shape = RoundedCornerShape(14.dp),
        color = themeColors.baseContainer.copy(alpha = 0.44f),
        border = androidx.compose.foundation.BorderStroke(1.dp, themeColors.base.copy(alpha = 0.08f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = feature,
                modifier = Modifier.weight(1.4f),
                style = MaterialTheme.typography.bodyMedium,
                color = themeColors.ink,
            )
            Text(
                text = freeValue,
                modifier = Modifier.weight(0.8f),
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.inkMuted,
            )
            Text(
                text = proValue,
                modifier = Modifier.weight(0.8f),
                style = MaterialTheme.typography.bodySmall,
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
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
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
fun MeStatItem(value: String, label: String, color: Color) {
    val themeColors = LocalThemeColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = themeColors.inkMuted)
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

@Composable
fun MeMenuSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val themeColors = LocalThemeColors.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = themeColors.inkFaint,
        )
        TinyVowCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(TinyVowRadius.Card),
        ) {
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
) {
    val themeColors = LocalThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.70f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (color == MaterialTheme.colorScheme.onSurface) themeColors.base else color,
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = themeColors.ink, modifier = Modifier.weight(1f))
        trailingText?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = themeColors.inkFaint,
                maxLines = 1,
            )
            Spacer(Modifier.width(8.dp))
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

