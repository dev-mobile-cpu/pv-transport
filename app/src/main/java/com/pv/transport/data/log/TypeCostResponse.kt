package com.pv.transport.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class TypeCostResponse(
    val data: List<CostType>
)


@Parcelize
data class CostType(
    val id: String,
    val name: String
): Parcelable