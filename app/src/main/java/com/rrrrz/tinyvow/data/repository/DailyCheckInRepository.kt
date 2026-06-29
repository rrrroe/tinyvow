package com.rrrrz.tinyvow.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.db.DailyArchiveEntity
import com.rrrrz.tinyvow.data.db.DailyCheckInEntity
import com.rrrrz.tinyvow.data.db.DailyGroupArchiveEntity
import com.rrrrz.tinyvow.data.db.RedemptionEntity
import com.rrrrz.tinyvow.data.db.RewardInventoryEntity
import com.rrrrz.tinyvow.data.time.BusinessDay
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

data class DailyCheckInTodayState(
    val date: LocalDate,
    val checkedIn: Boolean,
    val bufferCardCount: Int,
)

data class DailyCheckInMonthState(
    val month: YearMonth,
    val days: List<DailyCheckInDayState>,
    val checkedInDays: Int,
    val bufferCardCount: Int,
    val allControlKeptDays: Int,
    val encourageCompletedDays: Int,
)

data class DailyCheckInDayState(
    val date: LocalDate,
    val checkedIn: Boolean,
    val allControlKept: Boolean,
    val encourageCompleted: Boolean,
    val hasArchivedSignals: Boolean,
    val isToday: Boolean,
    val activityControlProgress: Float = 0f,
    val activityEncourageProgress: Float = 0f,
    val activityGrowthProgress: Float = 0f,
    val activityControlAvailable: Boolean = false,
    val activityEncourageAvailable: Boolean = false,
    val activityGrowthAvailable: Boolean = false,
    val activityRingsCompleted: Boolean = false,
)

sealed interface DailyCheckInResult {
    data class Success(val bufferCardCount: Int) : DailyCheckInResult

    data object AlreadyCheckedIn : DailyCheckInResult

    data object RewardUnavailable : DailyCheckInResult
}

class DailyCheckInRepository(
    context: Context,
    private val database: AppDatabase = AppDatabase.getDatabase(context),
) {
    private val zoneId = ZoneId.systemDefault()
    private val checkInDao = database.dailyCheckInDao()
    private val rewardInventoryDao = database.rewardInventoryDao()
    private val redemptionDao = database.redemptionDao()
    private val dailyArchiveDao = database.dailyArchiveDao()
    private val dailyGroupArchiveDao = database.dailyGroupArchiveDao()

    fun observeTodayState(): Flow<DailyCheckInTodayState> {
        val today = BusinessDay.today(zoneId, BusinessDay.cachedStartHour())
        val todayKey = today.toString()
        return combine(
            checkInDao.observeByDate(todayKey),
            rewardInventoryDao.observeAll(),
        ) { checkIn, inventory ->
            DailyCheckInTodayState(
                date = today,
                checkedIn = checkIn != null,
                bufferCardCount = inventory.sumBufferCards(),
            )
        }
    }

    fun observeMonth(month: YearMonth): Flow<DailyCheckInMonthState> {
        val from = month.atDay(1)
        val to = month.atEndOfMonth()
        return combine(
            checkInDao.observeByDateRange(from.toString(), to.toString()),
            dailyArchiveDao.getByDateRange(from.toString(), to.toString()),
            dailyGroupArchiveDao.getByDateRange(from.toString(), to.toString()),
            rewardInventoryDao.observeAll(),
        ) { checkIns, archives, groupArchives, inventory ->
            buildDailyCheckInMonthState(
                month = month,
                today = BusinessDay.today(zoneId, BusinessDay.cachedStartHour()),
                checkIns = checkIns,
                archives = archives,
                groupArchives = groupArchives,
                bufferCardCount = inventory.sumBufferCards(),
            )
        }
    }

    suspend fun checkInToday(): DailyCheckInResult =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val todayKey = ArchiveDateUtils.localDateAt(now, zoneId, BusinessDay.cachedStartHour()).toString()
            if (checkInDao.getByDate(todayKey) != null) {
                return@withContext DailyCheckInResult.AlreadyCheckedIn
            }
            val reward = ensureEmergencyUnlockReward() ?: return@withContext DailyCheckInResult.RewardUnavailable
            val existingInventory = rewardInventoryDao.getByRewardId(reward.id)
            val inventoryId = existingInventory?.id ?: "inventory:${reward.id}"
            val updatedInventory =
                RewardInventoryEntity(
                    id = inventoryId,
                    rewardId = reward.id,
                    rewardBuiltinKey = reward.builtinKey,
                    quantity = (existingInventory?.quantity ?: 0) + 1,
                    createdAt = existingInventory?.createdAt ?: now,
                    updatedAt = now,
                )
            var inserted = false
            database.withTransaction {
                if (checkInDao.getByDate(todayKey) == null) {
                    rewardInventoryDao.upsert(updatedInventory)
                    checkInDao.insert(
                        DailyCheckInEntity(
                            id = UUID.randomUUID().toString(),
                            checkInDate = todayKey,
                            checkedInAt = now,
                            rewardBuiltinKey = EMERGENCY_UNLOCK_REWARD_KEY,
                            rewardInventoryId = inventoryId,
                        ),
                    )
                    inserted = true
                }
            }
            if (inserted) {
                DailyCheckInResult.Success(updatedInventory.quantity)
            } else {
                DailyCheckInResult.AlreadyCheckedIn
            }
        }

    private suspend fun ensureEmergencyUnlockReward(): RedemptionEntity? {
        val existing = redemptionDao.getRedemptionByBuiltinKey(EMERGENCY_UNLOCK_REWARD_KEY)
        if (existing?.isActive == true) return existing
        BUILTIN_REWARD_DEFINITIONS
            .firstOrNull { it.builtinKey == EMERGENCY_UNLOCK_REWARD_KEY }
            ?.let { definition ->
                val now = System.currentTimeMillis()
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
        return redemptionDao.getRedemptionByBuiltinKey(EMERGENCY_UNLOCK_REWARD_KEY)?.takeIf { it.isActive }
    }

    private fun List<RewardInventoryEntity>.sumBufferCards(): Int =
        filter { it.rewardBuiltinKey == EMERGENCY_UNLOCK_REWARD_KEY }.sumOf { it.quantity.coerceAtLeast(0) }
}

internal fun buildDailyCheckInMonthState(
    month: YearMonth,
    today: LocalDate,
    checkIns: List<DailyCheckInEntity>,
    archives: List<DailyArchiveEntity>,
    groupArchives: List<DailyGroupArchiveEntity>,
    bufferCardCount: Int,
): DailyCheckInMonthState {
    val checkInDates = checkIns.mapTo(mutableSetOf()) { it.checkInDate }
    val archivesByDate = archives.associateBy { it.archiveDate }
    val groupByDate = groupArchives.groupBy { it.archiveDate }
    val days =
        (1..month.atEndOfMonth().dayOfMonth).map { day ->
            val date = month.atDay(day)
            val dateKey = date.toString()
            val archive = archivesByDate[dateKey]
            val groups = groupByDate[dateKey].orEmpty()
            val showArchivedBadges = date.isBefore(today)
            val activityRings =
                if (showArchivedBadges) {
                    archive?.toActivityRingProgressSnapshot()
                        ?: buildActivityRingProgressSnapshot(
                            groupSnapshots = groups,
                            pointsEarned = archive?.pointsEarned ?: groups.sumOf { it.earnedPoints },
                        )
                } else {
                    ActivityRingProgressSnapshot(
                        controlProgress = 0.0,
                        encourageProgress = 0.0,
                        growthProgress = 0.0,
                        controlAvailable = false,
                        encourageAvailable = false,
                        growthAvailable = false,
                        growthTargetPoints = 0.0,
                    )
                }
            DailyCheckInDayState(
                date = date,
                checkedIn = dateKey in checkInDates,
                allControlKept = showArchivedBadges && activityRings.controlAvailable && activityRings.controlProgress >= 1.0,
                encourageCompleted =
                    showArchivedBadges && activityRings.encourageAvailable && activityRings.encourageProgress >= 1.0,
                hasArchivedSignals = showArchivedBadges && (archive != null || groups.isNotEmpty()),
                isToday = date == today,
                activityControlProgress = activityRings.controlProgress.toFloat(),
                activityEncourageProgress = activityRings.encourageProgress.toFloat(),
                activityGrowthProgress = activityRings.growthProgress.toFloat(),
                activityControlAvailable = activityRings.controlAvailable,
                activityEncourageAvailable = activityRings.encourageAvailable,
                activityGrowthAvailable = activityRings.growthAvailable,
                activityRingsCompleted = showArchivedBadges && activityRings.ringsCompleted,
            )
        }
    return DailyCheckInMonthState(
        month = month,
        days = days,
        checkedInDays = days.count { it.checkedIn },
        bufferCardCount = bufferCardCount,
        allControlKeptDays = days.count { it.allControlKept },
        encourageCompletedDays = days.count { it.encourageCompleted },
    )
}

private fun DailyArchiveEntity.toActivityRingProgressSnapshot(): ActivityRingProgressSnapshot? {
    val hasStoredActivityRings =
        activityControlAvailable ||
            activityEncourageAvailable ||
            activityGrowthAvailable ||
            activityGrowthTargetPoints > 0.0
    if (!hasStoredActivityRings) return null
    return ActivityRingProgressSnapshot(
        controlProgress = activityControlProgress,
        encourageProgress = activityEncourageProgress,
        growthProgress = activityGrowthProgress,
        controlAvailable = activityControlAvailable,
        encourageAvailable = activityEncourageAvailable,
        growthAvailable = activityGrowthAvailable,
        growthTargetPoints = activityGrowthTargetPoints,
    )
}
