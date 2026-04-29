package com.rrrrz.tinyvow.data.server

import com.rrrrz.tinyvow.data.billing.ProEntitlementState
import com.rrrrz.tinyvow.data.billing.TINYVOW_PRO_PRODUCT_ID

data class VerifyGooglePlaySubscriptionRequest(
    val packageName: String,
    val productId: String,
    val purchaseToken: String,
    val userId: String?,
)

data class EntitlementQueryRequest(
    val userId: String,
    val productId: String = TINYVOW_PRO_PRODUCT_ID,
)

data class RtdnSubscriptionNotification(
    val packageName: String,
    val productId: String,
    val purchaseToken: String,
    val notificationType: Int,
    val eventTimeMillis: Long,
)

data class BackendEntitlement(
    val state: ProEntitlementState,
    val expiresAtMillis: Long?,
    val source: String,
)

interface SubscriptionApi {
    suspend fun verifyGooglePlaySubscription(request: VerifyGooglePlaySubscriptionRequest): Result<BackendEntitlement>
    suspend fun getEntitlement(request: EntitlementQueryRequest): Result<BackendEntitlement>
    suspend fun handleRealtimeDeveloperNotification(notification: RtdnSubscriptionNotification): Result<Unit>
}

class NoOpSubscriptionApi : SubscriptionApi {
    override suspend fun verifyGooglePlaySubscription(
        request: VerifyGooglePlaySubscriptionRequest,
    ): Result<BackendEntitlement> =
        Result.success(
            BackendEntitlement(
                state = ProEntitlementState.Free,
                expiresAtMillis = null,
                source = "local-noop",
            )
        )

    override suspend fun getEntitlement(request: EntitlementQueryRequest): Result<BackendEntitlement> =
        Result.success(
            BackendEntitlement(
                state = ProEntitlementState.Free,
                expiresAtMillis = null,
                source = "local-noop",
            )
        )

    override suspend fun handleRealtimeDeveloperNotification(
        notification: RtdnSubscriptionNotification,
    ): Result<Unit> =
        Result.success(Unit)
}
