package com.devicecontrol.engine.communication.protocol

import com.devicecontrol.engine.communication.CommunicationManager

/**
 * CAN Open协议使用示例
 * 
 * 协议格式说明：
 * t{ID}{DLC}{DATA}\r
 * 
 * 示例：t60182B40600001000000\r
 * - t: 命令前缀（transmit/send）
 * - 601: CAN ID（十六进制，11位标准帧或29位扩展帧）
 * - 8: DLC（Data Length Code，数据长度，0-8）
 * - 2B40600001000000: CanOpen数据（十六进制，8字节 = 16个字符）
 * - \r: 回车符（carriage return）
 */
object CanOpenProtocolExample {
    
    /**
     * 示例：发送CAN Open消息
     */
    fun sendCanOpenMessageExample(manager: CommunicationManager) {
        // 示例数据：t60182B40600001000000\r
        val message = CanOpenMessage(
            canId = 0x601,
            dlc = 8,
            data = byteArrayOf(0x2B, 0x40, 0x60, 0x00, 0x01, 0x00, 0x00, 0x00)
        )
        manager.sendCanOpenMessage(message)
        manager.sendCanOpenMessage(
            canId = 0x601,
            dlc = 8,
            data = byteArrayOf(0x2B, 0x40, 0x60, 0x00, 0x01, 0x00, 0x00, 0x00)
        )
        manager.sendCanOpenMessageFromHex(
            canId = 0x601,
            dataHex = "2B40600001000000"
        )
    }
    
    /**
     * 示例：接收CAN Open消息
     */
    fun receiveCanOpenMessageExample(manager: CommunicationManager) {
        manager.setCanOpenMessageCallback(object : CommunicationManager.CanOpenMessageCallback {
            override fun onCanOpenMessageReceived(message: CanOpenMessage) {
                android.util.Log.d("CAN", "收到CAN Open消息: $message")
                android.util.Log.d("CAN", "CAN ID: 0x${message.canId.toString(16).uppercase()}")
                android.util.Log.d("CAN", "DLC: ${message.dlc}")
                android.util.Log.d("CAN", "数据: ${message.data.joinToString(" ") { 
                    it.toUByte().toString(16).uppercase().padStart(2, '0') 
                }}")
            }
        })
    }
    
    /**
     * 示例：解析协议字符串
     */
    fun parseProtocolStringExample() {
        val protocolString = "t60182B40600001000000\r"
        
        // 解析协议字符串
        val message = CanOpenProtocol.parse(protocolString)
        
        if (message != null) {
            android.util.Log.d("CAN", "解析成功: $message")
            android.util.Log.d("CAN", "CAN ID: 0x${message.canId.toString(16).uppercase()}")
            android.util.Log.d("CAN", "DLC: ${message.dlc}")
            android.util.Log.d("CAN", "数据: ${message.data.joinToString(" ") { 
                it.toUByte().toString(16).uppercase().padStart(2, '0') 
            }}")
            
            // 转换回协议字符串
            val protocolString2 = message.toProtocolString()
            android.util.Log.d("CAN", "协议字符串: $protocolString2")
        } else {
            android.util.Log.e("CAN", "解析失败")
        }
    }
    
    /**
     * 示例：构建CAN Open消息
     */
    fun buildCanOpenMessageExample() {
        // 方法1：使用build方法
        val message1 = CanOpenProtocol.build(
            canId = 0x601,
            dlc = 8,
            data = byteArrayOf(0x2B, 0x40, 0x60, 0x00, 0x01, 0x00, 0x00, 0x00)
        )
        
        // 方法2：使用buildFromHex方法
        val message2 = CanOpenProtocol.buildFromHex(
            canId = 0x601,
            dataHex = "2B40600001000000"
        )
        
        // 转换为协议字符串
        val protocolString1 = message1.toProtocolString()
        val protocolString2 = message2.toProtocolString()
        
        android.util.Log.d("CAN", "协议字符串1: $protocolString1")
        android.util.Log.d("CAN", "协议字符串2: $protocolString2")
        // 输出：t60182B40600001000000\r
    }
    
    /**
     * 示例：完整的使用流程
     */
    fun completeExample(manager: CommunicationManager) {
        manager.setCanOpenMessageCallback(object : CommunicationManager.CanOpenMessageCallback {
            override fun onCanOpenMessageReceived(message: CanOpenMessage) {
                android.util.Log.d("CAN", "收到消息: CAN ID=0x${message.canId.toString(16)}, DLC=${message.dlc}")
                when (message.canId) {
                    0x601 -> android.util.Log.d("CAN", "处理0x601消息")
                    else -> android.util.Log.d("CAN", "未知CAN ID: 0x${message.canId.toString(16)}")
                }
            }
        })
        if (manager.isConnected()) {
            manager.sendCanOpenMessageFromHex(canId = 0x601, dataHex = "2B40600001000000")
        }
    }
}
