package com.pv.transport.data

import com.google.gson.annotations.SerializedName

data class RefreshResponse(
    val token: String,
    @SerializedName("expires_at")
    val expiresAt: String,
    val driver: Driver
)
