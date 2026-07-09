package com.rrrrz.tinyvow.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

const val STEP_DAY_SOURCE_SENSOR = "SENSOR"
const val STEP_DAY_SOURCE_HEALTH_CONNECT = "HEALTH_CONNECT"

@Entity(
    tableName = "step_days",
    indices = [
        Index(value = ["step_date"], unique = true),
    ],
)
data class StepDayEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "step_date")
    val stepDate: String,
    @ColumnInfo(name = "steps")
    val steps: Int,
    @ColumnInfo(name = "sensor_base_steps")
    val sensorBaseSteps: Long,
    @ColumnInfo(name = "last_sensor_steps")
    val lastSensorSteps: Long,
    @ColumnInfo(name = "source")
    val source: String = STEP_DAY_SOURCE_SENSOR,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)

@Entity(
    tableName = "step_point_credits",
    indices = [
        Index(value = ["group_id", "credit_date"], unique = true),
    ],
)
data class StepPointCreditEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "group_id")
    val groupId: String,
    @ColumnInfo(name = "credit_date")
    val creditDate: String,
    @ColumnInfo(name = "credited_steps")
    val creditedSteps: Int,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)

@Dao
interface StepDayDao {
    @Query("SELECT * FROM step_days WHERE step_date = :date LIMIT 1")
    suspend fun getByDate(date: String): StepDayEntity?

    @Query("SELECT * FROM step_days WHERE step_date = :date LIMIT 1")
    fun observeByDate(date: String): Flow<StepDayEntity?>

    @Query("SELECT * FROM step_days ORDER BY step_date ASC")
    fun observeAll(): Flow<List<StepDayEntity>>

    @Query("SELECT * FROM step_days WHERE step_date < :date ORDER BY step_date DESC LIMIT 1")
    suspend fun getLatestBefore(date: String): StepDayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(day: StepDayEntity)
}

@Dao
interface StepPointCreditDao {
    @Query("SELECT * FROM step_point_credits WHERE group_id = :groupId AND credit_date = :date LIMIT 1")
    suspend fun get(groupId: String, date: String): StepPointCreditEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(credit: StepPointCreditEntity)
}
