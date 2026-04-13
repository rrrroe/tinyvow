package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "redemption_history")
data class RedemptionHistoryEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "reward_title")
    val rewardTitle: String,

    @ColumnInfo(name = "point_cost")
    val pointCost: Int,

    @ColumnInfo(name = "history_type")
    val historyType: RedemptionHistoryType,

    @ColumnInfo(name = "bonus_minutes")
    val bonusMinutes: Int = 0,

    @ColumnInfo(name = "target_group_name")
    val targetGroupName: String? = null,

    @ColumnInfo(name = "redeemed_at")
    val redeemedAt: Long
)
