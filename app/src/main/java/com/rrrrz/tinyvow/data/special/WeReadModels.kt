package com.rrrrz.tinyvow.data.special

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class WeReadPeriodUsage(
    val totalUsageMillis: Long,
    val dailyUsageMillis: Map<LocalDate, Long>,
)

object WeReadUsageParser {
    fun parsePeriodUsage(
        json: String,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): WeReadPeriodUsage {
        val daily = mutableMapOf<LocalDate, Long>()
        val dailyReadTimes = extractObject(json, "dailyReadTimes")
        if (dailyReadTimes != null) {
            collectDailyBuckets(dailyReadTimes, zoneId, daily)
        } else {
            collectDailyBuckets(extractObject(json, "readTimes"), zoneId, daily)
        }
        return WeReadPeriodUsage(
            totalUsageMillis = extractLong(json, "totalReadTime") * 1_000L,
            dailyUsageMillis = daily,
        )
    }

    private fun collectDailyBuckets(
        source: String?,
        zoneId: ZoneId,
        out: MutableMap<LocalDate, Long>,
    ) {
        source ?: return
        BUCKET_REGEX.findAll(source).forEach { match ->
            val epochSeconds = match.groupValues[1].toLongOrNull() ?: return@forEach
            val seconds = match.groupValues[2].toLongOrNull()?.coerceAtLeast(0L) ?: return@forEach
            val date = Instant.ofEpochSecond(epochSeconds).atZone(zoneId).toLocalDate()
            out[date] = (out[date] ?: 0L) + seconds * 1_000L
        }
    }

    private fun extractLong(json: String, field: String): Long {
        val regex = Regex(""""$field"\s*:\s*(\d+)""")
        return regex.find(json)?.groupValues?.getOrNull(1)?.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
    }

    private fun extractObject(json: String, field: String): String? {
        val start = Regex(""""$field"\s*:\s*\{""").find(json)?.range?.last ?: return null
        var depth = 0
        for (index in start until json.length) {
            when (json[index]) {
                '{' -> depth += 1
                '}' -> {
                    depth -= 1
                    if (depth == 0) {
                        return json.substring(start, index + 1)
                    }
                }
            }
        }
        return null
    }

    private val BUCKET_REGEX = Regex(""""(\d+)"\s*:\s*(\d+)""")
}
