package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_group_archives",
    indices = [
        Index(value = ["archive_date", "group_id"], unique = true),
        Index(value = ["archive_date"]),
        Index(value = ["group_type", "archive_date"]),
    ],
)
data class DailyGroupArchiveEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "archive_date")
    val archiveDate: String,
    @ColumnInfo(name = "group_id")
    val groupId: String,
    @ColumnInfo(name = "group_name")
    val groupName: String,
    @ColumnInfo(name = "group_type")
    val groupType: GroupType,
    @ColumnInfo(name = "limit_period")
    val limitPeriod: LimitPeriod,
    @ColumnInfo(name = "limit_minutes")
    val limitMinutes: Int,
    @ColumnInfo(name = "bonus_minutes")
    val bonusMinutes: Int = 0,
    @ColumnInfo(name = "points_per_minute")
    val pointsPerMinute: Double = 0.0,
    @ColumnInfo(name = "package_count")
    val packageCount: Int = 0,
    @ColumnInfo(name = "daily_usage_millis")
    val dailyUsageMillis: Long = 0L,
    @ColumnInfo(name = "period_usage_millis_at_close")
    val periodUsageMillisAtClose: Long = 0L,
    @ColumnInfo(name = "effective_limit_millis_at_close")
    val effectiveLimitMillisAtClose: Long = 0L,
    @ColumnInfo(name = "remaining_millis_at_close")
    val remainingMillisAtClose: Long = 0L,
    @ColumnInfo(name = "exceeded_millis_at_close")
    val exceededMillisAtClose: Long = 0L,
    @ColumnInfo(name = "earned_points")
    val earnedPoints: Double = 0.0,
    @ColumnInfo(name = "spent_points")
    val spentPoints: Double = 0.0,
    val completed: Boolean = false,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
