package com.pv.transport.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_type_cache")
data class FuelTypeCacheEntity(
    @PrimaryKey
    val id: Int,
    val name: String
)
