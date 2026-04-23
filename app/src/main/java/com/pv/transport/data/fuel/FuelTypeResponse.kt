package com.pv.transport.data.fuel

import com.google.gson.annotations.SerializedName

data class FuelTypeResponse (
    @field:SerializedName("data")
    val records: List<FuelType>,
)