package com.rrrrz.tinyvow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.theme.TinyVowButton
import com.rrrrz.tinyvow.ui.theme.TinyVowButtonTone
import com.rrrrz.tinyvow.ui.theme.TinyVowSpacing
import com.rrrrz.tinyvow.ui.theme.TinyVowTheme

class HealthConnectRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppText.attach(this)
        setContent {
            CompositionLocalProvider(LocalContext provides AppText.localizedContext(this, AppText.currentLanguage())) {
                TinyVowTheme {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(TinyVowSpacing.PageHorizontal),
                        verticalArrangement = Arrangement.spacedBy(TinyVowSpacing.SectionGap),
                    ) {
                        Text(
                            text = AppText.t("health_connect_rationale_title"),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = AppText.t("health_connect_rationale_body"),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TinyVowButton(
                            text = AppText.t("offline_focus_rest_reminder_done"),
                            onClick = { finish() },
                            tone = TinyVowButtonTone.Primary,
                        )
                    }
                }
            }
        }
    }
}
