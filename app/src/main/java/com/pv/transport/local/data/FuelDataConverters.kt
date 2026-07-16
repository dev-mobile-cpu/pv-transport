package com.pv.transport.local.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pv.transport.data.fuel.FuelLogData
import com.pv.transport.data.log.Data

class FuelDataConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromFuelLogList(value: List<FuelLogData>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toFuelLogList(value: String): List<FuelLogData> {
        val listType = object : TypeToken<List<FuelLogData>>() {}.type
        return gson.fromJson(value, listType)
    }
}
