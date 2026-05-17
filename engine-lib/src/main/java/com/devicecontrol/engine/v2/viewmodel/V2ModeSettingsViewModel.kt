package com.devicecontrol.engine.v2.viewmodel

import androidx.annotation.DrawableRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.log.V2Log
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
class V2ModeSettingsViewModel : ViewModel() {

    private val _fields = MutableLiveData<List<V2StepperFieldUi>>()
    val fields: LiveData<List<V2StepperFieldUi>> = _fields

    fun loadAutoMode() {
        V2Log.i(TAG, "loadAutoMode")
        _fields.value = listOf(
            field("auto_jog_angle", "自动点动角度", 0.1, 0.1, "°/秒", R.drawable.ic_v2_mode_angle),
            field("base_jog_speed", "基础点动速度", 0.1, 0.1, "°/秒", R.drawable.ic_v2_mode_jog),
            field("auto_continuous", "自动连续速度", 0.1, 0.1, "°/秒", R.drawable.ic_v2_mode_continuous),
            field("reverse_speed", "回转速度", 0.1, 0.1, "°/秒", R.drawable.ic_v2_mode_reverse),
            field("auto_speed_step", "自动调速值", 0.1, 0.1, "°/秒", R.drawable.ic_v2_mode_speed),
            field("jog_hold", "点动停滞时间", 2.0, 1.0, "S", R.drawable.ic_v2_mode_timer, decimals = 0),
            field("auto_turns", "自动多转数", 2.0, 1.0, "片", R.drawable.ic_v2_mode_turns, decimals = 0),
        )
    }

    fun loadManualMode() {
        V2Log.i(TAG, "loadManualMode")
        _fields.value = listOf(
            field("manual_jog_angle", "手动点动角度", 0.1, 0.1, "°/秒", R.drawable.ic_v2_mode_angle),
            field("manual_continuous", "手动连续速度", 0.1, 0.1, "°/秒", R.drawable.ic_v2_mode_continuous),
            field("manual_speed_step", "手动调速值", 0.1, 0.1, "°/秒", R.drawable.ic_v2_mode_speed),
        )
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

    fun confirm() {
        V2Log.i(TAG, "confirm settings: ${_fields.value?.joinToString { "${it.key}=${it.value}${it.unit}" }}")
    }

    private fun field(
        key: String,
        label: String,
        value: Double,
        step: Double,
        unit: String,
        @DrawableRes iconRes: Int,
        decimals: Int = 1,
    ) = V2StepperFieldUi(key, label, value, step, unit, iconRes, decimals)

    private fun roundToPlaces(value: Double, places: Int): Double {
        if (places <= 0) return round(value).toDouble()
        val factor = Math.pow(10.0, places.toDouble())
        return round(value * factor) / factor
    }

    companion object {
        private const val TAG = "ModeSettingsVM"
    }
}
