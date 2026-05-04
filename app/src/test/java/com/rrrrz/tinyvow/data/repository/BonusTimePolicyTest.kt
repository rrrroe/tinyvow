package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.LimitPeriod
import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class BonusTimePolicyTest {
    private val timeZone = TimeZone.getTimeZone("Asia/Shanghai")

    @Test
    fun calculateBonusExpiryTime_dailyExpiresAtEndOfSameDay() {
        val createdAt = millis(2026, Calendar.APRIL, 22, 10, 15, 30, 123)

        assertEquals(
            millis(2026, Calendar.APRIL, 22, 23, 59, 59, 999),
            calculateBonusExpiryTime(createdAt, LimitPeriod.DAILY, timeZone),
        )
    }

    @Test
    fun calculateBonusExpiryTime_weeklyExpiresAtEndOfSeventhDay() {
        val createdAt = millis(2026, Calendar.APRIL, 22, 10, 15, 30, 123)

        assertEquals(
            millis(2026, Calendar.APRIL, 28, 23, 59, 59, 999),
            calculateBonusExpiryTime(createdAt, LimitPeriod.WEEKLY, timeZone),
        )
    }

    @Test
    fun calculateBonusExpiryTime_monthlyExpiresAtEndOfCurrentMonth() {
        val createdAt = millis(2026, Calendar.APRIL, 22, 10, 15, 30, 123)

        assertEquals(
            millis(2026, Calendar.APRIL, 30, 23, 59, 59, 999),
            calculateBonusExpiryTime(createdAt, LimitPeriod.MONTHLY, timeZone),
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
