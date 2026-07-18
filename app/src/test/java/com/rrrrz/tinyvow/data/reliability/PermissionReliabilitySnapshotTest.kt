package com.rrrrz.tinyvow.data.reliability

import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionReliabilitySnapshotTest {
    @Test
    fun newUserCompletesCorePermissionsBeforeCreatingFirstVow() {
        assertEquals(
            StartupReliabilityStep.ENABLE_USAGE_ACCESS,
            snapshot().primaryStep,
        )
        assertEquals(
            StartupReliabilityStep.ACCEPT_ACCESSIBILITY_DISCLOSURE,
            snapshot(usageAccessGranted = true).primaryStep,
        )
        assertEquals(
            StartupReliabilityStep.ENABLE_ACCESSIBILITY_SERVICE,
            snapshot(
                usageAccessGranted = true,
                accessibilityDisclosureAccepted = true,
            ).primaryStep,
        )
        assertEquals(
            StartupReliabilityStep.CHECK_ACCESSIBILITY_HEALTH,
            snapshot(
                usageAccessGranted = true,
                accessibilityDisclosureAccepted = true,
                accessibilityServiceEnabled = true,
            ).primaryStep,
        )
        assertEquals(
            StartupReliabilityStep.CREATE_FIRST_VOW,
            snapshot(
                usageAccessGranted = true,
                accessibilityDisclosureAccepted = true,
                accessibilityServiceEnabled = true,
                lastAccessibilityHeartbeatAtMillis = NOW_MILLIS,
            ).primaryStep,
        )
    }

    private fun snapshot(
        usageAccessGranted: Boolean = false,
        accessibilityDisclosureAccepted: Boolean = false,
        accessibilityServiceEnabled: Boolean = false,
        lastAccessibilityHeartbeatAtMillis: Long? = null,
    ): PermissionReliabilitySnapshot =
        PermissionReliabilitySnapshot(
            hasAnyVow = false,
            controlGroupCount = 0,
            encourageGroupCount = 0,
            usageAccessGranted = usageAccessGranted,
            accessibilityDisclosureAccepted = accessibilityDisclosureAccepted,
            accessibilityServiceEnabled = accessibilityServiceEnabled,
            notificationPermissionGranted = false,
            isIgnoringBatteryOptimizations = false,
            isAutoStartDismissed = false,
            lastAccessibilityHeartbeatAtMillis = lastAccessibilityHeartbeatAtMillis,
            nowMillis = NOW_MILLIS,
        )

    private companion object {
        const val NOW_MILLIS = 1_000_000L
    }
}
