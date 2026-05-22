package com.rrrrz.tinyvow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rrrrz.tinyvow.data.notification.TinyVowNotifier
import com.rrrrz.tinyvow.data.special.SpecialAppHistoryScheduler
import com.rrrrz.tinyvow.ui.home.HomeRoute
import com.rrrrz.tinyvow.ui.theme.TinyVowTheme

import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
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
        AppText.attach(this)
        enableEdgeToEdge()
        TinyVowNotifier(this).ensureChannel()
        SpecialAppHistoryScheduler(this).schedule()
        setContent {
            val lifecycle = LocalLifecycleOwner.current.lifecycle
            val prefs = remember { ManagedAppPreferences(this@MainActivity) }
            val selectedThemeId by prefs.selectedThemeId.collectAsStateWithLifecycle(initialValue = DefaultThemeSeed.id, lifecycle = lifecycle)
            val customThemes by prefs.customThemes.collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
            val selectedAppLanguageFlow = remember(prefs) {
                prefs.selectedAppLanguage.map<AppLanguage, AppLanguage?> { it }
            }
            val loadedAppLanguage by selectedAppLanguageFlow.collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
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





