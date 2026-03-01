package com.devicecontrol.engine.communication.protocol

import com.devicecontrol.engine.communication.CommunicationManager
import com.devicecontrol.engine.communication.DataCallback

/**
 * CANUSB协议使用示例
 * 
 * 根据LAWICEL CANUSB手册实现
 * CANUSB是一个CAN到USB的转换器，通过VCP（虚拟串口）进行通信
 * 
 * 参考文档：CANUSB Manual v1.0D
 * 网址：www.canusb.com
 * 
 * 使用流程：
 * 1. 连接USB设备（VCP模式）
 * 2. 设置CAN波特率（S命令）
 * 3. 打开CAN通道（O命令）
 * 4. 发送/接收CAN帧（t/T命令）
 * 5. 关闭CAN通道（C命令）
 */
object CanUsbProtocolExample {
    
    /**
     * 示例：完整的CANUSB初始化流程
     */
    fun initializeCanUsbExample(manager: CommunicationManager) {
        if (!manager.isConnected()) {
            android.util.Log.e("CANUSB", "设备未连接")
            return
        }
        manager.setCanBaudRate(CanUsbProtocol.CanBaudRate.BPS_500K)
        android.util.Log.d("CANUSB", "已设置CAN波特率: 500Kbps")
        manager.openCanChannel()
        android.util.Log.d("CANUSB", "已打开CAN通道")
        manager.setCanUsbTimeStamp(enabled = false)
        manager.getCanUsbVersion()
        manager.getCanUsbSerialNumber()
    }
    
    /**
     * 示例：发送CAN帧（对应格式：t60182B40600001000000\r）
     */
    fun sendCanFrameExample(manager: CommunicationManager) {
        val canId = 0x601
        val dlc = 8
        val data = byteArrayOf(0x2B, 0x40, 0x60, 0x00, 0x01, 0x00, 0x00, 0x00)
        val success = manager.sendStandardCanFrame(canId, dlc, data)
        if (success) {
            android.util.Log.d("CANUSB", "CAN帧发送成功: ID=0x${canId.toString(16)}, DLC=$dlc")
        } else {
            android.util.Log.e("CANUSB", "CAN帧发送失败")
        }
        manager.sendCanOpenMessageFromHex(canId = canId, dataHex = "2B40600001000000")
    }
    
    /**
     * 示例：接收CAN帧
     */
    fun receiveCanFrameExample(manager: CommunicationManager) {
        manager.setDataCallback(object : DataCallback {
            override fun onTextDataReceived(data: String) {
                // 尝试解析为CANUSB帧
                val frame = CanUsbProtocol.parseReceivedFrame(data)
                if (frame != null) {
                    android.util.Log.d("CANUSB", "收到CAN帧: $frame")
                    android.util.Log.d("CANUSB", "CAN ID: 0x${frame.canId.toString(16).uppercase()}")
                    android.util.Log.d("CANUSB", "DLC: ${frame.dlc}")
                    android.util.Log.d("CANUSB", "数据: ${frame.data.joinToString(" ") { 
                        it.toUByte().toString(16).uppercase().padStart(2, '0') 
                    }}")
                    if (frame.timeStamp != null) {
                        android.util.Log.d("CANUSB", "时间戳: ${frame.timeStamp}ms")
                    }
                } else {
                    // 可能是其他响应（如版本号、序列号等）
                    android.util.Log.d("CANUSB", "收到数据: $data")
                }
            }
            
            override fun onBinaryDataReceived(data: ByteArray) {
                // 尝试解析为CAN帧
                val text = String(data, Charsets.US_ASCII)
                val frame = CanUsbProtocol.parseReceivedFrame(text)
                if (frame != null) {
                    android.util.Log.d("CANUSB", "收到CAN帧: $frame")
                }
            }
            
            override fun onError(error: String) {
                android.util.Log.e("CANUSB", "错误: $error")
            }
        })
    }
    
    /**
     * 示例：完整的CANUSB使用流程
     */
    fun completeExample(manager: CommunicationManager) {
        manager.setDataCallback(object : DataCallback {
            override fun onTextDataReceived(data: String) {
                when {
                    data.startsWith("V") -> android.util.Log.d("CANUSB", "版本号: ${data.trim()}")
                    data.startsWith("N") -> android.util.Log.d("CANUSB", "序列号: ${data.trim()}")
                    data.startsWith("F") -> android.util.Log.d("CANUSB", "错误标志: ${data.trim()}")
                    data.startsWith("t") || data.startsWith("T") -> {
                        val frame = CanUsbProtocol.parseReceivedFrame(data)
                        if (frame != null) android.util.Log.d("CANUSB", "收到CAN帧: $frame")
                    }
                    else -> android.util.Log.d("CANUSB", "收到响应: $data")
                }
            }
            override fun onBinaryDataReceived(data: ByteArray) {
                val frame = CanUsbProtocol.parseReceivedFrame(String(data, Charsets.US_ASCII))
                if (frame != null) android.util.Log.d("CANUSB", "收到CAN帧: $frame")
            }
            override fun onError(error: String) {
                android.util.Log.e("CANUSB", "错误: $error")
            }
        })
        if (manager.isConnected()) {
            manager.setCanBaudRate(CanUsbProtocol.CanBaudRate.BPS_500K)
            manager.openCanChannel()
            manager.sendStandardCanFrame(
                canId = 0x601,
                dlc = 8,
                data = byteArrayOf(0x2B, 0x40, 0x60, 0x00, 0x01, 0x00, 0x00, 0x00)
            )
        }
    }
    
    /**
     * 示例：使用不同的CAN波特率
     */
    fun setDifferentBaudRatesExample(manager: CommunicationManager) {
        manager.setCanBaudRate(CanUsbProtocol.CanBaudRate.BPS_10K)
        manager.setCanBaudRate(CanUsbProtocol.CanBaudRate.BPS_100K)
        manager.setCanBaudRate(CanUsbProtocol.CanBaudRate.BPS_250K)
        manager.setCanBaudRate(CanUsbProtocol.CanBaudRate.BPS_500K)
        manager.setCanBaudRate(CanUsbProtocol.CanBaudRate.BPS_1M)
    }
}
