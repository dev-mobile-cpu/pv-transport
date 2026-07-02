package com.pv.transport.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pv.transport.local.data.DriverLogCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DriverLogCacheDao {
    @Query("SELECT * FROM driver_log_cache WHERE id = 'last_fetched_logs'")
    fun getCachedLogs(): Flow<DriverLogCacheEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: DriverLogCacheEntity)

    @Query("DELETE FROM driver_log_cache")
    suspend fun clearCache()
}
