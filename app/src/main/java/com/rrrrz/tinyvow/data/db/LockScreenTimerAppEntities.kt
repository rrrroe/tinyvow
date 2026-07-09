package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class LockScreenTimerAppStatus {
    UNKNOWN,
    ACTIVE,
    IDLE,
    SCREEN_OFF,
    UNLOCKED,
}

@Entity(tableName = "lock_screen_timer_app_configs")
data class LockScreenTimerAppConfigEntity(
    @PrimaryKey
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "app_label_snapshot")
    val appLabelSnapshot: String,
    val enabled: Boolean = true,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)

@Entity(
    tableName = "lock_screen_timer_app_days",
    primaryKeys = ["package_name", "timer_date"],
    indices = [
        Index(value = ["timer_date"]),
        Index(value = ["package_name"]),
        Index(value = ["package_name", "timer_date"]),
    ],
)
data class LockScreenTimerAppDayEntity(
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "timer_date")
    val timerDate: String,
    @ColumnInfo(name = "trusted_lock_millis")
    val trustedLockMillis: Long = 0L,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = false,
    @ColumnInfo(name = "active_started_at")
    val activeStartedAt: Long? = null,
    @ColumnInfo(name = "last_status")
    val lastStatus: LockScreenTimerAppStatus = LockScreenTimerAppStatus.UNKNOWN,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)

@Entity(
    tableName = "lock_screen_timer_app_segments",
    indices = [
        Index(value = ["package_name"]),
        Index(value = ["timer_date"]),
        Index(value = ["package_name", "start_millis"]),
        Index(value = ["package_name", "end_millis"]),
    ],
)
data class LockScreenTimerAppSegmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "timer_date")
    val timerDate: String,
    @ColumnInfo(name = "start_millis")
    val startMillis: Long,
    @ColumnInfo(name = "end_millis")
    val endMillis: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
