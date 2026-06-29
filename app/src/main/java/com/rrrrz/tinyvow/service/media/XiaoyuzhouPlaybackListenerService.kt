package com.rrrrz.tinyvow.service.media

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.rrrrz.tinyvow.data.time.BusinessDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.ZoneId

enum class XiaoyuzhouPlaybackStatus {
    UNKNOWN,
    PLAYING,
    PAUSED,
    STOPPED,
    BUFFERING,
}

enum class XiaoyuzhouPlaybackEventSource {
    SERVICE,
    MEDIA_SESSION,
    NOTIFICATION,
}

data class XiaoyuzhouPlaybackSnapshot(
    val targetPackageNames: List<String> = XiaoyuzhouPlaybackMonitor.targetPackageNames,
    val listenerConnected: Boolean = false,
    val targetSessionVisible: Boolean = false,
    val notificationVisible: Boolean = false,
    val status: XiaoyuzhouPlaybackStatus = XiaoyuzhouPlaybackStatus.UNKNOWN,
    val detectedPackageName: String? = null,
    val mediaTitle: String? = null,
    val mediaSubtitle: String? = null,
    val todayDate: String? = null,
    val todayAccumulatedPlaybackMillis: Long = 0L,
    val activeStartedAtMillis: Long? = null,
    val lastConfirmedAtMillis: Long? = null,
    val todayUntrustedPlaybackMillis: Long = 0L,
    val lastEventAtMillis: Long? = null,
    val lastNotificationAtMillis: Long? = null,
    val lastEventSource: XiaoyuzhouPlaybackEventSource = XiaoyuzhouPlaybackEventSource.SERVICE,
)

object XiaoyuzhouPlaybackMonitor {
    const val primaryPackageName: String = "app.podcast.cosmos"
    const val ACTIVE_SESSION_POLL_INTERVAL_MILLIS: Long = 60_000L
    const val TRUSTED_RECONNECT_WINDOW_MILLIS: Long = 30L * 60_000L
    val targetPackageNames: List<String> = listOf(primaryPackageName)

    private const val PREFS_NAME = "xiaoyuzhou_playback_monitor"
    private const val KEY_DATE = "date"
    private const val KEY_ACCUMULATED_MILLIS = "accumulated_millis"
    private const val KEY_UNTRUSTED_MILLIS = "untrusted_millis"
    private const val KEY_ACTIVE_STARTED_AT = "active_started_at"
    private const val KEY_LAST_CONFIRMED_AT = "last_confirmed_at"
    private const val KEY_PLAYING = "playing"

    private val _state = MutableStateFlow(XiaoyuzhouPlaybackSnapshot())
    val state: StateFlow<XiaoyuzhouPlaybackSnapshot> = _state.asStateFlow()

    fun isNotificationListenerEnabled(context: Context): Boolean {
        val componentName = ComponentName(context, XiaoyuzhouPlaybackListenerService::class.java)
        val enabledListeners =
            Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
                .orEmpty()
        return enabledListeners
            .split(':')
            .mapNotNull(ComponentName::unflattenFromString)
            .any { it.packageName == componentName.packageName && it.className == componentName.className }
    }

    fun requestListenerRebind(context: Context) {
        val componentName = ComponentName(context, XiaoyuzhouPlaybackListenerService::class.java)
        runCatching {
            NotificationListenerService.requestRebind(componentName)
        }
    }

    fun refreshStoredState(context: Context) {
        val stored = normalizeStoredState(context, System.currentTimeMillis())
        _state.update {
            it.copy(
                todayDate = stored.date,
                todayAccumulatedPlaybackMillis = stored.accumulatedMillis,
                todayUntrustedPlaybackMillis = stored.untrustedMillis,
                activeStartedAtMillis = stored.activeStartedAtMillis,
                lastConfirmedAtMillis = stored.lastConfirmedAtMillis,
            )
        }
    }

    fun resetToday(context: Context) {
        val now = System.currentTimeMillis()
        val date = businessDate(now)
        prefs(context)
            .edit()
            .putString(KEY_DATE, date)
            .putLong(KEY_ACCUMULATED_MILLIS, 0L)
            .putLong(KEY_UNTRUSTED_MILLIS, 0L)
            .remove(KEY_ACTIVE_STARTED_AT)
            .remove(KEY_LAST_CONFIRMED_AT)
            .putBoolean(KEY_PLAYING, false)
            .apply()
        _state.update {
            it.copy(
                status = XiaoyuzhouPlaybackStatus.UNKNOWN,
                todayDate = date,
                todayAccumulatedPlaybackMillis = 0L,
                todayUntrustedPlaybackMillis = 0L,
                activeStartedAtMillis = null,
                lastConfirmedAtMillis = null,
                lastEventAtMillis = now,
                lastEventSource = XiaoyuzhouPlaybackEventSource.SERVICE,
            )
        }
    }

    fun markListenerConnected(
        context: Context,
        connected: Boolean,
    ) {
        val now = System.currentTimeMillis()
        val stored =
            if (connected) {
                normalizeStoredState(context, now)
            } else {
                stopPlayback(context, normalizeStoredState(context, now), now, countGapSinceLastConfirmation = true)
            }
        _state.update {
            it.copy(
                listenerConnected = connected,
                todayDate = stored.date,
                todayAccumulatedPlaybackMillis = stored.accumulatedMillis,
                todayUntrustedPlaybackMillis = stored.untrustedMillis,
                activeStartedAtMillis = stored.activeStartedAtMillis,
                lastConfirmedAtMillis = stored.lastConfirmedAtMillis,
                status = if (connected) it.status else XiaoyuzhouPlaybackStatus.UNKNOWN,
                lastEventAtMillis = now,
                lastEventSource = XiaoyuzhouPlaybackEventSource.SERVICE,
            )
        }
    }

    fun markNotification(
        context: Context,
        packageName: String,
        visible: Boolean,
    ) {
        if (packageName !in targetPackageNames) return
        val now = System.currentTimeMillis()
        val stored = normalizeStoredState(context, now)
        _state.update {
            it.copy(
                notificationVisible = visible,
                detectedPackageName = packageName,
                todayDate = stored.date,
                todayAccumulatedPlaybackMillis = stored.accumulatedMillis,
                todayUntrustedPlaybackMillis = stored.untrustedMillis,
                activeStartedAtMillis = stored.activeStartedAtMillis,
                lastConfirmedAtMillis = stored.lastConfirmedAtMillis,
                lastNotificationAtMillis = now,
                lastEventAtMillis = now,
                lastEventSource = XiaoyuzhouPlaybackEventSource.NOTIFICATION,
            )
        }
    }

    fun updateFromController(
        context: Context,
        controller: MediaController,
        source: XiaoyuzhouPlaybackEventSource = XiaoyuzhouPlaybackEventSource.MEDIA_SESSION,
        initialSyncAfterListenerConnected: Boolean = false,
    ) {
        val now = System.currentTimeMillis()
        val status = controller.playbackState.toPlaybackStatus()
        val stored =
            when (status) {
                XiaoyuzhouPlaybackStatus.PLAYING ->
                    confirmPlaying(
                        context = context,
                        now = now,
                        allowReconnectEstimate = initialSyncAfterListenerConnected,
                    )
                else ->
                    stopPlayback(
                        context = context,
                        stored = normalizeStoredState(context, now),
                        now = now,
                        countGapSinceLastConfirmation = !initialSyncAfterListenerConnected,
                    )
            }
        val metadata = controller.metadata
        _state.update {
            it.copy(
                listenerConnected = true,
                targetSessionVisible = true,
                status = status,
                detectedPackageName = controller.packageName,
                mediaTitle = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE),
                mediaSubtitle =
                    metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                        ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM)
                        ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE),
                todayDate = stored.date,
                todayAccumulatedPlaybackMillis = stored.accumulatedMillis,
                todayUntrustedPlaybackMillis = stored.untrustedMillis,
                activeStartedAtMillis = stored.activeStartedAtMillis,
                lastConfirmedAtMillis = stored.lastConfirmedAtMillis,
                lastEventAtMillis = now,
                lastEventSource = source,
            )
        }
    }

    fun markNoTargetSession(
        context: Context,
        initialSyncAfterListenerConnected: Boolean = false,
    ) {
        val now = System.currentTimeMillis()
        val stored =
            stopPlayback(
                context = context,
                stored = normalizeStoredState(context, now),
                now = now,
                countGapSinceLastConfirmation = !initialSyncAfterListenerConnected,
            )
        _state.update {
            it.copy(
                targetSessionVisible = false,
                status = XiaoyuzhouPlaybackStatus.UNKNOWN,
                mediaTitle = null,
                mediaSubtitle = null,
                todayDate = stored.date,
                todayAccumulatedPlaybackMillis = stored.accumulatedMillis,
                todayUntrustedPlaybackMillis = stored.untrustedMillis,
                activeStartedAtMillis = stored.activeStartedAtMillis,
                lastConfirmedAtMillis = stored.lastConfirmedAtMillis,
                lastEventAtMillis = now,
                lastEventSource = XiaoyuzhouPlaybackEventSource.MEDIA_SESSION,
            )
        }
    }

    private fun confirmPlaying(
        context: Context,
        now: Long,
        allowReconnectEstimate: Boolean,
    ): StoredPlaybackState {
        val stored = normalizeStoredState(context, now)
        if (!stored.playing) {
            return startPlayback(context, stored, now)
        }

        val delta = (now - (stored.lastConfirmedAtMillis ?: now)).coerceAtLeast(0L)
        val shouldCountDelta = delta <= TRUSTED_RECONNECT_WINDOW_MILLIS || allowReconnectEstimate
        val trustedDelta =
            if (shouldCountDelta && delta <= TRUSTED_RECONNECT_WINDOW_MILLIS) {
                delta
            } else {
                0L
            }
        val untrustedDelta = if (trustedDelta == 0L && delta > 0L) delta else 0L
        val next =
            stored.copy(
                accumulatedMillis = stored.accumulatedMillis + trustedDelta,
                untrustedMillis = stored.untrustedMillis + untrustedDelta,
                lastConfirmedAtMillis = now,
            )
        saveState(context, next)
        return next
    }

    private fun startPlayback(
        context: Context,
        stored: StoredPlaybackState,
        now: Long,
    ): StoredPlaybackState {
        val next =
            stored.copy(
                activeStartedAtMillis = now,
                lastConfirmedAtMillis = now,
                playing = true,
            )
        saveState(context, next)
        return next
    }

    private fun stopPlayback(
        context: Context,
        stored: StoredPlaybackState,
        now: Long,
        countGapSinceLastConfirmation: Boolean,
    ): StoredPlaybackState {
        if (!stored.playing) return stored
        val delta = (now - (stored.lastConfirmedAtMillis ?: now)).coerceAtLeast(0L)
        val trustedDelta =
            if (countGapSinceLastConfirmation && delta <= TRUSTED_RECONNECT_WINDOW_MILLIS) {
                delta
            } else {
                0L
            }
        val untrustedDelta = if (trustedDelta == 0L && delta > 0L) delta else 0L
        val next =
            stored.copy(
                accumulatedMillis = stored.accumulatedMillis + trustedDelta,
                untrustedMillis = stored.untrustedMillis + untrustedDelta,
                activeStartedAtMillis = null,
                lastConfirmedAtMillis = null,
                playing = false,
            )
        saveState(context, next)
        return next
    }

    private fun saveState(
        context: Context,
        state: StoredPlaybackState,
    ) {
        prefs(context)
            .edit()
            .putString(KEY_DATE, state.date)
            .putLong(KEY_ACCUMULATED_MILLIS, state.accumulatedMillis)
            .putLong(KEY_UNTRUSTED_MILLIS, state.untrustedMillis)
            .putBoolean(KEY_PLAYING, state.playing)
            .apply {
                if (state.playing && state.activeStartedAtMillis != null && state.lastConfirmedAtMillis != null) {
                    putLong(KEY_ACTIVE_STARTED_AT, state.activeStartedAtMillis)
                    putLong(KEY_LAST_CONFIRMED_AT, state.lastConfirmedAtMillis)
                } else {
                    remove(KEY_ACTIVE_STARTED_AT)
                    remove(KEY_LAST_CONFIRMED_AT)
                }
            }
            .apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun businessDate(nowMillis: Long): String =
        BusinessDay.today(ZoneId.systemDefault(), BusinessDay.cachedStartHour(), nowMillis).toString()

    private fun businessDayStartMillis(nowMillis: Long): Long {
        val date = BusinessDay.today(ZoneId.systemDefault(), BusinessDay.cachedStartHour(), nowMillis)
        return BusinessDay.startOfDayMillis(date, ZoneId.systemDefault(), BusinessDay.cachedStartHour())
    }

    private fun normalizeStoredState(
        context: Context,
        now: Long,
    ): StoredPlaybackState {
        val prefs = prefs(context)
        val today = businessDate(now)
        val storedDate = prefs.getString(KEY_DATE, null)
        val wasPlaying = prefs.getBoolean(KEY_PLAYING, false)
        if (storedDate != today) {
            val carriedStart = if (wasPlaying) businessDayStartMillis(now).coerceAtMost(now) else null
            val state =
                StoredPlaybackState(
                    date = today,
                    accumulatedMillis = 0L,
                    untrustedMillis = 0L,
                    activeStartedAtMillis = carriedStart,
                    lastConfirmedAtMillis = carriedStart,
                    playing = wasPlaying,
                )
            saveState(context, state)
            return state
        }
        val activeStartedAt =
            prefs.getLong(KEY_ACTIVE_STARTED_AT, 0L)
                .takeIf { it > 0L && wasPlaying }
        val lastConfirmedAt =
            prefs.getLong(KEY_LAST_CONFIRMED_AT, 0L)
                .takeIf { it > 0L && wasPlaying }
        return StoredPlaybackState(
            date = today,
            accumulatedMillis = prefs.getLong(KEY_ACCUMULATED_MILLIS, 0L).coerceAtLeast(0L),
            untrustedMillis = prefs.getLong(KEY_UNTRUSTED_MILLIS, 0L).coerceAtLeast(0L),
            activeStartedAtMillis = activeStartedAt,
            lastConfirmedAtMillis = lastConfirmedAt,
            playing = wasPlaying,
        )
    }

    private fun PlaybackState?.toPlaybackStatus(): XiaoyuzhouPlaybackStatus =
        when (this?.state) {
            PlaybackState.STATE_PLAYING -> XiaoyuzhouPlaybackStatus.PLAYING
            PlaybackState.STATE_PAUSED -> XiaoyuzhouPlaybackStatus.PAUSED
            PlaybackState.STATE_STOPPED -> XiaoyuzhouPlaybackStatus.STOPPED
            PlaybackState.STATE_BUFFERING,
            PlaybackState.STATE_CONNECTING,
            -> XiaoyuzhouPlaybackStatus.BUFFERING
            else -> XiaoyuzhouPlaybackStatus.UNKNOWN
        }

    private data class StoredPlaybackState(
        val date: String,
        val accumulatedMillis: Long,
        val untrustedMillis: Long,
        val activeStartedAtMillis: Long?,
        val lastConfirmedAtMillis: Long?,
        val playing: Boolean,
    )
}

class XiaoyuzhouPlaybackListenerService : NotificationListenerService() {
    private val mediaSessionManager by lazy {
        getSystemService(MediaSessionManager::class.java)
    }
    private val listenerComponent by lazy {
        ComponentName(this, XiaoyuzhouPlaybackListenerService::class.java)
    }
    private val handler = Handler(Looper.getMainLooper())
    private val activeSessionListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            syncActiveSessions(
                controllers = controllers.orEmpty(),
                initialSyncAfterListenerConnected = false,
            )
        }
    private val activeSessionPoll =
        object : Runnable {
            override fun run() {
                refreshActiveNotifications()
                refreshActiveSessions(initialSyncAfterListenerConnected = false)
                handler.postDelayed(this, XiaoyuzhouPlaybackMonitor.ACTIVE_SESSION_POLL_INTERVAL_MILLIS)
            }
        }
    private val registrations = mutableMapOf<Any, ControllerRegistration>()
    private var activeSessionListenerRegistered = false
    private var initialSessionSyncPending = false

    override fun onCreate() {
        super.onCreate()
        XiaoyuzhouPlaybackMonitor.refreshStoredState(this)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        initialSessionSyncPending = true
        XiaoyuzhouPlaybackMonitor.markListenerConnected(this, connected = true)
        registerActiveSessionListener()
        refreshActiveNotifications()
        refreshActiveSessions(initialSyncAfterListenerConnected = true)
        startActiveSessionPolling()
    }

    override fun onListenerDisconnected() {
        stopActiveSessionPolling()
        XiaoyuzhouPlaybackMonitor.markListenerConnected(this, connected = false)
        unregisterControllers()
        unregisterActiveSessionListener()
        super.onListenerDisconnected()
    }

    override fun onDestroy() {
        stopActiveSessionPolling()
        unregisterControllers()
        unregisterActiveSessionListener()
        XiaoyuzhouPlaybackMonitor.markListenerConnected(this, connected = false)
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName ?: return
        if (packageName in XiaoyuzhouPlaybackMonitor.targetPackageNames) {
            XiaoyuzhouPlaybackMonitor.markNotification(this, packageName, visible = true)
            refreshActiveSessions(initialSyncAfterListenerConnected = false)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName ?: return
        if (packageName in XiaoyuzhouPlaybackMonitor.targetPackageNames) {
            XiaoyuzhouPlaybackMonitor.markNotification(this, packageName, visible = false)
            refreshActiveSessions(initialSyncAfterListenerConnected = false)
        }
    }

    private fun registerActiveSessionListener() {
        if (activeSessionListenerRegistered) return
        runCatching {
            mediaSessionManager.addOnActiveSessionsChangedListener(activeSessionListener, listenerComponent)
            activeSessionListenerRegistered = true
        }
    }

    private fun unregisterActiveSessionListener() {
        if (!activeSessionListenerRegistered) return
        runCatching {
            mediaSessionManager.removeOnActiveSessionsChangedListener(activeSessionListener)
        }
        activeSessionListenerRegistered = false
    }

    private fun startActiveSessionPolling() {
        handler.removeCallbacks(activeSessionPoll)
        handler.postDelayed(activeSessionPoll, XiaoyuzhouPlaybackMonitor.ACTIVE_SESSION_POLL_INTERVAL_MILLIS)
    }

    private fun stopActiveSessionPolling() {
        handler.removeCallbacks(activeSessionPoll)
    }

    private fun refreshActiveSessions(initialSyncAfterListenerConnected: Boolean) {
        runCatching {
            mediaSessionManager.getActiveSessions(listenerComponent)
        }.onSuccess { controllers ->
            syncActiveSessions(controllers, initialSyncAfterListenerConnected)
        }.onFailure {
            XiaoyuzhouPlaybackMonitor.markNoTargetSession(
                context = this,
                initialSyncAfterListenerConnected = initialSyncAfterListenerConnected,
            )
        }
    }

    private fun refreshActiveNotifications() {
        val hasTargetNotification =
            runCatching {
                activeNotifications.orEmpty()
                    .any { it.packageName in XiaoyuzhouPlaybackMonitor.targetPackageNames }
            }.getOrDefault(false)
        XiaoyuzhouPlaybackMonitor.markNotification(
            context = this,
            packageName = XiaoyuzhouPlaybackMonitor.primaryPackageName,
            visible = hasTargetNotification,
        )
    }

    private fun syncActiveSessions(
        controllers: List<MediaController>,
        initialSyncAfterListenerConnected: Boolean,
    ) {
        val targetControllers =
            controllers.filter { it.packageName in XiaoyuzhouPlaybackMonitor.targetPackageNames }
        val targetTokens = targetControllers.map { it.sessionToken }.toSet()

        registrations
            .filterKeys { it !in targetTokens }
            .values
            .forEach { it.controller.unregisterCallback(it.callback) }
        registrations.keys.removeAll { it !in targetTokens }

        targetControllers.forEach { controller ->
            if (controller.sessionToken !in registrations) {
                val callback =
                    object : MediaController.Callback() {
                        override fun onPlaybackStateChanged(state: PlaybackState?) {
                            XiaoyuzhouPlaybackMonitor.updateFromController(
                                context = this@XiaoyuzhouPlaybackListenerService,
                                controller = controller,
                                initialSyncAfterListenerConnected = false,
                            )
                        }

                        override fun onMetadataChanged(metadata: MediaMetadata?) {
                            XiaoyuzhouPlaybackMonitor.updateFromController(
                                context = this@XiaoyuzhouPlaybackListenerService,
                                controller = controller,
                                initialSyncAfterListenerConnected = false,
                            )
                        }

                        override fun onSessionDestroyed() {
                            refreshActiveSessions(initialSyncAfterListenerConnected = false)
                        }
                    }
                controller.registerCallback(callback)
                registrations[controller.sessionToken] = ControllerRegistration(controller, callback)
            }
        }

        val selectedController =
            targetControllers.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
                ?: targetControllers.firstOrNull()
        val isInitialSync = initialSyncAfterListenerConnected && initialSessionSyncPending
        if (selectedController != null) {
            XiaoyuzhouPlaybackMonitor.updateFromController(
                context = this,
                controller = selectedController,
                initialSyncAfterListenerConnected = isInitialSync,
            )
        } else {
            XiaoyuzhouPlaybackMonitor.markNoTargetSession(
                context = this,
                initialSyncAfterListenerConnected = isInitialSync,
            )
        }
        if (initialSyncAfterListenerConnected) {
            initialSessionSyncPending = false
        }
    }

    private fun unregisterControllers() {
        registrations.values.forEach { registration ->
            registration.controller.unregisterCallback(registration.callback)
        }
        registrations.clear()
    }

    private data class ControllerRegistration(
        val controller: MediaController,
        val callback: MediaController.Callback,
    )
}
