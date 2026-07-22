package com.rrrrz.tinyvow.data.activation

import android.app.Activity
import android.content.Context
import android.os.Build
import com.rrrrz.tinyvow.BuildConfig
import com.rrrrz.tinyvow.data.billing.ProEntitlementState
import com.rrrrz.tinyvow.data.billing.SubscriptionOffer
import com.rrrrz.tinyvow.data.billing.SubscriptionRepository
import com.rrrrz.tinyvow.data.payment.AlipayPaymentLauncher
import com.rrrrz.tinyvow.data.payment.PlatformAlipayPaymentLauncher
import com.rrrrz.tinyvow.data.payment.PendingPaymentAction
import com.rrrrz.tinyvow.data.payment.pendingPaymentAction
import com.rrrrz.tinyvow.data.server.BackendSessionResponse
import com.rrrrz.tinyvow.data.server.BackendSubscriptionStore
import com.rrrrz.tinyvow.data.server.HttpTinyVowBackendApi
import com.rrrrz.tinyvow.data.server.PendingBackendPayment
import com.rrrrz.tinyvow.data.server.TinyVowBackendApi
import com.rrrrz.tinyvow.data.server.TinyVowBackendException
import com.rrrrz.tinyvow.i18n.AppText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.delay
import java.util.UUID

class ChinaSubscriptionRepository(
    private val context: Context,
    private val localRepository: LocalActivationSubscriptionRepository,
    backendBaseUrl: String,
    private val nowMillis: () -> Long = { System.currentTimeMillis() },
    private val store: BackendSubscriptionStore = BackendSubscriptionStore(context),
    private val api: TinyVowBackendApi = HttpTinyVowBackendApi(
        baseUrl = backendBaseUrl,
        deviceName = listOf(Build.MANUFACTURER, Build.MODEL)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .takeIf { it.isNotBlank() },
        appVersion = BuildConfig.VERSION_NAME,
        channel = BuildConfig.STORE_CHANNEL,
    ),
    private val alipayPaymentLauncher: AlipayPaymentLauncher = PlatformAlipayPaymentLauncher(),
) : SubscriptionRepository {
    private val mutex = Mutex()
    private var boundUserId: String? = null
    private var backendEntitlement: ProEntitlementState? = null

    private val _entitlement = MutableStateFlow(ProEntitlementState.Free)
    override val entitlement: StateFlow<ProEntitlementState> = _entitlement.asStateFlow()

    private val _offers = MutableStateFlow<List<SubscriptionOffer>>(emptyList())
    override val offers: StateFlow<List<SubscriptionOffer>> = _offers.asStateFlow()

    suspend fun restorableUserId(): String? =
        localRepository.restorableUserId() ?: store.load()?.installId

    suspend fun bindUser(userId: String?) {
        boundUserId = userId
        localRepository.bindUser(userId)
        backendEntitlement = store.load()?.entitlement
        updateEffectiveEntitlement()
        refresh()
    }

    suspend fun activate(userId: String, code: String): Result<ProEntitlementState> =
        mutex.withLock {
            if (code.trim().startsWith(BACKEND_ACTIVATION_PREFIX, ignoreCase = true)) {
                activateBackend(userId, code)
            } else {
                val result = localRepository.activate(userId, code)
                if (result.isSuccess) {
                    updateEffectiveEntitlement()
                    claimLegacyActivationIfEligible(codeProof = code)
                }
                result.map { _entitlement.value }
            }
        }

    override suspend fun refresh(): Result<Unit> = mutex.withLock {
        localRepository.refresh()
        backendEntitlement = store.load()?.entitlement
        updateEffectiveEntitlement()

        val productsResult = runCatching { api.getProducts() }
        productsResult.getOrNull()?.let { _offers.value = it }

        val snapshot = store.load()
        if (snapshot == null) {
            return@withLock productsResult.map { Unit }
        }

        var paymentAccessToken = recoverPendingPayment(snapshot.accessToken, snapshot.installId)
        if (snapshot.account?.isRegistered == true) {
            paymentAccessToken = claimLegacyActivationIfEligible(
                accessToken = paymentAccessToken,
                installId = snapshot.installId,
            )
        }
        refreshBackendEntitlement(paymentAccessToken, snapshot.installId)
            .onSuccess { next ->
                backendEntitlement = next
                store.saveEntitlement(next)
                updateEffectiveEntitlement()
            }
            .map { Unit }
    }

    override suspend fun restore(): Result<Unit> = refresh()

    override suspend fun purchase(
        activity: Activity,
        offer: SubscriptionOffer,
        accountId: String?,
    ): Result<Unit> = runCatching {
        val userId = accountId ?: boundUserId
            ?: throw IllegalStateException(AppText.t("payment_error_account_unavailable"))
        val session = ensureBackendSession(userId)
        var paymentAccessToken = session.accessToken
        val clientRequestId = UUID.randomUUID().toString()
        val payment = try {
            api.createPaymentOrder(
                accessToken = session.accessToken,
                productId = offer.productId,
                provider = "ALIPAY",
                clientRequestId = clientRequestId,
            )
        } catch (error: TinyVowBackendException) {
            if (error.statusCode != 401) throw error
            val renewed = authenticate(userId)
            paymentAccessToken = renewed.accessToken
            api.createPaymentOrder(
                accessToken = renewed.accessToken,
                productId = offer.productId,
                provider = "ALIPAY",
                clientRequestId = clientRequestId,
            )
        }
        store.savePendingPayment(
            PendingBackendPayment(
                orderId = payment.order.orderId,
                productId = offer.productId,
                provider = "ALIPAY",
                createdAtMillis = nowMillis(),
            ),
        )
        val launchResult = alipayPaymentLauncher.launch(activity, payment.orderString)
        when {
            launchResult.wasCancelled -> {
                store.clearPendingPayment(payment.order.orderId)
                throw IllegalStateException(AppText.t("payment_cancelled"))
            }
            !launchResult.shouldConfirmWithServer -> {
                store.clearPendingPayment(payment.order.orderId)
                throw IllegalStateException(AppText.t("payment_error_launch_failed"))
            }
        }

        repeat(PAYMENT_CONFIRM_ATTEMPTS) {
            val order = api.getPaymentOrder(paymentAccessToken, payment.order.orderId)
            when (pendingPaymentAction(order.status)) {
                PendingPaymentAction.CLEAR_SUCCESS -> {
                    store.clearPendingPayment(payment.order.orderId)
                    refresh().getOrThrow()
                    return@runCatching
                }
                PendingPaymentAction.CLEAR_TERMINAL -> {
                    store.clearPendingPayment(payment.order.orderId)
                    throw IllegalStateException(AppText.t("payment_order_closed"))
                }
                PendingPaymentAction.KEEP -> Unit
            }
            delay(PAYMENT_CONFIRM_INTERVAL_MILLIS)
        }
        throw IllegalStateException(AppText.t("payment_confirmation_pending"))
    }.recoverCatching { error ->
        if (error is TinyVowBackendException) {
            throw IllegalStateException(paymentErrorMessage(error.errorCode), error)
        }
        throw error
    }

    override fun openManageSubscription(context: Context) = Unit

    suspend fun deleteAccount(): Result<Unit> = mutex.withLock {
        val snapshot = store.load()
        if (snapshot != null) {
            runCatching { api.deleteAccount(snapshot.accessToken) }
                .recoverCatching { error ->
                    if (error is TinyVowBackendException && error.statusCode == 401) Unit else throw error
                }
                .getOrElse { return@withLock Result.failure(it) }
        }
        store.clear()
        localRepository.clearActivationData()
        backendEntitlement = null
        updateEffectiveEntitlement()
        Result.success(Unit)
    }

    suspend fun clearLocalState() {
        store.clear()
        localRepository.clearActivationData()
        backendEntitlement = null
        updateEffectiveEntitlement()
    }

    private suspend fun activateBackend(userId: String, code: String): Result<ProEntitlementState> =
        runCatching {
            boundUserId = userId
            val session = ensureBackendSession(userId)
            val next = try {
                api.redeemActivationCode(session.accessToken, code)
            } catch (error: TinyVowBackendException) {
                if (error.statusCode != 401) throw error
                val renewed = authenticate(userId)
                api.redeemActivationCode(renewed.accessToken, code)
            }
            backendEntitlement = next
            store.saveEntitlement(next)
            updateEffectiveEntitlement()
            _entitlement.value
        }.recoverCatching { error ->
            if (error is TinyVowBackendException) {
                throw IllegalStateException(backendErrorMessage(error.errorCode), error)
            }
            throw IllegalStateException(AppText.t("activation_error_backend_unavailable"), error)
        }

    private suspend fun ensureBackendSession(userId: String): BackendSessionResponse {
        val stored = store.load()
        if (stored != null && stored.installId == userId) {
            return BackendSessionResponse(
                accessToken = stored.accessToken,
                userId = stored.serverUserId,
                deviceId = stored.deviceId,
                entitlement = stored.entitlement,
            )
        }
        return authenticate(userId)
    }

    private suspend fun authenticate(userId: String): BackendSessionResponse =
        api.authenticateAnonymous(
            installId = userId,
            deviceSecret = store.getOrCreateDeviceSecret(),
        ).also { store.saveSession(userId, it) }

    private suspend fun refreshBackendEntitlement(
        accessToken: String,
        installId: String,
    ): Result<ProEntitlementState> = runCatching {
        try {
            api.getEntitlement(accessToken)
        } catch (error: TinyVowBackendException) {
            if (error.statusCode != 401) throw error
            val renewed = authenticate(installId)
            renewed.entitlement
        }
    }

    private suspend fun claimLegacyActivationIfEligible(
        accessToken: String? = null,
        installId: String? = null,
        codeProof: String? = null,
    ): String {
        val stored = store.load() ?: return accessToken.orEmpty()
        if (stored.account?.isRegistered != true) return accessToken ?: stored.accessToken
        val claim = localRepository.legacyClaimSnapshot() ?: return accessToken ?: stored.accessToken
        if (claim.record.expiresAtMillis <= nowMillis()) return accessToken ?: stored.accessToken

        var currentAccessToken = accessToken ?: stored.accessToken
        val currentInstallId = installId ?: stored.installId
        val response = try {
            api.claimLegacyActivation(
                accessToken = currentAccessToken,
                localUserId = claim.record.userId,
                codeIds = claim.usedCodeIds.ifEmpty { setOf(claim.record.codeId) },
                activeCodeId = claim.record.codeId,
                activatedAtMillis = claim.record.activatedAtMillis,
                expiresAtMillis = claim.record.expiresAtMillis,
                codeProof = codeProof,
            )
        } catch (error: TinyVowBackendException) {
            if (error.statusCode == 401) {
                val renewed = authenticate(currentInstallId)
                currentAccessToken = renewed.accessToken
                runCatching {
                    api.claimLegacyActivation(
                        accessToken = currentAccessToken,
                        localUserId = claim.record.userId,
                        codeIds = claim.usedCodeIds.ifEmpty { setOf(claim.record.codeId) },
                        activeCodeId = claim.record.codeId,
                        activatedAtMillis = claim.record.activatedAtMillis,
                        expiresAtMillis = claim.record.expiresAtMillis,
                        codeProof = codeProof,
                    )
                }.getOrNull()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
        response?.let { claimed ->
            backendEntitlement = claimed.entitlement
            store.saveEntitlement(claimed.entitlement)
            updateEffectiveEntitlement()
        }
        return currentAccessToken
    }

    private suspend fun recoverPendingPayment(
        accessToken: String,
        installId: String,
    ): String {
        val pending = store.loadPendingPayment() ?: return accessToken
        var currentAccessToken = accessToken
        val order = try {
            api.getPaymentOrder(currentAccessToken, pending.orderId)
        } catch (error: TinyVowBackendException) {
            if (error.statusCode == 401) {
                val renewed = authenticate(installId)
                currentAccessToken = renewed.accessToken
                api.getPaymentOrder(currentAccessToken, pending.orderId)
            } else if (error.statusCode == 404) {
                store.clearPendingPayment(pending.orderId)
                return accessToken
            } else {
                throw error
            }
        }
        when (pendingPaymentAction(order.status)) {
            PendingPaymentAction.CLEAR_SUCCESS,
            PendingPaymentAction.CLEAR_TERMINAL -> store.clearPendingPayment(pending.orderId)
            PendingPaymentAction.KEEP -> Unit
        }
        return currentAccessToken
    }

    private fun updateEffectiveEntitlement() {
        _entitlement.value = resolveChinaEntitlement(
            local = localRepository.entitlement.value,
            backend = backendEntitlement,
            nowMillis = nowMillis(),
        )
    }

    private fun backendErrorMessage(errorCode: String): String =
        when (errorCode) {
            "activation_code_already_used" -> AppText.t("activation_error_code_already_used")
            "activation_code_expired" -> AppText.t("activation_error_expired_code")
            "activation_code_not_yet_valid" -> AppText.t("activation_error_not_yet_valid")
            "activation_code_disabled" -> AppText.t("activation_error_disabled")
            "product_unavailable" -> AppText.t("activation_error_product_unavailable")
            else -> AppText.t("activation_code_invalid")
        }

    private fun paymentErrorMessage(errorCode: String): String =
        when (errorCode) {
            "payment_provider_unavailable" -> AppText.t("payment_error_alipay_not_configured")
            "product_unavailable" -> AppText.t("activation_error_product_unavailable")
            "device_credential_invalid" -> AppText.t("payment_error_device_credential")
            else -> AppText.t("payment_error_create_order_failed")
        }

    private companion object {
        const val BACKEND_ACTIVATION_PREFIX = "TVB1-"
        const val PAYMENT_CONFIRM_ATTEMPTS = 15
        const val PAYMENT_CONFIRM_INTERVAL_MILLIS = 2_000L
    }
}
