package com.pv.transport.data

import com.google.gson.annotations.SerializedName

data class SocketResponse(
    val token: String,
    @SerializedName("corporate_driver_id")
    val corporateDriverId: Int,
    @SerializedName("driver_log_ids")
    val driverLogIds: List<Int>
)