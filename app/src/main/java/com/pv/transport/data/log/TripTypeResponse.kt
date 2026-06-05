package com.pv.transport.data.log

data class TripTypeResponse(
    val data: List<TripType>
)

data class TripType(
    val id: String,
    val value: String
)