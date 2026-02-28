package com.devicecontrol.engine.communication

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager

/**
 * USB通信策略抽象接口
 * 定义了USB通信的基本操作，支持不同的通信协议实现（HID、VCP等）
 */
interface UsbCommunicationStrategy {
    /**
     * 检查设备是否支持该通信策略
     * @param device USB设备
     * @return 是否支持
     */
    fun isDeviceSupported(device: UsbDevice): Boolean
    
    /**
     * 连接USB设备
     * @param manager USB管理器
     * @param device USB设备
     * @param connection USB设备连接
     * @param callback 数据回调
     * @return 是否连接成功
     */
    fun connect(
        manager: UsbManager,
        device: UsbDevice,
        connection: UsbDeviceConnection,
        callback: UsbDataCallback?
    ): Boolean
    
    /**
     * 断开连接
     */
    fun disconnect()
    
    /**
     * 检查是否已连接
     * @return 是否已连接
     */
    fun isConnected(): Boolean
    
    /**
     * 发送文本数据
     * @param text 要发送的文本
     * @return 是否发送成功
     */
    fun sendText(text: String): Boolean
    
    /**
     * 发送二进制数据
     * @param data 要发送的二进制数据
     * @return 是否发送成功
     */
    fun sendBinary(data: ByteArray): Boolean
    
    /**
     * 开始接收数据（启动数据监听）
     */
    fun startReceiving()
    
    /**
     * 停止接收数据
     */
    fun stopReceiving()
}
