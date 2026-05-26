package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class RewardEffectBenefitType {
    EXTRA_TIME_USED,
    PERIOD_PASS_EXEMPTED,
    DOUBLE_POINTS_EARNED,
    EMERGENCY_UNLOCK_USED,
    STREAK_SHIELD_USED,
}

@Entity(
    tableName = "reward_effect_benefits",
    indices = [
        Index(value = ["effect_id", "archive_date"], unique = true),
        Index(value = ["archive_date"]),
        Index(value = ["reward_id"]),
        Index(value = ["target_group_id", "archive_date"]),
    ],
)
data class RewardEffectBenefitEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "effect_id")
    val effectId: String,
    @ColumnInfo(name = "reward_id")
    val rewardId: String,
    @ColumnInfo(name = "reward_builtin_key")
    val rewardBuiltinKey: String? = null,
    @ColumnInfo(name = "reward_type")
    val rewardType: RewardType,
    @ColumnInfo(name = "archive_date")
    val archiveDate: String,
    @ColumnInfo(name = "target_group_id")
    val targetGroupId: String? = null,
    @ColumnInfo(name = "target_group_name_snapshot")
    val targetGroupNameSnapshot: String? = null,
    @ColumnInfo(name = "benefit_type")
    val benefitType: RewardEffectBenefitType,
    @ColumnInfo(name = "benefit_minutes")
    val benefitMinutes: Int = 0,
    @ColumnInfo(name = "benefit_points")
    val benefitPoints: Double = 0.0,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
