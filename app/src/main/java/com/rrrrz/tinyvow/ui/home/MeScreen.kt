package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.i18n.AppText

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.data.auth.UserSession
import com.rrrrz.tinyvow.data.billing.ProEntitlementState
import com.rrrrz.tinyvow.data.billing.ProEntitlementStatus
import com.rrrrz.tinyvow.data.billing.SubscriptionOffer
import com.rrrrz.tinyvow.i18n.AppLanguage
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.ThemePresets
import com.rrrrz.tinyvow.ui.theme.ThemeSeed
import com.rrrrz.tinyvow.ui.theme.argbToHex
import com.rrrrz.tinyvow.ui.theme.createCustomTheme
import java.text.DateFormat
import java.util.Date

@Composable
fun MeScreen(
    userSession: UserSession?,
    isGoogleSignInEnabled: Boolean,
    isGoogleSignInConfigured: Boolean,
    isPlayBillingEnabled: Boolean,
    isLocalActivationEnabled: Boolean,
    proEntitlement: ProEntitlementState,
    subscriptionOffers: List<SubscriptionOffer>,
    userPoints: Double,
    selectedThemeId: String,
    customThemes: List<ThemeSeed>,
    isProActive: Boolean,
    selectedAppLanguage: AppLanguage,
    usageAccessGranted: Boolean,
    accessibilityServiceEnabled: Boolean,
    isAutoStartDismissed: Boolean,
    isIgnoringBattery: Boolean,
    notificationPermissionGranted: Boolean,
    dismissedPermissionPrompts: Set<String>,
    onSelectAppLanguage: (AppLanguage) -> Unit,
    onSelectTheme: (String) -> Unit,
    onSaveCustomTheme: (ThemeSeed) -> Unit,
    onDeleteCustomTheme: (String) -> Unit,
    onShowProUpsell: (ProUpsellSource) -> Unit,
    onOpenUsageAccessSettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenAutoStartSettings: () -> Unit,
    onSetAutoStartDismissed: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onClearDismissedPermissionPrompts: () -> Unit,
    onNavigateToLaboratory: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToThemeSettings: () -> Unit,
    onNavigateToHelpFeedback: () -> Unit,
    onNavigateToContactUs: () -> Unit,
    onExportLocalData: () -> Unit,
    onClearLocalData: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onSignInWithGoogle: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: (clearLocalData: Boolean) -> Unit,
    onPurchasePro: (SubscriptionOffer) -> Unit,
    onRestorePurchases: () -> Unit,
    onManageSubscription: () -> Unit,
    onActivateProCode: (String) -> Unit,
) {
    val themeColors = LocalThemeColors.current
    var showPermissionSettings by remember { mutableStateOf(false) }
    var showDataPrivacy by remember { mutableStateOf(false) }
    var showLanguageSettings by remember { mutableStateOf(false) }
    var showDeleteAccountConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            themeColors.base,
                            themeColors.base.copy(alpha = 0.76f),
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = themeColors.onBase.copy(alpha = 0.14f),
                ) {
                    Image(
                        painter = painterResource(R.mipmap.ic_launcher_foreground),
                        contentDescription = "App Icon",
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape),
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        userSession?.displayName ?: AppText.t("me_not_signed_in"),
                        style = MaterialTheme.typography.titleLarge,
                        color = themeColors.onBase,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        userSession?.email ?: if (isGoogleSignInEnabled) {
                            AppText.t("me_sign_in_to_restore_subscriptions_and_prepare_for")
                        } else {
                            AppText.t("me_china_local_mode_subtitle")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.onBase.copy(alpha = 0.78f),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .offset(y = (-40).dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MeStatItem(value = userPoints.toInt().toString(), label = AppText.t("me_current_points"), color = themeColors.encourage)
                    HorizontalDivider(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    MeStatItem(value = "0", label = AppText.t("me_discipline_total"), color = themeColors.control)
                    HorizontalDivider(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    MeStatItem(value = "1", label = AppText.t("me_streak_days"), color = themeColors.base)
                }
            }

            MeMenuSection(AppText.t("me_account")) {
                if (!isGoogleSignInEnabled) {
                    LocalModeInfoPanel(userId = userSession?.userId)
                } else if (userSession == null) {
                    MeMenuItem(
                        icon = Icons.Default.Settings,
                        title = if (isGoogleSignInConfigured) AppText.t("me_usage_google_sign_in") else AppText.t("me_google_sign_in_is_not_configured"),
                        onClick = onSignInWithGoogle,
                        color = if (isGoogleSignInConfigured) themeColors.base else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    MeMenuItem(icon = Icons.Default.Settings, title = AppText.t("me_sign_out"), onClick = onSignOut)
                    MeMenuItem(
                        icon = Icons.Default.Delete,
                        title = AppText.t("me_delete_account"),
                        onClick = { showDeleteAccountConfirm = true },
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            MeMenuSection(AppText.t("me_subscription")) {
                SubscriptionStatusPanel(
                    entitlement = proEntitlement,
                    offers = subscriptionOffers,
                    isPlayBillingEnabled = isPlayBillingEnabled,
                    isLocalActivationEnabled = isLocalActivationEnabled,
                    localUserId = userSession?.userId,
                    onPurchasePro = onPurchasePro,
                    onRestorePurchases = onRestorePurchases,
                    onManageSubscription = onManageSubscription,
                    onActivateProCode = onActivateProCode,
                )
            }

            MeMenuSection(AppText.t("me_data_and_privacy")) {
                MeMenuItem(icon = Icons.Default.Settings, title = AppText.t("me_local_data_management"), onClick = { showDataPrivacy = true })
                MeMenuItem(icon = Icons.AutoMirrored.Filled.HelpOutline, title = AppText.t("me_privacy_policy"), onClick = onOpenPrivacyPolicy)
            }

            MeMenuSection(AppText.t("me_permission_settings")) {
                MeMenuItem(icon = Icons.Default.Settings, title = AppText.t("me_permission_settings"), onClick = { showPermissionSettings = true })
            }

            MeMenuSection(AppText.t("selected_language_title")) {
                MeMenuItem(
                    icon = Icons.Default.Settings,
                    title = "${AppText.t("selected_language_title")}: ${selectedAppLanguage.displayName()}",
                    onClick = { showLanguageSettings = true },
                )
            }

            MeMenuSection(AppText.t("me_appearance_theme")) {
                MeMenuItem(icon = Icons.Default.Palette, title = AppText.t("me_theme_management"), onClick = onNavigateToThemeSettings)
            }

            MeMenuSection(AppText.t("me_usage_records")) {
                MeMenuItem(icon = Icons.Default.History, title = AppText.t("me_usage_history"), onClick = onNavigateToHistory)
            }

            MeMenuSection(AppText.t("me_help_and_contact")) {
                MeMenuItem(icon = Icons.AutoMirrored.Filled.HelpOutline, title = AppText.t("me_help_and_feedback"), onClick = onNavigateToHelpFeedback)
                MeMenuItem(icon = Icons.Default.Email, title = AppText.t("me_contact_us"), onClick = onNavigateToContactUs)
            }

            MeMenuSection(AppText.t("me_label")) {
                MeMenuItem(
                    icon = Icons.Default.Science,
                    title = AppText.t("lab_laboratory_debug_tools"),
                    onClick = onNavigateToLaboratory,
                    color = themeColors.base,
                )
            }
        }
    }

    if (showPermissionSettings) {
        PermissionSettingsSheet(
            usageAccessGranted = usageAccessGranted,
            accessibilityServiceEnabled = accessibilityServiceEnabled,
            isAutoStartDismissed = isAutoStartDismissed,
            isIgnoringBattery = isIgnoringBattery,
            notificationPermissionGranted = notificationPermissionGranted,
            dismissedPermissionPrompts = dismissedPermissionPrompts,
            onDismiss = { showPermissionSettings = false },
            onOpenUsageAccessSettings = onOpenUsageAccessSettings,
            onOpenAccessibilitySettings = onOpenAccessibilitySettings,
            onOpenAutoStartSettings = onOpenAutoStartSettings,
            onSetAutoStartDismissed = onSetAutoStartDismissed,
            onRequestBatteryOptimization = onRequestBatteryOptimization,
            onRequestNotificationPermission = onRequestNotificationPermission,
            onClearDismissedPermissionPrompts = onClearDismissedPermissionPrompts,
        )
    }

    if (showDataPrivacy) {
        DataPrivacySheet(
            onDismiss = { showDataPrivacy = false },
            onExportLocalData = onExportLocalData,
            onClearLocalData = onClearLocalData,
            onOpenPrivacyPolicy = onOpenPrivacyPolicy,
        )
    }

    if (showLanguageSettings) {
        LanguageSettingsDialog(
            selected = selectedAppLanguage,
            onSelect = {
                onSelectAppLanguage(it)
                showLanguageSettings = false
            },
            onDismiss = { showLanguageSettings = false },
        )
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

@Composable
private fun LanguageSettingsDialog(
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppText.t("selected_language_title")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AppLanguage.entries.forEach { language ->
                    TextButton(
                        onClick = { onSelect(language) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = language.displayName(),
                            fontWeight = if (language == selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppText.t("group_cancel"))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DataPrivacySheet(
    onDismiss: () -> Unit,
    onExportLocalData: () -> Unit,
    onClearLocalData: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = AppText.t("me_local_data_management"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = AppText.t("me_tiny_vow_stores_the_apps_you_manage_usage"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Text(
                    text = AppText.t("me_export_files_are_created_only_in_local_cache"),
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = onExportLocalData,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(AppText.t("home_export_local_data"))
            }
            TextButton(
                onClick = onOpenPrivacyPolicy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(AppText.t("me_view_privacy_policy"))
            }
            TextButton(
                onClick = { showClearConfirm = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(AppText.t("me_clear_local_data"), color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(48.dp))
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
                        onDismiss()
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
private fun PermissionSettingsSheet(
    usageAccessGranted: Boolean,
    accessibilityServiceEnabled: Boolean,
    isAutoStartDismissed: Boolean,
    isIgnoringBattery: Boolean,
    notificationPermissionGranted: Boolean,
    dismissedPermissionPrompts: Set<String>,
    onDismiss: () -> Unit,
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

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = AppText.t("me_permission_settings"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = AppText.t("me_check_permission_status_or_restore_permission_prompts_dismiss"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun ThemeManager(
    selectedThemeId: String,
    customThemes: List<ThemeSeed>,
    onSelectTheme: (String) -> Unit,
    onSaveCustomTheme: (ThemeSeed) -> Unit,
    onDeleteCustomTheme: (String) -> Unit,
) {
    var editingTheme by remember { mutableStateOf<ThemeSeed?>(null) }
    val allThemes = ThemePresets + customThemes

    Column(modifier = Modifier.padding(vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            allThemes.forEach { theme ->
                ThemePreviewCard(
                    theme = theme,
                    selected = selectedThemeId == theme.id,
                    onSelect = { onSelectTheme(theme.id) },
                    onEdit = {
                        editingTheme = if (theme.isCustom) {
                            theme
                        } else {
                            createCustomTheme(
                                name = AppText.t("me_value_custom", theme.name),
                                controlColor = theme.controlColor,
                                encourageColor = theme.encourageColor,
                                baseColor = theme.baseColor,
                            )
                        }
                    },
                    onCopy = {
                        editingTheme = createCustomTheme(
                            name = AppText.t("me_value_copy", theme.name),
                            controlColor = theme.controlColor,
                            encourageColor = theme.encourageColor,
                            baseColor = theme.baseColor,
                        )
                    },
                    onDelete = if (theme.isCustom) {
                        { onDeleteCustomTheme(theme.id) }
                    } else {
                        null
                    },
                )
            }
            AddThemeCard {
                editingTheme = createCustomTheme(
                    name = AppText.t("settings_custom_theme"),
                    controlColor = ThemePresets.first().controlColor,
                    encourageColor = ThemePresets.first().encourageColor,
                    baseColor = ThemePresets.first().baseColor,
                )
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ThemeLegendDot(AppText.t("me_limit"), LocalThemeColors.current.control)
            ThemeLegendDot(AppText.t("me_encourage"), LocalThemeColors.current.encourage)
            ThemeLegendDot(AppText.t("me_base"), LocalThemeColors.current.base)
        }
    }

    editingTheme?.let { theme ->
        ThemeEditorDialog(
            initialTheme = theme,
            onDismiss = { editingTheme = null },
            onSave = {
                onSaveCustomTheme(it.copy(isCustom = true))
                editingTheme = null
            },
        )
    }
}

@Composable
private fun RowScope.ThemePreviewCard(
    theme: ThemeSeed,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onDelete: (() -> Unit)?,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Surface(
        modifier = Modifier
            .width(156.dp)
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.36f) else MaterialTheme.colorScheme.surface,
        tonalElevation = if (selected) 2.dp else 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = if (selected) 0.72f else 0.46f)),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.height(26.dp).clip(RoundedCornerShape(8.dp))) {
                ThemeStrip(Color(theme.controlColor))
                ThemeStrip(Color(theme.encourageColor))
                ThemeStrip(Color(theme.baseColor))
            }
            Text(
                text = theme.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onCopy, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = AppText.t("me_copy"), modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = AppText.t("me_edit"), modifier = Modifier.size(16.dp))
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = AppText.t("group_delete"), modifier = Modifier.size(16.dp), tint = LocalThemeColors.current.control)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.AddThemeCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(126.dp)
            .height(118.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Default.Add, contentDescription = AppText.t("me_new"), tint = MaterialTheme.colorScheme.primary)
            Text(AppText.t("me_new_theme"), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ThemeEditorDialog(
    initialTheme: ThemeSeed,
    onDismiss: () -> Unit,
    onSave: (ThemeSeed) -> Unit,
) {
    var name by remember(initialTheme.id) { mutableStateOf(initialTheme.name) }
    var control by remember(initialTheme.id) { mutableStateOf(initialTheme.controlColor) }
    var encourage by remember(initialTheme.id) { mutableStateOf(initialTheme.encourageColor) }
    var base by remember(initialTheme.id) { mutableStateOf(initialTheme.baseColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppText.t("me_edit_theme"), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(AppText.t("me_theme_name")) },
                    singleLine = true,
                )
                ColorSliderGroup(AppText.t("me_limit_color"), control, onColorChange = { control = it })
                ColorSliderGroup(AppText.t("me_encourage_color"), encourage, onColorChange = { encourage = it })
                ColorSliderGroup(AppText.t("me_base_color"), base, onColorChange = { base = it })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        initialTheme.copy(
                            name = name.ifBlank { AppText.t("settings_custom_theme") },
                            controlColor = control,
                            encourageColor = encourage,
                            baseColor = base,
                            isCustom = true,
                        )
                    )
                }
            ) {
                Text(AppText.t("group_save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppText.t("group_cancel")) }
        },
    )
}

@Composable
private fun ColorSliderGroup(
    label: String,
    color: Int,
    onColorChange: (Int) -> Unit,
) {
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var value by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(color) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color, hsv)
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
    }

    fun emit() {
        onColorChange(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(color))
            )
            Text("$label ${argbToHex(color)}", style = MaterialTheme.typography.labelMedium)
        }
        Slider(value = hue, onValueChange = { hue = it; emit() }, valueRange = 0f..360f)
        Slider(value = saturation, onValueChange = { saturation = it; emit() }, valueRange = 0.12f..0.82f)
        Slider(value = value, onValueChange = { value = it; emit() }, valueRange = 0.36f..0.92f)
    }
}

@Composable
private fun RowScope.ThemeStrip(color: Color) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(color)
    )
}

@Composable
private fun ThemeLegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LocalModeInfoPanel(userId: String?) {
    val clipboard = LocalClipboardManager.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = AppText.t("me_china_local_mode_title"),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = AppText.t("me_china_local_mode_body"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!userId.isNullOrBlank()) {
            Text(
                text = AppText.t("activation_user_id_label", userId),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(
                onClick = {
                    clipboard.setText(AnnotatedString(userId))
                },
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(AppText.t("activation_copy_user_id"))
            }
        }
    }
}

@Composable
private fun SubscriptionStatusPanel(
    entitlement: ProEntitlementState,
    offers: List<SubscriptionOffer>,
    isPlayBillingEnabled: Boolean,
    isLocalActivationEnabled: Boolean,
    localUserId: String?,
    onPurchasePro: (SubscriptionOffer) -> Unit,
    onRestorePurchases: () -> Unit,
    onManageSubscription: () -> Unit,
    onActivateProCode: (String) -> Unit,
) {
    val firstOffer = offers.firstOrNull()
    val isActive = entitlement.status == ProEntitlementStatus.ACTIVE
    val isPending = entitlement.status == ProEntitlementStatus.PENDING
    var showActivationDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Tiny Vow Pro",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = entitlementStatusText(entitlement),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = firstOffer?.price ?: if (isActive) AppText.t("me_unlocked") else AppText.t("me_loading"),
                style = MaterialTheme.typography.labelLarge,
                color = if (isActive) LocalThemeColors.current.encourage else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }

        Text(
            text = if (isPlayBillingEnabled) {
                AppText.t("me_core_features_remain_free_pro_is_reserved_for")
            } else {
                AppText.t("me_china_pro_activation_description")
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

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

        Button(
            onClick = {
                val offer = firstOffer
                if (offer == null) {
                    onRestorePurchases()
                } else {
                    onPurchasePro(offer)
                }
            },
            enabled = !isActive && !isPending,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (firstOffer == null) AppText.t("me_loading_subscription_info") else AppText.t("me_buy_pro"))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = onRestorePurchases) {
                Text(AppText.t("me_restore_purchases"))
            }
            TextButton(onClick = onManageSubscription) {
                Text(AppText.t("me_manage_subscription"))
            }
        }
    }
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
private fun ActivationCodeDialog(
    userId: String,
    onDismiss: () -> Unit,
    onActivate: (String) -> Unit,
) {
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = userId,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MeMenuSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun MeMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = color)
        Spacer(Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.weight(1f))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}
