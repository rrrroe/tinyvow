package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.i18n.AppText

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.TinyVowButton
import com.rrrrz.tinyvow.ui.theme.TinyVowButtonTone

enum class ProUpsellSource {
    GROUP_LIMIT,
    GROUP_APPS,
    CUSTOM_REWARD,
    CUSTOM_THEME,
    MEMBER_THEME,
    ADVANCED_REPORT,
    NOTIFICATION_CUSTOMIZATION,
    DAY_BOUNDARY_CUSTOMIZATION,
    STEP_POINTS,
}

@Composable
fun ProUpsellDialog(
    source: ProUpsellSource,
    onViewBenefits: () -> Unit,
    onDismiss: () -> Unit,
) {
    val themeColors = LocalThemeColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = AppText.t("pro_upsell_title"),
                style = MaterialTheme.typography.titleLarge,
                color = themeColors.inkStrong,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = proUpsellMessage(source),
                    style = MaterialTheme.typography.bodyMedium,
                    color = themeColors.ink,
                )
                Text(
                    text = AppText.t("pro_upsell_common_benefits"),
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColors.inkMuted,
                )
            }
        },
        confirmButton = {
            TinyVowButton(
                text = AppText.t("pro_view_benefits"),
                onClick = onViewBenefits,
                tone = TinyVowButtonTone.Primary,
            )
        },
        dismissButton = {
            TinyVowButton(
                text = AppText.t("group_cancel"),
                onClick = onDismiss,
            )
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
            ProUpsellSource.NOTIFICATION_CUSTOMIZATION -> "pro_upsell_notification_customization"
            ProUpsellSource.DAY_BOUNDARY_CUSTOMIZATION -> "pro_upsell_day_boundary_customization"
            ProUpsellSource.STEP_POINTS -> "pro_upsell_step_points"
        },
    )
