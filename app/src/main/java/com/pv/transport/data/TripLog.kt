package com.pv.transport.data

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