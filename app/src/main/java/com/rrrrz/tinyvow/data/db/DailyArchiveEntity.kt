package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_archives",
    indices = [Index(value = ["archive_date"], unique = true)],
)
data class DailyArchiveEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "archive_date")
    val archiveDate: String,
    @ColumnInfo(name = "day_start_at")
    val dayStartAt: Long,
    @ColumnInfo(name = "day_end_at")
    val dayEndAt: Long,
    @ColumnInfo(name = "control_usage_millis")
    val controlUsageMillis: Long = 0L,
    @ColumnInfo(name = "encourage_usage_millis")
    val encourageUsageMillis: Long = 0L,
    @ColumnInfo(name = "total_usage_millis")
    val totalUsageMillis: Long = 0L,
    @ColumnInfo(name = "saved_millis")
    val savedMillis: Long = 0L,
    @ColumnInfo(name = "control_exceeded_group_count")
    val controlExceededGroupCount: Int = 0,
    @ColumnInfo(name = "control_block_event_count")
    val controlBlockEventCount: Int = 0,
    @ColumnInfo(name = "control_completed_group_count")
    val controlCompletedGroupCount: Int = 0,
    @ColumnInfo(name = "encourage_completed_group_count")
    val encourageCompletedGroupCount: Int = 0,
    @ColumnInfo(name = "points_earned")
    val pointsEarned: Double = 0.0,
    @ColumnInfo(name = "points_spent")
    val pointsSpent: Double = 0.0,
    @ColumnInfo(name = "points_net")
    val pointsNet: Double = 0.0,
    @ColumnInfo(name = "redemption_count")
    val redemptionCount: Int = 0,
    @ColumnInfo(name = "activity_control_progress")
    val activityControlProgress: Double = 0.0,
    @ColumnInfo(name = "activity_encourage_progress")
    val activityEncourageProgress: Double = 0.0,
    @ColumnInfo(name = "activity_growth_progress")
    val activityGrowthProgress: Double = 0.0,
    @ColumnInfo(name = "activity_control_available")
    val activityControlAvailable: Boolean = false,
    @ColumnInfo(name = "activity_encourage_available")
    val activityEncourageAvailable: Boolean = false,
    @ColumnInfo(name = "activity_growth_available")
    val activityGrowthAvailable: Boolean = false,
    @ColumnInfo(name = "activity_growth_target_points")
    val activityGrowthTargetPoints: Double = 0.0,
    @ColumnInfo(name = "activity_rings_completed")
    val activityRingsCompleted: Boolean = false,
    @ColumnInfo(name = "archive_version")
    val archiveVersion: Int = 1,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
