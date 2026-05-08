package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class RewardType {
    TIME_ADD,
    PERIOD_PASS,
    EMERGENCY_UNLOCK,
    STREAK_SHIELD,
    DOUBLE_POINTS_DAY,
    CUSTOM
}

enum class RedemptionHistoryType {
    TIME_ADD,
    PERIOD_PASS,
    EMERGENCY_UNLOCK,
    STREAK_SHIELD,
    DOUBLE_POINTS_DAY,
    CUSTOM
}

@Entity(
    tableName = "redemptions",
    indices = [
        Index(value = ["builtin_key"], unique = true),
    ],
)
data class RedemptionEntity(
    @PrimaryKey
    val id: String,
    
    val title: String,
    
    val description: String,

    @ColumnInfo(name = "builtin_key")
    val builtinKey: String? = null,
    
    @ColumnInfo(name = "point_cost")
    val pointCost: Int,
    
    @ColumnInfo(name = "reward_type")
    val rewardType: RewardType,
    
    @ColumnInfo(name = "bonus_minutes")
    val bonusMinutes: Int = 0,

    @ColumnInfo(name = "payload_json")
    val payloadJson: String? = null,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "stock")
    val stock: Int = -1, // -1 means infinite
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
