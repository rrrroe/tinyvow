package com.rrrrz.tinyvow.data.usage

import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.time.BusinessDay
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class UsagePeriodBoundsTest {
    private val zoneId = ZoneId.of("Asia/Shanghai")
    private val currentDate = LocalDate.of(2026, 5, 8)
    private val nowMillis = currentDate.atTime(13, 30).atZone(zoneId).toInstant().toEpochMilli()
    private val dayStartHour = BusinessDay.DEFAULT_START_HOUR

    @Test
    fun daily_startsAtSystemLocalDay() {
        val bounds = usagePeriodBounds(LimitPeriod.DAILY, zoneId, currentDate, nowMillis, dayStartHour)

        assertEquals(
            currentDate.atStartOfDay(zoneId).toInstant().toEpochMilli(),
            bounds.startMillis,
        )
        assertEquals(nowMillis, bounds.endMillis)
    }

    @Test
    fun weekly_keepsExistingRollingSevenDayWindow() {
        val bounds = usagePeriodBounds(LimitPeriod.WEEKLY, zoneId, currentDate, nowMillis, dayStartHour)

        assertEquals(
            currentDate.minusDays(6).atStartOfDay(zoneId).toInstant().toEpochMilli(),
            bounds.startMillis,
        )
        assertEquals(nowMillis, bounds.endMillis)
    }

    @Test
    fun monthly_startsAtFirstDayOfCurrentMonth() {
        val bounds = usagePeriodBounds(LimitPeriod.MONTHLY, zoneId, currentDate, nowMillis, dayStartHour)

        assertEquals(
            LocalDate.of(2026, 5, 1).atStartOfDay(zoneId).toInstant().toEpochMilli(),
            bounds.startMillis,
        )
        assertEquals(nowMillis, bounds.endMillis)
    }

    @Test
    fun daily_startsAtCustomBusinessDayBoundary() {
        val bounds = usagePeriodBounds(LimitPeriod.DAILY, zoneId, currentDate, nowMillis, dayStartHour = 3)

        assertEquals(
            currentDate.atTime(3, 0).atZone(zoneId).toInstant().toEpochMilli(),
            bounds.startMillis,
        )
        assertEquals(nowMillis, bounds.endMillis)
    }

    @Test
    fun weekly_startsAtCustomBusinessDayBoundary() {
        val bounds = usagePeriodBounds(LimitPeriod.WEEKLY, zoneId, currentDate, nowMillis, dayStartHour = 3)

        assertEquals(
            currentDate.minusDays(6).atTime(3, 0).atZone(zoneId).toInstant().toEpochMilli(),
            bounds.startMillis,
        )
        assertEquals(nowMillis, bounds.endMillis)
    }

    @Test
    fun monthly_startsAtCustomBusinessDayBoundary() {
        val bounds = usagePeriodBounds(LimitPeriod.MONTHLY, zoneId, currentDate, nowMillis, dayStartHour = 3)

        assertEquals(
            LocalDate.of(2026, 5, 1).atTime(3, 0).atZone(zoneId).toInstant().toEpochMilli(),
            bounds.startMillis,
        )
        assertEquals(nowMillis, bounds.endMillis)
    }
}
