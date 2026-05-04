package com.rrrrz.tinyvow.data.billing

import com.rrrrz.tinyvow.i18n.AppText

import com.android.billingclient.api.Purchase

const val TINYVOW_PRO_PRODUCT_ID = "tinyvow_pro"

enum class ProEntitlementStatus {
    FREE,
    ACTIVE,
    PENDING,
    UNAVAILABLE,
}

data class ProEntitlementState(
    val status: ProEntitlementStatus,
    val productId: String = TINYVOW_PRO_PRODUCT_ID,
    val purchaseToken: String? = null,
    val message: String? = null,
) {
    val isProActive: Boolean
        get() = status == ProEntitlementStatus.ACTIVE

    companion object {
        val Free = ProEntitlementState(status = ProEntitlementStatus.FREE)

        fun active(purchaseToken: String) = ProEntitlementState(
            status = ProEntitlementStatus.ACTIVE,
            purchaseToken = purchaseToken,
        )

        fun pending() = ProEntitlementState(
            status = ProEntitlementStatus.PENDING,
            message = AppText.t("billing_subscription_payment_is_pending_pro_unlocks_automaticall"),
        )

        fun unavailable(message: String) = ProEntitlementState(
            status = ProEntitlementStatus.UNAVAILABLE,
            message = message,
        )
    }
}

data class SubscriptionOffer(
    val id: String,
    val productId: String,
    val offerToken: String,
    val title: String,
    val price: String,
    val billingPeriod: String,
)

data class PurchaseSnapshot(
    val productIds: List<String>,
    val purchaseToken: String,
    val purchaseState: Int,
    val acknowledged: Boolean,
    val autoRenewing: Boolean,
)

object SubscriptionEntitlementResolver {
    fun resolve(purchases: List<PurchaseSnapshot>): ProEntitlementState {
        val proPurchases = purchases.filter { TINYVOW_PRO_PRODUCT_ID in it.productIds }
        val purchased = proPurchases.firstOrNull {
            it.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        if (purchased != null) {
            return ProEntitlementState.active(purchased.purchaseToken)
        }

        val pending = proPurchases.any {
            it.purchaseState == Purchase.PurchaseState.PENDING
        }
        return if (pending) {
            ProEntitlementState.pending()
        } else {
            ProEntitlementState.Free
        }
    }
}
