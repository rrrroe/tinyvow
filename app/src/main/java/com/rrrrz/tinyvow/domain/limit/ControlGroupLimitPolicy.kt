package com.rrrrz.tinyvow.domain.limit

internal data class ControlGroupLimitDecision(
    val totalLimitMillis: Long,
    val exceededMillis: Long,
)

internal object ControlGroupLimitPolicy {
    fun shouldBypass(hasPeriodPass: Boolean): Boolean = hasPeriodPass

    fun evaluate(
        totalUsedMillis: Long,
        baseLimitMillis: Long,
        bonusMillis: Long,
    ): ControlGroupLimitDecision? {
        val totalLimitMillis = baseLimitMillis + bonusMillis
        val exceededMillis = totalUsedMillis - totalLimitMillis
        if (!isControlOverLimit(exceededMillis)) return null

        return ControlGroupLimitDecision(
            totalLimitMillis = totalLimitMillis,
            exceededMillis = exceededMillis,
        )
    }
}
