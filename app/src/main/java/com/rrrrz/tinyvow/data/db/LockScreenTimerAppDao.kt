package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LockScreenTimerAppConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: LockScreenTimerAppConfigEntity)

    @Query("SELECT * FROM lock_screen_timer_app_configs WHERE package_name = :packageName LIMIT 1")
    suspend fun get(packageName: String): LockScreenTimerAppConfigEntity?

    @Query("SELECT * FROM lock_screen_timer_app_configs ORDER BY updated_at DESC")
    suspend fun getAll(): List<LockScreenTimerAppConfigEntity>

    @Query("SELECT * FROM lock_screen_timer_app_configs WHERE enabled = 1 ORDER BY updated_at DESC")
    suspend fun getEnabled(): List<LockScreenTimerAppConfigEntity>

    @Query("SELECT package_name FROM lock_screen_timer_app_configs WHERE enabled = 1")
    suspend fun getEnabledPackageNames(): List<String>

    @Query("SELECT * FROM lock_screen_timer_app_configs ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<LockScreenTimerAppConfigEntity>>

    @Query(
        """
        UPDATE lock_screen_timer_app_configs
        SET enabled = 0,
            updated_at = :updatedAt
        WHERE package_name = :packageName
        """
    )
    suspend fun disable(packageName: String, updatedAt: Long)
}

@Dao
interface LockScreenTimerAppDayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(day: LockScreenTimerAppDayEntity)

    @Query(
        """
        SELECT *
        FROM lock_screen_timer_app_days
        WHERE package_name = :packageName AND timer_date = :date
        LIMIT 1
        """
    )
    suspend fun get(packageName: String, date: String): LockScreenTimerAppDayEntity?

    @Query(
        """
        SELECT *
        FROM lock_screen_timer_app_days
        WHERE package_name = :packageName
        ORDER BY timer_date DESC
        LIMIT 1
        """
    )
    suspend fun getLatestForPackage(packageName: String): LockScreenTimerAppDayEntity?

    @Query(
        """
        SELECT *
        FROM lock_screen_timer_app_days
        WHERE timer_date = :date
        ORDER BY package_name ASC
        """
    )
    suspend fun getByDate(date: String): List<LockScreenTimerAppDayEntity>

    @Query(
        """
        SELECT *
        FROM lock_screen_timer_app_days
        WHERE package_name IN (:packageNames)
            AND timer_date BETWEEN :fromDate AND :toDate
        ORDER BY timer_date ASC
        """
    )
    suspend fun getByPackagesAndDateRange(
        packageNames: List<String>,
        fromDate: String,
        toDate: String,
    ): List<LockScreenTimerAppDayEntity>
}

@Dao
interface LockScreenTimerAppSegmentDao {
    @Insert
    suspend fun insert(segment: LockScreenTimerAppSegmentEntity): Long

    @Query(
        """
        SELECT *
        FROM lock_screen_timer_app_segments
        WHERE package_name = :packageName
            AND end_millis = :endMillis
        ORDER BY start_millis DESC
        LIMIT 1
        """
    )
    suspend fun getLatestEndingAt(packageName: String, endMillis: Long): LockScreenTimerAppSegmentEntity?

    @Query(
        """
        UPDATE lock_screen_timer_app_segments
        SET end_millis = :endMillis,
            updated_at = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun updateEnd(id: Long, endMillis: Long, updatedAt: Long)

    @Query(
        """
        SELECT *
        FROM lock_screen_timer_app_segments
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
    ): List<LockScreenTimerAppSegmentEntity>
}
