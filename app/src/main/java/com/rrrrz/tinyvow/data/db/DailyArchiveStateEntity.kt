package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_archive_state")
data class DailyArchiveStateEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "archive_start_date")
    val archiveStartDate: String,
    @ColumnInfo(name = "last_archived_date")
    val lastArchivedDate: String? = null,
    @ColumnInfo(name = "last_attempted_at")
    val lastAttemptedAt: Long = 0L,
    @ColumnInfo(name = "last_succeeded_at")
    val lastSucceededAt: Long = 0L,
    @ColumnInfo(name = "last_error_message")
    val lastErrorMessage: String = "",
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
