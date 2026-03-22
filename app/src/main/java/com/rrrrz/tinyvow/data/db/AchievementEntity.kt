package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String, // 预置的 Key，如 "LIFETIME_POINTS_1000"
    
    val title: String,
    
    val description: String,
    
    val requirement: String, // JSON 格式记录条件，如 {"type": "points", "value": 1000}
    
    @ColumnInfo(name = "is_unlocked")
    val isUnlocked: Boolean = false,
    
    @ColumnInfo(name = "unlocked_at")
    val unlockedAt: Long? = null
)
