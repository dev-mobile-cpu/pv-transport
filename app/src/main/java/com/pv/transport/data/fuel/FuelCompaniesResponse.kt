package com.pv.transport.data.fuel

data class FuelCompaniesResponse(
    val data: List<FuelCompany>
)
data class FuelCompany(
    val id: Int,
    val name: String,
    val phone: String,
    val email: String,
    val address: String
)
