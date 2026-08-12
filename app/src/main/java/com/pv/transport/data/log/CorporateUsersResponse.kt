package com.pv.transport.data.log

import com.google.gson.annotations.SerializedName

data class CorporateUsersResponse(
    val id: String,
    @SerializedName("corporate_id")
    val corporateId: String,
    val name: String,
    val phone: String?,
    val email: String?,
    @SerializedName("pin_set")
    val pinSet: String
)