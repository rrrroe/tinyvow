package com.rrrrz.tinyvow.ui.block

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rrrrz.tinyvow.MainActivity
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.ui.theme.TinyVowTheme

class BlockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME).orEmpty()
        val exceededMillis = intent.getLongExtra(EXTRA_EXCEEDED_MILLIS, 0L)

        setContent {
            TinyVowTheme {
                BlockScreen(
                    packageName = packageName,
                    exceededText = formatDuration(exceededMillis),
                    onOpenApp = {
                        startActivity(
                            Intent(this, MainActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                        )
                        finish()
                    },
                    onGoHome = {
                        startActivity(
                            Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_HOME)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            },
                        )
                        finish()
                    },
                    onOpenAccessibilitySettings = {
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                )
            }
        }
    }

    private fun formatDuration(durationMillis: Long): String {
        val totalSeconds = durationMillis / 1_000
        val totalMinutes = durationMillis / 60_000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 && minutes > 0 -> "${hours}小时 ${minutes}分钟"
            hours > 0 -> "${hours}小时"
            totalMinutes > 0L -> "${minutes}分钟"
            totalSeconds > 0L -> "${seconds}秒"
            else -> "0秒"
        }
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_EXCEEDED_MILLIS = "extra_exceeded_millis"
        const val BLOCK_ACTIVITY_PACKAGE = "com.rrrrz.tinyvow"
    }
}

@Composable
private fun BlockScreen(
    packageName: String,
    exceededText: String,
    onOpenApp: () -> Unit,
    onGoHome: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
) {
    BackHandler(enabled = true) {}

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.block_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(
                    R.string.block_body,
                    packageName,
                    exceededText,
                ),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.block_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onOpenApp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.block_open_app))
            }
            OutlinedButton(
                onClick = onGoHome,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.block_go_home))
            }
            OutlinedButton(
                onClick = onOpenAccessibilitySettings,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.block_open_accessibility_settings))
            }
        }
    }
}
