package com.rrrrz.tinyvow.data.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rrrrz.tinyvow.data.notification.NotificationPermissionChecker
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.usage.UsageAccessStateChecker
import com.rrrrz.tinyvow.data.usage.UsageAccessStatus
import com.rrrrz.tinyvow.i18n.AppText

class ControlRemainingReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        if (!canSendReminders()) return Result.success()

        val preferences = ManagedAppPreferences(applicationContext)
        AppText.setLanguage(preferences.getSelectedAppLanguageOnce(), applicationContext)
        val isProActive = resolveReminderProActive(applicationContext, preferences)
        val settings = currentNotificationReminderSettings(preferences, isProActive)
        GroupReminderEvaluator(applicationContext).sendDueControlRemainingReminders(settings)
        return Result.success()
    }
}

class EncourageIncompleteReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val preferences = ManagedAppPreferences(applicationContext)
        AppText.setLanguage(preferences.getSelectedAppLanguageOnce(), applicationContext)
        val isProActive = resolveReminderProActive(applicationContext, preferences)
        val settings = currentNotificationReminderSettings(preferences, isProActive)

        if (canSendReminders()) {
            val scheduledTimeMinutes =
                inputData.getInt(ReminderScheduler.INPUT_ENCOURAGE_TIME_MINUTES, -1)
                    .takeIf { it in 0 until 24 * 60 }
                    ?: ReminderPolicy.scheduledTimeForCurrentMinute(
                        nowMillis = System.currentTimeMillis(),
                        reminderTimesMinutes = settings.encourageReminderTimesMinutes,
                    )
                    ?: ReminderPolicy.nextEncourageTimeMinutes(
                        nowMillis = System.currentTimeMillis(),
                        reminderTimesMinutes = settings.encourageReminderTimesMinutes,
                    )
            GroupReminderEvaluator(applicationContext).sendDueEncourageReminder(settings, scheduledTimeMinutes)
        }

        ReminderScheduler(applicationContext).scheduleNextEncourageReminder(settings)
        return Result.success()
    }
}

private fun CoroutineWorker.canSendReminders(): Boolean {
    if (UsageAccessStateChecker(applicationContext).getStatus() != UsageAccessStatus.GRANTED) {
        return false
    }
    if (!NotificationPermissionChecker(applicationContext).isGranted()) {
        return false
    }
    return true
}
