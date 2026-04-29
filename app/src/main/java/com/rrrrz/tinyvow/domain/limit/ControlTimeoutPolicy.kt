package com.rrrrz.tinyvow.domain.limit

const val CONTROL_TIMEOUT_GRACE_MILLIS: Long = 10L * 60L * 1000L

fun isControlTimeout(exceededMillis: Long): Boolean =
    exceededMillis > CONTROL_TIMEOUT_GRACE_MILLIS
