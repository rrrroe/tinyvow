package com.rrrrz.tinyvow.data.repository

internal object RewardIconCatalog {
    const val TIME_ADD_PRESET_KEY = "reward_preset_03"
    const val PERIOD_PASS_PRESET_KEY = "reward_preset_05"
    const val EMERGENCY_UNLOCK_PRESET_KEY = "reward_preset_06"
    const val STREAK_SHIELD_PRESET_KEY = "reward_preset_08"
    const val DOUBLE_POINTS_PRESET_KEY = "reward_preset_09"

    private val reservedPresetKeys =
        setOf(
            TIME_ADD_PRESET_KEY,
            PERIOD_PASS_PRESET_KEY,
            EMERGENCY_UNLOCK_PRESET_KEY,
            STREAK_SHIELD_PRESET_KEY,
            DOUBLE_POINTS_PRESET_KEY,
        )

    val allPresetKeys: List<String> = (1..50).map { "reward_preset_%02d".format(it) }

    val customPresetKeys: List<String> = allPresetKeys.filterNot { it in reservedPresetKeys }

    private val builtinPresetByRewardKey =
        mapOf(
            "reward_time_add_15" to TIME_ADD_PRESET_KEY,
            "reward_time_add_30" to TIME_ADD_PRESET_KEY,
            "reward_time_add_60" to TIME_ADD_PRESET_KEY,
            "reward_period_pass" to PERIOD_PASS_PRESET_KEY,
            "reward_emergency_unlock_10" to EMERGENCY_UNLOCK_PRESET_KEY,
            "reward_streak_shield_control" to STREAK_SHIELD_PRESET_KEY,
            "reward_streak_shield_encourage" to STREAK_SHIELD_PRESET_KEY,
            "reward_double_points_day" to DOUBLE_POINTS_PRESET_KEY,
        )

    fun builtinPresetKeyFor(builtinKey: String?): String? = builtinKey?.let { builtinPresetByRewardKey[it] }

    fun isValidCustomPresetKey(key: String): Boolean = key in customPresetKeys

    fun presetOrdinal(key: String): Int? =
        key.removePrefix("reward_preset_").toIntOrNull()
}
