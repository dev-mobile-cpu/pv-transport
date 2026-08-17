package com.pv.transport.data.log

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class DriverVehicle(
    val name: String? = null,
    val type: String? = null,
    @SerializedName(value = "license_plate", alternate = ["liscense_plate"])
    val licensePlate: String? = null
)

@Serializable
data class Driver(
    val id: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val corporate: String? = null,
    val vehicle: DriverVehicle? = null,
    /** Flat legacy plate; prefer [resolvedLicensePlate] which also reads nested vehicle. */
    @SerializedName(value = "license_plate", alternate = ["liscense_plate"])
    val licensePlate: String? = null,
    @SerializedName("driver_type")
    val driverType: String? = null,
    @SerializedName("fuel_type_id")
    val fuelTypeId: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("first_time_login")
    val firstTimeLogin: String? = null
) {
    val resolvedLicensePlate: String?
        get() = vehicle?.licensePlate?.takeUnless { it.isBlank() }
            ?: licensePlate?.takeUnless { it.isBlank() }

    val vehicleName: String?
        get() = vehicle?.name?.takeUnless { it.isBlank() }

    val vehicleType: String?
        get() = vehicle?.type?.takeUnless { it.isBlank() }
}
