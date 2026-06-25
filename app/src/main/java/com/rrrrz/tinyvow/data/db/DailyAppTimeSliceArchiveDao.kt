package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyAppTimeSliceArchiveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DailyAppTimeSliceArchiveEntity>)

    @Query("DELETE FROM daily_app_time_slice_archives WHERE archive_date = :date")
    suspend fun deleteByDate(date: String)

    @Query(
        """
        SELECT *
        FROM daily_app_time_slice_archives
        WHERE archive_date = :date
        ORDER BY slice_index ASC, usage_millis DESC, package_name ASC
        """
    )
    fun getByDate(date: String): Flow<List<DailyAppTimeSliceArchiveEntity>>

    @Query(
        """
        SELECT *
        FROM daily_app_time_slice_archives
        WHERE archive_date = :date
        ORDER BY slice_index ASC, usage_millis DESC, package_name ASC
        """
    )
    suspend fun getByDateSync(date: String): List<DailyAppTimeSliceArchiveEntity>

    @Transaction
    suspend fun replaceForDate(date: String, items: List<DailyAppTimeSliceArchiveEntity>) {
        deleteByDate(date)
        if (items.isNotEmpty()) {
            insertAll(items)
        }
    }
}
