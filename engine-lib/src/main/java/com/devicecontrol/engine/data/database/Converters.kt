package com.devicecontrol.engine.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromDoubleList(value: List<Double>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toDoubleList(value: String): List<Double> {
        val listType = object : TypeToken<List<Double>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromLongList(value: List<Long>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        val listType = object : TypeToken<List<Long>>() {}.type
        return gson.fromJson(value, listType)
    }
}
