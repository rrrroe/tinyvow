package com.rrrrz.tinyvow.data.repository

import com.rrrrz.tinyvow.data.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.Calendar

data class AppGroupWithApps(
    val group: AppGroupEntity,
    val packageNames: List<String>
)

class AppLimitRepository(private val database: AppDatabase) {
    private val groupDao = database.appGroupDao()
    private val crossRefDao = database.crossRefDao()
    private val redemptionDao = database.redemptionDao()
    private val bonusTimeDao = database.bonusTimeDao()
    private val achievementDao = database.achievementDao()

    private val _newAchievementsAction = MutableSharedFlow<AchievementEntity>()
    val newAchievementsAction: SharedFlow<AchievementEntity> = _newAchievementsAction.asSharedFlow()

    /**
     * 实时暴露出所有未软删除的分组及其关联的应用包名列表
     */
    fun getAllGroupsWithApps(): Flow<List<AppGroupWithApps>> {
        return combine(
            groupDao.getAllGroups(),
            crossRefDao.getAllValidCrossRefs()
        ) { groups, crossRefs ->
            groups.map { group ->
                val groupApps = crossRefs
                    .filter { it.groupId == group.id }
                    .map { it.packageName }
                AppGroupWithApps(group, groupApps)
            }
        }
    }

    /**
     * 创建或全量覆盖更新一个分组
     */
    suspend fun createOrUpdateGroup(
        id: String?, 
        name: String, 
        limitMinutes: Int,
        type: GroupType = GroupType.CONTROL,
        limitPeriod: LimitPeriod = LimitPeriod.DAILY,
        pointsPerMinute: Double = 0.0
    ): String {
        return withContext(Dispatchers.IO) {
            val groupId = id ?: UUID.randomUUID().toString()
            val existing = if (id != null) groupDao.getGroupByIdSync(id) else null
            
            val entity = AppGroupEntity(
                id = groupId,
                name = name,
                type = type,
                limitPeriod = limitPeriod,
                limitMinutes = limitMinutes,
                pointsPerMinute = pointsPerMinute,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            groupDao.insertGroup(entity)
            groupId
        }
    }

    /**
     * 软删除：标记整个分组及它下面挂载的所有中间表记录为已被删除
     */
    suspend fun deleteGroup(groupId: String) {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            groupDao.softDeleteGroup(groupId, now)
            crossRefDao.softDeleteAllForGroup(groupId, now)
        }
    }

    /**
     * 更新某个分组下的全部 App
     */
    suspend fun updateGroupApps(groupId: String, packageNames: List<String>) {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            crossRefDao.softDeleteAllForGroup(groupId, now)
            
            if (packageNames.isNotEmpty()) {
                val newRefs = packageNames.map {
                    GroupAppCrossRef(
                        packageName = it,
                        groupId = groupId,
                        updatedAt = now
                    )
                }
                crossRefDao.insertCrossRefs(newRefs)
            }
        }
    }

    fun getAllRewards(): Flow<List<RedemptionEntity>> = redemptionDao.getAllActiveRedemptions()
    fun getAllAchievements(): Flow<List<AchievementEntity>> = achievementDao.getAllAchievements()

    // ──────── 积分与加时逻辑 ────────

    /** 获取某个分组当前生效的所有加时时长（单位：毫秒） */
    fun getActiveBonusTimeMillis(groupId: String): Flow<Long> {
        val now = System.currentTimeMillis()
        return bonusTimeDao.getActiveBonusTimeForGroup(groupId, now).combine(database.appGroupDao().getAllGroups()) { bonusList, _ ->
            bonusList.sumOf { it.extraMinutes * 60_000L }
        }
    }

    /** 兑换加时包 */
    suspend fun redeemTimePack(groupId: String, extraMinutes: Int) {
        withContext(Dispatchers.IO) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }
            val bonus = BonusTimeEntity(
                id = UUID.randomUUID().toString(),
                targetGroupId = groupId,
                extraMinutes = extraMinutes,
                expiryTime = calendar.timeInMillis,
                createdAt = System.currentTimeMillis()
            )
            bonusTimeDao.insertBonusTime(bonus)
        }
    }

    suspend fun clearExpiredBonusTime(now: Long) {
        withContext(Dispatchers.IO) {
            bonusTimeDao.clearExpiredBonusTime(now)
        }
    }

    suspend fun addReward(title: String, cost: Int, type: RewardType, stock: Int = -1, description: String = "", bonusMinutes: Int = 0) {
        withContext(Dispatchers.IO) {
            redemptionDao.insertRedemption(
                RedemptionEntity(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    description = description,
                    pointCost = cost,
                    rewardType = type,
                    bonusMinutes = bonusMinutes,
                    stock = stock,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun updateReward(reward: RedemptionEntity) {
        withContext(Dispatchers.IO) {
            redemptionDao.insertRedemption(reward.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    /** 预置初始数据 */
    suspend fun seedInitialData() {
        withContext(Dispatchers.IO) {
            // 只保留三个基础可兑选项，默认库存无穷大
            addReward("30分钟 临时续命卡", 50, RewardType.TIME_PACK, -1, "立即获得30分钟额外时长", 30)
            addReward("1小时 自由冲浪卡", 100, RewardType.TIME_PACK, -1, "立即获得1小时额外时长", 60)
            addReward("大快朵颐 (线下奖励)", 500, RewardType.CUSTOM, 5, "给自己加个鸡腿！")

            // 初始成就
            achievementDao.insertAchievement(AchievementEntity("FIRST_10_POINTS", "初露锋芒", "赚取前 10 个积分", "{\"type\":\"points\",\"value\":10}"))
            achievementDao.insertAchievement(AchievementEntity("FIRST_100_POINTS", "积分达人", "累计赚取 100 个积分", "{\"type\":\"points\",\"value\":100}"))
            achievementDao.insertAchievement(AchievementEntity("CONSISTENT_3_DAYS", "坚持不懈", "连续 3 天未超标", "{\"type\":\"days\",\"value\":3}"))
        }
    }

    suspend fun checkAchievements(currentPoints: Double) {
        withContext(Dispatchers.IO) {
            val locked = achievementDao.getLockedAchievements()
            val now = System.currentTimeMillis()
            for (achievement in locked) {
                try {
                    // 简单的字符串包含逻辑来匹配 type:points 和 value
                    if (achievement.requirement.contains("\"type\":\"points\"")) {
                        val valueStr = achievement.requirement.split("\"value\":")[1]
                            .split("}")[0]
                            .trim()
                        if (currentPoints >= valueStr.toDouble()) {
                            achievementDao.unlockAchievement(achievement.id, now)
                            // 发送解锁成功通知
                            _newAchievementsAction.emit(achievement.copy(isUnlocked = true, unlockedAt = now))
                        }
                    }
                } catch (e: Exception) {
                    // Ignore parsing errors
                }
            }
        }
    }
}
