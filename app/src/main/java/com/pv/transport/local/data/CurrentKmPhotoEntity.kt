package com.pv.transport.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_km_photos")
data class CurrentKmPhotoEntity(
    @PrimaryKey
    val uuid: String,
    val fileName: String,
    val photoUrl: String,
    val thumbnailUrl: String
)
