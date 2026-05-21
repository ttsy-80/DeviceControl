package com.devicecontrol.engine.v2.inspection.strategy

import com.devicecontrol.engine.communication.protocol.SlcanManager
import com.devicecontrol.engine.communication.protocol.SlcanRequest

/**
 * 检测页操作策略：开发模式模拟成功并立即落库；发布模式走真实 SLCAN 且以执行结果为准。
 */
interface V2InspectionOperationStrategy {

    val requiresDeviceConnection: Boolean

    /** 无 CAN 映射时是否仍允许将 [executionAfterSuccess] 写入数据库（开发模式用于纯状态切换）。 */
    val allowsPersistWithoutCan: Boolean

    /** 点动/连续循环中的等待时长（毫秒）；开发模式缩短以便调试。 */
    fun motionDelayMs(realDelayMs: Long): Long

    suspend fun executeRequests(
        slcanManager: SlcanManager?,
        requests: List<SlcanRequest>,
    ): V2CanExecuteOutcome
}
