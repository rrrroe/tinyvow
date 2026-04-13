package com.rrrrz.tinyvow.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY tier ASC, id ASC")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    /** 种子数据专用：已存在则忽略，不覆盖已解锁状态 */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun seedAchievement(achievement: AchievementEntity)

    @Query("UPDATE achievements SET is_unlocked = 1, unlocked_at = :now WHERE id = :id")
    suspend fun unlockAchievement(id: String, now: Long)

    @Query("SELECT * FROM achievements WHERE is_unlocked = 0")
    suspend fun getLockedAchievements(): List<AchievementEntity>
}
