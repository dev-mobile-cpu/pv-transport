package com.pv.transport.data.fuel

import com.google.gson.annotations.SerializedName

data class GeneralResponse(
    val message: String? = null,
    val error: String? = null,
    val success: Boolean? = null
)
