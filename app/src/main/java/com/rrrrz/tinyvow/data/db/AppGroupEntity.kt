package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GroupType {
    CONTROL,      // 管控模式：超过时长即阻断
    ENCOURAGE     // 鼓励模式：使用累积积分
}

enum class LimitPeriod {
    DAILY,
    WEEKLY,
    MONTHLY
}

@Entity(tableName = "app_groups")
data class AppGroupEntity(
    @PrimaryKey
    val id: String,
    
    val name: String,
    
    @ColumnInfo(name = "type")
    val type: GroupType = GroupType.CONTROL,
    
    @ColumnInfo(name = "limit_period")
    val limitPeriod: LimitPeriod = LimitPeriod.DAILY,
    
    @ColumnInfo(name = "limit_minutes")
    val limitMinutes: Int,
    
    @ColumnInfo(name = "points_per_minute")
    val pointsPerMinute: Double = 0.0,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    
    @ColumnInfo(name = "last_bonus_at")
    val lastBonusAt: Long = 0,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0
)
