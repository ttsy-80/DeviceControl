package com.devicecontrol.engine.v2.inspection.strategy

import com.devicecontrol.engine.communication.protocol.SlcanManager
import com.devicecontrol.engine.communication.protocol.SlcanRequest

/** 发布中：必须经 SLCAN 执行成功后才视为操作成功。 */
object V2ReleaseInspectionOperationStrategy : V2InspectionOperationStrategy {

    override val requiresDeviceConnection: Boolean = true

    override val allowsPersistWithoutCan: Boolean = false

    override val startCommandPersistsStateOnly: Boolean = false

    override fun motionDelayMs(realDelayMs: Long): Long = realDelayMs

    override suspend fun executeRequests(
        slcanManager: SlcanManager?,
        requests: List<SlcanRequest>,
    ): V2CanExecuteOutcome {
        if (requests.isEmpty()) {
            return V2CanExecuteOutcome(success = false, error = "无 CAN 请求")
        }
        val res = slcanManager?.execute(requests)
            ?: return V2CanExecuteOutcome(success = false, error = "SLCAN 未就绪")
        return V2CanExecuteOutcome(
            success = res.success,
            values = res.values,
            error = res.error,
        )
    }
}
