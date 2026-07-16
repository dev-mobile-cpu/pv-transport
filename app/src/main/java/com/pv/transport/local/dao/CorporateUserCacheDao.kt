package com.pv.transport.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pv.transport.local.data.CorporateUserCacheEntity
import com.pv.transport.local.data.FuelCompanyCacheEntity
import com.pv.transport.local.data.FuelTypeCacheEntity

@Dao
interface CorporateUserCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(types: List<CorporateUserCacheEntity>)

    @Query("SELECT * FROM corporate_user_cache")
    suspend fun getAll(): List<CorporateUserCacheEntity>

    @Query("DELETE FROM corporate_user_cache")
    suspend fun clear()
}
