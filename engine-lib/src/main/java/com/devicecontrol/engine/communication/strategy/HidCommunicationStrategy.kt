package com.devicecontrol.engine.communication.strategy

import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import com.devicecontrol.engine.communication.UsbCommunicationStrategy
import com.devicecontrol.engine.communication.UsbDataCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * HID (Human Interface Device) 通信策略实现
 * 用于与HID类USB设备进行通信
 */
class HidCommunicationStrategy : UsbCommunicationStrategy {
    
    private var usbInterface: UsbInterface? = null
    private var inputEndpoint: UsbEndpoint? = null
    private var outputEndpoint: UsbEndpoint? = null
    private var connection: UsbDeviceConnection? = null
    private var callback: UsbDataCallback? = null
    private var receivingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    
    override fun isDeviceSupported(device: UsbDevice): Boolean {
        // 检查设备是否为HID类设备
        for (i in 0 until device.interfaceCount) {
            val usbInterface = device.getInterface(i)
            if (usbInterface.interfaceClass == UsbConstants.USB_CLASS_HID) {
                return true
            }
        }
        return false
    }
    
    override fun connect(
        manager: UsbManager,
        device: UsbDevice,
        connection: UsbDeviceConnection,
        callback: UsbDataCallback?
    ): Boolean {
        try {
            this.connection = connection
            this.callback = callback
            
            // 查找HID接口
            for (i in 0 until device.interfaceCount) {
                val usbInterface = device.getInterface(i)
                if (usbInterface.interfaceClass == UsbConstants.USB_CLASS_HID) {
                    this.usbInterface = usbInterface
                    
                    // 请求接口权限
                    if (connection.claimInterface(usbInterface, true)) {
                        // 查找输入和输出端点
                        for (j in 0 until usbInterface.endpointCount) {
                            val endpoint = usbInterface.getEndpoint(j)
                            when (endpoint.direction) {
                                UsbConstants.USB_DIR_IN -> {
                                    if (inputEndpoint == null) {
                                        inputEndpoint = endpoint
                                    }
                                }
                                UsbConstants.USB_DIR_OUT -> {
                                    if (outputEndpoint == null) {
                                        outputEndpoint = endpoint
                                    }
                                }
                            }
                        }
                        return true
                    }
                }
            }
            return false
        } catch (e: Exception) {
            callback?.onError("HID连接失败: ${e.message}")
            return false
        }
    }
    
    override fun disconnect() {
        stopReceiving()
        usbInterface?.let { interface_ ->
            connection?.releaseInterface(interface_)
        }
        connection?.close()
        connection = null
        usbInterface = null
        inputEndpoint = null
        outputEndpoint = null
        callback = null
    }
    
    override fun isConnected(): Boolean {
        return connection != null && usbInterface != null
    }
    
    override fun sendText(text: String): Boolean {
        return sendBinary(text.toByteArray(Charsets.UTF_8))
    }
    
    override fun sendBinary(data: ByteArray): Boolean {
        if (!isConnected() || outputEndpoint == null) {
            callback?.onError("设备未连接或输出端点不可用")
            return false
        }
        
        return try {
            val result = connection?.bulkTransfer(
                outputEndpoint,
                data,
                data.size,
                1000
            ) ?: -1
            
            if (result >= 0) {
                true
            } else {
                callback?.onError("HID发送数据失败，返回码: $result")
                false
            }
        } catch (e: Exception) {
            callback?.onError("HID发送数据异常: ${e.message}")
            false
        }
    }
    
    override fun startReceiving() {
        if (!isConnected() || inputEndpoint == null) {
            callback?.onError("设备未连接或输入端点不可用")
            return
        }
        
        if (receivingJob?.isActive == true) {
            return // 已经在接收中
        }
        
        receivingJob = scope.launch {
            val buffer = ByteArray(inputEndpoint!!.maxPacketSize)
            
            while (isActive && isConnected()) {
                try {
                    val result = connection?.bulkTransfer(
                        inputEndpoint,
                        buffer,
                        buffer.size,
                        1000
                    ) ?: -1
                    
                    if (result > 0) {
                        val receivedData = buffer.copyOf(result)
                        // 尝试作为文本解析，如果失败则作为二进制数据
                        try {
                            val text = String(receivedData, Charsets.UTF_8)
                            if (text.all { it.isLetterOrDigit() || it.isWhitespace() || it.isISOControl() }) {
                                callback?.onTextDataReceived(text)
                            } else {
                                callback?.onBinaryDataReceived(receivedData)
                            }
                        } catch (e: Exception) {
                            callback?.onBinaryDataReceived(receivedData)
                        }
                    } else if (result < 0 && result != -1) {
                        // 超时或其他错误，继续循环
                        continue
                    }
                } catch (e: Exception) {
                    if (isActive) {
                        callback?.onError("HID接收数据异常: ${e.message}")
                    }
                    break
                }
            }
        }
    }
    
    override fun stopReceiving() {
        receivingJob?.cancel()
        receivingJob = null
    }
}
