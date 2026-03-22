package com.rrrrz.tinyvow.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY id ASC")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    @Query("UPDATE achievements SET is_unlocked = 1, unlocked_at = :now WHERE id = :id")
    suspend fun unlockAchievement(id: String, now: Long)

    @Query("SELECT * FROM achievements WHERE is_unlocked = 0")
    suspend fun getLockedAchievements(): List<AchievementEntity>
}
