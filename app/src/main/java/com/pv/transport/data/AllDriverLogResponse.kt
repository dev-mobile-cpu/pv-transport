package com.pv.transport.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class AllDriverLogResponse(
    val data: List<Data>,
    val links: Link,
    val meta: Meta
)

@Parcelize
data class Data(
    val id: String,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String?,
    val reason: String,
    val remark: String,
    @SerializedName("start_km")
    val startKm: String,
    @SerializedName("end_km")
    val endKm: String?,
    @SerializedName("driver_log_id")
    val driverLogId: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val details: String?,
    @SerializedName("is_checkout")
    val isCheckout: String,
    val status: String,
    @SerializedName("driver_log")
    val driverLog: DriverLogData,
    val documents: List<Document>
) : Parcelable

@Parcelize
data class DriverLogData(
    val id: String,
    @SerializedName("car_plate_no")
    val carPlateNo: String,
    val type: String,
    @SerializedName("trip_type_id")
    val tripTypeId: String,
    val from: String,
    val to: String,
    val purpose: String,
    val date: String,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String?,
    @SerializedName("customer_id")
    val customerId: String,
    @SerializedName("customer_type")
    val customerType: String,
    val status: String,
    @SerializedName("cooperate_driver_id")
    val cooperateDriverId: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("start_km")
    val startKm: String,
    @SerializedName("end_km")
    val endKm: String?,
    @SerializedName("confirmed_id")
    val confirmedId: String?,
    @SerializedName("trip_type")
    val tripType: String

) : Parcelable


@Parcelize
data class Document(
    val id: String,
    @SerializedName("document_name")
    val documentName: String,
    @SerializedName("file_name")
    val fileName: String,
    @SerializedName("document_url")
    val documentUrl: String,
    @SerializedName("documentable_type")
    val documentableType: String,
    @SerializedName("documentable_id")
    val documentableId: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("kind_of_doc")
    val kindOfDoc: String
) : Parcelable
