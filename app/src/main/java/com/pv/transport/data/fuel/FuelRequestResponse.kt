package com.pv.transport.data.fuel

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


data class FuelRequestResponse(
    val data: List<FuelRequestData>,
    val links: Links,
    val meta: Meta
)

@Parcelize
data class FuelRequestData(
    val id: Int,
    val uuid: String? = null,
    val code: String? = null,
    @SerializedName("license_plate")
    val licensePlate: String? = null,
    val date: String? = null,
    @SerializedName("date_time")
    val dateTime: String? = null,
    @SerializedName("fuel_type")
    val fuelType: String? = null,
    @SerializedName("fuel_type_id")
    val fuelTypeId: String? = null,
    @SerializedName("request_type")
    val requestType: String? = null,
    @SerializedName("fuel_liter")
    val fuelLiter: String? = null,
    @SerializedName("fuel_amount")
    val fuelAmount: String? = null,
    val status: String? = null,
    val remark: String? = null,
    @SerializedName("approved_date")
    val approvedDate: String? = null
) : Parcelable