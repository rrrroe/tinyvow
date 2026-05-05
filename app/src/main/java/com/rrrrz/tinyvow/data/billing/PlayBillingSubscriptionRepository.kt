package com.rrrrz.tinyvow.data.billing

import com.rrrrz.tinyvow.i18n.AppText

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.MessageDigest
import kotlin.coroutines.resume

class PlayBillingSubscriptionRepository(
    context: Context,
) : SubscriptionRepository {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val productDetailsByOfferToken = mutableMapOf<String, ProductDetails>()

    private val _entitlement = MutableStateFlow(ProEntitlementState.Free)
    override val entitlement: StateFlow<ProEntitlementState> = _entitlement.asStateFlow()

    private val _offers = MutableStateFlow<List<SubscriptionOffer>>(emptyList())
    override val offers: StateFlow<List<SubscriptionOffer>> = _offers.asStateFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                scope.launch { processPurchases(purchases) }
            } else if (billingResult.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
                scope.launch { restore() }
            }
        }
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .build()

    override suspend fun refresh(): Result<Unit> = runCatching {
        val setup = ensureConnected()
        if (!setup.isOk()) {
            _entitlement.value = ProEntitlementState.unavailable(setup.debugMessage.ifBlank { AppText.t("billing_play_billing_is_temporarily_unavailable") })
            return@runCatching
        }

        val featureResult = billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
        if (!featureResult.isOk()) {
            _entitlement.value = ProEntitlementState.unavailable(AppText.t("billing_this_device_does_not_support_google_play_subscriptions"))
            _offers.value = emptyList()
            return@runCatching
        }

        val productDetails = queryProductDetails()
        updateOffers(productDetails)
        restore().getOrThrow()
        if (productDetails.isEmpty() && _entitlement.value.status == ProEntitlementStatus.FREE) {
            _entitlement.value = ProEntitlementState.unavailable(AppText.t("billing_the_tinyvow_pro_subscription_product_was_not_found"))
        }
    }

    override suspend fun restore(): Result<Unit> = runCatching {
        val setup = ensureConnected()
        if (!setup.isOk()) {
            _entitlement.value = ProEntitlementState.unavailable(setup.debugMessage.ifBlank { AppText.t("billing_play_billing_is_temporarily_unavailable") })
            return@runCatching
        }

        val purchases = queryPurchases()
        processPurchases(purchases)
    }

    override suspend fun purchase(activity: Activity, offer: SubscriptionOffer, accountId: String?): Result<Unit> = runCatching {
        val setup = ensureConnected()
        if (!setup.isOk()) {
            throw IllegalStateException(setup.debugMessage.ifBlank { AppText.t("billing_play_billing_is_temporarily_unavailable") })
        }

        updateOffers(queryProductDetails())
        val productDetails = productDetailsByOfferToken[offer.offerToken]
        if (productDetails == null) {
            throw IllegalStateException(AppText.t("billing_the_tinyvow_pro_subscription_product_was_not_found"))
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offer.offerToken)
            .build()
        val flowParamsBuilder = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
        accountId
            ?.takeIf { it.isNotBlank() }
            ?.let { flowParamsBuilder.setObfuscatedAccountId(it.sha256Hex()) }
        val result = billingClient.launchBillingFlow(
            activity,
            flowParamsBuilder.build(),
        )
        if (!result.isOk()) {
            throw IllegalStateException(result.debugMessage.ifBlank { AppText.t("billing_failed_to_start_purchase_flow") })
        }
    }

    override fun openManageSubscription(context: Context) {
        val uri = Uri.parse(
            "https://play.google.com/store/account/subscriptions?sku=$TINYVOW_PRO_PRODUCT_ID&package=${context.packageName}"
        )
        context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private suspend fun ensureConnected(): BillingResult {
        if (billingClient.isReady) {
            return BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build()
        }

        return suspendCancellableCoroutine { continuation ->
            billingClient.startConnection(
                object : BillingClientStateListener {
                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        if (continuation.isActive) {
                            continuation.resume(billingResult)
                        }
                    }

                    override fun onBillingServiceDisconnected() {
                        _entitlement.value = ProEntitlementState.unavailable(AppText.t("billing_play_billing_connection_was_lost"))
                    }
                }
            )
        }
    }

    private suspend fun queryProductDetails(): List<ProductDetails> =
        suspendCancellableCoroutine { continuation ->
            val product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(TINYVOW_PRO_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(listOf(product))
                .build()
            billingClient.queryProductDetailsAsync(params) { billingResult, queryResult ->
                if (billingResult.isOk()) {
                    continuation.resume(queryResult.productDetailsList)
                } else {
                    continuation.resume(emptyList())
                }
            }
        }

    private suspend fun queryPurchases(): List<Purchase> =
        suspendCancellableCoroutine { continuation ->
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
            billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
                if (billingResult.isOk()) {
                    continuation.resume(purchases)
                } else {
                    continuation.resume(emptyList())
                }
            }
        }

    private suspend fun processPurchases(purchases: List<Purchase>) {
        purchases
            .filter { TINYVOW_PRO_PRODUCT_ID in it.products }
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }
            .forEach { acknowledge(it) }

        val snapshots = purchases.map {
            PurchaseSnapshot(
                productIds = it.products,
                purchaseToken = it.purchaseToken,
                purchaseState = it.purchaseState,
                acknowledged = it.isAcknowledged,
                autoRenewing = it.isAutoRenewing,
            )
        }
        _entitlement.value = SubscriptionEntitlementResolver.resolve(snapshots)
    }

    private suspend fun acknowledge(purchase: Purchase) {
        suspendCancellableCoroutine<Unit> { continuation ->
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) {
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }
        }
    }

    private fun updateOffers(productDetailsList: List<ProductDetails>) {
        productDetailsByOfferToken.clear()
        val nextOffers = productDetailsList.flatMap { productDetails ->
            productDetails.subscriptionOfferDetails.orEmpty().mapNotNull { offerDetails ->
                val pricingPhase = offerDetails.pricingPhases.pricingPhaseList.lastOrNull()
                val offerToken = offerDetails.offerToken
                if (pricingPhase == null || offerToken.isBlank()) {
                    null
                } else {
                    productDetailsByOfferToken[offerToken] = productDetails
                    SubscriptionOffer(
                        id = offerToken,
                        productId = productDetails.productId,
                        offerToken = offerToken,
                        title = productDetails.title,
                        price = pricingPhase.formattedPrice,
                        billingPeriod = pricingPhase.billingPeriod,
                    )
                }
            }
        }
        _offers.value = nextOffers
    }

    private fun BillingResult.isOk(): Boolean =
        responseCode == BillingClient.BillingResponseCode.OK

    private fun String.sha256Hex(): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(toByteArray(Charsets.UTF_8))
        return bytes.joinToString(separator = "") { "%02x".format(it.toInt() and 0xff) }
    }
}
