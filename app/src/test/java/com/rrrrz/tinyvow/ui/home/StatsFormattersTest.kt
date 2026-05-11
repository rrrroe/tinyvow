package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.data.db.DailyAppArchiveEntity
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class StatsFormattersTest {
    @Test
    fun formatDuration_usesCompactHourMinuteLabels() {
        assertEquals("0m", formatDuration(0L))
        assertEquals("1m", formatDuration(60_000L))
        assertEquals("2h", formatDuration(120 * 60_000L))
        assertEquals("2h 5m", formatDuration(125 * 60_000L))
    }

    @Test
    fun parseDisplayDuration_readsFormattedDurationBackToMillis() {
        assertEquals(60_000L, parseDisplayDuration("1m"))
        assertEquals(2 * 60 * 60_000L, parseDisplayDuration("2h"))
        assertEquals(125 * 60_000L, parseDisplayDuration("2h 5m"))
    }

    @Test
    fun dayHourLabel_padsSingleDigitHour() {
        assertEquals("00:00", dayHourLabel(0))
        assertEquals("09:00", dayHourLabel(9))
        assertEquals("23:00", dayHourLabel(23))
    }

    @Test
    fun appHourlyBucketAt_readsSnapshotHourFields() {
        val item =
            DailyAppArchiveEntity(
                id = "id",
                archiveDate = "2026-05-09",
                packageName = "pkg",
                appLabel = "App",
                scopeKey = "device:pkg",
                groupId = null,
                groupName = null,
                groupType = null,
                limitPeriod = null,
                hour00Millis = 1L,
                hour12Millis = 12L,
                hour23Millis = 23L,
                createdAt = 0L,
                updatedAt = 0L,
            )

        assertEquals(1L, appHourlyBucketAt(item, 0))
        assertEquals(12L, appHourlyBucketAt(item, 12))
        assertEquals(23L, appHourlyBucketAt(item, 23))
        assertEquals(0L, appHourlyBucketAt(item, 24))
    }

    @Test
    fun formatClockTime_usesRequestedZone() {
        val zoneId = ZoneId.of("Asia/Shanghai")
        val millis = java.time.LocalDate.of(2026, 5, 9).atTime(7, 5).atZone(zoneId).toInstant().toEpochMilli()

        assertEquals("07:05", formatClockTime(millis, zoneId))
    }
}
