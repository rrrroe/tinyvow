package com.rrrrz.tinyvow.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardIconCatalogTest {
    @Test
    fun builtinRewardsResolveToReservedPresetKeys() {
        assertEquals(RewardIconCatalog.TIME_ADD_PRESET_KEY, RewardIconCatalog.builtinPresetKeyFor("reward_time_add_15"))
        assertEquals(RewardIconCatalog.PERIOD_PASS_PRESET_KEY, RewardIconCatalog.builtinPresetKeyFor("reward_period_pass"))
        assertEquals(RewardIconCatalog.EMERGENCY_UNLOCK_PRESET_KEY, RewardIconCatalog.builtinPresetKeyFor("reward_emergency_unlock_10"))
        assertEquals(RewardIconCatalog.STREAK_SHIELD_PRESET_KEY, RewardIconCatalog.builtinPresetKeyFor("reward_streak_shield_control"))
        assertEquals(RewardIconCatalog.DOUBLE_POINTS_PRESET_KEY, RewardIconCatalog.builtinPresetKeyFor("reward_double_points_day"))
    }

    @Test
    fun reservedBuiltInPresetKeysAreExcludedFromCustomPicker() {
        assertFalse(RewardIconCatalog.isValidCustomPresetKey(RewardIconCatalog.TIME_ADD_PRESET_KEY))
        assertFalse(RewardIconCatalog.isValidCustomPresetKey(RewardIconCatalog.PERIOD_PASS_PRESET_KEY))
        assertFalse(RewardIconCatalog.isValidCustomPresetKey(RewardIconCatalog.EMERGENCY_UNLOCK_PRESET_KEY))
        assertFalse(RewardIconCatalog.isValidCustomPresetKey(RewardIconCatalog.STREAK_SHIELD_PRESET_KEY))
        assertFalse(RewardIconCatalog.isValidCustomPresetKey(RewardIconCatalog.DOUBLE_POINTS_PRESET_KEY))
        assertTrue(RewardIconCatalog.isValidCustomPresetKey(RewardIconCatalog.customPresetKeys.first()))
    }

    @Test
    fun presetOrdinalReadsStableNumericSuffix() {
        assertEquals(12, RewardIconCatalog.presetOrdinal("reward_preset_12"))
        assertEquals(null, RewardIconCatalog.presetOrdinal("reward_icon_demo"))
    }
}
