package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyCheckInDao {
    @Query("SELECT * FROM daily_checkins WHERE checkin_date = :date LIMIT 1")
    fun observeByDate(date: String): Flow<DailyCheckInEntity?>

    @Query("SELECT * FROM daily_checkins WHERE checkin_date = :date LIMIT 1")
    suspend fun getByDate(date: String): DailyCheckInEntity?

    @Query("SELECT * FROM daily_checkins WHERE checkin_date BETWEEN :from AND :to ORDER BY checkin_date ASC")
    fun observeByDateRange(from: String, to: String): Flow<List<DailyCheckInEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: DailyCheckInEntity)
}
