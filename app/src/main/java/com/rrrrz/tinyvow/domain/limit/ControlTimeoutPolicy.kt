package com.rrrrz.tinyvow.domain.limit

const val CONTROL_STATS_GRACE_MILLIS: Long = 5L * 60L * 1000L

fun isControlOverLimit(exceededMillis: Long): Boolean =
    exceededMillis > 0L

fun isControlTimeoutForStats(exceededMillis: Long): Boolean =
    exceededMillis > CONTROL_STATS_GRACE_MILLIS
