package com.pv.transport.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_documents")
data class FuelDocumentEntity(
    @PrimaryKey
    val id: String,
    val fuelLogUuid: String, // OfflineFuelLogEntity.uuid နဲ့ link
    val documentName: String,
    val fileName: String,
    val documentUrl: String,
    val documentableType: String,
    val documentableId: Int,
    val createdAt: String,
    val updatedAt: String,
    val kindOfDoc: String
)
