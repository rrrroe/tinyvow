package com.rrrrz.tinyvow.ui.home

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.theme.LocalThemeColors
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing

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

