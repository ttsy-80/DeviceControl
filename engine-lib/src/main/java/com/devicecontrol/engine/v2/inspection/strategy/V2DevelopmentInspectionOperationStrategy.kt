package com.devicecontrol.engine.v2.inspection.strategy

import com.devicecontrol.engine.communication.protocol.SlcanManager
import com.devicecontrol.engine.communication.protocol.SlcanRequest
import com.devicecontrol.engine.v2.log.V2Log

/** 开发中：跳过硬件指令，模拟成功，便于离线调试界面与数据库状态。 */
object V2DevelopmentInspectionOperationStrategy : V2InspectionOperationStrategy {

    private const val TAG = "InspectionOpDev"
    private const val SIMULATED_MOTION_DELAY_MS = 150L

    override val requiresDeviceConnection: Boolean = false

    override val allowsPersistWithoutCan: Boolean = true

    override fun motionDelayMs(realDelayMs: Long): Long = SIMULATED_MOTION_DELAY_MS

    override suspend fun executeRequests(
        slcanManager: SlcanManager?,
        requests: List<SlcanRequest>,
    ): V2CanExecuteOutcome {
        if (requests.isNotEmpty()) {
            V2Log.d(TAG, "skip ${requests.size} CAN request(s) in development mode")
        }
        return V2CanExecuteOutcome(success = true)
    }
}
