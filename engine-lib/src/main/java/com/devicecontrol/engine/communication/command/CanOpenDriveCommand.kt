package com.devicecontrol.engine.communication.command

import com.devicecontrol.engine.communication.protocol.CanOpenMessage

/**
 * CAN Open 驱动指令（CiA 402 风格 SDO）
 * 对接你提供的指令说明：速度模式（先发模式→速度→使能即可运行）、位置模式（模式→速度→目标位置→0x2F/0x3F 开始，0x07 停止）
 *
 * 理解摘要：
 * - 速度模式：写 0x6060=3(速度模式) → 写目标速度(如 0x60FF 或 0x6081) → 写 0x6040=0x0F(使能) 即运行；写 0x6040=0x07 停止。
 * - 位置模式：写 0x6060=1(位置模式) → 写 0x6081(轮廓速度) → 写 0x607A(目标位置) → 写 0x6040=0x2F 再 0x3F 开始；写 0x6040=0x07 停止。
 * - 位置值/速度值需根据实际减速比与设备单位计算或读取，此处提供按你给出示例的固定帧与可参数化接口。
 */
object CanOpenDriveCommand {

    private const val CAN_ID = 0x601
    private const val DLC = 8

    private fun msg(vararg bytes: Int): CanOpenMessage {
        require(bytes.size == DLC) { "data must be 8 bytes" }
        return CanOpenMessage(CAN_ID, DLC, ByteArray(DLC) { bytes[it].toByte() })
    }

    private fun msgFromHex(hex: String): CanOpenMessage {
        val s = hex.replace(" ", "").uppercase().padEnd(16, '0').take(16)
        val data = ByteArray(8) { i -> s.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
        return CanOpenMessage(CAN_ID, DLC, data)
    }

    // ---------- 速度模式 ----------
    /** 设置速度模式 (0x6060 = 3) */
    fun setSpeedMode(): CanOpenMessage = msgFromHex("2F60600003000000")

    /** 设置 150 RPM（你给的示例：601823FF600000400600 数据段） */
    fun setVelocity150Rpm(): CanOpenMessage = msgFromHex("23FF600000400600")  // 14 字符按 8 字节补齐

    /** 使能 (0x6040 = 0x0F)，速度模式下发完模式+速度后发此帧即可运行 */
    fun enable(): CanOpenMessage = msgFromHex("2B4060000F000000")

    /** 停止 (0x6040 = 0x07) */
    fun stop(): CanOpenMessage = msgFromHex("2B40600007000000")

    /** 速度模式运行序列：模式 → 速度(150rpm) → 使能。如需其它转速可先发 [setSpeedMode] 再发自定义速度帧再 [enable]。 */
    fun speedModeRunSequence150Rpm(): List<CanOpenMessage> =
        listOf(setSpeedMode(), setVelocity150Rpm(), enable())

    /** 可参数化：按 32 位整数写速度（具体含义与单位需按设备/减速比调整） */
    fun setVelocityRaw(value32Le: Int): CanOpenMessage {
        val d = ByteArray(8)
        d[0] = 0x23
        d[1] = 0xFF.toByte()
        d[2] = 0x60
        d[3] = 0x00
        d[4] = (value32Le and 0xFF).toByte()
        d[5] = (value32Le shr 8 and 0xFF).toByte()
        d[6] = (value32Le shr 16 and 0xFF).toByte()
        d[7] = (value32Le shr 24 and 0xFF).toByte()
        return CanOpenMessage(CAN_ID, DLC, d)
    }

    // ---------- 位置模式 ----------
    /** 设置位置模式 (0x6060 = 1) */
    fun setPositionMode(): CanOpenMessage = msgFromHex("2F60600001000000")

    /** 轮廓速度 100 RPM（你给的示例） */
    fun setProfileVelocity100Rpm(): CanOpenMessage = msgFromHex("23816000BB2A0400")

    /** 目标位置 10000（示例；实际需根据位置单位/减速比计算或读取） */
    fun setTargetPosition10000(): CanOpenMessage = msgFromHex("237A600010270000")

    /** 可参数化：目标位置（32 位，小端，单位按设备） */
    fun setTargetPosition(position: Int): CanOpenMessage {
        val d = ByteArray(8)
        d[0] = 0x23
        d[1] = 0x7A.toByte()
        d[2] = 0x60
        d[3] = 0x00
        d[4] = (position and 0xFF).toByte()
        d[5] = (position shr 8 and 0xFF).toByte()
        d[6] = (position shr 16 and 0xFF).toByte()
        d[7] = (position shr 24 and 0xFF).toByte()
        return CanOpenMessage(CAN_ID, DLC, d)
    }

    /** 位置启动第一步 (0x6040 = 0x2F) */
    fun startPositionStep1(): CanOpenMessage = msgFromHex("2B4060002F000000")

    /** 位置启动第二步 (0x6040 = 0x3F)，与上一条一起发即开始运行 */
    fun startPositionStep2(): CanOpenMessage = msgFromHex("2B4060003F000000")

    /** 位置模式运行序列：模式 → 轮廓速度100rpm → 目标位置10000 → 0x2F → 0x3F。实际应替换速度/位置为计算值。 */
    fun positionModeRunSequence(): List<CanOpenMessage> =
        listOf(
            setPositionMode(),
            setProfileVelocity100Rpm(),
            setTargetPosition10000(),
            startPositionStep1(),
            startPositionStep2()
        )

    /** 位置模式运行序列（可传目标位置；速度仍用 100rpm 示例，可按需扩展） */
    fun positionModeRunSequence(targetPosition: Int): List<CanOpenMessage> =
        listOf(
            setPositionMode(),
            setProfileVelocity100Rpm(),
            setTargetPosition(targetPosition),
            startPositionStep1(),
            startPositionStep2()
        )
}
