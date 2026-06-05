package com.pv.transport.data.fuel

import com.google.gson.annotations.SerializedName
import retrofit2.http.Query

data class ShowFuelRequest(
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String,
    val page: Int?= null,
    @SerializedName("per_page")
    val perPage: Int = 20,
)
