package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "group_app_cross_ref",
    primaryKeys = ["package_name", "group_id"],
    indices = [
        Index("group_id")
    ]
)
data class GroupAppCrossRef(
    @ColumnInfo(name = "package_name")
    val packageName: String,
    
    @ColumnInfo(name = "group_id")
    val groupId: String,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)
