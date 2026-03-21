package com.devicecontrol.engine.usbserial

/**
 * USB 串口 VCP 通讯回调（仅用于 [UsbSerialVcp] 实现，不依赖项目其他通讯层）
 * 接收设备发来的数据与错误通知。
 */
interface UsbSerialVcpCallback {
    /**
     * VCP 连接状态发生变化（成功连接或断开）
     */
    fun onConnect(isConnect: Boolean)

    /**
     * 收到来自设备的数据（文本或二进制均可通过此回调；业务层可按需解码）
     */
    fun onDataReceived(data: ByteArray)

    /**
     * 发生错误（连接失败、发送失败、接收异常等）
     */
    fun onError(error: String)
}
