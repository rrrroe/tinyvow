package com.rrrrz.tinyvow

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.reminder.ReminderScheduler
import com.rrrrz.tinyvow.data.time.BusinessDay
import com.rrrrz.tinyvow.i18n.AppLanguage
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.service.offline.OfflineFocusTimerService
import com.rrrrz.tinyvow.ui.theme.DefaultThemeSeed
import com.rrrrz.tinyvow.ui.theme.resolveThemeSeed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

class MainActivity : ComponentActivity() {
    private val offlineFocusCompletionSessionId = mutableStateOf<String?>(null)
    private val offlineFocusDetailRequestToken = mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateOfflineFocusCompletionIntent(intent)
        AppText.attach(this)
        enableEdgeToEdge()
        requestHighestRefreshRate()
        TinyVowNotifier(this).ensureChannel()
        ReminderScheduler(this).schedule()
        SpecialAppHistoryScheduler(this).schedule()
        setContent {
            val lifecycle = LocalLifecycleOwner.current.lifecycle
            val prefs = remember { ManagedAppPreferences(this@MainActivity) }
            val selectedThemeId by prefs.selectedThemeId.collectAsStateWithLifecycle(initialValue = DefaultThemeSeed.id, lifecycle = lifecycle)
            val customThemes by prefs.customThemes.collectAsStateWithLifecycle(initialValue = emptyList(), lifecycle = lifecycle)
            val dayBoundaryHour by prefs.dayBoundaryHour.collectAsStateWithLifecycle(
                initialValue = BusinessDay.DEFAULT_START_HOUR,
                lifecycle = lifecycle,
            )
            var themeDate by remember(dayBoundaryHour) {
                mutableStateOf(BusinessDay.today(ZoneId.systemDefault(), dayBoundaryHour))
            }
            val selectedAppLanguageFlow = remember(prefs) {
                prefs.selectedAppLanguage.map<AppLanguage, AppLanguage?> { it }
            }
            val loadedAppLanguage by selectedAppLanguageFlow.collectAsStateWithLifecycle(initialValue = null, lifecycle = lifecycle)
            val selectedAppLanguage = loadedAppLanguage ?: AppText.currentLanguage()
            val localizedContext = remember(selectedAppLanguage) {
                AppText.localizedContext(this@MainActivity, selectedAppLanguage)
            }
            val themeSeed = remember(selectedThemeId, customThemes, themeDate) {
                resolveThemeSeed(selectedThemeId, customThemes, themeDate)
            }

            LaunchedEffect(dayBoundaryHour) {
                BusinessDay.updateCachedStartHour(dayBoundaryHour)
                themeDate = BusinessDay.today(ZoneId.systemDefault(), dayBoundaryHour)
            }

            LaunchedEffect(dayBoundaryHour, themeDate) {
                while (true) {
                    val nextMidnightMillis =
                        BusinessDay.nextDayStartMillis(themeDate, ZoneId.systemDefault(), dayBoundaryHour)
                    delay((nextMidnightMillis - System.currentTimeMillis()).coerceAtLeast(60_000L))
                    themeDate = BusinessDay.today(ZoneId.systemDefault(), dayBoundaryHour)
                }
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
                    HomeRoute(
                        completedOfflineFocusSessionId = offlineFocusCompletionSessionId.value,
                        offlineFocusDetailRequestToken = offlineFocusDetailRequestToken.value,
                        onCompletedOfflineFocusConsumed = {
                            offlineFocusCompletionSessionId.value = null
                        },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        updateOfflineFocusCompletionIntent(intent)
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        if (offlineFocusCompletionSessionId.value != null) {
            OfflineFocusTimerService.stopCompletionSignal(this)
        }
    }

    private fun updateOfflineFocusCompletionIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_OFFLINE_FOCUS_COMPLETED -> {
                offlineFocusCompletionSessionId.value = intent.getStringExtra(EXTRA_OFFLINE_FOCUS_SESSION_ID)
            }
            ACTION_OFFLINE_FOCUS_COMPLETED_CLICK -> {
                OfflineFocusTimerService.stopCompletionSignal(this)
                offlineFocusCompletionSessionId.value = intent.getStringExtra(EXTRA_OFFLINE_FOCUS_SESSION_ID)
            }
            ACTION_OFFLINE_FOCUS_ACTIVE -> {
                offlineFocusDetailRequestToken.value += 1
            }
        }
    }

    private fun requestHighestRefreshRate() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

        val windowManager = getSystemService(WindowManager::class.java)
        @Suppress("DEPRECATION")
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) this.display else windowManager.defaultDisplay
        val currentMode = display?.mode
        val bestMode =
            display?.supportedModes
                ?.asSequence()
                ?.filter { mode ->
                    currentMode == null ||
                        (mode.physicalWidth == currentMode.physicalWidth &&
                            mode.physicalHeight == currentMode.physicalHeight)
                }
                ?.maxByOrNull { it.refreshRate }
                ?: currentMode

        if (bestMode != null) {
            window.attributes = window.attributes.apply {
                preferredDisplayModeId = bestMode.modeId
                preferredRefreshRate = bestMode.refreshRate
            }
        }
    }

    companion object {
        const val ACTION_OFFLINE_FOCUS_COMPLETED = "com.rrrrz.tinyvow.offline_focus.COMPLETED"
        const val ACTION_OFFLINE_FOCUS_COMPLETED_CLICK = "com.rrrrz.tinyvow.offline_focus.COMPLETED_CLICK"
        const val ACTION_OFFLINE_FOCUS_ACTIVE = "com.rrrrz.tinyvow.offline_focus.ACTIVE"
        const val EXTRA_OFFLINE_FOCUS_SESSION_ID = "offline_focus_session_id"
    }
}





