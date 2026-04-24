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

    @Query("SELECT COUNT(*) FROM redemption_history WHERE redeemed_at >= :startInclusive AND redeemed_at < :endExclusive")
    suspend fun countInRange(startInclusive: Long, endExclusive: Long): Int

    @Query("DELETE FROM redemption_history WHERE id = :historyId")
    suspend fun deleteById(historyId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: RedemptionHistoryEntity)
}
