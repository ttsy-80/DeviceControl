package com.devicecontrol.engine.communication

import com.devicecontrol.engine.communication.protocol.CanUsbProtocol

/**
 * CAN-USB 连接成功后自动初始化配置（按 LAWICEL CANUSB 手册）
 * 设置后，USB 连接成功时会按顺序发送：设置波特率 → 打开通道 → 可选时间戳/查版本/序列号
 */
data class CanUsbInitConfig(
    /** CAN 波特率，默认 500Kbps */
    val canBaudRate: CanUsbProtocol.CanBaudRate = CanUsbProtocol.CanBaudRate.BPS_500K,
    /** 是否打开 CAN 通道（O 命令），默认 true */
    val openChannel: Boolean = true,
    /** 是否启用时间戳（Z 命令），默认 false */
    val timeStamp: Boolean = false,
    /** 连接后是否查询版本号（V 命令） */
    val queryVersion: Boolean = false,
    /** 连接后是否查询序列号（N 命令） */
    val querySerialNumber: Boolean = false
)
