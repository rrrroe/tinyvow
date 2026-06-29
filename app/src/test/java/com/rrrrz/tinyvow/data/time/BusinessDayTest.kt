package com.rrrrz.tinyvow.data.time

import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class BusinessDayTest {
    private val zoneId = ZoneId.of("Asia/Shanghai")

    @Test
    fun dateAt_beforeCustomBoundaryBelongsToPreviousBusinessDay() {
        val millis = LocalDate
            .of(2026, 5, 8)
            .atTime(2, 59)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()

        assertEquals(LocalDate.of(2026, 5, 7), BusinessDay.dateAt(millis, zoneId, 3))
    }

    @Test
    fun dateAt_atCustomBoundaryBelongsToCurrentBusinessDay() {
        val millis = LocalDate
            .of(2026, 5, 8)
            .atTime(3, 0)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()

        assertEquals(LocalDate.of(2026, 5, 8), BusinessDay.dateAt(millis, zoneId, 3))
    }
}
