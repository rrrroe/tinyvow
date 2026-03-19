package com.rrrrz.tinyvow.data.usage

interface UsageRepository {
    suspend fun getTodayUsageMillis(packageName: String): Long
}
