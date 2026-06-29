package com.rrrrz.tinyvow.data.media

import com.rrrrz.tinyvow.data.db.MediaAppPlaybackStatus

internal data class MediaPlaybackAccountingState(
    val trustedPlaybackMillis: Long,
    val untrustedGapMillis: Long,
    val isPlaying: Boolean,
    val activeStartedAt: Long?,
    val lastConfirmedAt: Long?,
    val lastStatus: MediaAppPlaybackStatus,
)

internal data class MediaPlaybackTrustedInterval(
    val startMillis: Long,
    val endMillis: Long,
)

internal object MediaAppPlaybackAccountant {
    const val TRUSTED_RECONNECT_WINDOW_MILLIS: Long = 30L * 60_000L

    fun applyStatus(
        current: MediaPlaybackAccountingState,
        status: MediaAppPlaybackStatus,
        nowMillis: Long,
        countGapSinceLastConfirmation: Boolean,
        trustedReconnectWindowMillis: Long = TRUSTED_RECONNECT_WINDOW_MILLIS,
    ): MediaPlaybackAccountingState =
        if (status == MediaAppPlaybackStatus.PLAYING) {
            confirmPlaying(current, nowMillis, trustedReconnectWindowMillis)
        } else {
            confirmNotPlaying(current, status, nowMillis, countGapSinceLastConfirmation, trustedReconnectWindowMillis)
        }

    fun trustedIntervalForStatus(
        current: MediaPlaybackAccountingState,
        status: MediaAppPlaybackStatus,
        nowMillis: Long,
        countGapSinceLastConfirmation: Boolean,
        trustedReconnectWindowMillis: Long = TRUSTED_RECONNECT_WINDOW_MILLIS,
    ): MediaPlaybackTrustedInterval? {
        if (!current.isPlaying) return null
        val startMillis = current.lastConfirmedAt ?: nowMillis
        val delta = (nowMillis - startMillis).coerceAtLeast(0L)
        if (delta <= 0L) return null
        val trusted =
            if (status == MediaAppPlaybackStatus.PLAYING) {
                delta <= trustedReconnectWindowMillis
            } else {
                countGapSinceLastConfirmation && delta <= trustedReconnectWindowMillis
            }
        return if (trusted) {
            MediaPlaybackTrustedInterval(startMillis = startMillis, endMillis = nowMillis)
        } else {
            null
        }
    }

    private fun confirmPlaying(
        current: MediaPlaybackAccountingState,
        nowMillis: Long,
        trustedReconnectWindowMillis: Long,
    ): MediaPlaybackAccountingState {
        if (!current.isPlaying) {
            return current.copy(
                isPlaying = true,
                activeStartedAt = nowMillis,
                lastConfirmedAt = nowMillis,
                lastStatus = MediaAppPlaybackStatus.PLAYING,
            )
        }
        val delta = (nowMillis - (current.lastConfirmedAt ?: nowMillis)).coerceAtLeast(0L)
        val trustedDelta = if (delta <= trustedReconnectWindowMillis) delta else 0L
        val untrustedDelta = if (trustedDelta == 0L && delta > 0L) delta else 0L
        return current.copy(
            trustedPlaybackMillis = current.trustedPlaybackMillis + trustedDelta,
            untrustedGapMillis = current.untrustedGapMillis + untrustedDelta,
            lastConfirmedAt = nowMillis,
            lastStatus = MediaAppPlaybackStatus.PLAYING,
        )
    }

    private fun confirmNotPlaying(
        current: MediaPlaybackAccountingState,
        status: MediaAppPlaybackStatus,
        nowMillis: Long,
        countGapSinceLastConfirmation: Boolean,
        trustedReconnectWindowMillis: Long,
    ): MediaPlaybackAccountingState {
        if (!current.isPlaying) {
            return current.copy(lastStatus = status)
        }
        val delta = (nowMillis - (current.lastConfirmedAt ?: nowMillis)).coerceAtLeast(0L)
        val trustedDelta =
            if (countGapSinceLastConfirmation && delta <= trustedReconnectWindowMillis) {
                delta
            } else {
                0L
            }
        val untrustedDelta = if (trustedDelta == 0L && delta > 0L) delta else 0L
        return current.copy(
            trustedPlaybackMillis = current.trustedPlaybackMillis + trustedDelta,
            untrustedGapMillis = current.untrustedGapMillis + untrustedDelta,
            isPlaying = false,
            activeStartedAt = null,
            lastConfirmedAt = null,
            lastStatus = status,
        )
    }
}
