package com.pv.transport.data

import com.google.gson.annotations.SerializedName

data class GenerateQR(
    @SerializedName("log_ids")
    val logIds: List<Int>,

    @SerializedName("corporate_user_id")
    val corporateUserId: String,

    @SerializedName("actual_user")
    val actualUser: String
)
