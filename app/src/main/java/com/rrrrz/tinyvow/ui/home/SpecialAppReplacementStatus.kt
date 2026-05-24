package com.rrrrz.tinyvow.ui.home

import com.rrrrz.tinyvow.data.db.SpecialAppUsagePreference

internal enum class SpecialAppReplacementStatusType {
    ACTIVE,
    NEEDS_KEY,
    NEEDS_SYNC,
    NEEDS_SCOPE,
    INACTIVE,
}

internal data class SpecialAppReplacementStatus(
    val type: SpecialAppReplacementStatusType,
    val hasApiKey: Boolean,
    val hasSuccessfulSync: Boolean,
    val controlEnabled: Boolean,
    val encourageEnabled: Boolean,
    val usagePreference: SpecialAppUsagePreference,
) {
    val active: Boolean = type == SpecialAppReplacementStatusType.ACTIVE
}

internal fun buildSpecialAppReplacementStatus(
    hasApiKey: Boolean,
    lastSuccessAt: Long,
    enabledForControl: Boolean,
    enabledForEncourage: Boolean,
    syncEnabled: Boolean,
    usagePreference: SpecialAppUsagePreference,
): SpecialAppReplacementStatus {
    val hasSuccessfulSync = lastSuccessAt > 0L
    val hasScope = enabledForControl || enabledForEncourage
    val type =
        when {
            hasApiKey && hasSuccessfulSync && hasScope && syncEnabled -> SpecialAppReplacementStatusType.ACTIVE
            !hasApiKey -> SpecialAppReplacementStatusType.NEEDS_KEY
            !hasSuccessfulSync -> SpecialAppReplacementStatusType.NEEDS_SYNC
            !hasScope -> SpecialAppReplacementStatusType.NEEDS_SCOPE
            else -> SpecialAppReplacementStatusType.INACTIVE
        }
    return SpecialAppReplacementStatus(
        type = type,
        hasApiKey = hasApiKey,
        hasSuccessfulSync = hasSuccessfulSync,
        controlEnabled = enabledForControl,
        encourageEnabled = enabledForEncourage,
        usagePreference = usagePreference,
    )
}
