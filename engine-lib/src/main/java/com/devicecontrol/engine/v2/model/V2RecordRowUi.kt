package com.devicecontrol.engine.v2.model

/** 检测主控记录表行（UI 占位，后续对接 TaskRecord） */
data class V2RecordRowUi(
    val positionLabel: String,
    val bladeCount: Int,
)
