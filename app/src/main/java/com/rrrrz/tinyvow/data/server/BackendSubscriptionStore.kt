package com.rrrrz.tinyvow.data.server

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rrrrz.tinyvow.data.billing.ProEntitlementState
import com.rrrrz.tinyvow.data.billing.ProEntitlementStatus
import java.security.SecureRandom
import java.util.Base64
import kotlinx.coroutines.flow.first

private val Context.backendSubscriptionDataStore by preferencesDataStore(name = "backend_subscription_preferences")

data class BackendSubscriptionSnapshot(
    val installId: String,
    val accessToken: String,
    val serverUserId: String,
    val deviceId: String,
    val entitlement: ProEntitlementState,
)

data class PendingBackendPayment(
    val orderId: String,
    val productId: String,
    val provider: String,
    val createdAtMillis: Long,
)

class BackendSubscriptionStore(
    private val context: Context,
) {
    private object Keys {
        val installId = stringPreferencesKey("install_id")
        val accessToken = stringPreferencesKey("access_token")
        val serverUserId = stringPreferencesKey("server_user_id")
        val deviceId = stringPreferencesKey("device_id")
        val deviceSecret = stringPreferencesKey("device_secret")
        val pendingOrderId = stringPreferencesKey("pending_order_id")
        val pendingProductId = stringPreferencesKey("pending_product_id")
        val pendingProvider = stringPreferencesKey("pending_provider")
        val pendingCreatedAtMillis = longPreferencesKey("pending_created_at_millis")
        val entitlementStatus = stringPreferencesKey("entitlement_status")
        val productId = stringPreferencesKey("product_id")
        val entitlementSource = stringPreferencesKey("entitlement_source")
        val expiresAtMillis = longPreferencesKey("expires_at_millis")
    }

    suspend fun load(): BackendSubscriptionSnapshot? {
        val preferences = context.backendSubscriptionDataStore.data.first()
        val installId = preferences[Keys.installId] ?: return null
        val accessToken = preferences[Keys.accessToken] ?: return null
        val serverUserId = preferences[Keys.serverUserId] ?: return null
        val deviceId = preferences[Keys.deviceId] ?: return null
        val entitlement = ProEntitlementState(
            status = preferences[Keys.entitlementStatus]
                ?.let { runCatching { ProEntitlementStatus.valueOf(it) }.getOrNull() }
                ?: ProEntitlementStatus.FREE,
            productId = preferences[Keys.productId] ?: "tinyvow_pro",
            purchaseToken = "backend",
            source = preferences[Keys.entitlementSource],
            expiresAtMillis = preferences[Keys.expiresAtMillis],
        )
        return BackendSubscriptionSnapshot(installId, accessToken, serverUserId, deviceId, entitlement)
    }

    suspend fun saveSession(
        installId: String,
        response: BackendSessionResponse,
    ) {
        context.backendSubscriptionDataStore.edit { preferences ->
            preferences[Keys.installId] = installId
            preferences[Keys.accessToken] = response.accessToken
            preferences[Keys.serverUserId] = response.userId
            preferences[Keys.deviceId] = response.deviceId
            preferences.writeEntitlement(response.entitlement)
        }
    }

    suspend fun getOrCreateDeviceSecret(): String {
        val existing = context.backendSubscriptionDataStore.data.first()[Keys.deviceSecret]
        if (!existing.isNullOrBlank()) return existing
        val generated = ByteArray(32)
            .also { SecureRandom().nextBytes(it) }
            .let { Base64.getUrlEncoder().withoutPadding().encodeToString(it) }
        context.backendSubscriptionDataStore.edit { preferences ->
            if (preferences[Keys.deviceSecret].isNullOrBlank()) {
                preferences[Keys.deviceSecret] = generated
            }
        }
        return context.backendSubscriptionDataStore.data.first()[Keys.deviceSecret] ?: generated
    }

    suspend fun saveEntitlement(entitlement: ProEntitlementState) {
        context.backendSubscriptionDataStore.edit { it.writeEntitlement(entitlement) }
    }

    suspend fun loadPendingPayment(): PendingBackendPayment? {
        val preferences = context.backendSubscriptionDataStore.data.first()
        val orderId = preferences[Keys.pendingOrderId] ?: return null
        val productId = preferences[Keys.pendingProductId] ?: return null
        val provider = preferences[Keys.pendingProvider] ?: return null
        val createdAtMillis = preferences[Keys.pendingCreatedAtMillis] ?: return null
        return PendingBackendPayment(orderId, productId, provider, createdAtMillis)
    }

    suspend fun savePendingPayment(payment: PendingBackendPayment) {
        context.backendSubscriptionDataStore.edit { preferences ->
            preferences[Keys.pendingOrderId] = payment.orderId
            preferences[Keys.pendingProductId] = payment.productId
            preferences[Keys.pendingProvider] = payment.provider
            preferences[Keys.pendingCreatedAtMillis] = payment.createdAtMillis
        }
    }

    suspend fun clearPendingPayment(expectedOrderId: String? = null) {
        context.backendSubscriptionDataStore.edit { preferences ->
            if (expectedOrderId == null || preferences[Keys.pendingOrderId] == expectedOrderId) {
                preferences.remove(Keys.pendingOrderId)
                preferences.remove(Keys.pendingProductId)
                preferences.remove(Keys.pendingProvider)
                preferences.remove(Keys.pendingCreatedAtMillis)
            }
        }
    }

    suspend fun clear() = clearStoredData(context)

    private fun androidx.datastore.preferences.core.MutablePreferences.writeEntitlement(
        entitlement: ProEntitlementState,
    ) {
        this[Keys.entitlementStatus] = entitlement.status.name
        this[Keys.productId] = entitlement.productId
        entitlement.source?.let { this[Keys.entitlementSource] = it }
            ?: remove(Keys.entitlementSource)
        entitlement.expiresAtMillis?.let { this[Keys.expiresAtMillis] = it }
            ?: remove(Keys.expiresAtMillis)
    }

    companion object {
        suspend fun clearStoredData(context: Context) {
            context.backendSubscriptionDataStore.edit { it.clear() }
        }
    }
}
