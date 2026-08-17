package com.pv.transport.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pv.transport.data.fuel.CurrentKmPhoto
import com.pv.transport.data.fuel.FuelDocument
import com.pv.transport.data.fuel.FuelLogData
import kotlin.jvm.java

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
    val isSynced: Boolean = false,
    val isSyncing: Boolean = false,
    val serverRecordId: String? = null
)


fun OfflineFuelLogEntity.toFuelLogData(): FuelLogData {

    val isSyncing = isSyncing

    return FuelLogData(
        id = "",
        uuid = uuid,
        date = date,
        carPlateNo = carPlateNo,
        fuelShop = fuelShop,
        fuelAmount = fuelAmount,
        fuelLiter = fuelLiter,
        customer = "",
        fuelType = fuelTypeId,
        status = if (isSyncing) "SYNCING" else "OFFLINE",
        documents = emptyList(),
        currentKm = currentKm,
        currentKmPhoto = CurrentKmPhoto(
            fileName = currentKmPhotoPath,
            photoUrl = "",
            thumbnailUrl = ""
        ),
        isSynced = isSynced
    )
}