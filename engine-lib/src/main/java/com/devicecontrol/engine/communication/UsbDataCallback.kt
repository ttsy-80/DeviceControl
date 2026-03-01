package com.devicecontrol.engine.communication

/**
 * USB 数据接收回调接口（兼容旧用法，继承自通用 [DataCallback]）
 */
interface UsbDataCallback : DataCallback {
    override fun onTextDataReceived(data: String)
    override fun onBinaryDataReceived(data: ByteArray)
    override fun onError(error: String)
}
