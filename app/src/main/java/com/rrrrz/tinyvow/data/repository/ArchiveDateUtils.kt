package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.time.BusinessDay
import java.time.LocalDate
import java.time.ZoneId

internal object ArchiveDateUtils {
    fun formatDate(date: LocalDate): String = date.toString()

    fun localDateAt(
        millis: Long,
        zoneId: ZoneId,
        dayStartHour: Int = BusinessDay.cachedStartHour(),
    ): LocalDate = BusinessDay.dateAt(millis, zoneId, dayStartHour)

    fun startOfDayMillis(
        date: LocalDate,
        zoneId: ZoneId,
        dayStartHour: Int = BusinessDay.cachedStartHour(),
    ): Long = BusinessDay.startOfDayMillis(date, zoneId, dayStartHour)

    fun endOfDayMillis(
        date: LocalDate,
        zoneId: ZoneId,
        dayStartHour: Int = BusinessDay.cachedStartHour(),
    ): Long = BusinessDay.endOfDayMillis(date, zoneId, dayStartHour)

    fun nextDayStartMillis(
        date: LocalDate,
        zoneId: ZoneId,
        dayStartHour: Int = BusinessDay.cachedStartHour(),
    ): Long = BusinessDay.nextDayStartMillis(date, zoneId, dayStartHour)

    fun periodStart(date: LocalDate, period: LimitPeriod): LocalDate {
        return when (period) {
            LimitPeriod.DAILY -> date
            LimitPeriod.WEEKLY -> date.minusDays(6)
            LimitPeriod.MONTHLY -> date.withDayOfMonth(1)
        }
    }
}
