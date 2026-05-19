package com.devicecontrol.engine.v2.viewmodel

import androidx.annotation.DrawableRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.data.V2InspectionRepository
import com.devicecontrol.engine.v2.inspection.V2InspectionConfigUiBridge
import com.devicecontrol.engine.v2.log.V2Log
import kotlinx.coroutines.launch
import kotlin.math.round

data class V2StepperFieldUi(
    val key: String,
    val label: String,
    var value: Double,
    val step: Double,
    val unit: String,
    @DrawableRes val iconRes: Int,
    val decimalPlaces: Int = 1,
    val minValue: Double = 0.0,
    val maxValue: Double = Double.MAX_VALUE,
)

/** 自动/手动模式参数页（P8 / P11）：直接读写 Room 配置实体。 */
class V2ModeSettingsViewModel(
    private val inspectionRepository: V2InspectionRepository,
) : ViewModel() {

    private var modelId: Long = 0
    private var manualMode: Boolean = false

    private val _fields = MutableLiveData<List<V2StepperFieldUi>>()
    val fields: LiveData<List<V2StepperFieldUi>> = _fields

    fun init(modelId: Long, manual: Boolean) {
        this.modelId = modelId
        this.manualMode = manual
        viewModelScope.launch { reloadFields() }
    }

    fun adjustField(key: String, delta: Int) {
        val list = _fields.value?.toMutableList() ?: return
        val idx = list.indexOfFirst { it.key == key }
        if (idx < 0) return
        val item = list[idx]
        item.value = clampFieldValue(
            item,
            roundToPlaces(item.value + item.step * delta, item.decimalPlaces),
        )
        list[idx] = item
        _fields.value = list
    }

    fun setFieldValue(key: String, raw: String): Boolean {
        val parsed = raw.trim().replace(',', '.').toDoubleOrNull() ?: return false
        val list = _fields.value?.toMutableList() ?: return false
        val idx = list.indexOfFirst { it.key == key }
        if (idx < 0) return false
        val item = list[idx]
        item.value = clampFieldValue(item, roundToPlaces(parsed, item.decimalPlaces))
        list[idx] = item
        _fields.value = list
        return true
    }

    fun formatValue(field: V2StepperFieldUi): String {
        if (field.decimalPlaces <= 0) return field.value.toInt().toString()
        val rounded = roundToPlaces(field.value, field.decimalPlaces)
        return String.format("%.${field.decimalPlaces}f", rounded)
    }

    suspend fun saveSettings() {
        val list = _fields.value.orEmpty()
        if (manualMode) {
            inspectionRepository.saveManualConfig(
                V2InspectionConfigUiBridge.fieldsToManual(modelId, list),
            )
        } else {
            inspectionRepository.saveAutoConfig(
                V2InspectionConfigUiBridge.fieldsToAuto(modelId, list),
            )
        }
        V2Log.i(TAG, "saveSettings modelId=$modelId manual=$manualMode")
    }

    private suspend fun reloadFields() {
        _fields.value = if (manualMode) {
            V2InspectionConfigUiBridge.manualToFields(
                inspectionRepository.getOrCreateManualConfig(modelId),
            )
        } else {
            V2InspectionConfigUiBridge.autoToFields(
                inspectionRepository.getOrCreateAutoConfig(modelId),
            )
        }
    }

    private fun clampFieldValue(field: V2StepperFieldUi, value: Double): Double =
        value.coerceIn(field.minValue, field.maxValue)

    private fun roundToPlaces(value: Double, places: Int): Double {
        if (places <= 0) return round(value).toDouble()
        val factor = Math.pow(10.0, places.toDouble())
        return round(value * factor) / factor
    }

    companion object {
        private const val TAG = "ModeSettingsVM"
    }
}
