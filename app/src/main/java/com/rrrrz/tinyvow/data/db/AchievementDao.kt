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

    @Query(
        """
        UPDATE achievements
        SET title = :title,
            description = :description,
            requirement = :requirement,
            tier = :tier,
            icon_emoji = :iconEmoji
        WHERE id = :id
        """
    )
    suspend fun updateAchievementDefinition(
        id: String,
        title: String,
        description: String,
        requirement: String,
        tier: Int,
        iconEmoji: String,
    ): Int

    /** 刷新内置成就定义，同时保留用户已解锁状态和解锁时间 */
    @Transaction
    suspend fun upsertAchievementDefinition(achievement: AchievementEntity) {
        val updated = updateAchievementDefinition(
            id = achievement.id,
            title = achievement.title,
            description = achievement.description,
            requirement = achievement.requirement,
            tier = achievement.tier,
            iconEmoji = achievement.iconEmoji,
        )
        if (updated == 0) {
            seedAchievement(achievement)
        }
    }

    @Query("UPDATE achievements SET is_unlocked = 1, unlocked_at = :now WHERE id = :id")
    suspend fun unlockAchievement(id: String, now: Long): Int

    @Query("SELECT * FROM achievements WHERE is_unlocked = 0")
    suspend fun getLockedAchievements(): List<AchievementEntity>
}
