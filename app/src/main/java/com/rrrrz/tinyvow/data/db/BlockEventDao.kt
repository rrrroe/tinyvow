package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BlockEventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: BlockEventEntity)

    @Query("SELECT COUNT(*) FROM block_events WHERE event_date = :date")
    suspend fun countByDate(date: String): Int

    @Query("SELECT COUNT(*) FROM block_events WHERE event_date = :date AND group_id = :groupId")
    suspend fun countByDateAndGroup(date: String, groupId: String): Int
}
