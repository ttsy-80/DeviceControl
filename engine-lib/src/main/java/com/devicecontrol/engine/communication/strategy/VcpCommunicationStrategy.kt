package com.devicecontrol.engine.communication.strategy

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import com.devicecontrol.engine.communication.UsbCommunicationStrategy
import com.devicecontrol.engine.communication.UsbDataCallback
import com.devicecontrol.engine.log.EngineLog
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * VCP (Virtual COM Port) 通信策略实现
 * 用于与 CDC/ACM 类 USB 设备（虚拟串口）进行通信。
 *
 * - 区分控制接口(CDC)与数据接口(CDC_DATA)：先 claim 控制接口并下发 SET_LINE_CODING / SET_CONTROL_LINE_STATE，再 claim 数据接口做 bulk 收发。
 * - 收发端点：OUT=主机发、IN=主机收；日志中 IN=0x8x、OUT=0x0x。
 * - 部分设备数据接口需 setInterface(interface, 1) 才能收到 IN 数据，会尝试一次。
 */
class VcpCommunicationStrategy : UsbCommunicationStrategy {

    companion object {
        private const val TAG = "VcpStrategy"
        private const val READ_WAIT_MS = 500
        private const val WRITE_WAIT_MS = 2000
    }

    private var serialPort: UsbSerialPort? = null
    private var connection: UsbDeviceConnection? = null
    private var callback: UsbDataCallback? = null
    private var receivingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val isReceiving = AtomicBoolean(false)

    /** 波特率，连接前可改；默认 115200 */
    var baudRate: Int = 115200
    var dataBits: Int = UsbSerialPort.DATABITS_8
    var stopBits: Int = UsbSerialPort.STOPBITS_1
    var parity: Int = UsbSerialPort.PARITY_NONE

    override fun isDeviceSupported(device: UsbDevice): Boolean {
        val drivers = UsbSerialProber.getDefaultProber().findAllDrivers(null as UsbManager?) // Prober doesn't strictly need manager here if we just want to check device
        // But for prober in usb-serial-for-android, we usually probe via the manager list or a single device.
        // Let's use a simpler way: if any driver matches this device.
        val driver = UsbSerialProber.getDefaultProber().probeDevice(device)
        return driver != null
    }

    override fun connect(
        manager: UsbManager,
        device: UsbDevice,
        connection: UsbDeviceConnection,
        callback: UsbDataCallback?
    ): Boolean {
        this.connection = connection
        this.callback = callback

        val driver = UsbSerialProber.getDefaultProber().probeDevice(device)
        if (driver == null) {
            EngineLog.e(TAG, "connect: 未找到驱动")
            callback?.onError("未找到设备驱动")
            return false
        }

        if (driver.ports.isEmpty()) {
            EngineLog.e(TAG, "connect: 设备没有可用端口")
            callback?.onError("设备没有可用端口")
            return false
        }

        val port = driver.ports[0] // 默认使用第一个端口
        try {
            port.open(connection)
            port.setParameters(baudRate, dataBits, stopBits, parity)
            this.serialPort = port
            EngineLog.i(TAG, "connect: 成功 baud=$baudRate")
            return true
        } catch (e: Exception) {
            EngineLog.e(TAG, "connect: ${e.message}", e)
            callback?.onError("连接失败: ${e.message}")
            return false
        }
    }

    fun setPortParams(baudRate: Int, dataBits: Int = UsbSerialPort.DATABITS_8, stopBits: Int = UsbSerialPort.STOPBITS_1, parity: Int = UsbSerialPort.PARITY_NONE) {
        this.baudRate = baudRate
        this.dataBits = dataBits
        this.stopBits = stopBits
        this.parity = parity
        serialPort?.let {
            try {
                it.setParameters(baudRate, dataBits, stopBits, parity)
            } catch (e: IOException) {
                EngineLog.e(TAG, "setPortParams error: ${e.message}")
            }
        }
    }

    override fun disconnect() {
        stopReceiving()
        try {
            serialPort?.close()
        } catch (e: IOException) {
            EngineLog.w(TAG, "disconnect close error: ${e.message}")
        }
        serialPort = null
        connection = null
        callback = null
    }

    override fun isConnected(): Boolean = serialPort?.isOpen == true

    override fun sendText(text: String): Boolean = sendBinary(text.toByteArray(Charsets.UTF_8))

    override fun sendBinary(data: ByteArray): Boolean {
        val port = serialPort
        if (port == null || !port.isOpen) {
            callback?.onError("串口未连接")
            return false
        }
        return try {
            port.write(data, WRITE_WAIT_MS)
            EngineLog.d(TAG, "sendBinary: len=${data.size}")
            true
        } catch (e: IOException) {
            callback?.onError("发送失败: ${e.message}")
            false
        }
    }

    override fun startReceiving() {
        val port = serialPort
        if (port == null || !port.isOpen) {
            callback?.onError("串口未打开，无法接收")
            return
        }
        if (!isReceiving.compareAndSet(false, true)) return

        receivingJob = scope.launch {
            val buffer = ByteArray(4096)
            while (isActive && isConnected() && isReceiving.get()) {
                try {
                    val len = port.read(buffer, READ_WAIT_MS)
                    if (len > 0) {
                        val data = buffer.copyOf(len)
                        processData(data)
                    }
                } catch (e: IOException) {
                    EngineLog.e(TAG, "startReceiving error: ${e.message}")
                    if (isActive) {
                        callback?.onError("读取异常: ${e.message}")
                    }
                    break
                }
            }
            isReceiving.set(false)
        }
    }

    private fun processData(data: ByteArray) {
        // 直接回调二进制数据
        callback?.onBinaryDataReceived(data)
        // 尝试转换为文本回调（如果需要兼容旧逻辑）
        try {
            val text = String(data, Charsets.UTF_8)
            callback?.onTextDataReceived(text)
        } catch (_: Exception) {}
    }

    override fun stopReceiving() {
        isReceiving.set(false)
        receivingJob?.cancel()
        receivingJob = null
    }
}
