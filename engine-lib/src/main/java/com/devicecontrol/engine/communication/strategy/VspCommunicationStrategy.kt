package com.devicecontrol.engine.communication.strategy

import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import com.devicecontrol.engine.communication.UsbCommunicationStrategy
import com.devicecontrol.engine.communication.UsbDataCallback
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.devicecontrol.engine.log.EngineLog
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * VSP (Virtual Serial Port) 通信策略
 * 基于 usb-serial-for-android 库，支持 CDC/ACM、FTDI、CH340、Cp21xx 等常见 USB 串口芯片
 */
class VspCommunicationStrategy : UsbCommunicationStrategy {

    companion object {
        private const val TAG = "VspStrategy"
    }

    private var port: UsbSerialPort? = null
    private var connection: UsbDeviceConnection? = null
    private var callback: UsbDataCallback? = null
    private var receivingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    /** 默认波特率，可通过 [setBaudRate] 在连接前修改 */
    var baudRate: Int = 115200
        set(value) {
            if (!isConnected()) field = value
        }

    override fun isDeviceSupported(device: UsbDevice): Boolean {
        // 与库支持的设备类型一致：CDC、FTDI、CH340 等（通过接口类或常见 VID 粗略判断）
        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            if (iface.interfaceClass == UsbConstants.USB_CLASS_CDC_DATA
//                || iface.interfaceClass == UsbConstants.USB_CLASS_CDC
            ) return true
        }
        // 常见 USB 串口芯片 VID
        val vid = device.vendorId
        if (vid == 0x0403 || vid == 0x1a86 || vid == 0x10c4 || vid == 0x067b) return true
        return false
    }

    override fun connect(
        manager: UsbManager,
        device: UsbDevice,
        connection: UsbDeviceConnection,
        callback: UsbDataCallback?
    ): Boolean {
        return try {
            this.connection = connection
            this.callback = callback
            val drivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
            val driver = drivers.find { it.device == device }
                ?: run {
                    EngineLog.w(TAG, "connect: 未找到设备对应串口驱动 vid=${device.vendorId} pid=${device.productId}")
                    callback?.onError("VSP: 未找到该设备对应的串口驱动")
                    return false
                }
            val p = driver.ports.firstOrNull()
                ?: run {
                    EngineLog.w(TAG, "connect: 该设备无可用串口")
                    callback?.onError("VSP: 该设备无可用串口")
                    return false
                }
            p.open(connection)
            p.setParameters(baudRate, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            port = p
            EngineLog.i(TAG, "connect: 成功 baudRate=$baudRate")
            true
        } catch (e: IOException) {
            EngineLog.e(TAG, "connect: 失败 ${e.message}", e)
            callback?.onError("VSP 连接失败: ${e.message}")
            false
        }
    }

    override fun disconnect() {
        EngineLog.d(TAG, "disconnect")
        stopReceiving()
        try {
            port?.close()
        } catch (e: IOException) {
            EngineLog.w(TAG, "disconnect: port.close() ${e.message}")
        }
        port = null
        connection?.close()
        connection = null
        callback = null
    }

    override fun isConnected(): Boolean = port?.isOpen == true

    override fun sendText(text: String): Boolean = sendBinary(text.toByteArray(Charsets.UTF_8))

    override fun sendBinary(data: ByteArray): Boolean {
        val p = port
        if (p == null || !p.isOpen) {
            callback?.onError("VSP: 设备未连接")
            return false
        }
        return try {
            p.write(data, 2000)
            true
        } catch (e: IOException) {
            EngineLog.e(TAG, "sendBinary: ${e.message}")
            callback?.onError("VSP 发送失败: ${e.message}")
            false
        }
    }

    override fun startReceiving() {
        val p = port ?: return
        if (!p.isOpen) {
            callback?.onError("VSP: 设备未连接")
            return
        }
        if (receivingJob?.isActive == true) return
        receivingJob = scope.launch {
            val buffer = ByteArray(4096)
            while (isActive && p.isOpen) {
                try {
                    val n = p.read(buffer, 500)
                    if (n > 0) {
                        val data = buffer.copyOf(n)
                        val text = String(data, Charsets.UTF_8)
                        if (text.any { it.isLetterOrDigit() || it.isWhitespace() || it.isISOControl() }) {
                            callback?.onTextDataReceived(text)
                        } else {
                            callback?.onBinaryDataReceived(data)
                        }
                    }
                } catch (e: IOException) {
                    if (isActive) {
                        EngineLog.e(TAG, "startReceiving: ${e.message}")
                        callback?.onError("VSP 接收异常: ${e.message}")
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
