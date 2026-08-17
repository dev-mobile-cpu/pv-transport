package com.pv.transport.data.log

data class OtherExpenseResponse(
    val message: String,
    val data: OtherExpenseResponseData? = null
)

data class OtherExpenseResponseData(
    val id: String? = null
)