package com.rrrrz.tinyvow.ui.theme

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModelsTest {
    @Test
    fun dailyRandomThemeSeedIsStableForSameDate() {
        val date = LocalDate.of(2026, 5, 27)

        val first = dailyRandomThemeSeed(date)
        val second = dailyRandomThemeSeed(date)

        assertEquals(first.id, second.id)
    }

    @Test
    fun dailyRandomThemeSeedVariesAcrossConsecutiveDays() {
        val start = LocalDate.of(2026, 1, 1)

        val distinctThemeIds =
            (0 until ThemePresets.size)
                .map { offset -> dailyRandomThemeSeed(start.plusDays(offset.toLong())).id }
                .toSet()

        assertTrue(
            "Daily random should cover more than one preset across consecutive days.",
            distinctThemeIds.size > 1,
        )
    }

    @Test
    fun dailyRandomThemeIndexStaysHealthyAcrossDifferentPresetCounts() {
        val start = LocalDate.of(2026, 1, 1)

        for (presetCount in 2..128) {
            val sampleDays = maxOf(16, presetCount * 4)
            val distinctIndices =
                (0 until sampleDays)
                    .map { offset -> dailyRandomThemeIndex(start.plusDays(offset.toLong()), presetCount) }
                    .toSet()

            assertTrue(
                "Daily random should not collapse when presetCount=$presetCount.",
                distinctIndices.size >= minOf(presetCount, 4),
            )
        }
    }
}
