package com.rrrrz.tinyvow.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BonusTimeDao {
    @Query("SELECT * FROM bonus_times WHERE target_group_id = :groupId AND expiry_time > :now")
    fun getActiveBonusTimeForGroup(groupId: String, now: Long): Flow<List<BonusTimeEntity>>

    @Query("SELECT * FROM bonus_times WHERE target_group_id = :groupId AND expiry_time > :now")
    fun getActiveBonusTimeForGroupSync(groupId: String, now: Long): List<BonusTimeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBonusTime(bonusTime: BonusTimeEntity)

    @Query("DELETE FROM bonus_times WHERE expiry_time <= :now")
    suspend fun clearExpiredBonusTime(now: Long)
}
