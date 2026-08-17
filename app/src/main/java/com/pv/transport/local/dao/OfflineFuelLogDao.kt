package com.pv.transport.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pv.transport.local.data.OfflineFuelLogEntity
import com.pv.transport.local.data.SyncedRecordMapping
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineFuelLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OfflineFuelLogEntity)

    @Query("SELECT * FROM offline_fuel_logs WHERE isSynced = 0 ORDER BY clientTimestamp DESC")
    suspend fun getPendingFuelLogs(): List<OfflineFuelLogEntity>

    @Query("SELECT * FROM offline_fuel_logs WHERE isSynced = 0 ORDER BY clientTimestamp DESC")
    fun observePendingFuelLogs(): Flow<List<OfflineFuelLogEntity>>

    @Query("UPDATE offline_fuel_logs SET isSynced = 1, isSyncing = 0, serverRecordId = :recordId WHERE uuid = :uuid")
    suspend fun markSynced(uuid: String, recordId: String?)

    @Query("UPDATE offline_fuel_logs SET isSyncing = :isSyncing WHERE uuid = :uuid")
    suspend fun updateSyncingStatus(uuid: String, isSyncing: Boolean)

    @Query("UPDATE offline_fuel_logs SET isSyncing = 0")
    suspend fun resetSyncingStatus()

    @Query("SELECT uuid, serverRecordId, clientTimestamp FROM offline_fuel_logs WHERE isSynced = 1 AND serverRecordId IS NOT NULL AND clientTimestamp >= :cutoffTimestamp")
    fun observeRecentlySyncedMappings(cutoffTimestamp: Long): Flow<List<SyncedRecordMapping>>

    @Query("DELETE FROM offline_fuel_logs WHERE isSynced = 1 AND clientTimestamp < :cutoffTimestamp")
    suspend fun deleteSyncedOlderThan(cutoffTimestamp: Long)

    @Query("DELETE FROM offline_fuel_logs")
    suspend fun deleteAll()
}
