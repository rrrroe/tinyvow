package com.rrrrz.tinyvow.ui.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.rrrrz.tinyvow.data.apps.InstalledAppRepository
import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.usage.UsageAccessStateChecker
import com.rrrrz.tinyvow.data.usage.UsageAccessStatus
import com.rrrrz.tinyvow.data.usage.UsageStatsUsageRepository
import com.rrrrz.tinyvow.ui.theme.TinyVowTheme
import kotlinx.coroutines.launch

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val checker = remember(context) { UsageAccessStateChecker(context) }
    val appRepository = remember(context) { InstalledAppRepository(context) }
    val preferences = remember(context) { ManagedAppPreferences(context) }
    val usageRepository = remember(context) { UsageStatsUsageRepository(context) }

    var usageAccessStatus by remember { mutableStateOf(checker.getStatus()) }
    var installedApps by remember { mutableStateOf<List<ManagedApp>>(emptyList()) }
    var isLoadingApps by remember { mutableStateOf(false) }
    var isLoadingUsage by remember { mutableStateOf(false) }
    var todayUsageMillis by remember { mutableLongStateOf(0L) }
    var usageRefreshTick by remember { mutableIntStateOf(0) }
    val selectedPackageName by preferences.selectedPackageName.collectAsState(initial = null)

    DisposableEffect(lifecycleOwner, checker) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                usageAccessStatus = checker.getStatus()
                usageRefreshTick++
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(usageAccessStatus) {
        if (usageAccessStatus == UsageAccessStatus.GRANTED) {
            isLoadingApps = true
            installedApps = appRepository.getLaunchableApps()
            isLoadingApps = false
        } else {
            installedApps = emptyList()
            todayUsageMillis = 0L
            isLoadingApps = false
            isLoadingUsage = false
        }
    }

    LaunchedEffect(usageAccessStatus, installedApps, selectedPackageName) {
        if (
            usageAccessStatus == UsageAccessStatus.GRANTED &&
            installedApps.isNotEmpty() &&
            selectedPackageName == null
        ) {
            preferences.setSelectedPackageName(installedApps.first().packageName)
        }
    }

    LaunchedEffect(usageAccessStatus, selectedPackageName, usageRefreshTick) {
        if (usageAccessStatus == UsageAccessStatus.GRANTED && selectedPackageName != null) {
            isLoadingUsage = true
            todayUsageMillis = usageRepository.getTodayUsageMillis(selectedPackageName!!)
            isLoadingUsage = false
        } else {
            todayUsageMillis = 0L
            isLoadingUsage = false
        }
    }

    HomeScreen(
        usageAccessStatus = usageAccessStatus,
        installedApps = installedApps,
        selectedApp = installedApps.firstOrNull { it.packageName == selectedPackageName },
        isLoadingApps = isLoadingApps,
        isLoadingUsage = isLoadingUsage,
        todayUsageMillis = todayUsageMillis,
        onOpenUsageAccessSettings = {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        },
        onSelectApp = { app ->
            coroutineScope.launch {
                preferences.setSelectedPackageName(app.packageName)
            }
        },
        onRefreshUsage = {
            usageRefreshTick++
        },
        modifier = modifier,
    )
}

@Composable
fun HomeScreen(
    usageAccessStatus: UsageAccessStatus,
    installedApps: List<ManagedApp>,
    selectedApp: ManagedApp?,
    isLoadingApps: Boolean,
    isLoadingUsage: Boolean,
    todayUsageMillis: Long,
    onOpenUsageAccessSettings: () -> Unit,
    onSelectApp: (ManagedApp) -> Unit,
    onRefreshUsage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val usageAccessGranted = usageAccessStatus == UsageAccessStatus.GRANTED
    val statusColor = if (usageAccessGranted) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            PermissionCard(
                usageAccessGranted = usageAccessGranted,
                statusColor = statusColor,
                onOpenUsageAccessSettings = onOpenUsageAccessSettings,
            )

            if (usageAccessGranted) {
                UsageOverviewCard(
                    selectedApp = selectedApp,
                    todayUsageMillis = todayUsageMillis,
                    isLoadingUsage = isLoadingUsage,
                    onRefreshUsage = onRefreshUsage,
                )
                AppPickerCard(
                    installedApps = installedApps,
                    selectedApp = selectedApp,
                    isLoadingApps = isLoadingApps,
                    onSelectApp = onSelectApp,
                )
            } else {
                GuidanceCard(
                    title = stringResource(R.string.permission_steps_title),
                    body = stringResource(R.string.permission_steps_body),
                )
            }

            GuidanceCard(
                title = stringResource(R.string.mvp_scope_title),
                body = stringResource(R.string.mvp_scope_body),
            )

            GuidanceCard(
                title = stringResource(R.string.next_step_title),
                body = if (usageAccessGranted) {
                    stringResource(R.string.next_step_body_enabled)
                } else {
                    stringResource(R.string.next_step_body)
                },
            )

            Spacer(modifier = Modifier.height(12.dp))
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
private fun UsageOverviewCard(
    selectedApp: ManagedApp?,
    todayUsageMillis: Long,
    isLoadingUsage: Boolean,
    onRefreshUsage: () -> Unit,
) {
    ElevatedCard(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.usage_card_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = selectedApp?.appName ?: stringResource(R.string.app_picker_empty_selected),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            if (selectedApp != null) {
                Text(
                    text = selectedApp.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (isLoadingUsage) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text(text = stringResource(R.string.usage_loading))
                }
            } else {
                Text(
                    text = formatUsageDuration(todayUsageMillis),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                text = stringResource(R.string.usage_card_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedButton(
                onClick = onRefreshUsage,
                enabled = selectedApp != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.usage_refresh_action))
            }
        }
    }
}

@Composable
private fun AppPickerCard(
    installedApps: List<ManagedApp>,
    selectedApp: ManagedApp?,
    isLoadingApps: Boolean,
    onSelectApp: (ManagedApp) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

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
                text = stringResource(R.string.app_picker_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.app_picker_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (isLoadingApps) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text(text = stringResource(R.string.app_picker_loading))
                }
            } else if (installedApps.isEmpty()) {
                Text(
                    text = stringResource(R.string.app_picker_empty),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = selectedApp?.appName
                                ?: stringResource(R.string.app_picker_action),
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .heightIn(max = 360.dp),
                    ) {
                        installedApps.forEach { app ->
                            DropdownMenuItem(
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(text = app.appName)
                                        Text(
                                            text = app.packageName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                },
                                onClick = {
                                    expanded = false
                                    onSelectApp(app)
                                },
                            )
                        }
                    }
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

private fun formatUsageDuration(durationMillis: Long): String {
    val totalMinutes = durationMillis / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}小时 ${minutes}分钟"
        hours > 0 -> "${hours}小时"
        else -> "${minutes}分钟"
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreviewDenied() {
    TinyVowTheme {
        HomeScreen(
            usageAccessStatus = UsageAccessStatus.DENIED,
            installedApps = emptyList(),
            selectedApp = null,
            isLoadingApps = false,
            isLoadingUsage = false,
            todayUsageMillis = 0L,
            onOpenUsageAccessSettings = {},
            onSelectApp = {},
            onRefreshUsage = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreviewGranted() {
    TinyVowTheme {
        HomeScreen(
            usageAccessStatus = UsageAccessStatus.GRANTED,
            installedApps = listOf(
                ManagedApp(
                    packageName = "com.example.video",
                    appName = "短视频",
                ),
            ),
            selectedApp = ManagedApp(
                packageName = "com.example.video",
                appName = "短视频",
            ),
            isLoadingApps = false,
            isLoadingUsage = false,
            todayUsageMillis = 5_400_000L,
            onOpenUsageAccessSettings = {},
            onSelectApp = {},
            onRefreshUsage = {},
        )
    }
}
