package com.pv.transport.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pv.transport.local.data.FuelTypeCacheEntity

@Dao
interface FuelTypeCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(types: List<FuelTypeCacheEntity>)

    @Query("SELECT * FROM fuel_type_cache")
    suspend fun getAll(): List<FuelTypeCacheEntity>

    @Query("DELETE FROM fuel_type_cache")
    suspend fun clear()
}
