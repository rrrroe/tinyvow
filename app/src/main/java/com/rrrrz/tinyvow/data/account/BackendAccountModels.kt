package com.rrrrz.tinyvow.data.account

import com.rrrrz.tinyvow.data.billing.ProEntitlementState

data class BackendAccount(
    val userId: String,
    val accountType: String,
    val email: String?,
    val emailVerified: Boolean,
    val displayName: String?,
    val avatarUrl: String?,
    val createdAtMillis: Long,
    val registeredAtMillis: Long?,
    val lastLoginAtMillis: Long?,
    val totalSpentCents: Long,
    val paidOrderCount: Int,
    val recentLogins: List<BackendLoginEvent>,
    val entitlement: ProEntitlementState,
) {
    val isRegistered: Boolean
        get() = accountType != ACCOUNT_TYPE_ANONYMOUS

    companion object {
        const val ACCOUNT_TYPE_ANONYMOUS = "ANONYMOUS"
    }
}

data class BackendLoginEvent(
    val authMethod: String,
    val platform: String?,
    val deviceName: String?,
    val loggedInAtMillis: Long,
)
