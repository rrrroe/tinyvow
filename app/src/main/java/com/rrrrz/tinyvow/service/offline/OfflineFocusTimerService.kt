package com.rrrrz.tinyvow.service.offline

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.rrrrz.tinyvow.MainActivity
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.db.OfflineFocusPauseReason
import com.rrrrz.tinyvow.data.db.OfflineFocusSessionStatus
import com.rrrrz.tinyvow.data.repository.OfflineFocusRepository
import com.rrrrz.tinyvow.i18n.AppText
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class OfflineFocusTimerService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var repository: OfflineFocusRepository
    private var timerJob: kotlinx.coroutines.Job? = null
    private var screenReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        AppText.attach(applicationContext)
        repository = OfflineFocusRepository(applicationContext, AppDatabase.getDatabase(applicationContext))
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_EARLY -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
                if (sessionId != null) {
                    serviceScope.launch {
                        repository.stopSessionEarly(sessionId)
                        stopForegroundCompat()
                        stopSelf()
                    }
                }
                return START_NOT_STICKY
            }
            ACTION_ABANDON -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
                if (sessionId != null) {
                    serviceScope.launch {
                        repository.abandonSession(sessionId)
                        stopForegroundCompat()
                        stopSelf()
                    }
                }
                return START_NOT_STICKY
            }
            ACTION_START -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
                if (sessionId != null) {
                    startForeground(
                        NOTIFICATION_ID,
                        buildNotification(
                            sessionId = sessionId,
                            categoryName = AppText.localizedContext(this).getString(R.string.offline_focus_title),
                            remainingMillis = 0L,
                            paused = false,
                            starting = true,
                        ),
                    )
                    startTimerLoop(sessionId)
                }
            }
            else -> {
                serviceScope.launch {
                    repository.getActiveSessionOnce()?.let { startTimerLoop(it.id) } ?: stopSelf()
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        timerJob?.cancel()
        unregisterScreenReceiver()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startTimerLoop(sessionId: String) {
        timerJob?.cancel()
        timerJob =
            serviceScope.launch {
                while (isActive) {
                    val session = repository.getActiveSessionOnce()
                    if (session == null || session.id != sessionId) {
                        stopForegroundCompat()
                        stopSelf()
                        return@launch
                    }
                    updateScreenReceiver(sessionId)
                    if (session.status == OfflineFocusSessionStatus.PAUSED) {
                        val pausedAt = session.pausedAt ?: System.currentTimeMillis()
                        val elapsed = (pausedAt - session.startedAt).coerceAtLeast(0L)
                        val remaining = (session.plannedDurationMillis - elapsed).coerceAtLeast(0L)
                        startForeground(NOTIFICATION_ID, buildNotification(sessionId, session.categoryName, remaining, paused = true))
                        delay(1_000L)
                        continue
                    }
                    if (session.status != OfflineFocusSessionStatus.RUNNING) {
                        delay(1_000L)
                        continue
                    }
                    val now = System.currentTimeMillis()
                    val elapsed = (now - session.startedAt).coerceAtLeast(0L)
                    val remaining = (session.plannedDurationMillis - elapsed).coerceAtLeast(0L)
                    startForeground(NOTIFICATION_ID, buildNotification(sessionId, session.categoryName, remaining, paused = false))
                    if (remaining <= 0L) {
                        repository.completeSession(sessionId, now)
                        stopForegroundCompat()
                        stopSelf()
                        return@launch
                    }
                    delay(1_000L)
                }
            }
    }

    private fun buildNotification(
        sessionId: String,
        categoryName: String,
        remainingMillis: Long,
        paused: Boolean,
        starting: Boolean = false,
    ): Notification {
        val textContext = AppText.localizedContext(this)
        val openIntent =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val stopIntent =
            PendingIntent.getService(
                this,
                1,
                Intent(this, OfflineFocusTimerService::class.java)
                    .setAction(ACTION_STOP_EARLY)
                    .putExtra(EXTRA_SESSION_ID, sessionId),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val abandonIntent =
            PendingIntent.getService(
                this,
                2,
                Intent(this, OfflineFocusTimerService::class.java)
                    .setAction(ACTION_ABANDON)
                    .putExtra(EXTRA_SESSION_ID, sessionId),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(textContext.getString(R.string.offline_focus_notification_title, categoryName))
            .setContentText(
                if (starting) {
                    textContext.getString(R.string.offline_focus_notification_starting_body)
                } else if (paused) {
                    textContext.getString(R.string.offline_focus_notification_paused_body, formatRemaining(remainingMillis))
                } else {
                    textContext.getString(R.string.offline_focus_notification_body, formatRemaining(remainingMillis))
                },
            )
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(0, textContext.getString(R.string.offline_focus_finish_early), stopIntent)
            .addAction(0, textContext.getString(R.string.offline_focus_abandon), abandonIntent)
            .build()
    }

    private suspend fun updateScreenReceiver(sessionId: String) {
        if (repository.shouldContinueOnLock()) {
            unregisterScreenReceiver()
            return
        }
        if (screenReceiver != null) return
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val action = intent?.action ?: return
                    serviceScope.launch {
                        when (action) {
                            Intent.ACTION_SCREEN_OFF -> repository.pauseSession(sessionId, OfflineFocusPauseReason.LOCK_SCREEN)
                            Intent.ACTION_USER_PRESENT,
                            Intent.ACTION_SCREEN_ON -> repository.resumeSession(sessionId)
                        }
                    }
                }
            }
        screenReceiver = receiver
        val filter =
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
    }

    private fun unregisterScreenReceiver() {
        val receiver = screenReceiver ?: return
        runCatching { unregisterReceiver(receiver) }
        screenReceiver = null
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val textContext = AppText.localizedContext(this)
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                textContext.getString(R.string.offline_focus_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = textContext.getString(R.string.offline_focus_notification_channel_desc)
            }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun formatRemaining(remainingMillis: Long): String {
        val totalSeconds = ((remainingMillis + 999L) / 1000L).coerceAtLeast(0L)
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    companion object {
        const val ACTION_START = "com.rrrrz.tinyvow.offline_focus.START"
        const val ACTION_STOP_EARLY = "com.rrrrz.tinyvow.offline_focus.STOP_EARLY"
        const val ACTION_ABANDON = "com.rrrrz.tinyvow.offline_focus.ABANDON"
        const val EXTRA_SESSION_ID = "session_id"
        const val CHANNEL_ID = "offline_focus_timer"
        private const val NOTIFICATION_ID = 20_260_601

        fun start(
            context: Context,
            sessionId: String,
        ) {
            val intent =
                Intent(context, OfflineFocusTimerService::class.java)
                    .setAction(ACTION_START)
                    .putExtra(EXTRA_SESSION_ID, sessionId)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopEarly(
            context: Context,
            sessionId: String,
        ) {
            context.startService(
                Intent(context, OfflineFocusTimerService::class.java)
                    .setAction(ACTION_STOP_EARLY)
                    .putExtra(EXTRA_SESSION_ID, sessionId),
            )
        }

        fun abandon(
            context: Context,
            sessionId: String,
        ) {
            context.startService(
                Intent(context, OfflineFocusTimerService::class.java)
                    .setAction(ACTION_ABANDON)
                    .putExtra(EXTRA_SESSION_ID, sessionId),
            )
        }
    }
}
