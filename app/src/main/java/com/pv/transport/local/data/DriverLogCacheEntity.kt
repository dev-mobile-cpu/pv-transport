package com.pv.transport.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pv.transport.data.log.Data

@Entity(tableName = "driver_log_cache")
data class DriverLogCacheEntity(
    @PrimaryKey
    val id: String = "last_fetched_logs", // We use a fixed ID to store the latest list
    val logs: List<Data>,
    val lastUpdated: Long = System.currentTimeMillis()
)
