package com.rrrrz.tinyvow.data.billing

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.StateFlow

interface SubscriptionRepository {
    val entitlement: StateFlow<ProEntitlementState>
    val offers: StateFlow<List<SubscriptionOffer>>

    suspend fun refresh(): Result<Unit>
    suspend fun restore(): Result<Unit>
    suspend fun purchase(activity: Activity, offer: SubscriptionOffer): Result<Unit>
    fun openManageSubscription(context: Context)
}
