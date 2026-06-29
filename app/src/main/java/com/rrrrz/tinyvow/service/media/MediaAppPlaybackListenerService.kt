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
import com.rrrrz.tinyvow.data.db.MediaAppPlaybackStatus
import com.rrrrz.tinyvow.data.media.MediaAppPlaybackAccountant
import com.rrrrz.tinyvow.data.media.MediaAppPlaybackRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MediaAppPlaybackListenerSnapshot(
    val listenerConnected: Boolean = false,
    val sessionPackages: Set<String> = emptySet(),
    val notificationPackages: Set<String> = emptySet(),
    val statuses: Map<String, MediaAppPlaybackStatus> = emptyMap(),
    val detectedPackageName: String? = null,
    val mediaTitle: String? = null,
    val mediaSubtitle: String? = null,
    val lastEventAtMillis: Long? = null,
)

object MediaAppPlaybackMonitor {
    const val ACTIVE_SESSION_POLL_INTERVAL_MILLIS: Long = 60_000L
    const val TRUSTED_RECONNECT_WINDOW_MILLIS: Long = MediaAppPlaybackAccountant.TRUSTED_RECONNECT_WINDOW_MILLIS

    private val _state = MutableStateFlow(MediaAppPlaybackListenerSnapshot())
    val state: StateFlow<MediaAppPlaybackListenerSnapshot> = _state.asStateFlow()

    fun isNotificationListenerEnabled(context: Context): Boolean {
        val componentName = ComponentName(context, MediaAppPlaybackListenerService::class.java)
        val enabledListeners =
            Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
                .orEmpty()
        return enabledListeners
            .split(':')
            .mapNotNull(ComponentName::unflattenFromString)
            .any { it.packageName == componentName.packageName && it.className == componentName.className }
    }

    fun requestListenerRebind(context: Context) {
        val componentName = ComponentName(context, MediaAppPlaybackListenerService::class.java)
        runCatching {
            NotificationListenerService.requestRebind(componentName)
        }
    }

    fun markListenerConnected(connected: Boolean) {
        val now = System.currentTimeMillis()
        _state.update {
            it.copy(
                listenerConnected = connected,
                sessionPackages = if (connected) it.sessionPackages else emptySet(),
                statuses = if (connected) it.statuses else emptyMap(),
                detectedPackageName = if (connected) it.detectedPackageName else null,
                mediaTitle = if (connected) it.mediaTitle else null,
                mediaSubtitle = if (connected) it.mediaSubtitle else null,
                lastEventAtMillis = now,
            )
        }
    }

    fun markNotifications(packageNames: Set<String>) {
        _state.update {
            it.copy(
                notificationPackages = packageNames,
                lastEventAtMillis = System.currentTimeMillis(),
            )
        }
    }

    fun markPackageStatus(
        packageName: String,
        status: MediaAppPlaybackStatus,
        sessionVisible: Boolean,
        metadata: MediaMetadata?,
    ) {
        val now = System.currentTimeMillis()
        _state.update { current ->
            val sessions =
                if (sessionVisible) {
                    current.sessionPackages + packageName
                } else {
                    current.sessionPackages - packageName
                }
            current.copy(
                listenerConnected = true,
                sessionPackages = sessions,
                statuses = current.statuses + (packageName to status),
                detectedPackageName = packageName,
                mediaTitle = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE),
                mediaSubtitle =
                    metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                        ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM)
                        ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE),
                lastEventAtMillis = now,
            )
        }
    }
}

class MediaAppPlaybackListenerService : NotificationListenerService() {
    private val mediaSessionManager by lazy {
        getSystemService(MediaSessionManager::class.java)
    }
    private val listenerComponent by lazy {
        ComponentName(this, MediaAppPlaybackListenerService::class.java)
    }
    private val repository by lazy {
        MediaAppPlaybackRepository(applicationContext)
    }
    private val handler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
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
                handler.postDelayed(this, MediaAppPlaybackMonitor.ACTIVE_SESSION_POLL_INTERVAL_MILLIS)
            }
        }
    private val registrations = mutableMapOf<Any, ControllerRegistration>()
    private var activeSessionListenerRegistered = false
    private var initialSessionSyncPending = false

    override fun onListenerConnected() {
        super.onListenerConnected()
        initialSessionSyncPending = true
        MediaAppPlaybackMonitor.markListenerConnected(connected = true)
        registerActiveSessionListener()
        refreshActiveNotifications()
        refreshActiveSessions(initialSyncAfterListenerConnected = true)
        startActiveSessionPolling()
    }

    override fun onListenerDisconnected() {
        handleListenerDisconnected()
        super.onListenerDisconnected()
    }

    override fun onDestroy() {
        handleListenerDisconnected()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName ?: return
        serviceScope.launch {
            if (packageName in repository.getEnabledPackageNames()) {
                refreshActiveNotifications()
                refreshActiveSessions(initialSyncAfterListenerConnected = false)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName ?: return
        serviceScope.launch {
            if (packageName in repository.getEnabledPackageNames()) {
                refreshActiveNotifications()
                refreshActiveSessions(initialSyncAfterListenerConnected = false)
            }
        }
    }

    private fun handleListenerDisconnected() {
        stopActiveSessionPolling()
        unregisterControllers()
        unregisterActiveSessionListener()
        MediaAppPlaybackMonitor.markListenerConnected(connected = false)
        serviceScope.launch {
            repository.markListenerDisconnected()
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
        handler.postDelayed(activeSessionPoll, MediaAppPlaybackMonitor.ACTIVE_SESSION_POLL_INTERVAL_MILLIS)
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
            serviceScope.launch {
                val activePackages = repository.getEnabledPackageNames()
                activePackages.forEach { packageName ->
                    repository.recordPlaybackStatus(
                        packageName = packageName,
                        status = MediaAppPlaybackStatus.NO_SESSION,
                        countGapSinceLastConfirmation = !initialSyncAfterListenerConnected,
                    )
                    MediaAppPlaybackMonitor.markPackageStatus(
                        packageName = packageName,
                        status = MediaAppPlaybackStatus.NO_SESSION,
                        sessionVisible = false,
                        metadata = null,
                    )
                }
            }
        }
    }

    private fun refreshActiveNotifications() {
        serviceScope.launch {
            val activePackages = repository.getEnabledPackageNames().toSet()
            val notificationPackages =
                runCatching {
                    activeNotifications.orEmpty()
                        .map { it.packageName }
                        .filter { it in activePackages }
                        .toSet()
                }.getOrDefault(emptySet())
            MediaAppPlaybackMonitor.markNotifications(notificationPackages)
        }
    }

    private fun syncActiveSessions(
        controllers: List<MediaController>,
        initialSyncAfterListenerConnected: Boolean,
    ) {
        serviceScope.launch {
            val activePackages = repository.getEnabledPackageNames().toSet()
            val targetControllers = controllers.filter { it.packageName in activePackages }
            withContext(Dispatchers.Main.immediate) {
                registerControllerCallbacks(targetControllers)
            }

            val isInitialSync = initialSyncAfterListenerConnected && initialSessionSyncPending
            activePackages.forEach { packageName ->
                val packageControllers = targetControllers.filter { it.packageName == packageName }
                val selectedController =
                    packageControllers.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
                        ?: packageControllers.firstOrNull()
                if (selectedController == null) {
                    repository.recordPlaybackStatus(
                        packageName = packageName,
                        status = MediaAppPlaybackStatus.NO_SESSION,
                        countGapSinceLastConfirmation = !isInitialSync,
                    )
                    MediaAppPlaybackMonitor.markPackageStatus(
                        packageName = packageName,
                        status = MediaAppPlaybackStatus.NO_SESSION,
                        sessionVisible = false,
                        metadata = null,
                    )
                } else {
                    val status = selectedController.playbackState.toPlaybackStatus()
                    repository.recordPlaybackStatus(
                        packageName = packageName,
                        status = status,
                        countGapSinceLastConfirmation = !isInitialSync,
                    )
                    MediaAppPlaybackMonitor.markPackageStatus(
                        packageName = packageName,
                        status = status,
                        sessionVisible = true,
                        metadata = selectedController.metadata,
                    )
                }
            }
            if (initialSyncAfterListenerConnected) {
                initialSessionSyncPending = false
            }
        }
    }

    private fun registerControllerCallbacks(targetControllers: List<MediaController>) {
        val targetTokens = targetControllers.map { it.sessionToken }.toSet()
        registrations
            .filterKeys { it !in targetTokens }
            .values
            .forEach { it.controller.unregisterCallback(it.callback) }
        registrations.keys.removeAll { it !in targetTokens }

        targetControllers.forEach { controller ->
            if (controller.sessionToken in registrations) return@forEach
            val callback =
                object : MediaController.Callback() {
                    override fun onPlaybackStateChanged(state: PlaybackState?) {
                        refreshActiveSessions(initialSyncAfterListenerConnected = false)
                    }

                    override fun onMetadataChanged(metadata: MediaMetadata?) {
                        refreshActiveSessions(initialSyncAfterListenerConnected = false)
                    }

                    override fun onSessionDestroyed() {
                        refreshActiveSessions(initialSyncAfterListenerConnected = false)
                    }
                }
            controller.registerCallback(callback)
            registrations[controller.sessionToken] = ControllerRegistration(controller, callback)
        }
    }

    private fun unregisterControllers() {
        registrations.values.forEach { registration ->
            registration.controller.unregisterCallback(registration.callback)
        }
        registrations.clear()
    }

    private fun PlaybackState?.toPlaybackStatus(): MediaAppPlaybackStatus =
        when (this?.state) {
            PlaybackState.STATE_PLAYING -> MediaAppPlaybackStatus.PLAYING
            PlaybackState.STATE_PAUSED -> MediaAppPlaybackStatus.PAUSED
            PlaybackState.STATE_STOPPED -> MediaAppPlaybackStatus.STOPPED
            PlaybackState.STATE_BUFFERING,
            PlaybackState.STATE_CONNECTING,
            -> MediaAppPlaybackStatus.BUFFERING
            else -> MediaAppPlaybackStatus.UNKNOWN
        }

    private data class ControllerRegistration(
        val controller: MediaController,
        val callback: MediaController.Callback,
    )
}
