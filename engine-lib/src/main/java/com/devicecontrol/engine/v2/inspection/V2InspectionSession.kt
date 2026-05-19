package com.devicecontrol.engine.v2.inspection

import com.devicecontrol.engine.data.model.ConfigItem
import com.devicecontrol.engine.data.model.EngineModel
import com.devicecontrol.engine.data.repository.EngineRepository
import com.devicecontrol.engine.v2.data.V2InspectionRepository

/**
 * 进入 P7 时确保隐式检测 [com.devicecontrol.engine.data.model.Task] 与配置项就绪。
 */
class V2InspectionSession(
    private val engineRepository: EngineRepository,
    private val inspectionRepository: V2InspectionRepository,
) {

    data class Loaded(
        val model: EngineModel,
        val configItems: List<ConfigItem>,
        val taskId: Long,
    )

    suspend fun load(modelId: Long): Loaded? {
        val data = engineRepository.getModelWithConfigItemsById(modelId) ?: return null
        if (data.configItems.isEmpty()) return null
        val taskId = inspectionRepository.findOrCreateV2InspectionTask(
            modelId = data.model.id,
            modelName = data.model.name,
            configItemIds = data.configItems.map { it.id },
            configItemCount = data.configItems.size,
        )
        return Loaded(
            model = data.model,
            configItems = data.configItems,
            taskId = taskId,
        )
    }
}
