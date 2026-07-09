package com.rrrrz.tinyvow.ui.home

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rrrrz.tinyvow.data.db.OfflineFocusAbandonReason
import com.rrrrz.tinyvow.data.db.OfflineFocusMode
import com.rrrrz.tinyvow.data.db.OfflineFocusSessionStatus
import com.rrrrz.tinyvow.data.repository.OfflineFocusSession
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.TinyVowButton
import com.rrrrz.tinyvow.ui.theme.TinyVowButtonTone
import com.rrrrz.tinyvow.ui.theme.TinyVowCard
import com.rrrrz.tinyvow.ui.theme.TinyVowIconSurface
import com.rrrrz.tinyvow.ui.theme.TinyVowMetricTile
import com.rrrrz.tinyvow.ui.theme.TinyVowPageBackground
import com.rrrrz.tinyvow.ui.theme.TinyVowRadius
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
internal fun OfflineFocusCompletionDialog(
    session: OfflineFocusSession,
    restReminderEnabled: Boolean,
    restReminderMinutes: Int,
    restReminderSoundEnabled: Boolean,
    restReminderVibrationEnabled: Boolean,
    restReminderRingtoneUri: String?,
    onDismiss: () -> Unit,
    onSetRestReminderEnabled: (Boolean) -> Unit,
    onSetRestReminderSoundEnabled: (Boolean) -> Unit,
    onSetRestReminderVibrationEnabled: (Boolean) -> Unit,
    onSetRestReminderRingtoneUri: (String?) -> Unit,
    onAdjustEndEarlier: (Int) -> Unit,
    onUserInteraction: () -> Unit,
    onStartAgain: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    val accent = Color(session.colorArgb)
    val endedEarly =
        session.status == OfflineFocusSessionStatus.ABANDONED &&
            session.abandonedReason == OfflineFocusAbandonReason.BELOW_THRESHOLD
    val now by produceState(initialValue = System.currentTimeMillis(), session.id, session.completedAt) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1_000L)
        }
    }
    val endedAt = session.completedAt ?: session.abandonedAt ?: now
    val restMillis = (now - endedAt).coerceAtLeast(0L)

    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
    ) {
        TinyVowPageBackground(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(session.id) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                if (event.changes.any { it.pressed }) {
                                    onUserInteraction()
                                }
                            }
                        }
                    },
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = TinyVowSpacing.PageHorizontal, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = AppText.t("group_cancel"),
                            tint = themeColors.inkMuted,
                        )
                    }
                }

                FocusTypeIcon(
                    iconKey = session.iconKey,
                    customIconPath = session.customIconPath,
                    color = accent,
                    modifier = Modifier.size(86.dp),
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text =
                            AppText.t(
                                if (endedEarly) {
                                    "offline_focus_ended_early_title"
                                } else {
                                    "offline_focus_completed_title"
                                },
                            ),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.inkStrong,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = session.categoryName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                TinyVowCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(TinyVowRadius.FeaturedCard),
                    borderAlpha = 0.34f,
                ) {
                    Column(
                        modifier =
                            Modifier.padding(
                                horizontal = TinyVowSpacing.CardHorizontal,
                                vertical = TinyVowSpacing.CardVertical,
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = AppText.t("offline_focus_rest_elapsed"),
                            style = MaterialTheme.typography.labelLarge,
                            color = themeColors.inkMuted,
                        )
                        Text(
                            text = formatFocusDurationClock(restMillis),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.inkStrong,
                        )
                        Text(
                            text =
                                AppText.t(
                                    if (endedEarly) {
                                        "offline_focus_ended_early_rest_hint"
                                    } else {
                                        "offline_focus_completed_rest_hint"
                                    },
                                ),
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.inkMuted,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                TinyVowCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(TinyVowRadius.Card),
                    borderAlpha = 0.28f,
                ) {
                    Column(
                        modifier =
                            Modifier.padding(
                                horizontal = TinyVowSpacing.CardHorizontal,
                                vertical = TinyVowSpacing.CardVertical,
                            ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            TinyVowMetricTile(
                                label = AppText.t("offline_focus_completed_metric_duration"),
                                value = formatFocusDurationShort(session.actualDurationMillis),
                                color = accent,
                                modifier = Modifier.weight(1f),
                            )
                            TinyVowMetricTile(
                                label = AppText.t("offline_focus_completed_metric_points"),
                                value =
                                    if (endedEarly) {
                                        AppText.t("offline_focus_no_points")
                                    } else {
                                        "+${session.pointsAwarded.roundToInt()}"
                                    },
                                color = themeColors.encourage,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(TinyVowSpacing.CardGap),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            TinyVowMetricTile(
                                label = AppText.t("offline_focus_completed_metric_mode"),
                                value =
                                    when (session.focusMode) {
                                        OfflineFocusMode.NORMAL -> AppText.t("offline_focus_mode_normal")
                                        OfflineFocusMode.STRICT -> AppText.t("offline_focus_mode_strict")
                                    },
                                color = themeColors.base,
                                modifier = Modifier.weight(1f),
                            )
                            TinyVowMetricTile(
                                label = AppText.t("offline_focus_completed_metric_end"),
                                value = formatFocusClockTime(endedAt),
                                color = themeColors.save,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                TinyVowButton(
                    onClick = onStartAgain,
                    tone = TinyVowButtonTone.Primary,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text =
                            AppText.t("offline_focus_start_again"),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun RestReminderStatusRow(
    enabled: Boolean,
    fired: Boolean,
    remainingMillis: Long,
    firedAtMillis: Long,
    onOpenSettings: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenSettings),
        shape = RoundedCornerShape(TinyVowRadius.Control),
        color = themeColors.surfaceSoft,
        border = BorderStroke(1.dp, themeColors.borderSoft),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TinyVowIconSurface(
                icon = Icons.Default.Timer,
                contentDescription = null,
                size = 34.dp,
                iconSize = 18.dp,
                containerColor = themeColors.base.copy(alpha = 0.12f),
                contentColor = themeColors.base,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = AppText.t("offline_focus_rest_reminder_title"),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.inkStrong,
                )
                Text(
                    text =
                        when {
                            !enabled -> AppText.t("offline_focus_rest_reminder_off")
                            fired -> AppText.t("offline_focus_rest_reminder_fired", formatFocusClockTime(firedAtMillis))
                            else -> AppText.t("offline_focus_rest_reminder_countdown", formatFocusDurationClock(remainingMillis))
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.inkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = themeColors.inkMuted,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OfflineFocusRestReminderSettingsSheet(
    enabled: Boolean,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    ringtoneUri: String?,
    onEnabledChange: (Boolean) -> Unit,
    onSoundEnabledChange: (Boolean) -> Unit,
    onVibrationEnabledChange: (Boolean) -> Unit,
    onRingtoneUriChange: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val themeColors = LocalThemeColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showRingtonePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = TinyVowSpacing.PageHorizontal, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TinyVowIconSurface(
                    icon = Icons.Default.Settings,
                    contentDescription = null,
                    containerColor = themeColors.base.copy(alpha = 0.12f),
                    contentColor = themeColors.base,
                )
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = AppText.t("offline_focus_rest_reminder_settings"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.inkStrong,
                    )
                    Text(
                        text = AppText.t("offline_focus_rest_reminder_settings_desc"),
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.inkMuted,
                    )
                }
            }

            TinyVowCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TinyVowRadius.Card),
                borderAlpha = 0.26f,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = TinyVowSpacing.CardHorizontal, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    RestReminderSwitchRow(
                        title = AppText.t("offline_focus_rest_reminder_enable"),
                        subtitle = AppText.t("offline_focus_rest_reminder_enable_desc"),
                        checked = enabled,
                        onCheckedChange = onEnabledChange,
                    )
                    RestReminderSwitchRow(
                        title = AppText.t("offline_focus_rest_reminder_sound"),
                        subtitle = AppText.t("offline_focus_rest_reminder_sound_desc"),
                        checked = soundEnabled,
                        enabled = enabled,
                        onCheckedChange = onSoundEnabledChange,
                    )
                    RestReminderSwitchRow(
                        title = AppText.t("offline_focus_rest_reminder_vibration"),
                        subtitle = AppText.t("offline_focus_rest_reminder_vibration_desc"),
                        checked = vibrationEnabled,
                        enabled = enabled,
                        onCheckedChange = onVibrationEnabledChange,
                    )
                }
            }

            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(enabled = enabled && soundEnabled) {
                            showRingtonePicker = true
                        },
                shape = RoundedCornerShape(TinyVowRadius.Card),
                color = themeColors.surfaceSoft,
                border = BorderStroke(1.dp, themeColors.borderSoft),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = TinyVowSpacing.CardHorizontal, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TinyVowIconSurface(
                        icon = Icons.Default.Timer,
                        contentDescription = null,
                        size = 36.dp,
                        iconSize = 18.dp,
                        containerColor = themeColors.base.copy(alpha = if (enabled && soundEnabled) 0.12f else 0.07f),
                        contentColor = if (enabled && soundEnabled) themeColors.base else themeColors.inkMuted,
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = AppText.t("offline_focus_rest_reminder_ringtone"),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (enabled && soundEnabled) themeColors.inkStrong else themeColors.inkMuted,
                        )
                        Text(
                            text = resolveRingtoneTitle(context, ringtoneUri),
                            style = MaterialTheme.typography.bodySmall,
                            color = themeColors.inkMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            TinyVowButton(
                text = AppText.t("offline_focus_rest_reminder_done"),
                onClick = onDismiss,
                tone = TinyVowButtonTone.Primary,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showRingtonePicker) {
        OfflineFocusRestReminderRingtonePickerSheet(
            currentRingtoneUri = ringtoneUri,
            onDismiss = { showRingtonePicker = false },
            onSelect = { uri ->
                onRingtoneUriChange(uri)
                showRingtonePicker = false
            },
        )
    }
}

@Composable
private fun RestReminderSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    val themeColors = LocalThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (enabled) themeColors.inkStrong else themeColors.inkMuted,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = themeColors.inkMuted,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OfflineFocusEndTimeAdjustSheet(
    session: OfflineFocusSession,
    onDismiss: () -> Unit,
    onApply: (Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val themeColors = LocalThemeColors.current
    val accent = Color(session.colorArgb)
    val endedAt = session.completedAt ?: session.abandonedAt ?: System.currentTimeMillis()
    val maxEarlierMinutes =
        (((endedAt - session.startedAt).coerceAtLeast(0L) / 60_000L) - 1L)
            .coerceAtLeast(0L)
            .coerceAtMost(60L)
            .toInt()
    var selectedMinutes by remember(session.id, endedAt) { mutableIntStateOf(0) }
    val minuteOptions = remember(maxEarlierMinutes) { (0..maxEarlierMinutes).toList() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = TinyVowSpacing.PageHorizontal, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TinyVowIconSurface(
                    icon = Icons.Default.Timer,
                    contentDescription = null,
                    containerColor = accent.copy(alpha = 0.12f),
                    contentColor = accent,
                )
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = AppText.t("offline_focus_adjust_end_time_title"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.inkStrong,
                    )
                    Text(
                        text = AppText.t("offline_focus_adjust_end_time_desc"),
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.inkMuted,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(TinyVowRadius.Card),
                color = themeColors.surfaceSoft,
                border = BorderStroke(1.dp, themeColors.borderSoft),
            ) {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(minuteOptions) { minutes ->
                        val selected = minutes == selectedMinutes
                        Surface(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp)
                                    .clickable { selectedMinutes = minutes },
                            shape = RoundedCornerShape(TinyVowRadius.Control),
                            color = if (selected) accent.copy(alpha = 0.13f) else Color.Transparent,
                            border = if (selected) BorderStroke(1.dp, accent.copy(alpha = 0.28f)) else null,
                        ) {
                            Text(
                                text =
                                    if (minutes == 0) {
                                        AppText.t("offline_focus_adjust_keep_current")
                                    } else {
                                        AppText.t("offline_focus_adjust_minutes_earlier", minutes)
                                    },
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) accent else themeColors.ink,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            TinyVowButton(
                text = AppText.t("offline_focus_apply_end_time_adjustment"),
                onClick = { onApply(selectedMinutes) },
                tone = TinyVowButtonTone.Primary,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun formatFocusDurationClock(millis: Long): String {
    val totalSeconds = (millis / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}

private fun triggerOfflineFocusRestReminder(
    context: Context,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    ringtoneUri: String?,
): Ringtone? {
    if (vibrationEnabled) {
        val vibrator =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(VibratorManager::class.java).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        val pattern = longArrayOf(0L, 160L, 80L, 220L)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, 0)
        }
    }
    if (soundEnabled) {
        val uri = ringtoneUri?.let(Uri::parse) ?: defaultRestReminderRingtoneUri()
        return runCatching {
            RingtoneManager.getRingtone(context, uri)?.also { ringtone ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ringtone.isLooping = true
                }
                ringtone.play()
            }
        }.getOrNull()
    }
    return null
}

private fun stopOfflineFocusRestReminderVibration(context: Context) {
    val vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    runCatching { vibrator.cancel() }
}

internal fun resolveRingtoneTitle(
    context: Context,
    ringtoneUri: String?,
): String {
    val uri = ringtoneUri?.let(Uri::parse) ?: defaultRestReminderRingtoneUri()
    return runCatching {
        RingtoneManager.getRingtone(context, uri)?.getTitle(context)
    }.getOrNull()?.takeIf { it.isNotBlank() } ?: AppText.t("offline_focus_rest_reminder_default_ringtone")
}

private fun formatFocusDurationShort(millis: Long): String {
    val totalMinutes = (millis / 60_000L).coerceAtLeast(0L)
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    return if (hours > 0L) {
        AppText.t("offline_focus_duration_hours_minutes", hours, minutes)
    } else {
        AppText.t("offline_focus_minutes_format", minutes.toInt())
    }
}

private fun formatFocusClockTime(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm"))
