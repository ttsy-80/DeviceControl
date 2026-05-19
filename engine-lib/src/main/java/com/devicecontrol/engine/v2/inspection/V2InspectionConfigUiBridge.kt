package com.devicecontrol.engine.v2.inspection

import com.devicecontrol.engine.R
import com.devicecontrol.engine.data.model.V2AutoInspectionConfig
import com.devicecontrol.engine.data.model.V2ManualInspectionConfig
import com.devicecontrol.engine.v2.viewmodel.V2StepperFieldUi

/** P8/P11 步进器 UI 与 Room 配置实体互转（仅设置页用，不参与指令映射）。 */
object V2InspectionConfigUiBridge {

    fun autoToFields(config: V2AutoInspectionConfig): List<V2StepperFieldUi> {
        val c = config.clamped()
        return listOf(
            field(
                key = "auto_jog_angle",
                label = "自动点动角度",
                value = c.autoJogAngle,
                step = 1.0,
                unit = "°",
                iconRes = R.drawable.ic_v2_mode_angle,
                decimals = 0,
                min = V2AutoInspectionConfig.MIN_AUTO_JOG_ANGLE,
                max = V2AutoInspectionConfig.MAX_AUTO_JOG_ANGLE,
            ),
            field(
                key = "base_jog_speed",
                label = "基础点动速度",
                value = c.baseJogSpeed,
                step = 1.0,
                unit = "°/分钟",
                iconRes = R.drawable.ic_v2_mode_jog,
                decimals = 0,
                min = V2AutoInspectionConfig.MIN_BASE_JOG_SPEED,
                max = V2AutoInspectionConfig.MAX_BASE_JOG_SPEED,
            ),
            field("auto_continuous", "自动连续速度", c.autoContinuous, 0.1, "°/秒", R.drawable.ic_v2_mode_continuous),
            field("reverse_speed", "回转速度", c.reverseSpeed, 0.1, "°/秒", R.drawable.ic_v2_mode_reverse),
            field("auto_speed_step", "自动调速值", c.autoSpeedStep, 0.1, "°/秒", R.drawable.ic_v2_mode_speed),
            field("jog_hold", "点动停滞时间", c.jogHoldSec, 1.0, "S", R.drawable.ic_v2_mode_timer, decimals = 0),
            field("auto_turns", "自动多转数", c.autoTurns, 1.0, "片", R.drawable.ic_v2_mode_turns, decimals = 0),
        )
    }

    fun manualToFields(config: V2ManualInspectionConfig): List<V2StepperFieldUi> = listOf(
        field("manual_jog_angle", "手动点动角度", config.manualJogAngle, 0.1, "°/秒", R.drawable.ic_v2_mode_angle),
        field("manual_continuous", "手动连续速度", config.manualContinuous, 0.1, "°/秒", R.drawable.ic_v2_mode_continuous),
        field("manual_speed_step", "手动调速值", config.manualSpeedStep, 0.1, "°/秒", R.drawable.ic_v2_mode_speed),
    )

    fun fieldsToAuto(modelId: Long, fields: List<V2StepperFieldUi>): V2AutoInspectionConfig {
        fun v(key: String, fallback: Double) = fields.firstOrNull { it.key == key }?.value ?: fallback
        return V2AutoInspectionConfig(
            modelId = modelId,
            autoJogAngle = v("auto_jog_angle", V2AutoInspectionConfig.DEFAULT_AUTO_JOG_ANGLE),
            baseJogSpeed = v("base_jog_speed", V2AutoInspectionConfig.DEFAULT_BASE_JOG_SPEED),
            autoContinuous = v("auto_continuous", 0.1),
            reverseSpeed = v("reverse_speed", 0.1),
            autoSpeedStep = v("auto_speed_step", 0.1),
            jogHoldSec = v("jog_hold", 2.0),
            autoTurns = v("auto_turns", 2.0),
        ).clamped()
    }

    fun fieldsToManual(modelId: Long, fields: List<V2StepperFieldUi>): V2ManualInspectionConfig {
        fun v(key: String, fallback: Double) = fields.firstOrNull { it.key == key }?.value ?: fallback
        return V2ManualInspectionConfig(
            modelId = modelId,
            manualJogAngle = v("manual_jog_angle", 0.1),
            manualContinuous = v("manual_continuous", 0.1),
            manualSpeedStep = v("manual_speed_step", 0.1),
        )
    }

    private fun field(
        key: String,
        label: String,
        value: Double,
        step: Double,
        unit: String,
        iconRes: Int,
        decimals: Int = 1,
        min: Double = 0.0,
        max: Double = Double.MAX_VALUE,
    ) = V2StepperFieldUi(
        key = key,
        label = label,
        value = value,
        step = step,
        unit = unit,
        iconRes = iconRes,
        decimalPlaces = decimals,
        minValue = min,
        maxValue = max,
    )
}
