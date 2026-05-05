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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDate
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID

data class AppGroupWithApps(
    val group: AppGroupEntity,
    val packageNames: List<String>
)

sealed interface RedeemRewardResult {
    data class Success(
        val pointCost: Int,
        val message: String,
    ) : RedeemRewardResult

    data object InsufficientPoints : RedeemRewardResult

    data object OutOfStock : RedeemRewardResult

    data object MissingTargetGroup : RedeemRewardResult

    data object InvalidReward : RedeemRewardResult
}

enum class RewardSaveValidationError {
    TITLE_REQUIRED,
    POINT_COST_INVALID,
    STOCK_INVALID,
    REWARD_NOT_EDITABLE,
}

sealed interface RewardSaveResult {
    data object Success : RewardSaveResult

    data class Invalid(
        val error: RewardSaveValidationError,
    ) : RewardSaveResult
}

internal data class BuiltinRewardDefinition(
    val builtinKey: String,
    val title: String,
    val description: String,
    val rewardType: RewardType,
    val pointCost: Int,
    val stock: Int,
    val bonusMinutes: Int = 0,
)

data class AchievementProgress(
    val earnedPointsTotal: Double = 0.0,
    val redeemedPointsTotal: Double = 0.0,
    val controlDaysTotal: Int = 0,
    val controlStreak: Int = 0,
    val encourageDaysTotal: Int = 0,
    val encourageStreak: Int = 0,
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

internal fun calculateAchievementProgress(
    earnedPointsTotal: Double,
    redeemedPointsTotal: Double,
    archives: List<DailyArchiveEntity>,
): AchievementProgress {
    val sortedArchives = archives.sortedBy { it.archiveDate }
    return AchievementProgress(
        earnedPointsTotal = earnedPointsTotal,
        redeemedPointsTotal = redeemedPointsTotal,
        controlDaysTotal = sortedArchives.count { it.controlCompletedGroupCount > 0 },
        controlStreak = calculateCompletedArchiveStreak(sortedArchives) {
            it.controlCompletedGroupCount > 0
        },
        encourageDaysTotal = sortedArchives.count { it.encourageCompletedGroupCount > 0 },
        encourageStreak = calculateCompletedArchiveStreak(sortedArchives) {
            it.encourageCompletedGroupCount > 0
        },
    )
}

internal fun validateCustomRewardInput(
    title: String,
    pointCost: Int,
    stock: Int,
): RewardSaveValidationError? =
    when {
        title.isBlank() -> RewardSaveValidationError.TITLE_REQUIRED
        pointCost <= 0 -> RewardSaveValidationError.POINT_COST_INVALID
        stock != -1 && stock <= 0 -> RewardSaveValidationError.STOCK_INVALID
        else -> null
    }

private val BUILTIN_REWARD_DEFINITIONS =
    listOf(
        BuiltinRewardDefinition(
            builtinKey = "reward_time_pack_30",
            title = "30-minute Time Pass",
            description = "Get 30 extra minutes immediately.",
            rewardType = RewardType.TIME_PACK,
            pointCost = 50,
            stock = -1,
            bonusMinutes = 30,
        ),
        BuiltinRewardDefinition(
            builtinKey = "reward_time_pack_60",
            title = "1-hour Free Browsing Pass",
            description = "Get 1 extra hour immediately.",
            rewardType = RewardType.TIME_PACK,
            pointCost = 100,
            stock = -1,
            bonusMinutes = 60,
        ),
        BuiltinRewardDefinition(
            builtinKey = "reward_offline_treat",
            title = "Offline Treat",
            description = "Treat yourself offline.",
            rewardType = RewardType.CUSTOM,
            pointCost = 500,
            stock = 5,
        ),
    )

private fun calculateCompletedArchiveStreak(
    sortedArchives: List<DailyArchiveEntity>,
    isCompleted: (DailyArchiveEntity) -> Boolean,
): Int {
    var expectedDate: LocalDate? = null
    var streak = 0
    for (archive in sortedArchives.asReversed()) {
        val archiveDate = runCatching { LocalDate.parse(archive.archiveDate) }.getOrNull() ?: break
        val currentExpectedDate = expectedDate
        if (currentExpectedDate != null && archiveDate != currentExpectedDate) break
        if (!isCompleted(archive)) break
        streak += 1
        expectedDate = archiveDate.minusDays(1)
    }
    return streak
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
    private val dailyArchiveDao = database.dailyArchiveDao()
    private val preferences = ManagedAppPreferences(context)
    private val rewardRedeemMutex = Mutex()

    private val _newAchievementsAction = MutableSharedFlow<AchievementEntity>()
    val newAchievementsAction: SharedFlow<AchievementEntity> = _newAchievementsAction.asSharedFlow()

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
    ): RedeemRewardResult {
        return withContext(Dispatchers.IO) {
            rewardRedeemMutex.withLock {
                val latestReward =
                    redemptionDao.getRedemptionById(reward.id)
                        ?: return@withLock RedeemRewardResult.InvalidReward
                if (!latestReward.isActive || latestReward.pointCost <= 0) {
                    return@withLock RedeemRewardResult.InvalidReward
                }
                if (latestReward.stock == 0) {
                    return@withLock RedeemRewardResult.OutOfStock
                }

                val targetGroup =
                    if (latestReward.rewardType == RewardType.TIME_PACK) {
                        if (latestReward.bonusMinutes <= 0) {
                            return@withLock RedeemRewardResult.InvalidReward
                        }
                        val groupId = targetGroupId ?: return@withLock RedeemRewardResult.MissingTargetGroup
                        groupDao.getGroupByIdSync(groupId)
                            ?.takeIf { it.type == GroupType.CONTROL }
                            ?: return@withLock RedeemRewardResult.MissingTargetGroup
                    } else {
                        null
                    }

                if (preferences.userPoints.first() < latestReward.pointCost) {
                    return@withLock RedeemRewardResult.InsufficientPoints
                }

                val redeemedAt = System.currentTimeMillis()
                val redemptionHistoryId = UUID.randomUUID().toString()
                val ledgerEntryId = UUID.randomUUID().toString()
                var createdBonusId: String? = null
                val targetGroupName = targetGroup?.name
                val redeemedGroupId =
                    if (latestReward.rewardType == RewardType.TIME_PACK) targetGroup!!.id else null

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

                checkAchievements()
                RedeemRewardResult.Success(
                    pointCost = latestReward.pointCost,
                    message =
                        when (latestReward.rewardType) {
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
                        },
                )
            }
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

    fun observeAchievementProgress(): Flow<AchievementProgress> =
        combine(
            pointLedgerDao.observeEarnedTotal(),
            pointLedgerDao.observeRewardSpentTotal(),
            dailyArchiveDao.observeAllAsc(),
        ) { earnedPointsTotal, redeemedPointsTotal, archives ->
            calculateAchievementProgress(
                earnedPointsTotal = earnedPointsTotal,
                redeemedPointsTotal = redeemedPointsTotal,
                archives = archives,
            )
        }

    suspend fun getAchievementProgress(): AchievementProgress =
        withContext(Dispatchers.IO) {
            calculateAchievementProgress(
                earnedPointsTotal = pointLedgerDao.sumEarnedTotal(),
                redeemedPointsTotal = pointLedgerDao.sumRewardSpentTotal(),
                archives = dailyArchiveDao.getAllAsc(),
            )
        }

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
    ): RewardSaveResult {
        val validation = validateCustomRewardInput(title, cost, stock)
        if (validation != null) {
            return RewardSaveResult.Invalid(validation)
        }
        if (type != RewardType.CUSTOM || builtinKey != null || bonusMinutes != 0) {
            return RewardSaveResult.Invalid(RewardSaveValidationError.REWARD_NOT_EDITABLE)
        }
        withContext(Dispatchers.IO) {
            redemptionDao.insertRedemption(
                RedemptionEntity(
                    id = UUID.randomUUID().toString(),
                    title = title.trim(),
                    description = description.trim(),
                    builtinKey = null,
                    pointCost = cost,
                    rewardType = type,
                    bonusMinutes = 0,
                    stock = stock,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
            )
        }
        return RewardSaveResult.Success
    }

    suspend fun updateReward(reward: RedemptionEntity): RewardSaveResult {
        if (reward.builtinKey != null) {
            return RewardSaveResult.Invalid(RewardSaveValidationError.REWARD_NOT_EDITABLE)
        }
        val validation = validateCustomRewardInput(reward.title, reward.pointCost, reward.stock)
        if (validation != null) {
            return RewardSaveResult.Invalid(validation)
        }
        withContext(Dispatchers.IO) {
            redemptionDao.insertRedemption(
                reward.copy(
                    title = reward.title.trim(),
                    description = reward.description.trim(),
                    updatedAt = System.currentTimeMillis(),
                )
            )
        }
        return RewardSaveResult.Success
    }

    suspend fun archiveReward(rewardId: String) {
        withContext(Dispatchers.IO) {
            redemptionDao.deactivateRedemption(rewardId)
        }
    }

    suspend fun syncBuiltinRewards() {
        withContext(Dispatchers.IO) {
            val activeBuiltinKeys = BUILTIN_REWARD_DEFINITIONS.map { it.builtinKey }
            redemptionDao.deactivateBuiltinRedemptionsExcept(activeBuiltinKeys)
            BUILTIN_REWARD_DEFINITIONS.forEach { definition ->
                upsertBuiltinReward(definition)
            }
        }
    }

    /** 兼容旧初始化入口 */
    suspend fun seedInitialData() {
        withContext(Dispatchers.IO) {
            syncBuiltinRewards()
            syncAchievementDefinitionsInternal()
        }
    }

    private suspend fun upsertBuiltinReward(definition: BuiltinRewardDefinition) {
        val now = System.currentTimeMillis()
        val existing = redemptionDao.getRedemptionByBuiltinKey(definition.builtinKey)
        redemptionDao.insertRedemption(
            RedemptionEntity(
                id = existing?.id ?: "builtin:${definition.builtinKey}",
                title = definition.title,
                description = definition.description,
                builtinKey = definition.builtinKey,
                pointCost = definition.pointCost,
                rewardType = definition.rewardType,
                bonusMinutes = definition.bonusMinutes,
                isActive = true,
                stock = definition.stock,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            )
        )
    }

    suspend fun syncAchievementDefinitions() {
        withContext(Dispatchers.IO) {
            syncAchievementDefinitionsInternal()
        }
    }

    private suspend fun syncAchievementDefinitionsInternal() {
        suspend fun seed(id: String, title: String, desc: String, req: String, tier: Int, emoji: String) {
            achievementDao.upsertAchievementDefinition(AchievementEntity(id, title, desc, req, tier, emoji))
        }

        // ══════════ 🥉 Bronze 铜阶 ══════════
        seed("BRONZE_POINTS", "Starlet Gatherer", "Earn 1,000 points",
            """{"type":"points","value":1000}""", AchievementTier.BRONZE, "🌱")
        seed("BRONZE_REDEEM", "First Keepsake", "Spend 1,000 points",
            """{"type":"redeem_points","value":1000}""", AchievementTier.BRONZE, "🍬")
        seed("BRONZE_CTRL_DAYS", "Grain of Promise", "Stay within commitment limits for 10 total days",
            """{"type":"control_days","value":10}""", AchievementTier.BRONZE, "🤝")
        seed("BRONZE_CTRL_STREAK", "Three Dawns", "Stay within commitment limits for 3 days in a row",
            """{"type":"control_streak","value":3}""", AchievementTier.BRONZE, "🪨")
        seed("BRONZE_ENC_DAYS", "Sprout of Sun", "Meet encouragement goals for 10 total days",
            """{"type":"encourage_days","value":10}""", AchievementTier.BRONZE, "🌻")
        seed("BRONZE_ENC_STREAK", "Threefold Gleam", "Meet encouragement goals for 3 days in a row",
            """{"type":"encourage_streak","value":3}""", AchievementTier.BRONZE, "🕯️")

        // ══════════ 🥈 Silver 银阶 ══════════
        seed("SILVER_POINTS", "Moonlight Seeker", "Earn 3,000 points",
            """{"type":"points","value":3000}""", AchievementTier.SILVER, "🌊")
        seed("SILVER_REDEEM", "Silver Wish", "Spend 3,000 points",
            """{"type":"redeem_points","value":3000}""", AchievementTier.SILVER, "🛒")
        seed("SILVER_CTRL_DAYS", "Verdant Stone", "Stay within commitment limits for 30 total days",
            """{"type":"control_days","value":30}""", AchievementTier.SILVER, "⛰️")
        seed("SILVER_CTRL_STREAK", "Tenfold Dawn", "Stay within commitment limits for 10 days in a row",
            """{"type":"control_streak","value":10}""", AchievementTier.SILVER, "🌓")
        seed("SILVER_ENC_DAYS", "Dewlit Branch", "Meet encouragement goals for 30 total days",
            """{"type":"encourage_days","value":30}""", AchievementTier.SILVER, "📖")
        seed("SILVER_ENC_STREAK", "Ten Rays Aligned", "Meet encouragement goals for 10 days in a row",
            """{"type":"encourage_streak","value":10}""", AchievementTier.SILVER, "🌿")

        // ══════════ 🥇 Gold 金阶 ══════════
        seed("GOLD_POINTS", "Solar Chaser", "Earn 10,000 points",
            """{"type":"points","value":10000}""", AchievementTier.GOLD, "👑")
        seed("GOLD_REDEEM", "Golden Seal", "Spend 10,000 points",
            """{"type":"redeem_points","value":10000}""", AchievementTier.GOLD, "🎯")
        seed("GOLD_CTRL_DAYS", "Rampart Builder", "Stay within commitment limits for 100 total days",
            """{"type":"control_days","value":100}""", AchievementTier.GOLD, "🛡️")
        seed("GOLD_CTRL_STREAK", "Balanced Moon", "Stay within commitment limits for 30 days in a row",
            """{"type":"control_streak","value":30}""", AchievementTier.GOLD, "🌙")
        seed("GOLD_ENC_DAYS", "Field in Bloom", "Meet encouragement goals for 100 total days",
            """{"type":"encourage_days","value":100}""", AchievementTier.GOLD, "🔥")
        seed("GOLD_ENC_STREAK", "Unfading Moonlight", "Meet encouragement goals for 30 days in a row",
            """{"type":"encourage_streak","value":30}""", AchievementTier.GOLD, "🎍")

        // ══════════ 💎 Diamond 钻石阶 ══════════
        seed("DIAMOND_POINTS", "River of Stars", "Earn 30,000 points",
            """{"type":"points","value":30000}""", AchievementTier.DIAMOND, "💫")
        seed("DIAMOND_REDEEM", "Crystal Keybearer", "Spend 30,000 points",
            """{"type":"redeem_points","value":30000}""", AchievementTier.DIAMOND, "🏛️")
        seed("DIAMOND_CTRL_DAYS", "Crystal Citadel", "Stay within commitment limits for 365 total days",
            """{"type":"control_days","value":365}""", AchievementTier.DIAMOND, "🏰")
        seed("DIAMOND_CTRL_STREAK", "Hundred Days True", "Stay within commitment limits for 100 days in a row",
            """{"type":"control_streak","value":100}""", AchievementTier.DIAMOND, "⚡")
        seed("DIAMOND_ENC_DAYS", "Canopy of Green", "Meet encouragement goals for 365 total days",
            """{"type":"encourage_days","value":365}""", AchievementTier.DIAMOND, "⚔️")
        seed("DIAMOND_ENC_STREAK", "Crown of Hundred Rays", "Meet encouragement goals for 100 days in a row",
            """{"type":"encourage_streak","value":100}""", AchievementTier.DIAMOND, "🗡️")

        // ══════════ 🌟 Legendary 传奇阶 ══════════
        seed("LEGEND_POINTS", "Celestial Ascendant", "Earn 100,000 points",
            """{"type":"points","value":100000}""", AchievementTier.LEGENDARY, "🐉")
        seed("LEGEND_REDEEM", "Vault of Wonders", "Spend 100,000 points",
            """{"type":"redeem_points","value":100000}""", AchievementTier.LEGENDARY, "💰")
        seed("LEGEND_CTRL_DAYS", "Eternal Peak", "Stay within commitment limits for 1,000 total days",
            """{"type":"control_days","value":1000}""", AchievementTier.LEGENDARY, "⭐")
        seed("LEGEND_CTRL_STREAK", "Yearbound Oath", "Stay within commitment limits for 365 days in a row",
            """{"type":"control_streak","value":365}""", AchievementTier.LEGENDARY, "🔱")
        seed("LEGEND_ENC_DAYS", "Sunlit Courtyard", "Meet encouragement goals for 1,000 total days",
            """{"type":"encourage_days","value":1000}""", AchievementTier.LEGENDARY, "🌈")
        seed("LEGEND_ENC_STREAK", "Everlasting Radiance", "Meet encouragement goals for 365 days in a row",
            """{"type":"encourage_streak","value":365}""", AchievementTier.LEGENDARY, "⏳")
    }

    private fun localizedRewardTitle(reward: RedemptionEntity): String =
        reward.builtinKey?.let { AppText.t("${it}_title") } ?: reward.title

    /** 检查并解锁达成条件的成就 */
    suspend fun checkAchievements() {
        checkAchievements(getAchievementProgress())
    }

    internal suspend fun checkAchievements(progress: AchievementProgress) {
        withContext(Dispatchers.IO) {
            val locked = achievementDao.getLockedAchievements()
            val now = System.currentTimeMillis()
            for (achievement in locked) {
                try {
                    val json = org.json.JSONObject(achievement.requirement)
                    val type = json.optString("type", "")
                    val value = json.optDouble("value", Double.MAX_VALUE)

                    val shouldUnlock = when (type) {
                        "points" -> progress.earnedPointsTotal >= value
                        "redeem_points" -> progress.redeemedPointsTotal >= value
                        "control_days" -> progress.controlDaysTotal >= value.toInt()
                        "control_streak" -> progress.controlStreak >= value.toInt()
                        "encourage_days" -> progress.encourageDaysTotal >= value.toInt()
                        "encourage_streak" -> progress.encourageStreak >= value.toInt()
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
