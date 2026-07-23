package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.OfflineFocusSessionStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class OfflineFocusElapsedDurationTest {
    @Test
    fun resumedSessionAddsOnlyTheCurrentActiveSegment() {
        val session = session(
            actualDurationMillis = 10 * MINUTE,
            status = OfflineFocusSessionStatus.RUNNING,
            resumedAt = 15 * MINUTE,
        )

        assertEquals(25 * MINUTE, session.elapsedDurationMillisAt(30 * MINUTE))
    }

    @Test
    fun pausedSessionKeepsTheStoredActiveDuration() {
        val session = session(
            actualDurationMillis = 10 * MINUTE,
            status = OfflineFocusSessionStatus.PAUSED,
            resumedAt = null,
        )

        assertEquals(10 * MINUTE, session.elapsedDurationMillisAt(30 * MINUTE))
    }

    private fun session(
        actualDurationMillis: Long,
        status: OfflineFocusSessionStatus,
        resumedAt: Long?,
    ): OfflineFocusSession =
        OfflineFocusSession(
            id = "session",
            categoryId = "reading",
            categoryName = "Reading",
            iconKey = "reading",
            colorArgb = 0xFF3F7CAC.toInt(),
            plannedDurationMillis = 60 * MINUTE,
            actualDurationMillis = actualDurationMillis,
            status = status,
            startedAt = 0L,
            pausedAt = null,
            resumedAt = resumedAt,
            completedAt = null,
            abandonedAt = null,
            pointsAwarded = 0.0,
        )

    private companion object {
        const val MINUTE = 60_000L
    }
}
