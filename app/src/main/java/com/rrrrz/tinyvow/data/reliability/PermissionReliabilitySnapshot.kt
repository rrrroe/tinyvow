package com.rrrrz.tinyvow.data.reliability

private const val ACCESSIBILITY_HEARTBEAT_FRESH_MS = 5 * 60 * 1000L

enum class StartupReliabilityStep {
    CREATE_FIRST_VOW,
    ENABLE_USAGE_ACCESS,
    ACCEPT_ACCESSIBILITY_DISCLOSURE,
    ENABLE_ACCESSIBILITY_SERVICE,
    CHECK_ACCESSIBILITY_HEALTH,
    READY,
}

data class PermissionReliabilitySnapshot(
    val hasAnyVow: Boolean,
    val controlGroupCount: Int,
    val encourageGroupCount: Int,
    val usageAccessGranted: Boolean,
    val accessibilityDisclosureAccepted: Boolean,
    val accessibilityServiceEnabled: Boolean,
    val notificationPermissionGranted: Boolean,
    val isIgnoringBatteryOptimizations: Boolean,
    val isAutoStartDismissed: Boolean,
    val lastAccessibilityHeartbeatAtMillis: Long?,
    val nowMillis: Long,
) {
    val accessibilityHeartbeatHealthy: Boolean
        get() =
            accessibilityServiceEnabled &&
                lastAccessibilityHeartbeatAtMillis != null &&
                nowMillis - lastAccessibilityHeartbeatAtMillis <= ACCESSIBILITY_HEARTBEAT_FRESH_MS

    val optionalSuggestionCount: Int
        get() =
            listOf(
                notificationPermissionGranted,
                isIgnoringBatteryOptimizations,
                isAutoStartDismissed,
            ).count { !it }

    val primaryStep: StartupReliabilityStep
        get() =
            when {
                !usageAccessGranted -> StartupReliabilityStep.ENABLE_USAGE_ACCESS
                !accessibilityDisclosureAccepted -> StartupReliabilityStep.ACCEPT_ACCESSIBILITY_DISCLOSURE
                !accessibilityServiceEnabled -> StartupReliabilityStep.ENABLE_ACCESSIBILITY_SERVICE
                !accessibilityHeartbeatHealthy -> StartupReliabilityStep.CHECK_ACCESSIBILITY_HEALTH
                !hasAnyVow -> StartupReliabilityStep.CREATE_FIRST_VOW
                else -> StartupReliabilityStep.READY
            }

    val coreReady: Boolean
        get() = primaryStep == StartupReliabilityStep.READY

    companion object {
        fun build(
            groups: List<com.rrrrz.tinyvow.data.repository.AppGroupWithApps>,
            usageAccessGranted: Boolean,
            accessibilityDisclosureAccepted: Boolean,
            accessibilityServiceEnabled: Boolean,
            notificationPermissionGranted: Boolean,
            isIgnoringBatteryOptimizations: Boolean,
            isAutoStartDismissed: Boolean,
            lastAccessibilityHeartbeatAtMillis: Long?,
            nowMillis: Long,
        ): PermissionReliabilitySnapshot {
            return PermissionReliabilitySnapshot(
                hasAnyVow = groups.isNotEmpty(),
                controlGroupCount = groups.count { it.group.type == com.rrrrz.tinyvow.data.db.GroupType.CONTROL },
                encourageGroupCount = groups.count { it.group.type == com.rrrrz.tinyvow.data.db.GroupType.ENCOURAGE },
                usageAccessGranted = usageAccessGranted,
                accessibilityDisclosureAccepted = accessibilityDisclosureAccepted,
                accessibilityServiceEnabled = accessibilityServiceEnabled,
                notificationPermissionGranted = notificationPermissionGranted,
                isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations,
                isAutoStartDismissed = isAutoStartDismissed,
                lastAccessibilityHeartbeatAtMillis = lastAccessibilityHeartbeatAtMillis,
                nowMillis = nowMillis,
            )
        }
    }
}
