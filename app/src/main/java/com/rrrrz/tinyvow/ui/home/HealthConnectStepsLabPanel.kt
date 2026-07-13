package com.rrrrz.tinyvow.ui.home

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.records.metadata.Metadata
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.rrrrz.tinyvow.data.steps.HealthConnectStepProbe
import com.rrrrz.tinyvow.data.steps.HealthConnectStepProbeCacheWarning
import com.rrrrz.tinyvow.data.steps.HealthConnectStepProbeSnapshot
import com.rrrrz.tinyvow.data.steps.HealthConnectStepProbeStatus
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.TinyVowButton
import com.rrrrz.tinyvow.ui.theme.TinyVowButtonTone
import com.rrrrz.tinyvow.ui.theme.TinyVowCard
import com.rrrrz.tinyvow.ui.theme.TinyVowCardContent
import com.rrrrz.tinyvow.ui.theme.TinyVowSection
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import com.rrrrz.tinyvow.ui.theme.TinyVowStatusPill
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
internal fun HealthConnectStepsLabPanel() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val probe = remember(context) { HealthConnectStepProbe(context) }
    val themeColors = LocalThemeColors.current
    var available by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }
    var snapshot by remember { mutableStateOf<HealthConnectStepProbeSnapshot?>(null) }
    var retainedDays by remember { mutableStateOf(0) }
    var cacheWarning by remember { mutableStateOf<HealthConnectStepProbeCacheWarning?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    suspend fun reload(readToday: Boolean) {
        isLoading = true
        errorMessage = null
        try {
            available = probe.isAvailable()
            val retained = probe.loadRetainedHistory()
            retainedDays = retained.retainedHistory.size
            cacheWarning = retained.cacheWarning
            val today = LocalDate.now(ZoneId.systemDefault()).toString()
            snapshot = retained.retainedHistory.firstOrNull { it.date == today }
            permissionGranted = available && probe.hasReadStepsPermission()
            if (readToday && permissionGranted) {
                val result = probe.readToday()
                snapshot = result.snapshot
                retainedDays = result.retainedHistory.size
                cacheWarning = result.cacheWarning
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            errorMessage = error.message ?: error::class.java.simpleName
        } finally {
            isLoading = false
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = PermissionController.createRequestPermissionResultContract(),
        ) { grantedPermissions ->
            coroutineScope.launch {
                reload(readToday = true)
                if (!grantedPermissions.containsAll(HealthConnectStepProbe.STEP_PERMISSIONS)) {
                    errorMessage = AppText.t("lab_health_steps_permission_not_granted")
                }
            }
        }

    LaunchedEffect(probe) {
        reload(readToday = true)
    }

    DisposableEffect(lifecycleOwner, probe) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    coroutineScope.launch { reload(readToday = true) }
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val statusText =
        when {
            isLoading -> AppText.t("lab_health_steps_reading")
            errorMessage != null -> AppText.t("lab_health_steps_status_error")
            !available -> AppText.t("lab_health_steps_status_unavailable")
            !permissionGranted -> AppText.t("lab_health_steps_status_permission_required")
            snapshot == null -> AppText.t("lab_health_steps_status_not_queried")
            snapshot?.status == HealthConnectStepProbeStatus.XIAOMI_SOURCE_FOUND ->
                AppText.t("lab_health_steps_status_xiaomi_found")
            snapshot?.status == HealthConnectStepProbeStatus.XIAOMI_SOURCE_DIAGNOSTICS_INCOMPLETE ->
                AppText.t("lab_health_steps_status_diagnostics_incomplete")
            snapshot?.status == HealthConnectStepProbeStatus.XIAOMI_SOURCE_WITH_MANUAL_RECORDS ->
                AppText.t("lab_health_steps_status_manual_records")
            snapshot?.status == HealthConnectStepProbeStatus.MULTIPLE_XIAOMI_SOURCES ->
                AppText.t("lab_health_steps_status_multiple_sources")
            snapshot?.status == HealthConnectStepProbeStatus.SYSTEM_DATA_ONLY ->
                AppText.t("lab_health_steps_status_system_only")
            else -> AppText.t("lab_health_steps_status_no_records")
        }
    val statusColor =
        when {
            errorMessage != null || !available -> themeColors.restraint
            snapshot?.status == HealthConnectStepProbeStatus.XIAOMI_SOURCE_FOUND -> themeColors.save
            else -> themeColors.base
        }

    TinyVowSection(
        title = AppText.t("lab_health_steps_title"),
        subtitle = AppText.t("lab_health_steps_description"),
        icon = Icons.AutoMirrored.Filled.DirectionsWalk,
    ) {
        TinyVowCard(modifier = Modifier.fillMaxWidth()) {
            TinyVowCardContent {
                TinyVowStatusPill(text = statusText, color = statusColor)

                HealthConnectProbeMetricRow(
                    label = AppText.t("lab_health_steps_health_connect_status"),
                    value =
                        if (available) {
                            AppText.t("diagnostics_status_enabled")
                        } else {
                            AppText.t("diagnostics_status_disabled")
                        },
                )
                HealthConnectProbeMetricRow(
                    label = AppText.t("lab_health_steps_permission_status"),
                    value =
                        if (permissionGranted) {
                            AppText.t("diagnostics_status_granted")
                        } else {
                            AppText.t("diagnostics_status_denied")
                        },
                )
                HealthConnectProbeMetricRow(
                    label = AppText.t("lab_health_steps_xiaomi_total"),
                    value = snapshot?.xiaomiSteps.formatStepsOrDash(),
                )
                HealthConnectProbeMetricRow(
                    label = AppText.t("lab_health_steps_system_total"),
                    value = snapshot?.systemSteps.formatStepsOrDash(),
                )
                HealthConnectProbeMetricRow(
                    label = AppText.t("lab_health_steps_selected_source"),
                    value = snapshot?.sourcePackage ?: AppText.t("lab_health_steps_source_none"),
                )
                HealthConnectProbeMetricRow(
                    label = AppText.t("lab_health_steps_source_totals"),
                    value = snapshot.xiaomiSourceTotalsLabel(),
                )
                HealthConnectProbeMetricRow(
                    label = AppText.t("lab_health_steps_origins"),
                    value =
                        snapshot?.originPackages
                            ?.takeIf { it.isNotEmpty() }
                            ?.joinToString(separator = "\n")
                            ?: AppText.t("lab_health_steps_source_none"),
                )
                HealthConnectProbeMetricRow(
                    label = AppText.t("lab_health_steps_raw_records"),
                    value =
                        snapshot?.let {
                            AppText.t(
                                "lab_health_steps_raw_records_value",
                                it.xiaomiRecordCount,
                                it.rawRecordCount,
                                if (it.rawRecordsComplete) {
                                    AppText.t("lab_health_steps_records_complete")
                                } else {
                                    AppText.t("lab_health_steps_records_truncated")
                                },
                            )
                        } ?: AppText.t("lab_health_steps_not_available_value"),
                )
                HealthConnectProbeMetricRow(
                    label = AppText.t("lab_health_steps_recording_methods"),
                    value = snapshot.recordingMethodsLabel(),
                )
                HealthConnectProbeMetricRow(
                    label = AppText.t("lab_health_steps_latest_record_end"),
                    value = snapshot?.latestXiaomiRecordEndMillis.formatDateTimeOrDash(),
                )
                HealthConnectProbeMetricRow(
                    label = AppText.t("lab_health_steps_latest_modified"),
                    value = snapshot?.latestXiaomiModifiedMillis.formatDateTimeOrDash(),
                )
                Text(
                    text = AppText.t("lab_health_steps_sync_note"),
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.inkMuted,
                )
                HealthConnectProbeMetricRow(
                    label = AppText.t("lab_health_steps_query_window"),
                    value = snapshot.queryWindowLabel(),
                )
                HealthConnectProbeMetricRow(
                    label = AppText.t("lab_health_steps_extension_version"),
                    value =
                        snapshot?.uExtensionVersion?.toString()
                            ?: AppText.t("lab_health_steps_not_available_value"),
                )
                HealthConnectProbeMetricRow(
                    label = AppText.t("lab_health_steps_last_query"),
                    value = snapshot?.fetchedAtMillis.formatDateTimeOrDash(),
                )
                HealthConnectProbeMetricRow(
                    label = AppText.t("lab_health_steps_cache"),
                    value = AppText.t("lab_health_steps_cache_value", retainedDays),
                )

                errorMessage?.let { message ->
                    Text(
                        text = AppText.t("lab_health_steps_failed", message),
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.restraint,
                    )
                }
                cacheWarning?.let { warning ->
                    Text(
                        text =
                            when (warning) {
                                HealthConnectStepProbeCacheWarning.CORRUPT_RESET ->
                                    AppText.t("lab_health_steps_cache_corrupt_reset")
                                HealthConnectStepProbeCacheWarning.WRITE_FAILED ->
                                    AppText.t("lab_health_steps_cache_write_failed")
                            },
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.restraint,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
                ) {
                    TinyVowButton(
                        onClick = {
                            if (permissionGranted) {
                                coroutineScope.launch { reload(readToday = true) }
                            } else {
                                try {
                                    permissionLauncher.launch(HealthConnectStepProbe.STEP_PERMISSIONS)
                                } catch (error: Exception) {
                                    errorMessage =
                                        AppText.t(
                                            "lab_health_steps_permission_launch_failed",
                                            error.message ?: error::class.java.simpleName,
                                        )
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = available && !isLoading,
                        tone = TinyVowButtonTone.Primary,
                    ) {
                        Text(
                            if (permissionGranted) {
                                AppText.t("lab_health_steps_refresh")
                            } else {
                                AppText.t("lab_health_steps_request_permission")
                            },
                        )
                    }
                    TinyVowButton(
                        onClick = {
                            try {
                                context.startActivity(
                                    Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                )
                            } catch (error: Exception) {
                                errorMessage = error.message ?: error::class.java.simpleName
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                    ) {
                        Text(AppText.t("lab_health_steps_open_settings"))
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthConnectProbeMetricRow(
    label: String,
    value: String,
) {
    val themeColors = LocalThemeColors.current
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = TinyVowSpacing.CardGap / 4f),
        horizontalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
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
            fontWeight = FontWeight.Medium,
            color = themeColors.inkStrong,
            textAlign = TextAlign.End,
        )
    }
}

private fun Long?.formatStepsOrDash(): String =
    this?.let {
        AppText.t("lab_health_steps_value", NumberFormat.getIntegerInstance().format(it))
    } ?: AppText.t("lab_health_steps_not_available_value")

private fun Long?.formatDateTimeOrDash(): String =
    this?.let {
        DATE_TIME_FORMATTER.withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(it))
    } ?: AppText.t("lab_health_steps_not_available_value")

private fun HealthConnectStepProbeSnapshot?.queryWindowLabel(): String =
    this?.let {
        AppText.t(
            "lab_health_steps_query_window_value",
            queryStartMillis.formatDateTimeOrDash(),
            queryEndMillis.formatDateTimeOrDash(),
        )
    } ?: AppText.t("lab_health_steps_not_available_value")

private fun HealthConnectStepProbeSnapshot?.recordingMethodsLabel(): String {
    val methods = this?.recordingMethods.orEmpty()
    if (methods.isEmpty()) return AppText.t("lab_health_steps_not_available_value")
    return methods.joinToString { method ->
        when (method) {
            Metadata.RECORDING_METHOD_ACTIVELY_RECORDED ->
                AppText.t("lab_health_steps_method_active")
            Metadata.RECORDING_METHOD_AUTOMATICALLY_RECORDED ->
                AppText.t("lab_health_steps_method_automatic")
            Metadata.RECORDING_METHOD_MANUAL_ENTRY ->
                AppText.t("lab_health_steps_method_manual")
            else -> AppText.t("lab_health_steps_method_unknown")
        }
    }
}

private fun HealthConnectStepProbeSnapshot?.xiaomiSourceTotalsLabel(): String {
    val totals = this?.xiaomiSourceTotals.orEmpty()
    if (totals.isEmpty()) return AppText.t("lab_health_steps_not_available_value")
    return totals.entries.joinToString(separator = "\n") { (packageName, steps) ->
        AppText.t(
            "lab_health_steps_source_total_value",
            packageName,
            NumberFormat.getIntegerInstance().format(steps),
        )
    }
}

private val DATE_TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
