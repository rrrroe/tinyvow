package com.rrrrz.tinyvow.data.supermode

import com.rrrrz.tinyvow.data.db.RewardType

enum class GuardedAction {
    EDIT_GROUP,
    DELETE_GROUP,
    ADD_CUSTOM_REWARD,
    EDIT_CUSTOM_REWARD,
    EDIT_REWARD_PRICE,
    ;

    companion object {
        fun fromRewardType(rewardType: RewardType): GuardedAction? =
            when (rewardType) {
                RewardType.TIME_ADD,
                RewardType.PERIOD_PASS,
                RewardType.EMERGENCY_UNLOCK,
                RewardType.STREAK_SHIELD,
                RewardType.DOUBLE_POINTS_DAY,
                RewardType.CUSTOM,
                -> null
            }
    }
}

data class SuperModeStoredState(
    val enabled: Boolean = false,
    val passwordHash: String? = null,
    val passwordSalt: String? = null,
    val recoveryQuestion: String? = null,
    val recoveryAnswerHash: String? = null,
    val recoveryAnswerSalt: String? = null,
    val debugBypassActive: Boolean = false,
    val isActive: Boolean = false,
    val lastActiveAtMillis: Long? = null,
    val customWindowStartMinutes: Int? = null,
    val customWindowEndMinutes: Int? = null,
)

data class SuperModeStatus(
    val isConfigured: Boolean,
    val isEnabled: Boolean,
    val isActive: Boolean,
    val isAvailableNow: Boolean,
    val windowLabel: String,
    val windowStartMinutes: Int,
    val windowEndMinutes: Int,
    val expiresAt: Long?,
    val remainingMillis: Long,
)

sealed interface SuperModeEnterResult {
    data class Success(
        val status: SuperModeStatus,
    ) : SuperModeEnterResult

    data object NotConfigured : SuperModeEnterResult

    data object OutsideAllowedWindow : SuperModeEnterResult

    data object IncorrectPassword : SuperModeEnterResult
}

sealed interface SuperModeRecoveryResult {
    data object Success : SuperModeRecoveryResult

    data object NotConfigured : SuperModeRecoveryResult

    data object IncorrectAnswer : SuperModeRecoveryResult
}

sealed interface SuperModeWindowUpdateResult {
    data object Success : SuperModeWindowUpdateResult

    data object ProRequired : SuperModeWindowUpdateResult

    data object InvalidWindow : SuperModeWindowUpdateResult
}

enum class SuperModeExitReason {
    MANUAL,
    IDLE_TIMEOUT,
    BACKGROUND,
    OUTSIDE_ALLOWED_WINDOW,
}
