package com.rrrrz.tinyvow.data.reminder

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReminderScheduler(
    private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun schedule() {
        scheduleControlRemainingReminder()
        scope.launch {
            val preferences = ManagedAppPreferences(context)
            val isProActive = resolveReminderProActive(context, preferences)
            scheduleNextEncourageReminder(currentNotificationReminderSettings(preferences, isProActive))
        }
    }

    fun scheduleControlRemainingReminder() {
        val request = PeriodicWorkRequestBuilder<ControlRemainingReminderWorker>(
            CONTROL_INTERVAL_MINUTES,
            TimeUnit.MINUTES,
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CONTROL_UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun scheduleNextEncourageReminder(settings: NotificationReminderSettings? = null) {
        val nowMillis = System.currentTimeMillis()
        val times = settings?.encourageReminderTimesMinutes
            ?: ManagedAppPreferences.DEFAULT_ENCOURAGE_REMINDER_TIMES_MINUTES
        val nextTime = ReminderPolicy.nextEncourageTimeMinutes(nowMillis, times)
        val delayMillis = ReminderPolicy.nextEncourageDelayMillis(nowMillis, times)
        val request =
            OneTimeWorkRequestBuilder<EncourageIncompleteReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf(INPUT_ENCOURAGE_TIME_MINUTES to nextTime))
                .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ENCOURAGE_UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    companion object {
        const val INPUT_ENCOURAGE_TIME_MINUTES = "encourage_time_minutes"
        const val CONTROL_UNIQUE_WORK_NAME = "notification_control_remaining_work"
        const val ENCOURAGE_UNIQUE_WORK_NAME = "notification_encourage_incomplete_work"
        private const val CONTROL_INTERVAL_MINUTES = 15L
    }
}
