package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.i18n.AppText

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaboratoryScreen(
    onAddPoints: (Double) -> Unit,
    onTriggerAchievementPopupTest: () -> Unit,
    onResetSummary: () -> Unit,
    onTriggerSummary: () -> Unit,
    showDebugProControls: Boolean,
    onExtendDebugPro: (Int) -> Unit,
    onClearDebugPro: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AppText.t("lab_laboratory_debug_tools")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = AppText.t("group_back"))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(AppText.t("lab_points_simulation"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onAddPoints(10.0) }, modifier = Modifier.weight(1f)) { Text("+10") }
                Button(onClick = { onAddPoints(100.0) }, modifier = Modifier.weight(1f)) { Text("+100") }
            }

            HorizontalDivider()

            Text(AppText.t("lab_achievement_test"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                AppText.t("lab_achievement_test_description"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onTriggerAchievementPopupTest,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(AppText.t("lab_trigger_achievement_popup"))
            }

            if (showDebugProControls) {
                HorizontalDivider()

                Text(AppText.t("lab_pro_debug"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    AppText.t("lab_pro_debug_description"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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

            HorizontalDivider()

            Text(AppText.t("lab_report_test"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                AppText.t("lab_tip_after_resetting_restart_the_app_to_verify"),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
            )
        }
    }
}
