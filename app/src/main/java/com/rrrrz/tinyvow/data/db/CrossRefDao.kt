package com.rrrrz.tinyvow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CrossRefDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(refs: List<GroupAppCrossRef>)

    @Query("UPDATE group_app_cross_ref SET is_deleted = 1, updated_at = :updatedAt WHERE package_name = :packageName AND group_id = :groupId")
    suspend fun softDeleteCrossRef(packageName: String, groupId: String, updatedAt: Long)
    
    @Query("UPDATE group_app_cross_ref SET is_deleted = 1, updated_at = :updatedAt WHERE group_id = :groupId")
    suspend fun softDeleteAllForGroup(groupId: String, updatedAt: Long)

    @Query("SELECT * FROM group_app_cross_ref WHERE is_deleted = 0")
    fun getAllValidCrossRefs(): Flow<List<GroupAppCrossRef>>

    @Query("SELECT * FROM group_app_cross_ref WHERE is_deleted = 0")
    fun getAllValidCrossRefsSync(): List<GroupAppCrossRef>

    /** 同步查询：给定 packageName，返回它所属的所有 groupId */
    @Query("SELECT group_id FROM group_app_cross_ref WHERE package_name = :packageName AND is_deleted = 0")
    fun getGroupIdsForPackageSync(packageName: String): List<String>

    /** 同步查询：给定 groupId，返回组内所有 packageName */
    @Query("SELECT package_name FROM group_app_cross_ref WHERE group_id = :groupId AND is_deleted = 0")
    fun getPackageNamesForGroupSync(groupId: String): List<String>
}
