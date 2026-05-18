package com.devicecontrol.engine.v2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicecontrol.engine.data.model.ConfigItem
import com.devicecontrol.engine.data.model.EngineModel
import com.devicecontrol.engine.data.repository.EngineRepository
import com.devicecontrol.engine.v2.log.V2Log
import com.devicecontrol.engine.v2.model.V2ModelAppendArgs
import com.devicecontrol.engine.v2.model.V2ModelEngineFieldUi
import kotlinx.coroutines.launch

enum class V2ModelDetailPageMode {
    VIEW,
    TABLE_EDIT,
}

data class V2ModelDetailRowUi(
    val configItemId: Long,
    val position: String,
    val bladeCount: Int,
    val isRowEditing: Boolean = false,
) {
    val id: String get() = configItemId.toString()
}

class V2ModelDetailViewModel(
    private val repository: EngineRepository,
) : ViewModel() {

    private var modelNameKey: String = ""
    private var engineModel: EngineModel? = null
    private var configItems: List<ConfigItem> = emptyList()

    private val _modelName = MutableLiveData("")
    val modelName: LiveData<String> = _modelName

    private val _engineFields = MutableLiveData<List<V2ModelEngineFieldUi>>()
    val engineFields: LiveData<List<V2ModelEngineFieldUi>> = _engineFields

    private val _imagePath = MutableLiveData<String?>(null)
    val imagePath: LiveData<String?> = _imagePath

    private val _rows = MutableLiveData<List<V2ModelDetailRowUi>>()
    val rows: LiveData<List<V2ModelDetailRowUi>> = _rows

    private val _pageMode = MutableLiveData(V2ModelDetailPageMode.VIEW)
    val pageMode: LiveData<V2ModelDetailPageMode> = _pageMode

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _modelRemoved = MutableLiveData(false)
    val modelRemoved: LiveData<Boolean> = _modelRemoved

    fun init(modelName: String) {
        modelNameKey = modelName
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            _errorMessage.value = null
            val data = repository.getModelWithConfigItemsByName(modelNameKey)
            if (data == null) {
                V2Log.w(TAG, "reload: model not found name=$modelNameKey")
                _errorMessage.value = "未找到型号"
                return@launch
            }
            applyLoaded(data.model, data.configItems)
        }
    }

    fun getAppendArgs(): V2ModelAppendArgs? {
        val model = engineModel ?: return null
        val ratio = configItems.firstOrNull()?.gearRatio ?: return null
        return V2ModelAppendArgs(
            modelId = model.id,
            modelName = model.name,
            safeTorque = model.safeTorque,
            gearRatio = ratio,
            imagePath = model.imagePath,
        )
    }

    fun enterTableEdit() {
        V2Log.i(TAG, "enterTableEdit")
        _pageMode.value = V2ModelDetailPageMode.TABLE_EDIT
        _rows.value = _rows.value?.map { it.copy(isRowEditing = false) }
    }

    fun exitTableEdit(save: Boolean, tableRows: List<V2ModelDetailRowUi>?) {
        V2Log.i(TAG, "exitTableEdit save=$save")
        if (save && tableRows != null) {
            viewModelScope.launch {
                try {
                    tableRows.forEach { row ->
                        val item = configItems.firstOrNull { it.id == row.configItemId } ?: return@forEach
                        val pos = row.position.trim()
                        if (pos.isEmpty()) return@launch
                        if (item.position != pos || item.bladeCount != row.bladeCount) {
                            repository.updateConfigItem(
                                item.copy(position = pos, bladeCount = row.bladeCount),
                            )
                        }
                    }
                    reload()
                } catch (e: Exception) {
                    V2Log.e(TAG, "exitTableEdit save failed", e)
                    _errorMessage.value = "保存失败: ${e.message}"
                }
            }
        }
        _pageMode.value = V2ModelDetailPageMode.VIEW
        _rows.value = _rows.value?.map { it.copy(isRowEditing = false) }
    }

    fun deleteRow(configItemId: Long) {
        viewModelScope.launch {
            val item = configItems.firstOrNull { it.id == configItemId } ?: return@launch
            try {
                val modelDeleted = repository.deleteConfigItem(item)
                if (modelDeleted) {
                    _modelRemoved.value = true
                } else {
                    reload()
                }
                V2Log.d(TAG, "deleteRow id=$configItemId modelDeleted=$modelDeleted")
            } catch (e: Exception) {
                V2Log.e(TAG, "deleteRow failed", e)
                _errorMessage.value = "删除失败: ${e.message}"
            }
        }
    }

    fun duplicateRow(configItemId: Long) {
        viewModelScope.launch {
            val source = configItems.firstOrNull { it.id == configItemId } ?: return@launch
            try {
                repository.insertConfigItem(
                    source.copy(
                        id = 0,
                        position = source.position,
                        bladeCount = source.bladeCount,
                    ),
                )
                reload()
                V2Log.d(TAG, "duplicateRow from $configItemId")
            } catch (e: Exception) {
                V2Log.e(TAG, "duplicateRow failed", e)
                _errorMessage.value = "复制失败: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun consumeModelRemoved() {
        _modelRemoved.value = false
    }

    private fun applyLoaded(model: EngineModel, items: List<ConfigItem>) {
        engineModel = model
        configItems = items
        _modelName.value = model.name
        _imagePath.value = model.imagePath
        val gearRatioText = items.firstOrNull()?.gearRatio?.toString().orEmpty()
        _engineFields.value = listOf(
            V2ModelEngineFieldUi("safe_torque", "安全力矩：", model.safeTorque, editable = false),
            V2ModelEngineFieldUi("gear_ratio", "变速比：", gearRatioText, editable = false),
        )
        _rows.value = items.map { item ->
            V2ModelDetailRowUi(
                configItemId = item.id,
                position = item.position,
                bladeCount = item.bladeCount,
            )
        }
        V2Log.i(TAG, "loaded model=${model.name} configs=${items.size}")
    }

    companion object {
        private const val TAG = "ModelDetailVM"
    }
}
