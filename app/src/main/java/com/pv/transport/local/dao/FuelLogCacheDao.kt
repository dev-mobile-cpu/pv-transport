package com.pv.transport.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pv.transport.local.data.DriverLogCacheEntity
import com.pv.transport.local.data.FuelLogCacheEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface FuelLogCacheDao {
    @Query("SELECT * FROM fuel_log_cache WHERE id = 'last_fetched_logs'")
    fun getCachedLogs(): Flow<FuelLogCacheEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: FuelLogCacheEntity)

    @Query("DELETE FROM fuel_log_cache")
    suspend fun clearCache()
}