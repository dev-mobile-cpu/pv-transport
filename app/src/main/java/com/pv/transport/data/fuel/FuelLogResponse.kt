package com.pv.transport.data.fuel

import com.google.gson.annotations.SerializedName

data class FuelLogResponse(
    @SerializedName("data")
    val data: List<FuelLogData>,
    val links: Links,
    val meta: Meta
)

data class FuelLogData(
    val id: String,
    val date: String,
    @SerializedName("car_plate_no")
    val carPlateNo: String,
    @SerializedName("fuel_shop")
    val fuelShop: String,
    @SerializedName("fuel_amount")
    val fuelAmount: String,
    @SerializedName("fuel_liter")
    val fuelLiter: String,
    val customer: String,
    @SerializedName("fuel_type")
    val fuelType: String,
    val status: String,
    @SerializedName("documents")
    val documents: List<FuelDocument>,
    @SerializedName("current_km")
    val currentKm: String,
    @SerializedName("current_km_photo")
    val currentKmPhoto: CurrentKmPhoto
)

data class FuelDocument(
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
    val documentableId: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("kind_of_doc")
    val kindOfDoc: String
)

data class CurrentKmPhoto(
    @SerializedName("file_name")
    val fileName: String,
    @SerializedName("photo_url")
    val photoUrl: String,
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String
)
