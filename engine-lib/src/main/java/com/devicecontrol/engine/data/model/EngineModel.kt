package com.devicecontrol.engine.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "engine_models")
data class EngineModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)
