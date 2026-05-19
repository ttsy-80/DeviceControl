package com.devicecontrol.engine.v2.data

import com.devicecontrol.engine.data.database.dao.V2ModeSettingDao
import com.devicecontrol.engine.data.model.Task
import com.devicecontrol.engine.data.model.TaskExecution
import com.devicecontrol.engine.data.model.V2ModeSetting
import com.devicecontrol.engine.data.repository.TaskRepository
import com.devicecontrol.engine.v2.inspection.V2ModeSettingsSnapshot
import com.devicecontrol.engine.v2.viewmodel.V2StepperFieldUi

/**
 * V2 检测页数据：隐式任务、P8/P11 模式参数（Room）。
 */
class V2InspectionRepository(
    private val taskRepository: TaskRepository,
    private val modeSettingDao: V2ModeSettingDao,
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
        applyPreviousTaskConfigurations(modelId, taskId, configItemCount)
        return taskId
    }

    suspend fun loadModeSettings(
        modelId: Long,
        manual: Boolean,
        defaults: List<V2StepperFieldUi>,
    ): V2ModeSettingsSnapshot {
        if (modelId <= 0L) {
            return V2ModeSettingsSnapshot(defaults.associate { it.key to it.value })
        }
        val stored = modeSettingDao.getByModel(modelId, manual).associate { it.settingKey to it.value }
        val map = defaults.associate { field ->
            field.key to (stored[field.key] ?: field.value)
        }
        return V2ModeSettingsSnapshot(map)
    }

    suspend fun saveModeSettings(
        modelId: Long,
        manual: Boolean,
        fields: List<V2StepperFieldUi>,
    ) {
        if (modelId <= 0L) return
        modeSettingDao.deleteByModel(modelId, manual)
        modeSettingDao.upsertAll(
            fields.map { field ->
                V2ModeSetting(
                    modelId = modelId,
                    manual = manual,
                    settingKey = field.key,
                    value = field.value,
                )
            },
        )
    }

    private suspend fun applyPreviousTaskConfigurations(
        modelId: Long,
        newTaskId: Long,
        configItemCount: Int,
    ) {
        val previousExecution = taskRepository.getLatestTaskExecutionByModelId(modelId, newTaskId)
        for (index in 0 until configItemCount) {
            val execution = if (previousExecution != null) {
                TaskExecution(
                    taskId = newTaskId,
                    gearRatioIndex = index,
                    speed = previousExecution.speed,
                    speedStep = previousExecution.speedStep,
                    continuousCycles = previousExecution.continuousCycles,
                    jogInterval = previousExecution.jogInterval,
                    playbackSpeed = previousExecution.playbackSpeed,
                    operationMode = previousExecution.operationMode,
                    rotationDirection = previousExecution.rotationDirection,
                )
            } else {
                TaskExecution(
                    taskId = newTaskId,
                    gearRatioIndex = index,
                    speed = 300.0,
                    speedStep = 5.0,
                    continuousCycles = 1,
                    jogInterval = 1,
                    playbackSpeed = TaskExecution.DEFAULT_PLAYBACK_SEC_PER_REV,
                )
            }
            taskRepository.insertOrUpdateTaskExecution(execution)
        }
    }

    companion object {
        const val SOURCE_V2_INSPECTION = "v2_inspection"
    }
}
