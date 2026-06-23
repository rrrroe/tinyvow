package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_checkins",
    indices = [
        Index(value = ["checkin_date"], unique = true),
        Index(value = ["checked_in_at"]),
    ],
)
data class DailyCheckInEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "checkin_date")
    val checkInDate: String,
    @ColumnInfo(name = "checked_in_at")
    val checkedInAt: Long,
    @ColumnInfo(name = "reward_builtin_key")
    val rewardBuiltinKey: String,
    @ColumnInfo(name = "reward_inventory_id")
    val rewardInventoryId: String,
)
