package com.rrrrz.tinyvow.domain.limit

data class DailyLimitEvaluation(
    val usedMillis: Long,
    val limitMillis: Long,
    val remainingMillis: Long,
    val exceededMillis: Long,
    val isExceeded: Boolean,
)
