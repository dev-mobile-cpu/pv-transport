package com.pv.transport.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pv.transport.local.data.OfflineCheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineCheckInDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OfflineCheckInEntity)

    @Query("SELECT * FROM offline_check_ins WHERE isSynced = 0 ORDER BY clientTimestamp ASC")
    suspend fun getPendingCheckIns(): List<OfflineCheckInEntity>

    @Query("SELECT * FROM offline_check_ins WHERE isSynced = 0 ORDER BY clientTimestamp ASC")
    fun observePendingCheckIns(): Flow<List<OfflineCheckInEntity>>

    @Query("SELECT * FROM offline_check_ins WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): OfflineCheckInEntity?

    @Query("UPDATE offline_check_ins SET isSynced = 1, isSyncing = 0, serverRecordId = :recordId WHERE uuid = :uuid")
    suspend fun markSynced(uuid: String, recordId: String)

    @Query("UPDATE offline_check_ins SET isSyncing = :isSyncing WHERE uuid = :uuid")
    suspend fun updateSyncingStatus(uuid: String, isSyncing: Boolean)

    @Query("UPDATE offline_check_ins SET isSyncing = 0")
    suspend fun resetSyncingStatus()

    @Query("DELETE FROM offline_check_ins WHERE isSynced = 1")
    suspend fun deleteSynced()

    @Query("DELETE FROM offline_check_ins WHERE isSynced = 1 AND clientTimestamp < :cutoffTimestamp")
    suspend fun deleteSyncedOlderThan(cutoffTimestamp: Long)

    @Query("SELECT * FROM offline_check_ins WHERE isSynced = 1 AND clientTimestamp >= :cutoffTimestamp ORDER BY clientTimestamp DESC")
    fun observeRecentlySyncedCheckIns(cutoffTimestamp: Long): Flow<List<OfflineCheckInEntity>>

    @Query("DELETE FROM offline_check_ins")
    suspend fun deleteAll()
}
