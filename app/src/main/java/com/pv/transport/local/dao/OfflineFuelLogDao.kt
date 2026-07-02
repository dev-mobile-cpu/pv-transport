package com.pv.transport.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pv.transport.local.data.OfflineFuelLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineFuelLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OfflineFuelLogEntity)

    @Query("SELECT * FROM offline_fuel_logs WHERE isSynced = 0 ORDER BY clientTimestamp ASC")
    suspend fun getPendingFuelLogs(): List<OfflineFuelLogEntity>

    @Query("SELECT * FROM offline_fuel_logs WHERE isSynced = 0 ORDER BY clientTimestamp ASC")
    fun observePendingFuelLogs(): Flow<List<OfflineFuelLogEntity>>

    @Query("UPDATE offline_fuel_logs SET isSynced = 1 WHERE uuid = :uuid")
    suspend fun markSynced(uuid: String)

    @Query("DELETE FROM offline_fuel_logs WHERE isSynced = 1")
    suspend fun deleteSynced()
}
