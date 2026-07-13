package com.rrrrz.tinyvow.data.steps

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthConnectStepProbeTest {
    @Test
    fun retainsOnlyLatestSevenCalendarDays() {
        val today = LocalDate.of(2026, 7, 11)
        val snapshots =
            (-8L..1L).map { offset ->
                snapshot(
                    date = today.plusDays(offset),
                    fetchedAtMillis = 100L + offset,
                )
            }

        val retained = HealthConnectStepProbe.retainRecentSevenDays(snapshots, today)

        assertEquals(7, retained.size)
        assertEquals(today.toString(), retained.first().date)
        assertEquals(today.minusDays(6).toString(), retained.last().date)
        assertFalse(retained.any { it.date == today.minusDays(7).toString() })
        assertFalse(retained.any { it.date == today.plusDays(1).toString() })
    }

    @Test
    fun keepsOnlyLatestSnapshotForEachDate() {
        val today = LocalDate.of(2026, 7, 11)
        val older = snapshot(today, fetchedAtMillis = 100L, xiaomiSteps = 1_000L)
        val newer = snapshot(today, fetchedAtMillis = 200L, xiaomiSteps = 1_500L)

        val retained =
            HealthConnectStepProbe.retainRecentSevenDays(
                snapshots = listOf(older, newer),
                today = today,
            )

        assertEquals(1, retained.size)
        assertEquals(1_500L, retained.single().xiaomiSteps)
        assertEquals(200L, retained.single().fetchedAtMillis)
    }

    @Test
    fun strictSourceAllowlistDoesNotIncludeZeppLife() {
        assertTrue("com.mi.health" in HealthConnectStepProbe.XIAOMI_SOURCE_PACKAGES)
        assertTrue("com.xiaomi.wearable" in HealthConnectStepProbe.XIAOMI_SOURCE_PACKAGES)
        assertFalse("com.xiaomi.hm.health" in HealthConnectStepProbe.XIAOMI_SOURCE_PACKAGES)
    }

    private fun snapshot(
        date: LocalDate,
        fetchedAtMillis: Long,
        xiaomiSteps: Long = 1_000L,
    ): HealthConnectStepProbeSnapshot =
        HealthConnectStepProbeSnapshot(
            date = date.toString(),
            status = HealthConnectStepProbeStatus.XIAOMI_SOURCE_FOUND,
            xiaomiSteps = xiaomiSteps,
            systemSteps = xiaomiSteps,
            sourcePackage = "com.mi.health",
            xiaomiSourceTotals = mapOf("com.mi.health" to xiaomiSteps),
            originPackages = listOf("com.mi.health"),
            rawRecordCount = 1,
            xiaomiRecordCount = 1,
            manualRecordCount = 0,
            recordingMethods = listOf(2),
            latestXiaomiRecordEndMillis = fetchedAtMillis,
            latestXiaomiModifiedMillis = fetchedAtMillis,
            rawRecordsComplete = true,
            queryStartMillis = 0L,
            queryEndMillis = fetchedAtMillis,
            fetchedAtMillis = fetchedAtMillis,
            uExtensionVersion = 17,
        )
}
