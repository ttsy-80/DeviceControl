package com.devicecontrol.engine.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "config_items",
    foreignKeys = [
        ForeignKey(
            entity = EngineModel::class,
            parentColumns = ["id"],
            childColumns = ["modelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["modelId"])]
)
data class ConfigItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val modelId: Long,
    val gearRatio: Double,
    val position: String,
    val bladeCount: Int,
    val jogCount: Int
)
