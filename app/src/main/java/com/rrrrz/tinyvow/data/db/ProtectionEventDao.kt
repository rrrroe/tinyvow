package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtectionEventDao {
    @Query("SELECT * FROM protection_events ORDER BY occurred_at DESC")
    fun observeAll(): Flow<List<ProtectionEventEntity>>

    @Query("SELECT * FROM protection_events WHERE event_date = :date ORDER BY occurred_at DESC")
    fun observeByDate(date: String): Flow<List<ProtectionEventEntity>>

    @Query("SELECT * FROM protection_events WHERE event_date = :date ORDER BY occurred_at DESC")
    suspend fun getByDateSync(date: String): List<ProtectionEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: ProtectionEventEntity)
}
