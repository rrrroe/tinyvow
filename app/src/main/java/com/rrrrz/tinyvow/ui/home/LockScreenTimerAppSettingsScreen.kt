package com.rrrrz.tinyvow.ui.home

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rrrrz.tinyvow.data.accessibility.AccessibilityServiceStateChecker
import com.rrrrz.tinyvow.data.apps.InstalledAppRepository
import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.data.db.LockScreenTimerAppStatus
import com.rrrrz.tinyvow.data.lockscreen.LockScreenTimerAppRepository
import com.rrrrz.tinyvow.data.lockscreen.LockScreenTimerAppSettingsRow
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.service.block.AppLimitAccessibilityService
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.TinyVowButton
import com.rrrrz.tinyvow.ui.theme.TinyVowButtonTone
import com.rrrrz.tinyvow.ui.theme.TinyVowCard
import com.rrrrz.tinyvow.ui.theme.TinyVowDetailScaffold
import com.rrrrz.tinyvow.ui.theme.TinyVowEmptyState
import com.rrrrz.tinyvow.ui.theme.TinyVowRadius
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreenTimerAppSettingsScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val themeColors = LocalThemeColors.current
    val scope = rememberCoroutineScope()
    val repository = remember(context) { LockScreenTimerAppRepository(context) }
    val installedAppRepository = remember(context) { InstalledAppRepository(context) }
    val accessibilityChecker = remember(context) { AccessibilityServiceStateChecker(context) }
    var rows by remember { mutableStateOf<List<LockScreenTimerAppSettingsRow>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var accessibilityEnabled by remember {
        mutableStateOf(accessibilityChecker.isEnabled(AppLimitAccessibilityService::class.java))
    }
    var showAddDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    fun refresh() {
        scope.launch {
            loading = rows.isEmpty()
            rows = repository.buildSettingsRows()
            accessibilityEnabled = accessibilityChecker.isEnabled(AppLimitAccessibilityService::class.java)
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            rows = repository.buildSettingsRows()
            accessibilityEnabled = accessibilityChecker.isEnabled(AppLimitAccessibilityService::class.java)
            loading = false
            delay(1_000L)
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text(AppText.t("lock_screen_timer_app_permission_dialog_title")) },
            text = { Text(AppText.t("lock_screen_timer_app_permission_dialog_body")) },
            confirmButton = {
                TinyVowButton(
                    onClick = {
                        showPermissionDialog = false
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                    tone = TinyVowButtonTone.Primary,
                ) {
                    Text(AppText.t("lock_screen_timer_app_open_accessibility_settings"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(AppText.t("group_cancel"))
                }
            },
        )
    }

    if (showAddDialog) {
        LockScreenTimerAppAddDialog(
            installedAppRepository = installedAppRepository,
            excludedPackageNames = rows.map { it.config.packageName }.toSet(),
            onDismiss = { showAddDialog = false },
            onAdd = { app ->
                scope.launch {
                    repository.addOrEnableApp(app)
                    showAddDialog = false
                    refresh()
                }
            },
        )
    }

    TinyVowDetailScaffold(
        title = AppText.t("lock_screen_timer_app_settings_title"),
        onBack = onBack,
        navigationContentDescription = AppText.t("group_back"),
        actions = {
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = AppText.t("lock_screen_timer_app_add_app"))
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = TinyVowSpacing.PageHorizontal,
                    vertical = TinyVowSpacing.PageTop,
                ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.SectionGap),
        ) {
            TinyVowCard(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(TinyVowRadius.FeaturedCard),
                color = MaterialTheme.colorScheme.primaryContainer,
                borderAlpha = 0.18f,
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = TinyVowSpacing.CardHorizontal,
                        vertical = TinyVowSpacing.CardVertical,
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = AppText.t("lock_screen_timer_app_settings_description"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeColors.ink.copy(alpha = 0.78f),
                    )
                    Text(
                        text = AppText.t("lock_screen_timer_app_limit_note"),
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.inkMuted,
                    )
                }
            }

            LockScreenTimerPermissionCard(
                accessibilityEnabled = accessibilityEnabled,
                onOpenSettings = { showPermissionDialog = true },
            )

            if (loading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text(AppText.t("special_app_request_running"), style = MaterialTheme.typography.bodySmall)
                }
            } else if (rows.isEmpty()) {
                LockScreenTimerEmptyCard(onAdd = { showAddDialog = true })
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    rows.forEach { row ->
                        LockScreenTimerAppRow(
                            row = row,
                            onRemove = {
                                scope.launch {
                                    repository.removeApp(row.config.packageName)
                                    refresh()
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LockScreenTimerPermissionCard(
    accessibilityEnabled: Boolean,
    onOpenSettings: () -> Unit,
) {
    LockScreenSettingsSurface {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.LockClock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = AppText.t("lock_screen_timer_app_permission_title"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (accessibilityEnabled) {
                        AppText.t("lock_screen_timer_app_permission_enabled")
                    } else {
                        AppText.t("lock_screen_timer_app_permission_disabled")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TinyVowButton(text = AppText.t("lock_screen_timer_app_permission_action"), onClick = onOpenSettings)
        }
    }
}

@Composable
private fun LockScreenTimerEmptyCard(onAdd: () -> Unit) {
    TinyVowEmptyState(
        title = AppText.t("lock_screen_timer_app_empty_title"),
        body = AppText.t("lock_screen_timer_app_empty_description"),
        icon = Icons.Default.LockClock,
        action = {
            TinyVowButton(
                text = AppText.t("lock_screen_timer_app_add_app"),
                onClick = onAdd,
                tone = TinyVowButtonTone.Primary,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}

@Composable
private fun LockScreenTimerAppRow(
    row: LockScreenTimerAppSettingsRow,
    onRemove: () -> Unit,
) {
    LockScreenSettingsSurface {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LockScreenTimerAppIcon(packageName = row.config.packageName, appName = row.config.appLabelSnapshot)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = row.config.appLabelSnapshot,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = AppText.t("lock_screen_timer_app_remove_app"),
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            LockScreenMetricRow(
                label = AppText.t("lock_screen_timer_app_today_lock_screen"),
                value = formatLockScreenTimerDuration(row.todayLockScreenMillis),
            )
            LockScreenMetricRow(
                label = AppText.t("lock_screen_timer_app_today_foreground"),
                value = formatLockScreenTimerDuration(row.todayForegroundUsageMillis),
            )
            LockScreenMetricRow(
                label = AppText.t("lock_screen_timer_app_status"),
                value = row.lastStatus.label(row.isActive),
            )
        }
    }
}

@Composable
private fun LockScreenMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun LockScreenTimerAppAddDialog(
    installedAppRepository: InstalledAppRepository,
    excludedPackageNames: Set<String>,
    onDismiss: () -> Unit,
    onAdd: (ManagedApp) -> Unit,
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var apps by remember { mutableStateOf<List<ManagedApp>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val systemLauncherPackages = remember(context) { resolveLockScreenSystemLauncherPackages(context) }

    LaunchedEffect(systemLauncherPackages, excludedPackageNames) {
        apps = installedAppRepository.getAllInstalledApps()
            .filterNot { it.packageName in excludedPackageNames }
            .filterNot { it.packageName in systemLauncherPackages }
            .filterNot { it.looksLikeLockScreenLauncherApp() }
        loading = false
    }

    val filteredApps =
        remember(apps, query) {
            val normalizedQuery = query.trim().lowercase()
            if (normalizedQuery.isBlank()) {
                apps
            } else {
                apps.filter {
                    it.appName.lowercase().contains(normalizedQuery) ||
                        it.packageName.lowercase().contains(normalizedQuery)
                }
            }
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppText.t("lock_screen_timer_app_add_dialog_title")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(AppText.t("lock_screen_timer_app_search_hint")) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (loading) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text(AppText.t("special_app_request_running"), style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            LockScreenTimerAppCandidateRow(app = app, onClick = { onAdd(app) })
                        }
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

@Composable
private fun LockScreenTimerAppCandidateRow(
    app: ManagedApp,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LockScreenTimerAppIcon(packageName = app.packageName, appName = app.appName)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = app.appName,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LockScreenTimerAppIcon(
    packageName: String,
    appName: String,
) {
    val context = LocalContext.current
    var icon by remember(packageName) { mutableStateOf<Drawable?>(null) }

    LaunchedEffect(packageName) {
        icon = withContext(Dispatchers.IO) {
            AppVisualCache.getIcon(context, packageName)
        }
    }

    Surface(
        modifier = Modifier.size(48.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (icon != null) {
                AsyncImage(
                    model = icon,
                    contentDescription = appName,
                    modifier = Modifier.size(38.dp),
                )
            } else {
                Icon(
                    imageVector = Icons.Default.LockClock,
                    contentDescription = appName,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun resolveLockScreenSystemLauncherPackages(context: Context): Set<String> {
    val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    val resolvedHome =
        runCatching {
            context.packageManager.resolveActivity(homeIntent, 0)?.activityInfo?.packageName
        }.getOrNull()
    return setOfNotNull(
        context.packageName,
        resolvedHome,
        "com.android.systemui",
        "com.google.android.systemui",
        "com.miui.home",
        "com.android.launcher",
        "com.android.launcher3",
        "com.google.android.apps.nexuslauncher",
        "com.huawei.android.launcher",
        "com.oppo.launcher",
        "com.vivo.launcher",
        "com.sec.android.app.launcher",
    )
}

private fun ManagedApp.looksLikeLockScreenLauncherApp(): Boolean {
    val packageLower = packageName.lowercase()
    val nameLower = appName.lowercase()
    return packageLower.contains("launcher") ||
        nameLower == "系统桌面" ||
        nameLower == "桌面" ||
        nameLower.contains("launcher")
}

@Composable
private fun LockScreenSettingsSurface(content: @Composable () -> Unit) {
    TinyVowCard(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

private fun LockScreenTimerAppStatus.label(isActive: Boolean): String =
    when {
        isActive -> AppText.t("lock_screen_timer_app_status_active")
        this == LockScreenTimerAppStatus.ACTIVE -> AppText.t("lock_screen_timer_app_status_active")
        this == LockScreenTimerAppStatus.IDLE -> AppText.t("lock_screen_timer_app_status_idle")
        this == LockScreenTimerAppStatus.SCREEN_OFF -> AppText.t("lock_screen_timer_app_status_screen_off")
        this == LockScreenTimerAppStatus.UNLOCKED -> AppText.t("lock_screen_timer_app_status_unlocked")
        else -> AppText.t("lock_screen_timer_app_status_unknown")
    }

private fun formatLockScreenTimerDuration(durationMillis: Long): String {
    val totalSeconds = (durationMillis / 1_000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L
    return when {
        hours > 0L -> AppText.t("media_app_duration_hms", hours, minutes, seconds)
        minutes > 0L -> AppText.t("media_app_duration_ms", minutes, seconds)
        else -> AppText.t("media_app_duration_s", seconds)
    }
}
