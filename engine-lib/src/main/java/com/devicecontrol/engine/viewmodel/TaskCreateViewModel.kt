package com.devicecontrol.engine.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicecontrol.engine.data.model.ConfigItem
import com.devicecontrol.engine.data.model.EngineModelWithConfigItems
import com.devicecontrol.engine.data.model.Task
import com.devicecontrol.engine.data.model.TaskExecution
import com.devicecontrol.engine.data.repository.EngineRepository
import com.devicecontrol.engine.data.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskCreateViewModel(
    private val engineRepository: EngineRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {
    
    private val _modelsWithConfigItems = MutableLiveData<List<EngineModelWithConfigItems>>(emptyList())
    val modelsWithConfigItems: LiveData<List<EngineModelWithConfigItems>> = _modelsWithConfigItems
    
    private val _selectedModel = MutableLiveData<EngineModelWithConfigItems?>()
    val selectedModel: LiveData<EngineModelWithConfigItems?> = _selectedModel
    
    private val _availableGearRatios = MutableLiveData<List<ConfigItem>>(emptyList())
    val availableGearRatios: LiveData<List<ConfigItem>> = _availableGearRatios
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    init {
        loadModels()
    }
    
    private fun loadModels() {
        viewModelScope.launch {
            try {
                engineRepository.getAllModelsWithConfigItems().collect { models ->
                    // 过滤出有配置项的型号
                    val modelsWithItems = models.filter { it.configItems.isNotEmpty() }
                    // 确保在主线程更新LiveData
                    _modelsWithConfigItems.postValue(modelsWithItems)
                }
            } catch (e: Exception) {
                _errorMessage.postValue("加载型号数据失败: ${e.message}")
            }
        }
    }
    
    fun selectModel(model: EngineModelWithConfigItems?) {
        _selectedModel.value = model
        _availableGearRatios.value = model?.configItems ?: emptyList()
    }
    
    fun createTask(
        selectedConfigItemIds: List<Long>,
        onSuccess: (Long) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val model = _selectedModel.value
                if (model == null) {
                    onError("请选择型号")
                    return@launch
                }
                
                if (selectedConfigItemIds.isEmpty()) {
                    onError("请至少选择一个配置项")
                    return@launch
                }
                
                // 验证选择的配置项ID是否属于该型号
                val validConfigItemIds = model.configItems.map { it.id }.toSet()
                val invalidIds = selectedConfigItemIds.filter { it !in validConfigItemIds }
                if (invalidIds.isNotEmpty()) {
                    onError("选择的配置项不属于该型号")
                    return@launch
                }
                
                val task = Task(
                    modelId = model.model.id,
                    modelName = model.model.name,
                    configItemIds = selectedConfigItemIds
                )
                
                android.util.Log.d("TaskCreateViewModel", "Creating task: modelId=${task.modelId}, modelName=${task.modelName}, configItemIds=${task.configItemIds}, size=${task.configItemIds.size}")
                val taskId = taskRepository.insertTask(task)
                android.util.Log.d("TaskCreateViewModel", "Task created with id: $taskId")
                
                // 查找该型号下之前的任务配置，应用到新任务
                applyPreviousTaskConfigurations(model.model.id, taskId, selectedConfigItemIds.size)
                
                onSuccess(taskId)
            } catch (e: Exception) {
                onError("创建任务失败: ${e.message}")
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * 应用该型号下之前任务的配置到新创建的任务
     * 如果该型号下之前有任务配置了基础配置（speedStep/continuousCycles/jogInterval/playbackSpeed），
     * 需要把这些配置带到新创建任务中
     */
    private fun applyPreviousTaskConfigurations(modelId: Long, newTaskId: Long, configItemCount: Int) {
        viewModelScope.launch {
            try {
                val previousExecution = taskRepository.getLatestTaskExecutionByModelId(modelId, newTaskId)
                
                if (previousExecution != null) {
                    for (index in 0 until configItemCount) {
                        val execution = TaskExecution(
                            taskId = newTaskId,
                            gearRatioIndex = index,
                            speed = previousExecution.speed,
                            speedStep = 5.0,
                            continuousCycles = previousExecution.continuousCycles,
                            jogInterval = previousExecution.jogInterval,
                            playbackSpeed = previousExecution.playbackSpeed
                        )
                        taskRepository.insertOrUpdateTaskExecution(execution)
                    }
                    android.util.Log.d("TaskCreateViewModel", "Applied previous task configurations: speedStep=${previousExecution.speedStep}, continuousCycles=${previousExecution.continuousCycles}, jogInterval=${previousExecution.jogInterval}, playbackSpeed=${previousExecution.playbackSpeed}")
                } else {
                    for (index in 0 until configItemCount) {
                        val execution = TaskExecution(
                            taskId = newTaskId,
                            gearRatioIndex = index,
                            speed = 300.0,
                            speedStep = 5.0,
                            continuousCycles = 1,
                            jogInterval = 1,
                            playbackSpeed = TaskExecution.DEFAULT_PLAYBACK_SEC_PER_REV
                        )
                        taskRepository.insertOrUpdateTaskExecution(execution)
                    }
                    android.util.Log.d("TaskCreateViewModel", "No previous task configurations found, using default values")
                }
            } catch (e: Exception) {
                android.util.Log.e("TaskCreateViewModel", "Error applying previous task configurations: ${e.message}", e)
                for (index in 0 until configItemCount) {
                    val execution = TaskExecution(
                        taskId = newTaskId,
                        gearRatioIndex = index,
                        speed = 300.0,
                        speedStep = 5.0,
                        continuousCycles = 1,
                        jogInterval = 1,
                        playbackSpeed = TaskExecution.DEFAULT_PLAYBACK_SEC_PER_REV
                    )
                    taskRepository.insertOrUpdateTaskExecution(execution)
                }
            }
        }
    }
}
