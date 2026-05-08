package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reward_use_history",
    indices = [
        Index(value = ["used_at"]),
        Index(value = ["reward_id"]),
    ],
)
data class RewardUseHistoryEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "reward_id")
    val rewardId: String,
    @ColumnInfo(name = "reward_title")
    val rewardTitle: String,
    @ColumnInfo(name = "reward_type")
    val rewardType: RewardType,
    @ColumnInfo(name = "reward_builtin_key")
    val rewardBuiltinKey: String? = null,
    @ColumnInfo(name = "target_group_name")
    val targetGroupName: String? = null,
    @ColumnInfo(name = "payload_json")
    val payloadJson: String? = null,
    @ColumnInfo(name = "used_at")
    val usedAt: Long,
)
