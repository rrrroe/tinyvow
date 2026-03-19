package com.rrrrz.tinyvow.data.reminder

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class LimitReminderScheduler(
    private val context: Context,
) {
    fun schedule() {
        val request = PeriodicWorkRequestBuilder<LimitReminderWorker>(
            15,
            TimeUnit.MINUTES,
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    companion object {
        const val UNIQUE_WORK_NAME = "daily_limit_reminder_work"
    }
}
