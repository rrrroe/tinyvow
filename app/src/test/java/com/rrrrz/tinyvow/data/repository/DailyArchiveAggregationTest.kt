package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.usage.AppSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyArchiveAggregationTest {
    @Test
    fun summarizeAppBehavior_splitsSessionsAcrossHoursAndNightWindows() {
        val dayStart = 0L
        val nextDayStart = 24L * 60L * 60L * 1000L
        val sessions =
            listOf(
                AppSession(
                    packageName = "demo.app",
                    startTime = 5L * 60L * 60L * 1000L + 30L * 60L * 1000L,
                    endTime = 6L * 60L * 60L * 1000L + 30L * 60L * 1000L,
                ),
                AppSession(
                    packageName = "demo.app",
                    startTime = 21L * 60L * 60L * 1000L + 30L * 60L * 1000L,
                    endTime = 22L * 60L * 60L * 1000L + 30L * 60L * 1000L,
                ),
            )

        val summary = summarizeAppBehavior(sessions, dayStart, nextDayStart)

        assertEquals(2, summary.sessionCount)
        assertEquals(60L * 60L * 1000L, summary.longestSessionMillis)
        assertEquals(60L * 60L * 1000L, summary.nightUsageMillis)
        assertEquals(30L * 60L * 1000L, summary.hourlyUsageMillis[5])
        assertEquals(30L * 60L * 1000L, summary.hourlyUsageMillis[6])
        assertEquals(30L * 60L * 1000L, summary.hourlyUsageMillis[21])
        assertEquals(30L * 60L * 1000L, summary.hourlyUsageMillis[22])
        assertTrue(summary.hourlyUsageMillis.withIndex().all { (hour, value) ->
            hour in listOf(5, 6, 21, 22) || value == 0L
        })
    }

    @Test
    fun allocateGroupEarnedPoints_prefersUsageWeightsAndFallsBackToEvenShare() {
        val weightedAllocation =
            allocateGroupEarnedPoints(
                totalPoints = 100.0,
                packageNames = listOf("a", "b"),
                usageByPackage = mapOf("a" to 300L, "b" to 100L),
            )

        assertEquals(75.0, weightedAllocation.getValue("a"), 0.0001)
        assertEquals(25.0, weightedAllocation.getValue("b"), 0.0001)

        val evenAllocation =
            allocateGroupEarnedPoints(
                totalPoints = 10.0,
                packageNames = listOf("a", "b"),
                usageByPackage = emptyMap(),
            )

        assertEquals(5.0, evenAllocation.getValue("a"), 0.0001)
        assertEquals(5.0, evenAllocation.getValue("b"), 0.0001)
    }

    @Test
    fun selectUngroupedLaunchablePackages_onlyKeepsLaunchableUngroupedPackages() {
        val selected =
            selectUngroupedLaunchablePackages(
                activePackageNames = setOf("grouped.framework", "launchable.app", "background.framework"),
                groupedPackageNames = setOf("grouped.framework"),
                launchablePackageNames = setOf("launchable.app"),
            )

        assertEquals(setOf("launchable.app"), selected)
    }

    @Test
    fun selectUngroupedLaunchablePackages_keepsGroupedPackagesOutOfUngroupedCandidates() {
        val selected =
            selectUngroupedLaunchablePackages(
                activePackageNames = setOf("managed.system"),
                groupedPackageNames = setOf("managed.system"),
                launchablePackageNames = emptySet(),
            )

        assertTrue(selected.isEmpty())
    }

    @Test
    fun selectPackagesToArchive_keepsGroupedPackagesAndLaunchableUngroupedPackages() {
        val packagesToArchive =
            selectPackagesToArchive(
                groupedPackageNames = setOf("managed.system"),
                ungroupedLaunchablePackages = setOf("launchable.app"),
            )

        assertEquals(setOf("managed.system", "launchable.app"), packagesToArchive)
    }
}
