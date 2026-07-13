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
    val purchasedInLimitPeriodCount: Int,
    val purchaseLimitPeriod: RewardPurchaseLimitPeriod,
)

enum class RewardPurchaseLimitPeriod {
    DAILY,
    MONTHLY,
}

enum class RewardStoreUnavailableReason {
    OUT_OF_STOCK,
    DAILY_LIMIT_REACHED,
    NEEDS_CONTROL_GROUP,
    NEEDS_ENCOURAGE_GROUP,
    INSUFFICIENT_POINTS,
}

data class RewardStoreAvailability(
    val canAfford: Boolean,
    val inStock: Boolean,
    val dailyLimitReached: Boolean,
    val needsControlGroups: Boolean,
    val needsEncourageGroups: Boolean,
) {
    val canPurchase: Boolean =
        canAfford && inStock && !dailyLimitReached && !needsControlGroups && !needsEncourageGroups

    val unavailableReason: RewardStoreUnavailableReason? =
        when {
            !inStock -> RewardStoreUnavailableReason.OUT_OF_STOCK
            dailyLimitReached -> RewardStoreUnavailableReason.DAILY_LIMIT_REACHED
            needsControlGroups -> RewardStoreUnavailableReason.NEEDS_CONTROL_GROUP
            needsEncourageGroups -> RewardStoreUnavailableReason.NEEDS_ENCOURAGE_GROUP
            !canAfford -> RewardStoreUnavailableReason.INSUFFICIENT_POINTS
            else -> null
        }
}

fun evaluateRewardStoreAvailability(
    item: RewardStoreItem,
    userPoints: Double,
    controlGroupCount: Int,
    encourageGroupCount: Int,
): RewardStoreAvailability {
    val reward = item.reward
    val needsControlGroups =
        (reward.rewardType == RewardType.TIME_ADD || reward.rewardType == RewardType.PERIOD_PASS) &&
            controlGroupCount == 0
    val needsEncourageGroups =
        reward.rewardType == RewardType.DOUBLE_POINTS_DAY && encourageGroupCount == 0
    return RewardStoreAvailability(
        canAfford = userPoints >= reward.pointCost,
        inStock = reward.stock == -1 || reward.stock > 0,
        dailyLimitReached = reward.builtinKey != null && item.purchasedInLimitPeriodCount >= 1,
        needsControlGroups = needsControlGroups,
        needsEncourageGroups = needsEncourageGroups,
    )
}

data class InventoryRewardItem(
    val reward: RedemptionEntity,
    val quantity: Int,
    val activeCount: Int,
    val pendingCount: Int,
    val shieldTarget: StreakShieldTarget? = null,
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

    data object MonthlyLimitReached : PurchaseRewardResult

    data object InvalidReward : PurchaseRewardResult
}

sealed interface UseRewardResult {
    data class Success(
        val rewardTitle: String,
        val messageKey: String,
        val messageArgs: List<Any> = emptyList(),
        val pendingEffectId: String? = null,
    ) : UseRewardResult

    data object NotOwned : UseRewardResult

    data object InvalidTargetGroup : UseRewardResult

    data object AlreadyActive : UseRewardResult

    data object PeriodPassAlreadyActive : UseRewardResult

    data object DoublePointsAlreadyActive : UseRewardResult

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
