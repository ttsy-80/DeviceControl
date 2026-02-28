package com.devicecontrol.engine.communication.protocol

import com.devicecontrol.engine.communication.UsbCommunicationManager

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
    fun initializeCanUsbExample(usbManager: UsbCommunicationManager) {
        // 1. 确保已连接到USB设备（VCP模式）
        if (!usbManager.isConnected()) {
            android.util.Log.e("CANUSB", "设备未连接")
            return
        }
        
        // 2. 设置CAN波特率为500Kbps
        usbManager.setCanBaudRate(CanUsbProtocol.CanBaudRate.BPS_500K)
        android.util.Log.d("CANUSB", "已设置CAN波特率: 500Kbps")
        
        // 3. 打开CAN通道
        usbManager.openCanChannel()
        android.util.Log.d("CANUSB", "已打开CAN通道")
        
        // 4. （可选）设置时间戳
        usbManager.setCanUsbTimeStamp(enabled = false)
        
        // 5. （可选）获取版本号
        usbManager.getCanUsbVersion()
        
        // 6. （可选）获取序列号
        usbManager.getCanUsbSerialNumber()
    }
    
    /**
     * 示例：发送CAN帧（对应你提供的格式：t60182B40600001000000\r）
     */
    fun sendCanFrameExample(usbManager: UsbCommunicationManager) {
        // 示例数据：t60182B40600001000000\r
        // CAN ID: 0x601 (十进制 1537)
        // DLC: 8 (8字节)
        // 数据: 2B 40 60 00 01 00 00 00
        
        val canId = 0x601
        val dlc = 8
        val data = byteArrayOf(0x2B, 0x40, 0x60, 0x00, 0x01, 0x00, 0x00, 0x00)
        
        // 方法1：使用标准CAN帧发送（11位ID）
        val success = usbManager.sendStandardCanFrame(canId, dlc, data)
        if (success) {
            android.util.Log.d("CANUSB", "CAN帧发送成功: ID=0x${canId.toString(16)}, DLC=$dlc")
        } else {
            android.util.Log.e("CANUSB", "CAN帧发送失败")
        }
        
        // 方法2：使用便捷方法（兼容之前的CanOpenMessage）
        usbManager.sendCanOpenMessageFromHex(
            canId = canId,
            dataHex = "2B40600001000000"
        )
    }
    
    /**
     * 示例：接收CAN帧
     */
    fun receiveCanFrameExample(usbManager: UsbCommunicationManager) {
        // 设置数据回调，自动解析CAN帧
        usbManager.setDataCallback(object : com.devicecontrol.engine.communication.UsbDataCallback {
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
    fun completeExample(context: android.content.Context) {
        // 1. 获取USB通信管理器
        val usbManager = UsbCommunicationManager.getInstance(context)
        
        // 2. 确保使用VCP协议（默认就是VCP）
        usbManager.setProtocol(com.devicecontrol.engine.communication.UsbProtocol.VCP)
        
        // 3. 设置数据回调
        usbManager.setDataCallback(object : com.devicecontrol.engine.communication.UsbDataCallback {
            override fun onTextDataReceived(data: String) {
                // 解析CANUSB响应
                when {
                    data.startsWith("V") -> {
                        // 版本号响应：V1013[CR]
                        val version = data.trim()
                        android.util.Log.d("CANUSB", "版本号: $version")
                    }
                    data.startsWith("N") -> {
                        // 序列号响应：NA123[CR]
                        val serial = data.trim()
                        android.util.Log.d("CANUSB", "序列号: $serial")
                    }
                    data.startsWith("F") -> {
                        // 错误标志响应：F01[CR]
                        val errorFlags = data.trim()
                        android.util.Log.d("CANUSB", "错误标志: $errorFlags")
                    }
                    data.startsWith("t") || data.startsWith("T") -> {
                        // CAN帧响应
                        val frame = CanUsbProtocol.parseReceivedFrame(data)
                        if (frame != null) {
                            android.util.Log.d("CANUSB", "收到CAN帧: $frame")
                        }
                    }
                    else -> {
                        android.util.Log.d("CANUSB", "收到响应: $data")
                    }
                }
            }
            
            override fun onBinaryDataReceived(data: ByteArray) {
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
        
        // 4. 连接设备（假设已选择设备）
        // usbManager.connect(device)
        
        // 5. 初始化CANUSB
        if (usbManager.isConnected()) {
            // 设置波特率
            usbManager.setCanBaudRate(CanUsbProtocol.CanBaudRate.BPS_500K)
            
            // 打开CAN通道
            usbManager.openCanChannel()
            
            // 发送CAN帧（示例：t60182B40600001000000\r）
            usbManager.sendStandardCanFrame(
                canId = 0x601,
                dlc = 8,
                data = byteArrayOf(0x2B, 0x40, 0x60, 0x00, 0x01, 0x00, 0x00, 0x00)
            )
        }
    }
    
    /**
     * 示例：使用不同的CAN波特率
     */
    fun setDifferentBaudRatesExample(usbManager: UsbCommunicationManager) {
        // 10Kbps
        usbManager.setCanBaudRate(CanUsbProtocol.CanBaudRate.BPS_10K)
        
        // 100Kbps
        usbManager.setCanBaudRate(CanUsbProtocol.CanBaudRate.BPS_100K)
        
        // 250Kbps
        usbManager.setCanBaudRate(CanUsbProtocol.CanBaudRate.BPS_250K)
        
        // 500Kbps（常用）
        usbManager.setCanBaudRate(CanUsbProtocol.CanBaudRate.BPS_500K)
        
        // 1Mbps
        usbManager.setCanBaudRate(CanUsbProtocol.CanBaudRate.BPS_1M)
    }
}
