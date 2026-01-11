package com.devicecontrol.engine.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicecontrol.engine.data.model.ConfigItem
import com.devicecontrol.engine.data.model.EngineModelWithConfigItems
import com.devicecontrol.engine.data.model.Task
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
                onSuccess(taskId)
            } catch (e: Exception) {
                onError("创建任务失败: ${e.message}")
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
