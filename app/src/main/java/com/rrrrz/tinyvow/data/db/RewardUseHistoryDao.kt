package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardUseHistoryDao {
    @Query("SELECT * FROM reward_use_history ORDER BY used_at DESC")
    fun observeAll(): Flow<List<RewardUseHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RewardUseHistoryEntity)
}
