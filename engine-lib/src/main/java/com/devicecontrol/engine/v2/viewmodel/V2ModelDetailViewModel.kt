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
    val showBottomDivider: Boolean = true,
    /** 列表刷新代次，追加行后强制重绑以更新行底橙线 */
    val listGeneration: Int = 0,
) {
    val id: String get() = configItemId.toString()
}

class V2ModelDetailViewModel(
    private val repository: EngineRepository,
) : ViewModel() {

    private var modelNameKey: String = ""
    private var engineModel: EngineModel? = null
    private var configItems: List<ConfigItem> = emptyList()
    private var listGeneration: Int = 0

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

    private val _saveSuccess = MutableLiveData(false)
    val saveSuccess: LiveData<Boolean> = _saveSuccess

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
        publishEngineFields(editable = true)
    }

    fun exitTableEdit(
        save: Boolean,
        tableRows: List<V2ModelDetailRowUi>?,
        engineValues: Map<String, String>? = null,
    ) {
        V2Log.i(TAG, "exitTableEdit save=$save")
        if (!save) {
            _pageMode.value = V2ModelDetailPageMode.VIEW
            _rows.value = _rows.value?.map { it.copy(isRowEditing = false) }
            publishEngineFields(editable = false)
            return
        }
        if (tableRows == null) return
        viewModelScope.launch {
            _errorMessage.value = null
            try {
                val model = engineModel ?: return@launch
                val safeTorque = engineValues?.get("safe_torque")?.trim().orEmpty()
                val gearRatioText = engineValues?.get("gear_ratio")?.trim().orEmpty()
                val gearRatio = gearRatioText.toDoubleOrNull()
                if (gearRatio == null || gearRatio <= 0) {
                    _errorMessage.value = V2ModelAddViewModel.MSG_INVALID_NUMBER
                    return@launch
                }
                for (row in tableRows) {
                    if (row.position.trim().isEmpty()) {
                        _errorMessage.value = V2ModelAddViewModel.MSG_POSITION_EMPTY
                        return@launch
                    }
                }
                if (model.safeTorque != safeTorque) {
                    repository.updateModel(model.copy(safeTorque = safeTorque))
                }
                val currentRatio = configItems.firstOrNull()?.gearRatio
                if (currentRatio != null && currentRatio != gearRatio) {
                    repository.updateAllConfigItemsGearRatioByModelId(model.id, gearRatio)
                }
                tableRows.forEach { row ->
                    val item = configItems.firstOrNull { it.id == row.configItemId } ?: return@forEach
                    val pos = row.position.trim()
                    if (item.position != pos || item.bladeCount != row.bladeCount) {
                        repository.updateConfigItem(
                            item.copy(position = pos, bladeCount = row.bladeCount),
                        )
                    }
                }
                _pageMode.value = V2ModelDetailPageMode.VIEW
                _saveSuccess.value = true
                reload()
            } catch (e: Exception) {
                V2Log.e(TAG, "exitTableEdit save failed", e)
                _errorMessage.value = "保存失败: ${e.message}"
            }
        }
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

    fun consumeSaveSuccess() {
        _saveSuccess.value = false
    }

    private fun applyLoaded(model: EngineModel, items: List<ConfigItem>) {
        engineModel = model
        configItems = items
        _modelName.value = model.name
        _imagePath.value = model.imagePath
        val editing = _pageMode.value == V2ModelDetailPageMode.TABLE_EDIT
        publishEngineFieldsFromModel(model, items, editable = editing)
        listGeneration++
        _rows.value = items.map { item ->
            V2ModelDetailRowUi(
                configItemId = item.id,
                position = item.position,
                bladeCount = item.bladeCount,
                showBottomDivider = true,
                listGeneration = listGeneration,
            )
        }
        V2Log.i(TAG, "loaded model=${model.name} configs=${items.size}")
    }

    private fun publishEngineFields(editable: Boolean) {
        val model = engineModel ?: return
        publishEngineFieldsFromModel(model, configItems, editable = editable)
    }

    private fun publishEngineFieldsFromModel(
        model: EngineModel,
        items: List<ConfigItem>,
        editable: Boolean,
    ) {
        val gearRatioText = items.firstOrNull()?.gearRatio?.toString().orEmpty()
        _engineFields.value = listOf(
            V2ModelEngineFieldUi("safe_torque", "安全扭矩：", model.safeTorque, editable = editable),
            V2ModelEngineFieldUi("gear_ratio", "变速比：", gearRatioText, editable = editable),
        )
    }

    companion object {
        private const val TAG = "ModelDetailVM"
    }
}
