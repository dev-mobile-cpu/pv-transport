package com.pv.transport.data.fuel

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "transaction_record")
data class TransactionRecord  (
        @PrimaryKey
        val id: Int,
        val date: String,
        @field:SerializedName("license_plate")
        val licensePlate: String,
        val code: String,
        @field:SerializedName("driver_name")
        val driverName: String,
        val station: String,
        @field:SerializedName("fuel_type")
        val fuelType: String,
        val liter: Double,
        val amount: Double,
        val status: String,
        val type: String,
        @field:SerializedName("payslip_uploaded")
        val payslipUploaded: String
)