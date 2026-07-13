package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.RedemptionEntity
import com.rrrrz.tinyvow.data.db.RewardType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardStoreAvailabilityTest {
    @Test
    fun evaluateRewardStoreAvailability_allowsAffordableUnlimitedCustomReward() {
        val availability =
            evaluateRewardStoreAvailability(
                item = storeItem(rewardType = RewardType.CUSTOM, stock = -1, pointCost = 20),
                userPoints = 20.0,
                controlGroupCount = 0,
                encourageGroupCount = 0,
            )

        assertTrue(availability.canPurchase)
        assertNull(availability.unavailableReason)
    }

    @Test
    fun evaluateRewardStoreAvailability_blocksOutOfStockBeforeOtherReasons() {
        val availability =
            evaluateRewardStoreAvailability(
                item = storeItem(
                    rewardType = RewardType.TIME_ADD,
                    stock = 0,
                    pointCost = 50,
                    purchasedTodayCount = 1,
                    builtinKey = "time_add",
                ),
                userPoints = 0.0,
                controlGroupCount = 0,
                encourageGroupCount = 0,
            )

        assertFalse(availability.canPurchase)
        assertEquals(RewardStoreUnavailableReason.OUT_OF_STOCK, availability.unavailableReason)
    }

    @Test
    fun evaluateRewardStoreAvailability_blocksBuiltinRewardAfterDailyPurchase() {
        val availability =
            evaluateRewardStoreAvailability(
                item = storeItem(
                    rewardType = RewardType.CUSTOM,
                    pointCost = 10,
                    purchasedTodayCount = 1,
                    builtinKey = "custom_builtin",
                ),
                userPoints = 100.0,
                controlGroupCount = 1,
                encourageGroupCount = 1,
            )

        assertFalse(availability.canPurchase)
        assertEquals(RewardStoreUnavailableReason.DAILY_LIMIT_REACHED, availability.unavailableReason)
    }

    @Test
    fun evaluateRewardStoreAvailability_requiresControlGroupForTimeRewards() {
        listOf(RewardType.TIME_ADD, RewardType.PERIOD_PASS).forEach { rewardType ->
            val availability =
                evaluateRewardStoreAvailability(
                    item = storeItem(rewardType = rewardType, pointCost = 10),
                    userPoints = 100.0,
                    controlGroupCount = 0,
                    encourageGroupCount = 1,
                )

            assertFalse(availability.canPurchase)
            assertEquals(RewardStoreUnavailableReason.NEEDS_CONTROL_GROUP, availability.unavailableReason)
        }
    }

    @Test
    fun evaluateRewardStoreAvailability_requiresEncourageGroupForDoublePoints() {
        val availability =
            evaluateRewardStoreAvailability(
                item = storeItem(rewardType = RewardType.DOUBLE_POINTS_DAY, pointCost = 10),
                userPoints = 100.0,
                controlGroupCount = 1,
                encourageGroupCount = 0,
            )

        assertFalse(availability.canPurchase)
        assertEquals(RewardStoreUnavailableReason.NEEDS_ENCOURAGE_GROUP, availability.unavailableReason)
    }

    @Test
    fun evaluateRewardStoreAvailability_blocksInsufficientPointsLast() {
        val availability =
            evaluateRewardStoreAvailability(
                item = storeItem(rewardType = RewardType.EMERGENCY_UNLOCK, pointCost = 100),
                userPoints = 99.0,
                controlGroupCount = 0,
                encourageGroupCount = 0,
            )

        assertFalse(availability.canPurchase)
        assertEquals(RewardStoreUnavailableReason.INSUFFICIENT_POINTS, availability.unavailableReason)
    }

    @Test
    fun isRewardVisibleInStore_hidesEmergencyUnlockBufferCard() {
        assertFalse(
            isRewardVisibleInStore(
                reward(
                    rewardType = RewardType.EMERGENCY_UNLOCK,
                    builtinKey = EMERGENCY_UNLOCK_REWARD_KEY,
                ),
            ),
        )
        assertTrue(
            isRewardVisibleInStore(
                reward(
                    rewardType = RewardType.TIME_ADD,
                    builtinKey = "reward_time_add_15",
                ),
            ),
        )
    }

    private fun storeItem(
        rewardType: RewardType,
        stock: Int = -1,
        pointCost: Int = 10,
        purchasedTodayCount: Int = 0,
        builtinKey: String? = null,
    ): RewardStoreItem =
        RewardStoreItem(
            reward = reward(rewardType = rewardType, stock = stock, pointCost = pointCost, builtinKey = builtinKey),
            ownedQuantity = 0,
            isManualUse = true,
            purchasedInLimitPeriodCount = purchasedTodayCount,
            purchaseLimitPeriod = RewardPurchaseLimitPeriod.DAILY,
        )

    private fun reward(
        rewardType: RewardType,
        stock: Int = -1,
        pointCost: Int = 10,
        builtinKey: String? = null,
    ): RedemptionEntity =
        RedemptionEntity(
            id = "reward-${rewardType.name}-${builtinKey.orEmpty()}",
            title = rewardType.name,
            description = rewardType.name,
            builtinKey = builtinKey,
            pointCost = pointCost,
            rewardType = rewardType,
            stock = stock,
            createdAt = 1L,
            updatedAt = 1L,
        )
}
