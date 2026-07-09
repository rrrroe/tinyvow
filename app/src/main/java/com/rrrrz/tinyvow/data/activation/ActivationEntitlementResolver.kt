package com.rrrrz.tinyvow.data.activation

import com.rrrrz.tinyvow.data.billing.ProEntitlementState
import com.rrrrz.tinyvow.i18n.AppText

data class ActivationEntitlementResolution(
    val entitlement: ProEntitlementState,
    val nextLastSeenWallClockMillis: Long?,
)

object ActivationEntitlementResolver {
    fun restorableUserId(
        record: LocalActivationRecord?,
        nowMillis: Long,
        lastSeenWallClockMillis: Long?,
    ): String? {
        if (record == null) return null
        return resolve(
            record = record,
            userId = record.userId,
            nowMillis = nowMillis,
            lastSeenWallClockMillis = lastSeenWallClockMillis,
        ).entitlement.takeIf { it.isProActive }?.let { record.userId }
    }

    fun resolve(
        record: LocalActivationRecord?,
        userId: String?,
        nowMillis: Long,
        lastSeenWallClockMillis: Long?,
    ): ActivationEntitlementResolution {
        if (lastSeenWallClockMillis != null &&
            nowMillis < lastSeenWallClockMillis - ACTIVATION_TIME_ROLLBACK_TOLERANCE_MILLIS
        ) {
            return ActivationEntitlementResolution(
                entitlement = ProEntitlementState.unavailable(
                    AppText.t("activation_system_time_is_abnormal_restore_time"),
                ),
                nextLastSeenWallClockMillis = lastSeenWallClockMillis,
            )
        }

        val nextLastSeen = maxOf(lastSeenWallClockMillis ?: 0L, nowMillis)
        if (record == null || userId == null || record.userId != userId || nowMillis > record.expiresAtMillis) {
            return ActivationEntitlementResolution(
                entitlement = ProEntitlementState.Free,
                nextLastSeenWallClockMillis = nextLastSeen,
            )
        }

        return ActivationEntitlementResolution(
            entitlement = ProEntitlementState.active(
                purchaseToken = "local:${record.codeId}",
                expiresAtMillis = record.expiresAtMillis,
                source = ACTIVATION_SOURCE_LOCAL,
            ),
            nextLastSeenWallClockMillis = nextLastSeen,
        )
    }
}

object ActivationExpiryCalculator {
    private const val DAY_MILLIS = 86_400_000L

    fun extendFrom(
        nowMillis: Long,
        currentExpiresAtMillis: Long?,
        durationDays: Int,
    ): Long =
        maxOf(nowMillis, currentExpiresAtMillis ?: 0L) + durationDays * DAY_MILLIS
}
