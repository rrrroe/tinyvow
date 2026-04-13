package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RedemptionHistoryDao {
    @Query("SELECT * FROM redemption_history ORDER BY redeemed_at DESC")
    fun getAllHistory(): Flow<List<RedemptionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: RedemptionHistoryEntity)
}
