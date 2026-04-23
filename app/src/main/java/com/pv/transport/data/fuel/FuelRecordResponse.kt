package com.pv.transport.data.fuel

import com.google.gson.annotations.SerializedName

data class FuelRecordResponse (
    @field:SerializedName("current_page")
    val currentPage: Int,
    @field:SerializedName("data")
    val records: List<FuelRecord>,
    @field:SerializedName("count")
    val count: Int,
    @field:SerializedName("total")
    val total: Int
   )

data class FuelRecordData (
    @field:SerializedName("data") val data : List<FuelRecord>
)

data class FuelRecordRequestResponse (
    @field:SerializedName("data")
    val records: List<TransactionRecord>,
    @field:SerializedName("count")
    val count: Int
)