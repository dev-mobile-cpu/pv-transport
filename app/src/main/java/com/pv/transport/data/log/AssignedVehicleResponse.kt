package com.pv.transport.data.log

import com.google.gson.annotations.SerializedName

data class AssignedVehicleResponse(
    val data: List<AssignedVehicle>
)
data class AssignedVehicle(
    val id: Int,
    @SerializedName("license_plate")
    val licensePlate: String,
    @SerializedName("is_primary")
    val isPrimary: String,

)
