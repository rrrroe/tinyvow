package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.i18n.AppText

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

enum class ProUpsellSource {
    GROUP_LIMIT,
    GROUP_APPS,
    CUSTOM_REWARD,
    CUSTOM_THEME,
    MEMBER_THEME,
    ADVANCED_REPORT,
}

@Composable
fun ProUpsellDialog(
    source: ProUpsellSource,
    onViewBenefits: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = AppText.t("pro_upsell_title"),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = proUpsellMessage(source),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = AppText.t("pro_upsell_common_benefits"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(onClick = onViewBenefits) {
                Text(AppText.t("pro_view_benefits"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppText.t("group_cancel"))
            }
        },
    )
}

private fun proUpsellMessage(source: ProUpsellSource): String =
    AppText.t(
        when (source) {
            ProUpsellSource.GROUP_LIMIT -> "pro_upsell_group_limit"
            ProUpsellSource.GROUP_APPS -> "pro_upsell_group_apps"
            ProUpsellSource.CUSTOM_REWARD -> "pro_upsell_custom_reward"
            ProUpsellSource.CUSTOM_THEME -> "pro_upsell_custom_theme"
            ProUpsellSource.MEMBER_THEME -> "pro_upsell_member_theme"
            ProUpsellSource.ADVANCED_REPORT -> "pro_upsell_advanced_report"
        },
    )
