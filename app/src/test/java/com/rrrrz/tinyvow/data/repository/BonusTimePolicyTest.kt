package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.LimitPeriod
import java.util.Calendar
import java.util.TimeZone
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class BonusTimePolicyTest {
    private val timeZone = TimeZone.getTimeZone("Asia/Shanghai")
    private val zoneId = ZoneId.of("Asia/Shanghai")
    private val dayStartHour = 3

    @Test
    fun calculateBonusExpiryTime_dailyExpiresAtBusinessDayEnd() {
        val createdAt = millis(2026, Calendar.APRIL, 22, 10, 15, 30, 123)

        assertEquals(
            millis(2026, Calendar.APRIL, 23, 2, 59, 59, 999),
            calculateBonusExpiryTime(createdAt, LimitPeriod.DAILY, zoneId, dayStartHour),
        )
    }

    @Test
    fun calculateBonusExpiryTime_dailyBeforeBoundaryExpiresAtCurrentClockDayBoundary() {
        val createdAt = millis(2026, Calendar.APRIL, 22, 2, 15, 30, 123)

        assertEquals(
            millis(2026, Calendar.APRIL, 22, 2, 59, 59, 999),
            calculateBonusExpiryTime(createdAt, LimitPeriod.DAILY, zoneId, dayStartHour),
        )
    }

    @Test
    fun calculateBonusExpiryTime_weeklyExpiresAtSeventhBusinessDayEnd() {
        val createdAt = millis(2026, Calendar.APRIL, 22, 10, 15, 30, 123)

        assertEquals(
            millis(2026, Calendar.APRIL, 29, 2, 59, 59, 999),
            calculateBonusExpiryTime(createdAt, LimitPeriod.WEEKLY, zoneId, dayStartHour),
        )
    }

    @Test
    fun calculateBonusExpiryTime_monthlyExpiresAtBusinessMonthEnd() {
        val createdAt = millis(2026, Calendar.APRIL, 22, 10, 15, 30, 123)

        assertEquals(
            millis(2026, Calendar.MAY, 1, 2, 59, 59, 999),
            calculateBonusExpiryTime(createdAt, LimitPeriod.MONTHLY, zoneId, dayStartHour),
        )
    }

    private fun millis(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int,
        millisecond: Int,
    ): Long {
        return Calendar.getInstance(timeZone).apply {
            clear()
            set(year, month, day, hour, minute, second)
            set(Calendar.MILLISECOND, millisecond)
        }.timeInMillis
    }
}
