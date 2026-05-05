package com.rrrrz.tinyvow.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RedemptionDao {
    @Query("SELECT * FROM redemptions WHERE is_active = 1 ORDER BY created_at DESC")
    fun getAllActiveRedemptions(): Flow<List<RedemptionEntity>>

    @Query("SELECT * FROM redemptions WHERE builtin_key = :builtinKey LIMIT 1")
    suspend fun getRedemptionByBuiltinKey(builtinKey: String): RedemptionEntity?

    @Query("SELECT * FROM redemptions WHERE id = :id LIMIT 1")
    suspend fun getRedemptionById(id: String): RedemptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRedemption(redemption: RedemptionEntity)

    @Query("UPDATE redemptions SET is_active = 0 WHERE id = :id")
    suspend fun deactivateRedemption(id: String)

    @Query("UPDATE redemptions SET is_active = 0 WHERE builtin_key IS NOT NULL AND builtin_key NOT IN (:builtinKeys)")
    suspend fun deactivateBuiltinRedemptionsExcept(builtinKeys: List<String>)
}
