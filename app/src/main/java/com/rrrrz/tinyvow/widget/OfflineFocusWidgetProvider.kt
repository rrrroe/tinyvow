package com.rrrrz.tinyvow.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import com.rrrrz.tinyvow.MainActivity
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.db.OfflineFocusSessionStatus
import com.rrrrz.tinyvow.data.repository.OfflineFocusRepository
import com.rrrrz.tinyvow.data.repository.OfflineFocusSession
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.i18n.AppText
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class OfflineFocusWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val pendingResult = goAsync()
        widgetScope.launch {
            try {
                renderWidgets(context.applicationContext, appWidgetManager, appWidgetIds)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val REQUEST_OPEN_HOME = 20_261_001
        private const val REQUEST_OPEN_ACTIVE = 20_261_002
        private const val REQUEST_OPEN_START = 20_261_003
        private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun updateAllWidgets(context: Context) {
            val appContext = context.applicationContext
            val manager = AppWidgetManager.getInstance(appContext)
            val ids = manager.getAppWidgetIds(ComponentName(appContext, OfflineFocusWidgetProvider::class.java))
            if (ids.isEmpty()) return
            widgetScope.launch {
                renderWidgets(appContext, manager, ids)
            }
        }

        private suspend fun renderWidgets(
            context: Context,
            manager: AppWidgetManager,
            ids: IntArray,
        ) {
            if (ids.isEmpty()) return
            val preferences = prepareLocalizedPreferences(context)
            val localizedContext =
                AppText.localizedContext(context, preferences.getSelectedAppLanguageOnce())
            val session =
                OfflineFocusRepository(context, AppDatabase.getDatabase(context))
                    .getActiveSessionOnce()
            val views = buildRemoteViews(context, localizedContext, session)
            ids.forEach { manager.updateAppWidget(it, views) }
        }

        private suspend fun prepareLocalizedPreferences(context: Context): ManagedAppPreferences {
            val preferences = ManagedAppPreferences(context)
            AppText.setLanguage(preferences.getSelectedAppLanguageOnce(), context)
            return preferences
        }

        private fun buildRemoteViews(
            context: Context,
            textContext: Context,
            session: OfflineFocusSession?,
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.offline_focus_widget)
            views.setContentDescription(
                R.id.offline_focus_widget_container,
                textContext.getString(R.string.offline_focus_widget_description),
            )

            if (session == null) {
                views.setTextViewText(R.id.offline_focus_widget_title, textContext.getString(R.string.offline_focus_title))
                views.setViewVisibility(R.id.offline_focus_widget_status, View.VISIBLE)
                views.setTextViewText(
                    R.id.offline_focus_widget_status,
                    textContext.getString(R.string.offline_focus_widget_subtitle),
                )
                views.setViewVisibility(R.id.offline_focus_widget_timer, View.GONE)
                views.setTextViewText(
                    R.id.offline_focus_widget_action,
                    textContext.getString(R.string.offline_focus_start_short),
                )
                views.setOnClickPendingIntent(
                    R.id.offline_focus_widget_container,
                    openAppPendingIntent(context, showActiveFocus = false),
                )
                views.setOnClickPendingIntent(
                    R.id.offline_focus_widget_action,
                    openFocusStartPendingIntent(context),
                )
                return views
            }

            views.setTextViewText(R.id.offline_focus_widget_title, session.categoryName)
            views.setTextViewText(
                R.id.offline_focus_widget_action,
                textContext.getString(R.string.offline_focus_details),
            )
            val openActiveIntent = openAppPendingIntent(context, showActiveFocus = true)
            views.setOnClickPendingIntent(R.id.offline_focus_widget_container, openActiveIntent)
            views.setOnClickPendingIntent(R.id.offline_focus_widget_action, openActiveIntent)
            bindTimer(views, textContext, session)
            return views
        }

        private fun bindTimer(
            views: RemoteViews,
            textContext: Context,
            session: OfflineFocusSession,
            nowMillis: Long = System.currentTimeMillis(),
            elapsedRealtime: Long = SystemClock.elapsedRealtime(),
        ) {
            val referenceNow =
                if (session.status == OfflineFocusSessionStatus.PAUSED) {
                    session.pausedAt ?: nowMillis
                } else {
                    nowMillis
                }
            val elapsed = (referenceNow - session.startedAt).coerceAtLeast(0L)
            val finite = session.plannedDurationMillis > 0L
            val displayedMillis =
                if (finite) {
                    (session.plannedDurationMillis - elapsed).coerceAtLeast(0L)
                } else {
                    elapsed
                }

            if (session.status == OfflineFocusSessionStatus.PAUSED) {
                views.setViewVisibility(R.id.offline_focus_widget_timer, View.GONE)
                views.setViewVisibility(R.id.offline_focus_widget_status, View.VISIBLE)
                val textKey =
                    if (finite) {
                        R.string.offline_focus_widget_paused_remaining_format
                    } else {
                        R.string.offline_focus_widget_paused_elapsed_format
                    }
                views.setTextViewText(
                    R.id.offline_focus_widget_status,
                    textContext.getString(textKey, formatDuration(displayedMillis)),
                )
                return
            }

            views.setViewVisibility(R.id.offline_focus_widget_status, View.GONE)
            views.setViewVisibility(R.id.offline_focus_widget_timer, View.VISIBLE)
            val base = if (finite) elapsedRealtime + displayedMillis else elapsedRealtime - displayedMillis
            val format =
                textContext.getString(
                    if (finite) {
                        R.string.offline_focus_widget_remaining_format
                    } else {
                        R.string.offline_focus_widget_elapsed_format
                    },
                    "%s",
                )
            views.setChronometer(R.id.offline_focus_widget_timer, base, format, true)
            views.setBoolean(R.id.offline_focus_widget_timer, "setCountDown", finite)
        }

        private fun openAppPendingIntent(
            context: Context,
            showActiveFocus: Boolean,
        ): PendingIntent {
            val intent =
                Intent(context, MainActivity::class.java)
                    .setAction(
                        if (showActiveFocus) {
                            MainActivity.ACTION_OFFLINE_FOCUS_ACTIVE
                        } else {
                            Intent.ACTION_MAIN
                        },
                    )
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            if (!showActiveFocus) intent.addCategory(Intent.CATEGORY_LAUNCHER)
            return PendingIntent.getActivity(
                context,
                if (showActiveFocus) REQUEST_OPEN_ACTIVE else REQUEST_OPEN_HOME,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        private fun openFocusStartPendingIntent(context: Context): PendingIntent =
            PendingIntent.getActivity(
                context,
                REQUEST_OPEN_START,
                Intent(context, MainActivity::class.java)
                    .setAction(MainActivity.ACTION_OFFLINE_FOCUS_START)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        private fun formatDuration(millis: Long): String {
            val totalSeconds = ((millis + 999L) / 1_000L).coerceAtLeast(0L)
            val hours = totalSeconds / 3_600L
            val minutes = (totalSeconds % 3_600L) / 60L
            val seconds = totalSeconds % 60L
            return if (hours > 0L) {
                String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }
    }
}
