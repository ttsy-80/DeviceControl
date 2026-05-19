package com.devicecontrol.engine.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** V2 检测页按机型的界面偏好（上次选择的自动/手动模式）。 */
@Entity(tableName = "v2_inspection_prefs")
data class V2InspectionPrefs(
    @PrimaryKey
    val modelId: Long,
    /** [MODE_AUTO] 或 [MODE_MANUAL] */
    val lastUiMode: String = MODE_AUTO,
) {
    companion object {
        const val MODE_AUTO = "AUTO"
        const val MODE_MANUAL = "MANUAL"

        fun defaults(modelId: Long) = V2InspectionPrefs(modelId = modelId)
    }
}
