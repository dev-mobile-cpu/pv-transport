package com.pv.transport.data


import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Driver(
    val id: String,
    val name: String,
    @SerializedName("license_plate")
    val licensePlate: String,
    val phone: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("first_time_login")
    val firstTimeLogin: String
)

