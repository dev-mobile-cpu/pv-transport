package com.pv.transport.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_other_expenses")
data class OfflineOtherExpenseEntity(
    @PrimaryKey
    val uuid: String,
    val date: String,
    val typeOfCostId: String,
    val amount: String,
    val licensePlate: String,
    val filesPaths: String,           // JSON array of absolute file paths
    val clientTimestamp: Long,
    val isSynced: Boolean = false
)
