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

    companion object {
        const val CHANNEL_ID = "daily_limit_alerts"
    }
}
