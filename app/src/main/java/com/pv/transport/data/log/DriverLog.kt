package com.pv.transport.data.log

import com.google.gson.annotations.SerializedName

data class DriverLog(
    @SerializedName("car_plate_no")
    val carPlateNo: String,
    val date: String,
    val reason: String,
    val remark: String,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String,
    @SerializedName("start_km")
    val startKm: String,
    @SerializedName("end_km")
    val endKm: String,
    @SerializedName("start_photo")
    val startPhoto: String,
    @SerializedName("end_photo")
    val endPhoto: String
)