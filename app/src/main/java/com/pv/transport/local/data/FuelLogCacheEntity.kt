package com.pv.transport.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.pv.transport.data.fuel.FuelLogData
import com.pv.transport.data.fuel.Links
import com.pv.transport.data.fuel.Meta
import com.pv.transport.data.log.Data

@Entity(tableName = "fuel_log_cache")
data class FuelLogCacheEntity(
    @PrimaryKey
    val id: String = "last_fetched_logs", // We use a fixed ID to store the latest list
    val logs: List<FuelLogData>,
    val lastUpdated: Long = System.currentTimeMillis()
)
