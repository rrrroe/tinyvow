package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.data.db.SpecialAppUsagePreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpecialAppReplacementStatusTest {
    @Test
    fun activeRequiresKeySuccessfulSyncScopeAndSyncEnabled() {
        val status =
            buildSpecialAppReplacementStatus(
                hasApiKey = true,
                lastSuccessAt = 100L,
                enabledForControl = true,
                enabledForEncourage = false,
                syncEnabled = true,
                usagePreference = SpecialAppUsagePreference.READING_FIRST,
            )

        assertEquals(SpecialAppReplacementStatusType.ACTIVE, status.type)
        assertTrue(status.active)
        assertTrue(status.controlEnabled)
        assertFalse(status.encourageEnabled)
    }

    @Test
    fun missingKeyIsReportedBeforeOtherMissingSetup() {
        val status =
            buildSpecialAppReplacementStatus(
                hasApiKey = false,
                lastSuccessAt = 0L,
                enabledForControl = false,
                enabledForEncourage = false,
                syncEnabled = false,
                usagePreference = SpecialAppUsagePreference.READING_FIRST,
            )

        assertEquals(SpecialAppReplacementStatusType.NEEDS_KEY, status.type)
        assertFalse(status.active)
    }

    @Test
    fun successfulSyncIsRequiredBeforeScopesCanBecomeActive() {
        val status =
            buildSpecialAppReplacementStatus(
                hasApiKey = true,
                lastSuccessAt = 0L,
                enabledForControl = true,
                enabledForEncourage = true,
                syncEnabled = true,
                usagePreference = SpecialAppUsagePreference.PHONE_FIRST,
            )

        assertEquals(SpecialAppReplacementStatusType.NEEDS_SYNC, status.type)
        assertEquals(SpecialAppUsagePreference.PHONE_FIRST, status.usagePreference)
    }

    @Test
    fun atLeastOneScopeMustBeSelected() {
        val status =
            buildSpecialAppReplacementStatus(
                hasApiKey = true,
                lastSuccessAt = 100L,
                enabledForControl = false,
                enabledForEncourage = false,
                syncEnabled = false,
                usagePreference = SpecialAppUsagePreference.READING_FIRST,
            )

        assertEquals(SpecialAppReplacementStatusType.NEEDS_SCOPE, status.type)
    }

    @Test
    fun disabledSyncKeepsLegacyInconsistentConfigInactive() {
        val status =
            buildSpecialAppReplacementStatus(
                hasApiKey = true,
                lastSuccessAt = 100L,
                enabledForControl = true,
                enabledForEncourage = false,
                syncEnabled = false,
                usagePreference = SpecialAppUsagePreference.READING_FIRST,
            )

        assertEquals(SpecialAppReplacementStatusType.INACTIVE, status.type)
        assertFalse(status.active)
    }
}
