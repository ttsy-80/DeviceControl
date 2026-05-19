package com.devicecontrol.engine.v2.inspection

import com.devicecontrol.engine.data.model.ConfigItem
import com.devicecontrol.engine.data.model.TaskExecution
import com.devicecontrol.engine.data.model.V2AutoInspectionConfig
import com.devicecontrol.engine.data.model.V2ManualInspectionConfig
import com.devicecontrol.engine.v2.viewmodel.V2UiOperationMode

/**
 * V2 检测下发指令时的上下文：将自动/手动配置实体与当前运行态一并传给指令层，
 * 供 [V2InspectionCommandDispatcher] 及后续嵌入式协议对接使用。
 */
data class V2InspectionCommandContext(
    val uiMode: V2UiOperationMode,
    val autoConfig: V2AutoInspectionConfig,
    val manualConfig: V2ManualInspectionConfig,
    val execution: TaskExecution,
    val configItem: ConfigItem?,
    val modelName: String?,
    val position: String?,
    /** 手控调速后的临时速度（秒/圈）；为空则使用配置实体中的速度字段。 */
    val speedSecOverride: Double? = null,
)
