package com.rrrrz.tinyvow.data.usage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UsageStatsUsageRepositoryTest {
    @Test
    fun clipSessionToRange_keepsOnlyOverlapWithinDay() {
        val session = AppSession(packageName = "pkg", startTime = 1_000L, endTime = 7_000L)

        val clipped = clipSessionToRange(session, rangeStart = 5_000L, rangeEnd = 9_000L)

        assertEquals(AppSession(packageName = "pkg", startTime = 5_000L, endTime = 7_000L), clipped)
    }

    @Test
    fun clipSessionToRange_returnsNullWhenNoOverlap() {
        val session = AppSession(packageName = "pkg", startTime = 1_000L, endTime = 2_000L)

        val clipped = clipSessionToRange(session, rangeStart = 3_000L, rangeEnd = 4_000L)

        assertNull(clipped)
    }

    @Test
    fun aggregateUsageFromSessions_sumsForegroundSessionsByPackage() {
        val sessions =
            listOf(
                AppSession(packageName = "pkg.a", startTime = 0L, endTime = 120_000L),
                AppSession(packageName = "pkg.a", startTime = 180_000L, endTime = 240_000L),
                AppSession(packageName = "pkg.b", startTime = 30_000L, endTime = 90_000L),
            )

        val usage = aggregateUsageFromSessions(sessions)

        assertEquals(180_000L, usage["pkg.a"])
        assertEquals(60_000L, usage["pkg.b"])
    }

    @Test
    fun buildSessionsFromTransitions_keepsSamePackageActivitySwitchAsOneSession() {
        val transitions =
            listOf(
                UsageEventTransition("pkg", "A", 0L, UsageEventTransitionType.RESUMED),
                UsageEventTransition("pkg", "B", 10_000L, UsageEventTransitionType.RESUMED),
                UsageEventTransition("pkg", "A", 11_000L, UsageEventTransitionType.PAUSED),
                UsageEventTransition("pkg", "A", 12_000L, UsageEventTransitionType.PAUSED),
                UsageEventTransition("pkg", "B", 3_600_000L, UsageEventTransitionType.PAUSED),
            )

        val sessions =
            buildSessionsFromTransitions(
                transitions = transitions,
                rangeStart = 0L,
                rangeEnd = 3_700_000L,
                nowMillis = 3_700_000L,
            )

        assertEquals(listOf(AppSession("pkg", 0L, 3_600_000L)), sessions)
    }
}
