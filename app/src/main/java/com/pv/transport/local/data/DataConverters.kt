package com.pv.transport.local.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pv.transport.data.ExpenseData
import com.pv.transport.data.log.Data

class DataConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromDriverLogList(value: List<Data>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toDriverLogList(value: String): List<Data> {
        val listType = object : TypeToken<List<Data>>() {}.type
        return gson.fromJson(value, listType)
    }

    // ExpenseData List -> JSON
    @TypeConverter
    fun fromExpenseDataList(value: List<ExpenseData>): String {
        return gson.toJson(value)
    }

    // JSON -> ExpenseData List
    @TypeConverter
    fun toExpenseDataList(value: String): List<ExpenseData> {
        val listType = object : TypeToken<List<ExpenseData>>() {}.type
        return gson.fromJson(value, listType)
    }
}
