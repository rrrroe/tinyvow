package com.rrrrz.tinyvow.data.activation

import com.rrrrz.tinyvow.data.billing.ProEntitlementState
import com.rrrrz.tinyvow.data.billing.ProEntitlementStatus

internal fun resolveChinaEntitlement(
    local: ProEntitlementState,
    backend: ProEntitlementState?,
    nowMillis: Long,
): ProEntitlementState {
    val validBackend = backend?.takeIf { entitlement ->
        entitlement.status == ProEntitlementStatus.ACTIVE &&
            (entitlement.expiresAtMillis == null || entitlement.expiresAtMillis > nowMillis)
    }
    if (!local.isProActive) return validBackend ?: local
    if (validBackend == null) return local

    val localExpiry = local.expiresAtMillis ?: Long.MAX_VALUE
    val backendExpiry = validBackend.expiresAtMillis ?: Long.MAX_VALUE
    return if (backendExpiry > localExpiry) validBackend else local
}
