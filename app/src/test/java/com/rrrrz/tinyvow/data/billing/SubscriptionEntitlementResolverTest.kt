package com.rrrrz.tinyvow.data.billing

import com.android.billingclient.api.Purchase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubscriptionEntitlementResolverTest {
    @Test
    fun purchasedProUnlocksActiveEntitlement() {
        val state = SubscriptionEntitlementResolver.resolve(
            listOf(
                PurchaseSnapshot(
                    productIds = listOf(TINYVOW_PRO_PRODUCT_ID),
                    purchaseToken = "token-1",
                    purchaseState = Purchase.PurchaseState.PURCHASED,
                    acknowledged = false,
                    autoRenewing = true,
                )
            )
        )

        assertEquals(ProEntitlementStatus.ACTIVE, state.status)
        assertTrue(state.isProActive)
        assertEquals("token-1", state.purchaseToken)
    }

    @Test
    fun pendingProDoesNotUnlockActiveEntitlement() {
        val state = SubscriptionEntitlementResolver.resolve(
            listOf(
                PurchaseSnapshot(
                    productIds = listOf(TINYVOW_PRO_PRODUCT_ID),
                    purchaseToken = "token-2",
                    purchaseState = Purchase.PurchaseState.PENDING,
                    acknowledged = false,
                    autoRenewing = false,
                )
            )
        )

        assertEquals(ProEntitlementStatus.PENDING, state.status)
        assertEquals(false, state.isProActive)
    }

    @Test
    fun unrelatedPurchaseKeepsFreeEntitlement() {
        val state = SubscriptionEntitlementResolver.resolve(
            listOf(
                PurchaseSnapshot(
                    productIds = listOf("other_product"),
                    purchaseToken = "token-3",
                    purchaseState = Purchase.PurchaseState.PURCHASED,
                    acknowledged = true,
                    autoRenewing = true,
                )
            )
        )

        assertEquals(ProEntitlementStatus.FREE, state.status)
    }
}
