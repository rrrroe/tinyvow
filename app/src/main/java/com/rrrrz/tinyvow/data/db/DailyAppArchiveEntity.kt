package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_app_archives",
    indices = [
        Index(value = ["archive_date", "package_name", "scope_key"], unique = true),
        Index(value = ["archive_date"]),
        Index(value = ["group_id", "archive_date"]),
        Index(value = ["package_name", "archive_date"]),
        Index(value = ["is_grouped", "archive_date"]),
    ],
)
data class DailyAppArchiveEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "archive_date")
    val archiveDate: String,
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "app_label")
    val appLabel: String,
    @ColumnInfo(name = "scope_key")
    val scopeKey: String,
    @ColumnInfo(name = "is_grouped")
    val isGrouped: Boolean = true,
    @ColumnInfo(name = "group_id")
    val groupId: String?,
    @ColumnInfo(name = "group_name")
    val groupName: String?,
    @ColumnInfo(name = "group_type")
    val groupType: GroupType?,
    @ColumnInfo(name = "limit_period")
    val limitPeriod: LimitPeriod?,
    @ColumnInfo(name = "daily_usage_millis")
    val dailyUsageMillis: Long = 0L,
    @ColumnInfo(name = "open_count")
    val openCount: Int = 0,
    @ColumnInfo(name = "session_count")
    val sessionCount: Int = 0,
    @ColumnInfo(name = "longest_session_millis")
    val longestSessionMillis: Long = 0L,
    @ColumnInfo(name = "night_usage_millis")
    val nightUsageMillis: Long = 0L,
    @ColumnInfo(name = "earned_points")
    val earnedPoints: Double = 0.0,
    val completed: Boolean = false,
    @ColumnInfo(name = "hour_00_millis")
    val hour00Millis: Long = 0L,
    @ColumnInfo(name = "hour_01_millis")
    val hour01Millis: Long = 0L,
    @ColumnInfo(name = "hour_02_millis")
    val hour02Millis: Long = 0L,
    @ColumnInfo(name = "hour_03_millis")
    val hour03Millis: Long = 0L,
    @ColumnInfo(name = "hour_04_millis")
    val hour04Millis: Long = 0L,
    @ColumnInfo(name = "hour_05_millis")
    val hour05Millis: Long = 0L,
    @ColumnInfo(name = "hour_06_millis")
    val hour06Millis: Long = 0L,
    @ColumnInfo(name = "hour_07_millis")
    val hour07Millis: Long = 0L,
    @ColumnInfo(name = "hour_08_millis")
    val hour08Millis: Long = 0L,
    @ColumnInfo(name = "hour_09_millis")
    val hour09Millis: Long = 0L,
    @ColumnInfo(name = "hour_10_millis")
    val hour10Millis: Long = 0L,
    @ColumnInfo(name = "hour_11_millis")
    val hour11Millis: Long = 0L,
    @ColumnInfo(name = "hour_12_millis")
    val hour12Millis: Long = 0L,
    @ColumnInfo(name = "hour_13_millis")
    val hour13Millis: Long = 0L,
    @ColumnInfo(name = "hour_14_millis")
    val hour14Millis: Long = 0L,
    @ColumnInfo(name = "hour_15_millis")
    val hour15Millis: Long = 0L,
    @ColumnInfo(name = "hour_16_millis")
    val hour16Millis: Long = 0L,
    @ColumnInfo(name = "hour_17_millis")
    val hour17Millis: Long = 0L,
    @ColumnInfo(name = "hour_18_millis")
    val hour18Millis: Long = 0L,
    @ColumnInfo(name = "hour_19_millis")
    val hour19Millis: Long = 0L,
    @ColumnInfo(name = "hour_20_millis")
    val hour20Millis: Long = 0L,
    @ColumnInfo(name = "hour_21_millis")
    val hour21Millis: Long = 0L,
    @ColumnInfo(name = "hour_22_millis")
    val hour22Millis: Long = 0L,
    @ColumnInfo(name = "hour_23_millis")
    val hour23Millis: Long = 0L,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
