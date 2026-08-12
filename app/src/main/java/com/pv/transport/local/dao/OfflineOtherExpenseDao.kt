package com.pv.transport.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pv.transport.local.data.OfflineOtherExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineOtherExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OfflineOtherExpenseEntity)

    @Query("SELECT * FROM offline_other_expenses WHERE isSynced = 0 ORDER BY clientTimestamp ASC")
    suspend fun getPendingExpenses(): List<OfflineOtherExpenseEntity>

    @Query("SELECT * FROM offline_other_expenses WHERE isSynced = 0 ORDER BY clientTimestamp ASC")
    fun observePendingExpenses(): Flow<List<OfflineOtherExpenseEntity>>

    @Query("UPDATE offline_other_expenses SET isSynced = 1 WHERE uuid = :uuid")
    suspend fun markSynced(uuid: String)

    @Query("UPDATE offline_other_expenses SET isSyncing = :isSyncing WHERE uuid = :uuid")
    suspend fun updateSyncingStatus(uuid: String, isSyncing: Boolean)

    @Query("DELETE FROM offline_other_expenses WHERE isSynced = 1")
    suspend fun deleteSynced()
}
