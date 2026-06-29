package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PointLedgerEntryType {
    USAGE_EARN,
    TARGET_BONUS_EARN,
    OFFLINE_FOCUS,
    REWARD_SPEND,
    MANUAL_ADJUSTMENT,
}

@Entity(
    tableName = "point_ledger",
    indices = [
        Index(value = ["ledger_date"]),
        Index(value = ["occurred_at"]),
        Index(value = ["group_id", "ledger_date"]),
        Index(value = ["source_ref_id"], unique = true),
    ],
)
data class PointLedgerEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "occurred_at")
    val occurredAt: Long,
    @ColumnInfo(name = "ledger_date")
    val ledgerDate: String,
    @ColumnInfo(name = "entry_type")
    val entryType: PointLedgerEntryType,
    @ColumnInfo(name = "delta_points")
    val deltaPoints: Double,
    @ColumnInfo(name = "group_id")
    val groupId: String? = null,
    @ColumnInfo(name = "group_name_snapshot")
    val groupNameSnapshot: String? = null,
    @ColumnInfo(name = "reward_id")
    val rewardId: String? = null,
    @ColumnInfo(name = "reward_title_snapshot")
    val rewardTitleSnapshot: String? = null,
    @ColumnInfo(name = "source_ref_id")
    val sourceRefId: String? = null,
    @ColumnInfo(name = "message_key")
    val messageKey: String? = null,
    @ColumnInfo(name = "message_args_json")
    val messageArgsJson: String? = null,
    val note: String = "",
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
