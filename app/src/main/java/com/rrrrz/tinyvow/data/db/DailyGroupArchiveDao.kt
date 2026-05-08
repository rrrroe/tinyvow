package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyGroupArchiveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DailyGroupArchiveEntity>)

    @Query("DELETE FROM daily_group_archives WHERE archive_date = :date")
    suspend fun deleteByDate(date: String)

    @Query("SELECT * FROM daily_group_archives WHERE archive_date = :date ORDER BY sort_order ASC, created_at ASC")
    fun getByDate(date: String): Flow<List<DailyGroupArchiveEntity>>

    @Query("SELECT * FROM daily_group_archives WHERE archive_date = :date ORDER BY sort_order ASC, created_at ASC")
    suspend fun getByDateSync(date: String): List<DailyGroupArchiveEntity>

    @Query(
        """
        SELECT *
        FROM daily_group_archives
        WHERE archive_date BETWEEN :from AND :to
        ORDER BY archive_date ASC, sort_order ASC, created_at ASC
        """
    )
    fun getByDateRange(from: String, to: String): Flow<List<DailyGroupArchiveEntity>>

    @Query("SELECT * FROM daily_group_archives ORDER BY archive_date ASC, sort_order ASC, created_at ASC")
    suspend fun getAllAsc(): List<DailyGroupArchiveEntity>

    @Query("SELECT * FROM daily_group_archives ORDER BY archive_date ASC, sort_order ASC, created_at ASC")
    fun observeAllAsc(): Flow<List<DailyGroupArchiveEntity>>

    @Query("SELECT COUNT(*) FROM daily_group_archives WHERE archive_date = :date AND package_count > 0")
    suspend fun countGroupsWithPackagesByDate(date: String): Int
}
