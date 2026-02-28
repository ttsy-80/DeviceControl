package com.devicecontrol.engine.communication.model

import android.hardware.usb.UsbDevice

/**
 * USB设备信息模型
 * 用于封装USB设备的基本信息
 */
data class UsbDeviceInfo(
    val device: UsbDevice,
    val deviceName: String,
    val vendorId: Int,
    val productId: Int,
    val deviceClass: Int,
    val supportedProtocols: List<String> = emptyList()
) {
    /**
     * 获取设备显示名称
     */
    fun getDisplayName(): String {
        return "$deviceName (VID: ${vendorId.toString(16)}, PID: ${productId.toString(16)})"
    }
}
