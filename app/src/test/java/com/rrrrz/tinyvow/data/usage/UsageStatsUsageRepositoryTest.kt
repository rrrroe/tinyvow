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

    @Test
    fun countAppForegroundEntries_countsSamePackageActivitySwitchAsOneOpen() {
        val transitions =
            listOf(
                foregroundResume("pkg", 1_000L),
                foregroundResume("pkg", 2_000L),
                foregroundResume("pkg", 2_000L),
            )

        val counts = countAppForegroundEntries(transitions, rangeStart = 0L, rangeEnd = 3_000L)

        assertEquals(mapOf("pkg" to 1), counts)
    }

    @Test
    fun countAppForegroundEntries_countsReturnFromAnotherPackageAsNewOpen() {
        val transitions =
            listOf(
                foregroundResume("pkg.a", 1_000L),
                foregroundResume("pkg.b", 2_000L),
                foregroundResume("pkg.a", 3_000L),
            )

        val counts = countAppForegroundEntries(transitions, rangeStart = 0L, rangeEnd = 4_000L)

        assertEquals(mapOf("pkg.a" to 2, "pkg.b" to 1), counts)
    }

    @Test
    fun countAppForegroundEntries_usesLookbackStateWithoutCountingIt() {
        val transitions =
            listOf(
                foregroundResume("pkg", 500L),
                foregroundResume("pkg", 1_500L),
                foregroundResume("other", 2_000L),
            )

        val counts = countAppForegroundEntries(transitions, rangeStart = 1_000L, rangeEnd = 3_000L)

        assertEquals(mapOf("other" to 1), counts)
    }

    @Test
    fun countAppForegroundEntries_countsResumeAfterScreenBoundaryAsNewOpen() {
        val transitions =
            listOf(
                foregroundResume("pkg", 1_000L),
                AppForegroundTransition(
                    packageName = null,
                    timeStamp = 2_000L,
                    type = AppForegroundTransitionType.FOREGROUND_CLEARED,
                ),
                foregroundResume("pkg", 3_000L),
            )

        val counts = countAppForegroundEntries(transitions, rangeStart = 0L, rangeEnd = 4_000L)

        assertEquals(mapOf("pkg" to 2), counts)
    }

    private fun foregroundResume(packageName: String, timeStamp: Long): AppForegroundTransition =
        AppForegroundTransition(
            packageName = packageName,
            timeStamp = timeStamp,
            type = AppForegroundTransitionType.RESUMED,
        )
}
