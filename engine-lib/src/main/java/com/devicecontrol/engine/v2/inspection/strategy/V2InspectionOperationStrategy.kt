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

    /**
     * 开始指令是否仅将 [TaskStatus.RUNNING] 写入数据库并同步 LiveData，
     * 不启动点动/连续执行协程、不模拟发动机运转。
     */
    val startCommandPersistsStateOnly: Boolean

    /** 点动/连续循环中的等待时长（毫秒）；发布模式使用真实耗时。 */
    fun motionDelayMs(realDelayMs: Long): Long

    suspend fun executeRequests(
        slcanManager: SlcanManager?,
        requests: List<SlcanRequest>,
    ): V2CanExecuteOutcome
}
