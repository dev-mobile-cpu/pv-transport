package com.pv.transport.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pv.transport.local.data.OfflineCheckOutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineCheckOutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OfflineCheckOutEntity)

    @Query("SELECT * FROM offline_check_outs WHERE isSynced = 0 ORDER BY clientTimestamp ASC")
    suspend fun getPendingCheckOuts(): List<OfflineCheckOutEntity>

    @Query("SELECT * FROM offline_check_outs WHERE localCheckInUuid = :checkInUuid AND isSynced = 0")
    suspend fun getPendingCheckOutForCheckIn(checkInUuid: String): OfflineCheckOutEntity?

    @Query("UPDATE offline_check_outs SET isSynced = 1 WHERE uuid = :uuid")
    suspend fun markSynced(uuid: String)

    @Query("DELETE FROM offline_check_outs WHERE isSynced = 1")
    suspend fun deleteSynced()

    @Query("SELECT * FROM offline_check_outs WHERE isSynced = 0 ORDER BY clientTimestamp ASC")
    fun observePendingCheckOuts(): Flow<List<OfflineCheckOutEntity>>
}
