package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardEffectBenefitDao {
    @Query("SELECT * FROM reward_effect_benefits ORDER BY archive_date DESC, created_at DESC")
    fun observeAll(): Flow<List<RewardEffectBenefitEntity>>

    @Query("SELECT * FROM reward_effect_benefits WHERE archive_date = :archiveDate ORDER BY created_at DESC")
    fun observeByDate(archiveDate: String): Flow<List<RewardEffectBenefitEntity>>

    @Query("SELECT * FROM reward_effect_benefits WHERE archive_date = :archiveDate ORDER BY created_at DESC")
    suspend fun getByDateSync(archiveDate: String): List<RewardEffectBenefitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: RewardEffectBenefitEntity)

    @Query("DELETE FROM reward_effect_benefits WHERE archive_date = :archiveDate")
    suspend fun deleteByDate(archiveDate: String)
}
