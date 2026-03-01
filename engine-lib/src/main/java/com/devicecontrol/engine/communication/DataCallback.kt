package com.devicecontrol.engine.communication

/**
 * 通用数据接收回调接口
 * 业务层只关心：接收文本/二进制数据、错误通知，与具体连接方式（USB、WiFi 等）无关
 */
interface DataCallback {
    fun onTextDataReceived(data: String)
    fun onBinaryDataReceived(data: ByteArray)
    fun onError(error: String)
}
