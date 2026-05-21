package com.devicecontrol.engine.v2.inspection.strategy

/** CAN 指令执行结果（开发模式无真实应答时 [values] 为空）。 */
data class V2CanExecuteOutcome(
    val success: Boolean,
    val values: Map<String, Any> = emptyMap(),
    val error: String? = null,
)
