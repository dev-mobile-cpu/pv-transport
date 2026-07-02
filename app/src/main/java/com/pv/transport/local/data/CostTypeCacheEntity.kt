package com.pv.transport.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cost_type_cache")
data class CostTypeCacheEntity(
    @PrimaryKey
    val id: String,
    val name: String
)
