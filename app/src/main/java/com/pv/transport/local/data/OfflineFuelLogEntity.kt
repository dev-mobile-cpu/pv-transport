package com.pv.transport.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_fuel_logs")
data class OfflineFuelLogEntity(
    @PrimaryKey
    val uuid: String,
    val carPlateNo: String,
    val date: String,
    val fuelCompanyId: String,
    val fuelShop: String,
    val fuelTypeId: String,
    val fuelAmount: String,
    val fuelLiter: String,
    val filesPaths: String,           // JSON array of absolute file paths
    val currentKm: String,
    val currentKmPhotoPath: String,
    val walletBucket: String,
    val clientTimestamp: Long,
    val isSynced: Boolean = false
)
