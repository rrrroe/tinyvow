package com.rrrrz.tinyvow.data.activation

import com.rrrrz.tinyvow.data.billing.ProEntitlementStatus
import com.rrrrz.tinyvow.data.billing.TINYVOW_PRO_PRODUCT_ID
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivationEntitlementResolverTest {
    @Test
    fun activeRecordResolvesToActiveEntitlement() {
        val resolution = ActivationEntitlementResolver.resolve(
            record = record(expiresAtMillis = 2_000L),
            userId = "user-1",
            nowMillis = 1_000L,
            lastSeenWallClockMillis = 900L,
        )

        assertEquals(ProEntitlementStatus.ACTIVE, resolution.entitlement.status)
        assertEquals(2_000L, resolution.entitlement.expiresAtMillis)
        assertEquals(ACTIVATION_SOURCE_LOCAL, resolution.entitlement.source)
        assertEquals(1_000L, resolution.nextLastSeenWallClockMillis)
    }

    @Test
    fun expiredRecordResolvesToFree() {
        val resolution = ActivationEntitlementResolver.resolve(
            record = record(expiresAtMillis = 999L),
            userId = "user-1",
            nowMillis = 1_000L,
            lastSeenWallClockMillis = 900L,
        )

        assertEquals(ProEntitlementStatus.FREE, resolution.entitlement.status)
    }

    @Test
    fun timeRollbackPausesPro() {
        val resolution = ActivationEntitlementResolver.resolve(
            record = record(expiresAtMillis = 2_000_000L),
            userId = "user-1",
            nowMillis = 1_000L,
            lastSeenWallClockMillis = 1_000L + ACTIVATION_TIME_ROLLBACK_TOLERANCE_MILLIS + 1,
        )

        assertEquals(ProEntitlementStatus.UNAVAILABLE, resolution.entitlement.status)
        assertEquals(1_000L + ACTIVATION_TIME_ROLLBACK_TOLERANCE_MILLIS + 1, resolution.nextLastSeenWallClockMillis)
    }

    @Test
    fun activeRecordProvidesRestorableUserId() {
        val userId = ActivationEntitlementResolver.restorableUserId(
            record = record(expiresAtMillis = 2_000L),
            nowMillis = 1_000L,
            lastSeenWallClockMillis = 900L,
        )

        assertEquals("user-1", userId)
    }

    @Test
    fun expiredRecordDoesNotProvideRestorableUserId() {
        val userId = ActivationEntitlementResolver.restorableUserId(
            record = record(expiresAtMillis = 999L),
            nowMillis = 1_000L,
            lastSeenWallClockMillis = 900L,
        )

        assertEquals(null, userId)
    }

    @Test
    fun activationExtendsFromCurrentExpiryWhenAlreadyActive() {
        val now = 1_000L
        val currentExpiry = now + 5 * 86_400_000L

        val nextExpiry = ActivationExpiryCalculator.extendFrom(
            nowMillis = now,
            currentExpiresAtMillis = currentExpiry,
            durationDays = 30,
        )

        assertEquals(currentExpiry + 30 * 86_400_000L, nextExpiry)
    }

    private fun record(expiresAtMillis: Long) = LocalActivationRecord(
        userId = "user-1",
        codeId = "code-1",
        productId = TINYVOW_PRO_PRODUCT_ID,
        channel = ACTIVATION_CHANNEL_CHINA,
        durationDays = 30,
        activatedAtMillis = 500L,
        expiresAtMillis = expiresAtMillis,
    )
}
