package com.devicecontrol.engine.v2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devicecontrol.engine.v2.log.V2Log
import com.devicecontrol.engine.v2.model.V2ModelAddDetailRowUi
import com.devicecontrol.engine.v2.model.V2ModelEngineFieldUi

class V2ModelAddViewModel : ViewModel() {

    private val _modelName = MutableLiveData("")
    val modelName: LiveData<String> = _modelName

    private val _engineFields = MutableLiveData<List<V2ModelEngineFieldUi>>()
    val engineFields: LiveData<List<V2ModelEngineFieldUi>> = _engineFields

    private val _detailRows = MutableLiveData<List<V2ModelAddDetailRowUi>>()
    val detailRows: LiveData<List<V2ModelAddDetailRowUi>> = _detailRows

    fun load() {
        V2Log.i(TAG, "load add model")
        _modelName.value = ""
        _engineFields.value = listOf(
            V2ModelEngineFieldUi("model_name", "型号名称：", ""),
            V2ModelEngineFieldUi("safe_torque", "安全力矩：", "1480 lb·ft"),
            V2ModelEngineFieldUi("gear_ratio", "变速比：", "0.5"),
        )
        _detailRows.value = listOf(
            V2ModelAddDetailRowUi("position", "位置", "LPC1"),
            V2ModelAddDetailRowUi("blade", "叶片数", "45"),
        )
    }

    fun setModelName(name: String) {
        _modelName.value = name
    }

    fun updateEngineField(key: String, value: String) {
        updateList(_engineFields) { list ->
            list.map { if (it.key == key) it.copy(value = value) else it }
        }
        if (key == "model_name") {
            _modelName.value = value
        }
    }

    fun updateDetailRow(key: String, value: String) {
        updateList(_detailRows) { list ->
            list.map { if (it.key == key) it.copy(value = value) else it }
        }
    }

    fun confirm(onSuccess: () -> Unit, onEmptyName: () -> Unit) {
        val name = _modelName.value?.trim().orEmpty()
        if (name.isEmpty()) {
            V2Log.w(TAG, "confirm failed: empty model name")
            onEmptyName()
            return
        }
        V2Log.i(TAG, "confirm add model=$name fields=${_engineFields.value} rows=${_detailRows.value}")
        onSuccess()
    }

    private fun <T> updateList(liveData: MutableLiveData<List<T>>, block: (List<T>) -> List<T>) {
        val current = liveData.value ?: return
        liveData.value = block(current)
    }

    companion object {
        private const val TAG = "ModelAddVM"
    }
}
