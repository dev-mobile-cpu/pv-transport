package com.pv.transport.data.fuel

import com.google.gson.annotations.SerializedName

data class FuelRequest(
    val amount: String,
    @SerializedName("fuel_type_id")
    val fuelTypeId: String,
    val remark: String,
    @SerializedName("request_type")
    val requestType: String
)