package com.rrrrz.tinyvow.data.activation

import com.rrrrz.tinyvow.data.billing.ProEntitlementState
import com.rrrrz.tinyvow.data.billing.ProEntitlementStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ChinaSubscriptionPolicyTest {
    @Test
    fun `local active entitlement survives missing backend state`() {
        val local = active(expiresAt = 2_000, source = "local_activation")
        assertEquals(local, resolveChinaEntitlement(local, backend = null, nowMillis = 1_000))
    }

    @Test
    fun `backend active entitlement unlocks when local is free`() {
        val backend = active(expiresAt = 2_000, source = "activation_code")
        assertEquals(backend, resolveChinaEntitlement(ProEntitlementState.Free, backend, nowMillis = 1_000))
    }

    @Test
    fun `expired cached backend entitlement does not unlock pro`() {
        val backend = active(expiresAt = 999, source = "activation_code")
        assertEquals(
            ProEntitlementState.Free,
            resolveChinaEntitlement(ProEntitlementState.Free, backend, nowMillis = 1_000),
        )
    }

    @Test
    fun `later entitlement wins without weakening permanent local access`() {
        val backend = active(expiresAt = 3_000, source = "activation_code")
        val permanentLocal = active(expiresAt = null, source = "local_activation")
        assertEquals(
            permanentLocal,
            resolveChinaEntitlement(permanentLocal, backend, nowMillis = 1_000),
        )
    }

    private fun active(expiresAt: Long?, source: String) =
        ProEntitlementState(
            status = ProEntitlementStatus.ACTIVE,
            purchaseToken = source,
            expiresAtMillis = expiresAt,
            source = source,
        )
}
