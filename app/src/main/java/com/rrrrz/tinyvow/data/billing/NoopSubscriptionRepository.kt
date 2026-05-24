package com.rrrrz.tinyvow.data.billing

import android.app.Activity
import android.content.Context
import com.rrrrz.tinyvow.i18n.AppText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NoopSubscriptionRepository : SubscriptionRepository {
    private val _entitlement = MutableStateFlow(ProEntitlementState.Free)
    override val entitlement: StateFlow<ProEntitlementState> = _entitlement.asStateFlow()

    private val _offers = MutableStateFlow<List<SubscriptionOffer>>(emptyList())
    override val offers: StateFlow<List<SubscriptionOffer>> = _offers.asStateFlow()

    override suspend fun refresh(): Result<Unit> = Result.success(Unit)

    override suspend fun restore(): Result<Unit> = Result.success(Unit)

    override suspend fun purchase(activity: Activity, offer: SubscriptionOffer, accountId: String?): Result<Unit> =
        Result.failure(IllegalStateException(AppText.t("billing_error_play_billing_disabled_for_channel")))

    override fun openManageSubscription(context: Context) = Unit
}
