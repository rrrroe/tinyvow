package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "block_events",
    indices = [
        Index(value = ["event_date"]),
        Index(value = ["group_id", "event_date"]),
        Index(value = ["package_name", "event_date"]),
    ],
)
data class BlockEventEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "event_date")
    val eventDate: String,
    @ColumnInfo(name = "occurred_at")
    val occurredAt: Long,
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "group_id")
    val groupId: String,
    @ColumnInfo(name = "group_name_snapshot")
    val groupNameSnapshot: String,
    @ColumnInfo(name = "exceeded_millis")
    val exceededMillis: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
