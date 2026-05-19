package com.devicecontrol.engine.v2.inspection

/** P8 / P11 步进器参数快照（键与 [com.devicecontrol.engine.v2.viewmodel.V2ModeSettingsViewModel] 一致） */
data class V2ModeSettingsSnapshot(
    val values: Map<String, Double> = emptyMap(),
) {
    fun get(key: String, default: Double = 0.0): Double = values[key] ?: default
}
