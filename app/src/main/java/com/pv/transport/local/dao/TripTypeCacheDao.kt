package com.pv.transport.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pv.transport.local.data.TripTypeCacheEntity

@Dao
interface TripTypeCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(types: List<TripTypeCacheEntity>)

    @Query("SELECT * FROM trip_type_cache")
    suspend fun getAll(): List<TripTypeCacheEntity>

    @Query("DELETE FROM trip_type_cache")
    suspend fun clear()
}
