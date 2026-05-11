package com.rrrrz.tinyvow.data.usage

import com.rrrrz.tinyvow.data.db.LimitPeriod
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class UsagePeriodBoundsTest {
    private val zoneId = ZoneId.of("Asia/Shanghai")
    private val currentDate = LocalDate.of(2026, 5, 8)
    private val nowMillis = currentDate.atTime(13, 30).atZone(zoneId).toInstant().toEpochMilli()

    @Test
    fun daily_startsAtCurrentLocalDay() {
        val bounds = usagePeriodBounds(LimitPeriod.DAILY, zoneId, currentDate, nowMillis)

        assertEquals(currentDate.atStartOfDay(zoneId).toInstant().toEpochMilli(), bounds.startMillis)
        assertEquals(nowMillis, bounds.endMillis)
    }

    @Test
    fun weekly_keepsExistingRollingSevenDayWindow() {
        val bounds = usagePeriodBounds(LimitPeriod.WEEKLY, zoneId, currentDate, nowMillis)

        assertEquals(currentDate.minusDays(6).atStartOfDay(zoneId).toInstant().toEpochMilli(), bounds.startMillis)
        assertEquals(nowMillis, bounds.endMillis)
    }

    @Test
    fun monthly_startsAtFirstDayOfCurrentMonth() {
        val bounds = usagePeriodBounds(LimitPeriod.MONTHLY, zoneId, currentDate, nowMillis)

        assertEquals(LocalDate.of(2026, 5, 1).atStartOfDay(zoneId).toInstant().toEpochMilli(), bounds.startMillis)
        assertEquals(nowMillis, bounds.endMillis)
    }
}
