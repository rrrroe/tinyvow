package com.rrrrz.tinyvow.domain.limit

import android.content.Context
import android.os.SystemClock
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.db.AppGroupEntity
import com.rrrrz.tinyvow.data.usage.UsageStatsUsageRepository

/**
 * 多分组交叉短板效应评估器
 *
 * 核心算法：当前前台 App → 查找它所属的 N 个分组 → 逐组聚合计算所有组内 App 总时长
 *         → 任意一个分组超标 → 返回需要阻断的评估结果
 *
 * 内置两层缓存防抖：
 * 1. 配置缓存 (configCacheTtlMs)：避免每次 accessibility 事件都查 Room
 * 2. 使用量缓存 (usageCacheTtlMs)：避免每次都调 UsageStatsManager
 */
class GroupLimitEnforcer(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val crossRefDao = database.crossRefDao()
    private val groupDao = database.appGroupDao()
    private val usageRepository = UsageStatsUsageRepository(context)

    // ── 配置缓存：packageName → (groupIds, timestamp) ──
    private data class ConfigCacheEntry(
        val groupIds: List<String>,
        val fetchedAt: Long
    )
    private val configCache = mutableMapOf<String, ConfigCacheEntry>()

    // ── 使用量缓存：groupId → (usageMillis, timestamp) ──
    private data class UsageCacheEntry(
        val usedMillis: Long,
        val fetchedAt: Long
    )
    private val usageCache = mutableMapOf<String, UsageCacheEntry>()

    /**
     * 对 [packageName] 执行多维短板评估。
     *
     * @return 若任意分组超标则返回 [GroupExceededResult]，否则返回 null
     */
    suspend fun evaluate(packageName: String): GroupExceededResult? {
        val now = SystemClock.elapsedRealtime()

        // 1. 取缓存中的分组 ID 列表；过期则重新查库
        val groupIds = getCachedGroupIds(packageName, now)
        if (groupIds.isEmpty()) return null   // 该 App 不在任何管控组中

        // 2. 遍历每个分组，检查是否超标
        for (groupId in groupIds) {
            val group = groupDao.getGroupByIdSync(groupId) ?: continue
            val limitMillis = group.dailyLimitMinutes * 60_000L

            // 3. 聚合该组所有 App 今日总用量
            val totalUsedMillis = getCachedGroupUsage(groupId, now)

            if (totalUsedMillis >= limitMillis) {
                return GroupExceededResult(
                    groupName = group.name,
                    groupId = groupId,
                    limitMinutes = group.dailyLimitMinutes,
                    totalUsedMillis = totalUsedMillis,
                    exceededMillis = totalUsedMillis - limitMillis
                )
            }
        }

        return null
    }

    /** 清除全部缓存（比如午夜跨天、用户修改配置时） */
    fun invalidateAll() {
        configCache.clear()
        usageCache.clear()
    }

    // ──────── 内部缓存逻辑 ────────

    private fun getCachedGroupIds(packageName: String, now: Long): List<String> {
        val cached = configCache[packageName]
        if (cached != null && now - cached.fetchedAt < CONFIG_CACHE_TTL_MS) {
            return cached.groupIds
        }
        val fresh = crossRefDao.getGroupIdsForPackageSync(packageName)
        configCache[packageName] = ConfigCacheEntry(fresh, now)
        return fresh
    }

    private suspend fun getCachedGroupUsage(groupId: String, now: Long): Long {
        val cached = usageCache[groupId]
        if (cached != null && now - cached.fetchedAt < USAGE_CACHE_TTL_MS) {
            return cached.usedMillis
        }
        val packages = crossRefDao.getPackageNamesForGroupSync(groupId)
        var total = 0L
        for (pkg in packages) {
            total += usageRepository.getTodayUsageMillis(pkg)
        }
        usageCache[groupId] = UsageCacheEntry(total, now)
        return total
    }

    companion object {
        /** 配置查询缓存有效期：30 秒 */
        private const val CONFIG_CACHE_TTL_MS = 30_000L
        /** 使用量查询缓存有效期：15 秒 */
        private const val USAGE_CACHE_TTL_MS = 15_000L
    }
}

/**
 * 超标结果 DTO
 */
data class GroupExceededResult(
    val groupName: String,
    val groupId: String,
    val limitMinutes: Int,
    val totalUsedMillis: Long,
    val exceededMillis: Long
)
