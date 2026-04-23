package com.pv.transport.data.fuel

import com.google.gson.annotations.SerializedName

data class WalletResponse(
    val data: WalletData
)

data class WalletData(
    val currency: String,
    val cash: Balance,
    val credit: Balance,
    val transactions: TransactionWrapper
)

data class Balance(
    val total: String,
    val earmarked: String,
    val available: String
)

data class TransactionWrapper(
    val data: List<Transaction>,
    val links: Links,
    val meta: Meta
)

data class Transaction(
    val id: String,
    val type: String,
    val category: String,
    val bucket: String,
    val amount: String,
    @SerializedName("reference_type")
    val referenceType: String,
    @SerializedName("reference_id")
    val referenceId: String,
    @SerializedName("created_at")
    val createdAt: String
)
