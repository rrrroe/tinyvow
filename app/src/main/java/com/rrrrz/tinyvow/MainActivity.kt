package com.rrrrz.tinyvow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rrrrz.tinyvow.data.notification.TinyVowNotifier
import com.rrrrz.tinyvow.ui.home.HomeRoute
import com.rrrrz.tinyvow.ui.theme.TinyVowTheme

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.ui.theme.DefaultThemeSeed
import com.rrrrz.tinyvow.ui.theme.resolveThemeSeed

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        TinyVowNotifier(this).ensureChannel()
        setContent {
            val prefs = remember { ManagedAppPreferences(this@MainActivity) }
            val selectedThemeId by prefs.selectedThemeId.collectAsState(initial = DefaultThemeSeed.id)
            val customThemes by prefs.customThemes.collectAsState(initial = emptyList())
            val themeSeed = remember(selectedThemeId, customThemes) {
                resolveThemeSeed(selectedThemeId, customThemes)
            }
            
            TinyVowTheme(
                themeSeed = themeSeed
            ) {
                HomeRoute()
            }
        }
    }
}
