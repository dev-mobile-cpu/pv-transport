package com.pv.transport.data.log

data class TripLog(
    val title: String,
    val date: String,
    val timeRange: String,
    val startKm: String,
    val endKm: String,
    val distance: String,
    val status: String,
    val tag: String,
    val reviewInfo: String? = null
)