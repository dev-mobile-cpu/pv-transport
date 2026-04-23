package com.pv.transport.data.fuel

import com.google.gson.annotations.SerializedName

data class Meta(
    @SerializedName("current_page")
    val currentPage: String,
    val from: String,
    @SerializedName("last_page")
    val lastPage: String,
    val path: String,
    @SerializedName("per_page")
    val perPage: String,
    val to: String,
    val total: String
)