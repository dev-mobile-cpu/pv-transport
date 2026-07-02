package com.pv.transport.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reason_cache")
data class ReasonCacheEntity(
    @PrimaryKey
    val id: String,
    val value: String
)
