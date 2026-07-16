package com.rrrrz.tinyvow.domain.limit

import android.content.Context
import android.os.SystemClock
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.db.RewardType
import com.rrrrz.tinyvow.data.repository.parseRewardPayload
import com.rrrrz.tinyvow.data.usage.MergedUsageRepository

/**
 * 多分组交叉短板效应评估器 (升级版：支持周期时长与加时包)
 */
class GroupLimitEnforcer(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val crossRefDao = database.crossRefDao()
    private val groupDao = database.appGroupDao()
    private val bonusTimeDao = database.bonusTimeDao()
    private val activeRewardEffectDao = database.activeRewardEffectDao()
    private val usageRepository = MergedUsageRepository(context)

    private data class ConfigCacheEntry(val groupIds: List<String>, val fetchedAt: Long)
    private val configCache = mutableMapOf<String, ConfigCacheEntry>()

    private data class PeriodUsageCacheEntry(val usageByPackage: Map<String, Long>, val fetchedAt: Long)
    private val periodUsageCache = mutableMapOf<LimitPeriod, PeriodUsageCacheEntry>()

    suspend fun evaluate(packageName: String): GroupExceededResult? {
        val now = SystemClock.elapsedRealtime()
        val currentTimeMillis = System.currentTimeMillis()

        val groupIds = getCachedGroupIds(packageName, now)
        if (groupIds.isEmpty()) return null

        // 一次性批量读取所有分组，避免热路径循环查询 DB
        val groups = groupDao.getGroupsByIdsSync(groupIds).filter { it.type == GroupType.CONTROL }

        for (group in groups) {
            val activeEffects = activeRewardEffectDao.getActiveForGroup(group.id, currentTimeMillis)
            val hasPeriodPass = activeEffects.any { it.effectType == RewardType.PERIOD_PASS }
            if (ControlGroupLimitPolicy.shouldBypass(hasPeriodPass)) {
                continue
            }
            val effectBonusMillis =
                activeEffects
                    .filter { it.effectType == RewardType.TIME_ADD || it.effectType == RewardType.EMERGENCY_UNLOCK }
                    .sumOf { parseRewardPayload(it.payloadJson).minutes * 60_000L }
            val baseLimitMillis = group.limitMinutes * 60_000L
            val bonusMillis = getSyncBonusTimeMillis(group.id, currentTimeMillis) + effectBonusMillis
            val totalUsedMillis = getCachedGroupUsage(group.id, group.limitPeriod, now)
            val decision =
                ControlGroupLimitPolicy.evaluate(
                    totalUsedMillis = totalUsedMillis,
                    baseLimitMillis = baseLimitMillis,
                    bonusMillis = bonusMillis,
                )
            if (decision != null) {
                return GroupExceededResult(
                    groupName = group.name,
                    groupId = group.id,
                    groupType = group.type,
                    limitMinutes = group.limitMinutes + (bonusMillis / 60_000).toInt(),
                    totalUsedMillis = totalUsedMillis,
                    exceededMillis = decision.exceededMillis,
                )
            }
        }
        return null
    }

    fun invalidateCaches(packageName: String? = null) {
        if (packageName == null) {
            configCache.clear()
        } else {
            configCache.remove(packageName)
        }
        periodUsageCache.clear()
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
        val cached = periodUsageCache[period]
        if (cached != null && now - cached.fetchedAt < USAGE_CACHE_TTL_MS) {
            val packages = crossRefDao.getPackageNamesForGroupSync(groupId)
            return packages.sumOf { cached.usageByPackage[it] ?: 0L }
        }
        val fresh = usageRepository.getUsageStatsInPeriod(period, GroupType.CONTROL)
        periodUsageCache[period] = PeriodUsageCacheEntry(fresh, now)
        val packages = crossRefDao.getPackageNamesForGroupSync(groupId)
        return packages.sumOf { fresh[it] ?: 0L }
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
