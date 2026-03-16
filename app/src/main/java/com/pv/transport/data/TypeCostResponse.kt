package com.pv.transport.data

data class TypeCostResponse(
    val data: List<CostType>
)

data class CostType(
    val id: String,
    val name: String
)
