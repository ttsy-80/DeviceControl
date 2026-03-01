package com.devicecontrol.engine.communication

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 连接层抽象：只负责连接、发送、接收
 * 具体实现可以是 USB（HID/VCP）、WiFi 等，业务层通过 [CommunicationManager] 统一使用
 */
interface CommunicationTransport {
    /** 当前可用连接目标列表 */
    fun getAvailableTargets(): List<ConnectTarget>
    /** 刷新可用目标（如扫描 USB 设备 / WiFi 设备） */
    fun refreshAvailableTargets()

    /**
     * 连接
     * @param target 目标（如 [ConnectTarget.Usb] / [ConnectTarget.Wifi]）
     * @param callback 接收数据与错误回调，可为 null 表示使用已设置的 callback
     */
    fun connect(target: ConnectTarget, callback: DataCallback?)

    fun disconnect()
    fun isConnected(): Boolean
    fun sendText(text: String): Boolean
    fun sendBinary(data: ByteArray): Boolean

    /** 连接状态流 */
    fun getConnectionState(): StateFlow<ConnectionState>

    /** 可用目标列表流（如 USB 插拔时更新；默认返回空列表流） */
    open fun getAvailableTargetsState(): StateFlow<List<ConnectTarget>> =
        MutableStateFlow(emptyList<ConnectTarget>()).asStateFlow()

    /** 释放资源（如注销广播） */
    fun release()
}
