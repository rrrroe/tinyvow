package com.rrrrz.tinyvow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rrrrz.tinyvow.data.notification.TinyVowNotifier
import com.rrrrz.tinyvow.ui.home.HomeRoute
import com.rrrrz.tinyvow.ui.theme.TinyVowTheme

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.i18n.AppLanguage
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.ui.theme.DefaultThemeSeed
import com.rrrrz.tinyvow.ui.theme.resolveThemeSeed
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        TinyVowNotifier(this).ensureChannel()
        setContent {
            val prefs = remember { ManagedAppPreferences(this@MainActivity) }
            val selectedThemeId by prefs.selectedThemeId.collectAsState(initial = DefaultThemeSeed.id)
            val customThemes by prefs.customThemes.collectAsState(initial = emptyList())
            val selectedAppLanguageFlow = remember(prefs) {
                prefs.selectedAppLanguage.map<AppLanguage, AppLanguage?> { it }
            }
            val loadedAppLanguage by selectedAppLanguageFlow.collectAsState(initial = null)
            val selectedAppLanguage = loadedAppLanguage ?: AppText.currentLanguage()
            val localizedContext = remember(selectedAppLanguage) {
                AppText.localizedContext(this@MainActivity, selectedAppLanguage)
            }
            val themeSeed = remember(selectedThemeId, customThemes) {
                resolveThemeSeed(selectedThemeId, customThemes)
            }

            LaunchedEffect(loadedAppLanguage) {
                val language = loadedAppLanguage ?: return@LaunchedEffect
                AppText.setLanguage(language, this@MainActivity)
                TinyVowNotifier(this@MainActivity).ensureChannel()
            }
            
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalActivityResultRegistryOwner provides this@MainActivity,
            ) {
                TinyVowTheme(
                    themeSeed = themeSeed
                ) {
                    HomeRoute()
                }
            }
        }
    }
}
