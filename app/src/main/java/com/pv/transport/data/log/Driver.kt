package com.pv.transport.data.log

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Driver(
    val id: String,
    val name: String,
    @SerializedName("license_plate")
    val licensePlate: String,
    val phone: String,
    @SerializedName("driver_type")
    val driverType: String,
    @SerializedName("fuel_type_id")
    val fuelTypeId: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("first_time_login")
    val firstTimeLogin: String
)