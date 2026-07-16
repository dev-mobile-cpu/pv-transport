package com.pv.transport.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_company_cache")
data class FuelCompanyCacheEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val phone: String,
    val email: String,
    val address: String
)
