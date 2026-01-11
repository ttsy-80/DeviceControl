package com.devicecontrol.engine.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class EngineModelWithConfigItems(
    @Embedded
    val model: EngineModel,
    @Relation(
        parentColumn = "id",
        entityColumn = "modelId"
    )
    val configItems: List<ConfigItem>
)
