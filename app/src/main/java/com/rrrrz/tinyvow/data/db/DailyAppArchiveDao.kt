package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyAppArchiveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DailyAppArchiveEntity>)

    @Query("DELETE FROM daily_app_archives WHERE archive_date = :date")
    suspend fun deleteByDate(date: String)

    @Query("SELECT COUNT(*) FROM daily_app_archives WHERE archive_date = :date")
    suspend fun countByDate(date: String): Int

    @Query("SELECT COUNT(*) FROM daily_app_archives WHERE archive_date = :date AND is_grouped = 0")
    suspend fun countUngroupedByDate(date: String): Int

    @Transaction
    suspend fun replaceForDate(date: String, items: List<DailyAppArchiveEntity>) {
        deleteByDate(date)
        if (items.isNotEmpty()) {
            insertAll(items)
        }
    }

    @Query(
        """
        SELECT *
        FROM daily_app_archives
        WHERE archive_date BETWEEN :from AND :to
        ORDER BY archive_date ASC, is_grouped DESC, group_name ASC, daily_usage_millis DESC, app_label ASC
        """
    )
    fun getByDateRange(from: String, to: String): Flow<List<DailyAppArchiveEntity>>

    @Query(
        """
        SELECT *
        FROM daily_app_archives
        WHERE archive_date = :date
        ORDER BY is_grouped DESC, group_type ASC, group_name ASC, daily_usage_millis DESC, app_label ASC
        """
    )
    fun getByDate(date: String): Flow<List<DailyAppArchiveEntity>>

    @Query(
        """
        SELECT *
        FROM daily_app_archives
        WHERE archive_date = :date AND is_grouped = 0
        ORDER BY daily_usage_millis DESC, app_label ASC
        """
    )
    fun getUngroupedByDate(date: String): Flow<List<DailyAppArchiveEntity>>

    @Query(
        """
        SELECT *
        FROM daily_app_archives
        WHERE is_grouped = 1 AND group_id = :groupId AND archive_date BETWEEN :from AND :to
        ORDER BY archive_date ASC, daily_usage_millis DESC, app_label ASC
        """
    )
    fun getByGroupAndRange(groupId: String, from: String, to: String): Flow<List<DailyAppArchiveEntity>>

    @Query(
        """
        SELECT *
        FROM daily_app_archives
        WHERE package_name = :packageName AND archive_date BETWEEN :from AND :to
        ORDER BY archive_date ASC, group_name ASC, daily_usage_millis DESC
        """
    )
    fun getByPackageAndRange(packageName: String, from: String, to: String): Flow<List<DailyAppArchiveEntity>>

    @Query(
        """
        WITH scoped_daily_app_archives AS (
            SELECT
                archive_date,
                package_name,
                MAX(app_label) AS app_label,
                MAX(daily_usage_millis) AS daily_usage_millis,
                MAX(open_count) AS open_count,
                MAX(session_count) AS session_count,
                MAX(night_usage_millis) AS night_usage_millis,
                SUM(earned_points) AS earned_points
            FROM daily_app_archives
            WHERE archive_date BETWEEN :from AND :to
                AND (:groupId IS NULL OR (is_grouped = 1 AND group_id = :groupId))
            GROUP BY archive_date, package_name
        )
        SELECT
            package_name,
            MAX(app_label) AS app_label,
            COALESCE(SUM(daily_usage_millis), 0) AS total_usage_millis,
            COALESCE(SUM(open_count), 0) AS total_open_count,
            COALESCE(SUM(session_count), 0) AS total_session_count,
            COALESCE(SUM(night_usage_millis), 0) AS total_night_usage_millis,
            COALESCE(SUM(earned_points), 0) AS total_earned_points
        FROM scoped_daily_app_archives
        GROUP BY package_name
        ORDER BY total_usage_millis DESC, total_open_count DESC, app_label ASC
        LIMIT :limit
        """
    )
    fun getTopAppsByRange(
        groupId: String?,
        from: String,
        to: String,
        limit: Int,
    ): Flow<List<TopAppArchiveSummary>>
}

data class TopAppArchiveSummary(
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "app_label")
    val appLabel: String,
    @ColumnInfo(name = "total_usage_millis")
    val totalUsageMillis: Long,
    @ColumnInfo(name = "total_open_count")
    val totalOpenCount: Int,
    @ColumnInfo(name = "total_session_count")
    val totalSessionCount: Int,
    @ColumnInfo(name = "total_night_usage_millis")
    val totalNightUsageMillis: Long,
    @ColumnInfo(name = "total_earned_points")
    val totalEarnedPoints: Double,
)
