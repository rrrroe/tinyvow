package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ProtectionEventType {
    SUPER_MODE_CONFIGURED,
    SUPER_MODE_ENABLED,
    SUPER_MODE_DISABLED,
    SUPER_MODE_CLEARED,
    SUPER_MODE_WINDOW_CHANGED,
    SUPER_MODE_CREDENTIALS_CHANGED,
    GROUP_UPDATED,
    GROUP_DELETED,
    TIME_REWARD_PURCHASED,
    GUARDED_ACTION_BLOCKED_OUTSIDE_WINDOW,
}

@Entity(
    tableName = "protection_events",
    indices = [
        Index(value = ["event_date"]),
        Index(value = ["event_type", "event_date"]),
        Index(value = ["occurred_at"]),
        Index(value = ["target_id"]),
    ],
)
data class ProtectionEventEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "event_type")
    val eventType: ProtectionEventType,
    @ColumnInfo(name = "event_date")
    val eventDate: String,
    @ColumnInfo(name = "occurred_at")
    val occurredAt: Long,
    @ColumnInfo(name = "title_key")
    val titleKey: String,
    @ColumnInfo(name = "message_key")
    val messageKey: String,
    @ColumnInfo(name = "message_args_json")
    val messageArgsJson: String? = null,
    @ColumnInfo(name = "target_id")
    val targetId: String? = null,
    @ColumnInfo(name = "target_label")
    val targetLabel: String? = null,
    @ColumnInfo(name = "before_json")
    val beforeJson: String? = null,
    @ColumnInfo(name = "after_json")
    val afterJson: String? = null,
    @ColumnInfo(name = "within_window")
    val withinWindow: Boolean? = null,
    @ColumnInfo(name = "protection_enabled")
    val protectionEnabled: Boolean,
)
