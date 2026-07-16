package com.pv.transport.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "corporate_user_cache")
data class CorporateUserCacheEntity(
    @PrimaryKey
    val id: Int,
    val corporateId: Int,
    val name: String,
    val phone: String,
    val email: String,
    val firstTimeLogin: String
)
