package com.devicecontrol.engine.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.devicecontrol.engine.data.database.Converters

@Entity(tableName = "tasks")
@TypeConverters(Converters::class)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val modelId: Long,
    val modelName: String,
    val gearRatios: List<Double>
)
