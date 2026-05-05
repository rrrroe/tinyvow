package com.rrrrz.tinyvow.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RewardValidationTest {
    @Test
    fun blankTitleIsRejected() {
        assertEquals(
            RewardSaveValidationError.TITLE_REQUIRED,
            validateCustomRewardInput(
                title = "   ",
                pointCost = 100,
                stock = -1,
            ),
        )
    }

    @Test
    fun nonPositivePointCostIsRejected() {
        assertEquals(
            RewardSaveValidationError.POINT_COST_INVALID,
            validateCustomRewardInput(
                title = "Tea break",
                pointCost = 0,
                stock = -1,
            ),
        )
    }

    @Test
    fun nonPositiveFiniteStockIsRejected() {
        assertEquals(
            RewardSaveValidationError.STOCK_INVALID,
            validateCustomRewardInput(
                title = "Tea break",
                pointCost = 100,
                stock = 0,
            ),
        )
    }

    @Test
    fun validCustomRewardPassesValidation() {
        assertNull(
            validateCustomRewardInput(
                title = "Tea break",
                pointCost = 100,
                stock = 3,
            ),
        )
    }
}
