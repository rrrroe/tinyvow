package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.LimitPeriod
import java.util.Calendar
import java.util.TimeZone

internal fun calculateBonusExpiryTime(
    createdAt: Long,
    period: LimitPeriod,
    timeZone: TimeZone = TimeZone.getDefault(),
): Long {
    return Calendar.getInstance(timeZone).apply {
        timeInMillis = createdAt
        when (period) {
            LimitPeriod.DAILY -> Unit
            LimitPeriod.WEEKLY -> add(Calendar.DAY_OF_YEAR, 6)
            LimitPeriod.MONTHLY -> set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis
}
