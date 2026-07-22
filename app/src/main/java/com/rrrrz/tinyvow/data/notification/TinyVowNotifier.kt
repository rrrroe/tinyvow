package com.rrrrz.tinyvow.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.rrrrz.tinyvow.MainActivity
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.data.db.AchievementEntity
import com.rrrrz.tinyvow.data.reminder.EncourageProgressReminder
import com.rrrrz.tinyvow.i18n.AppText
import java.util.Locale
import java.util.concurrent.ThreadLocalRandom

class TinyVowNotifier(
    private val context: Context,
) {
    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val textContext = AppText.localizedContext(context)
        val channel = NotificationChannel(
            CHANNEL_ID,
            textContext.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = textContext.getString(R.string.notification_channel_desc)
        }
        notificationManager.createNotificationChannel(channel)
        val achievementChannel = NotificationChannel(
            ACHIEVEMENT_CHANNEL_ID,
            textContext.getString(R.string.notification_achievement_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = textContext.getString(R.string.notification_achievement_channel_desc)
        }
        notificationManager.createNotificationChannel(achievementChannel)
    }

    fun notifyLimitExceeded(
        packageName: String,
        appName: String,
        exceededText: String,
    ) {
        ensureChannel()
        val textContext = AppText.localizedContext(context)
        val openAppIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            packageName.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(
                textContext.getString(
                    R.string.notification_limit_title,
                    appName,
                ),
            )
            .setContentText(
                textContext.getString(
                    R.string.notification_limit_body,
                    exceededText,
                ),
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        postNotification(packageName.hashCode(), notification)
    }

    fun notifyControlRemaining(
        groupId: String,
        groupName: String,
        remainingText: String,
    ) {
        ensureChannel()
        val textContext = AppText.localizedContext(context)
        val pendingIntent = PendingIntent.getActivity(
            context,
            groupId.hashCode(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(textContext.getString(R.string.notification_control_remaining_title, groupName))
            .setContentText(textContext.getString(R.string.notification_control_remaining_body, remainingText))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        if (!canPostNotifications()) return
        postNotification(groupId.hashCode(), notification)
    }

    fun notifyEncourageIncomplete(
        timeText: String,
        groupNames: List<String>,
        progressReminder: EncourageProgressReminder?,
    ) {
        ensureChannel()
        val textContext = AppText.localizedContext(context)
        val pendingIntent = PendingIntent.getActivity(
            context,
            ENCOURAGE_INCOMPLETE_NOTIFICATION_ID,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val groupText = groupNames.take(3).joinToString(textContext.getString(R.string.list_separator))
        val remainingCount = (groupNames.size - 3).coerceAtLeast(0)
        val statusText =
            if (remainingCount > 0) {
                textContext.getString(
                    R.string.notification_encourage_incomplete_body_more,
                    groupText,
                    remainingCount,
                )
            } else {
                textContext.getString(R.string.notification_encourage_incomplete_body, groupText)
            }
        val progressText = progressReminder?.let {
            randomEncourageProgressText(
                textContext = textContext,
                remainingMinutes = it.remainingMinutes,
                groupName = it.groupName,
            )
        }
        val body = listOfNotNull(progressText, statusText).joinToString("\n")
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(textContext.getString(R.string.notification_encourage_incomplete_title, timeText))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        if (!canPostNotifications()) return
        postNotification(ENCOURAGE_INCOMPLETE_NOTIFICATION_ID, notification)
    }

    fun notifyEncourageCompleted(
        groupId: String,
        groupName: String,
    ) {
        ensureChannel()
        val textContext = AppText.localizedContext(context)
        val pendingIntent = PendingIntent.getActivity(
            context,
            groupId.hashCode(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val body = textContext.getString(R.string.notification_encourage_completed_body, groupName)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(textContext.getString(R.string.notification_encourage_completed_title))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        if (!canPostNotifications()) return
        postNotification(ENCOURAGE_COMPLETED_NOTIFICATION_BASE_ID + groupId.hashCode(), notification)
    }

    fun notifyAchievementUnlocked(achievement: AchievementEntity) {
        ensureChannel()
        val textContext = AppText.localizedContext(context)
        val title = achievement.localizedTitle()
        val description = achievement.localizedDescription()
        val pendingIntent = PendingIntent.getActivity(
            context,
            ACHIEVEMENT_NOTIFICATION_BASE_ID + achievement.id.hashCode(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, ACHIEVEMENT_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(textContext.getString(R.string.notification_achievement_title, title))
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        postNotification(ACHIEVEMENT_NOTIFICATION_BASE_ID + achievement.id.hashCode(), notification)
    }

    private fun AchievementEntity.localizedTitle(): String {
        val key = "achievement_${id.lowercase()}_title"
        return AppText.t(key).takeUnless { it == key } ?: title
    }

    private fun AchievementEntity.localizedDescription(): String {
        val key = "achievement_${id.lowercase()}_desc"
        return AppText.t(key).takeUnless { it == key } ?: description
    }

    private fun randomEncourageProgressText(
        textContext: Context,
        remainingMinutes: Int,
        groupName: String,
    ): String {
        val messages = textContext.resources.getStringArray(R.array.notification_encourage_progress_messages)
        val template = messages.randomOrNullByThread() ?: return ""
        return String.format(
            textContext.resources.configuration.locales[0] ?: Locale.getDefault(),
            template,
            remainingMinutes,
            groupName,
        )
    }

    private fun Array<String>.randomOrNullByThread(): String? {
        if (isEmpty()) return null
        return this[ThreadLocalRandom.current().nextInt(size)]
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun postNotification(id: Int, notification: Notification) {
        if (!canPostNotifications()) return
        runCatching {
            NotificationManagerCompat.from(context).notify(id, notification)
        }
    }

    companion object {
        const val CHANNEL_ID = "daily_limit_alerts"
        const val ACHIEVEMENT_CHANNEL_ID = "achievement_unlocks"
        private const val ENCOURAGE_INCOMPLETE_NOTIFICATION_ID = 20_240_501
        private const val ENCOURAGE_COMPLETED_NOTIFICATION_BASE_ID = 20_240_600
        private const val ACHIEVEMENT_NOTIFICATION_BASE_ID = 20_240_700
    }
}
