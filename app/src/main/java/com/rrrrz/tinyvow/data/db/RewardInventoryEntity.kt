package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reward_inventory",
    indices = [
        Index(value = ["reward_id"], unique = true),
        Index(value = ["reward_builtin_key"], unique = true),
    ],
)
data class RewardInventoryEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "reward_id")
    val rewardId: String,
    @ColumnInfo(name = "reward_builtin_key")
    val rewardBuiltinKey: String? = null,
    val quantity: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
