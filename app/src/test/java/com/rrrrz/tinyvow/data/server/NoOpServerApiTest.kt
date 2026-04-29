package com.rrrrz.tinyvow.data.server

import com.rrrrz.tinyvow.data.billing.ProEntitlementStatus
import com.rrrrz.tinyvow.data.billing.TINYVOW_PRO_PRODUCT_ID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoOpServerApiTest {
    @Test
    fun authExchangeReturnsLocalOnlySessionWithoutNetwork() = runBlocking {
        val result = NoOpAuthApi().exchangeGoogleSignIn(
            GoogleSignInExchangeRequest(
                googleIdToken = "id-token",
                googleSubject = "google-subject",
                email = "user@example.com",
                displayName = "User",
                deviceId = "device-1",
            )
        )

        val session = result.getOrThrow()
        assertTrue(session.localOnly)
        assertEquals("local:google-subject", session.userId)
        assertEquals("", session.accessToken)
    }

    @Test
    fun subscriptionApiKeepsFreeEntitlementInFirstClientOnlyVersion() = runBlocking {
        val api = NoOpSubscriptionApi()

        val verify = api.verifyGooglePlaySubscription(
            VerifyGooglePlaySubscriptionRequest(
                packageName = "com.rrrrz.tinyvow",
                productId = TINYVOW_PRO_PRODUCT_ID,
                purchaseToken = "purchase-token",
                userId = "local-user",
            )
        ).getOrThrow()
        val entitlement = api.getEntitlement(EntitlementQueryRequest(userId = "local-user")).getOrThrow()

        assertEquals(ProEntitlementStatus.FREE, verify.state.status)
        assertEquals(ProEntitlementStatus.FREE, entitlement.state.status)
        assertEquals("local-noop", verify.source)
    }

    @Test
    fun realtimeDeveloperNotificationContractIsAcceptedByNoOpImplementation() = runBlocking {
        val result = NoOpSubscriptionApi().handleRealtimeDeveloperNotification(
            RtdnSubscriptionNotification(
                packageName = "com.rrrrz.tinyvow",
                productId = TINYVOW_PRO_PRODUCT_ID,
                purchaseToken = "purchase-token",
                notificationType = 4,
                eventTimeMillis = 1_700_000_000_000L,
            )
        )

        assertTrue(result.isSuccess)
    }
}
