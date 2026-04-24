package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DailyArchiveStateDao {
    @Query("SELECT * FROM daily_archive_state WHERE id = :id LIMIT 1")
    suspend fun get(id: String = "main"): DailyArchiveStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: DailyArchiveStateEntity)
}
