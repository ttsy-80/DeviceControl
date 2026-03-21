package com.devicecontrol.engine.communication.protocol

import com.devicecontrol.engine.communication.protocol.SlcanManager.Companion.sdoReadValidator
import com.devicecontrol.engine.communication.protocol.SlcanManager.Companion.sdoWriteValidator

/**
 * 定义一个 CANOpen 对象字典实体
 */
data class CanOpenObject(
    val index: Int,
    val subIndex: Int,
    val dataSize: Int, // 1, 2, 或是 4 字节
    val name: String
)

/**
 * 驱动器 CiA 402 标准对象字典映射表
 */
object CiA402 {
    val ControlWord = CanOpenObject(0x6040, 0x00, 2, "ControlWord")
    val StatusWord = CanOpenObject(0x6041, 0x00, 2, "StatusWord")
    val OperationMode = CanOpenObject(0x6060, 0x00, 1, "OperationMode")
    val TargetVelocity = CanOpenObject(0x60FF, 0x00, 4, "TargetVelocity")
    val ProfileVelocity = CanOpenObject(0x6081, 0x00, 4, "ProfileVelocity")
    val TargetPosition = CanOpenObject(0x607A, 0x00, 4, "TargetPosition")
    val ActualVelocity = CanOpenObject(0x606C, 0x00, 4, "ActualVelocity")
    val ActualPosition = CanOpenObject(0x6063, 0x00, 4, "ActualPosition")
    val EncoderResolution = CanOpenObject(0x6410, 0x03, 4, "EncoderResolution")
}

/**
 * 提供对象字典到 SlcanRequest 的转换构建服务，及常用的控制方法序列。
 * 结合了标准 SDO 校验器与值提取器。
 */
object CANOpenHelper {

    const val DEFAULT_NODE_ID = 1

    /**
     * 构建单点【写入】请求
     */
    fun buildWriteRequest(nodeId: Int, obj: CanOpenObject, value: Int): SlcanRequest {
        val frame = when (obj.dataSize) {
            1 -> CanFrame.sdoWrite1B(nodeId, obj.index, obj.subIndex, value)
            2 -> CanFrame.sdoWrite2B(nodeId, obj.index, obj.subIndex, value)
            4 -> CanFrame.sdoWrite(nodeId, obj.index, obj.subIndex, value)
            else -> throw IllegalArgumentException("Unsupported data size ${obj.dataSize}")
        }

        return SlcanRequest(
            frame = frame,
            expectedResponseId = 0x580 + nodeId, // SDO Tx 发来回复
            validator = sdoWriteValidator,
            label = "写 ${obj.name} = $value"
        )
    }

    /**
     * 构建批量【写入】请求，严格保持传入 Pair 列表的执行顺序
     */
    fun buildWriteRequests(nodeId: Int, params: List<Pair<CanOpenObject, Int>>): List<SlcanRequest> {
        return params.map { buildWriteRequest(nodeId, it.first, it.second) }
    }

    /**
     * 构建批量【读取】请求，自动绑定校验与该数据维度的提取器
     */
    fun buildReadRequests(nodeId: Int, objs: List<CanOpenObject>): List<SlcanRequest> {
        return objs.map { obj ->
            SlcanRequest(
                frame = CanFrame.sdoRead(nodeId, obj.index, obj.subIndex),
                expectedResponseId = 0x580 + nodeId,
                validator = sdoReadValidator,
                extractor = { resp ->
                    val value = extractValueByDataType(resp.data, obj.dataSize)
                    mapOf(obj.name to value)
                },
                label = "读 ${obj.name}"
            )
        }
    }

    private fun extractValueByDataType(data: ByteArray, size: Int): Int {
        return when (size) {
            1 -> (data[4].toInt() and 0xFF)
            2 -> (data[4].toInt() and 0xFF) or ((data[5].toInt() and 0xFF) shl 8)
            4 -> (data[4].toInt() and 0xFF) or ((data[5].toInt() and 0xFF) shl 8) or
                    ((data[6].toInt() and 0xFF) shl 16) or ((data[7].toInt() and 0xFF) shl 24)
            else -> 0
        }
    }

    // ===================================
    // 抽象业务序列功能（直接映射上位机手册指令）
    // ===================================

    /** 
     * 启动指令 (速度模式)：设置速度模式 -> 写入期望转速 -> 开启使能 
     */
    fun startSpeedMode(speedVal: Int, nodeId: Int = DEFAULT_NODE_ID): List<SlcanRequest> {
        return buildWriteRequests(nodeId, listOf(
            CiA402.OperationMode to 3,
            CiA402.TargetVelocity to speedVal,
            CiA402.ControlWord to 0x000F
        ))
    }

    /** 
     * 暂停指令：停止运行 
     */
    fun stop(nodeId: Int = DEFAULT_NODE_ID): List<SlcanRequest> {
        return buildWriteRequests(nodeId, listOf(
            CiA402.ControlWord to 0x0007
        ))
    }

    /** 
     * 速度指令单独控制速度：运行期间直接变更速度参数
     */
    fun changeVelocity(speedVal: Int, nodeId: Int = DEFAULT_NODE_ID): List<SlcanRequest> {
        return buildWriteRequests(nodeId, listOf(
            CiA402.TargetVelocity to speedVal
        ))
    }

    /** 
     * 正反转切换："正负变化的时候，最好发一下停止指令在运行反向的指令"
     * 即：停 -> 设反速 -> 重新使能
     */
    fun reverseDirection(speedVal: Int, nodeId: Int = DEFAULT_NODE_ID): List<SlcanRequest> {
        return buildWriteRequests(nodeId, listOf(
            CiA402.ControlWord to 0x0007,
            CiA402.TargetVelocity to speedVal,
            CiA402.ControlWord to 0x000F
        ))
    }

    /** 
     * 回查模式：位置模式运行，按照当前运行方向，以当前的速度信息，回到上位机所发的位置处
     * 顺序：模式 -> 速度 -> 位置 -> 第一步启动 -> 第二步启动 
     */
    fun startPositionMode(profileSpeed: Int, targetPosition: Int, nodeId: Int = DEFAULT_NODE_ID): List<SlcanRequest> {
        return buildWriteRequests(nodeId, listOf(
            CiA402.ControlWord to 0x000F,
            CiA402.OperationMode to 1,
            CiA402.ProfileVelocity to profileSpeed,
            CiA402.TargetPosition to targetPosition,
            CiA402.ControlWord to 0x002F,
            CiA402.ControlWord to 0x003F
        ))
    }

    /** 
     * 记录指令：从伺服中读取编码器实际位置信息供上层落盘记录 
     */
    fun readPosition(nodeId: Int = DEFAULT_NODE_ID): List<SlcanRequest> {
        return buildReadRequests(nodeId, listOf(CiA402.ActualPosition))
    }

    /** 
     * 读取编码器分辨率（脉冲数/圈）
     */
    fun readEncoderResolution(nodeId: Int = DEFAULT_NODE_ID): List<SlcanRequest> {
        return buildReadRequests(nodeId, listOf(CiA402.EncoderResolution))
    }

    /** 
     * 相对位置模式：以相对当前位置移动给定脉冲数（用于点动循环）
     * 顺序：模式(1) -> 速度 -> 相对位置 -> 第一步启动(0x4F) -> 第二步启动(0x5F触发Bit4) 
     */
    fun startRelativePositionMode(profileSpeed: Int, relativePosition: Int, nodeId: Int = DEFAULT_NODE_ID): List<SlcanRequest> {
        return buildWriteRequests(nodeId, listOf(
            CiA402.ControlWord to 0x000F,
            CiA402.OperationMode to 1,
            CiA402.ProfileVelocity to profileSpeed,
            CiA402.TargetPosition to relativePosition,
            CiA402.ControlWord to 0x004F, // Relative flag on, New Setpoint off
            CiA402.ControlWord to 0x005F  // Toggle New Setpoint to trigger move
        ))
    }
}
