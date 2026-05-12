package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.AppGroupEntity
import com.rrrrz.tinyvow.data.db.RedemptionEntity
import com.rrrrz.tinyvow.data.db.RewardIconSource
import com.rrrrz.tinyvow.data.db.RewardType
import com.rrrrz.tinyvow.data.db.StreakShieldPendingEntity
import com.rrrrz.tinyvow.data.db.StreakShieldTarget

data class AppGroupWithApps(
    val group: AppGroupEntity,
    val packageNames: List<String>,
)

data class RewardPayload(
    val minutes: Int = 0,
    val pointsMultiplier: Double = 1.0,
    val shieldTarget: StreakShieldTarget? = null,
)

data class RewardIconSpec(
    val source: RewardIconSource,
    val value: String,
)

data class CustomRewardDraft(
    val title: String,
    val pointCost: Int,
    val stock: Int,
    val description: String,
    val iconSpec: RewardIconSpec? = null,
)

data class RewardStoreItem(
    val reward: RedemptionEntity,
    val ownedQuantity: Int,
    val isManualUse: Boolean,
    val purchasedTodayCount: Int,
)

data class InventoryRewardItem(
    val reward: RedemptionEntity,
    val quantity: Int,
    val activeCount: Int,
    val pendingCount: Int,
)

data class PendingStreakShieldItem(
    val pending: StreakShieldPendingEntity,
    val title: String,
    val ownedQuantity: Int,
)

sealed interface InventoryRecordTab {
    data object Items : InventoryRecordTab

    data object Purchases : InventoryRecordTab

    data object Uses : InventoryRecordTab
}

sealed interface PurchaseRewardResult {
    data class Success(
        val rewardTitle: String,
        val pointCost: Int,
    ) : PurchaseRewardResult

    data object InsufficientPoints : PurchaseRewardResult

    data object OutOfStock : PurchaseRewardResult

    data object DailyLimitReached : PurchaseRewardResult

    data object InvalidReward : PurchaseRewardResult
}

sealed interface UseRewardResult {
    data class Success(
        val rewardTitle: String,
        val messageKey: String,
        val messageArgs: List<Any> = emptyList(),
    ) : UseRewardResult

    data object NotOwned : UseRewardResult

    data object InvalidTargetGroup : UseRewardResult

    data object AlreadyActive : UseRewardResult

    data object AlreadyCompleted : UseRewardResult

    data object InvalidReward : UseRewardResult
}

enum class RewardSaveValidationError {
    TITLE_REQUIRED,
    POINT_COST_INVALID,
    STOCK_INVALID,
    ICON_INVALID,
    REWARD_NOT_EDITABLE,
}

sealed interface RewardSaveResult {
    data object Success : RewardSaveResult

    data class Invalid(
        val error: RewardSaveValidationError,
    ) : RewardSaveResult
}

internal data class BuiltinRewardDefinition(
    val builtinKey: String,
    val title: String,
    val description: String,
    val rewardType: RewardType,
    val pointCost: Int,
    val stock: Int,
    val payload: RewardPayload = RewardPayload(),
)

data class AchievementProgress(
    val earnedPointsTotal: Double = 0.0,
    val redeemedPointsTotal: Double = 0.0,
    val controlDaysTotal: Int = 0,
    val controlStreak: Int = 0,
    val encourageDaysTotal: Int = 0,
    val encourageStreak: Int = 0,
)
