package com.devicecontrol.engine.v2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicecontrol.engine.data.repository.EngineRepository
import com.devicecontrol.engine.v2.log.V2Log
import com.devicecontrol.engine.v2.model.V2ModelAppendArgs
import kotlinx.coroutines.launch

class V2ModelAddViewModel(
    private val repository: EngineRepository,
) : ViewModel() {

    private val _modelName = MutableLiveData("")
    val modelName: LiveData<String> = _modelName

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _saveSuccess = MutableLiveData(false)
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private var appendArgs: V2ModelAppendArgs? = null

    fun initNewModel() {
        appendArgs = null
        _modelName.value = ""
    }

    fun initAppendConfig(args: V2ModelAppendArgs) {
        appendArgs = args
        _modelName.value = args.modelName
    }

    fun isAppendMode(): Boolean = appendArgs != null

    fun updateModelNameTitle(name: String) {
        if (appendArgs != null) return
        _modelName.value = name
    }

    fun confirm(
        modelName: String,
        safeTorque: String,
        gearRatioText: String,
        position: String,
        bladeText: String,
    ) {
        viewModelScope.launch {
            _errorMessage.value = null
            val append = appendArgs
            if (append != null) {
                confirmAppendConfig(append, gearRatioText, position, bladeText)
                return@launch
            }
            confirmNewModel(modelName, safeTorque, gearRatioText, position, bladeText)
        }
    }

    private suspend fun confirmAppendConfig(
        append: V2ModelAppendArgs,
        gearRatioText: String,
        position: String,
        bladeText: String,
    ) {
        val gearRatio = gearRatioText.trim().toDoubleOrNull() ?: append.gearRatio
        if (gearRatio <= 0) {
            _errorMessage.value = MSG_INVALID_NUMBER
            return
        }
        val positionTrimmed = position.trim()
        if (positionTrimmed.isEmpty()) {
            _errorMessage.value = MSG_POSITION_EMPTY
            return
        }
        val bladeCount = bladeText.trim().toIntOrNull()
        if (bladeCount == null || bladeCount <= 0) {
            _errorMessage.value = MSG_INVALID_NUMBER
            return
        }
        try {
            val configId = repository.addConfigItemForModel(
                modelId = append.modelId,
                gearRatio = gearRatio,
                position = positionTrimmed,
                bladeCount = bladeCount,
            )
            V2Log.i(TAG, "append config success configId=$configId model=${append.modelName}")
            _saveSuccess.value = true
        } catch (e: Exception) {
            V2Log.e(TAG, "append config failed", e)
            _errorMessage.value = "操作失败: ${e.message}"
        }
    }

    private suspend fun confirmNewModel(
        modelName: String,
        safeTorque: String,
        gearRatioText: String,
        position: String,
        bladeText: String,
    ) {
        val name = modelName.trim()
        if (name.isEmpty()) {
            _errorMessage.value = MSG_NAME_EMPTY
            return
        }
        val existing = repository.getModelByName(name)
        if (existing != null) {
            _errorMessage.value = MSG_NAME_DUPLICATE
            return
        }
        val gearRatio = gearRatioText.trim().toDoubleOrNull()
        if (gearRatio == null || gearRatio <= 0) {
            _errorMessage.value = MSG_INVALID_NUMBER
            return
        }
        val positionTrimmed = position.trim()
        if (positionTrimmed.isEmpty()) {
            _errorMessage.value = MSG_POSITION_EMPTY
            return
        }
        val bladeCount = bladeText.trim().toIntOrNull()
        if (bladeCount == null || bladeCount <= 0) {
            _errorMessage.value = MSG_INVALID_NUMBER
            return
        }
        try {
            val modelId = repository.createModelWithFirstConfigItem(
                modelName = name,
                safeTorque = safeTorque.trim(),
                gearRatio = gearRatio,
                position = positionTrimmed,
                bladeCount = bladeCount,
                imagePath = null,
            )
            V2Log.i(TAG, "confirm success modelId=$modelId name=$name")
            _saveSuccess.value = true
        } catch (e: Exception) {
            V2Log.e(TAG, "confirm failed", e)
            _errorMessage.value = "操作失败: ${e.message}"
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun consumeSaveSuccess() {
        _saveSuccess.value = false
    }

    companion object {
        private const val TAG = "ModelAddVM"
        const val MSG_NAME_EMPTY = "型号名称不能为空"
        const val MSG_NAME_DUPLICATE = "型号名称已存在"
        const val MSG_INVALID_NUMBER = "请输入有效的数值"
        const val MSG_POSITION_EMPTY = "位置不能为空"
    }
}
