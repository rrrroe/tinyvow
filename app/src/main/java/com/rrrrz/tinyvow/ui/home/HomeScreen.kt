package com.rrrrz.tinyvow.ui.home

import android.Manifest
import android.content.Intent
import android.provider.Settings
import android.os.PowerManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.data.apps.InstalledAppRepository
import com.rrrrz.tinyvow.data.accessibility.AccessibilityServiceStateChecker
import com.rrrrz.tinyvow.data.apps.ManagedApp
import com.rrrrz.tinyvow.data.notification.NotificationPermissionChecker
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.reminder.LimitReminderScheduler
import com.rrrrz.tinyvow.data.usage.UsageAccessStateChecker
import com.rrrrz.tinyvow.data.usage.UsageAccessStatus
import com.rrrrz.tinyvow.data.usage.UsageStatsUsageRepository
import com.rrrrz.tinyvow.domain.limit.DailyLimitEvaluation
import com.rrrrz.tinyvow.domain.limit.DailyTimeLimitPolicy
import com.rrrrz.tinyvow.service.block.AppLimitAccessibilityService
import com.rrrrz.tinyvow.ui.theme.TinyVowTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flowOf

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
    val notificationPermissionChecker = remember(context) { NotificationPermissionChecker(context) }
    val powerManager = remember(context) { context.getSystemService(android.content.Context.POWER_SERVICE) as PowerManager }
    var isIgnoringBattery by remember { mutableStateOf(powerManager.isIgnoringBatteryOptimizations(context.packageName)) }
    val preferences = remember(context) { ManagedAppPreferences(context) }
    val reminderScheduler = remember(context) { LimitReminderScheduler(context) }
    val usageRepository = remember(context) { UsageStatsUsageRepository(context) }
    val limitPolicy = remember { DailyTimeLimitPolicy() }

    var usageAccessStatus by remember { mutableStateOf(checker.getStatus()) }
    var accessibilityServiceEnabled by remember {
        mutableStateOf(accessibilityServiceStateChecker.isEnabled(AppLimitAccessibilityService::class.java))
    }
    var notificationPermissionGranted by remember {
        mutableStateOf(notificationPermissionChecker.isGranted())
    }
    var installedApps by remember { mutableStateOf<List<ManagedApp>>(emptyList()) }
    var isLoadingApps by remember { mutableStateOf(false) }
    var isLoadingUsage by remember { mutableStateOf(false) }
    var todayUsageMillis by remember { mutableLongStateOf(0L) }
    var usageRefreshTick by remember { mutableIntStateOf(0) }
    val selectedPackageName by preferences.selectedPackageName.collectAsState(initial = null)
    val dailyLimitMinutesFlow = remember(selectedPackageName) {
        selectedPackageName?.let(preferences::dailyLimitMinutes) ?: flowOf(null)
    }
    val dailyLimitMinutes by dailyLimitMinutesFlow.collectAsState(initial = null)
    val isAutoStartDismissed by preferences.isAutoStartDismissed.collectAsState(initial = false)
    val limitEvaluation = remember(todayUsageMillis, dailyLimitMinutes) {
        dailyLimitMinutes?.let { limitMinutes ->
            limitPolicy.evaluate(
                usageMillis = todayUsageMillis,
                limitMillis = limitMinutes * 60_000L,
            )
        }
    }
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

    LaunchedEffect(usageAccessStatus, dailyLimitMinutes, selectedPackageName, notificationPermissionGranted) {
        if (
            usageAccessStatus == UsageAccessStatus.GRANTED &&
            selectedPackageName != null &&
            dailyLimitMinutes != null &&
            notificationPermissionGranted
        ) {
            reminderScheduler.schedule()
        }
    }

    HomeScreen(
        usageAccessStatus = usageAccessStatus,
        accessibilityServiceEnabled = accessibilityServiceEnabled,
        notificationPermissionGranted = notificationPermissionGranted,
        isIgnoringBattery = isIgnoringBattery,
        installedApps = installedApps,
        selectedApp = installedApps.firstOrNull { it.packageName == selectedPackageName },
        isLoadingApps = isLoadingApps,
        isLoadingUsage = isLoadingUsage,
        todayUsageMillis = todayUsageMillis,
        dailyLimitMinutes = dailyLimitMinutes,
        limitEvaluation = limitEvaluation,
        onOpenUsageAccessSettings = {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        },
        onOpenAccessibilitySettings = {
            context.startActivity(
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        },
        onRequestNotificationPermission = {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
        onOpenAutoStartSettings = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        },
        onRequestBatteryOptimization = {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
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
        onSaveDailyLimit = { minutes ->
            val packageName = selectedPackageName ?: return@HomeScreen
            coroutineScope.launch {
                preferences.setDailyLimitMinutes(packageName, minutes)
            }
        },
        isAutoStartDismissed = isAutoStartDismissed,
        onSetAutoStartDismissed = {
            coroutineScope.launch {
                preferences.setAutoStartDismissed(true)
            }
        },
        onClearDailyLimit = {
            val packageName = selectedPackageName ?: return@HomeScreen
            coroutineScope.launch {
                preferences.clearDailyLimitMinutes(packageName)
            }
        },
        modifier = modifier,
    )
}

@Composable
fun HomeScreen(
    usageAccessStatus: UsageAccessStatus,
    accessibilityServiceEnabled: Boolean,
    notificationPermissionGranted: Boolean,
    isIgnoringBattery: Boolean,
    installedApps: List<ManagedApp>,
    selectedApp: ManagedApp?,
    isLoadingApps: Boolean,
    isLoadingUsage: Boolean,
    todayUsageMillis: Long,
    dailyLimitMinutes: Int?,
    limitEvaluation: DailyLimitEvaluation?,
    onOpenUsageAccessSettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onOpenAutoStartSettings: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    onSelectApp: (ManagedApp) -> Unit,
    onRefreshUsage: () -> Unit,
    onSaveDailyLimit: (Int) -> Unit,
    isAutoStartDismissed: Boolean,
    onSetAutoStartDismissed: () -> Unit,
    onClearDailyLimit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val usageAccessGranted = usageAccessStatus == UsageAccessStatus.GRANTED
    val statusColor = if (usageAccessGranted) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    var showDiagnosticMenu by remember { mutableStateOf(false) }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val showCardsOnHome = !usageAccessGranted || !accessibilityServiceEnabled || !isAutoStartDismissed || !isIgnoringBattery || !notificationPermissionGranted

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showCardsOnHome) stringResource(R.string.home_title) else stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                androidx.compose.material3.IconButton(onClick = { showDiagnosticMenu = true }) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                        contentDescription = stringResource(R.string.action_diagnostic_settings)
                    )
                }
            }
            if (showCardsOnHome) {
                Text(
                    text = stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (showCardsOnHome) {
                PermissionProcessList(
                    isMenuMode = false,
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

            if (usageAccessGranted) {
                UsageOverviewCard(
                    selectedApp = selectedApp,
                    todayUsageMillis = todayUsageMillis,
                    dailyLimitMinutes = dailyLimitMinutes,
                    limitEvaluation = limitEvaluation,
                    isLoadingUsage = isLoadingUsage,
                    onRefreshUsage = onRefreshUsage,
                )
                AppPickerCard(
                    installedApps = installedApps,
                    selectedApp = selectedApp,
                    isLoadingApps = isLoadingApps,
                    onSelectApp = onSelectApp,
                )
                DailyLimitCard(
                    selectedApp = selectedApp,
                    dailyLimitMinutes = dailyLimitMinutes,
                    onSaveDailyLimit = onSaveDailyLimit,
                    onClearDailyLimit = onClearDailyLimit,
                )
            } else {
                GuidanceCard(
                    title = stringResource(R.string.permission_steps_title),
                    body = stringResource(R.string.permission_steps_body),
                )
            }

            if (showCardsOnHome) {
                GuidanceCard(
                    title = stringResource(R.string.mvp_scope_title),
                    body = stringResource(R.string.mvp_scope_body),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
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
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun PermissionProcessList(
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

        if (isMenuMode || !isAutoStartDismissed) {
            AutoStartCard(
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
            )
            Text(
                text = if (accessibilityServiceEnabled) {
                    stringResource(R.string.accessibility_card_enabled)
                } else {
                    stringResource(R.string.accessibility_card_disabled)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor,
                fontWeight = FontWeight.Medium,
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
            )
            Text(
                text = if (notificationPermissionGranted) {
                    stringResource(R.string.reminder_card_enabled)
                } else {
                    stringResource(R.string.reminder_card_disabled)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor,
                fontWeight = FontWeight.Medium,
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
    dailyLimitMinutes: Int?,
    limitEvaluation: DailyLimitEvaluation?,
    isLoadingUsage: Boolean,
    onRefreshUsage: () -> Unit,
) {
    val budgetColor = when {
        limitEvaluation?.isExceeded == true -> MaterialTheme.colorScheme.error
        limitEvaluation != null -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

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

            if (dailyLimitMinutes != null && limitEvaluation != null) {
                Text(
                    text = stringResource(
                        R.string.daily_limit_summary,
                        dailyLimitMinutes,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (limitEvaluation.isExceeded) {
                        stringResource(
                            R.string.daily_limit_exceeded,
                            formatUsageDuration(limitEvaluation.exceededMillis),
                        )
                    } else {
                        stringResource(
                            R.string.daily_limit_remaining,
                            formatUsageDuration(limitEvaluation.remainingMillis),
                        )
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = budgetColor,
                    fontWeight = FontWeight.Medium,
                )
            } else {
                Text(
                    text = stringResource(R.string.daily_limit_missing),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun DailyLimitCard(
    selectedApp: ManagedApp?,
    dailyLimitMinutes: Int?,
    onSaveDailyLimit: (Int) -> Unit,
    onClearDailyLimit: () -> Unit,
) {
    var sliderMinutes by remember(selectedApp?.packageName, dailyLimitMinutes) {
        mutableFloatStateOf((dailyLimitMinutes ?: 60).coerceIn(5, 240).toFloat())
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
                text = stringResource(R.string.limit_card_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = selectedApp?.let {
                    stringResource(R.string.limit_card_desc_selected, it.appName)
                } ?: stringResource(R.string.limit_card_desc_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    R.string.limit_slider_value,
                    sliderMinutes.toInt(),
                ),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Slider(
                value = sliderMinutes,
                onValueChange = { sliderMinutes = it },
                valueRange = 5f..240f,
                steps = 46,
                enabled = selectedApp != null,
            )

            Button(
                onClick = { onSaveDailyLimit(sliderMinutes.toInt()) },
                enabled = selectedApp != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.limit_save_action))
            }

            OutlinedButton(
                onClick = onClearDailyLimit,
                enabled = selectedApp != null && dailyLimitMinutes != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.limit_clear_action))
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
private fun AutoStartCard(
    onOpenAutoStartSettings: () -> Unit,
    onSetAutoStartDismissed: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.autostart_card_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.autostart_card_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            )
            Text(
                text = if (isIgnoringBattery) {
                    stringResource(R.string.battery_card_enabled)
                } else {
                    stringResource(R.string.battery_card_disabled)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor,
                fontWeight = FontWeight.Medium,
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
private fun formatUsageDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1_000
    val totalMinutes = durationMillis / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val seconds = totalSeconds % 60

    return when {
        hours > 0 && minutes > 0 -> stringResource(
            R.string.duration_hours_minutes,
            hours,
            minutes,
        )
        hours > 0 -> stringResource(R.string.duration_hours_only, hours)
        totalMinutes > 0L -> stringResource(R.string.duration_minutes_only, minutes)
        totalSeconds > 0L -> stringResource(R.string.duration_seconds_only, seconds)
        else -> stringResource(R.string.duration_zero_seconds)
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
            selectedApp = null,
            isLoadingApps = false,
            isLoadingUsage = false,
            todayUsageMillis = 0L,
            notificationPermissionGranted = false,
            isIgnoringBattery = false,
            dailyLimitMinutes = null,
            limitEvaluation = null,
            isAutoStartDismissed = false,
            onOpenUsageAccessSettings = {},
            onOpenAccessibilitySettings = {},
            onRequestNotificationPermission = {},
            onOpenAutoStartSettings = {},
            onRequestBatteryOptimization = {},
            onSetAutoStartDismissed = {},
            onSelectApp = {},
            onRefreshUsage = {},
            onSaveDailyLimit = {},
            onClearDailyLimit = {},
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
            selectedApp = ManagedApp(
                packageName = "com.example.video",
                appName = "Video App",
            ),
            isLoadingApps = false,
            isLoadingUsage = false,
            todayUsageMillis = 5_400_000L,
            notificationPermissionGranted = true,
            isIgnoringBattery = true,
            dailyLimitMinutes = 90,
            limitEvaluation = DailyTimeLimitPolicy().evaluate(
                usageMillis = 5_400_000L,
                limitMillis = 90 * 60_000L,
            ),
            isAutoStartDismissed = false,
            onOpenUsageAccessSettings = {},
            onOpenAccessibilitySettings = {},
            onRequestNotificationPermission = {},
            onOpenAutoStartSettings = {},
            onRequestBatteryOptimization = {},
            onSetAutoStartDismissed = {},
            onSelectApp = {},
            onRefreshUsage = {},
            onSaveDailyLimit = {},
            onClearDailyLimit = {},
        )
    }
}
