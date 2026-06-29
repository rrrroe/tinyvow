package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaAppConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: MediaAppConfigEntity)

    @Query("SELECT * FROM media_app_configs WHERE package_name = :packageName LIMIT 1")
    suspend fun get(packageName: String): MediaAppConfigEntity?

    @Query("SELECT * FROM media_app_configs ORDER BY updated_at DESC")
    suspend fun getAll(): List<MediaAppConfigEntity>

    @Query("SELECT * FROM media_app_configs WHERE enabled = 1 ORDER BY updated_at DESC")
    suspend fun getEnabled(): List<MediaAppConfigEntity>

    @Query("SELECT package_name FROM media_app_configs WHERE enabled = 1")
    suspend fun getEnabledPackageNames(): List<String>

    @Query("SELECT * FROM media_app_configs ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<MediaAppConfigEntity>>

    @Query(
        """
        UPDATE media_app_configs
        SET enabled = 0,
            updated_at = :updatedAt
        WHERE package_name = :packageName
        """
    )
    suspend fun disable(packageName: String, updatedAt: Long)

    @Query("DELETE FROM media_app_configs WHERE package_name = :packageName")
    suspend fun delete(packageName: String)
}

@Dao
interface MediaAppPlaybackDayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(day: MediaAppPlaybackDayEntity)

    @Query(
        """
        SELECT *
        FROM media_app_playback_days
        WHERE package_name = :packageName AND playback_date = :date
        LIMIT 1
        """
    )
    suspend fun get(packageName: String, date: String): MediaAppPlaybackDayEntity?

    @Query(
        """
        SELECT *
        FROM media_app_playback_days
        WHERE package_name = :packageName
        ORDER BY playback_date DESC
        LIMIT 1
        """
    )
    suspend fun getLatestForPackage(packageName: String): MediaAppPlaybackDayEntity?

    @Query(
        """
        SELECT *
        FROM media_app_playback_days
        WHERE playback_date = :date
        ORDER BY package_name ASC
        """
    )
    suspend fun getByDate(date: String): List<MediaAppPlaybackDayEntity>

    @Query(
        """
        SELECT *
        FROM media_app_playback_days
        WHERE package_name IN (:packageNames)
            AND playback_date BETWEEN :fromDate AND :toDate
        ORDER BY playback_date ASC
        """
    )
    suspend fun getByPackagesAndDateRange(
        packageNames: List<String>,
        fromDate: String,
        toDate: String,
    ): List<MediaAppPlaybackDayEntity>

    @Query(
        """
        SELECT *
        FROM media_app_playback_days
        WHERE package_name = :packageName
            AND playback_date BETWEEN :fromDate AND :toDate
        ORDER BY playback_date ASC
        """
    )
    suspend fun getByPackageAndDateRange(
        packageName: String,
        fromDate: String,
        toDate: String,
    ): List<MediaAppPlaybackDayEntity>

    @Query("SELECT * FROM media_app_playback_days WHERE playback_date = :date")
    fun observeByDate(date: String): Flow<List<MediaAppPlaybackDayEntity>>
}

@Dao
interface MediaAppPlaybackSegmentDao {
    @Insert
    suspend fun insert(segment: MediaAppPlaybackSegmentEntity): Long

    @Query(
        """
        SELECT *
        FROM media_app_playback_segments
        WHERE package_name = :packageName
            AND end_millis = :endMillis
        ORDER BY start_millis DESC
        LIMIT 1
        """
    )
    suspend fun getLatestEndingAt(packageName: String, endMillis: Long): MediaAppPlaybackSegmentEntity?

    @Query(
        """
        UPDATE media_app_playback_segments
        SET end_millis = :endMillis,
            updated_at = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun updateEnd(id: Long, endMillis: Long, updatedAt: Long)

    @Query(
        """
        SELECT *
        FROM media_app_playback_segments
        WHERE package_name = :packageName
            AND start_millis < :endMillis
            AND end_millis > :startMillis
        ORDER BY start_millis ASC
        """
    )
    suspend fun getOverlapping(
        packageName: String,
        startMillis: Long,
        endMillis: Long,
    ): List<MediaAppPlaybackSegmentEntity>
}
