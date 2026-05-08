package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveRewardEffectDao {
    @Query("SELECT * FROM active_reward_effects ORDER BY created_at DESC")
    fun observeAll(): Flow<List<ActiveRewardEffectEntity>>

    @Query("SELECT * FROM active_reward_effects ORDER BY created_at DESC")
    suspend fun getAllSync(): List<ActiveRewardEffectEntity>

    @Query(
        """
        SELECT * FROM active_reward_effects
        WHERE status = 'ACTIVE' AND expire_at > :now
        ORDER BY created_at DESC
        """
    )
    fun observeActive(now: Long): Flow<List<ActiveRewardEffectEntity>>

    @Query(
        """
        SELECT * FROM active_reward_effects
        WHERE target_group_id = :groupId
            AND status = 'ACTIVE'
            AND start_at <= :now
            AND expire_at > :now
        ORDER BY created_at DESC
        """
    )
    suspend fun getActiveForGroup(groupId: String, now: Long): List<ActiveRewardEffectEntity>

    @Query(
        """
        SELECT * FROM active_reward_effects
        WHERE target_group_id = :groupId
            AND effect_type = :effectType
            AND status = 'ACTIVE'
            AND period_start_date = :periodStartDate
            AND period_end_date = :periodEndDate
        LIMIT 1
        """
    )
    suspend fun getActiveForGroupAndPeriod(
        groupId: String,
        effectType: RewardType,
        periodStartDate: String,
        periodEndDate: String,
    ): ActiveRewardEffectEntity?

    @Query(
        """
        SELECT * FROM active_reward_effects
        WHERE target_group_id = :groupId
            AND status = 'ACTIVE'
            AND effect_type IN ('TIME_ADD', 'PERIOD_PASS', 'EMERGENCY_UNLOCK', 'DOUBLE_POINTS_DAY')
            AND period_start_date <= :archiveDate
            AND period_end_date >= :archiveDate
        """
    )
    suspend fun getEffectsForGroupOnDate(groupId: String, archiveDate: String): List<ActiveRewardEffectEntity>

    @Query("SELECT * FROM active_reward_effects WHERE id = :effectId LIMIT 1")
    suspend fun getById(effectId: String): ActiveRewardEffectEntity?

    @Query(
        """
        UPDATE active_reward_effects
        SET status = :status, consumed_at = :consumedAt
        WHERE id = :effectId
        """
    )
    suspend fun updateStatus(effectId: String, status: ActiveRewardEffectStatus, consumedAt: Long?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ActiveRewardEffectEntity)
}
