package com.pv.transport.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_check_outs")
data class OfflineCheckOutEntity(
    @PrimaryKey
    val uuid: String,
    // Set when check-in was already synced (server record id is known)
    val serverRecordId: String? = null,
    // Set when check-in is still pending (link to local check-in uuid)
    val localCheckInUuid: String? = null,
    val remark: String,
    val site: String = "",
    val purpose: String = "",
    val endTime: String,
    val endKm: String,
    val endPhotoPath: String,
    val clientTimestamp: Long,
    val isSynced: Boolean = false,
    val isSyncing: Boolean = false
)
