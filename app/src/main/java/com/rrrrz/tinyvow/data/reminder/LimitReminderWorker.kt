package com.rrrrz.tinyvow.data.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rrrrz.tinyvow.data.apps.InstalledAppRepository
import com.rrrrz.tinyvow.data.notification.NotificationPermissionChecker
import com.rrrrz.tinyvow.data.notification.TinyVowNotifier
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.usage.UsageAccessStateChecker
import com.rrrrz.tinyvow.data.usage.UsageAccessStatus
import com.rrrrz.tinyvow.data.usage.UsageStatsUsageRepository
import com.rrrrz.tinyvow.domain.limit.DailyTimeLimitPolicy
import java.time.LocalDate
import java.time.ZoneId

class LimitReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val usageAccessStateChecker = UsageAccessStateChecker(applicationContext)
        if (usageAccessStateChecker.getStatus() != UsageAccessStatus.GRANTED) {
            return Result.success()
        }

        val notificationPermissionChecker = NotificationPermissionChecker(applicationContext)
        if (!notificationPermissionChecker.isGranted()) {
            return Result.success()
        }

        val preferences = ManagedAppPreferences(applicationContext)
        val selectedPackageName = preferences.getSelectedPackageNameOnce() ?: return Result.success()
        val dailyLimitMinutes =
            preferences.getDailyLimitMinutesOnce(selectedPackageName) ?: return Result.success()
        val usageMillis =
            UsageStatsUsageRepository(applicationContext).getTodayUsageMillis(selectedPackageName)
        val evaluation = DailyTimeLimitPolicy().evaluate(
            usageMillis = usageMillis,
            limitMillis = dailyLimitMinutes * 60_000L,
        )

        val todayKey = LocalDate.now(ZoneId.systemDefault()).toString()
        val lastReminderDay = preferences.getLastReminderDateOnce(selectedPackageName)

        if (!evaluation.isExceeded) {
            preferences.clearLastReminderDate(selectedPackageName)
            return Result.success()
        }

        if (lastReminderDay == todayKey) {
            return Result.success()
        }

        val appName = InstalledAppRepository(applicationContext)
            .getAllInstalledApps()
            .firstOrNull { it.packageName == selectedPackageName }
            ?.appName
            ?: selectedPackageName

        TinyVowNotifier(applicationContext).notifyLimitExceeded(
            packageName = selectedPackageName,
            appName = appName,
            exceededText = formatDuration(evaluation.exceededMillis),
        )
        preferences.setLastReminderDate(selectedPackageName, todayKey)
        return Result.success()
    }

    private fun formatDuration(durationMillis: Long): String {
        val totalSeconds = durationMillis / 1_000
        val totalMinutes = durationMillis / 60_000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 && minutes > 0 -> "${hours}小时 ${minutes}分钟"
            hours > 0 -> "${hours}小时"
            totalMinutes > 0L -> "${minutes}分钟"
            totalSeconds > 0L -> "${seconds}秒"
            else -> "0秒"
        }
    }
}
