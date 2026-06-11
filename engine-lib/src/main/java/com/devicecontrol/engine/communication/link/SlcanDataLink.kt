package com.devicecontrol.engine.communication.link

/**
 * SLCAN 物理链路抽象：USB 串口 / BLE 透传等实现统一发送与连接态查询。
 */
interface SlcanDataLink {
    fun isConnected(): Boolean
    fun sendTextLine(text: String): Boolean
    fun sendText(text: String): Boolean
    fun disconnect()
    fun release()
}
