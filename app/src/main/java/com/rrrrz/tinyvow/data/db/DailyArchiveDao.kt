package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyArchiveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(archive: DailyArchiveEntity)

    @Query("SELECT * FROM daily_archives ORDER BY archive_date DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<DailyArchiveEntity>>

    @Query("SELECT * FROM daily_archives WHERE archive_date = :date LIMIT 1")
    fun getByDate(date: String): Flow<DailyArchiveEntity?>

    @Query("SELECT * FROM daily_archives WHERE archive_date = :date LIMIT 1")
    suspend fun getByDateSync(date: String): DailyArchiveEntity?

    @Query("SELECT * FROM daily_archives WHERE archive_date BETWEEN :from AND :to ORDER BY archive_date ASC")
    fun getByDateRange(from: String, to: String): Flow<List<DailyArchiveEntity>>

    @Query("SELECT archive_date FROM daily_archives ORDER BY archive_date ASC")
    suspend fun getAllArchiveDatesAsc(): List<String>

    @Query("SELECT * FROM daily_archives ORDER BY archive_date ASC")
    suspend fun getAllAsc(): List<DailyArchiveEntity>

    @Query("SELECT * FROM daily_archives ORDER BY archive_date ASC")
    fun observeAllAsc(): Flow<List<DailyArchiveEntity>>
}
