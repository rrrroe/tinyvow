package com.rrrrz.tinyvow.domain.limit

import android.content.Context
import android.os.SystemClock
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.db.AppGroupEntity
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.usage.UsageStatsUsageRepository

/**
 * 多分组交叉短板效应评估器 (升级版：支持周期时长与加时包)
 */
class GroupLimitEnforcer(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val crossRefDao = database.crossRefDao()
    private val groupDao = database.appGroupDao()
    private val bonusTimeDao = database.bonusTimeDao()
    private val usageRepository = UsageStatsUsageRepository(context)

    private data class ConfigCacheEntry(val groupIds: List<String>, val fetchedAt: Long)
    private val configCache = mutableMapOf<String, ConfigCacheEntry>()

    private data class UsageCacheEntry(val usedMillis: Long, val fetchedAt: Long)
    private val usageCache = mutableMapOf<String, UsageCacheEntry>()

    suspend fun evaluate(packageName: String): GroupExceededResult? {
        val now = SystemClock.elapsedRealtime()
        val currentTimeMillis = System.currentTimeMillis()

        val groupIds = getCachedGroupIds(packageName, now)
        if (groupIds.isEmpty()) return null

        // 一次性批量读取所有分组，避免热路径循环查询 DB
        val groups = groupDao.getGroupsByIdsSync(groupIds)

        for (group in groups) {
            // 基础限额 + 加时包
            val baseLimitMillis = group.limitMinutes * 60_000L
            val bonusMillis = getSyncBonusTimeMillis(group.id, currentTimeMillis)
            val totalLimitMillis = baseLimitMillis + bonusMillis

            // 统计周期内的历史用量
            val totalUsedMillis = getCachedGroupUsage(group.id, group.limitPeriod, now)

            val exceededMillis = totalUsedMillis - totalLimitMillis
            if (isControlOverLimit(exceededMillis)) {
                return GroupExceededResult(
                    groupName = group.name,
                    groupId = group.id,
                    groupType = group.type,
                    limitMinutes = group.limitMinutes + (bonusMillis / 60_000).toInt(),
                    totalUsedMillis = totalUsedMillis,
                    exceededMillis = exceededMillis,
                )
            }
        }
        return null
    }

    private fun getSyncBonusTimeMillis(groupId: String, now: Long): Long {
        val bonusList = bonusTimeDao.getActiveBonusTimeForGroupSync(groupId, now)
        return bonusList.sumOf { it.extraMinutes * 60_000L }
    }

    private fun getCachedGroupIds(packageName: String, now: Long): List<String> {
        val cached = configCache[packageName]
        if (cached != null && now - cached.fetchedAt < CONFIG_CACHE_TTL_MS) {
            return cached.groupIds
        }
        val fresh = crossRefDao.getGroupIdsForPackageSync(packageName)
        configCache[packageName] = ConfigCacheEntry(fresh, now)
        return fresh
    }

    private suspend fun getCachedGroupUsage(groupId: String, period: LimitPeriod, now: Long): Long {
        val cacheKey = "${groupId}_${period}"
        val cached = usageCache[cacheKey]
        if (cached != null && now - cached.fetchedAt < USAGE_CACHE_TTL_MS) {
            return cached.usedMillis
        }
        val packages = crossRefDao.getPackageNamesForGroupSync(groupId)
        var total = 0L
        for (pkg in packages) {
            total += usageRepository.getUsageInPeriod(pkg, period)
        }
        usageCache[cacheKey] = UsageCacheEntry(total, now)
        return total
    }

    companion object {
        private const val CONFIG_CACHE_TTL_MS = 30_000L
        private const val USAGE_CACHE_TTL_MS = 15_000L
    }
}

data class GroupExceededResult(
    val groupName: String,
    val groupId: String,
    val groupType: GroupType,
    val limitMinutes: Int,
    val totalUsedMillis: Long,
    val exceededMillis: Long
)
