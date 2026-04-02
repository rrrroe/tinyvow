package com.rrrrz.tinyvow.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "redemption_history")
data class RedemptionHistoryEntity(
    @PrimaryKey
    val id: String,
    val rewardTitle: String,
    val pointCost: Int,
    val redeemedAt: Long
)
