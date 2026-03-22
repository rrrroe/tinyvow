package com.rrrrz.tinyvow.data.usage

import com.rrrrz.tinyvow.data.db.LimitPeriod

interface UsageRepository {
    suspend fun getTodayUsageMillis(packageName: String): Long
    suspend fun getUsageInPeriod(packageName: String, period: LimitPeriod): Long
    suspend fun getYesterdayUsageMillis(packageName: String): Long
}
