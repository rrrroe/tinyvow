package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.data.db.DailyAppArchiveEntity
import com.rrrrz.tinyvow.i18n.AppText
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

internal fun formatDuration(durationMillis: Long): String {
    if (durationMillis <= 0L) return "0min"
    val totalMinutes = durationMillis / 60_000L
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}min"
        hours > 0 -> "${hours}h"
        else -> "${minutes}min"
    }
}

internal fun parseDisplayDuration(durationText: String): Long {
    val hourMinuteMatch = Regex("""(?:(\d+)h)?(?: ?(\d+)m(?:in)?)?""").matchEntire(durationText)
    if (hourMinuteMatch != null) {
        val hours = hourMinuteMatch.groupValues[1].toLongOrNull() ?: 0L
        val minutes = hourMinuteMatch.groupValues[2].toLongOrNull() ?: 0L
        return (hours * 60L + minutes) * 60_000L
    }
    return 0L
}

internal fun formatAxisDuration(durationMillis: Long): String {
    val totalMinutes = durationMillis / 60_000L
    val hours = totalMinutes / 60
    return when {
        hours >= 1 -> "${hours}h"
        totalMinutes > 0 -> "${totalMinutes}min"
        else -> "0"
    }
}

internal fun formatClockTime(epochMillis: Long, zoneId: ZoneId): String {
    return java.time.Instant.ofEpochMilli(epochMillis)
        .atZone(zoneId)
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA))
}

internal fun formatArchiveDate(date: String, pattern: String): String {
    return LocalDate
        .parse(date)
        .format(DateTimeFormatter.ofPattern(pattern, Locale.CHINA))
}

internal fun dayHourLabel(hour: Int): String {
    return "${hour.toString().padStart(2, '0')}:00"
}

internal fun appHourlyBucketAt(appItem: DailyAppArchiveEntity, hour: Int): Long {
    return when (hour) {
        0 -> appItem.hour00Millis
        1 -> appItem.hour01Millis
        2 -> appItem.hour02Millis
        3 -> appItem.hour03Millis
        4 -> appItem.hour04Millis
        5 -> appItem.hour05Millis
        6 -> appItem.hour06Millis
        7 -> appItem.hour07Millis
        8 -> appItem.hour08Millis
        9 -> appItem.hour09Millis
        10 -> appItem.hour10Millis
        11 -> appItem.hour11Millis
        12 -> appItem.hour12Millis
        13 -> appItem.hour13Millis
        14 -> appItem.hour14Millis
        15 -> appItem.hour15Millis
        16 -> appItem.hour16Millis
        17 -> appItem.hour17Millis
        18 -> appItem.hour18Millis
        19 -> appItem.hour19Millis
        20 -> appItem.hour20Millis
        21 -> appItem.hour21Millis
        22 -> appItem.hour22Millis
        23 -> appItem.hour23Millis
        else -> 0L
    }
}

internal fun formatSignedPointsLocal(value: Double): String {
    val formatted = String.format(Locale.CHINA, "%.1f", abs(value))
    return if (value >= 0) "+$formatted" else "-$formatted"
}

internal fun deltaDescription(
    current: Long,
    baseline: Long,
    prefix: String,
    countUnit: String? = null,
): String {
    if (baseline <= 0L && current <= 0L) return AppText.t("stats_value_flat", prefix)
    val delta = current - baseline
    if (delta == 0L) return AppText.t("stats_value_flat", prefix)
    val direction = if (delta > 0L) AppText.t("stats_label_2") else AppText.t("stats_label_12")
    val deltaValue = countUnit?.let { "${abs(delta)} $it" } ?: formatDuration(abs(delta))
    return "$prefix $direction $deltaValue"
}

internal fun Double.roundToLongSafe(): Long {
    return if (this.isNaN()) 0L else roundToLong()
}
