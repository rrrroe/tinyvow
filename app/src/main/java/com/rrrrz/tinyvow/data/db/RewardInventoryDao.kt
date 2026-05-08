package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardInventoryDao {
    @Query("SELECT * FROM reward_inventory ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<RewardInventoryEntity>>

    @Query("SELECT * FROM reward_inventory ORDER BY updated_at DESC")
    suspend fun getAllSync(): List<RewardInventoryEntity>

    @Query("SELECT * FROM reward_inventory WHERE reward_id = :rewardId LIMIT 1")
    suspend fun getByRewardId(rewardId: String): RewardInventoryEntity?

    @Query("SELECT * FROM reward_inventory WHERE reward_id = :rewardId LIMIT 1")
    fun observeByRewardId(rewardId: String): Flow<RewardInventoryEntity?>

    @Query(
        """
        SELECT COALESCE(SUM(quantity), 0)
        FROM reward_inventory
        WHERE reward_id IN (
            SELECT id FROM redemptions WHERE reward_type = :rewardType AND is_active = 1
        )
        """
    )
    suspend fun sumQuantityByRewardType(rewardType: RewardType): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: RewardInventoryEntity)
}
