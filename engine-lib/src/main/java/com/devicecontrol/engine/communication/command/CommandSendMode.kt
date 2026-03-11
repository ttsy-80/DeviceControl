package com.devicecontrol.engine.communication.command

/**
 * 指令下发方式：一个标记位切换所有指令的发送方式
 * - [CAN_OPEN]：按 CAN Open 协议组帧，[CommunicationManager.sendCanOpenMessage] 下发
 * - [TEXT_JSON]：按自定义 JSON 指令组帧，[CommunicationManager.sendText] 下发
 */
enum class CommandSendMode {
    /** CAN Open 帧，sendCanOpenMessage */
    CAN_OPEN,
    /** 自定义 JSON 指令，sendText */
    TEXT_JSON
}
