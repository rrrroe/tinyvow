package com.rrrrz.tinyvow.data.special

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class SpecialAppHistoryWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val repository = SpecialAppUsageRepository(applicationContext)
        val config = repository.getWeReadConfig()
        if (!config.syncEnabled) return Result.success()
        return repository
            .syncMissingWeReadHistoryUpToYesterday()
            .fold(
                onSuccess = { Result.success() },
                onFailure = { Result.retry() },
            )
    }
}

class SpecialAppHistoryScheduler(
    private val context: Context,
) {
    fun schedule() {
        val request = PeriodicWorkRequestBuilder<SpecialAppHistoryWorker>(
            6,
            TimeUnit.HOURS,
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    companion object {
        const val UNIQUE_WORK_NAME = "special_app_history_sync_work"
    }
}
