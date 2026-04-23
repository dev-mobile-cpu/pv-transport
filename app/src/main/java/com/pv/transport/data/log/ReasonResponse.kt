package com.pv.transport.data.log

data class ReasonResponse(
    val data: List<ReasonListResponse>
)

data class ReasonListResponse(
    val id: String,
    val value: String
)
