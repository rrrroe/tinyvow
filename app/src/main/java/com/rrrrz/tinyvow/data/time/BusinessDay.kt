package com.rrrrz.tinyvow.data.time

import android.content.Context
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object BusinessDay {
    const val DEFAULT_START_HOUR = 0
    const val MIN_START_HOUR = 0
    const val MAX_START_HOUR = 6

    @Volatile
    private var cachedStartHour: Int = DEFAULT_START_HOUR

    fun normalizeStartHour(hour: Int?): Int =
        (hour ?: DEFAULT_START_HOUR).coerceIn(MIN_START_HOUR, MAX_START_HOUR)

    fun cachedStartHour(): Int = cachedStartHour

    fun updateCachedStartHour(hour: Int) {
        cachedStartHour = normalizeStartHour(hour)
    }

    suspend fun loadStartHour(context: Context): Int {
        val hour = ManagedAppPreferences(context.applicationContext).getDayBoundaryHourOnce()
        updateCachedStartHour(hour)
        return cachedStartHour
    }

    fun dateAt(
        millis: Long,
        zoneId: ZoneId,
        startHour: Int,
    ): LocalDate {
        val normalizedHour = normalizeStartHour(startHour)
        val zonedDateTime = Instant.ofEpochMilli(millis).atZone(zoneId)
        val date = zonedDateTime.toLocalDate()
        return if (zonedDateTime.hour < normalizedHour) {
            date.minusDays(1)
        } else {
            date
        }
    }

    fun today(
        zoneId: ZoneId,
        startHour: Int,
        nowMillis: Long = System.currentTimeMillis(),
    ): LocalDate = dateAt(nowMillis, zoneId, startHour)

    fun startOfDayMillis(
        date: LocalDate,
        zoneId: ZoneId,
        startHour: Int,
    ): Long {
        val normalizedHour = normalizeStartHour(startHour)
        return date.atTime(normalizedHour, 0).atZone(zoneId).toInstant().toEpochMilli()
    }

    fun nextDayStartMillis(
        date: LocalDate,
        zoneId: ZoneId,
        startHour: Int,
    ): Long = startOfDayMillis(date.plusDays(1), zoneId, startHour)

    fun endOfDayMillis(
        date: LocalDate,
        zoneId: ZoneId,
        startHour: Int,
    ): Long = nextDayStartMillis(date, zoneId, startHour) - 1L

    fun bucketStartHour(
        bucketIndex: Int,
        startHour: Int,
    ): Int = (normalizeStartHour(startHour) + bucketIndex).mod(24)

    fun isNightBucket(
        bucketIndex: Int,
        startHour: Int,
    ): Boolean {
        val actualHour = bucketStartHour(bucketIndex, startHour)
        return actualHour < 6 || actualHour >= 22
    }
}
