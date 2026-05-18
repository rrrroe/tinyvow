package com.rrrrz.tinyvow.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rrrrz.tinyvow.data.supermode.SuperModeController
import com.rrrrz.tinyvow.data.supermode.SuperModeStatus
import com.rrrrz.tinyvow.i18n.AppText

@Composable
fun SuperModeHomeActionChip(
    status: SuperModeStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent =
        when {
            status.isActive -> MaterialTheme.colorScheme.primary
            status.isConfigured -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.outline
        }
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = if (status.isActive) 0.14f else 0.08f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.16f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = AppText.t("super_mode_title"),
                    tint = accent,
                    modifier = Modifier.size(18.dp),
                )
                if (status.isConfigured) {
                    Surface(
                        modifier =
                            Modifier
                                .align(Alignment.BottomEnd)
                                .size(8.dp),
                        shape = CircleShape,
                        color =
                            if (status.isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
                    ) {}
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = AppText.t("super_mode_title"),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Text(
                    text = describeSuperModeStatus(status),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperModeSettingsSheet(
    status: SuperModeStatus,
    isProActive: Boolean,
    currentTimeLabel: String,
    recoveryQuestion: String?,
    onDismiss: () -> Unit,
    onConfigure: () -> Unit,
    onEnter: () -> Unit,
    onExit: () -> Unit,
    onEditCredentials: () -> Unit,
    onEditWindow: () -> Unit,
    onRecoveryReset: () -> Unit,
    onDisable: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = AppText.t("super_mode_title"),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = AppText.t("super_mode_sheet_summary"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SuperModeStatusRow(
                        label = AppText.t("super_mode_status_label"),
                        value = describeSuperModeStatus(status),
                    )
                    SuperModeStatusRow(
                        label = AppText.t("super_mode_current_time_label"),
                        value = currentTimeLabel,
                    )
                    SuperModeStatusRow(
                        label = AppText.t("super_mode_window_label"),
                        value = status.windowLabel,
                    )
                    SuperModeStatusRow(
                        label = AppText.t("super_mode_auto_exit_label"),
                        value = AppText.t("super_mode_auto_exit_summary"),
                    )
                    if (!recoveryQuestion.isNullOrBlank()) {
                        SuperModeStatusRow(
                            label = AppText.t("super_mode_recovery_question_label"),
                            value = recoveryQuestion,
                        )
                    }
                }
            }

            if (!status.isConfigured) {
                Button(
                    onClick = onConfigure,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(AppText.t("super_mode_configure_action"))
                }
            } else {
                if (status.isActive) {
                    Button(
                        onClick = onExit,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(AppText.t("super_mode_exit_action"))
                    }
                } else {
                    Button(
                        onClick = onEnter,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(AppText.t("super_mode_enter_action"))
                    }
                }

                OutlinedButton(
                    onClick = onEditCredentials,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(AppText.t("super_mode_edit_credentials_action"))
                }

                OutlinedButton(
                    onClick = onEditWindow,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (isProActive) {
                            AppText.t("super_mode_edit_window_action")
                        } else {
                            AppText.t("super_mode_window_pro_only")
                        },
                    )
                }

                OutlinedButton(
                    onClick = onRecoveryReset,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(AppText.t("super_mode_reset_with_recovery_action"))
                }

                TextButton(
                    onClick = onDisable,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = AppText.t("super_mode_disable_action"),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SuperModeStatusRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun SuperModeCredentialDialog(
    initialQuestion: String,
    isEditing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (password: String, recoveryQuestion: String, recoveryAnswer: String) -> Unit,
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var recoveryQuestion by remember(initialQuestion) { mutableStateOf(initialQuestion) }
    var recoveryAnswer by remember { mutableStateOf("") }
    var showValidation by remember { mutableStateOf(false) }

    val passwordTooShort = password.length < 4
    val passwordsMismatch = password != confirmPassword
    val isValid =
        password.isNotBlank() &&
            !passwordTooShort &&
            confirmPassword.isNotBlank() &&
            !passwordsMismatch &&
            recoveryQuestion.trim().isNotBlank() &&
            recoveryAnswer.trim().isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text =
                    if (isEditing) {
                        AppText.t("super_mode_edit_credentials_title")
                    } else {
                        AppText.t("super_mode_configure_title")
                    },
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = AppText.t("super_mode_credentials_hint"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(AppText.t("super_mode_password_label")) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = showValidation && (password.isBlank() || passwordTooShort),
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(AppText.t("super_mode_password_confirm_label")) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = showValidation && (confirmPassword.isBlank() || passwordsMismatch),
                )
                OutlinedTextField(
                    value = recoveryQuestion,
                    onValueChange = { recoveryQuestion = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(AppText.t("super_mode_recovery_question_label")) },
                    isError = showValidation && recoveryQuestion.trim().isBlank(),
                )
                OutlinedTextField(
                    value = recoveryAnswer,
                    onValueChange = { recoveryAnswer = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(AppText.t("super_mode_recovery_answer_label")) },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = showValidation && recoveryAnswer.trim().isBlank(),
                )
                if (showValidation && !isValid) {
                    Text(
                        text =
                            when {
                                passwordTooShort -> AppText.t("super_mode_password_too_short")
                                passwordsMismatch -> AppText.t("super_mode_password_mismatch")
                                else -> AppText.t("super_mode_fill_all_required")
                            },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    showValidation = true
                    if (isValid) {
                        onConfirm(password, recoveryQuestion.trim(), recoveryAnswer.trim())
                    }
                },
            ) {
                Text(
                    if (isEditing) {
                        AppText.t("group_save")
                    } else {
                        AppText.t("super_mode_configure_action")
                    },
                )
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
fun SuperModePasswordDialog(
    title: String,
    message: String,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(AppText.t("super_mode_password_label")) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(password) },
                enabled = password.isNotBlank(),
            ) {
                Text(AppText.t("super_mode_enter_action"))
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
fun SuperModeUnavailableDialog(
    currentTimeLabel: String,
    windowLabel: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppText.t("super_mode_unavailable_title")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = AppText.t("super_mode_unavailable_body"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(AppText.t("super_mode_current_time_value", currentTimeLabel))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(AppText.t("super_mode_allowed_window_value", windowLabel))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(AppText.t("generic_ok"))
            }
        },
    )
}

@Composable
fun SuperModeSetupRequiredDialog(
    actionLabel: String,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppText.t("super_mode_setup_required_title")) },
        text = {
            Text(
                text = AppText.t("super_mode_setup_required_body", actionLabel),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            Button(onClick = onOpenSettings) {
                Text(AppText.t("super_mode_open_settings_action"))
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
fun SuperModeRecoveryResetDialog(
    question: String,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var answer by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppText.t("super_mode_recovery_reset_title")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(AppText.t("super_mode_recovery_answer_label")) },
                    visualTransformation = PasswordVisualTransformation(),
                )
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(answer) },
                enabled = answer.trim().isNotBlank(),
            ) {
                Text(AppText.t("super_mode_reset_action"))
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
fun SuperModeWindowDialog(
    initialStartMinutes: Int,
    initialEndMinutes: Int,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    var startHour by remember(initialStartMinutes) { mutableStateOf((initialStartMinutes / 60).toString()) }
    var startMinute by remember(initialStartMinutes) { mutableStateOf((initialStartMinutes % 60).toString().padStart(2, '0')) }
    var endHour by remember(initialEndMinutes) { mutableStateOf((initialEndMinutes / 60).toString()) }
    var endMinute by remember(initialEndMinutes) { mutableStateOf((initialEndMinutes % 60).toString().padStart(2, '0')) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppText.t("super_mode_window_dialog_title")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = AppText.t("super_mode_window_dialog_body"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TimeInputRow(
                    title = AppText.t("super_mode_window_start_label"),
                    hour = startHour,
                    minute = startMinute,
                    onHourChange = { startHour = it.filter(Char::isDigit).take(2) },
                    onMinuteChange = { startMinute = it.filter(Char::isDigit).take(2) },
                )
                TimeInputRow(
                    title = AppText.t("super_mode_window_end_label"),
                    hour = endHour,
                    minute = endMinute,
                    onHourChange = { endHour = it.filter(Char::isDigit).take(2) },
                    onMinuteChange = { endMinute = it.filter(Char::isDigit).take(2) },
                )
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val startHourValue = startHour.toIntOrNull()
                    val startMinuteValue = startMinute.toIntOrNull()
                    val endHourValue = endHour.toIntOrNull()
                    val endMinuteValue = endMinute.toIntOrNull()
                    if (
                        startHourValue != null &&
                        startMinuteValue != null &&
                        endHourValue != null &&
                        endMinuteValue != null
                    ) {
                        onConfirm(
                            startHourValue * 60 + startMinuteValue,
                            endHourValue * 60 + endMinuteValue,
                        )
                    }
                },
            ) {
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
private fun TimeInputRow(
    title: String,
    hour: String,
    minute: String,
    onHourChange: (String) -> Unit,
    onMinuteChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = hour,
                onValueChange = onHourChange,
                modifier = Modifier.weight(1f),
                label = { Text(AppText.t("super_mode_hour_label")) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
            Text(":")
            OutlinedTextField(
                value = minute,
                onValueChange = onMinuteChange,
                modifier = Modifier.weight(1f),
                label = { Text(AppText.t("super_mode_minute_label")) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
        }
    }
}

fun describeSuperModeStatus(status: SuperModeStatus): String =
    when {
        !status.isConfigured -> AppText.t("super_mode_not_configured")
        status.isActive -> AppText.t("super_mode_active_status", formatDurationMinutes(status.remainingMillis))
        status.isAvailableNow -> AppText.t("super_mode_ready_status")
        else -> AppText.t("super_mode_locked_until_status")
    }

private fun formatDurationMinutes(millis: Long): String {
    val minutes = maxOf(1L, (millis + 59_999L) / 60_000L)
    return AppText.t("super_mode_minutes_value", minutes)
}

fun formatSuperModeTimeLabel(
    controller: SuperModeController,
    minutes: Int,
): String = controller.formatTime(minutes)
