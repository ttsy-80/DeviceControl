package com.devicecontrol.engine.communication

import android.content.Context
import com.devicecontrol.engine.communication.model.UsbDeviceInfo
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * USB通信使用示例
 * 
 * 此文件展示了如何使用USB通信管理器进行设备连接和数据传输
 * 
 * 使用步骤：
 * 1. 获取USB通信管理器实例
 * 2. 设置通信协议（HID 或 VCP，默认为VCP）
 * 3. 实现UsbDataCallback接口处理接收到的数据
 * 4. 刷新可用设备列表
 * 5. 选择设备并连接
 * 6. 发送数据（文本或二进制）
 * 7. 接收数据（通过回调）
 * 8. 断开连接
 */
object UsbCommunicationExample {
    
    /**
     * 示例：基本使用流程
     */
    fun basicUsageExample(context: Context) {
        // 1. 获取USB通信管理器实例
        val usbManager = UsbCommunicationManager.getInstance(context)
        
        // 2. 设置通信协议（可选，默认为VCP）
        // 使用VCP协议（默认）
        // usbManager.setProtocol(UsbProtocol.VCP)
        // 或使用HID协议
        // usbManager.setProtocol(UsbProtocol.HID)
        
        // 3. 实现数据回调接口
        val dataCallback = object : UsbDataCallback {
            override fun onTextDataReceived(data: String) {
                // 处理接收到的文本数据
                android.util.Log.d("USB", "收到文本数据: $data")
            }
            
            override fun onBinaryDataReceived(data: ByteArray) {
                // 处理接收到的二进制数据
                android.util.Log.d("USB", "收到二进制数据，长度: ${data.size}")
            }
            
            override fun onError(error: String) {
                // 处理错误
                android.util.Log.e("USB", "通信错误: $error")
            }
        }
        
        // 设置数据回调
        usbManager.setDataCallback(dataCallback)
        
        // 4. 刷新可用设备列表
        usbManager.refreshAvailableDevices()
        
        // 5. 监听可用设备变化
        usbManager.availableDevices.onEach { devices ->
            android.util.Log.d("USB", "可用设备数量: ${devices.size}")
            devices.forEach { device ->
                android.util.Log.d("USB", "设备: ${device.getDisplayName()}, 支持协议: ${device.supportedProtocols}")
            }
        }.launchIn(kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main))
        
        // 6. 监听连接状态变化
        usbManager.connectionState.onEach { state ->
            when (state) {
                is UsbCommunicationManager.ConnectionState.Disconnected -> {
                    android.util.Log.d("USB", "已断开连接")
                }
                is UsbCommunicationManager.ConnectionState.RequestingPermission -> {
                    android.util.Log.d("USB", "正在请求USB权限")
                }
                is UsbCommunicationManager.ConnectionState.Connecting -> {
                    android.util.Log.d("USB", "正在连接...")
                }
                is UsbCommunicationManager.ConnectionState.Connected -> {
                    android.util.Log.d("USB", "已连接，协议: ${state.protocol}")
                }
            }
        }.launchIn(kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main))
        
        // 7. 选择设备并连接（假设从设备列表中选择第一个设备）
        // 在实际使用中，应该让用户选择设备
        // 注意：连接将使用当前设置的协议（通过 setProtocol() 设置，默认为VCP）
        usbManager.availableDevices.value.firstOrNull()?.let { deviceInfo ->
            usbManager.connect(deviceInfo.device, dataCallback)
        }
        
        // 8. 发送文本数据
        if (usbManager.isConnected()) {
            usbManager.sendText("Hello, Engine!")
        }
        
        // 9. 发送二进制数据
        if (usbManager.isConnected()) {
            val binaryData = byteArrayOf(0x01, 0x02, 0x03, 0x04)
            usbManager.sendBinary(binaryData)
        }
        
        // 10. 断开连接
        // usbManager.disconnect()
        
        // 11. 释放资源（在Activity/Fragment销毁时调用）
        // usbManager.release()
    }
    
    /**
     * 示例：在Activity中使用
     */
    class ExampleActivity : androidx.appcompat.app.AppCompatActivity() {
        private lateinit var usbManager: UsbCommunicationManager
        private val dataCallback = object : UsbDataCallback {
            override fun onTextDataReceived(data: String) {
                runOnUiThread {
                    // 更新UI显示接收到的数据
                    android.util.Log.d("USB", "收到数据: $data")
                }
            }
            
            override fun onBinaryDataReceived(data: ByteArray) {
                runOnUiThread {
                    // 处理二进制数据
                    android.util.Log.d("USB", "收到二进制数据")
                }
            }
            
            override fun onError(error: String) {
                runOnUiThread {
                    // 显示错误信息
                    android.util.Log.e("USB", "错误: $error")
                }
            }
        }
        
        override fun onCreate(savedInstanceState: android.os.Bundle?) {
            super.onCreate(savedInstanceState)
            
            // 初始化USB管理器
            usbManager = UsbCommunicationManager.getInstance(this)
            
            // 设置通信协议（可选，默认为VCP）
            // 使用VCP协议（默认）
            // usbManager.setProtocol(UsbProtocol.VCP)
            // 或使用HID协议
            // usbManager.setProtocol(UsbProtocol.HID)
            
            usbManager.setDataCallback(dataCallback)
            
            // 刷新设备列表
            usbManager.refreshAvailableDevices()
        }
        
        override fun onDestroy() {
            super.onDestroy()
            // 断开连接并释放资源
            usbManager.disconnect()
            usbManager.release()
        }
        
        private fun connectToDevice(device: UsbDeviceInfo) {
            usbManager.connect(device.device, dataCallback)
        }
        
        private fun sendCommand(command: String) {
            if (usbManager.isConnected()) {
                usbManager.sendText(command)
            }
        }
    }
}
