package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bonus_times")
data class BonusTimeEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "target_group_id")
    val targetGroupId: String,
    
    @ColumnInfo(name = "extra_minutes")
    val extraMinutes: Int,
    
    @ColumnInfo(name = "expiry_time")
    val expiryTime: Long, // 加时包失效时间，通常是当日 23:59:59
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
