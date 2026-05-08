package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ActiveRewardEffectStatus {
    ACTIVE,
    CONSUMED,
    EXPIRED,
}

@Entity(
    tableName = "active_reward_effects",
    indices = [
        Index(value = ["target_group_id", "status", "expire_at"]),
        Index(value = ["effect_type", "status", "expire_at"]),
        Index(value = ["source_reward_id"]),
    ],
)
data class ActiveRewardEffectEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "effect_type")
    val effectType: RewardType,
    @ColumnInfo(name = "source_reward_id")
    val sourceRewardId: String,
    @ColumnInfo(name = "source_builtin_key")
    val sourceBuiltinKey: String? = null,
    @ColumnInfo(name = "target_group_id")
    val targetGroupId: String? = null,
    @ColumnInfo(name = "target_group_type")
    val targetGroupType: GroupType? = null,
    @ColumnInfo(name = "start_at")
    val startAt: Long,
    @ColumnInfo(name = "expire_at")
    val expireAt: Long,
    @ColumnInfo(name = "period_start_date")
    val periodStartDate: String? = null,
    @ColumnInfo(name = "period_end_date")
    val periodEndDate: String? = null,
    val status: ActiveRewardEffectStatus = ActiveRewardEffectStatus.ACTIVE,
    @ColumnInfo(name = "payload_json")
    val payloadJson: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "consumed_at")
    val consumedAt: Long? = null,
)
