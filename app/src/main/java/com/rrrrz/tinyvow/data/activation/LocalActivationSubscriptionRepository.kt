package com.rrrrz.tinyvow.data.activation

import android.app.Activity
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rrrrz.tinyvow.data.billing.ProEntitlementState
import com.rrrrz.tinyvow.data.billing.SubscriptionOffer
import com.rrrrz.tinyvow.data.billing.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val Context.activationDataStore by preferencesDataStore(name = "activation_preferences")

class LocalActivationSubscriptionRepository(
    private val context: Context,
    publicKeyBase64: String,
    private val nowMillis: () -> Long = { System.currentTimeMillis() },
) : SubscriptionRepository {
    private val verifier = ActivationCodeVerifier(publicKeyBase64)
    private val mutex = Mutex()
    private var boundUserId: String? = null

    private object Keys {
        val activationJson = stringPreferencesKey("activation_json")
        val usedCodeIds = stringSetPreferencesKey("used_code_ids")
        val lastSeenWallClockMillis = longPreferencesKey("last_seen_wall_clock_millis")
    }

    private val _entitlement = MutableStateFlow(ProEntitlementState.Free)
    override val entitlement: StateFlow<ProEntitlementState> = _entitlement.asStateFlow()

    private val _offers = MutableStateFlow<List<SubscriptionOffer>>(emptyList())
    override val offers: StateFlow<List<SubscriptionOffer>> = _offers.asStateFlow()

    suspend fun bindUser(userId: String?) {
        boundUserId = userId
        refresh()
    }

    suspend fun activate(userId: String, code: String): Result<ProEntitlementState> =
        mutex.withLock {
            runCatching {
                val now = nowMillis()
                val payload = verifier.verify(code)
                val preferences = context.activationDataStore.data.first()
                val usedCodeIds = preferences[Keys.usedCodeIds].orEmpty()
                payload.validateFor(userId = userId, nowMillis = now, usedCodeIds = usedCodeIds)

                val currentRecord = LocalActivationRecord.fromJsonString(preferences[Keys.activationJson])
                val expiresAtMillis = ActivationExpiryCalculator.extendFrom(
                    nowMillis = now,
                    currentExpiresAtMillis = currentRecord?.expiresAtMillis,
                    durationDays = payload.durationDays,
                )
                val record = LocalActivationRecord(
                    userId = userId,
                    codeId = payload.codeId,
                    productId = payload.productId,
                    channel = payload.channel,
                    durationDays = payload.durationDays,
                    activatedAtMillis = now,
                    expiresAtMillis = expiresAtMillis,
                )

                context.activationDataStore.edit { mutablePreferences ->
                    mutablePreferences[Keys.activationJson] = record.toJsonString()
                    mutablePreferences[Keys.usedCodeIds] = usedCodeIds + payload.codeId
                    mutablePreferences[Keys.lastSeenWallClockMillis] =
                        maxOf(preferences[Keys.lastSeenWallClockMillis] ?: 0L, now)
                }
                boundUserId = userId
                refresh().getOrThrow()
                _entitlement.value
            }
        }

    override suspend fun refresh(): Result<Unit> =
        runCatching {
            val preferences = context.activationDataStore.data.first()
            val record = LocalActivationRecord.fromJsonString(preferences[Keys.activationJson])
            val resolution = ActivationEntitlementResolver.resolve(
                record = record,
                userId = boundUserId,
                nowMillis = nowMillis(),
                lastSeenWallClockMillis = preferences[Keys.lastSeenWallClockMillis],
            )
            resolution.nextLastSeenWallClockMillis?.let { nextLastSeen ->
                if (nextLastSeen != preferences[Keys.lastSeenWallClockMillis]) {
                    context.activationDataStore.edit { it[Keys.lastSeenWallClockMillis] = nextLastSeen }
                }
            }
            _entitlement.value = resolution.entitlement
        }

    override suspend fun restore(): Result<Unit> = refresh()

    override suspend fun purchase(activity: Activity, offer: SubscriptionOffer): Result<Unit> =
        Result.failure(IllegalStateException("本渠道不使用 Google Play Billing"))

    override fun openManageSubscription(context: Context) = Unit

    suspend fun clearActivationData() {
        context.activationDataStore.edit { it.clear() }
        _entitlement.value = ProEntitlementState.Free
    }
}
