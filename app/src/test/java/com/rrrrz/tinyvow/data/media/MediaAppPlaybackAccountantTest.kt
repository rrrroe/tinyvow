package com.rrrrz.tinyvow.data.media

import com.rrrrz.tinyvow.data.db.MediaAppPlaybackStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaAppPlaybackAccountantTest {
    @Test
    fun playingPollAccumulatesTrustedDelta() {
        val started =
            MediaAppPlaybackAccountant.applyStatus(
                current = emptyState(),
                status = MediaAppPlaybackStatus.PLAYING,
                nowMillis = 0L,
                countGapSinceLastConfirmation = true,
            )

        val confirmed =
            MediaAppPlaybackAccountant.applyStatus(
                current = started,
                status = MediaAppPlaybackStatus.PLAYING,
                nowMillis = 60_000L,
                countGapSinceLastConfirmation = true,
            )

        assertTrue(confirmed.isPlaying)
        assertEquals(60_000L, confirmed.trustedPlaybackMillis)
        assertEquals(0L, confirmed.untrustedGapMillis)
        assertEquals(
            MediaPlaybackTrustedInterval(0L, 60_000L),
            MediaAppPlaybackAccountant.trustedIntervalForStatus(
                current = started,
                status = MediaAppPlaybackStatus.PLAYING,
                nowMillis = 60_000L,
                countGapSinceLastConfirmation = true,
            ),
        )
    }

    @Test
    fun reconnectStillPlayingWithinThirtyMinutesIsTrusted() {
        val current =
            emptyState().copy(
                isPlaying = true,
                activeStartedAt = 0L,
                lastConfirmedAt = 0L,
                lastStatus = MediaAppPlaybackStatus.PLAYING,
            )

        val confirmed =
            MediaAppPlaybackAccountant.applyStatus(
                current = current,
                status = MediaAppPlaybackStatus.PLAYING,
                nowMillis = 29L * 60_000L,
                countGapSinceLastConfirmation = true,
            )

        assertEquals(29L * 60_000L, confirmed.trustedPlaybackMillis)
        assertEquals(0L, confirmed.untrustedGapMillis)
        assertTrue(confirmed.isPlaying)
    }

    @Test
    fun reconnectPausedDoesNotTrustOfflineGap() {
        val current =
            emptyState().copy(
                isPlaying = true,
                activeStartedAt = 0L,
                lastConfirmedAt = 0L,
                lastStatus = MediaAppPlaybackStatus.PLAYING,
            )

        val paused =
            MediaAppPlaybackAccountant.applyStatus(
                current = current,
                status = MediaAppPlaybackStatus.PAUSED,
                nowMillis = 10L * 60_000L,
                countGapSinceLastConfirmation = false,
            )

        assertEquals(0L, paused.trustedPlaybackMillis)
        assertEquals(10L * 60_000L, paused.untrustedGapMillis)
        assertFalse(paused.isPlaying)
    }

    @Test
    fun reconnectStillPlayingAfterThirtyMinutesIsUntrusted() {
        val current =
            emptyState().copy(
                isPlaying = true,
                activeStartedAt = 0L,
                lastConfirmedAt = 0L,
                lastStatus = MediaAppPlaybackStatus.PLAYING,
            )

        val confirmed =
            MediaAppPlaybackAccountant.applyStatus(
                current = current,
                status = MediaAppPlaybackStatus.PLAYING,
                nowMillis = 31L * 60_000L,
                countGapSinceLastConfirmation = true,
            )

        assertEquals(0L, confirmed.trustedPlaybackMillis)
        assertEquals(31L * 60_000L, confirmed.untrustedGapMillis)
        assertTrue(confirmed.isPlaying)
        assertEquals(
            null,
            MediaAppPlaybackAccountant.trustedIntervalForStatus(
                current = current,
                status = MediaAppPlaybackStatus.PLAYING,
                nowMillis = 31L * 60_000L,
                countGapSinceLastConfirmation = true,
            ),
        )
    }

    @Test
    fun normalStopSettlesRecentTrustedDelta() {
        val current =
            emptyState().copy(
                isPlaying = true,
                activeStartedAt = 0L,
                lastConfirmedAt = 0L,
                lastStatus = MediaAppPlaybackStatus.PLAYING,
            )

        val stopped =
            MediaAppPlaybackAccountant.applyStatus(
                current = current,
                status = MediaAppPlaybackStatus.STOPPED,
                nowMillis = 60_000L,
                countGapSinceLastConfirmation = true,
            )

        assertEquals(60_000L, stopped.trustedPlaybackMillis)
        assertEquals(0L, stopped.untrustedGapMillis)
        assertFalse(stopped.isPlaying)
    }

    private fun emptyState(): MediaPlaybackAccountingState =
        MediaPlaybackAccountingState(
            trustedPlaybackMillis = 0L,
            untrustedGapMillis = 0L,
            isPlaying = false,
            activeStartedAt = null,
            lastConfirmedAt = null,
            lastStatus = MediaAppPlaybackStatus.UNKNOWN,
        )
}
