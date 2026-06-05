package com.pv.transport.data.fuel

import com.google.gson.annotations.SerializedName
import com.pv.transport.data.log.Document

data class FuelRecord (
    val id:Int,
    @SerializedName("license_plate")
    val licensePlate: String,
    val code : String,
    @field:SerializedName("driver_name")
    val driverName: String,
    val station: String,
    @field:SerializedName("fuel_type")
    val fuelType: String?,
    val liter: Double,
    val amount: Double,
    val status: String,
    val date : String,
    val documents: List<Document>,
    @SerializedName("fuel_liter")
    val fuelLiter: Double,
    val type: String,
    @SerializedName("payslip_uploaded")
    val payslipUploaded:String

)