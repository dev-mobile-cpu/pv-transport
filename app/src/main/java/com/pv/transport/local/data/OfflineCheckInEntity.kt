package com.pv.transport.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_check_ins")
data class OfflineCheckInEntity(
    @PrimaryKey
    val uuid: String,
    val date: String,
    val type: String,
    val site: String,
    val reason: String,
    val remark: String,
    val startTime: String,
    val startKm: String,
    val startPhotoPath: String,
    // Trip-type fields (null for Daily type)
    val tripTypeId: String? = null,
    val fromLocation: String? = null,
    val toLocation: String? = null,
    val purpose: String? = null,
    val clientTimestamp: Long,
    val isSynced: Boolean = false,
    val isSyncing: Boolean = false,
    val serverRecordId: String? = null
)
