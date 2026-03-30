package com.pv.transport.data

data class TripTypeResponse(
    val data: List<TripType>
)

data class TripType(
    val id: String,
    val value: String
)
