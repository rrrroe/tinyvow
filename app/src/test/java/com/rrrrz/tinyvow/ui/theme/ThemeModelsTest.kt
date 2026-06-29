package com.rrrrz.tinyvow.ui.theme

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModelsTest {
    @Test
    fun currentPresetResolvesToItself() {
        val seed = resolveThemeSeed(
            selectedThemeId = "preset_soft_mist_blue",
            customThemes = emptyList(),
        )

        assertEquals("preset_soft_mist_blue", seed.id)
    }

    @Test
    fun removedPresetFallsBackToDefaultTheme() {
        val seed = resolveThemeSeed(
            selectedThemeId = "preset_fresh_glow_lavender_mint",
            customThemes = emptyList(),
        )

        assertEquals(DefaultThemeSeed.id, seed.id)
    }

    @Test
    fun removedMemberPresetFallsBackToDefaultTheme() {
        val seed = resolveThemeSeed(
            selectedThemeId = "member_noir_moss",
            customThemes = emptyList(),
        )

        assertEquals(DefaultThemeSeed.id, seed.id)
    }

    @Test
    fun oldDailyRandomThemeIdFallsBackToDefaultTheme() {
        val seed = resolveThemeSeed(
            selectedThemeId = DailyRandomThemeId,
            customThemes = emptyList(),
            today = LocalDate.of(2026, 1, 1),
        )

        assertEquals(DefaultThemeSeed.id, seed.id)
    }
}
