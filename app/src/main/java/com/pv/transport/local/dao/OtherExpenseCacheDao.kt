package com.pv.transport.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pv.transport.local.data.DriverLogCacheEntity
import com.pv.transport.local.data.FuelLogCacheEntity
import com.pv.transport.local.data.OtherExpenseCacheEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface OtherExpenseCacheDao {
    @Query("SELECT * FROM other_expense_cache WHERE id = 'last_fetched_logs'")
    fun getCachedLogs(): Flow<OtherExpenseCacheEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: OtherExpenseCacheEntity)

    @Query("DELETE FROM other_expense_cache")
    suspend fun clearCache()
}