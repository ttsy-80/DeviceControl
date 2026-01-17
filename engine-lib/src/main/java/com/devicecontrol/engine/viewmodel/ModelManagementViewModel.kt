package com.devicecontrol.engine.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicecontrol.engine.data.model.ConfigItem
import com.devicecontrol.engine.data.model.EngineModel
import com.devicecontrol.engine.data.model.EngineModelWithConfigItems
import com.devicecontrol.engine.data.repository.EngineRepository
import kotlinx.coroutines.launch

class ModelManagementViewModel(private val repository: EngineRepository) : ViewModel() {
    
    private val _modelsWithConfigItems = MutableLiveData<List<EngineModelWithConfigItems>>()
    val modelsWithConfigItems: LiveData<List<EngineModelWithConfigItems>> = _modelsWithConfigItems
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _selectedModel = MutableLiveData<EngineModelWithConfigItems?>()
    val selectedModel: LiveData<EngineModelWithConfigItems?> = _selectedModel
    
    init {
        loadModels()
    }
    
    private fun loadModels() {
        viewModelScope.launch {
            repository.getAllModelsWithConfigItems().collect { models ->
                _modelsWithConfigItems.postValue(models)
            }
        }
    }
    
    fun selectModel(model: EngineModelWithConfigItems) {
        _selectedModel.value = model
    }
    
    fun createModelWithConfigItem(
        modelName: String,
        gearRatio: Double,
        position: String,
        bladeCount: Int,
        jogCount: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 验证型号名称
                if (modelName.isBlank()) {
                    onError("型号名称不能为空")
                    return@launch
                }
                
                // 检查型号名称是否已存在
                val existingModel = repository.getModelByName(modelName.trim())
                if (existingModel != null) {
                    onError("型号名称已存在")
                    return@launch
                }
                
                // 验证配置项数据（移除点动次数验证）
                if (gearRatio <= 0 || bladeCount <= 0) {
                    onError("请输入有效的数值")
                    return@launch
                }
                
                if (position.isBlank()) {
                    onError("位置不能为空")
                    return@launch
                }
                
                val model = EngineModel(name = modelName.trim())
                val configItem = ConfigItem(
                    modelId = 0, // 会在插入时更新
                    gearRatio = gearRatio,
                    position = position.trim(),
                    bladeCount = bladeCount,
                    jogCount = jogCount
                )
                
                repository.insertModelWithConfigItem(model, configItem)
                onSuccess()
            } catch (e: Exception) {
                onError("操作失败: ${e.message}")
            }
        }
    }
    
    fun addConfigItem(
        modelId: Long,
        gearRatio: Double,
        position: String,
        bladeCount: Int,
        jogCount: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 验证配置项数据（移除点动次数验证）
                if (gearRatio <= 0 || bladeCount <= 0) {
                    onError("请输入有效的数值")
                    return@launch
                }
                
                if (position.isBlank()) {
                    onError("位置不能为空")
                    return@launch
                }
                
                val configItem = ConfigItem(
                    modelId = modelId,
                    gearRatio = gearRatio,
                    position = position.trim(),
                    bladeCount = bladeCount,
                    jogCount = jogCount
                )
                
                repository.insertConfigItem(configItem)
                onSuccess()
            } catch (e: Exception) {
                onError("操作失败: ${e.message}")
            }
        }
    }
    
    fun deleteConfigItem(configItem: ConfigItem, onModelDeleted: () -> Unit) {
        viewModelScope.launch {
            try {
                val modelDeleted = repository.deleteConfigItem(configItem)
                if (modelDeleted) {
                    onModelDeleted()
                }
            } catch (e: Exception) {
                _errorMessage.value = "删除失败: ${e.message}"
            }
        }
    }
    
    fun deleteModel(model: EngineModel) {
        viewModelScope.launch {
            try {
                repository.deleteModel(model)
            } catch (e: Exception) {
                _errorMessage.value = "删除失败: ${e.message}"
            }
        }
    }
    
    fun updateConfigItem(
        configItem: ConfigItem,
        gearRatio: Double,
        position: String,
        bladeCount: Int,
        jogCount: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 验证配置项数据（移除点动次数验证）
                if (gearRatio <= 0 || bladeCount <= 0) {
                    onError("请输入有效的数值")
                    return@launch
                }
                
                if (position.isBlank()) {
                    onError("位置不能为空")
                    return@launch
                }
                
                // 如果变速比改变了，批量更新该型号下所有配置项的变速比
                if (configItem.gearRatio != gearRatio) {
                    repository.updateAllConfigItemsGearRatioByModelId(configItem.modelId, gearRatio)
                }
                
                // 更新当前配置项的其他字段（位置、叶片数，点动次数保留原值）
                val updatedConfigItem = configItem.copy(
                    gearRatio = gearRatio, // 使用新的变速比
                    position = position.trim(),
                    bladeCount = bladeCount,
                    jogCount = configItem.jogCount // 保留原值，不更新
                )
                repository.updateConfigItem(updatedConfigItem)
                
                onSuccess()
            } catch (e: Exception) {
                onError("操作失败: ${e.message}")
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
