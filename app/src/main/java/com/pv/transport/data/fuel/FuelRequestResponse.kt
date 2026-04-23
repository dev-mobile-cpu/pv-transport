package com.pv.transport.data.fuel

import com.google.gson.annotations.SerializedName

data class FuelRequestResponse(
    val data: List<FuelRequestData>,
    val links: Links,
    val meta: Meta
)

data class FuelRequestData(
    val id: Int,
    val uuid: String,
    val code: String,
    @SerializedName("license_plate")
    val licensePlate: String,
    val date: String,
    @SerializedName("date_time")
    val dateTime: String,
    @SerializedName("fuel_type")
    val fuelType: String,
    @SerializedName("fuel_type_id")
    val fuelTypeId: String,
    @SerializedName("request_type")
    val requestType: String,
    @SerializedName("fuel_liter")
    val fuelLiter: String?,
    @SerializedName("fuel_amount")
    val fuelAmount: String,
    val status: String,
    val remark: String,
    @SerializedName("approved_date")
    val approvedDate: String?
)