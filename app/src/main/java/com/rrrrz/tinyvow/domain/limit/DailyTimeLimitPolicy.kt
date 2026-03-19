package com.rrrrz.tinyvow.domain.limit

class DailyTimeLimitPolicy : LimitPolicy {
    override fun evaluate(usageMillis: Long, limitMillis: Long): DailyLimitEvaluation {
        val remainingMillis = (limitMillis - usageMillis).coerceAtLeast(0L)
        val exceededMillis = (usageMillis - limitMillis).coerceAtLeast(0L)

        return DailyLimitEvaluation(
            usedMillis = usageMillis,
            limitMillis = limitMillis,
            remainingMillis = remainingMillis,
            exceededMillis = exceededMillis,
            isExceeded = usageMillis >= limitMillis,
        )
    }
}
