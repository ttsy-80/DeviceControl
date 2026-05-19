package com.devicecontrol.engine.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** V2 检测页自动模式（P8）参数，按机型一行。单位与 UI 步进器一致。 */
@Entity(tableName = "v2_auto_inspection_configs")
data class V2AutoInspectionConfig(
    @PrimaryKey
    val modelId: Long,
    /** 自动点动角度（度），范围 1～120，默认 1° */
    val autoJogAngle: Double = DEFAULT_AUTO_JOG_ANGLE,
    /** 基础点动速度（度/分钟），范围 30～120，默认 60°/分钟 */
    val baseJogSpeed: Double = DEFAULT_BASE_JOG_SPEED,
    /** 自动连续速度 °/秒 */
    val autoContinuous: Double = 0.1,
    /** 回转速度 °/秒 */
    val reverseSpeed: Double = 0.1,
    /** 自动调速步进 °/秒 */
    val autoSpeedStep: Double = 0.1,
    /** 点动停滞时间 S */
    val jogHoldSec: Double = 2.0,
    /** 自动多转数（片） */
    val autoTurns: Double = 2.0,
) {
    fun clamped(): V2AutoInspectionConfig = copy(
        autoJogAngle = autoJogAngle.coerceIn(MIN_AUTO_JOG_ANGLE, MAX_AUTO_JOG_ANGLE),
        baseJogSpeed = baseJogSpeed.coerceIn(MIN_BASE_JOG_SPEED, MAX_BASE_JOG_SPEED),
    )

    companion object {
        const val MIN_AUTO_JOG_ANGLE = 1.0
        const val MAX_AUTO_JOG_ANGLE = 120.0
        const val DEFAULT_AUTO_JOG_ANGLE = 1.0

        const val MIN_BASE_JOG_SPEED = 30.0
        const val MAX_BASE_JOG_SPEED = 120.0
        const val DEFAULT_BASE_JOG_SPEED = 60.0

        fun defaults(modelId: Long) = V2AutoInspectionConfig(modelId = modelId).clamped()
    }
}
