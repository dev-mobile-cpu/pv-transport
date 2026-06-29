package com.pv.transport.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "driver_logs")
data class DriverLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val type: String,
    val reason: String,
    val remark: String,
    val startTime: String,
    val startKm: String,
    val startPhoto: String,
    val syncStatus: Int = 0
)