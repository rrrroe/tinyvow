package com.rrrrz.tinyvow.data.media

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaUsageIntervalMergeTest {
    @Test
    fun mergeUsageIntervals_doesNotDoubleCountOverlaps() {
        val usage =
            mergeUsageIntervals(
                intervals = listOf(
                    UsageInterval(0L, 10_000L),
                    UsageInterval(5_000L, 20_000L),
                    UsageInterval(30_000L, 40_000L),
                ),
                rangeStart = 0L,
                rangeEnd = 60_000L,
            )

        assertEquals(30_000L, usage)
    }

    @Test
    fun mergeUsageIntervals_clipsToQueryWindow() {
        val usage =
            mergeUsageIntervals(
                intervals = listOf(
                    UsageInterval(0L, 10_000L),
                    UsageInterval(15_000L, 30_000L),
                ),
                rangeStart = 5_000L,
                rangeEnd = 20_000L,
            )

        assertEquals(10_000L, usage)
    }
}
