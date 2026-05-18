package com.devicecontrol.engine.v2.model

/** 从型号详情进入添加页时携带的只读发动机信息 */
data class V2ModelAppendArgs(
    val modelId: Long,
    val modelName: String,
    val safeTorque: String,
    val gearRatio: Double,
    val imagePath: String?,
)
