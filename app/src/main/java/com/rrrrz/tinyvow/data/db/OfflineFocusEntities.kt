package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class OfflineFocusSessionStatus {
    RUNNING,
    PAUSED,
    COMPLETED,
    ABANDONED,
    SETTLED,
}

enum class OfflineFocusMode {
    NORMAL,
    STRICT,
}

enum class OfflineFocusPauseReason {
    USER,
    LOCK_SCREEN,
    NON_WHITELIST_APP,
}

enum class OfflineFocusAbandonReason {
    USER,
    STRICT_VIOLATION,
    BELOW_THRESHOLD,
}

@Entity(
    tableName = "offline_focus_categories",
    indices = [
        Index(value = ["sort_order"]),
        Index(value = ["is_archived", "sort_order"]),
    ],
)
data class OfflineFocusCategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    @ColumnInfo(name = "icon_key")
    val iconKey: String,
    @ColumnInfo(name = "custom_icon_path")
    val customIconPath: String? = null,
    @ColumnInfo(name = "color_argb")
    val colorArgb: Int,
    @ColumnInfo(name = "points_per_minute")
    val pointsPerMinute: Double = 1.0,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
    @ColumnInfo(name = "is_built_in")
    val isBuiltIn: Boolean,
    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)

@Entity(
    tableName = "offline_focus_sessions",
    indices = [
        Index(value = ["category_id"]),
        Index(value = ["started_at"]),
        Index(value = ["completed_at"]),
        Index(value = ["status", "started_at"]),
        Index(value = ["settled_ledger_id"], unique = true),
    ],
)
data class OfflineFocusSessionEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "category_id")
    val categoryId: String,
    @ColumnInfo(name = "category_name_snapshot")
    val categoryNameSnapshot: String,
    @ColumnInfo(name = "category_icon_key_snapshot")
    val categoryIconKeySnapshot: String,
    @ColumnInfo(name = "category_custom_icon_path_snapshot")
    val categoryCustomIconPathSnapshot: String? = null,
    @ColumnInfo(name = "category_color_argb_snapshot")
    val categoryColorArgbSnapshot: Int,
    @ColumnInfo(name = "points_per_minute_snapshot")
    val pointsPerMinuteSnapshot: Double = 1.0,
    @ColumnInfo(name = "planned_duration_millis")
    val plannedDurationMillis: Long,
    @ColumnInfo(name = "actual_duration_millis")
    val actualDurationMillis: Long,
    val status: OfflineFocusSessionStatus,
    @ColumnInfo(name = "focus_mode")
    val focusMode: OfflineFocusMode = OfflineFocusMode.NORMAL,
    @ColumnInfo(name = "started_at")
    val startedAt: Long,
    @ColumnInfo(name = "paused_at")
    val pausedAt: Long? = null,
    @ColumnInfo(name = "resumed_at")
    val resumedAt: Long? = null,
    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,
    @ColumnInfo(name = "abandoned_at")
    val abandonedAt: Long? = null,
    @ColumnInfo(name = "pause_reason")
    val pauseReason: OfflineFocusPauseReason? = null,
    @ColumnInfo(name = "abandoned_reason")
    val abandonedReason: OfflineFocusAbandonReason? = null,
    @ColumnInfo(name = "violation_started_at")
    val violationStartedAt: Long? = null,
    @ColumnInfo(name = "violation_package_name")
    val violationPackageName: String? = null,
    @ColumnInfo(name = "points_awarded")
    val pointsAwarded: Double,
    @ColumnInfo(name = "settled_ledger_id")
    val settledLedgerId: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
