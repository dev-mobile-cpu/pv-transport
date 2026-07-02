package com.pv.transport.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_type_cache")
data class TripTypeCacheEntity(
    @PrimaryKey
    val id: String,
    val value: String
)
