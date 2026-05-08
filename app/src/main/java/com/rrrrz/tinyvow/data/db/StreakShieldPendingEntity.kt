package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class StreakShieldTarget {
    CONTROL_STREAK,
    ENCOURAGE_STREAK,
}

enum class StreakShieldPendingStatus {
    PENDING,
    USED,
    DISMISSED,
}

@Entity(
    tableName = "streak_shield_pending",
    indices = [
        Index(value = ["archive_date", "shield_target"], unique = true),
        Index(value = ["status", "created_at"]),
    ],
)
data class StreakShieldPendingEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "archive_date")
    val archiveDate: String,
    @ColumnInfo(name = "shield_target")
    val shieldTarget: StreakShieldTarget,
    val status: StreakShieldPendingStatus = StreakShieldPendingStatus.PENDING,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "resolved_at")
    val resolvedAt: Long? = null,
)
