package com.rrrrz.tinyvow.data.repository

import android.content.Context
import com.rrrrz.tinyvow.data.db.AppDatabase
import com.rrrrz.tinyvow.data.db.AppGroupEntity
import com.rrrrz.tinyvow.data.db.PointLedgerEntity
import com.rrrrz.tinyvow.data.db.PointLedgerEntryType
import com.rrrrz.tinyvow.data.db.RedemptionEntity
import com.rrrrz.tinyvow.data.settings.ManagedAppPreferences
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PointsRepository(
    private val context: Context,
    private val database: AppDatabase,
) {
    private val preferences = ManagedAppPreferences(context)
    private val pointLedgerDao = database.pointLedgerDao()
    private val zoneId = ZoneId.systemDefault()

    suspend fun applyBalanceDelta(deltaPoints: Double) {
        if (deltaPoints == 0.0) return
        withContext(Dispatchers.IO) {
            preferences.addUserPoints(deltaPoints)
        }
    }

    suspend fun recordUsageEarn(
        group: AppGroupEntity,
        deltaPoints: Double,
        sourcePackageName: String,
        usageDurationMillis: Long,
        occurredAt: Long = System.currentTimeMillis(),
    ) {
        record(
            deltaPoints = deltaPoints,
            entryType = PointLedgerEntryType.USAGE_EARN,
            occurredAt = occurredAt,
            group = group,
            sourcePackageName = sourcePackageName,
            usageDurationMillis = usageDurationMillis,
        )
    }

    suspend fun recordTargetBonusEarn(
        group: AppGroupEntity,
        deltaPoints: Double,
        occurredAt: Long = System.currentTimeMillis(),
    ) {
        record(
            deltaPoints = deltaPoints,
            entryType = PointLedgerEntryType.TARGET_BONUS_EARN,
            occurredAt = occurredAt,
            group = group,
        )
    }

    suspend fun recordRewardSpend(
        reward: RedemptionEntity,
        sourceRefId: String,
        occurredAt: Long = System.currentTimeMillis(),
    ) {
        record(
            deltaPoints = -reward.pointCost.toDouble(),
            entryType = PointLedgerEntryType.REWARD_SPEND,
            occurredAt = occurredAt,
            reward = reward,
            sourceRefId = sourceRefId,
        )
    }

    suspend fun recordManualAdjustment(
        deltaPoints: Double,
        note: String,
        occurredAt: Long = System.currentTimeMillis(),
    ) {
        record(
            deltaPoints = deltaPoints,
            entryType = PointLedgerEntryType.MANUAL_ADJUSTMENT,
            occurredAt = occurredAt,
            note = note,
        )
    }

    suspend fun record(
        deltaPoints: Double,
        entryType: PointLedgerEntryType,
        occurredAt: Long = System.currentTimeMillis(),
        group: AppGroupEntity? = null,
        reward: RedemptionEntity? = null,
        rewardTitle: String? = null,
        sourceRefId: String? = null,
        sourcePackageName: String? = null,
        usageDurationMillis: Long? = null,
        note: String = "",
    ) {
        if (deltaPoints == 0.0) return

        withContext(Dispatchers.IO) {
            val dayStartHour = preferences.getDayBoundaryHourOnce()
            val ledgerDate =
                ArchiveDateUtils.formatDate(
                    ArchiveDateUtils.localDateAt(occurredAt, zoneId, dayStartHour),
                )
            pointLedgerDao.insert(
                PointLedgerEntity(
                    id = UUID.randomUUID().toString(),
                    occurredAt = occurredAt,
                    ledgerDate = ledgerDate,
                    entryType = entryType,
                    deltaPoints = deltaPoints,
                    groupId = group?.id,
                    groupNameSnapshot = group?.name,
                    rewardId = reward?.id,
                    rewardTitleSnapshot = rewardTitle ?: reward?.title,
                    sourceRefId = sourceRefId,
                    sourcePackageName = sourcePackageName,
                    usageDurationMillis = usageDurationMillis,
                    note = note,
                    createdAt = System.currentTimeMillis(),
                )
            )
            applyBalanceDelta(deltaPoints)
            AppLimitRepository(context, database).checkAchievements()
        }
    }
}
