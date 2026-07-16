package com.pv.transport.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pv.transport.local.data.FuelCompanyCacheEntity
import com.pv.transport.local.data.FuelTypeCacheEntity

@Dao
interface FuelCompanyCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(types: List<FuelCompanyCacheEntity>)

    @Query("SELECT * FROM fuel_company_cache")
    suspend fun getAll(): List<FuelCompanyCacheEntity>

    @Query("DELETE FROM fuel_company_cache")
    suspend fun clear()
}
