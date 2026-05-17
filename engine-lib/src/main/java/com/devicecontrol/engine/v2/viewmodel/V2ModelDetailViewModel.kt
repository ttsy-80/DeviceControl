package com.devicecontrol.engine.v2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devicecontrol.engine.v2.log.V2Log
import com.devicecontrol.engine.v2.model.V2ModelEngineFieldUi
import java.util.UUID

enum class V2ModelDetailPageMode {
    VIEW,
    TABLE_EDIT,
}

data class V2ModelDetailRowUi(
    val id: String,
    val position: String,
    val bladeCount: Int,
    val isRowEditing: Boolean = false,
)

/**
 * 型号详情/编辑（P15～P17）UI 状态，后续对接 Room EngineModel / ConfigItem。
 */
class V2ModelDetailViewModel : ViewModel() {

    private val _modelName = MutableLiveData("CFM56-3B")
    val modelName: LiveData<String> = _modelName

    private val _engineFields = MutableLiveData<List<V2ModelEngineFieldUi>>()
    val engineFields: LiveData<List<V2ModelEngineFieldUi>> = _engineFields

    private val _rows = MutableLiveData<List<V2ModelDetailRowUi>>()
    val rows: LiveData<List<V2ModelDetailRowUi>> = _rows

    private val _pageMode = MutableLiveData(V2ModelDetailPageMode.VIEW)
    val pageMode: LiveData<V2ModelDetailPageMode> = _pageMode

    fun init(modelName: String) {
        V2Log.i(TAG, "init model=$modelName")
        _modelName.value = modelName
        _pageMode.value = V2ModelDetailPageMode.VIEW
        _engineFields.value = listOf(
            V2ModelEngineFieldUi("safe_torque", "安全力矩：", "1480 lb·ft", editable = false),
            V2ModelEngineFieldUi("gear_ratio", "变速比：", "0.5", editable = false),
            V2ModelEngineFieldUi("position", "位置：", "实际位置", editable = false),
        )
        _rows.value = List(6) { index ->
            V2ModelDetailRowUi(
                id = "row_$index",
                position = "LPC1",
                bladeCount = 45,
            )
        }
    }

    fun enterTableEdit() {
        V2Log.i(TAG, "enterTableEdit")
        _pageMode.value = V2ModelDetailPageMode.TABLE_EDIT
        _rows.value = _rows.value?.map { it.copy(isRowEditing = false) }
    }

    fun exitTableEdit(save: Boolean) {
        V2Log.i(TAG, "exitTableEdit save=$save")
        _pageMode.value = V2ModelDetailPageMode.VIEW
        _rows.value = _rows.value?.map { it.copy(isRowEditing = false) }
    }

    fun startRowEdit(rowId: String) {
        _rows.value = _rows.value?.map {
            it.copy(isRowEditing = it.id == rowId)
        }
    }

    fun saveRowEdit(rowId: String, position: String, bladeCount: Int) {
        _rows.value = _rows.value?.map {
            if (it.id == rowId) {
                it.copy(position = position, bladeCount = bladeCount, isRowEditing = false)
            } else {
                it.copy(isRowEditing = false)
            }
        }
        V2Log.d(TAG, "saveRowEdit $rowId -> $position / $bladeCount")
    }

    fun deleteRow(rowId: String) {
        _rows.value = _rows.value?.filterNot { it.id == rowId }
        V2Log.d(TAG, "deleteRow $rowId")
    }

    fun duplicateRow(rowId: String) {
        val list = _rows.value?.toMutableList() ?: return
        val source = list.firstOrNull { it.id == rowId } ?: return
        list.add(
            source.copy(
                id = UUID.randomUUID().toString(),
                isRowEditing = false,
            ),
        )
        _rows.value = list
        V2Log.d(TAG, "duplicateRow from $rowId")
    }

    fun updateRowsFromEditor(updated: List<V2ModelDetailRowUi>) {
        _rows.value = updated
    }

    fun onImportImage() {
        V2Log.i(TAG, "onImportImage")
    }

    companion object {
        private const val TAG = "ModelDetailVM"
    }
}
