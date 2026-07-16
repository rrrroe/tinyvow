package com.rrrrz.tinyvow.data.reminder

import android.content.Context
import com.rrrrz.tinyvow.BuildConfig
import com.rrrrz.tinyvow.data.activation.ChinaSubscriptionRepository
import com.rrrrz.tinyvow.data.activation.LocalActivationSubscriptionRepository
import com.rrrrz.tinyvow.data.auth.LocalAuthRepository
import com.rrrrz.tinyvow.data.billing.PlayBillingSubscriptionRepository
import com.rrrrz.tinyvow.data.billing.ProEntitlementStatus
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences

internal suspend fun resolveReminderProActive(
    context: Context,
    preferences: ManagedAppPreferences,
    nowMillis: Long = System.currentTimeMillis(),
): Boolean {
    val debugExpiresAt = preferences.getDebugProExpiresAtMillisOnce()
    if (BuildConfig.DEBUG && debugExpiresAt != null && debugExpiresAt > nowMillis) {
        return true
    }

    return when {
        BuildConfig.ENABLE_LOCAL_ACTIVATION -> {
            val authRepository = LocalAuthRepository(context)
            val localRepository = LocalActivationSubscriptionRepository(
                context = context,
                publicKeyBase64 = BuildConfig.ACTIVATION_PUBLIC_KEY_BASE64,
            )
            val repository = ChinaSubscriptionRepository(
                context = context,
                localRepository = localRepository,
                backendBaseUrl = BuildConfig.TINYVOW_BACKEND_BASE_URL,
            )
            val session = authRepository.ensureLocalSession(
                preferredUserId = repository.restorableUserId(),
            )
            repository.bindUser(session.userId)
            repository.entitlement.value.status == ProEntitlementStatus.ACTIVE
        }
        BuildConfig.ENABLE_PLAY_BILLING -> {
            val repository = PlayBillingSubscriptionRepository(context)
            repository.restore().getOrNull()
            repository.entitlement.value.status == ProEntitlementStatus.ACTIVE
        }
        else -> false
    }
}
