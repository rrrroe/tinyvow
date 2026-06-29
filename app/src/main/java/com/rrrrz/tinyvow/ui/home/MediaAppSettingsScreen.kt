package com.rrrrz.tinyvow.ui.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.rrrrz.tinyvow.data.apps.InstalledAppRepository
import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.data.db.MediaAppPlaybackStatus
import com.rrrrz.tinyvow.data.media.MediaAppPlaybackRepository
import com.rrrrz.tinyvow.data.media.MediaAppSettingsRow
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.service.media.MediaAppPlaybackListenerService
import com.rrrrz.tinyvow.service.media.MediaAppPlaybackMonitor
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.TinyVowCard
import com.rrrrz.tinyvow.ui.theme.TinyVowRadius
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaAppSettingsScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val themeColors = LocalThemeColors.current
    val scope = rememberCoroutineScope()
    val repository = remember(context) { MediaAppPlaybackRepository(context) }
    val installedAppRepository = remember(context) { InstalledAppRepository(context) }
    var rows by remember { mutableStateOf<List<MediaAppSettingsRow>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var listenerEnabled by remember { mutableStateOf(MediaAppPlaybackMonitor.isNotificationListenerEnabled(context)) }
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    fun refresh() {
        scope.launch {
            loading = rows.isEmpty()
            rows = repository.buildSettingsRows()
            listenerEnabled = MediaAppPlaybackMonitor.isNotificationListenerEnabled(context)
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            rows = repository.buildSettingsRows()
            listenerEnabled = MediaAppPlaybackMonitor.isNotificationListenerEnabled(context)
            loading = false
            delay(1_000L)
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text(AppText.t("media_app_permission_dialog_title")) },
            text = { Text(AppText.t("media_app_permission_dialog_body")) },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        context.openNotificationListenerSettings(
                            ComponentName(context, MediaAppPlaybackListenerService::class.java),
                        )
                    },
                ) {
                    Text(AppText.t("media_app_open_listener_settings"))
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
        MediaAppAddDialog(
            installedAppRepository = installedAppRepository,
            excludedPackageNames = rows.map { it.config.packageName }.toSet(),
            onDismiss = { showAddDialog = false },
            onAdd = { app ->
                scope.launch {
                    repository.addOrEnableApp(app)
                    showAddDialog = false
                    MediaAppPlaybackMonitor.requestListenerRebind(context)
                    refresh()
                }
            },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppText.t("media_app_settings_title"),
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
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = AppText.t("media_app_add_app"))
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
                        text = AppText.t("media_app_settings_description"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = themeColors.ink.copy(alpha = 0.78f),
                    )
                    Text(
                        text = AppText.t("media_app_settings_limit_note"),
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.inkMuted,
                    )
                }
            }

            MediaPermissionCard(
                listenerEnabled = listenerEnabled,
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
                MediaEmptyCard(onAdd = { showAddDialog = true })
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    rows.forEach { row ->
                        MediaAppRow(
                            row = row,
                            nowMillis = nowMillis,
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
private fun MediaPermissionCard(
    listenerEnabled: Boolean,
    onOpenSettings: () -> Unit,
) {
    SettingsSurface {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Headphones,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = AppText.t("media_app_permission_title"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (listenerEnabled) {
                        AppText.t("media_app_permission_enabled")
                    } else {
                        AppText.t("media_app_permission_disabled")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(onClick = onOpenSettings) {
                Text(AppText.t("media_app_permission_action"))
            }
        }
    }
}

@Composable
private fun MediaEmptyCard(onAdd: () -> Unit) {
    SettingsSurface {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = AppText.t("media_app_empty_title"),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = AppText.t("media_app_empty_description"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
                Text(AppText.t("media_app_add_app"))
            }
        }
    }
}

@Composable
private fun MediaAppRow(
    row: MediaAppSettingsRow,
    nowMillis: Long,
    onRemove: () -> Unit,
) {
    val displayedBackgroundMillis =
        row.todayTrustedPlaybackMillis + if (row.isPlaying && row.lastConfirmedAt != null) {
            (nowMillis - row.lastConfirmedAt).coerceIn(0L, MediaAppPlaybackMonitor.TRUSTED_RECONNECT_WINDOW_MILLIS)
        } else {
            0L
        }
    SettingsSurface {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MediaAppIcon(packageName = row.config.packageName, appName = row.config.appLabelSnapshot)
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
                    Icon(Icons.Default.DeleteOutline, contentDescription = AppText.t("media_app_remove_app"))
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            MediaMetricRow(
                label = AppText.t("media_app_today_background"),
                value = formatMediaAppDuration(displayedBackgroundMillis),
            )
            MediaMetricRow(
                label = AppText.t("media_app_today_foreground"),
                value = formatMediaAppDuration(row.todayForegroundUsageMillis),
            )
            MediaMetricRow(
                label = AppText.t("media_app_listening_status"),
                value = row.lastStatus.label(),
            )
            if (row.todayUntrustedGapMillis > 0L) {
                MediaMetricRow(
                    label = AppText.t("media_app_untrusted_gap"),
                    value = formatMediaAppDuration(row.todayUntrustedGapMillis),
                )
            }
        }
    }
}

@Composable
private fun MediaMetricRow(label: String, value: String) {
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
private fun MediaAppAddDialog(
    installedAppRepository: InstalledAppRepository,
    excludedPackageNames: Set<String>,
    onDismiss: () -> Unit,
    onAdd: (ManagedApp) -> Unit,
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var apps by remember { mutableStateOf<List<ManagedApp>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val systemLauncherPackages = remember(context) { resolveSystemLauncherPackages(context) }

    LaunchedEffect(systemLauncherPackages, excludedPackageNames) {
        apps = installedAppRepository.getAllInstalledApps()
            .filterNot { it.packageName in excludedPackageNames }
            .filterNot { it.packageName in systemLauncherPackages }
            .filterNot { it.looksLikeLauncherApp() }
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
        title = { Text(AppText.t("media_app_add_dialog_title")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(AppText.t("media_app_search_hint")) },
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
                            MediaAppCandidateRow(app = app, onClick = { onAdd(app) })
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
private fun MediaAppCandidateRow(
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
        MediaAppIcon(packageName = app.packageName, appName = app.appName)
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
private fun MediaAppIcon(
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
                    imageVector = Icons.Default.Headphones,
                    contentDescription = appName,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun resolveSystemLauncherPackages(context: Context): Set<String> {
    val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    val resolvedHome =
        runCatching {
            context.packageManager.resolveActivity(homeIntent, 0)?.activityInfo?.packageName
        }.getOrNull()
    return setOfNotNull(
        context.packageName,
        resolvedHome,
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

private fun ManagedApp.looksLikeLauncherApp(): Boolean {
    val packageLower = packageName.lowercase()
    val nameLower = appName.lowercase()
    return packageLower.contains("launcher") ||
        nameLower == "系统桌面" ||
        nameLower == "桌面" ||
        nameLower.contains("launcher")
}

@Composable
private fun SettingsSurface(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)),
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

private fun MediaAppPlaybackStatus.label(): String =
    when (this) {
        MediaAppPlaybackStatus.UNKNOWN -> AppText.t("media_app_status_unknown")
        MediaAppPlaybackStatus.PLAYING -> AppText.t("media_app_status_playing")
        MediaAppPlaybackStatus.PAUSED -> AppText.t("media_app_status_paused")
        MediaAppPlaybackStatus.STOPPED -> AppText.t("media_app_status_stopped")
        MediaAppPlaybackStatus.BUFFERING -> AppText.t("media_app_status_buffering")
        MediaAppPlaybackStatus.NO_SESSION -> AppText.t("media_app_status_no_session")
    }

private fun formatMediaAppDuration(durationMillis: Long): String {
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
