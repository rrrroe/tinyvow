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
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.rrrrz.tinyvow.MainActivity
import com.rrrrz.tinyvow.R
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.db.OfflineFocusAbandonReason
import com.rrrrz.tinyvow.data.db.OfflineFocusPauseReason
import com.rrrrz.tinyvow.data.db.OfflineFocusSessionStatus
import com.rrrrz.tinyvow.data.repository.OfflineFocusRepository
import com.rrrrz.tinyvow.data.repository.OfflineFocusSession
import com.rrrrz.tinyvow.data.repository.elapsedDurationMillisAt
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.i18n.AppText
import com.rrrrz.tinyvow.widget.OfflineFocusWidgetProvider
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
    private lateinit var preferences: ManagedAppPreferences
    private var timerJob: kotlinx.coroutines.Job? = null
    private var completionAlertJob: kotlinx.coroutines.Job? = null
    private var restNotificationJob: kotlinx.coroutines.Job? = null
    private var completionRingtone: Ringtone? = null
    private var screenReceiver: BroadcastReceiver? = null
    private var completionSignalReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        AppText.attach(applicationContext)
        repository = OfflineFocusRepository(applicationContext, AppDatabase.getDatabase(applicationContext))
        preferences = ManagedAppPreferences(applicationContext)
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_EARLY -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
                if (sessionId != null) {
                    val showCompletionAlert = intent.getBooleanExtra(EXTRA_SHOW_COMPLETION_ALERT, true)
                    serviceScope.launch {
                        val completedSession = repository.stopSessionEarly(sessionId)
                        OfflineFocusWidgetProvider.updateAllWidgets(this@OfflineFocusTimerService)
                        completedSession?.let {
                            if (
                                showCompletionAlert &&
                                (
                                    it.status == OfflineFocusSessionStatus.COMPLETED ||
                                        it.status == OfflineFocusSessionStatus.SETTLED ||
                                        (
                                            it.status == OfflineFocusSessionStatus.ABANDONED &&
                                                it.abandonedReason == OfflineFocusAbandonReason.BELOW_THRESHOLD
                                        )
                                )
                            ) {
                                sendCompletionAlert(it)
                                return@launch
                            }
                        }
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
                        clearCompletionAlert()
                        repository.abandonSession(sessionId)
                        OfflineFocusWidgetProvider.updateAllWidgets(this@OfflineFocusTimerService)
                        stopForegroundCompat()
                        stopSelf()
                    }
                }
                return START_NOT_STICKY
            }
            ACTION_STOP_COMPLETION_SIGNAL -> {
                stopCompletionSignal()
                if (restNotificationJob == null) {
                    stopSelf()
                }
                return START_STICKY
            }
            ACTION_DISMISS_COMPLETION_ALERT -> {
                clearCompletionAlert()
                stopForegroundCompat()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_PAUSE -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
                if (sessionId != null) {
                    serviceScope.launch {
                        repository.pauseSession(sessionId)
                        OfflineFocusWidgetProvider.updateAllWidgets(this@OfflineFocusTimerService)
                    }
                }
                return START_STICKY
            }
            ACTION_RESUME -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
                if (sessionId != null) {
                    serviceScope.launch {
                        repository.resumeSession(sessionId)
                        OfflineFocusWidgetProvider.updateAllWidgets(this@OfflineFocusTimerService)
                    }
                }
                return START_STICKY
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
                    OfflineFocusWidgetProvider.updateAllWidgets(this)
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
        clearCompletionAlert()
        unregisterScreenReceiver()
        serviceScope.cancel()
        OfflineFocusWidgetProvider.updateAllWidgets(this)
        super.onDestroy()
    }

    private fun startTimerLoop(sessionId: String) {
        timerJob?.cancel()
        timerJob =
            serviceScope.launch {
                while (isActive) {
                    val session = repository.getActiveSessionOnce()
                    if (session == null || session.id != sessionId) {
                        OfflineFocusWidgetProvider.updateAllWidgets(this@OfflineFocusTimerService)
                        stopForegroundCompat()
                        stopSelf()
                        return@launch
                    }
                    updateScreenReceiver(sessionId)
                    if (session.status == OfflineFocusSessionStatus.PAUSED) {
                        val elapsed = session.elapsedDurationMillisAt(System.currentTimeMillis())
                        val remaining = remainingMillis(session.plannedDurationMillis, elapsed)
                        startForeground(
                            NOTIFICATION_ID,
                            buildNotification(
                                sessionId = sessionId,
                                categoryName = session.categoryName,
                                elapsedMillis = elapsed,
                                remainingMillis = remaining,
                                plannedDurationMillis = session.plannedDurationMillis,
                                paused = true,
                            ),
                        )
                        delay(1_000L)
                        continue
                    }
                    if (session.status != OfflineFocusSessionStatus.RUNNING) {
                        delay(1_000L)
                        continue
                    }
                    val now = System.currentTimeMillis()
                    val elapsed = session.elapsedDurationMillisAt(now)
                    val remaining = remainingMillis(session.plannedDurationMillis, elapsed)
                    startForeground(
                        NOTIFICATION_ID,
                        buildNotification(
                            sessionId = sessionId,
                            categoryName = session.categoryName,
                            elapsedMillis = elapsed,
                            remainingMillis = remaining,
                            plannedDurationMillis = session.plannedDurationMillis,
                            paused = false,
                        ),
                    )
                    if (session.plannedDurationMillis > 0L && remaining <= 0L) {
                        repository.completeSession(sessionId, now)?.let { completedSession ->
                            OfflineFocusWidgetProvider.updateAllWidgets(this@OfflineFocusTimerService)
                            unregisterScreenReceiver()
                            sendCompletionAlert(completedSession)
                        }
                        return@launch
                    }
                    delay(1_000L)
                }
            }
    }

    private fun buildNotification(
        sessionId: String,
        categoryName: String,
        elapsedMillis: Long = 0L,
        remainingMillis: Long,
        plannedDurationMillis: Long = 0L,
        paused: Boolean,
        starting: Boolean = false,
    ): Notification {
        val textContext = AppText.localizedContext(this)
        val openIntent =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java)
                    .setAction(MainActivity.ACTION_OFFLINE_FOCUS_ACTIVE)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
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
        val pauseResumeIntent =
            PendingIntent.getService(
                this,
                4,
                Intent(this, OfflineFocusTimerService::class.java)
                    .setAction(if (paused) ACTION_RESUME else ACTION_PAUSE)
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
        val elapsedText = formatRemaining(elapsedMillis)
        val unlimited = plannedDurationMillis <= 0L && !starting
        val plannedText =
            if (unlimited) {
                textContext.getString(R.string.offline_focus_unlimited)
            } else {
                formatRemaining(plannedDurationMillis)
            }
        val remainingText = formatRemaining(remainingMillis)
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(textContext.getString(R.string.offline_focus_notification_title, categoryName))
            .setContentText(
                if (starting) {
                    textContext.getString(R.string.offline_focus_notification_starting_body)
                } else if (unlimited && paused) {
                    textContext.getString(R.string.offline_focus_notification_unlimited_paused_body, elapsedText)
                } else if (unlimited) {
                    textContext.getString(R.string.offline_focus_notification_unlimited_body, elapsedText)
                } else if (paused) {
                    textContext.getString(R.string.offline_focus_notification_paused_body, remainingText)
                } else {
                    textContext.getString(R.string.offline_focus_notification_body, remainingText)
                },
            )
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    if (unlimited) {
                        textContext.getString(R.string.offline_focus_notification_unlimited_detail, elapsedText)
                    } else {
                        textContext.getString(R.string.offline_focus_notification_detail, elapsedText, plannedText, remainingText)
                    },
                ),
            )
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(
                0,
                textContext.getString(
                    if (paused) {
                        R.string.offline_focus_resume
                    } else {
                        R.string.offline_focus_pause
                    },
                ),
                pauseResumeIntent,
            )
            .addAction(0, textContext.getString(R.string.offline_focus_end), stopIntent)
            .addAction(0, textContext.getString(R.string.offline_focus_abandon), abandonIntent)
        if (starting || plannedDurationMillis <= 0L) {
            builder.setProgress(0, 0, starting)
        } else {
            val maxSeconds = ((plannedDurationMillis + 999L) / 1000L).coerceAtLeast(1L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            val progressSeconds = ((elapsedMillis + 999L) / 1000L).coerceIn(0L, maxSeconds.toLong()).toInt()
            builder.setProgress(maxSeconds, progressSeconds, false)
        }
        return builder.build()
    }

    private suspend fun sendCompletionAlert(session: OfflineFocusSession) {
        startCompletionAlert(
            soundEnabled = preferences.getOfflineFocusRestReminderSoundEnabledOnce(),
            vibrationEnabled = preferences.getOfflineFocusRestReminderVibrationEnabledOnce(),
            ringtoneUri = preferences.getOfflineFocusRestReminderRingtoneUriOnce(),
        )
        startRestNotificationLoop(session)
    }

    private fun buildCompletionNotification(
        session: OfflineFocusSession,
        restMillis: Long,
    ): Notification {
        val textContext = AppText.localizedContext(this)
        val openIntent = buildCompletionPendingIntent(
            sessionId = session.id,
            action = MainActivity.ACTION_OFFLINE_FOCUS_COMPLETED_CLICK,
        )
        val fullScreenIntent = buildCompletionPendingIntent(
            sessionId = session.id,
            action = MainActivity.ACTION_OFFLINE_FOCUS_COMPLETED,
        )
        val stopAlertIntent =
            PendingIntent.getService(
                this,
                5,
                Intent(this, OfflineFocusTimerService::class.java)
                    .setAction(ACTION_STOP_COMPLETION_SIGNAL),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val completedText =
            textContext.getString(
                if (session.status == OfflineFocusSessionStatus.ABANDONED) {
                    R.string.offline_focus_ended_early_notification_body
                } else {
                    R.string.offline_focus_completed_notification_body
                },
                session.categoryName,
                (session.actualDurationMillis / 60_000L).toInt(),
            )
        val restText =
            textContext.getString(
                R.string.offline_focus_rest_notification_body,
                session.categoryName,
                formatRemaining(restMillis),
            )
        return NotificationCompat.Builder(this, COMPLETION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(textContext.getString(R.string.offline_focus_rest_notification_title))
            .setContentText(restText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$restText\n$completedText"))
            .setContentIntent(openIntent)
            .setFullScreenIntent(fullScreenIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .addAction(0, textContext.getString(R.string.offline_focus_alert_stop), stopAlertIntent)
            .build()
    }

    private fun startRestNotificationLoop(session: OfflineFocusSession) {
        restNotificationJob?.cancel()
        val restStartedAt = session.completedAt ?: session.abandonedAt ?: System.currentTimeMillis()
        restNotificationJob =
            serviceScope.launch {
                while (isActive) {
                    val restMillis = (System.currentTimeMillis() - restStartedAt).coerceAtLeast(0L)
                    startForeground(COMPLETION_NOTIFICATION_ID, buildCompletionNotification(session, restMillis))
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.cancel(NOTIFICATION_ID)
                    delay(1_000L)
                }
            }
    }

    private fun buildCompletionPendingIntent(
        sessionId: String,
        action: String,
    ): PendingIntent {
        val intent =
            Intent(this, MainActivity::class.java)
                .setAction(action)
                .putExtra(MainActivity.EXTRA_OFFLINE_FOCUS_SESSION_ID, sessionId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(
            this,
            if (action == MainActivity.ACTION_OFFLINE_FOCUS_COMPLETED_CLICK) 6 else 3,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun startCompletionAlert(
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
        ringtoneUri: String?,
    ) {
        clearCompletionAlert()
        if (vibrationEnabled) {
            playCompletionVibration(loop = true)
        }
        if (soundEnabled) {
            playCompletionSound(ringtoneUri)
        }
        if (soundEnabled || vibrationEnabled) {
            registerCompletionSignalReceiver()
        }
    }

    private fun stopCompletionSignal() {
        completionAlertJob?.cancel()
        completionAlertJob = null
        runCatching { completionRingtone?.stop() }
        completionRingtone = null
        runCatching { completionVibrator().cancel() }
        unregisterCompletionSignalReceiver()
    }

    private fun clearCompletionAlert() {
        restNotificationJob?.cancel()
        restNotificationJob = null
        stopCompletionSignal()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(COMPLETION_NOTIFICATION_ID)
    }

    private fun playCompletionSound(ringtoneUri: String?) {
        val uri =
            ringtoneUri
                ?.takeIf { it.isNotBlank() }
                ?.let(Uri::parse)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: return
        val ringtone = runCatching { RingtoneManager.getRingtone(this, uri) }.getOrNull() ?: return
        completionRingtone = ringtone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone.isLooping = true
            ringtone.play()
        } else {
            completionAlertJob =
                serviceScope.launch {
                    while (isActive) {
                        if (!ringtone.isPlaying) {
                            ringtone.play()
                        }
                        delay(1_500L)
                    }
                }
        }
    }

    private fun playCompletionVibration(loop: Boolean) {
        val vibrator = completionVibrator()
        val pattern = longArrayOf(0L, 180L, 90L, 180L, 420L)
        val repeatIndex = if (loop) 0 else -1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeatIndex))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, repeatIndex)
        }
    }

    private fun completionVibrator(): Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

    private fun registerCompletionSignalReceiver() {
        if (completionSignalReceiver != null) return
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    when (intent?.action) {
                        Intent.ACTION_SCREEN_ON,
                        Intent.ACTION_USER_PRESENT -> stopCompletionSignal()
                    }
                }
            }
        completionSignalReceiver = receiver
        val filter =
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
    }

    private fun unregisterCompletionSignalReceiver() {
        val receiver = completionSignalReceiver ?: return
        runCatching { unregisterReceiver(receiver) }
        completionSignalReceiver = null
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
        val completionChannel =
            NotificationChannel(
                COMPLETION_CHANNEL_ID,
                textContext.getString(R.string.offline_focus_completed_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = textContext.getString(R.string.offline_focus_completed_channel_desc)
                enableVibration(false)
                setSound(null, null)
            }
        manager.createNotificationChannel(completionChannel)
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

    private fun remainingMillis(plannedDurationMillis: Long, elapsedMillis: Long): Long =
        if (plannedDurationMillis <= 0L) 0L else (plannedDurationMillis - elapsedMillis).coerceAtLeast(0L)

    companion object {
        const val ACTION_START = "com.rrrrz.tinyvow.offline_focus.START"
        const val ACTION_STOP_EARLY = "com.rrrrz.tinyvow.offline_focus.STOP_EARLY"
        const val ACTION_ABANDON = "com.rrrrz.tinyvow.offline_focus.ABANDON"
        const val ACTION_STOP_COMPLETION_SIGNAL = "com.rrrrz.tinyvow.offline_focus.STOP_COMPLETION_SIGNAL"
        const val ACTION_DISMISS_COMPLETION_ALERT = "com.rrrrz.tinyvow.offline_focus.DISMISS_COMPLETION_ALERT"
        const val ACTION_PAUSE = "com.rrrrz.tinyvow.offline_focus.PAUSE"
        const val ACTION_RESUME = "com.rrrrz.tinyvow.offline_focus.RESUME"
        const val EXTRA_SESSION_ID = "session_id"
        const val EXTRA_SHOW_COMPLETION_ALERT = "show_completion_alert"
        const val CHANNEL_ID = "offline_focus_timer"
        const val COMPLETION_CHANNEL_ID = "offline_focus_completed_signal"
        private const val NOTIFICATION_ID = 20_260_601
        private const val COMPLETION_NOTIFICATION_ID = 20_260_602

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
            showCompletionAlert: Boolean = true,
        ) {
            context.startService(
                Intent(context, OfflineFocusTimerService::class.java)
                    .setAction(ACTION_STOP_EARLY)
                    .putExtra(EXTRA_SESSION_ID, sessionId)
                    .putExtra(EXTRA_SHOW_COMPLETION_ALERT, showCompletionAlert),
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

        fun pause(
            context: Context,
            sessionId: String,
        ) {
            context.startService(
                Intent(context, OfflineFocusTimerService::class.java)
                    .setAction(ACTION_PAUSE)
                    .putExtra(EXTRA_SESSION_ID, sessionId),
            )
        }

        fun resume(
            context: Context,
            sessionId: String,
        ) {
            context.startService(
                Intent(context, OfflineFocusTimerService::class.java)
                    .setAction(ACTION_RESUME)
                    .putExtra(EXTRA_SESSION_ID, sessionId),
            )
        }

        fun dismissCompletionAlert(context: Context) {
            context.startService(
                Intent(context, OfflineFocusTimerService::class.java)
                    .setAction(ACTION_DISMISS_COMPLETION_ALERT),
            )
        }

        fun stopCompletionSignal(context: Context) {
            context.startService(
                Intent(context, OfflineFocusTimerService::class.java)
                    .setAction(ACTION_STOP_COMPLETION_SIGNAL),
            )
        }
    }
}
