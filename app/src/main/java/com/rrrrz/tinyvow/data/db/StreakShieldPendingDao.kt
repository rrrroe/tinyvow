package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakShieldPendingDao {
    @Query("SELECT * FROM streak_shield_pending WHERE status = 'PENDING' ORDER BY created_at DESC")
    fun observePending(): Flow<List<StreakShieldPendingEntity>>

    @Query("SELECT * FROM streak_shield_pending ORDER BY created_at DESC")
    fun observeAll(): Flow<List<StreakShieldPendingEntity>>

    @Query("SELECT * FROM streak_shield_pending ORDER BY created_at DESC")
    suspend fun getAllSync(): List<StreakShieldPendingEntity>

    @Query(
        """
        SELECT * FROM streak_shield_pending
        WHERE archive_date = :archiveDate AND shield_target = :shieldTarget
        LIMIT 1
        """
    )
    suspend fun getByArchiveDateAndTarget(
        archiveDate: String,
        shieldTarget: StreakShieldTarget,
    ): StreakShieldPendingEntity?

    @Query("SELECT * FROM streak_shield_pending WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): StreakShieldPendingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: StreakShieldPendingEntity)
}
