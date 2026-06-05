package com.pv.transport.data.log

import com.google.gson.annotations.SerializedName

data class GenerateQRResponse(
    @SerializedName("qr_url")
    val qrUrl: String,
    val token: String
)