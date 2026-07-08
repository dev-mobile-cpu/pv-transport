package com.pv.transport.data.fuel

import com.google.gson.annotations.SerializedName

data class FuelRequest(
    @SerializedName("request_category")
    val requestCategory: String = "fuel_request",
    val amount: String,
    @SerializedName("fuel_type_id")
    val fuelTypeId: String? = null,
    val remark: String? = null,
    @SerializedName("request_type")
    val requestType: String? = null
)