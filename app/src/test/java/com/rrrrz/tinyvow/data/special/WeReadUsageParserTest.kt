package com.rrrrz.tinyvow.data.special

import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class WeReadUsageParserTest {
    private val zoneId = ZoneId.of("Asia/Shanghai")

    @Test
    fun parsePeriodUsage_convertsSecondBucketsToMillis() {
        val may18 = LocalDate.of(2026, 5, 18).atStartOfDay(zoneId).toEpochSecond()
        val may17 = LocalDate.of(2026, 5, 17).atStartOfDay(zoneId).toEpochSecond()

        val usage =
            WeReadUsageParser.parsePeriodUsage(
                """
                {
                  "totalReadTime": 5400,
                  "readTimes": {
                    "$may18": 1800,
                    "$may17": 600
                  }
                }
                """.trimIndent(),
                zoneId,
            )

        assertEquals(5_400_000L, usage.totalUsageMillis)
        assertEquals(1_800_000L, usage.dailyUsageMillis[LocalDate.of(2026, 5, 18)])
        assertEquals(600_000L, usage.dailyUsageMillis[LocalDate.of(2026, 5, 17)])
    }

    @Test
    fun parsePeriodUsage_prefersDailyReadTimesWhenBothBucketsExist() {
        val may18 = LocalDate.of(2026, 5, 18).atStartOfDay(zoneId).toEpochSecond()

        val usage =
            WeReadUsageParser.parsePeriodUsage(
                """
                {
                  "totalReadTime": 1800,
                  "dailyReadTimes": { "$may18": 600 },
                  "readTimes": { "$may18": 1200 }
                }
                """.trimIndent(),
                zoneId,
            )

        assertEquals(600_000L, usage.dailyUsageMillis[LocalDate.of(2026, 5, 18)])
    }
}
