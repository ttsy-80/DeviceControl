package com.devicecontrol.engine.v2.model

data class V2ModelEngineFieldUi(
    val key: String,
    val label: String,
    var value: String,
    val editable: Boolean = true,
)

data class V2ModelAddDetailRowUi(
    val key: String,
    val label: String,
    var value: String,
)
