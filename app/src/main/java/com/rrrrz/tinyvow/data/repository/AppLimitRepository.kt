package com.rrrrz.tinyvow.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.rrrrz.tinyvow.data.db.*
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.i18n.AppText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID

data class AppGroupWithApps(
    val group: AppGroupEntity,
    val packageNames: List<String>
)

data class RedemptionResult(
    val pointCost: Int,
    val message: String,
)

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

class AppLimitRepository(
    private val context: Context,
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
            if (latestReward.pointCost <= 0) return@withContext null
            if (preferences.userPoints.first() < latestReward.pointCost) return@withContext null

            val redeemedAt = System.currentTimeMillis()
            val redemptionHistoryId = UUID.randomUUID().toString()
            val ledgerEntryId = UUID.randomUUID().toString()
            var createdBonusId: String? = null
            val targetGroup =
                if (latestReward.rewardType == RewardType.TIME_PACK) {
                    if (latestReward.bonusMinutes <= 0) return@withContext null
                    val groupId = targetGroupId ?: return@withContext null
                    groupDao.getGroupByIdSync(groupId)
                        ?.takeIf { it.type == GroupType.CONTROL }
                        ?: return@withContext null
                } else {
                    null
                }
            val targetGroupName = targetGroup?.name
            val redeemedGroupId =
                if (latestReward.rewardType == RewardType.TIME_PACK) {
                    targetGroup!!.id
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
                        rewardBuiltinKey = latestReward.builtinKey,
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
                        messageKey = when (latestReward.rewardType) {
                            RewardType.TIME_PACK -> "ledger_redeemed_time_pack"
                            RewardType.CUSTOM -> "ledger_redeemed_custom_reward"
                        },
                        messageArgsJson = when (latestReward.rewardType) {
                            RewardType.TIME_PACK ->
                                JSONObject()
                                    .put("rewardTitle", latestReward.title)
                                    .put("pointCost", latestReward.pointCost)
                                    .put("groupName", targetGroupName.orEmpty())
                                    .put("bonusMinutes", latestReward.bonusMinutes)
                                    .toString()
                            RewardType.CUSTOM ->
                                JSONObject()
                                    .put("rewardTitle", latestReward.title)
                                    .put("pointCost", latestReward.pointCost)
                                    .toString()
                        },
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
                    val groupName = targetGroupName ?: AppText.t("generic_target_group")
                    AppText.t(
                        "redeem_success_time_pack",
                        localizedRewardTitle(latestReward),
                        latestReward.pointCost,
                        groupName,
                        latestReward.bonusMinutes,
                    )
                }
                RewardType.CUSTOM -> {
                    AppText.t(
                        "redeem_success_custom_reward",
                        localizedRewardTitle(latestReward),
                        latestReward.pointCost,
                    )
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
            val sortOrder = existing?.sortOrder ?: (groupDao.getMaxSortOrder(type) + 1)
            
            val entity = AppGroupEntity(
                id = groupId,
                name = name,
                type = type,
                limitPeriod = limitPeriod,
                limitMinutes = limitMinutes,
                pointsPerMinute = pointsPerMinute,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isDeleted = existing?.isDeleted ?: false,
                lastBonusAt = existing?.lastBonusAt ?: 0L,
                sortOrder = sortOrder,
            )
            groupDao.insertGroup(entity)
            groupId
        }
    }

    suspend fun reorderGroups(type: GroupType, orderedIds: List<String>) {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            database.withTransaction {
                orderedIds.distinct().forEachIndexed { index, groupId ->
                    groupDao.updateSortOrder(groupId, type, index, now)
                }
            }
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
        val groupPeriod = groupDao.getGroupByIdSync(groupId)?.limitPeriod ?: LimitPeriod.DAILY
        val bonus =
            BonusTimeEntity(
                id = bonusId,
                targetGroupId = groupId,
                extraMinutes = extraMinutes,
                expiryTime = calculateBonusExpiryTime(createdAt, groupPeriod),
                createdAt = createdAt,
            )
        bonusTimeDao.insertBonusTime(bonus)
    }

    suspend fun addReward(
        title: String,
        cost: Int,
        type: RewardType,
        stock: Int = -1,
        description: String = "",
        bonusMinutes: Int = 0,
        builtinKey: String? = null,
    ) {
        withContext(Dispatchers.IO) {
            redemptionDao.insertRedemption(
                RedemptionEntity(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    description = description,
                    builtinKey = builtinKey,
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
            addReward("30-minute Time Pass", 50, RewardType.TIME_PACK, -1, "Get 30 extra minutes immediately.", 30, "reward_time_pack_30")
            addReward("1-hour Free Browsing Pass", 100, RewardType.TIME_PACK, -1, "Get 1 extra hour immediately.", 60, "reward_time_pack_60")
            addReward("Offline Treat", 500, RewardType.CUSTOM, 5, "Treat yourself offline.", 0, "reward_offline_treat")

            // 使用 seedAchievement (IGNORE) 避免覆盖已解锁状态
            suspend fun seed(id: String, title: String, desc: String, req: String, tier: Int, emoji: String) {
                achievementDao.seedAchievement(AchievementEntity(id, title, desc, req, tier, emoji))
            }

            // ══════════ 🥉 Bronze 铜阶 ══════════
            seed("BRONZE_POINTS", "First Steps", "Earn 100 points",
                """{"type":"points","value":100}""", AchievementTier.BRONZE, "🌱")
            seed("BRONZE_REDEEM", "First Spend", "Spend 100 points",
                """{"type":"redeem_points","value":100}""", AchievementTier.BRONZE, "🍬")
            seed("BRONZE_CTRL_DAYS", "Small Wins", "Stay within commitment limits for 10 total days",
                """{"type":"control_days","value":10}""", AchievementTier.BRONZE, "🤝")
            seed("BRONZE_CTRL_STREAK", "Three-Day Rock", "Stay within commitment limits for 3 days in a row",
                """{"type":"control_streak","value":3}""", AchievementTier.BRONZE, "🪨")
            seed("BRONZE_ENC_DAYS", "Toward the Sun", "Meet encouragement goals for 10 total days",
                """{"type":"encourage_days","value":10}""", AchievementTier.BRONZE, "🌻")
            seed("BRONZE_ENC_STREAK", "Original Intention", "Meet encouragement goals for 3 days in a row",
                """{"type":"encourage_streak","value":3}""", AchievementTier.BRONZE, "🕯️")

            // ══════════ 🥈 Silver 银阶 ══════════
            seed("SILVER_POINTS", "Ride the Waves", "Earn 300 points",
                """{"type":"points","value":300}""", AchievementTier.SILVER, "🌊")
            seed("SILVER_REDEEM", "Smart Shopper", "Spend 300 points",
                """{"type":"redeem_points","value":300}""", AchievementTier.SILVER, "🛒")
            seed("SILVER_CTRL_DAYS", "Steady as a Mountain", "Stay within commitment limits for 30 total days",
                """{"type":"control_days","value":30}""", AchievementTier.SILVER, "⛰️")
            seed("SILVER_CTRL_STREAK", "Ten-Day Stand", "Stay within commitment limits for 10 days in a row",
                """{"type":"control_streak","value":10}""", AchievementTier.SILVER, "🌓")
            seed("SILVER_ENC_DAYS", "Habit Builder", "Meet encouragement goals for 30 total days",
                """{"type":"encourage_days","value":30}""", AchievementTier.SILVER, "📖")
            seed("SILVER_ENC_STREAK", "Ten Out of Ten", "Meet encouragement goals for 10 days in a row",
                """{"type":"encourage_streak","value":10}""", AchievementTier.SILVER, "🌿")

            // ══════════ 🥇 Gold 金阶 ══════════
            seed("GOLD_POINTS", "Thousand-Point Master", "Earn 1000 points",
                """{"type":"points","value":1000}""", AchievementTier.GOLD, "👑")
            seed("GOLD_REDEEM", "Bounty Hunter", "Spend 1000 points",
                """{"type":"redeem_points","value":1000}""", AchievementTier.GOLD, "🎯")
            seed("GOLD_CTRL_DAYS", "Hundred-Day Guardian", "Stay within commitment limits for 100 total days",
                """{"type":"control_days","value":100}""", AchievementTier.GOLD, "🛡️")
            seed("GOLD_CTRL_STREAK", "Moon Warrior", "Stay within commitment limits for 30 days in a row",
                """{"type":"control_streak","value":30}""", AchievementTier.GOLD, "🌙")
            seed("GOLD_ENC_DAYS", "Forged by Practice", "Meet encouragement goals for 100 total days",
                """{"type":"encourage_days","value":100}""", AchievementTier.GOLD, "🔥")
            seed("GOLD_ENC_STREAK", "Bamboo Momentum", "Meet encouragement goals for 30 days in a row",
                """{"type":"encourage_streak","value":30}""", AchievementTier.GOLD, "🎍")

            // ══════════ 💎 Diamond 钻石阶 ══════════
            seed("DIAMOND_POINTS", "Known Far and Wide", "Earn 3000 points",
                """{"type":"points","value":3000}""", AchievementTier.DIAMOND, "💫")
            seed("DIAMOND_REDEEM", "Big Spender", "Spend 3000 points",
                """{"type":"redeem_points","value":3000}""", AchievementTier.DIAMOND, "🏛️")
            seed("DIAMOND_CTRL_DAYS", "A Year in Tune", "Stay within commitment limits for 365 total days",
                """{"type":"control_days","value":365}""", AchievementTier.DIAMOND, "🏰")
            seed("DIAMOND_CTRL_STREAK", "Hundred-Day Flawless", "Stay within commitment limits for 100 days in a row",
                """{"type":"control_streak","value":100}""", AchievementTier.DIAMOND, "⚡")
            seed("DIAMOND_ENC_DAYS", "A Full Year Ahead", "Meet encouragement goals for 365 total days",
                """{"type":"encourage_days","value":365}""", AchievementTier.DIAMOND, "⚔️")
            seed("DIAMOND_ENC_STREAK", "Hundred Battles Won", "Meet encouragement goals for 100 days in a row",
                """{"type":"encourage_streak","value":100}""", AchievementTier.DIAMOND, "🗡️")

            // ══════════ 🌟 Legendary 传奇阶 ══════════
            seed("LEGEND_POINTS", "Immortal Legend", "Earn 10000 points",
                """{"type":"points","value":10000}""", AchievementTier.LEGENDARY, "🐉")
            seed("LEGEND_REDEEM", "Ten Thousand Spent", "Spend 10000 points",
                """{"type":"redeem_points","value":10000}""", AchievementTier.LEGENDARY, "💰")
            seed("LEGEND_CTRL_DAYS", "Eternal Commitment", "Stay within commitment limits for 10000 total days",
                """{"type":"control_days","value":10000}""", AchievementTier.LEGENDARY, "⭐")
            seed("LEGEND_CTRL_STREAK", "Vow Eternal", "Stay within commitment limits for 365 days in a row",
                """{"type":"control_streak","value":365}""", AchievementTier.LEGENDARY, "🔱")
            seed("LEGEND_ENC_DAYS", "Bright as the Sun", "Meet encouragement goals for 10000 total days",
                """{"type":"encourage_days","value":10000}""", AchievementTier.LEGENDARY, "🌈")
            seed("LEGEND_ENC_STREAK", "Time Keeper", "Meet encouragement goals for 365 days in a row",
                """{"type":"encourage_streak","value":365}""", AchievementTier.LEGENDARY, "⏳")
        }
    }

    private fun localizedRewardTitle(reward: RedemptionEntity): String =
        reward.builtinKey?.let { AppText.t("${it}_title") } ?: reward.title

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
