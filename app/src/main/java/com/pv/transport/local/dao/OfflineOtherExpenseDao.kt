package com.pv.transport.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pv.transport.local.data.OfflineOtherExpenseEntity
import com.pv.transport.local.data.SyncedRecordMapping
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineOtherExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OfflineOtherExpenseEntity)

    @Query("SELECT * FROM offline_other_expenses WHERE isSynced = 0 ORDER BY clientTimestamp ASC")
    suspend fun getPendingExpenses(): List<OfflineOtherExpenseEntity>

    @Query("SELECT * FROM offline_other_expenses WHERE isSynced = 0 ORDER BY clientTimestamp ASC")
    fun observePendingExpenses(): Flow<List<OfflineOtherExpenseEntity>>

    @Query("UPDATE offline_other_expenses SET isSynced = 1, isSyncing = 0, serverRecordId = :recordId WHERE uuid = :uuid")
    suspend fun markSynced(uuid: String, recordId: String?)

    @Query("UPDATE offline_other_expenses SET isSyncing = :isSyncing WHERE uuid = :uuid")
    suspend fun updateSyncingStatus(uuid: String, isSyncing: Boolean)

    @Query("UPDATE offline_other_expenses SET isSyncing = 0")
    suspend fun resetSyncingStatus()

    @Query("SELECT uuid, serverRecordId, clientTimestamp FROM offline_other_expenses WHERE isSynced = 1 AND serverRecordId IS NOT NULL AND clientTimestamp >= :cutoffTimestamp")
    fun observeRecentlySyncedMappings(cutoffTimestamp: Long): Flow<List<SyncedRecordMapping>>

    @Query("SELECT * FROM offline_other_expenses WHERE isSynced = 1 AND clientTimestamp >= :cutoffTimestamp ORDER BY clientTimestamp DESC")
    fun observeRecentlySyncedExpenses(cutoffTimestamp: Long): Flow<List<OfflineOtherExpenseEntity>>

    @Query("DELETE FROM offline_other_expenses WHERE isSynced = 1 AND clientTimestamp < :cutoffTimestamp")
    suspend fun deleteSyncedOlderThan(cutoffTimestamp: Long)

    @Query("DELETE FROM offline_other_expenses")
    suspend fun deleteAll()
}
