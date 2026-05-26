package com.rrrrz.tinyvow.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.rrrrz.tinyvow.MainActivity
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.i18n.AppText

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

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(context).notify(packageName.hashCode(), notification)
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
        NotificationManagerCompat.from(context).notify(groupId.hashCode(), notification)
    }

    fun notifyEncourageIncomplete(
        timeText: String,
        groupNames: List<String>,
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
        val body =
            if (remainingCount > 0) {
                textContext.getString(
                    R.string.notification_encourage_incomplete_body_more,
                    groupText,
                    remainingCount,
                )
            } else {
                textContext.getString(R.string.notification_encourage_incomplete_body, groupText)
            }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(textContext.getString(R.string.notification_encourage_incomplete_title, timeText))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        if (!canPostNotifications()) return
        NotificationManagerCompat.from(context).notify(ENCOURAGE_INCOMPLETE_NOTIFICATION_ID, notification)
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    companion object {
        const val CHANNEL_ID = "daily_limit_alerts"
        private const val ENCOURAGE_INCOMPLETE_NOTIFICATION_ID = 20_240_501
    }
}
