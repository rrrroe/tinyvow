package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MediaAppPlaybackStatus {
    UNKNOWN,
    PLAYING,
    PAUSED,
    STOPPED,
    BUFFERING,
    NO_SESSION,
}

@Entity(tableName = "media_app_configs")
data class MediaAppConfigEntity(
    @androidx.room.PrimaryKey
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
    tableName = "media_app_playback_days",
    primaryKeys = ["package_name", "playback_date"],
    indices = [
        Index(value = ["playback_date"]),
        Index(value = ["package_name"]),
        Index(value = ["package_name", "playback_date"]),
    ],
)
data class MediaAppPlaybackDayEntity(
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "playback_date")
    val playbackDate: String,
    @ColumnInfo(name = "trusted_playback_millis")
    val trustedPlaybackMillis: Long = 0L,
    @ColumnInfo(name = "untrusted_gap_millis")
    val untrustedGapMillis: Long = 0L,
    @ColumnInfo(name = "is_playing")
    val isPlaying: Boolean = false,
    @ColumnInfo(name = "active_started_at")
    val activeStartedAt: Long? = null,
    @ColumnInfo(name = "last_confirmed_at")
    val lastConfirmedAt: Long? = null,
    @ColumnInfo(name = "last_status")
    val lastStatus: MediaAppPlaybackStatus = MediaAppPlaybackStatus.UNKNOWN,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)

@Entity(
    tableName = "media_app_playback_segments",
    indices = [
        Index(value = ["package_name"]),
        Index(value = ["playback_date"]),
        Index(value = ["package_name", "start_millis"]),
        Index(value = ["package_name", "end_millis"]),
    ],
)
data class MediaAppPlaybackSegmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "playback_date")
    val playbackDate: String,
    @ColumnInfo(name = "start_millis")
    val startMillis: Long,
    @ColumnInfo(name = "end_millis")
    val endMillis: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
