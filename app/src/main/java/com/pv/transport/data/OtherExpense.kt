package com.pv.transport.data

import com.google.gson.annotations.SerializedName

data class OtherExpense(
    val id: String,
    val date: String,
    @SerializedName("type_of_cost_id")
    val typeOfCostId: Int,
    val amount: Int,
    val files: List<String>,
    @SerializedName("delete_docs")
    val deleteDocs: List<String>
)
