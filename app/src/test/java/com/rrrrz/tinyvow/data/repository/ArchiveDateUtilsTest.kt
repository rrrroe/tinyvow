package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.LimitPeriod
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class ArchiveDateUtilsTest {
    @Test
    fun weeklyPeriodStart_usesRollingSevenDays() {
        val date = LocalDate.of(2026, 4, 22)

        assertEquals(LocalDate.of(2026, 4, 16), ArchiveDateUtils.periodStart(date, LimitPeriod.WEEKLY))
    }

    @Test
    fun endOfDayMillis_matchesNextDayMinusOne() {
        val zoneId = ZoneId.of("Asia/Shanghai")
        val date = LocalDate.of(2026, 4, 22)

        val nextDayStart = ArchiveDateUtils.nextDayStartMillis(date, zoneId)
        val endOfDay = ArchiveDateUtils.endOfDayMillis(date, zoneId)

        assertEquals(nextDayStart - 1L, endOfDay)
    }
}
