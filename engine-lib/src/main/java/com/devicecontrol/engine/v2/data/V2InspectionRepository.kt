package com.devicecontrol.engine.v2.data

import com.devicecontrol.engine.data.database.dao.V2InspectionConfigDao
import com.devicecontrol.engine.data.model.Task
import com.devicecontrol.engine.data.model.TaskExecution
import com.devicecontrol.engine.data.model.V2AutoInspectionConfig
import com.devicecontrol.engine.data.model.V2InspectionPrefs
import com.devicecontrol.engine.data.model.V2ManualInspectionConfig
import com.devicecontrol.engine.data.repository.TaskRepository
import com.devicecontrol.engine.v2.viewmodel.V2UiOperationMode

/**
 * V2 检测页：隐式任务、自动/手动配置实体、上次 UI 模式。
 */
class V2InspectionRepository(
    private val taskRepository: TaskRepository,
    private val configDao: V2InspectionConfigDao,
) {

    suspend fun findOrCreateV2InspectionTask(
        modelId: Long,
        modelName: String,
        configItemIds: List<Long>,
        configItemCount: Int,
    ): Long {
        val expected = configItemIds.sorted()
        val existing = taskRepository.getTasksByModelId(modelId)
            .filter {
                it.source == SOURCE_V2_INSPECTION && it.configItemIds.sorted() == expected
            }
            .maxByOrNull { it.id }
        if (existing != null) return existing.id

        val taskId = taskRepository.insertTask(
            Task(
                modelId = modelId,
                modelName = modelName,
                configItemIds = configItemIds,
                source = SOURCE_V2_INSPECTION,
            ),
        )
        applyInitialExecutions(taskId, configItemCount)
        return taskId
    }

    suspend fun getOrCreateAutoConfig(modelId: Long): V2AutoInspectionConfig {
        val raw = configDao.getAutoConfig(modelId)
        if (raw != null) {
            val clamped = raw.clamped()
            if (clamped != raw) {
                configDao.upsertAutoConfig(clamped)
            }
            return clamped
        }
        val defaults = V2AutoInspectionConfig.defaults(modelId)
        configDao.upsertAutoConfig(defaults)
        return defaults
    }

    suspend fun getOrCreateManualConfig(modelId: Long): V2ManualInspectionConfig {
        val stored = configDao.getManualConfig(modelId)
        if (stored != null) return stored
        val defaults = V2ManualInspectionConfig.defaults(modelId)
        configDao.upsertManualConfig(defaults)
        return defaults
    }

    suspend fun saveAutoConfig(config: V2AutoInspectionConfig) {
        configDao.upsertAutoConfig(config.clamped())
    }

    suspend fun saveManualConfig(config: V2ManualInspectionConfig) {
        configDao.upsertManualConfig(config)
    }

    suspend fun getLastUiMode(modelId: Long): V2UiOperationMode {
        val prefs = configDao.getPrefs(modelId) ?: return V2UiOperationMode.AUTO
        return when (prefs.lastUiMode) {
            V2InspectionPrefs.MODE_MANUAL -> V2UiOperationMode.MANUAL
            else -> V2UiOperationMode.AUTO
        }
    }

    suspend fun saveLastUiMode(modelId: Long, mode: V2UiOperationMode) {
        val code = when (mode) {
            V2UiOperationMode.AUTO -> V2InspectionPrefs.MODE_AUTO
            V2UiOperationMode.MANUAL -> V2InspectionPrefs.MODE_MANUAL
        }
        configDao.upsertPrefs(V2InspectionPrefs(modelId = modelId, lastUiMode = code))
    }

    private suspend fun applyInitialExecutions(newTaskId: Long, configItemCount: Int) {
        for (index in 0 until configItemCount) {
            taskRepository.insertOrUpdateTaskExecution(
                TaskExecution(
                    taskId = newTaskId,
                    gearRatioIndex = index,
                    speed = 300.0,
                    speedStep = 5.0,
                    continuousCycles = 1,
                    jogInterval = 1,
                    playbackSpeed = TaskExecution.DEFAULT_PLAYBACK_SEC_PER_REV,
                ),
            )
        }
    }

    companion object {
        const val SOURCE_V2_INSPECTION = "v2_inspection"
    }
}
