package com.devicecontrol.engine.communication

import com.devicecontrol.engine.communication.model.UsbDeviceInfo

/**
 * 连接目标：泛化连接层，业务层通过此类型选择要连接的设备/地址
 */
sealed class ConnectTarget {
    data class Usb(val deviceInfo: UsbDeviceInfo) : ConnectTarget()
    /** 预留：WiFi 连接 */
    data class Wifi(val host: String, val port: Int) : ConnectTarget()
}
