package com.devicecontrol.engine.communication.strategy

import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import com.devicecontrol.engine.communication.UsbCommunicationStrategy
import com.devicecontrol.engine.communication.UsbDataCallback
import com.devicecontrol.engine.log.EngineLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * VCP (Virtual COM Port) 通信策略实现
 * 用于与CDC/ACM类USB设备（虚拟串口）进行通信
 */
class VcpCommunicationStrategy : UsbCommunicationStrategy {

    companion object {
        private const val TAG = "VcpStrategy"
    }

    private var usbInterface: UsbInterface? = null
    private var inputEndpoint: UsbEndpoint? = null
    private var outputEndpoint: UsbEndpoint? = null
    private var connection: UsbDeviceConnection? = null
    private var callback: UsbDataCallback? = null
    private var receivingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    
    override fun isDeviceSupported(device: UsbDevice): Boolean {
        // 检查设备是否为CDC/ACM类设备（虚拟串口）
        for (i in 0 until device.interfaceCount) {
            val usbInterface = device.getInterface(i)
            // CDC/ACM设备的接口类通常是USB_CLASS_CDC_DATA (0x0A)
            if (usbInterface.interfaceClass == UsbConstants.USB_CLASS_CDC_DATA
//                || usbInterface.interfaceClass == UsbConstants.USB_CLASS_CDC
                ) {
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
            
            // 查找CDC/ACM数据接口
            for (i in 0 until device.interfaceCount) {
                val usbInterface = device.getInterface(i)
                if (usbInterface.interfaceClass == UsbConstants.USB_CLASS_CDC_DATA
//                    || usbInterface.interfaceClass == UsbConstants.USB_CLASS_CDC
                    ) {
                    this.usbInterface = usbInterface
                    
                    // 请求接口权限
                    if (connection.claimInterface(usbInterface, true)) {
                        EngineLog.d(TAG, "connect: 已 claim 接口 index=$i endpointCount=${usbInterface.endpointCount}")
                        for (j in 0 until usbInterface.endpointCount) {
                            val endpoint = usbInterface.getEndpoint(j)
                            val dir = if (endpoint.direction == UsbConstants.USB_DIR_IN) "IN" else "OUT"
                            EngineLog.d(TAG, "connect: endpoint[$j] address=${endpoint.address} dir=$dir maxPacketSize=${endpoint.maxPacketSize} type=${endpoint.type}")
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
                        EngineLog.i(TAG, "connect: 成功 inputEp=${inputEndpoint?.address} outputEp=${outputEndpoint?.address}")
                        return true
                    } else {
                        EngineLog.w(TAG, "connect: claimInterface 失败 interfaceIndex=$i")
                    }
                }
            }
            EngineLog.w(TAG, "connect: 未找到CDC/ACM接口")
            return false
        } catch (e: Exception) {
            EngineLog.e(TAG, "connect: ${e.message}", e)
            callback?.onError("VCP连接失败: ${e.message}")
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
                callback?.onError("VCP发送数据失败，返回码: $result")
                false
            }
        } catch (e: Exception) {
            callback?.onError("VCP发送数据异常: ${e.message}")
            false
        }
    }
    
    override fun startReceiving() {
        if (!isConnected() || inputEndpoint == null) {
            EngineLog.w(TAG, "startReceiving: 未连接或输入端点不可用 connected=${isConnected()} inputEp=$inputEndpoint")
            callback?.onError("设备未连接或输入端点不可用")
            return
        }
        if (receivingJob?.isActive == true) {
            EngineLog.d(TAG, "startReceiving: 已在接收中，忽略")
            return
        }
        val ep = inputEndpoint!!
        EngineLog.i(TAG, "startReceiving: 开始 端点address=${ep.address} maxPacketSize=${ep.maxPacketSize} type=${ep.type}")
        receivingJob = scope.launch {
            val buffer = ByteArray(ep.maxPacketSize)
            var timeoutCount = 0
            while (isActive && isConnected()) {
                try {
                    val result = connection?.bulkTransfer(
                        inputEndpoint,
                        buffer,
                        buffer.size,
                        1000
                    ) ?: -1
                    when {
                        result > 0 -> {
                            EngineLog.d(TAG, "startReceiving: 收到字节数=$result data=${buffer.copyOf(result).take(32)}")
                            val receivedData = buffer.copyOf(result)
                            try {
                                val text = String(receivedData, Charsets.UTF_8)
                                if (text.any { it.isLetterOrDigit() || it.isWhitespace() || it.isISOControl() }) {
                                    callback?.onTextDataReceived(text)
                                } else {
                                    callback?.onBinaryDataReceived(receivedData)
                                }
                            } catch (e: Exception) {
                                callback?.onBinaryDataReceived(receivedData)
                            }
                            timeoutCount = 0
                        }
                        result == -1 -> {
                            timeoutCount++
                            if (timeoutCount % 30 == 1 && timeoutCount > 1) {
                                EngineLog.d(TAG, "startReceiving: bulkTransfer 超时(继续轮询) 累计约 ${timeoutCount} 次")
                            }
                        }
                        else -> {
                            EngineLog.w(TAG, "startReceiving: bulkTransfer 返回 error=$result (非超时)")
                        }
                    }
                } catch (e: Exception) {
                    EngineLog.e(TAG, "startReceiving: 异常 ${e.message}", e)
                    if (isActive) {
                        callback?.onError("VCP接收数据异常: ${e.message}")
                    }
                    break
                }
            }
            EngineLog.d(TAG, "startReceiving: 接收循环已退出 isActive=$isActive connected=${isConnected()}")
        }
    }
    
    override fun stopReceiving() {
        receivingJob?.cancel()
        receivingJob = null
    }
}
