package com.rrrrz.tinyvow.data.supermode

import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import java.time.Instant
import java.time.ZoneId

class SuperModeController(
    private val preferences: ManagedAppPreferences,
    private val zoneIdProvider: () -> ZoneId = { ZoneId.systemDefault() },
    private val clock: () -> Long = { System.currentTimeMillis() },
) {
    companion object {
        const val DEFAULT_START_MINUTES = SuperModePolicy.DEFAULT_START_MINUTES
        const val DEFAULT_END_MINUTES = SuperModePolicy.DEFAULT_END_MINUTES
        const val IDLE_TIMEOUT_MILLIS = SuperModePolicy.IDLE_TIMEOUT_MILLIS
    }

    suspend fun currentStatus(isProActive: Boolean): SuperModeStatus =
        buildStatus(preferences.getSuperModeStateOnce(), isProActive, clock())

    fun buildStatus(
        storedState: SuperModeStoredState,
        isProActive: Boolean,
        nowMillis: Long = clock(),
    ): SuperModeStatus = SuperModePolicy.buildStatus(storedState, isProActive, nowMillis, zoneIdProvider())

    suspend fun enter(
        password: String,
        isProActive: Boolean,
    ): SuperModeEnterResult {
        val storedState = preferences.getSuperModeStateOnce()
        val status = buildStatus(storedState, isProActive)
        if (!status.isConfigured) return SuperModeEnterResult.NotConfigured
        if (!status.isAvailableNow) {
            preferences.setSuperModeActive(active = false, lastActiveAtMillis = null)
            return SuperModeEnterResult.OutsideAllowedWindow
        }
        val passwordSalt = storedState.passwordSalt ?: return SuperModeEnterResult.NotConfigured
        val passwordHash = storedState.passwordHash ?: return SuperModeEnterResult.NotConfigured
        if (!SuperModeCrypto.verifySecret(password, passwordSalt, passwordHash)) {
            return SuperModeEnterResult.IncorrectPassword
        }
        val now = clock()
        preferences.setSuperModeActive(active = true, lastActiveAtMillis = now)
        return SuperModeEnterResult.Success(currentStatus(isProActive))
    }

    suspend fun touch(isProActive: Boolean): SuperModeStatus {
        val status = currentStatus(isProActive)
        if (!status.isActive) {
            preferences.setSuperModeActive(active = false, lastActiveAtMillis = null)
            return currentStatus(isProActive)
        }
        val now = clock()
        preferences.touchSuperMode(now)
        return buildStatus(preferences.getSuperModeStateOnce(), isProActive, now)
    }

    suspend fun exit(reason: SuperModeExitReason = SuperModeExitReason.MANUAL) {
        reason
        preferences.setSuperModeActive(active = false, lastActiveAtMillis = null)
    }

    suspend fun updateCredentials(
        password: String,
        recoveryQuestion: String,
        recoveryAnswer: String,
    ) {
        val passwordSalt = SuperModeCrypto.newSalt()
        val recoverySalt = SuperModeCrypto.newSalt()
        preferences.saveSuperModeCredentials(
            passwordHash = SuperModeCrypto.hashSecret(password, passwordSalt),
            passwordSalt = passwordSalt,
            recoveryQuestion = recoveryQuestion.trim(),
            recoveryAnswerHash = SuperModeCrypto.hashSecret(recoveryAnswer.trim(), recoverySalt),
            recoveryAnswerSalt = recoverySalt,
        )
    }

    suspend fun updateWindow(
        startMinutes: Int,
        endMinutes: Int,
        isProActive: Boolean,
    ): SuperModeWindowUpdateResult {
        if (!isProActive) return SuperModeWindowUpdateResult.ProRequired
        if (!isValidWindow(startMinutes, endMinutes)) return SuperModeWindowUpdateResult.InvalidWindow
        preferences.setSuperModeWindow(startMinutes, endMinutes)
        return SuperModeWindowUpdateResult.Success
    }

    suspend fun resetWithRecovery(answer: String): SuperModeRecoveryResult {
        val storedState = preferences.getSuperModeStateOnce()
        if (!buildStatus(storedState, isProActive = false).isConfigured) {
            return SuperModeRecoveryResult.NotConfigured
        }
        val salt = storedState.recoveryAnswerSalt ?: return SuperModeRecoveryResult.NotConfigured
        val hash = storedState.recoveryAnswerHash ?: return SuperModeRecoveryResult.NotConfigured
        if (!SuperModeCrypto.verifySecret(answer.trim(), salt, hash)) {
            return SuperModeRecoveryResult.IncorrectAnswer
        }
        preferences.clearSuperMode()
        return SuperModeRecoveryResult.Success
    }

    suspend fun clearConfiguration() {
        preferences.clearSuperMode()
    }

    fun isWithinWindow(
        nowMillis: Long,
        startMinutes: Int,
        endMinutes: Int,
    ): Boolean = SuperModePolicy.isWithinWindow(nowMillis, startMinutes, endMinutes, zoneIdProvider())

    fun isValidWindow(
        startMinutes: Int,
        endMinutes: Int,
    ): Boolean = SuperModePolicy.isValidWindow(startMinutes, endMinutes)

    fun formatWindowLabel(
        startMinutes: Int,
        endMinutes: Int,
    ): String = SuperModePolicy.formatWindowLabel(startMinutes, endMinutes)

    fun formatTime(minutes: Int): String = SuperModePolicy.formatTime(minutes)
}

object SuperModePolicy {
    const val DEFAULT_START_MINUTES = 6 * 60
    const val DEFAULT_END_MINUTES = 10 * 60
    const val IDLE_TIMEOUT_MILLIS = 5 * 60 * 1000L

    fun buildStatus(
        storedState: SuperModeStoredState,
        isProActive: Boolean,
        nowMillis: Long,
        zoneId: ZoneId,
    ): SuperModeStatus {
        val windowStartMinutes =
            if (isProActive) {
                storedState.customWindowStartMinutes ?: DEFAULT_START_MINUTES
            } else {
                DEFAULT_START_MINUTES
            }
        val windowEndMinutes =
            if (isProActive) {
                storedState.customWindowEndMinutes ?: DEFAULT_END_MINUTES
            } else {
                DEFAULT_END_MINUTES
            }
        val isConfigured =
            storedState.enabled &&
                !storedState.passwordHash.isNullOrBlank() &&
                !storedState.passwordSalt.isNullOrBlank() &&
                !storedState.recoveryQuestion.isNullOrBlank() &&
                !storedState.recoveryAnswerHash.isNullOrBlank() &&
                !storedState.recoveryAnswerSalt.isNullOrBlank()
        val isAvailableNow = isWithinWindow(nowMillis, windowStartMinutes, windowEndMinutes, zoneId)
        val expiresAt =
            storedState.lastActiveAtMillis
                ?.takeIf { storedState.isActive }
                ?.plus(IDLE_TIMEOUT_MILLIS)
        val isSessionValid = expiresAt?.let { it > nowMillis } ?: false
        val isActive = isConfigured && storedState.isActive && isAvailableNow && isSessionValid
        val remainingMillis = if (isActive && expiresAt != null) maxOf(0L, expiresAt - nowMillis) else 0L
        return SuperModeStatus(
            isConfigured = isConfigured,
            isActive = isActive,
            isAvailableNow = isAvailableNow,
            windowLabel = formatWindowLabel(windowStartMinutes, windowEndMinutes),
            windowStartMinutes = windowStartMinutes,
            windowEndMinutes = windowEndMinutes,
            expiresAt = if (isActive) expiresAt else null,
            remainingMillis = remainingMillis,
        )
    }

    fun isWithinWindow(
        nowMillis: Long,
        startMinutes: Int,
        endMinutes: Int,
        zoneId: ZoneId,
    ): Boolean {
        val localTime = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalTime()
        val currentMinutes = localTime.hour * 60 + localTime.minute
        return currentMinutes in startMinutes until endMinutes
    }

    fun isValidWindow(
        startMinutes: Int,
        endMinutes: Int,
    ): Boolean = startMinutes in 0 until 1440 && endMinutes in 1..1440 && startMinutes < endMinutes

    fun formatWindowLabel(
        startMinutes: Int,
        endMinutes: Int,
    ): String = "${formatTime(startMinutes)} - ${formatTime(endMinutes)}"

    fun formatTime(minutes: Int): String {
        val safeMinutes = minutes.coerceIn(0, 1440)
        val hour = safeMinutes / 60
        val minute = safeMinutes % 60
        return "%02d:%02d".format(hour, minute)
    }
}
