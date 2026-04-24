package com.rrrrz.tinyvow.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.rrrrz.tinyvow.data.db.*
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
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

data class RedemptionResult(
    val pointCost: Int,
    val message: String,
)

class AppLimitRepository(
    context: Context,
    private val database: AppDatabase,
) {
    private val groupDao = database.appGroupDao()
    private val crossRefDao = database.crossRefDao()
    private val redemptionDao = database.redemptionDao()
    private val bonusTimeDao = database.bonusTimeDao()
    private val achievementDao = database.achievementDao()
    private val redemptionHistoryDao = database.redemptionHistoryDao()
    private val pointLedgerDao = database.pointLedgerDao()
    private val preferences = ManagedAppPreferences(context)

    private val _newAchievementsAction = MutableSharedFlow<AchievementEntity>()
    val newAchievementsAction: SharedFlow<AchievementEntity> = _newAchievementsAction.asSharedFlow()
    private val _redemptionEvents = MutableSharedFlow<String>()
    val redemptionEvents: SharedFlow<String> = _redemptionEvents.asSharedFlow()

    // ──────── 兑换记录 ────────

    fun getRedemptionHistory(): Flow<List<RedemptionHistoryEntity>> =
        redemptionHistoryDao.getAllHistory()

    suspend fun recordRedemption(title: String, pointCost: Int) {
        withContext(Dispatchers.IO) {
            val redemptionHistoryId = UUID.randomUUID().toString()
            redemptionHistoryDao.insertHistory(
                RedemptionHistoryEntity(
                    id = redemptionHistoryId,
                    rewardTitle = title,
                    pointCost = pointCost,
                    historyType = RedemptionHistoryType.CUSTOM,
                    redeemedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun redeemReward(
        reward: RedemptionEntity,
        targetGroupId: String?,
    ): RedemptionResult? {
        return withContext(Dispatchers.IO) {
            val latestReward = redemptionDao.getRedemptionById(reward.id) ?: return@withContext null
            if (!latestReward.isActive || latestReward.stock == 0) return@withContext null

            val redeemedAt = System.currentTimeMillis()
            val redemptionHistoryId = UUID.randomUUID().toString()
            val ledgerEntryId = UUID.randomUUID().toString()
            var createdBonusId: String? = null
            val targetGroupName =
                if (latestReward.rewardType == RewardType.TIME_PACK && targetGroupId != null) {
                    groupDao.getGroupByIdSync(targetGroupId)?.name
                } else {
                    null
                }
            val redeemedGroupId =
                if (latestReward.rewardType == RewardType.TIME_PACK) {
                    targetGroupId ?: return@withContext null
                } else {
                    null
                }

            database.withTransaction {
                if (latestReward.rewardType == RewardType.TIME_PACK) {
                    val bonusId = UUID.randomUUID().toString()
                    insertTimePackBonus(
                        bonusId = bonusId,
                        groupId = redeemedGroupId!!,
                        extraMinutes = latestReward.bonusMinutes,
                        createdAt = redeemedAt,
                    )
                    createdBonusId = bonusId
                }

                if (latestReward.stock > 0) {
                    redemptionDao.insertRedemption(
                        latestReward.copy(
                            stock = latestReward.stock - 1,
                            updatedAt = redeemedAt,
                        )
                    )
                }

                redemptionHistoryDao.insertHistory(
                    RedemptionHistoryEntity(
                        id = redemptionHistoryId,
                        rewardTitle = latestReward.title,
                        pointCost = latestReward.pointCost,
                        historyType = when (latestReward.rewardType) {
                            RewardType.TIME_PACK -> RedemptionHistoryType.TIME_PACK
                            RewardType.CUSTOM -> RedemptionHistoryType.CUSTOM
                        },
                        bonusMinutes = latestReward.bonusMinutes,
                        targetGroupName = targetGroupName,
                        redeemedAt = redeemedAt,
                    )
                )

                pointLedgerDao.insert(
                    PointLedgerEntity(
                        id = ledgerEntryId,
                        occurredAt = redeemedAt,
                        ledgerDate = ArchiveDateUtils.formatDate(ArchiveDateUtils.localDateAt(redeemedAt, java.time.ZoneId.systemDefault())),
                        entryType = PointLedgerEntryType.REWARD_SPEND,
                        deltaPoints = -latestReward.pointCost.toDouble(),
                        rewardId = latestReward.id,
                        rewardTitleSnapshot = latestReward.title,
                        sourceRefId = redemptionHistoryId,
                        createdAt = redeemedAt,
                    )
                )
            }

            try {
                preferences.addUserPoints(-latestReward.pointCost.toDouble())
            } catch (error: Exception) {
                database.withTransaction {
                    if (latestReward.stock > 0) {
                        redemptionDao.insertRedemption(latestReward)
                    }
                    redemptionHistoryDao.deleteById(redemptionHistoryId)
                    pointLedgerDao.deleteById(ledgerEntryId)
                    createdBonusId?.let { bonusTimeDao.deleteById(it) }
                }
                throw error
            }

            val message = when (latestReward.rewardType) {
                RewardType.TIME_PACK -> {
                    val groupName = targetGroupName ?: "目标分组"
                    "已兑换 ${latestReward.title}，-${latestReward.pointCost} PT，$groupName +${latestReward.bonusMinutes} 分钟"
                }
                RewardType.CUSTOM -> {
                    "已兑换 ${latestReward.title}，-${latestReward.pointCost} PT"
                }
            }

            _redemptionEvents.emit(message)
            RedemptionResult(
                pointCost = latestReward.pointCost,
                message = message,
            )
        }
    }

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
            insertTimePackBonus(
                bonusId = UUID.randomUUID().toString(),
                groupId = groupId,
                extraMinutes = extraMinutes,
                createdAt = System.currentTimeMillis(),
            )
        }
    }

    suspend fun clearExpiredBonusTime(now: Long) {
        withContext(Dispatchers.IO) {
            bonusTimeDao.clearExpiredBonusTime(now)
        }
    }

    private suspend fun insertTimePackBonus(
        bonusId: String,
        groupId: String,
        extraMinutes: Int,
        createdAt: Long,
    ) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = createdAt
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
        val bonus =
            BonusTimeEntity(
                id = bonusId,
                targetGroupId = groupId,
                extraMinutes = extraMinutes,
                expiryTime = calendar.timeInMillis,
                createdAt = createdAt,
            )
        bonusTimeDao.insertBonusTime(bonus)
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

            // 使用 seedAchievement (IGNORE) 避免覆盖已解锁状态
            suspend fun seed(id: String, title: String, desc: String, req: String, tier: Int, emoji: String) {
                achievementDao.seedAchievement(AchievementEntity(id, title, desc, req, tier, emoji))
            }

            // ══════════ 🥉 Bronze 铜阶 ══════════
            seed("BRONZE_POINTS", "初入江湖", "累计赚取 100 积分",
                """{"type":"points","value":100}""", AchievementTier.BRONZE, "🌱")
            seed("BRONZE_REDEEM", "初次消费", "累计消费 100 积分",
                """{"type":"redeem_points","value":100}""", AchievementTier.BRONZE, "🍬")
            seed("BRONZE_CTRL_DAYS", "点滴积累", "累计 10 天约定未超标",
                """{"type":"control_days","value":10}""", AchievementTier.BRONZE, "🤝")
            seed("BRONZE_CTRL_STREAK", "三日磐石", "连续 3 天约定未超标",
                """{"type":"control_streak","value":3}""", AchievementTier.BRONZE, "🪨")
            seed("BRONZE_ENC_DAYS", "向阳而生", "累计 10 天鼓励达标",
                """{"type":"encourage_days","value":10}""", AchievementTier.BRONZE, "🌻")
            seed("BRONZE_ENC_STREAK", "初心不改", "连续 3 天鼓励达标",
                """{"type":"encourage_streak","value":3}""", AchievementTier.BRONZE, "🕯️")

            // ══════════ 🥈 Silver 银阶 ══════════
            seed("SILVER_POINTS", "踏浪前行", "累计赚取 300 积分",
                """{"type":"points","value":300}""", AchievementTier.SILVER, "🌊")
            seed("SILVER_REDEEM", "购物达人", "累计消费 300 积分",
                """{"type":"redeem_points","value":300}""", AchievementTier.SILVER, "🛒")
            seed("SILVER_CTRL_DAYS", "守约如山", "累计 30 天约定未超标",
                """{"type":"control_days","value":30}""", AchievementTier.SILVER, "⛰️")
            seed("SILVER_CTRL_STREAK", "十日坚守", "连续 10 天约定未超标",
                """{"type":"control_streak","value":10}""", AchievementTier.SILVER, "🌓")
            seed("SILVER_ENC_DAYS", "习惯养成", "累计 30 天鼓励达标",
                """{"type":"encourage_days","value":30}""", AchievementTier.SILVER, "📖")
            seed("SILVER_ENC_STREAK", "十全十美", "连续 10 天鼓励达标",
                """{"type":"encourage_streak","value":10}""", AchievementTier.SILVER, "🌿")

            // ══════════ 🥇 Gold 金阶 ══════════
            seed("GOLD_POINTS", "千分大师", "累计赚取 1000 积分",
                """{"type":"points","value":1000}""", AchievementTier.GOLD, "👑")
            seed("GOLD_REDEEM", "赏金猎人", "累计消费 1000 积分",
                """{"type":"redeem_points","value":1000}""", AchievementTier.GOLD, "🎯")
            seed("GOLD_CTRL_DAYS", "百日守护", "累计 100 天约定未超标",
                """{"type":"control_days","value":100}""", AchievementTier.GOLD, "🛡️")
            seed("GOLD_CTRL_STREAK", "月之战神", "连续 30 天约定未超标",
                """{"type":"control_streak","value":30}""", AchievementTier.GOLD, "🌙")
            seed("GOLD_ENC_DAYS", "百炼成钢", "累计 100 天鼓励达标",
                """{"type":"encourage_days","value":100}""", AchievementTier.GOLD, "🔥")
            seed("GOLD_ENC_STREAK", "势如破竹", "连续 30 天鼓励达标",
                """{"type":"encourage_streak","value":30}""", AchievementTier.GOLD, "🎍")

            // ══════════ 💎 Diamond 钻石阶 ══════════
            seed("DIAMOND_POINTS", "名满天下", "累计赚取 3000 积分",
                """{"type":"points","value":3000}""", AchievementTier.DIAMOND, "💫")
            seed("DIAMOND_REDEEM", "挥金如土", "累计消费 3000 积分",
                """{"type":"redeem_points","value":3000}""", AchievementTier.DIAMOND, "🏛️")
            seed("DIAMOND_CTRL_DAYS", "岁月如歌", "累计 365 天约定未超标",
                """{"type":"control_days","value":365}""", AchievementTier.DIAMOND, "🏰")
            seed("DIAMOND_CTRL_STREAK", "百日无懈", "连续 100 天约定未超标",
                """{"type":"control_streak","value":100}""", AchievementTier.DIAMOND, "⚡")
            seed("DIAMOND_ENC_DAYS", "年年有余", "累计 365 天鼓励达标",
                """{"type":"encourage_days","value":365}""", AchievementTier.DIAMOND, "⚔️")
            seed("DIAMOND_ENC_STREAK", "百战百胜", "连续 100 天鼓励达标",
                """{"type":"encourage_streak","value":100}""", AchievementTier.DIAMOND, "🗡️")

            // ══════════ 🌟 Legendary 传奇阶 ══════════
            seed("LEGEND_POINTS", "不朽传说", "累计赚取 10000 积分",
                """{"type":"points","value":10000}""", AchievementTier.LEGENDARY, "🐉")
            seed("LEGEND_REDEEM", "万金散尽", "累计消费 10000 积分",
                """{"type":"redeem_points","value":10000}""", AchievementTier.LEGENDARY, "💰")
            seed("LEGEND_CTRL_DAYS", "万世千秋", "累计 10000 天约定未超标",
                """{"type":"control_days","value":10000}""", AchievementTier.LEGENDARY, "⭐")
            seed("LEGEND_CTRL_STREAK", "誓约永恒", "连续 365 天约定未超标",
                """{"type":"control_streak","value":365}""", AchievementTier.LEGENDARY, "🔱")
            seed("LEGEND_ENC_DAYS", "与日同辉", "累计 10000 天鼓励达标",
                """{"type":"encourage_days","value":10000}""", AchievementTier.LEGENDARY, "🌈")
            seed("LEGEND_ENC_STREAK", "时间领主", "连续 365 天鼓励达标",
                """{"type":"encourage_streak","value":365}""", AchievementTier.LEGENDARY, "⏳")
        }
    }

    /**
     * 检查并解锁达成条件的成就
     * @param currentPoints 累计赚取积分
     * @param redeemedPointsTotal 累计消费积分
     * @param controlDaysTotal 累计约定未超标天数
     * @param controlStreak 连续约定未超标天数
     * @param encourageDaysTotal 累计鼓励达标天数
     * @param encourageStreak 连续鼓励达标天数
     */
    suspend fun checkAchievements(
        currentPoints: Double,
        redeemedPointsTotal: Double = 0.0,
        controlDaysTotal: Int = 0,
        controlStreak: Int = 0,
        encourageDaysTotal: Int = 0,
        encourageStreak: Int = 0
    ) {
        withContext(Dispatchers.IO) {
            val locked = achievementDao.getLockedAchievements()
            val now = System.currentTimeMillis()
            for (achievement in locked) {
                try {
                    val json = org.json.JSONObject(achievement.requirement)
                    val type = json.optString("type", "")
                    val value = json.optDouble("value", Double.MAX_VALUE)

                    val shouldUnlock = when (type) {
                        "points" -> currentPoints >= value
                        "redeem_points" -> redeemedPointsTotal >= value
                        "control_days" -> controlDaysTotal >= value.toInt()
                        "control_streak" -> controlStreak >= value.toInt()
                        "encourage_days" -> encourageDaysTotal >= value.toInt()
                        "encourage_streak" -> encourageStreak >= value.toInt()
                        else -> false
                    }

                    if (shouldUnlock) {
                        achievementDao.unlockAchievement(achievement.id, now)
                        _newAchievementsAction.emit(achievement.copy(isUnlocked = true, unlockedAt = now))
                    }
                } catch (e: Exception) {
                    android.util.Log.w("AchievementCheck", "Failed to parse requirement for ${achievement.id}", e)
                }
            }
        }
    }
}
