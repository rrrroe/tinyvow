package com.rrrrz.tinyvow.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.rrrrz.tinyvow.data.db.ActiveRewardEffectDao
import com.rrrrz.tinyvow.data.db.ActiveRewardEffectEntity
import com.rrrrz.tinyvow.data.db.ActiveRewardEffectStatus
import com.rrrrz.tinyvow.data.db.AchievementDao
import com.rrrrz.tinyvow.data.db.AchievementEntity
import com.rrrrz.tinyvow.data.db.AchievementTier
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.db.AppGroupDao
import com.rrrrz.tinyvow.data.db.AppGroupEntity
import com.rrrrz.tinyvow.data.db.CrossRefDao
import com.rrrrz.tinyvow.data.db.DailyArchiveDao
import com.rrrrz.tinyvow.data.db.DailyArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyGroupArchiveDao
import com.rrrrz.tinyvow.data.db.DailyGroupArchiveEntity
import com.rrrrz.tinyvow.data.db.GroupAppCrossRef
import com.rrrrz.tinyvow.data.db.GroupType
import com.rrrrz.tinyvow.data.db.LimitPeriod
import com.rrrrz.tinyvow.data.db.PointLedgerDao
import com.rrrrz.tinyvow.data.db.PointLedgerEntity
import com.rrrrz.tinyvow.data.db.PointLedgerEntryType
import com.rrrrz.tinyvow.data.db.RedemptionDao
import com.rrrrz.tinyvow.data.db.RedemptionEntity
import com.rrrrz.tinyvow.data.db.RedemptionHistoryDao
import com.rrrrz.tinyvow.data.db.RedemptionHistoryEntity
import com.rrrrz.tinyvow.data.db.RedemptionHistoryType
import com.rrrrz.tinyvow.data.db.RewardInventoryDao
import com.rrrrz.tinyvow.data.db.RewardInventoryEntity
import com.rrrrz.tinyvow.data.db.RewardType
import com.rrrrz.tinyvow.data.db.RewardUseHistoryDao
import com.rrrrz.tinyvow.data.db.RewardUseHistoryEntity
import com.rrrrz.tinyvow.data.db.StreakShieldPendingDao
import com.rrrrz.tinyvow.data.db.StreakShieldPendingEntity
import com.rrrrz.tinyvow.data.db.StreakShieldPendingStatus
import com.rrrrz.tinyvow.data.db.StreakShieldTarget
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import com.rrrrz.tinyvow.data.usage.UsageStatsUsageRepository
import com.rrrrz.tinyvow.i18n.AppText
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class AppGroupWithApps(
    val group: AppGroupEntity,
    val packageNames: List<String>,
)

data class RewardPayload(
    val minutes: Int = 0,
    val pointsMultiplier: Double = 1.0,
    val shieldTarget: StreakShieldTarget? = null,
)

data class RewardStoreItem(
    val reward: RedemptionEntity,
    val ownedQuantity: Int,
    val isManualUse: Boolean,
    val purchasedTodayCount: Int,
)

data class InventoryRewardItem(
    val reward: RedemptionEntity,
    val quantity: Int,
    val activeCount: Int,
    val pendingCount: Int,
)

data class PendingStreakShieldItem(
    val pending: StreakShieldPendingEntity,
    val title: String,
    val ownedQuantity: Int,
)

sealed interface InventoryRecordTab {
    data object Items : InventoryRecordTab

    data object Purchases : InventoryRecordTab

    data object Uses : InventoryRecordTab
}

sealed interface PurchaseRewardResult {
    data class Success(
        val rewardTitle: String,
        val pointCost: Int,
    ) : PurchaseRewardResult

    data object InsufficientPoints : PurchaseRewardResult

    data object OutOfStock : PurchaseRewardResult

    data object DailyLimitReached : PurchaseRewardResult

    data object InvalidReward : PurchaseRewardResult
}

sealed interface UseRewardResult {
    data class Success(
        val rewardTitle: String,
        val messageKey: String,
        val messageArgs: List<Any> = emptyList(),
    ) : UseRewardResult

    data object NotOwned : UseRewardResult

    data object InvalidTargetGroup : UseRewardResult

    data object AlreadyActive : UseRewardResult

    data object AlreadyCompleted : UseRewardResult

    data object InvalidReward : UseRewardResult
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
    val payload: RewardPayload = RewardPayload(),
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

private data class CompletionSignal(
    val completedDates: Set<LocalDate>,
    val shieldedDates: Set<LocalDate>,
)

internal fun calculateAchievementProgress(
    earnedPointsTotal: Double,
    redeemedPointsTotal: Double,
    archives: List<DailyArchiveEntity>,
    groupArchives: List<DailyGroupArchiveEntity> = emptyList(),
    shieldPendings: List<StreakShieldPendingEntity> = emptyList(),
): AchievementProgress {
    val sortedArchives = archives.sortedBy { it.archiveDate }
    if (groupArchives.isEmpty()) {
        return AchievementProgress(
            earnedPointsTotal = earnedPointsTotal,
            redeemedPointsTotal = redeemedPointsTotal,
            controlDaysTotal = sortedArchives.count { it.controlCompletedGroupCount > 0 },
            controlStreak = calculateLegacyCompletedArchiveStreak(sortedArchives) { it.controlCompletedGroupCount > 0 },
            encourageDaysTotal = sortedArchives.count { it.encourageCompletedGroupCount > 0 },
            encourageStreak = calculateLegacyCompletedArchiveStreak(sortedArchives) { it.encourageCompletedGroupCount > 0 },
        )
    }
    val groupedByDate = groupArchives.groupBy { it.archiveDate }
    val usedPendings = shieldPendings.filter { it.status == StreakShieldPendingStatus.USED }
    val controlCompletedDates =
        completionDatesForGroupType(
            groupedByDate = groupedByDate,
            groupType = GroupType.CONTROL,
        )
    val encourageCompletedDates =
        completionDatesForGroupType(
            groupedByDate = groupedByDate,
            groupType = GroupType.ENCOURAGE,
        )
    val controlSignal =
        buildCompletionSignal(
            groupedByDate = groupedByDate,
            groupType = GroupType.CONTROL,
            shieldTarget = StreakShieldTarget.CONTROL_STREAK,
            usedPendings = usedPendings,
        )
    val encourageSignal =
        buildCompletionSignal(
            groupedByDate = groupedByDate,
            groupType = GroupType.ENCOURAGE,
            shieldTarget = StreakShieldTarget.ENCOURAGE_STREAK,
            usedPendings = usedPendings,
        )
    return AchievementProgress(
        earnedPointsTotal = earnedPointsTotal,
        redeemedPointsTotal = redeemedPointsTotal,
        controlDaysTotal = controlCompletedDates.size,
        controlStreak = calculateCompletedArchiveStreak(sortedArchives, controlSignal),
        encourageDaysTotal = encourageCompletedDates.size,
        encourageStreak = calculateCompletedArchiveStreak(sortedArchives, encourageSignal),
    )
}

private fun calculateLegacyCompletedArchiveStreak(
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
            builtinKey = "reward_time_add_15",
            title = "Extra 15 minutes",
            description = "Buy now, use from inventory on one control group.",
            rewardType = RewardType.TIME_ADD,
            pointCost = 15,
            stock = -1,
            payload = RewardPayload(minutes = 15),
        ),
        BuiltinRewardDefinition(
            builtinKey = "reward_time_add_30",
            title = "Extra 30 minutes",
            description = "Buy now, use from inventory on one control group.",
            rewardType = RewardType.TIME_ADD,
            pointCost = 30,
            stock = -1,
            payload = RewardPayload(minutes = 30),
        ),
        BuiltinRewardDefinition(
            builtinKey = "reward_time_add_60",
            title = "Extra 60 minutes",
            description = "Buy now, use from inventory on one control group.",
            rewardType = RewardType.TIME_ADD,
            pointCost = 60,
            stock = -1,
            payload = RewardPayload(minutes = 60),
        ),
        BuiltinRewardDefinition(
            builtinKey = "reward_period_pass",
            title = "Current period free pass",
            description = "Use on one control group to skip blocking for the active window.",
            rewardType = RewardType.PERIOD_PASS,
            pointCost = 200,
            stock = -1,
        ),
        BuiltinRewardDefinition(
            builtinKey = "reward_emergency_unlock_10",
            title = "Emergency unlock 10 min",
            description = "Buy from the store first. Use only from the blocking overlay.",
            rewardType = RewardType.EMERGENCY_UNLOCK,
            pointCost = 1,
            stock = -1,
            payload = RewardPayload(minutes = 10),
        ),
        BuiltinRewardDefinition(
            builtinKey = "reward_streak_shield_control",
            title = "Control streak shield",
            description = "Protect one control streak break after archive review.",
            rewardType = RewardType.STREAK_SHIELD,
            pointCost = 100,
            stock = -1,
            payload = RewardPayload(shieldTarget = StreakShieldTarget.CONTROL_STREAK),
        ),
        BuiltinRewardDefinition(
            builtinKey = "reward_streak_shield_encourage",
            title = "Encourage streak shield",
            description = "Protect one encourage streak break after archive review.",
            rewardType = RewardType.STREAK_SHIELD,
            pointCost = 100,
            stock = -1,
            payload = RewardPayload(shieldTarget = StreakShieldTarget.ENCOURAGE_STREAK),
        ),
        BuiltinRewardDefinition(
            builtinKey = "reward_double_points_day",
            title = "Daily double points",
            description = "Use on one encourage group. Points are doubled until today ends.",
            rewardType = RewardType.DOUBLE_POINTS_DAY,
            pointCost = 10,
            stock = -1,
            payload = RewardPayload(pointsMultiplier = 2.0),
        ),
    )

private fun buildCompletionSignal(
    groupedByDate: Map<String, List<DailyGroupArchiveEntity>>,
    groupType: GroupType,
    shieldTarget: StreakShieldTarget,
    usedPendings: List<StreakShieldPendingEntity>,
): CompletionSignal {
    val completedDates = completionDatesForGroupType(groupedByDate = groupedByDate, groupType = groupType)
    val shieldedDates =
        usedPendings
            .filter { it.shieldTarget == shieldTarget }
            .mapNotNull { runCatching { LocalDate.parse(it.archiveDate) }.getOrNull() }
            .toSet()
    return CompletionSignal(completedDates = completedDates, shieldedDates = shieldedDates)
}

private fun completionDatesForGroupType(
    groupedByDate: Map<String, List<DailyGroupArchiveEntity>>,
    groupType: GroupType,
): Set<LocalDate> =
    groupedByDate
        .mapNotNull { (archiveDate, archives) ->
            val sameTypeArchives = archives.filter { it.groupType == groupType }
            if (sameTypeArchives.isEmpty()) {
                return@mapNotNull null
            }
            val isCompleted =
                when (groupType) {
                    GroupType.CONTROL -> sameTypeArchives.all { it.completed }
                    GroupType.ENCOURAGE -> sameTypeArchives.any { it.completed }
                }
            if (!isCompleted) {
                return@mapNotNull null
            }
            runCatching { LocalDate.parse(archiveDate) }.getOrNull()
        }
        .toSet()

private fun calculateCompletedArchiveStreak(
    sortedArchives: List<DailyArchiveEntity>,
    signal: CompletionSignal,
): Int {
    var expectedDate: LocalDate? = null
    var streak = 0
    for (archive in sortedArchives.asReversed()) {
        val archiveDate = runCatching { LocalDate.parse(archive.archiveDate) }.getOrNull() ?: break
        val currentExpectedDate = expectedDate
        if (currentExpectedDate != null && archiveDate != currentExpectedDate) break
        if (archiveDate !in signal.completedDates && archiveDate !in signal.shieldedDates) break
        streak += 1
        expectedDate = archiveDate.minusDays(1)
    }
    return streak
}

private fun RewardPayload.toJson(): String =
    JSONObject().apply {
        if (minutes > 0) put("minutes", minutes)
        if (pointsMultiplier > 1.0) put("pointsMultiplier", pointsMultiplier)
        shieldTarget?.let { put("shieldTarget", it.name) }
    }.toString()

internal fun parseRewardPayload(payloadJson: String?): RewardPayload {
    if (payloadJson.isNullOrBlank()) return RewardPayload()
    return runCatching {
        val json = JSONObject(payloadJson)
        RewardPayload(
            minutes = json.optInt("minutes", 0),
            pointsMultiplier =
                when {
                    json.has("pointsMultiplier") -> json.optDouble("pointsMultiplier", 1.0)
                    json.optInt("bonusPoints", 0) > 0 -> 2.0
                    else -> 1.0
                },
            shieldTarget =
                json.optString("shieldTarget")
                    .takeIf { it.isNotBlank() }
                    ?.let(StreakShieldTarget::valueOf),
        )
    }.getOrDefault(RewardPayload())
}

private fun RedemptionEntity.payload(): RewardPayload =
    parseRewardPayload(payloadJson).let { parsed ->
        if (parsed.minutes <= 0 && bonusMinutes > 0) {
            parsed.copy(minutes = bonusMinutes)
        } else {
            parsed
        }
    }

private fun localizedRewardTitle(reward: RedemptionEntity): String =
    reward.builtinKey?.let { AppText.t("${it}_title") } ?: reward.title

private fun localizedRewardDescription(reward: RedemptionEntity): String =
    reward.builtinKey?.let { AppText.t("${it}_description") } ?: reward.description

private fun rewardHistoryTypeFor(rewardType: RewardType): RedemptionHistoryType =
    when (rewardType) {
        RewardType.TIME_ADD -> RedemptionHistoryType.TIME_ADD
        RewardType.PERIOD_PASS -> RedemptionHistoryType.PERIOD_PASS
        RewardType.EMERGENCY_UNLOCK -> RedemptionHistoryType.EMERGENCY_UNLOCK
        RewardType.STREAK_SHIELD -> RedemptionHistoryType.STREAK_SHIELD
        RewardType.DOUBLE_POINTS_DAY -> RedemptionHistoryType.DOUBLE_POINTS_DAY
        RewardType.CUSTOM -> RedemptionHistoryType.CUSTOM
    }

private fun rewardNeedsManualUse(rewardType: RewardType): Boolean =
    when (rewardType) {
        RewardType.TIME_ADD,
        RewardType.PERIOD_PASS,
        RewardType.EMERGENCY_UNLOCK,
        RewardType.STREAK_SHIELD,
        RewardType.DOUBLE_POINTS_DAY,
        RewardType.CUSTOM
        -> true
    }

private fun rewardCanUseFromInventory(rewardType: RewardType): Boolean =
    when (rewardType) {
        RewardType.TIME_ADD,
        RewardType.PERIOD_PASS,
        RewardType.DOUBLE_POINTS_DAY
        -> true
        RewardType.EMERGENCY_UNLOCK,
        RewardType.STREAK_SHIELD,
        RewardType.CUSTOM
        -> false
    }

private fun inventoryItemTitle(
    reward: RedemptionEntity,
    pendingCount: Int,
): String =
    when (reward.rewardType) {
        RewardType.EMERGENCY_UNLOCK ->
            if (pendingCount > 0) AppText.t("redeem_inventory_emergency_unlock_ready")
            else AppText.t("redeem_inventory_overlay_only")
        RewardType.STREAK_SHIELD ->
            if (pendingCount > 0) AppText.t("redeem_inventory_pending_protection_value", pendingCount)
            else AppText.t("redeem_inventory_waiting_for_review")
        else -> localizedRewardDescription(reward)
    }

private fun rewardLedgerMessageKey(rewardType: RewardType): String =
    when (rewardType) {
        RewardType.TIME_ADD -> "ledger_redeemed_time_add"
        RewardType.PERIOD_PASS -> "ledger_redeemed_period_pass"
        RewardType.EMERGENCY_UNLOCK -> "ledger_redeemed_emergency_unlock"
        RewardType.STREAK_SHIELD -> "ledger_redeemed_streak_shield"
        RewardType.DOUBLE_POINTS_DAY -> "ledger_redeemed_double_points_day"
        RewardType.CUSTOM -> "ledger_redeemed_custom_reward"
    }

class AppLimitRepository(
    private val context: Context,
    private val database: AppDatabase,
) {
    private val groupDao: AppGroupDao = database.appGroupDao()
    private val crossRefDao: CrossRefDao = database.crossRefDao()
    private val redemptionDao: RedemptionDao = database.redemptionDao()
    private val achievementDao: AchievementDao = database.achievementDao()
    private val redemptionHistoryDao: RedemptionHistoryDao = database.redemptionHistoryDao()
    private val pointLedgerDao: PointLedgerDao = database.pointLedgerDao()
    private val dailyArchiveDao: DailyArchiveDao = database.dailyArchiveDao()
    private val dailyGroupArchiveDao: DailyGroupArchiveDao = database.dailyGroupArchiveDao()
    private val rewardInventoryDao: RewardInventoryDao = database.rewardInventoryDao()
    private val activeRewardEffectDao: ActiveRewardEffectDao = database.activeRewardEffectDao()
    private val streakShieldPendingDao: StreakShieldPendingDao = database.streakShieldPendingDao()
    private val rewardUseHistoryDao: RewardUseHistoryDao = database.rewardUseHistoryDao()
    private val bonusTimeDao = database.bonusTimeDao()
    private val preferences = ManagedAppPreferences(context)
    private val usageRepository = UsageStatsUsageRepository(context)
    private val rewardActionMutex = Mutex()
    private val zoneId = ZoneId.systemDefault()

    private val _newAchievementsAction = MutableSharedFlow<AchievementEntity>()
    val newAchievementsAction: SharedFlow<AchievementEntity> = _newAchievementsAction.asSharedFlow()

    fun getAllGroupsWithApps(): Flow<List<AppGroupWithApps>> =
        combine(
            groupDao.getAllGroups(),
            crossRefDao.getAllValidCrossRefs(),
        ) { groups, crossRefs ->
            groups.map { group ->
                AppGroupWithApps(
                    group = group,
                    packageNames =
                        crossRefs
                            .filter { it.groupId == group.id }
                            .map { it.packageName },
                )
            }
        }

    suspend fun createOrUpdateGroup(
        id: String?,
        name: String,
        limitMinutes: Int,
        type: GroupType,
        limitPeriod: LimitPeriod,
        pointsPerMinute: Double,
    ): String =
        withContext(Dispatchers.IO) {
            val groupId = id ?: UUID.randomUUID().toString()
            val existing = if (id != null) groupDao.getGroupByIdSync(id) else null
            val sortOrder = existing?.sortOrder ?: (groupDao.getMaxSortOrder(type) + 1)
            groupDao.insertGroup(
                AppGroupEntity(
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
                ),
            )
            groupId
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

    suspend fun deleteGroup(groupId: String) {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            groupDao.softDeleteGroup(groupId, now)
            crossRefDao.softDeleteAllForGroup(groupId, now)
        }
    }

    suspend fun updateGroupApps(groupId: String, packageNames: List<String>) {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            crossRefDao.softDeleteAllForGroup(groupId, now)
            if (packageNames.isNotEmpty()) {
                crossRefDao.insertCrossRefs(
                    packageNames.map {
                        GroupAppCrossRef(
                            packageName = it,
                            groupId = groupId,
                            updatedAt = now,
                        )
                    },
                )
            }
        }
    }

    fun getAllRewards(): Flow<List<RedemptionEntity>> = redemptionDao.getAllActiveRedemptions()

    fun observeStoreRewardsWithInventory(): Flow<List<RewardStoreItem>> =
        combine(
            redemptionDao.getAllActiveRedemptions(),
            rewardInventoryDao.observeAll(),
            redemptionHistoryDao.getAllHistory(),
        ) { rewards, inventory, history ->
            val ownedMap = inventory.associateBy { it.rewardId }
            val today = LocalDate.now(zoneId)
            val todayHistoryByBuiltinKey =
                history
                    .filter { record ->
                        ArchiveDateUtils.localDateAt(record.redeemedAt, zoneId) == today &&
                            !record.rewardBuiltinKey.isNullOrBlank()
                    }
                    .groupingBy { it.rewardBuiltinKey.orEmpty() }
                    .eachCount()
            rewards.map { reward ->
                RewardStoreItem(
                    reward = reward,
                    ownedQuantity = ownedMap[reward.id]?.quantity ?: 0,
                    isManualUse = rewardNeedsManualUse(reward.rewardType),
                    purchasedTodayCount = reward.builtinKey?.let { todayHistoryByBuiltinKey[it] ?: 0 } ?: 0,
                )
            }
        }

    fun observeInventoryRewards(): Flow<List<InventoryRewardItem>> =
        combine(
            redemptionDao.getAllActiveRedemptions(),
            rewardInventoryDao.observeAll(),
            activeRewardEffectDao.observeAll(),
            streakShieldPendingDao.observePending(),
        ) { rewards, inventory, effects, pending ->
            val now = System.currentTimeMillis()
            val rewardById = rewards.associateBy { it.id }
            inventory.mapNotNull { item ->
                if (item.quantity <= 0) return@mapNotNull null
                val reward = rewardById[item.rewardId] ?: return@mapNotNull null
                val activeCount =
                    effects.count {
                        it.sourceRewardId == reward.id &&
                            it.status == ActiveRewardEffectStatus.ACTIVE &&
                            it.startAt <= now &&
                            it.expireAt > now
                    }
                val pendingCount =
                    if (reward.rewardType == RewardType.STREAK_SHIELD) {
                        pending.count { pendingItem ->
                            pendingItem.status == StreakShieldPendingStatus.PENDING &&
                                pendingItem.shieldTarget == reward.payload().shieldTarget
                        }
                    } else {
                        0
                    }
                InventoryRewardItem(
                    reward = reward.copy(description = inventoryItemTitle(reward, pendingCount)),
                    quantity = item.quantity,
                    activeCount = activeCount,
                    pendingCount = pendingCount,
                )
            }.sortedByDescending { it.reward.updatedAt }
        }

    fun getRedemptionHistory(): Flow<List<RedemptionHistoryEntity>> = redemptionHistoryDao.getAllHistory()

    fun observeRewardUseHistory(): Flow<List<RewardUseHistoryEntity>> = rewardUseHistoryDao.observeAll()

    fun observePendingStreakShields(): Flow<List<PendingStreakShieldItem>> =
        combine(
            streakShieldPendingDao.observePending(),
            rewardInventoryDao.observeAll(),
            redemptionDao.getAllActiveRedemptions(),
        ) { pendingItems, inventoryItems, rewards ->
            val ownedByType =
                inventoryItems.associate { inventory ->
                    val reward = rewards.firstOrNull { it.id == inventory.rewardId }
                    val shieldTarget = reward?.payload()?.shieldTarget
                    shieldTarget to inventory.quantity
                }
            pendingItems.map { pending ->
                PendingStreakShieldItem(
                    pending = pending,
                    title =
                        when (pending.shieldTarget) {
                            StreakShieldTarget.CONTROL_STREAK -> AppText.t("redeem_pending_control_shield")
                            StreakShieldTarget.ENCOURAGE_STREAK -> AppText.t("redeem_pending_encourage_shield")
                        },
                    ownedQuantity = ownedByType[pending.shieldTarget] ?: 0,
                )
            }
        }

    fun getAllAchievements(): Flow<List<AchievementEntity>> = achievementDao.getAllAchievements()

    fun observeAchievementProgress(): Flow<AchievementProgress> =
        combine(
            pointLedgerDao.observeEarnedTotal(),
            pointLedgerDao.observeRewardSpentTotal(),
            dailyArchiveDao.observeAllAsc(),
            dailyGroupArchiveDao.observeAllAsc(),
            streakShieldPendingDao.observeAll(),
        ) { earnedPointsTotal, redeemedPointsTotal, archives, groupArchives, shieldPendings ->
            calculateAchievementProgress(
                earnedPointsTotal = earnedPointsTotal,
                redeemedPointsTotal = redeemedPointsTotal,
                archives = archives,
                groupArchives = groupArchives,
                shieldPendings = shieldPendings,
            )
        }

    suspend fun getAchievementProgress(): AchievementProgress =
        withContext(Dispatchers.IO) {
            calculateAchievementProgress(
                earnedPointsTotal = pointLedgerDao.sumEarnedTotal(),
                redeemedPointsTotal = pointLedgerDao.sumRewardSpentTotal(),
                archives = dailyArchiveDao.getAllAsc(),
                groupArchives = dailyGroupArchiveDao.getAllAsc(),
                shieldPendings = streakShieldPendingDao.getAllSync(),
            )
        }

    suspend fun purchaseReward(rewardId: String): PurchaseRewardResult =
        withContext(Dispatchers.IO) {
            rewardActionMutex.withLock {
                val reward = redemptionDao.getRedemptionById(rewardId) ?: return@withLock PurchaseRewardResult.InvalidReward
                if (!reward.isActive || reward.pointCost <= 0) return@withLock PurchaseRewardResult.InvalidReward
                if (reward.stock == 0) return@withLock PurchaseRewardResult.OutOfStock
                reward.builtinKey?.let { builtinKey ->
                    val (todayStart, tomorrowStart) = currentDayPurchaseWindow()
                    if (redemptionHistoryDao.countBuiltinPurchasesInRange(builtinKey, todayStart, tomorrowStart) >= 1) {
                        return@withLock PurchaseRewardResult.DailyLimitReached
                    }
                }
                if (preferences.userPoints.first() < reward.pointCost) {
                    return@withLock PurchaseRewardResult.InsufficientPoints
                }

                val purchasedAt = System.currentTimeMillis()
                val historyId = UUID.randomUUID().toString()
                val ledgerId = UUID.randomUUID().toString()
                val inventory = rewardInventoryDao.getByRewardId(reward.id)
                val updatedInventory =
                    RewardInventoryEntity(
                        id = inventory?.id ?: "inventory:${reward.id}",
                        rewardId = reward.id,
                        rewardBuiltinKey = reward.builtinKey,
                        quantity = (inventory?.quantity ?: 0) + 1,
                        createdAt = inventory?.createdAt ?: purchasedAt,
                        updatedAt = purchasedAt,
                    )
                database.withTransaction {
                    if (reward.stock > 0) {
                        redemptionDao.insertRedemption(
                            reward.copy(
                                stock = reward.stock - 1,
                                updatedAt = purchasedAt,
                            ),
                        )
                    }
                    rewardInventoryDao.upsert(updatedInventory)
                    insertRewardHistoryAndLedger(
                        reward = reward,
                        historyId = historyId,
                        ledgerId = ledgerId,
                        redeemedAt = purchasedAt,
                        targetGroupName = null,
                    )
                }
                try {
                    preferences.addUserPoints(-reward.pointCost.toDouble())
                } catch (error: Exception) {
                    database.withTransaction {
                        rewardInventoryDao.upsert(inventory ?: updatedInventory.copy(quantity = 0))
                        if (reward.stock > 0) redemptionDao.insertRedemption(reward)
                        redemptionHistoryDao.deleteById(historyId)
                        pointLedgerDao.deleteById(ledgerId)
                    }
                    throw error
                }
                checkAchievements()
                PurchaseRewardResult.Success(
                    rewardTitle = localizedRewardTitle(reward),
                    pointCost = reward.pointCost,
                )
            }
        }

    suspend fun useInventoryReward(
        rewardId: String,
        targetGroupId: String?,
    ): UseRewardResult =
        withContext(Dispatchers.IO) {
            rewardActionMutex.withLock {
                val reward = redemptionDao.getRedemptionById(rewardId) ?: return@withLock UseRewardResult.InvalidReward
                if (!rewardCanUseFromInventory(reward.rewardType)) return@withLock UseRewardResult.InvalidReward
                val inventory = rewardInventoryDao.getByRewardId(rewardId)
                if (inventory == null || inventory.quantity <= 0) return@withLock UseRewardResult.NotOwned
                val payload = reward.payload()
                val now = System.currentTimeMillis()
                val group =
                    targetGroupId?.let(groupDao::getGroupByIdSync)
                        ?: return@withLock UseRewardResult.InvalidTargetGroup

                when (reward.rewardType) {
                    RewardType.TIME_ADD -> {
                        if (group.type != GroupType.CONTROL || payload.minutes <= 0) {
                            return@withLock UseRewardResult.InvalidTargetGroup
                        }
                        createEffectAndConsumeInventory(
                            reward = reward,
                            inventory = inventory,
                            targetGroup = group,
                            payload = payload,
                            createdAt = now,
                            effectType = RewardType.TIME_ADD,
                        )
                        insertRewardUseHistory(
                            reward = reward,
                            targetGroupName = group.name,
                            usedAt = now,
                        )
                        return@withLock UseRewardResult.Success(
                            rewardTitle = localizedRewardTitle(reward),
                            messageKey = "redeem_use_success_time_add",
                            messageArgs = listOf(group.name, payload.minutes),
                        )
                    }
                    RewardType.PERIOD_PASS -> {
                        if (group.type != GroupType.CONTROL) return@withLock UseRewardResult.InvalidTargetGroup
                        val window = currentEffectWindow(now, group.limitPeriod)
                        if (
                            activeRewardEffectDao.getActiveForGroupAndPeriod(
                                groupId = group.id,
                                effectType = RewardType.PERIOD_PASS,
                                periodStartDate = window.startDate,
                                periodEndDate = window.endDate,
                            ) != null
                        ) {
                            return@withLock UseRewardResult.AlreadyActive
                        }
                        createEffectAndConsumeInventory(
                            reward = reward,
                            inventory = inventory,
                            targetGroup = group,
                            payload = payload,
                            createdAt = now,
                            effectType = RewardType.PERIOD_PASS,
                        )
                        insertRewardUseHistory(
                            reward = reward,
                            targetGroupName = group.name,
                            usedAt = now,
                        )
                        return@withLock UseRewardResult.Success(
                            rewardTitle = localizedRewardTitle(reward),
                            messageKey = "redeem_use_success_period_pass",
                            messageArgs = listOf(group.name),
                        )
                    }
                    RewardType.DOUBLE_POINTS_DAY -> {
                        if (group.type != GroupType.ENCOURAGE) return@withLock UseRewardResult.InvalidTargetGroup
                        val window = currentEffectWindow(now, LimitPeriod.DAILY)
                        if (
                            activeRewardEffectDao.getActiveForGroupAndPeriod(
                                groupId = group.id,
                                effectType = RewardType.DOUBLE_POINTS_DAY,
                                periodStartDate = window.startDate,
                                periodEndDate = window.endDate,
                            ) != null
                        ) {
                            return@withLock UseRewardResult.AlreadyActive
                        }
                        if (isEncourageTargetAlreadyReached(group)) {
                            return@withLock UseRewardResult.AlreadyCompleted
                        }
                        createEffectAndConsumeInventory(
                            reward = reward,
                            inventory = inventory,
                            targetGroup = group,
                            payload = payload,
                            createdAt = now,
                            effectType = RewardType.DOUBLE_POINTS_DAY,
                            effectPeriod = LimitPeriod.DAILY,
                        )
                        insertRewardUseHistory(
                            reward = reward,
                            targetGroupName = group.name,
                            usedAt = now,
                        )
                        return@withLock UseRewardResult.Success(
                            rewardTitle = localizedRewardTitle(reward),
                            messageKey = "redeem_use_success_double_points_day",
                            messageArgs = listOf(group.name),
                        )
                    }
                    RewardType.EMERGENCY_UNLOCK,
                    RewardType.STREAK_SHIELD,
                    RewardType.CUSTOM
                    -> return@withLock UseRewardResult.InvalidReward
                }
            }
        }

    suspend fun consumeEmergencyUnlockForBlockedGroup(groupId: String): UseRewardResult =
        withContext(Dispatchers.IO) {
            rewardActionMutex.withLock {
                val group = groupDao.getGroupByIdSync(groupId) ?: return@withLock UseRewardResult.InvalidTargetGroup
                if (group.type != GroupType.CONTROL) return@withLock UseRewardResult.InvalidTargetGroup
                val rewards = redemptionDao.getAllActiveRedemptionsSync()
                val inventoryItems = rewardInventoryDao.getAllSync()
                val ownedUnlockReward =
                    rewards
                        .filter { it.rewardType == RewardType.EMERGENCY_UNLOCK }
                        .sortedBy { it.payload().minutes }
                        .firstOrNull { reward ->
                            (inventoryItems.firstOrNull { it.rewardId == reward.id }?.quantity ?: 0) > 0
                        }
                        ?: return@withLock UseRewardResult.NotOwned
                val inventory = inventoryItems.first { it.rewardId == ownedUnlockReward.id }
                val payload = ownedUnlockReward.payload()
                if (payload.minutes <= 0) return@withLock UseRewardResult.InvalidReward
                val effectId = UUID.randomUUID().toString()
                val now = System.currentTimeMillis()
                database.withTransaction {
                    rewardInventoryDao.upsert(
                        inventory.copy(
                            quantity = inventory.quantity - 1,
                            updatedAt = now,
                        ),
                    )
                    activeRewardEffectDao.upsert(
                        ActiveRewardEffectEntity(
                            id = effectId,
                            effectType = RewardType.EMERGENCY_UNLOCK,
                            sourceRewardId = ownedUnlockReward.id,
                            sourceBuiltinKey = ownedUnlockReward.builtinKey,
                            targetGroupId = group.id,
                            targetGroupType = group.type,
                            startAt = now,
                            expireAt = now + payload.minutes * 60_000L,
                            periodStartDate = ArchiveDateUtils.formatDate(ArchiveDateUtils.localDateAt(now, zoneId)),
                            periodEndDate = ArchiveDateUtils.formatDate(ArchiveDateUtils.localDateAt(now, zoneId)),
                            status = ActiveRewardEffectStatus.ACTIVE,
                            payloadJson = ownedUnlockReward.payloadJson,
                            createdAt = now,
                        ),
                    )
                }
                insertRewardUseHistory(
                    reward = ownedUnlockReward,
                    targetGroupName = group.name,
                    usedAt = now,
                )
                return@withLock UseRewardResult.Success(
                    rewardTitle = localizedRewardTitle(ownedUnlockReward),
                    messageKey = "redeem_use_success_emergency_unlock",
                    messageArgs = listOf(group.name, payload.minutes),
                )
            }
        }

    suspend fun resolvePendingStreakShield(
        pendingId: String,
        useShield: Boolean,
    ): UseRewardResult =
        withContext(Dispatchers.IO) {
            rewardActionMutex.withLock {
                val pending = streakShieldPendingDao.getById(pendingId) ?: return@withLock UseRewardResult.InvalidReward
                if (pending.status != StreakShieldPendingStatus.PENDING) {
                    return@withLock UseRewardResult.InvalidReward
                }
                val now = System.currentTimeMillis()
                if (!useShield) {
                    streakShieldPendingDao.upsert(
                        pending.copy(
                            status = StreakShieldPendingStatus.DISMISSED,
                            resolvedAt = now,
                        ),
                    )
                    return@withLock UseRewardResult.Success(
                        rewardTitle =
                            when (pending.shieldTarget) {
                                StreakShieldTarget.CONTROL_STREAK -> AppText.t("redeem_pending_control_shield")
                                StreakShieldTarget.ENCOURAGE_STREAK -> AppText.t("redeem_pending_encourage_shield")
                            },
                        messageKey = "redeem_pending_dismissed",
                    )
                }
                val rewards = redemptionDao.getAllActiveRedemptionsSync()
                val inventoryItems = rewardInventoryDao.getAllSync()
                val ownedShieldReward =
                    rewards
                        .filter { it.rewardType == RewardType.STREAK_SHIELD }
                        .firstOrNull { reward ->
                            reward.payload().shieldTarget == pending.shieldTarget &&
                                (inventoryItems.firstOrNull { it.rewardId == reward.id }?.quantity ?: 0) > 0
                        }
                        ?: return@withLock UseRewardResult.NotOwned
                val inventory = inventoryItems.first { it.rewardId == ownedShieldReward.id }
                database.withTransaction {
                    rewardInventoryDao.upsert(
                        inventory.copy(
                            quantity = inventory.quantity - 1,
                            updatedAt = now,
                        ),
                    )
                    streakShieldPendingDao.upsert(
                        pending.copy(
                            status = StreakShieldPendingStatus.USED,
                            resolvedAt = now,
                        ),
                    )
                }
                insertRewardUseHistory(
                    reward = ownedShieldReward,
                    targetGroupName = null,
                    usedAt = now,
                )
                checkAchievements()
                return@withLock UseRewardResult.Success(
                    rewardTitle = localizedRewardTitle(ownedShieldReward),
                    messageKey = "redeem_pending_used",
                )
            }
        }

    suspend fun syncBuiltinRewardsV2() {
        withContext(Dispatchers.IO) {
            val activeBuiltinKeys = BUILTIN_REWARD_DEFINITIONS.map { it.builtinKey }
            redemptionDao.deactivateBuiltinRedemptionsExcept(activeBuiltinKeys)
            BUILTIN_REWARD_DEFINITIONS.forEach { definition ->
                upsertBuiltinReward(definition)
            }
        }
    }

    suspend fun syncBuiltinRewards() {
        syncBuiltinRewardsV2()
    }

    suspend fun seedInitialData() {
        withContext(Dispatchers.IO) {
            syncBuiltinRewardsV2()
            syncAchievementDefinitionsInternal()
        }
    }

    suspend fun clearExpiredBonusTime(now: Long) {
        withContext(Dispatchers.IO) {
            bonusTimeDao.clearExpiredBonusTime(now)
        }
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
        if (validation != null) return RewardSaveResult.Invalid(validation)
        if (type != RewardType.CUSTOM || builtinKey != null || bonusMinutes != 0) {
            return RewardSaveResult.Invalid(RewardSaveValidationError.REWARD_NOT_EDITABLE)
        }
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            redemptionDao.insertRedemption(
                RedemptionEntity(
                    id = UUID.randomUUID().toString(),
                    title = title.trim(),
                    description = description.trim(),
                    builtinKey = null,
                    pointCost = cost,
                    rewardType = RewardType.CUSTOM,
                    bonusMinutes = 0,
                    payloadJson = null,
                    isActive = true,
                    stock = stock,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        }
        return RewardSaveResult.Success
    }

    suspend fun updateReward(reward: RedemptionEntity): RewardSaveResult {
        if (reward.builtinKey != null) {
            if (reward.pointCost <= 0) {
                return RewardSaveResult.Invalid(RewardSaveValidationError.POINT_COST_INVALID)
            }
            withContext(Dispatchers.IO) {
                redemptionDao.insertRedemption(
                    reward.copy(
                        pointCost = reward.pointCost,
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
            }
            return RewardSaveResult.Success
        }
        val validation = validateCustomRewardInput(reward.title, reward.pointCost, reward.stock)
        if (validation != null) return RewardSaveResult.Invalid(validation)
        withContext(Dispatchers.IO) {
            redemptionDao.insertRedemption(
                reward.copy(
                    title = reward.title.trim(),
                    description = reward.description.trim(),
                    rewardType = RewardType.CUSTOM,
                    payloadJson = null,
                    bonusMinutes = 0,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }
        return RewardSaveResult.Success
    }

    suspend fun archiveReward(rewardId: String) {
        withContext(Dispatchers.IO) {
            redemptionDao.deactivateRedemption(rewardId)
        }
    }

    suspend fun checkAchievements() {
        checkAchievements(getAchievementProgress())
    }

    internal suspend fun checkAchievements(progress: AchievementProgress) {
        withContext(Dispatchers.IO) {
            val locked = achievementDao.getLockedAchievements()
            val now = System.currentTimeMillis()
            for (achievement in locked) {
                try {
                    val json = JSONObject(achievement.requirement)
                    val type = json.optString("type", "")
                    val value = json.optDouble("value", Double.MAX_VALUE)
                    val shouldUnlock =
                        when (type) {
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
                } catch (_: Exception) {
                }
            }
        }
    }

    suspend fun refreshStreakShieldPending() {
        withContext(Dispatchers.IO) {
            val archives = dailyArchiveDao.getAllAsc()
            val latestArchive = archives.lastOrNull() ?: return@withContext
            val groupArchives = dailyGroupArchiveDao.getAllAsc()
            val rewards = redemptionDao.getAllActiveRedemptionsSync()
            val inventory = rewardInventoryDao.getAllSync()
            syncPendingForTarget(
                latestArchiveDate = latestArchive.archiveDate,
                groupArchives = groupArchives,
                rewards = rewards,
                inventory = inventory,
                target = StreakShieldTarget.CONTROL_STREAK,
                groupType = GroupType.CONTROL,
            )
            syncPendingForTarget(
                latestArchiveDate = latestArchive.archiveDate,
                groupArchives = groupArchives,
                rewards = rewards,
                inventory = inventory,
                target = StreakShieldTarget.ENCOURAGE_STREAK,
                groupType = GroupType.ENCOURAGE,
            )
        }
    }

    private suspend fun syncPendingForTarget(
        latestArchiveDate: String,
        groupArchives: List<DailyGroupArchiveEntity>,
        rewards: List<RedemptionEntity>,
        inventory: List<RewardInventoryEntity>,
        target: StreakShieldTarget,
        groupType: GroupType,
    ) {
        val existing = streakShieldPendingDao.getByArchiveDateAndTarget(latestArchiveDate, target)
        if (existing != null) return
        val archiveDates = groupArchives.mapNotNull { runCatching { LocalDate.parse(it.archiveDate) }.getOrNull() }.distinct().sorted()
        val latestDate = runCatching { LocalDate.parse(latestArchiveDate) }.getOrNull() ?: return
        val completedDates =
            completionDatesForGroupType(
                groupedByDate = groupArchives.groupBy { it.archiveDate },
                groupType = groupType,
            )
        if (latestDate in completedDates) return
        val previousStreak = calculateHistoricalStreakBeforeDate(archiveDates, latestDate, completedDates)
        if (previousStreak <= 0) return
        val shieldReward =
            rewards.firstOrNull {
                it.rewardType == RewardType.STREAK_SHIELD && it.payload().shieldTarget == target
            } ?: return
        val ownedQuantity = inventory.firstOrNull { it.rewardId == shieldReward.id }?.quantity ?: 0
        if (ownedQuantity <= 0) return
        streakShieldPendingDao.upsert(
            StreakShieldPendingEntity(
                id = UUID.randomUUID().toString(),
                archiveDate = latestArchiveDate,
                shieldTarget = target,
                status = StreakShieldPendingStatus.PENDING,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    private fun calculateHistoricalStreakBeforeDate(
        archiveDates: List<LocalDate>,
        latestDate: LocalDate,
        completedDates: Set<LocalDate>,
    ): Int {
        var streak = 0
        var expectedDate = latestDate.minusDays(1)
        for (archiveDate in archiveDates.asReversed()) {
            if (archiveDate >= latestDate) continue
            if (archiveDate != expectedDate) break
            if (archiveDate !in completedDates) break
            streak += 1
            expectedDate = archiveDate.minusDays(1)
        }
        return streak
    }

    private suspend fun isEncourageTargetAlreadyReached(group: AppGroupEntity): Boolean {
        val totalUsage =
            crossRefDao
                .getPackageNamesForGroupSync(group.id)
                .sumOf { usageRepository.getUsageInPeriod(it, group.limitPeriod) }
        return totalUsage >= group.limitMinutes * 60_000L
    }

    private suspend fun createEffectAndConsumeInventory(
        reward: RedemptionEntity,
        inventory: RewardInventoryEntity,
        targetGroup: AppGroupEntity,
        payload: RewardPayload,
        createdAt: Long,
        effectType: RewardType,
        effectPeriod: LimitPeriod = targetGroup.limitPeriod,
    ) {
        val window = currentEffectWindow(createdAt, effectPeriod)
        database.withTransaction {
            rewardInventoryDao.upsert(
                inventory.copy(
                    quantity = inventory.quantity - 1,
                    updatedAt = createdAt,
                ),
            )
            activeRewardEffectDao.upsert(
                ActiveRewardEffectEntity(
                    id = UUID.randomUUID().toString(),
                    effectType = effectType,
                    sourceRewardId = reward.id,
                    sourceBuiltinKey = reward.builtinKey,
                    targetGroupId = targetGroup.id,
                    targetGroupType = targetGroup.type,
                    startAt = createdAt,
                    expireAt = window.expireAt,
                    periodStartDate = window.startDate,
                    periodEndDate = window.endDate,
                    status = ActiveRewardEffectStatus.ACTIVE,
                    payloadJson = reward.payloadJson,
                    createdAt = createdAt,
                ),
            )
        }
    }

    private suspend fun insertRewardUseHistory(
        reward: RedemptionEntity,
        targetGroupName: String?,
        usedAt: Long,
    ) {
        rewardUseHistoryDao.insert(
            RewardUseHistoryEntity(
                id = UUID.randomUUID().toString(),
                rewardId = reward.id,
                rewardTitle = reward.title,
                rewardType = reward.rewardType,
                rewardBuiltinKey = reward.builtinKey,
                targetGroupName = targetGroupName,
                payloadJson = reward.payloadJson,
                usedAt = usedAt,
            ),
        )
    }

    private data class RewardEffectWindow(
        val startDate: String,
        val endDate: String,
        val expireAt: Long,
    )

    private fun currentEffectWindow(
        createdAt: Long,
        period: LimitPeriod,
    ): RewardEffectWindow {
        val date = ArchiveDateUtils.localDateAt(createdAt, zoneId)
        val startDate = ArchiveDateUtils.formatDate(date)
        val endDate =
            when (period) {
                LimitPeriod.DAILY -> date
                LimitPeriod.WEEKLY -> date.plusDays(6)
                LimitPeriod.MONTHLY -> date.withDayOfMonth(date.lengthOfMonth())
            }
        return RewardEffectWindow(
            startDate = startDate,
            endDate = ArchiveDateUtils.formatDate(endDate),
            expireAt = calculateBonusExpiryTime(createdAt, period),
        )
    }

    private fun currentDayPurchaseWindow(
        now: Long = System.currentTimeMillis(),
    ): Pair<Long, Long> {
        val today = ArchiveDateUtils.localDateAt(now, zoneId)
        return ArchiveDateUtils.startOfDayMillis(today, zoneId) to ArchiveDateUtils.nextDayStartMillis(today, zoneId)
    }

    private suspend fun insertRewardHistoryAndLedger(
        reward: RedemptionEntity,
        historyId: String,
        ledgerId: String,
        redeemedAt: Long,
        targetGroupName: String?,
    ) {
        redemptionHistoryDao.insertHistory(
            RedemptionHistoryEntity(
                id = historyId,
                rewardTitle = reward.title,
                pointCost = reward.pointCost,
                historyType = rewardHistoryTypeFor(reward.rewardType),
                bonusMinutes = reward.payload().minutes,
                payloadJson = reward.payloadJson,
                targetGroupName = targetGroupName,
                rewardBuiltinKey = reward.builtinKey,
                redeemedAt = redeemedAt,
            ),
        )
        pointLedgerDao.insert(
            PointLedgerEntity(
                id = ledgerId,
                occurredAt = redeemedAt,
                ledgerDate = ArchiveDateUtils.formatDate(ArchiveDateUtils.localDateAt(redeemedAt, zoneId)),
                entryType = PointLedgerEntryType.REWARD_SPEND,
                deltaPoints = -reward.pointCost.toDouble(),
                rewardId = reward.id,
                rewardTitleSnapshot = reward.title,
                sourceRefId = historyId,
                messageKey = rewardLedgerMessageKey(reward.rewardType),
                messageArgsJson =
                    JSONObject()
                        .put("rewardTitle", reward.title)
                        .put("pointCost", reward.pointCost)
                        .put("targetGroupName", targetGroupName)
                        .put("payload", reward.payloadJson)
                        .toString(),
                createdAt = redeemedAt,
            ),
        )
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
                bonusMinutes = definition.payload.minutes,
                payloadJson = definition.payload.toJson(),
                isActive = true,
                stock = definition.stock,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            ),
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

        seed("BRONZE_POINTS", "Starlet Gatherer", "Earn 1,000 points", """{"type":"points","value":1000}""", AchievementTier.BRONZE, "🌱")
        seed("BRONZE_REDEEM", "First Keepsake", "Spend 1,000 points", """{"type":"redeem_points","value":1000}""", AchievementTier.BRONZE, "🍬")
        seed("BRONZE_CTRL_DAYS", "Grain of Promise", "Complete every control group for 10 total days", """{"type":"control_days","value":10}""", AchievementTier.BRONZE, "🤝")
        seed("BRONZE_CTRL_STREAK", "Three Dawns", "Complete every control group for 3 days in a row", """{"type":"control_streak","value":3}""", AchievementTier.BRONZE, "🪨")
        seed("BRONZE_ENC_DAYS", "Sprout of Sun", "Meet encouragement goals for 10 total days", """{"type":"encourage_days","value":10}""", AchievementTier.BRONZE, "🌻")
        seed("BRONZE_ENC_STREAK", "Threefold Gleam", "Meet encouragement goals for 3 days in a row", """{"type":"encourage_streak","value":3}""", AchievementTier.BRONZE, "🕯️")

        seed("SILVER_POINTS", "Moonlight Seeker", "Earn 3,000 points", """{"type":"points","value":3000}""", AchievementTier.SILVER, "🌊")
        seed("SILVER_REDEEM", "Silver Wish", "Spend 3,000 points", """{"type":"redeem_points","value":3000}""", AchievementTier.SILVER, "🛒")
        seed("SILVER_CTRL_DAYS", "Verdant Stone", "Complete every control group for 30 total days", """{"type":"control_days","value":30}""", AchievementTier.SILVER, "⛰️")
        seed("SILVER_CTRL_STREAK", "Tenfold Dawn", "Complete every control group for 10 days in a row", """{"type":"control_streak","value":10}""", AchievementTier.SILVER, "🌓")
        seed("SILVER_ENC_DAYS", "Dewlit Branch", "Meet encouragement goals for 30 total days", """{"type":"encourage_days","value":30}""", AchievementTier.SILVER, "📖")
        seed("SILVER_ENC_STREAK", "Ten Rays Aligned", "Meet encouragement goals for 10 days in a row", """{"type":"encourage_streak","value":10}""", AchievementTier.SILVER, "🌿")

        seed("GOLD_POINTS", "Solar Chaser", "Earn 10,000 points", """{"type":"points","value":10000}""", AchievementTier.GOLD, "👑")
        seed("GOLD_REDEEM", "Golden Seal", "Spend 10,000 points", """{"type":"redeem_points","value":10000}""", AchievementTier.GOLD, "🎯")
        seed("GOLD_CTRL_DAYS", "Rampart Builder", "Complete every control group for 100 total days", """{"type":"control_days","value":100}""", AchievementTier.GOLD, "🛡️")
        seed("GOLD_CTRL_STREAK", "Balanced Moon", "Complete every control group for 30 days in a row", """{"type":"control_streak","value":30}""", AchievementTier.GOLD, "🌙")
        seed("GOLD_ENC_DAYS", "Field in Bloom", "Meet encouragement goals for 100 total days", """{"type":"encourage_days","value":100}""", AchievementTier.GOLD, "🔥")
        seed("GOLD_ENC_STREAK", "Unfading Moonlight", "Meet encouragement goals for 30 days in a row", """{"type":"encourage_streak","value":30}""", AchievementTier.GOLD, "🎍")

        seed("DIAMOND_POINTS", "River of Stars", "Earn 30,000 points", """{"type":"points","value":30000}""", AchievementTier.DIAMOND, "💫")
        seed("DIAMOND_REDEEM", "Crystal Keybearer", "Spend 30,000 points", """{"type":"redeem_points","value":30000}""", AchievementTier.DIAMOND, "🏛️")
        seed("DIAMOND_CTRL_DAYS", "Crystal Citadel", "Complete every control group for 365 total days", """{"type":"control_days","value":365}""", AchievementTier.DIAMOND, "🏰")
        seed("DIAMOND_CTRL_STREAK", "Hundred Days True", "Complete every control group for 100 days in a row", """{"type":"control_streak","value":100}""", AchievementTier.DIAMOND, "⚡")
        seed("DIAMOND_ENC_DAYS", "Canopy of Green", "Meet encouragement goals for 365 total days", """{"type":"encourage_days","value":365}""", AchievementTier.DIAMOND, "⚔️")
        seed("DIAMOND_ENC_STREAK", "Crown of Hundred Rays", "Meet encouragement goals for 100 days in a row", """{"type":"encourage_streak","value":100}""", AchievementTier.DIAMOND, "🗡️")

        seed("LEGEND_POINTS", "Celestial Ascendant", "Earn 100,000 points", """{"type":"points","value":100000}""", AchievementTier.LEGENDARY, "🐉")
        seed("LEGEND_REDEEM", "Vault of Wonders", "Spend 100,000 points", """{"type":"redeem_points","value":100000}""", AchievementTier.LEGENDARY, "💰")
        seed("LEGEND_CTRL_DAYS", "Eternal Peak", "Complete every control group for 1,000 total days", """{"type":"control_days","value":1000}""", AchievementTier.LEGENDARY, "⭐")
        seed("LEGEND_CTRL_STREAK", "Yearbound Oath", "Complete every control group for 365 days in a row", """{"type":"control_streak","value":365}""", AchievementTier.LEGENDARY, "🔱")
        seed("LEGEND_ENC_DAYS", "Sunlit Courtyard", "Meet encouragement goals for 1,000 total days", """{"type":"encourage_days","value":1000}""", AchievementTier.LEGENDARY, "🌈")
        seed("LEGEND_ENC_STREAK", "Everlasting Radiance", "Meet encouragement goals for 365 days in a row", """{"type":"encourage_streak","value":365}""", AchievementTier.LEGENDARY, "⏳")
    }
}
