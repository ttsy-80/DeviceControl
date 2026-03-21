package com.devicecontrol.engine.data.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * 机型及其关联配置项的查询结果对象（非独立表）。
 *
 * 用于 Room 一次查询带出 [EngineModel] 与全部 [ConfigItem]，
 * 便于列表/详情页展示「某机型 + 其下所有档位配置」。
 */
data class EngineModelWithConfigItems(
    /** 内嵌的机型主实体 */
    @Embedded
    val model: EngineModel,
    /** 该机型下的配置项列表，按 [ConfigItem.modelId] = [EngineModel.id] 关联 */
    @Relation(
        parentColumn = "id",
        entityColumn = "modelId"
    )
    val configItems: List<ConfigItem>
)
