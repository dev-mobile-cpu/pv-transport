package com.pv.transport.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.pv.transport.data.log.Link
import com.pv.transport.data.log.Meta
import kotlinx.parcelize.Parcelize

data class AllOtherExpense(
    val data: List<ExpenseData>,
    val links: Link,
    val meta: Meta
)

@Parcelize
data class ExpenseData(
    val id: String,
    val uuid: String? = null,   // Offline unique id,Server response မှာ မပါရင် null ဖြစ်မယ်
    val date: String,
    @SerializedName("license_plate")
    val licensePlate: String,
    @SerializedName("type_of_cost_id")
    val typeOfCostId: String,
    val typeOfCost: CostType,
    val amount: String,
    val documents: List<ExpenseDocument>,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val isSynced: Boolean = true      // Offline / Online state
): Parcelable
@Parcelize
data class ExpenseDocument(
    val id: String,
    @SerializedName("document_name")
    val documentName: String,
    @SerializedName("file_name")
    val fileName: String,
    @SerializedName("document_url")
    val documentUrl: String,
    @SerializedName("documentable_type")
    val documentableType: String,
    @SerializedName("documentable_id")
    val documentableId: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("kind_of_doc")
    val kindOfDoc: String
): Parcelable