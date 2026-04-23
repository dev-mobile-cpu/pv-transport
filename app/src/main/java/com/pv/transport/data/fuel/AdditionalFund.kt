package com.pv.transport.data.fuel

import com.google.gson.annotations.SerializedName

data class AdditionalFund  (
    val id: Int,
    @SerializedName("license_plate")
    val licensePlate: String,
    val date: String,
    @SerializedName("fuel_amount")
    val fuelAmount: Double,
    val status : String
 )