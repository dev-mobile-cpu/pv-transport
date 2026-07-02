package com.pv.transport.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pv.transport.local.data.CostTypeCacheEntity

@Dao
interface CostTypeCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(types: List<CostTypeCacheEntity>)

    @Query("SELECT * FROM cost_type_cache")
    suspend fun getAll(): List<CostTypeCacheEntity>

    @Query("DELETE FROM cost_type_cache")
    suspend fun clear()
}
