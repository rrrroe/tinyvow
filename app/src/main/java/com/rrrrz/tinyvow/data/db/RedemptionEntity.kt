package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RewardType {
    TIME_PACK,
    CUSTOM
}

@Entity(tableName = "redemptions")
data class RedemptionEntity(
    @PrimaryKey
    val id: String,
    
    val title: String,
    
    val description: String,
    
    @ColumnInfo(name = "point_cost")
    val pointCost: Int,
    
    @ColumnInfo(name = "reward_type")
    val rewardType: RewardType,
    
    @ColumnInfo(name = "bonus_minutes")
    val bonusMinutes: Int = 0, // 仅针对 TIME_PACK
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
