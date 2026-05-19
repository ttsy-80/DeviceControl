package com.devicecontrol.engine.v2.viewmodel

import androidx.annotation.DrawableRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.data.V2InspectionRepository
import com.devicecontrol.engine.v2.log.V2Log
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.round

data class V2StepperFieldUi(
    val key: String,
    val label: String,
    var value: Double,
    val step: Double,
    val unit: String,
    @DrawableRes val iconRes: Int,
    val decimalPlaces: Int = 1,
)

/**
 * 自动/手动模式参数页（P8 / P11）步进器数据。
 */
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
        val next = roundToPlaces(item.value + item.step * delta, item.decimalPlaces)
        item.value = max(0.0, next)
        list[idx] = item
        V2Log.d(TAG, "adjustField $key -> ${item.value}")
        _fields.value = list
    }

    fun setFieldValue(key: String, raw: String): Boolean {
        val parsed = raw.trim().replace(',', '.').toDoubleOrNull() ?: return false
        val list = _fields.value?.toMutableList() ?: return false
        val idx = list.indexOfFirst { it.key == key }
        if (idx < 0) return false
        val item = list[idx]
        item.value = max(0.0, roundToPlaces(parsed, item.decimalPlaces))
        list[idx] = item
        V2Log.d(TAG, "setFieldValue $key -> ${item.value}")
        _fields.value = list
        return true
    }

    fun formatValue(field: V2StepperFieldUi): String {
        if (field.decimalPlaces <= 0) {
            return field.value.toInt().toString()
        }
        val rounded = roundToPlaces(field.value, field.decimalPlaces)
        return String.format("%.${field.decimalPlaces}f", rounded)
    }

    suspend fun saveSettings() {
        val list = _fields.value.orEmpty()
        inspectionRepository.saveModeSettings(modelId, manualMode, list)
        V2Log.i(TAG, "saveSettings modelId=$modelId manual=$manualMode count=${list.size}")
    }

    private suspend fun reloadFields() {
        val defaults = if (manualMode) defaultManualFields() else defaultAutoFields()
        val snapshot = inspectionRepository.loadModeSettings(modelId, manualMode, defaults)
        _fields.value = defaults.map { field ->
            field.copy(value = snapshot.get(field.key, field.value))
        }
        V2Log.i(TAG, "reloadFields modelId=$modelId manual=$manualMode")
    }

    private fun roundToPlaces(value: Double, places: Int): Double {
        if (places <= 0) return round(value).toDouble()
        val factor = Math.pow(10.0, places.toDouble())
        return round(value * factor) / factor
    }

    companion object {
        private const val TAG = "ModeSettingsVM"

        fun defaultAutoFields(): List<V2StepperFieldUi> = listOf(
            V2StepperFieldUi("auto_jog_angle", "自动点动角度", 0.1, 0.1, "°/秒", R.drawable.ic_v2_mode_angle),
            V2StepperFieldUi("base_jog_speed", "基础点动速度", 0.1, 0.1, "°/秒", R.drawable.ic_v2_mode_jog),
            V2StepperFieldUi("auto_continuous", "自动连续速度", 0.1, 0.1, "°/秒", R.drawable.ic_v2_mode_continuous),
            V2StepperFieldUi("reverse_speed", "回转速度", 0.1, 0.1, "°/秒", R.drawable.ic_v2_mode_reverse),
            V2StepperFieldUi("auto_speed_step", "自动调速值", 0.1, 0.1, "°/秒", R.drawable.ic_v2_mode_speed),
            V2StepperFieldUi("jog_hold", "点动停滞时间", 2.0, 1.0, "S", R.drawable.ic_v2_mode_timer,  0),
            V2StepperFieldUi("auto_turns", "自动多转数", 2.0, 1.0, "片", R.drawable.ic_v2_mode_turns,  0),
        )

        fun defaultManualFields(): List<V2StepperFieldUi> = listOf(
            V2StepperFieldUi("manual_jog_angle", "手动点动角度", 0.1, 0.1, "°/秒", R.drawable.ic_v2_mode_angle),
            V2StepperFieldUi("manual_continuous", "手动连续速度", 0.1, 0.1, "°/秒", R.drawable.ic_v2_mode_continuous),
            V2StepperFieldUi("manual_speed_step", "手动调速值", 0.1, 0.1, "°/秒", R.drawable.ic_v2_mode_speed),
        )
    }
}
