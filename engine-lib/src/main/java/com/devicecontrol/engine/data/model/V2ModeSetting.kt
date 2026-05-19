package com.devicecontrol.engine.data.model

import androidx.room.Entity
import androidx.room.Index

/**
 * V2 检测页自动(P8)/手动(P11)模式步进器参数，按机型持久化。
 */
@Entity(
    tableName = "v2_mode_settings",
    primaryKeys = ["modelId", "manual", "settingKey"],
    indices = [Index(value = ["modelId"])],
)
data class V2ModeSetting(
    val modelId: Long,
    /** false=自动(P8)，true=手动(P11) */
    val manual: Boolean,
    val settingKey: String,
    val value: Double,
)
