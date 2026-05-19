package com.devicecontrol.engine.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** V2 检测页手动模式（P11）参数，按机型一行。 */
@Entity(tableName = "v2_manual_inspection_configs")
data class V2ManualInspectionConfig(
    @PrimaryKey
    val modelId: Long,
    /** 手动点动角度 °/秒 */
    val manualJogAngle: Double = 0.1,
    /** 手动连续速度 °/秒 */
    val manualContinuous: Double = 0.1,
    /** 手动调速步进 °/秒 */
    val manualSpeedStep: Double = 0.1,
) {
    companion object {
        fun defaults(modelId: Long) = V2ManualInspectionConfig(modelId = modelId)
    }
}
