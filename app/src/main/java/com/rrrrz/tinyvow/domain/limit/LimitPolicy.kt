package com.rrrrz.tinyvow.domain.limit

interface LimitPolicy {
    fun evaluate(usageMillis: Long, limitMillis: Long): DailyLimitEvaluation
}
