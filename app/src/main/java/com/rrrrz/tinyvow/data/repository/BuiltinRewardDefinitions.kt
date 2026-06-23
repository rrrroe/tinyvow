package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.RewardType
import com.rrrrz.tinyvow.data.db.StreakShieldTarget

const val EMERGENCY_UNLOCK_REWARD_KEY = "reward_emergency_unlock_10"

internal val BUILTIN_REWARD_DEFINITIONS =
    listOf(
        BuiltinRewardDefinition(
            builtinKey = "reward_time_add_15",
            title = "Extra 15 minutes",
            description = "Buy now, use from inventory on one control group.",
            rewardType = RewardType.TIME_ADD,
            pointCost = 15,
            stock = -1,
            payload = RewardPayload(minutes = 15),
        ),
        BuiltinRewardDefinition(
            builtinKey = "reward_time_add_30",
            title = "Extra 30 minutes",
            description = "Buy now, use from inventory on one control group.",
            rewardType = RewardType.TIME_ADD,
            pointCost = 30,
            stock = -1,
            payload = RewardPayload(minutes = 30),
        ),
        BuiltinRewardDefinition(
            builtinKey = "reward_time_add_60",
            title = "Extra 60 minutes",
            description = "Buy now, use from inventory on one control group.",
            rewardType = RewardType.TIME_ADD,
            pointCost = 60,
            stock = -1,
            payload = RewardPayload(minutes = 60),
        ),
        BuiltinRewardDefinition(
            builtinKey = "reward_period_pass",
            title = "Current period free pass",
            description = "Use on one control group to skip blocking for the active window.",
            rewardType = RewardType.PERIOD_PASS,
            pointCost = 200,
            stock = -1,
        ),
        BuiltinRewardDefinition(
            builtinKey = EMERGENCY_UNLOCK_REWARD_KEY,
            title = "Emergency unlock 10 min",
            description = "Buy from the store first. Use only from the blocking overlay.",
            rewardType = RewardType.EMERGENCY_UNLOCK,
            pointCost = 1,
            stock = -1,
            payload = RewardPayload(minutes = 10),
        ),
        BuiltinRewardDefinition(
            builtinKey = "reward_streak_shield_control",
            title = "Control streak shield",
            description = "Protect one control streak break after archive review.",
            rewardType = RewardType.STREAK_SHIELD,
            pointCost = 100,
            stock = -1,
            payload = RewardPayload(shieldTarget = StreakShieldTarget.CONTROL_STREAK),
        ),
        BuiltinRewardDefinition(
            builtinKey = "reward_streak_shield_encourage",
            title = "Encourage streak shield",
            description = "Protect one encourage streak break after archive review.",
            rewardType = RewardType.STREAK_SHIELD,
            pointCost = 100,
            stock = -1,
            payload = RewardPayload(shieldTarget = StreakShieldTarget.ENCOURAGE_STREAK),
        ),
    )
