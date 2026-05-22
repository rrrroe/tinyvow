package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SpecialAppProvider {
    WEREAD,
}

enum class SpecialAppUsagePreference {
    READING_FIRST,
    PHONE_FIRST,
}

@Entity(tableName = "special_app_configs")
data class SpecialAppConfigEntity(
    @PrimaryKey
    val provider: SpecialAppProvider,
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "enabled_for_control")
    val enabledForControl: Boolean = false,
    @ColumnInfo(name = "enabled_for_encourage")
    val enabledForEncourage: Boolean = false,
    @ColumnInfo(name = "sync_enabled")
    val syncEnabled: Boolean = false,
    @ColumnInfo(name = "usage_preference")
    val usagePreference: SpecialAppUsagePreference = SpecialAppUsagePreference.READING_FIRST,
    @ColumnInfo(name = "last_sync_at")
    val lastSyncAt: Long = 0L,
    @ColumnInfo(name = "last_success_at")
    val lastSuccessAt: Long = 0L,
    @ColumnInfo(name = "last_error")
    val lastError: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)

@Entity(
    tableName = "special_app_usage_snapshots",
    indices = [
        Index(value = ["provider", "usage_date"], unique = true),
        Index(value = ["package_name", "usage_date"]),
    ],
)
data class SpecialAppUsageSnapshotEntity(
    @PrimaryKey
    val id: String,
    val provider: SpecialAppProvider,
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "usage_date")
    val usageDate: String,
    @ColumnInfo(name = "usage_millis")
    val usageMillis: Long,
    @ColumnInfo(name = "reading_bucket_available")
    val readingBucketAvailable: Boolean = false,
    @ColumnInfo(name = "phone_usage_millis")
    val phoneUsageMillis: Long = 0L,
    @ColumnInfo(name = "phone_collected_at")
    val phoneCollectedAt: Long = 0L,
    @ColumnInfo(name = "source_synced_at")
    val sourceSyncedAt: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)

@Entity(
    tableName = "special_app_point_credits",
    indices = [Index(value = ["provider", "group_id", "credit_date"], unique = true)],
)
data class SpecialAppPointCreditEntity(
    @PrimaryKey
    val id: String,
    val provider: SpecialAppProvider,
    @ColumnInfo(name = "group_id")
    val groupId: String,
    @ColumnInfo(name = "credit_date")
    val creditDate: String,
    @ColumnInfo(name = "credited_usage_millis")
    val creditedUsageMillis: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
