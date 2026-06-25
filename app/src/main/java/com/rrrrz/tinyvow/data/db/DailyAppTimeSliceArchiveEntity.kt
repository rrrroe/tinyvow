package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "daily_app_time_slice_archives",
    primaryKeys = ["archive_date", "slice_index", "package_name"],
    indices = [
        Index(value = ["archive_date"]),
        Index(value = ["archive_date", "slice_index"]),
        Index(value = ["archive_date", "package_name"]),
    ],
)
data class DailyAppTimeSliceArchiveEntity(
    @ColumnInfo(name = "archive_date")
    val archiveDate: String,
    @ColumnInfo(name = "slice_index")
    val sliceIndex: Int,
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "usage_millis")
    val usageMillis: Long,
)
