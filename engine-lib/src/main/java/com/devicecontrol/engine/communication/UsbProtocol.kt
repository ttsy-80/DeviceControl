package com.devicecontrol.engine.communication

/**
 * USB通信协议类型
 */
enum class UsbProtocol {
    /**
     * HID (Human Interface Device) 协议
     */
    HID,
    
    /**
     * VCP (Virtual COM Port) 协议：原生 CDC/ACM bulkTransfer
     */
    VCP,
    /**
     * VSP (Virtual Serial Port)：基于 usb-serial-for-android，支持 CDC/FTDI/CH340/Cp21xx 等
     */
    VSP
}
