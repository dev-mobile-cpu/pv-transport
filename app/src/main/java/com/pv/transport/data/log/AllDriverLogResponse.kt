package com.pv.transport.data.log

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
    @SerializedName("client_uuid")
    val clientUuid: String? = null,
    @SerializedName("checkout_client_uuid")
    val checkoutClientUuid: String? = null,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String? = null,
    val reason: String,
    val remark: String? = null,
    @SerializedName("start_km")
    val startKm: String,
    @SerializedName("end_km")
    val endKm: String? = null,
    @SerializedName("driver_log_id")
    val driverLogId: String? = null,
    val type: String,
    @SerializedName("trip_type_id")
    val tripTypeId: String? = null,
    val from: String? = null,
    val to: String? = null,
    val purpose: String? = null,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    val details: String? = null,
    @SerializedName("is_checkout")
    val isCheckout: String? = null,
    val status: String,
    @SerializedName("driver_log")
    val driverLog: DriverLogData? = null,
    val documents: List<Document> = emptyList(),
    @SerializedName("actual_user")
    val actualUser: String? = null,
    @SerializedName("corporate_user")
    val corporateUser: CorporateUser? = null,
    // Offline image paths
    val startImagePath: String? = null,
    val endImagePath: String? = null

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
    @SerializedName("is_disabled")
    val isDisabled: String,
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
    val tripType: String?
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


@Parcelize
data class CorporateUser(
    val id: String,
    @SerializedName("corporate_id")
    val corporateId: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null
) : Parcelable
