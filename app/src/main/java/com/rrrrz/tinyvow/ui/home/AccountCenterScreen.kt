package com.rrrrz.tinyvow.ui.home

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.rrrrz.tinyvow.data.account.BackendAccount
import com.rrrrz.tinyvow.data.account.BackendLoginEvent
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.TinyVowButton
import com.rrrrz.tinyvow.ui.theme.TinyVowButtonTone
import com.rrrrz.tinyvow.ui.theme.TinyVowCard
import com.rrrrz.tinyvow.ui.theme.TinyVowDetailScaffold
import com.rrrrz.tinyvow.ui.theme.TinyVowEmptyState
import com.rrrrz.tinyvow.ui.theme.TinyVowIconSurface
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import java.text.DateFormat
import java.util.Date

@Composable
fun AccountCenterScreen(
    account: BackendAccount?,
    profileDisplayName: String?,
    profileAvatarUri: String?,
    isBusy: Boolean,
    onBack: () -> Unit,
    onRegister: (email: String, password: String, displayName: String) -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onRequestEmailVerification: () -> Unit,
    onConfirmEmailVerification: (code: String) -> Unit,
    onRequestPasswordReset: (email: String) -> Unit,
    onConfirmPasswordReset: (email: String, code: String, newPassword: String) -> Unit,
    onUpdateProfileName: (String) -> Unit,
    onUpdateProfileAvatar: (String) -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    val context = LocalContext.current
    val effectiveDisplayName =
        account?.displayName?.takeIf { it.isNotBlank() }
            ?: profileDisplayName?.takeIf { it.isNotBlank() }
            ?: AppText.t("me_local_account_default_name")
    val effectiveAvatar = account?.avatarUrl ?: profileAvatarUri
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
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

    TinyVowDetailScaffold(
        title = AppText.t("me_account_center"),
        onBack = onBack,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = TinyVowSpacing.PageHorizontal,
                    vertical = TinyVowSpacing.PageTop,
                ),
            verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.SectionGap),
        ) {
            Text(
                text = AppText.t("account_center_description"),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalThemeColors.current.inkMuted,
            )
            AccountProfileCard(
                displayName = effectiveDisplayName,
                avatar = effectiveAvatar,
                isBusy = isBusy,
                onPickAvatar = {
                    avatarPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                onUpdateProfileName = onUpdateProfileName,
            )
            when {
                account == null -> {
                    TinyVowEmptyState(
                        title = AppText.t("account_loading_title"),
                        body = AppText.t("account_loading_description"),
                    )
                }
                !account.isRegistered -> {
                    AccountAccessCard(
                        isBusy = isBusy,
                        onRegister = onRegister,
                        onLogin = onLogin,
                        onRequestPasswordReset = onRequestPasswordReset,
                        onConfirmPasswordReset = onConfirmPasswordReset,
                    )
                    TinyVowCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = AppText.t("account_local_first_notice"),
                            modifier = Modifier.padding(TinyVowSpacing.CardHorizontal),
                            style = MaterialTheme.typography.bodySmall,
                            color = LocalThemeColors.current.inkMuted,
                        )
                    }
                }
                else -> {
                    RegisteredAccountContent(
                        account = account,
                        isBusy = isBusy,
                        onRequestEmailVerification = onRequestEmailVerification,
                        onConfirmEmailVerification = onConfirmEmailVerification,
                        onSignOut = onSignOut,
                        onDeleteAccount = onDeleteAccount,
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountProfileCard(
    displayName: String,
    avatar: String?,
    isBusy: Boolean,
    onPickAvatar: () -> Unit,
    onUpdateProfileName: (String) -> Unit,
) {
    var showNameEditor by remember { mutableStateOf(false) }
    var draftName by remember(displayName) { mutableStateOf(displayName) }

    TinyVowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TinyVowSpacing.CardHorizontal),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProfileAvatar(
                avatar = avatar,
                contentDescription = AppText.t("me_profile_choose_avatar"),
                size = 64.dp,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = !isBusy, onClick = onPickAvatar),
            )
            Text(
                text = displayName,
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = !isBusy) {
                        draftName = displayName
                        showNameEditor = true
                    }
                    .padding(vertical = 18.dp),
                style = MaterialTheme.typography.titleMedium,
                color = LocalThemeColors.current.inkStrong,
            )
        }
    }

    if (showNameEditor) {
        AlertDialog(
            onDismissRequest = { showNameEditor = false },
            title = { Text(AppText.t("me_profile_name")) },
            text = {
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it.take(40) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(AppText.t("me_profile_name")) },
                    placeholder = { Text(AppText.t("me_profile_name_placeholder")) },
                )
            },
            confirmButton = {
                TextButton(
                    enabled = draftName.trim().isNotEmpty(),
                    onClick = {
                        onUpdateProfileName(draftName.trim())
                        showNameEditor = false
                    },
                ) {
                    Text(AppText.t("group_save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameEditor = false }) {
                    Text(AppText.t("group_cancel"))
                }
            },
        )
    }
}

@Composable
private fun AccountAccessCard(
    isBusy: Boolean,
    onRegister: (String, String, String) -> Unit,
    onLogin: (String, String) -> Unit,
    onRequestPasswordReset: (String) -> Unit,
    onConfirmPasswordReset: (String, String, String) -> Unit,
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var isResetMode by remember { mutableStateOf(false) }
    var resetCodeSent by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var resetCode by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }

    TinyVowCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(TinyVowSpacing.CardHorizontal),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text =
                        if (isResetMode) {
                            AppText.t("account_reset_password_title")
                        } else if (isRegisterMode) {
                            AppText.t("account_register_title")
                        } else {
                            AppText.t("account_login_title")
                        },
                    style = MaterialTheme.typography.titleMedium,
                    color = LocalThemeColors.current.inkStrong,
                )
                TextButton(
                    enabled = !isBusy,
                    onClick = {
                        if (isResetMode) {
                            isResetMode = false
                            resetCodeSent = false
                        } else {
                            isRegisterMode = !isRegisterMode
                        }
                    },
                ) {
                    Text(
                        if (isResetMode || isRegisterMode) {
                            AppText.t("account_have_account")
                        } else {
                            AppText.t("account_create_account")
                        },
                    )
                }
            }
            if (isRegisterMode && !isResetMode) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it.take(40) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isBusy,
                    singleLine = true,
                    label = { Text(AppText.t("me_profile_name")) },
                )
            }
            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim().take(160) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBusy,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                label = { Text(AppText.t("account_email")) },
            )
            if (!isResetMode || resetCodeSent) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it.take(72) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isBusy,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    label = {
                        Text(
                            if (isResetMode) {
                                AppText.t("account_new_password")
                            } else {
                                AppText.t("account_password")
                            },
                        )
                    },
                    supportingText = {
                        if (isRegisterMode || (isResetMode && resetCodeSent)) {
                            Text(AppText.t("account_password_hint"))
                        }
                    },
                )
            }
            if ((isRegisterMode && !isResetMode) || (isResetMode && resetCodeSent)) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it.take(72) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isBusy,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    label = { Text(AppText.t("account_confirm_password")) },
                    isError = confirmPassword.isNotEmpty() && confirmPassword != password,
                    supportingText = {
                        if (confirmPassword.isNotEmpty() && confirmPassword != password) {
                            Text(AppText.t("account_password_mismatch"))
                        }
                    },
                )
            }
            if (isResetMode && resetCodeSent) {
                OutlinedTextField(
                    value = resetCode,
                    onValueChange = { resetCode = it.filter(Char::isDigit).take(6) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isBusy,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text(AppText.t("account_email_code")) },
                )
                TinyVowButton(
                    text = AppText.t("account_reset_password_action"),
                    enabled =
                        !isBusy &&
                            resetCode.length == 6 &&
                            password.length >= 8 &&
                            confirmPassword == password,
                    onClick = {
                        onConfirmPasswordReset(email, resetCode, password)
                    },
                    tone = TinyVowButtonTone.Primary,
                )
                TextButton(
                    enabled = !isBusy,
                    onClick = { onRequestPasswordReset(email) },
                ) {
                    Text(AppText.t("account_resend_code"))
                }
            } else {
                TinyVowButton(
                    text =
                        when {
                            isResetMode -> AppText.t("account_send_reset_code")
                            isRegisterMode -> AppText.t("account_register_action")
                            else -> AppText.t("account_login_action")
                        },
                    enabled =
                        !isBusy &&
                            email.isNotBlank() &&
                            (
                                isResetMode ||
                                    (
                                        password.length >= 8 &&
                                            (
                                                !isRegisterMode ||
                                                    (
                                                        displayName.isNotBlank() &&
                                                            confirmPassword == password
                                                        )
                                                )
                                        )
                                ),
                    onClick = {
                        when {
                            isResetMode -> {
                                onRequestPasswordReset(email)
                                resetCodeSent = true
                            }
                            isRegisterMode -> onRegister(email, password, displayName)
                            else -> onLogin(email, password)
                        }
                    },
                    tone = TinyVowButtonTone.Primary,
                )
            }
            if (!isRegisterMode && !isResetMode) {
                TextButton(
                    enabled = !isBusy,
                    onClick = {
                        isResetMode = true
                        password = ""
                        confirmPassword = ""
                    },
                ) {
                    Text(AppText.t("account_forgot_password"))
                }
            }
        }
    }
}

@Composable
private fun RegisteredAccountContent(
    account: BackendAccount,
    isBusy: Boolean,
    onRequestEmailVerification: () -> Unit,
    onConfirmEmailVerification: (String) -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    TinyVowCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(TinyVowSpacing.CardHorizontal),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AccountInfoRow(
                icon = { TinyVowIconSurface(Icons.Default.VerifiedUser, null) },
                label = AppText.t("account_email"),
                value = account.email.orEmpty(),
            )
            AccountValueRow(
                label = AppText.t("account_email_status"),
                value =
                    if (account.emailVerified) {
                        AppText.t("account_email_verified")
                    } else {
                        AppText.t("account_email_unverified")
                    },
            )
            AccountInfoRow(
                icon = { TinyVowIconSurface(Icons.Default.History, null) },
                label = AppText.t("account_registered_at"),
                value = formatAccountDate(account.registeredAtMillis ?: account.createdAtMillis),
            )
            AccountInfoRow(
                icon = { TinyVowIconSurface(Icons.Default.Devices, null) },
                label = AppText.t("account_last_login"),
                value = account.lastLoginAtMillis?.let(::formatAccountDate)
                    ?: AppText.t("account_no_login_record"),
            )
        }
    }

    if (!account.emailVerified) {
        EmailVerificationCard(
            email = account.email.orEmpty(),
            isBusy = isBusy,
            onRequestCode = onRequestEmailVerification,
            onConfirmCode = onConfirmEmailVerification,
        )
    }

    TinyVowCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(TinyVowSpacing.CardHorizontal),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = AppText.t("account_recent_logins"),
                style = MaterialTheme.typography.titleMedium,
                color = LocalThemeColors.current.inkStrong,
            )
            if (account.recentLogins.isEmpty()) {
                Text(
                    text = AppText.t("account_no_login_record"),
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalThemeColors.current.inkMuted,
                )
            } else {
                account.recentLogins.forEach { event ->
                    RecentLoginRow(event)
                }
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TinyVowButton(
            text = AppText.t("me_sign_out"),
            enabled = !isBusy,
            onClick = onSignOut,
        )
        TinyVowButton(
            text = AppText.t("me_delete_account"),
            enabled = !isBusy,
            onClick = { showDeleteConfirm = true },
            tone = TinyVowButtonTone.Danger,
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(AppText.t("me_delete_account")) },
            text = { Text(AppText.t("account_delete_server_data_notice")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteAccount()
                    },
                ) {
                    Text(
                        text = AppText.t("me_delete_account"),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(AppText.t("group_cancel"))
                }
            },
        )
    }
}

@Composable
private fun EmailVerificationCard(
    email: String,
    isBusy: Boolean,
    onRequestCode: () -> Unit,
    onConfirmCode: (String) -> Unit,
) {
    var code by remember { mutableStateOf("") }
    var codeRequested by remember { mutableStateOf(false) }
    TinyVowCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(TinyVowSpacing.CardHorizontal),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = AppText.t("account_verify_email_title"),
                style = MaterialTheme.typography.titleMedium,
                color = LocalThemeColors.current.inkStrong,
            )
            Text(
                text = AppText.t("account_verify_email_description", email),
                style = MaterialTheme.typography.bodySmall,
                color = LocalThemeColors.current.inkMuted,
            )
            if (codeRequested) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.filter(Char::isDigit).take(6) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isBusy,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text(AppText.t("account_email_code")) },
                )
                TinyVowButton(
                    text = AppText.t("account_confirm_verification"),
                    enabled = !isBusy && code.length == 6,
                    onClick = { onConfirmCode(code) },
                    tone = TinyVowButtonTone.Primary,
                )
                TextButton(
                    enabled = !isBusy,
                    onClick = onRequestCode,
                ) {
                    Text(AppText.t("account_resend_code"))
                }
            } else {
                TinyVowButton(
                    text = AppText.t("account_send_verification_code"),
                    enabled = !isBusy,
                    onClick = {
                        onRequestCode()
                        codeRequested = true
                    },
                    tone = TinyVowButtonTone.Primary,
                )
            }
        }
    }
}

@Composable
private fun AccountInfoRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        icon()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = LocalThemeColors.current.inkMuted,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = LocalThemeColors.current.inkStrong,
            )
        }
    }
}

@Composable
private fun AccountValueRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = LocalThemeColors.current.inkMuted,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = LocalThemeColors.current.inkStrong,
        )
    }
}

@Composable
private fun RecentLoginRow(event: BackendLoginEvent) {
    val method =
        when (event.authMethod) {
            "PASSWORD_REGISTER" -> AppText.t("account_login_method_register")
            "PASSWORD" -> AppText.t("account_login_method_password")
            "TRUSTED_DEVICE" -> AppText.t("account_login_method_trusted_device")
            else -> AppText.t("account_login_method_other")
        }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = event.deviceName?.takeIf { it.isNotBlank() }
                ?: AppText.t("account_unknown_device"),
            style = MaterialTheme.typography.bodyMedium,
            color = LocalThemeColors.current.inkStrong,
        )
        Text(
            text = AppText.t(
                "account_login_record_format",
                method,
                formatAccountDate(event.loggedInAtMillis),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = LocalThemeColors.current.inkMuted,
        )
    }
}

private fun formatAccountDate(timestamp: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(timestamp))
