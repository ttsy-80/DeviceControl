package com.devicecontrol.engine.communication

/**
 * USB数据接收回调接口
 * 用于接收从发动机设备返回的数据
 */
interface UsbDataCallback {
    /**
     * 接收到文本数据
     * @param data 接收到的文本数据
     */
    fun onTextDataReceived(data: String)
    
    /**
     * 接收到二进制数据
     * @param data 接收到的二进制数据
     */
    fun onBinaryDataReceived(data: ByteArray)
    
    /**
     * 通信错误回调
     * @param error 错误信息
     */
    fun onError(error: String)
}
