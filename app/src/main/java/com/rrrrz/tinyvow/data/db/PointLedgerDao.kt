package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PointLedgerDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: PointLedgerEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(entry: PointLedgerEntity): Long

    @Query("DELETE FROM point_ledger WHERE id = :entryId")
    suspend fun deleteById(entryId: String)

    @Query(
        """
        SELECT COALESCE(SUM(delta_points), 0)
        FROM point_ledger
        WHERE ledger_date = :date AND delta_points > 0
        """
    )
    suspend fun sumEarnedByDate(date: String): Double

    @Query(
        """
        SELECT COALESCE(SUM(delta_points), 0)
        FROM point_ledger
        WHERE ledger_date = :date AND delta_points > 0 AND group_id IS NULL
        """
    )
    suspend fun sumUngroupedEarnedByDate(date: String): Double

    @Query(
        """
        SELECT COALESCE(ABS(SUM(delta_points)), 0)
        FROM point_ledger
        WHERE ledger_date = :date AND delta_points < 0
        """
    )
    suspend fun sumSpentByDate(date: String): Double

    @Query(
        """
        SELECT COALESCE(SUM(delta_points), 0)
        FROM point_ledger
        WHERE ledger_date = :date AND group_id = :groupId AND delta_points > 0
        """
    )
    suspend fun sumEarnedByDateAndGroup(date: String, groupId: String): Double
}
