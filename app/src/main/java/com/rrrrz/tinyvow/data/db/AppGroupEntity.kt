package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_groups")
data class AppGroupEntity(
    @PrimaryKey
    val id: String,
    
    val name: String,
    
    @ColumnInfo(name = "daily_limit_minutes")
    val dailyLimitMinutes: Int,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)
