package com.pv.transport.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pv.transport.local.data.ReasonCacheEntity

@Dao
interface ReasonCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reasons: List<ReasonCacheEntity>)

    @Query("SELECT * FROM reason_cache")
    suspend fun getAll(): List<ReasonCacheEntity>

    @Query("DELETE FROM reason_cache")
    suspend fun clear()
}
