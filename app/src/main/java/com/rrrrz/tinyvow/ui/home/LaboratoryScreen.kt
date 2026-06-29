package com.rrrrz.tinyvow.ui.home

import android.content.ComponentName
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rrrrz.tinyvow.data.db.OfflineFocusAbandonReason
import com.rrrrz.tinyvow.data.db.OfflineFocusMode
import com.rrrrz.tinyvow.data.db.OfflineFocusPauseReason
import com.rrrrz.tinyvow.data.db.OfflineFocusSessionStatus
import com.rrrrz.tinyvow.data.repository.OfflineFocusCategory
import com.rrrrz.tinyvow.data.repository.OfflineFocusDebugSessionInput
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.service.media.XiaoyuzhouPlaybackEventSource
import com.rrrrz.tinyvow.service.media.XiaoyuzhouPlaybackListenerService
import com.rrrrz.tinyvow.service.media.XiaoyuzhouPlaybackMonitor
import com.rrrrz.tinyvow.service.media.XiaoyuzhouPlaybackSnapshot
import com.rrrrz.tinyvow.service.media.XiaoyuzhouPlaybackStatus
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaboratoryScreen(
    onAddPoints: (Double) -> Unit,
    onTriggerAchievementPopupTest: () -> Unit,
    onResetSummary: () -> Unit,
    onTriggerSummary: () -> Unit,
    onTriggerWelcomeIntro: () -> Unit,
    onTriggerCoachmarkTutorial: () -> Unit,
    onTriggerAdvancedCenterTest: () -> Unit,
    onOpenFocusHistoryEditor: () -> Unit,
    showDebugProControls: Boolean,
    onExtendDebugPro: (Int) -> Unit,
    onClearDebugPro: () -> Unit,
    showDebugSuperModeControls: Boolean,
    onEnterSuperMode: () -> Unit,
    onBack: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppText.t("lab_laboratory_debug_tools"),
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(
                    horizontal = TinyVowSpacing.PageHorizontal,
                    vertical = TinyVowSpacing.PageTop,
                )
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                AppText.t("lab_points_simulation"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.inkStrong,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onAddPoints(10.0) }, modifier = Modifier.weight(1f)) {
                    Text(AppText.t("lab_add_points", 10))
                }
                Button(onClick = { onAddPoints(100.0) }, modifier = Modifier.weight(1f)) {
                    Text(AppText.t("lab_add_points", 100))
                }
            }

            HorizontalDivider()

            Text(
                AppText.t("lab_advanced_center_test"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.inkStrong,
            )
            Text(
                AppText.t("lab_advanced_center_test_description"),
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.inkMuted,
            )
            Button(
                onClick = onTriggerAdvancedCenterTest,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(AppText.t("lab_trigger_advanced_center_test"))
            }

            HorizontalDivider()

            Text(
                AppText.t("lab_focus_history_test"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.inkStrong,
            )
            Text(
                AppText.t("lab_focus_history_test_description"),
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.inkMuted,
            )
            Button(
                onClick = onOpenFocusHistoryEditor,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(AppText.t("lab_open_focus_history_editor"))
            }

            HorizontalDivider()

            HomeActivityRingsLabPanel()

            HorizontalDivider()

            XiaoyuzhouPlaybackLabPanel()

            HorizontalDivider()

            BehaviorRingLabPanel()

            HorizontalDivider()

            Text(
                AppText.t("lab_achievement_test"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.inkStrong,
            )
            Text(
                AppText.t("lab_achievement_test_description"),
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.inkMuted,
            )
            Button(
                onClick = onTriggerAchievementPopupTest,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(AppText.t("lab_trigger_achievement_popup"))
            }

            HorizontalDivider()

            Text(
                AppText.t("lab_welcome_intro_test"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.inkStrong,
            )
            Text(
                AppText.t("lab_welcome_intro_test_description"),
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.inkMuted,
            )
            Button(
                onClick = onTriggerWelcomeIntro,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(AppText.t("lab_trigger_welcome_intro"))
            }
            Button(
                onClick = onTriggerCoachmarkTutorial,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(AppText.t("lab_trigger_coachmark_tutorial"))
            }

            if (showDebugProControls) {
                HorizontalDivider()

                Text(
                    AppText.t("lab_pro_debug"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.inkStrong,
                )
                Text(
                    AppText.t("lab_pro_debug_description"),
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.inkMuted,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onExtendDebugPro(1) }, modifier = Modifier.weight(1f)) {
                        Text(AppText.t("lab_add_pro_days", 1))
                    }
                    Button(onClick = { onExtendDebugPro(7) }, modifier = Modifier.weight(1f)) {
                        Text(AppText.t("lab_add_pro_days", 7))
                    }
                    Button(onClick = { onExtendDebugPro(30) }, modifier = Modifier.weight(1f)) {
                        Text(AppText.t("lab_add_pro_days", 30))
                    }
                }
                Button(
                    onClick = onClearDebugPro,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                ) {
                    Text(AppText.t("lab_clear_debug_pro"))
                }
            }

            if (showDebugSuperModeControls) {
                HorizontalDivider()

                Text(
                    AppText.t("lab_super_mode_debug"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.inkStrong,
                )
                Text(
                    AppText.t("lab_super_mode_debug_description"),
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.inkMuted,
                )
                Button(
                    onClick = onEnterSuperMode,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(AppText.t("lab_super_mode_debug_activate"))
                }
            }

            HorizontalDivider()

            Text(
                AppText.t("lab_report_test"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.inkStrong,
            )
            Button(
                onClick = onResetSummary,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
            ) {
                Text(AppText.t("lab_reset_report_state_clear_today_s_record"))
            }

            Button(
                onClick = onTriggerSummary,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(AppText.t("lab_trigger_report_dialog_directly"))
            }

            Text(
                AppText.t("lab_tip_after_resetting_restart_the_app_to_verify"),
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.inkFaint,
            )
        }
    }
}

@Composable
private fun XiaoyuzhouPlaybackLabPanel() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val themeColors = LocalThemeColors.current
    val snapshot by XiaoyuzhouPlaybackMonitor.state.collectAsStateWithLifecycle()
    var listenerEnabled by remember {
        mutableStateOf(XiaoyuzhouPlaybackMonitor.isNotificationListenerEnabled(context))
    }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDisclosure by remember { mutableStateOf(false) }

    fun refresh() {
        listenerEnabled = XiaoyuzhouPlaybackMonitor.isNotificationListenerEnabled(context)
        if (listenerEnabled && !snapshot.listenerConnected) {
            XiaoyuzhouPlaybackMonitor.requestListenerRebind(context)
        }
        XiaoyuzhouPlaybackMonitor.refreshStoredState(context)
        nowMillis = System.currentTimeMillis()
    }

    LaunchedEffect(Unit) {
        refresh()
    }
    LaunchedEffect(snapshot.activeStartedAtMillis) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1_000L)
        }
    }
    DisposableEffect(lifecycleOwner, context) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    refresh()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (showDisclosure) {
        AlertDialog(
            onDismissRequest = { showDisclosure = false },
            title = { Text(AppText.t("lab_xiaoyuzhou_permission_dialog_title")) },
            text = { Text(AppText.t("lab_xiaoyuzhou_permission_dialog_body")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDisclosure = false
                        context.openNotificationListenerSettings(
                            ComponentName(context, XiaoyuzhouPlaybackListenerService::class.java),
                        )
                    },
                ) {
                    Text(AppText.t("lab_xiaoyuzhou_permission_dialog_confirm"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisclosure = false }) {
                    Text(AppText.t("action_cancel"))
                }
            },
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            AppText.t("lab_xiaoyuzhou_monitor_title"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = themeColors.inkStrong,
        )
        Text(
            AppText.t("lab_xiaoyuzhou_monitor_description"),
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.inkMuted,
        )
        XiaoyuzhouMonitorMetricRow(
            label = AppText.t("lab_xiaoyuzhou_permission_status"),
            value =
                if (listenerEnabled) {
                    AppText.t("diagnostics_status_granted")
                } else {
                    AppText.t("diagnostics_status_denied")
                },
            valueColor = if (listenerEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        )
        XiaoyuzhouMonitorMetricRow(
            label = AppText.t("lab_xiaoyuzhou_listener_service"),
            value =
                if (snapshot.listenerConnected) {
                    AppText.t("diagnostics_status_enabled")
                } else {
                    AppText.t("diagnostics_status_disabled")
                },
            valueColor = if (snapshot.listenerConnected) MaterialTheme.colorScheme.primary else themeColors.inkMuted,
        )
        XiaoyuzhouMonitorMetricRow(
            label = AppText.t("lab_xiaoyuzhou_target_package"),
            value = snapshot.targetPackageNames.joinToString(),
        )
        XiaoyuzhouMonitorMetricRow(
            label = AppText.t("lab_xiaoyuzhou_detected_package"),
            value = snapshot.detectedPackageName ?: AppText.t("stats_none"),
        )
        XiaoyuzhouMonitorMetricRow(
            label = AppText.t("lab_xiaoyuzhou_media_session"),
            value =
                if (snapshot.targetSessionVisible) {
                    AppText.t("lab_xiaoyuzhou_session_visible")
                } else {
                    AppText.t("lab_xiaoyuzhou_session_missing")
                },
        )
        XiaoyuzhouMonitorMetricRow(
            label = AppText.t("lab_xiaoyuzhou_notification"),
            value =
                if (snapshot.notificationVisible) {
                    AppText.t("lab_xiaoyuzhou_notification_visible")
                } else {
                    AppText.t("lab_xiaoyuzhou_notification_missing")
                },
        )
        XiaoyuzhouMonitorMetricRow(
            label = AppText.t("lab_xiaoyuzhou_playback_status"),
            value = snapshot.status.label(),
            valueColor =
                if (snapshot.status == XiaoyuzhouPlaybackStatus.PLAYING) {
                    MaterialTheme.colorScheme.primary
                } else {
                    themeColors.inkStrong
                },
        )
        XiaoyuzhouMonitorMetricRow(
            label = AppText.t("lab_xiaoyuzhou_today_playback"),
            value = formatPlaybackMonitorDuration(snapshot.currentTodayMillis(nowMillis)),
            valueColor = MaterialTheme.colorScheme.primary,
        )
        XiaoyuzhouMonitorMetricRow(
            label = AppText.t("lab_xiaoyuzhou_untrusted_playback"),
            value = formatPlaybackMonitorDuration(snapshot.todayUntrustedPlaybackMillis),
            valueColor =
                if (snapshot.todayUntrustedPlaybackMillis > 0L) {
                    MaterialTheme.colorScheme.error
                } else {
                    themeColors.inkStrong
                },
        )
        XiaoyuzhouMonitorMetricRow(
            label = AppText.t("lab_xiaoyuzhou_active_started"),
            value = snapshot.activeStartedAtMillis.formatMonitorTime(),
        )
        XiaoyuzhouMonitorMetricRow(
            label = AppText.t("lab_xiaoyuzhou_last_confirmed"),
            value = snapshot.lastConfirmedAtMillis.formatMonitorTime(),
        )
        XiaoyuzhouMonitorMetricRow(
            label = AppText.t("lab_xiaoyuzhou_media_title"),
            value = snapshot.mediaTitle ?: AppText.t("stats_none"),
        )
        XiaoyuzhouMonitorMetricRow(
            label = AppText.t("lab_xiaoyuzhou_media_subtitle"),
            value = snapshot.mediaSubtitle ?: AppText.t("stats_none"),
        )
        XiaoyuzhouMonitorMetricRow(
            label = AppText.t("lab_xiaoyuzhou_last_event"),
            value =
                AppText.t(
                    "lab_xiaoyuzhou_last_event_value",
                    snapshot.lastEventSource.label(),
                    snapshot.lastEventAtMillis.formatMonitorTime(),
                ),
        )
        Text(
            AppText.t("lab_xiaoyuzhou_monitor_note"),
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.inkFaint,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { showDisclosure = true },
                modifier = Modifier.weight(1f),
            ) {
                Text(AppText.t("lab_xiaoyuzhou_open_notification_listener"))
            }
            OutlinedButton(
                onClick = { refresh() },
                modifier = Modifier.weight(1f),
            ) {
                Text(AppText.t("lab_xiaoyuzhou_refresh"))
            }
        }
        OutlinedButton(
            onClick = {
                XiaoyuzhouPlaybackMonitor.resetToday(context)
                refresh()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(AppText.t("lab_xiaoyuzhou_reset_today"))
        }
    }
}

@Composable
private fun XiaoyuzhouMonitorMetricRow(
    label: String,
    value: String,
    valueColor: Color = LocalThemeColors.current.inkStrong,
) {
    val themeColors = LocalThemeColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.42f),
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.inkMuted,
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.58f),
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun BehaviorRingLabPanel() {
    val themeColors = LocalThemeColors.current
    var totalUsage by remember { mutableStateOf("180") }
    var investment by remember { mutableStateOf("70") }
    var control by remember { mutableStateOf("45") }
    var saved by remember { mutableStateOf("60") }

    fun minutesOf(value: String): Long = value.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
    fun millisOf(value: String): Long = minutesOf(value) * 60_000L
    fun onInputChange(update: (String) -> Unit): (String) -> Unit =
        { value -> update(value.filter { it.isDigit() }.take(4)) }

    val totalUsageMinutes = minutesOf(totalUsage)
    val investmentMinutes = minutesOf(investment)
    val controlMinutes = minutesOf(control)
    val savedMinutes = minutesOf(saved)
    val savedAccent = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            AppText.t("lab_behavior_ring_test"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = themeColors.inkStrong,
        )
        Text(
            AppText.t("lab_behavior_ring_test_description"),
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.inkMuted,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BehaviorRingInputField(
                label = AppText.t("stats_behavior_corner_usage"),
                value = totalUsage,
                onValueChange = onInputChange { totalUsage = it },
                modifier = Modifier.weight(1f),
            )
            BehaviorRingInputField(
                label = AppText.t("stats_behavior_corner_investment"),
                value = investment,
                onValueChange = onInputChange { investment = it },
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BehaviorRingInputField(
                label = AppText.t("stats_behavior_corner_control"),
                value = control,
                onValueChange = onInputChange { control = it },
                modifier = Modifier.weight(1f),
            )
            BehaviorRingInputField(
                label = AppText.t("stats_behavior_corner_savings"),
                value = saved,
                onValueChange = onInputChange { saved = it },
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            text = AppText.t(
                "lab_behavior_ring_ratio",
                totalUsageMinutes,
                investmentMinutes,
                controlMinutes,
                savedMinutes,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.inkFaint,
        )
        BehaviorRadarPanel(
            metrics = behaviorRingLabScoreMetrics(),
            cornerMetrics =
                listOf(
                    BehaviorCornerMetric(
                        label = AppText.t("stats_behavior_corner_usage"),
                        value = totalUsageMinutes.toString(),
                        unit = AppText.t("stats_behavior_unit_minutes_short"),
                        accent = themeColors.base,
                        rawMillis = millisOf(totalUsage),
                        align = Alignment.TopStart,
                    ),
                    BehaviorCornerMetric(
                        label = AppText.t("stats_behavior_corner_investment"),
                        value = investmentMinutes.toString(),
                        unit = AppText.t("stats_behavior_unit_minutes_short"),
                        accent = themeColors.encourage,
                        rawMillis = millisOf(investment),
                        align = Alignment.TopEnd,
                    ),
                    BehaviorCornerMetric(
                        label = AppText.t("stats_behavior_corner_control"),
                        value = controlMinutes.toString(),
                        unit = AppText.t("stats_behavior_unit_minutes_short"),
                        accent = themeColors.control,
                        rawMillis = millisOf(control),
                        align = Alignment.BottomStart,
                    ),
                    BehaviorCornerMetric(
                        label = AppText.t("stats_behavior_corner_savings"),
                        value = savedMinutes.toString(),
                        unit = AppText.t("stats_behavior_unit_minutes_short"),
                        accent = savedAccent,
                        rawMillis = millisOf(saved),
                        align = Alignment.BottomEnd,
                    ),
                ),
            totalMetric =
                BehaviorTotalMetric(
                    label = AppText.t("stats_behavior_total_score"),
                    value = "82",
                    unit = "",
                    accent = themeColors.base,
                ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OfflineFocusHistoryEditorScreen(
    categories: List<OfflineFocusCategory>,
    onCreate: (OfflineFocusDebugSessionInput) -> Unit,
    onBack: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val now = remember { System.currentTimeMillis() }
    val defaultCategory = categories.firstOrNull()
    var selectedCategoryId by remember(categories) { mutableStateOf(defaultCategory?.id) }
    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId } ?: defaultCategory
    var sessionId by remember { mutableStateOf("") }
    var categoryName by remember(selectedCategory?.id) { mutableStateOf(selectedCategory?.name.orEmpty()) }
    var iconKey by remember(selectedCategory?.id) { mutableStateOf(selectedCategory?.iconKey.orEmpty()) }
    var customIconPath by remember(selectedCategory?.id) { mutableStateOf(selectedCategory?.customIconPath.orEmpty()) }
    var colorArgb by remember(selectedCategory?.id) { mutableStateOf((selectedCategory?.colorArgb ?: 0xFFFF6161.toInt()).toString()) }
    var pointsPerMinute by remember(selectedCategory?.id) { mutableStateOf(formatDebugDouble(selectedCategory?.pointsPerMinute ?: 1.0)) }
    var plannedMillis by remember { mutableStateOf("1500000") }
    var actualMillis by remember { mutableStateOf("1500000") }
    var status by remember { mutableStateOf(OfflineFocusSessionStatus.SETTLED) }
    var focusMode by remember { mutableStateOf(OfflineFocusMode.NORMAL) }
    var pauseReason by remember { mutableStateOf<OfflineFocusPauseReason?>(null) }
    var abandonedReason by remember { mutableStateOf<OfflineFocusAbandonReason?>(null) }
    var startedAt by remember { mutableStateOf((now - 1_500_000L).toString()) }
    var pausedAt by remember { mutableStateOf("") }
    var resumedAt by remember { mutableStateOf("") }
    var completedAt by remember { mutableStateOf(now.toString()) }
    var abandonedAt by remember { mutableStateOf("") }
    var violationStartedAt by remember { mutableStateOf("") }
    var violationPackageName by remember { mutableStateOf("") }
    var pointsAwarded by remember { mutableStateOf("25") }
    var createdAt by remember { mutableStateOf((now - 1_500_000L).toString()) }
    var updatedAt by remember { mutableStateOf(now.toString()) }
    var createLedger by remember { mutableStateOf(true) }
    var ledgerDate by remember { mutableStateOf("") }
    var ledgerOccurredAt by remember { mutableStateOf(now.toString()) }

    val input =
        buildDebugFocusInput(
            sessionId = sessionId,
            categoryId = selectedCategoryId,
            categoryName = categoryName,
            iconKey = iconKey,
            customIconPath = customIconPath,
            colorArgb = colorArgb,
            pointsPerMinute = pointsPerMinute,
            plannedMillis = plannedMillis,
            actualMillis = actualMillis,
            status = status,
            focusMode = focusMode,
            pauseReason = pauseReason,
            abandonedReason = abandonedReason,
            startedAt = startedAt,
            pausedAt = pausedAt,
            resumedAt = resumedAt,
            completedAt = completedAt,
            abandonedAt = abandonedAt,
            violationStartedAt = violationStartedAt,
            violationPackageName = violationPackageName,
            pointsAwarded = pointsAwarded,
            createdAt = createdAt,
            updatedAt = updatedAt,
            createLedger = createLedger,
            ledgerDate = ledgerDate,
            ledgerOccurredAt = ledgerOccurredAt,
        )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppText.t("lab_focus_history_editor_title"),
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = TinyVowSpacing.PageHorizontal, vertical = TinyVowSpacing.PageTop),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(AppText.t("lab_focus_history_editor_desc"), style = MaterialTheme.typography.bodySmall, color = themeColors.inkMuted)
            DebugSectionTitle(AppText.t("lab_focus_history_category_section"))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { category ->
                    AssistChip(
                        onClick = {
                            selectedCategoryId = category.id
                            categoryName = category.name
                            iconKey = category.iconKey
                            customIconPath = category.customIconPath.orEmpty()
                            colorArgb = category.colorArgb.toString()
                            pointsPerMinute = formatDebugDouble(category.pointsPerMinute)
                        },
                        label = { Text(category.name) },
                    )
                }
            }
            DebugTextField(AppText.t("lab_focus_history_session_id"), sessionId, { sessionId = it }, AppText.t("lab_focus_history_auto_id_hint"))
            DebugTextField(AppText.t("lab_focus_history_category_name"), categoryName, { categoryName = it })
            DebugTextField(AppText.t("lab_focus_history_icon_key"), iconKey, { iconKey = it })
            DebugTextField(AppText.t("lab_focus_history_custom_icon_path"), customIconPath, { customIconPath = it })
            DebugTextField(AppText.t("lab_focus_history_color_argb"), colorArgb, { colorArgb = signedLongInput(it) })
            DebugTextField(AppText.t("lab_focus_history_points_per_minute"), pointsPerMinute, { pointsPerMinute = decimalInput(it) })

            DebugSectionTitle(AppText.t("lab_focus_history_status_section"))
            DebugEnumChips(OfflineFocusSessionStatus.entries, status, { status = it }) { it.name }
            DebugEnumChips(OfflineFocusMode.entries, focusMode, { focusMode = it }) { it.name }
            DebugNullableEnumChips(AppText.t("lab_focus_history_pause_reason"), OfflineFocusPauseReason.entries, pauseReason, { pauseReason = it }) { it.name }
            DebugNullableEnumChips(AppText.t("lab_focus_history_abandon_reason"), OfflineFocusAbandonReason.entries, abandonedReason, { abandonedReason = it }) { it.name }

            DebugSectionTitle(AppText.t("lab_focus_history_duration_section"))
            DebugTextField(AppText.t("lab_focus_history_planned_millis"), plannedMillis, { plannedMillis = longInput(it) })
            DebugTextField(AppText.t("lab_focus_history_actual_millis"), actualMillis, { actualMillis = longInput(it) })
            DebugTextField(AppText.t("lab_focus_history_points_awarded"), pointsAwarded, { pointsAwarded = signedDecimalInput(it) })

            DebugSectionTitle(AppText.t("lab_focus_history_time_section"))
            DebugTextField(AppText.t("lab_focus_history_started_at"), startedAt, { startedAt = longInput(it) })
            DebugTextField(AppText.t("lab_focus_history_paused_at"), pausedAt, { pausedAt = optionalLongInput(it) })
            DebugTextField(AppText.t("lab_focus_history_resumed_at"), resumedAt, { resumedAt = optionalLongInput(it) })
            DebugTextField(AppText.t("lab_focus_history_completed_at"), completedAt, { completedAt = optionalLongInput(it) })
            DebugTextField(AppText.t("lab_focus_history_abandoned_at"), abandonedAt, { abandonedAt = optionalLongInput(it) })
            DebugTextField(AppText.t("lab_focus_history_violation_started_at"), violationStartedAt, { violationStartedAt = optionalLongInput(it) })
            DebugTextField(AppText.t("lab_focus_history_violation_package"), violationPackageName, { violationPackageName = it })
            DebugTextField(AppText.t("lab_focus_history_created_at"), createdAt, { createdAt = longInput(it) })
            DebugTextField(AppText.t("lab_focus_history_updated_at"), updatedAt, { updatedAt = longInput(it) })

            DebugSectionTitle(AppText.t("lab_focus_history_ledger_section"))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Checkbox(checked = createLedger, onCheckedChange = { createLedger = it })
                Text(AppText.t("lab_focus_history_create_ledger"))
            }
            DebugTextField(AppText.t("lab_focus_history_ledger_date"), ledgerDate, { ledgerDate = it }, AppText.t("lab_focus_history_ledger_date_hint"))
            DebugTextField(AppText.t("lab_focus_history_ledger_occurred_at"), ledgerOccurredAt, { ledgerOccurredAt = optionalLongInput(it) })

            Button(onClick = { input?.let(onCreate) }, enabled = input != null, modifier = Modifier.fillMaxWidth()) {
                Text(AppText.t("lab_focus_history_save"))
            }
            if (input == null) {
                Text(AppText.t("lab_focus_history_invalid"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun DebugSectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = LocalThemeColors.current.inkStrong)
}

@Composable
private fun DebugTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    supportingText: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        supportingText = supportingText?.let { { Text(it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> DebugEnumChips(values: List<T>, selected: T, onSelect: (T) -> Unit, label: (T) -> String) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        values.forEach { value -> AssistChip(onClick = { onSelect(value) }, label = { Text(label(value)) }) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> DebugNullableEnumChips(title: String, values: List<T>, selected: T?, onSelect: (T?) -> Unit, label: (T) -> String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = { onSelect(null) }, label = { Text(AppText.t("lab_focus_history_none")) })
            values.forEach { value -> AssistChip(onClick = { onSelect(value) }, label = { Text(label(value)) }) }
        }
    }
}

private fun buildDebugFocusInput(
    sessionId: String,
    categoryId: String?,
    categoryName: String,
    iconKey: String,
    customIconPath: String,
    colorArgb: String,
    pointsPerMinute: String,
    plannedMillis: String,
    actualMillis: String,
    status: OfflineFocusSessionStatus,
    focusMode: OfflineFocusMode,
    pauseReason: OfflineFocusPauseReason?,
    abandonedReason: OfflineFocusAbandonReason?,
    startedAt: String,
    pausedAt: String,
    resumedAt: String,
    completedAt: String,
    abandonedAt: String,
    violationStartedAt: String,
    violationPackageName: String,
    pointsAwarded: String,
    createdAt: String,
    updatedAt: String,
    createLedger: Boolean,
    ledgerDate: String,
    ledgerOccurredAt: String,
): OfflineFocusDebugSessionInput? {
    fun optional(value: String): Long? = value.trim().takeIf { it.isNotBlank() }?.toLongOrNull()
    fun invalidOptional(value: String): Boolean = value.isNotBlank() && optional(value) == null
    if (invalidOptional(pausedAt) || invalidOptional(resumedAt) || invalidOptional(completedAt) || invalidOptional(abandonedAt) ||
        invalidOptional(violationStartedAt) || invalidOptional(ledgerOccurredAt)
    ) {
        return null
    }
    return OfflineFocusDebugSessionInput(
        sessionId = sessionId.trim().takeIf { it.isNotBlank() },
        categoryId = categoryId ?: return null,
        categoryNameSnapshot = categoryName,
        categoryIconKeySnapshot = iconKey,
        categoryCustomIconPathSnapshot = customIconPath,
        categoryColorArgbSnapshot = colorArgb.toIntOrNull() ?: return null,
        pointsPerMinuteSnapshot = pointsPerMinute.toDoubleOrNull() ?: return null,
        plannedDurationMillis = plannedMillis.toLongOrNull() ?: return null,
        actualDurationMillis = actualMillis.toLongOrNull() ?: return null,
        status = status,
        focusMode = focusMode,
        startedAt = startedAt.toLongOrNull() ?: return null,
        pausedAt = optional(pausedAt),
        resumedAt = optional(resumedAt),
        completedAt = optional(completedAt),
        abandonedAt = optional(abandonedAt),
        pauseReason = pauseReason,
        abandonedReason = abandonedReason,
        violationStartedAt = optional(violationStartedAt),
        violationPackageName = violationPackageName,
        pointsAwarded = pointsAwarded.toDoubleOrNull() ?: return null,
        createdAt = createdAt.toLongOrNull() ?: return null,
        updatedAt = updatedAt.toLongOrNull() ?: return null,
        createLedger = createLedger,
        ledgerDate = ledgerDate,
        ledgerOccurredAt = optional(ledgerOccurredAt),
    )
}

private fun longInput(value: String): String = value.filter { it.isDigit() }.take(16)

private fun optionalLongInput(value: String): String = value.filter { it.isDigit() }.take(16)

private fun signedLongInput(value: String): String {
    val sign = if (value.startsWith("-")) "-" else ""
    return sign + value.filter { it.isDigit() }.take(12)
}

private fun decimalInput(value: String): String {
    val cleaned = buildString {
        var dotSeen = false
        value.forEach { char ->
            when {
                char.isDigit() -> append(char)
                char == '.' && !dotSeen -> {
                    append(char)
                    dotSeen = true
                }
            }
        }
    }
    return cleaned.take(8)
}

private fun signedDecimalInput(value: String): String {
    val sign = if (value.startsWith("-")) "-" else ""
    return sign + decimalInput(value)
}

private fun formatDebugDouble(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')

@Composable
private fun HomeActivityRingsLabPanel() {
    val themeColors = LocalThemeColors.current
    var control by remember { mutableStateOf("128") }
    var encourage by remember { mutableStateOf("84") }
    var growth by remember { mutableStateOf("116") }

    fun percentOf(value: String): Float =
        (value.toFloatOrNull() ?: 0f).coerceAtLeast(0f) / 100f

    fun onInputChange(update: (String) -> Unit): (String) -> Unit =
        { value -> update(value.filter { it.isDigit() }.take(4)) }

    val rings =
        HomeActivityRingsUiState(
            controlProgress = percentOf(control),
            encourageProgress = percentOf(encourage),
            growthProgress = percentOf(growth),
            controlAvailable = true,
            encourageAvailable = true,
            growthAvailable = true,
            growthTargetPoints = 100.0,
        )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            AppText.t("lab_home_activity_rings_test"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = themeColors.inkStrong,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            AppText.t("lab_home_activity_rings_test_description"),
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.inkMuted,
            modifier = Modifier.fillMaxWidth(),
        )
        HomeActivityRingsDial(
            rings = rings,
            outerColor = themeColors.control,
            middleColor = themeColors.encourage,
            innerColor = themeColors.base,
            replayToken = 0,
            revealProgress = 1f,
            onClick = {},
            modifier = Modifier.size(168.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HomeActivityRingInputField(
                label = AppText.t("home_activity_ring_control_label"),
                value = control,
                onValueChange = onInputChange { control = it },
                modifier = Modifier.weight(1f),
            )
            HomeActivityRingInputField(
                label = AppText.t("home_activity_ring_encourage_label"),
                value = encourage,
                onValueChange = onInputChange { encourage = it },
                modifier = Modifier.weight(1f),
            )
            HomeActivityRingInputField(
                label = AppText.t("home_activity_ring_growth_label"),
                value = growth,
                onValueChange = onInputChange { growth = it },
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            text = AppText.t(
                "lab_home_activity_rings_value",
                control.ifBlank { "0" }.toIntOrNull() ?: 0,
                encourage.ifBlank { "0" }.toIntOrNull() ?: 0,
                growth.ifBlank { "0" }.toIntOrNull() ?: 0,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = themeColors.inkFaint,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun HomeActivityRingInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        label = { Text(label) },
        suffix = { Text("%") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
private fun BehaviorRingInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        singleLine = true,
        label = { Text(label) },
        suffix = { Text(AppText.t("stats_behavior_unit_minutes")) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

private fun behaviorRingLabScoreMetrics(): List<DailyBehaviorScoreMetric> =
    listOf(
        DailyBehaviorScoreMetric(AppText.t("stats_score_kept_vow"), 82, "", 0),
        DailyBehaviorScoreMetric(AppText.t("stats_score_gains"), 76, "", 1),
        DailyBehaviorScoreMetric(AppText.t("stats_score_focus"), 88, "", 2),
        DailyBehaviorScoreMetric(AppText.t("stats_score_rhythm"), 70, "", 3),
        DailyBehaviorScoreMetric(AppText.t("stats_score_restraint"), 80, "", 4),
    )

private fun XiaoyuzhouPlaybackSnapshot.currentTodayMillis(nowMillis: Long): Long =
    todayAccumulatedPlaybackMillis +
        if (listenerConnected && status == XiaoyuzhouPlaybackStatus.PLAYING) {
            lastConfirmedAtMillis
                ?.let { (nowMillis - it).coerceIn(0L, XiaoyuzhouPlaybackMonitor.TRUSTED_RECONNECT_WINDOW_MILLIS) }
                .orEmpty()
        } else {
            0L
        }

private fun Long?.formatMonitorTime(): String =
    this
        ?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("MM-dd HH:mm:ss", Locale.CHINA))
        }
        ?: AppText.t("stats_none")

private fun XiaoyuzhouPlaybackStatus.label(): String =
    when (this) {
        XiaoyuzhouPlaybackStatus.UNKNOWN -> AppText.t("lab_xiaoyuzhou_status_unknown")
        XiaoyuzhouPlaybackStatus.PLAYING -> AppText.t("lab_xiaoyuzhou_status_playing")
        XiaoyuzhouPlaybackStatus.PAUSED -> AppText.t("lab_xiaoyuzhou_status_paused")
        XiaoyuzhouPlaybackStatus.STOPPED -> AppText.t("lab_xiaoyuzhou_status_stopped")
        XiaoyuzhouPlaybackStatus.BUFFERING -> AppText.t("lab_xiaoyuzhou_status_buffering")
    }

private fun XiaoyuzhouPlaybackEventSource.label(): String =
    when (this) {
        XiaoyuzhouPlaybackEventSource.SERVICE -> AppText.t("lab_xiaoyuzhou_event_service")
        XiaoyuzhouPlaybackEventSource.MEDIA_SESSION -> AppText.t("lab_xiaoyuzhou_event_media_session")
        XiaoyuzhouPlaybackEventSource.NOTIFICATION -> AppText.t("lab_xiaoyuzhou_event_notification")
    }

private fun formatPlaybackMonitorDuration(durationMillis: Long): String {
    val totalSeconds = (durationMillis / 1_000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L
    return when {
        hours > 0L -> AppText.t("lab_xiaoyuzhou_duration_hms", hours, minutes, seconds)
        minutes > 0L -> AppText.t("lab_xiaoyuzhou_duration_ms", minutes, seconds)
        else -> AppText.t("lab_xiaoyuzhou_duration_s", seconds)
    }
}

private fun Long?.orEmpty(): Long = this ?: 0L

