package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.LimitPeriod
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

internal object ArchiveDateUtils {
    fun formatDate(date: LocalDate): String = date.toString()

    fun localDateAt(millis: Long, zoneId: ZoneId): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
    }

    fun startOfDayMillis(date: LocalDate, zoneId: ZoneId): Long {
        return date.atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    fun endOfDayMillis(date: LocalDate, zoneId: ZoneId): Long {
        return startOfDayMillis(date.plusDays(1), zoneId) - 1L
    }

    fun nextDayStartMillis(date: LocalDate, zoneId: ZoneId): Long {
        return startOfDayMillis(date.plusDays(1), zoneId)
    }

    fun periodStart(date: LocalDate, period: LimitPeriod): LocalDate {
        return when (period) {
            LimitPeriod.DAILY -> date
            LimitPeriod.WEEKLY -> date.minusDays(6)
            LimitPeriod.MONTHLY -> date.withDayOfMonth(1)
        }
    }
}
