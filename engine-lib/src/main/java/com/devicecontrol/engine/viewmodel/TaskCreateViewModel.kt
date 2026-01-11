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
    
    private val _modelsWithConfigItems = MutableLiveData<List<EngineModelWithConfigItems>>()
    val modelsWithConfigItems: LiveData<List<EngineModelWithConfigItems>> = _modelsWithConfigItems
    
    private val _selectedModel = MutableLiveData<EngineModelWithConfigItems?>()
    val selectedModel: LiveData<EngineModelWithConfigItems?> = _selectedModel
    
    private val _availableGearRatios = MutableLiveData<List<ConfigItem>>()
    val availableGearRatios: LiveData<List<ConfigItem>> = _availableGearRatios
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    init {
        loadModels()
    }
    
    private fun loadModels() {
        viewModelScope.launch {
            engineRepository.getAllModelsWithConfigItems().collect { models ->
                val modelsWithItems = models.filter { it.configItems.isNotEmpty() }
                _modelsWithConfigItems.postValue(modelsWithItems)
            }
        }
    }
    
    fun selectModel(model: EngineModelWithConfigItems) {
        _selectedModel.value = model
        _availableGearRatios.value = model.configItems
    }
    
    fun createTask(
        selectedGearRatios: List<Double>,
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
                
                if (selectedGearRatios.isEmpty()) {
                    onError("请至少选择一个变速比")
                    return@launch
                }
                
                // 验证选择的变速比是否属于该型号
                val validGearRatios = model.configItems.map { it.gearRatio }
                val invalidRatios = selectedGearRatios.filter { it !in validGearRatios }
                if (invalidRatios.isNotEmpty()) {
                    onError("选择的变速比不属于该型号")
                    return@launch
                }
                
                val task = Task(
                    modelId = model.model.id,
                    modelName = model.model.name,
                    gearRatios = selectedGearRatios
                )
                
                val taskId = taskRepository.insertTask(task)
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
