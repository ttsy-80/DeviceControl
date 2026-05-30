package com.devicecontrol.engine.v2.model

import com.devicecontrol.engine.data.model.TaskRecord

/** 检测主控记录表行 */
data class V2RecordRowUi(
    val recordId: Long,
    val positionLabel: String,
    val bladeCount: Int,
    val taskRecord: TaskRecord? = null,
    val isPlaceholder: Boolean = false,
)
