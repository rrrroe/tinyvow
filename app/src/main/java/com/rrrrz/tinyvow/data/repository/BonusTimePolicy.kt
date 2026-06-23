package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.time.BusinessDay
import java.time.ZoneId

internal fun calculateBonusExpiryTime(
    createdAt: Long,
    period: LimitPeriod,
    zoneId: ZoneId = ZoneId.systemDefault(),
    dayStartHour: Int = BusinessDay.cachedStartHour(),
): Long {
    val startDate = BusinessDay.dateAt(createdAt, zoneId, dayStartHour)
    val endDate =
        when (period) {
            LimitPeriod.DAILY -> startDate
            LimitPeriod.WEEKLY -> startDate.plusDays(6)
            LimitPeriod.MONTHLY -> startDate.withDayOfMonth(startDate.lengthOfMonth())
        }
    return BusinessDay.endOfDayMillis(endDate, zoneId, dayStartHour)
}
