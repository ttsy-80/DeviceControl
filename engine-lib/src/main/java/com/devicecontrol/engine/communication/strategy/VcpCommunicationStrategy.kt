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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
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
        private const val RECV_TIMEOUT_MS = 2000
        private const val RECV_BUFFER_SIZE = 4096
        private const val CDC_REQUEST_TYPE = 0x21
        private const val CDC_SET_LINE_CODING = 0x20
        private const val CDC_SET_CONTROL_LINE_STATE = 0x22
    }

    private var controlInterface: UsbInterface? = null
    private var dataInterface: UsbInterface? = null
    private var inputEndpoint: UsbEndpoint? = null
    private var outputEndpoint: UsbEndpoint? = null
    private var connection: UsbDeviceConnection? = null
    private var callback: UsbDataCallback? = null
    private var receivingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val isReceiving = AtomicBoolean(false)
    private val accumulatedData = ByteArrayOutputStream()
    private var lastReceiveTime = 0L

    /** 波特率，连接前可改；默认 115200 */
    var baudRate: Int = 115200
        set(value) { if (!isConnected()) field = value }
    var dataBits: Int = 8
    var stopBits: Int = 0  // 0=1 stop, 1=1.5, 2=2
    var parity: Int = 0    // 0=none, 1=odd, 2=even, 3=mark, 4=space

    override fun isDeviceSupported(device: UsbDevice): Boolean {
        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            if (iface.interfaceClass == UsbConstants.USB_CLASS_COMM ||
                iface.interfaceClass == UsbConstants.USB_CLASS_CDC_DATA) return true
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
            dumpAllInterfaces(device)
            EngineLog.d(TAG, "connect: interfaceCount=${device.interfaceCount} vid=${device.vendorId} pid=${device.productId}")
            findAndClaimControlInterface(device, connection)
            if (!findAndClaimDataInterface(device, connection)) {
                EngineLog.w(TAG, "connect: 未找到或无法 claim 数据接口")
                return false
            }
            configureVcpPort()
            val inAddr = inputEndpoint?.address?.toString(16)?.uppercase() ?: "null"
            val outAddr = outputEndpoint?.address?.toString(16)?.uppercase() ?: "null"
            EngineLog.i(TAG, "connect: 成功 IN=0x$inAddr OUT=0x$outAddr baud=$baudRate")
            return inputEndpoint != null && outputEndpoint != null
        } catch (e: Exception) {
            EngineLog.e(TAG, "connect: ${e.message}", e)
            callback?.onError("VCP连接失败: ${e.message}")
            return false
        }
    }

    private fun dumpAllInterfaces(device: UsbDevice) {
        EngineLog.d(TAG, "========== 设备接口信息 ==========")
        EngineLog.d(TAG, "设备: VID=0x${device.vendorId.toString(16)} PID=0x${device.productId.toString(16)}")

        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            EngineLog.d(TAG, "接口[$i]:")
            EngineLog.d(TAG, "  Class: 0x${iface.interfaceClass.toString(16)} (${getInterfaceClassName(iface.interfaceClass)})")
            EngineLog.d(TAG, "  Subclass: 0x${iface.interfaceSubclass.toString(16)}")
            EngineLog.d(TAG, "  Protocol: 0x${iface.interfaceProtocol.toString(16)}")

            for (j in 0 until iface.endpointCount) {
                val ep = iface.getEndpoint(j)
                val dir = if (ep.direction == UsbConstants.USB_DIR_IN) "IN" else "OUT"
                val type = when (ep.type) {
                    UsbConstants.USB_ENDPOINT_XFER_CONTROL -> "CONTROL"
                    UsbConstants.USB_ENDPOINT_XFER_ISOC -> "ISOCHRONOUS"
                    UsbConstants.USB_ENDPOINT_XFER_BULK -> "BULK"
                    UsbConstants.USB_ENDPOINT_XFER_INT -> "INTERRUPT"
                    else -> "UNKNOWN"
                }
                EngineLog.d(TAG, "  端点[$j]: $dir $type, addr=0x${ep.address.toString(16)}, maxSize=${ep.maxPacketSize}")
            }
        }
        EngineLog.d(TAG, "=================================")
    }

    private fun getInterfaceClassName(cls: Int): String {
        return when (cls) {
            UsbConstants.USB_CLASS_COMM -> "USB_CLASS_COMM (CDC控制)"
            UsbConstants.USB_CLASS_CDC_DATA -> "USB_CLASS_CDC_DATA (CDC数据)"
            0xFF -> "厂商特定"
            else -> "其他(0x${cls.toString(16)})"
        }
    }

    private fun findAndClaimControlInterface(device: UsbDevice, connection: UsbDeviceConnection) {
        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            if (iface.interfaceClass == UsbConstants.USB_CLASS_COMM) {
                controlInterface = iface
                if (connection.claimInterface(iface, true)) {
                    EngineLog.d(TAG, "已 claim 控制接口 index=$i id=${iface.id}")
                } else {
                    EngineLog.w(TAG, "claim 控制接口失败 index=$i")
                }
                return
            }
        }
    }

    private fun findAndClaimDataInterface(device: UsbDevice, connection: UsbDeviceConnection): Boolean {
        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            EngineLog.i(TAG,"findAndClaimDataInterface interface:$iface")
            if (iface.interfaceClass != UsbConstants.USB_CLASS_CDC_DATA) continue
            dataInterface = iface
            if (!connection.claimInterface(iface, true)) {
                EngineLog.w(TAG, "claim 数据接口失败 index=$i")
                continue
            }
            try {
                connection.setInterface(iface)
                EngineLog.d(TAG, "setInterface(数据接口) index:$i 已调用")
            } catch (e: Exception) {
                EngineLog.d(TAG, "setInterface 跳过 error:${e}")
            }
            for (j in 0 until iface.endpointCount) {
                val ep = iface.getEndpoint(j)
                when (ep.direction) {
                    UsbConstants.USB_DIR_IN -> if (inputEndpoint == null) inputEndpoint = ep
                    UsbConstants.USB_DIR_OUT -> if (outputEndpoint == null) outputEndpoint = ep
                }
            }
            return inputEndpoint != null && outputEndpoint != null
        }
        return false
    }

    private fun configureVcpPort() {
        val conn = connection ?: return
        val ctrlId = controlInterface?.id ?: 0
        val lineCoding = byteArrayOf(
            (baudRate and 0xFF).toByte(),
            (baudRate shr 8 and 0xFF).toByte(),
            (baudRate shr 16 and 0xFF).toByte(),
            (baudRate shr 24 and 0xFF).toByte(),
            stopBits.toByte(),
            parity.toByte(),
            dataBits.toByte()
        )
        val r1 = conn.controlTransfer(CDC_REQUEST_TYPE, CDC_SET_LINE_CODING, 0, ctrlId, lineCoding, lineCoding.size, 5000)
        EngineLog.d(TAG, "SET_LINE_CODING result=$r1")
        val r2 = conn.controlTransfer(CDC_REQUEST_TYPE, CDC_SET_CONTROL_LINE_STATE, 0x03, ctrlId, null, 0, 5000)
        EngineLog.d(TAG, "SET_CONTROL_LINE_STATE result=$r2")
        Thread.sleep(50)
    }

    fun setPortParams(baudRate: Int, dataBits: Int = 8, stopBits: Int = 0, parity: Int = 0) {
        this.baudRate = baudRate
        this.dataBits = dataBits
        this.stopBits = stopBits
        this.parity = parity
        if (isConnected()) configureVcpPort()
    }

    override fun disconnect() {
        stopReceiving()
        dataInterface?.let { connection?.releaseInterface(it) }
        controlInterface?.let { connection?.releaseInterface(it) }
        connection?.close()
        connection = null
        controlInterface = null
        dataInterface = null
        inputEndpoint = null
        outputEndpoint = null
        callback = null
        accumulatedData.reset()
    }

    override fun isConnected(): Boolean = connection != null && dataInterface != null

    override fun sendText(text: String): Boolean = sendBinary(text.toByteArray(Charsets.UTF_8))

    override fun sendBinary(data: ByteArray): Boolean {
        if (!isConnected() || outputEndpoint == null) {
            callback?.onError("设备未连接或输出端点不可用")
            return false
        }
        val maxPacket = outputEndpoint!!.maxPacketSize
        var offset = 0
        while (offset < data.size) {
            val len = minOf(maxPacket, data.size - offset)
            val chunk = data.copyOfRange(offset, offset + len)
            val result = connection?.bulkTransfer(outputEndpoint, chunk, chunk.size, 3000) ?: -1

            if (result < 0) {
                EngineLog.w(TAG, "sendBinary error result=$result")
                callback?.onError("VCP发送失败 result=$result")
                return false
            } else {
                EngineLog.d(TAG, "sendBinary: result=$result")
            }
            offset += len
        }
        return true
    }

    override fun startReceiving() {
        if (!isConnected() || inputEndpoint == null) {
            EngineLog.w(TAG, "startReceiving: 未连接或无 IN 端点")
            callback?.onError("设备未连接或输入端点不可用")
            return
        }
        if (!isReceiving.compareAndSet(false, true)) {
            EngineLog.d(TAG, "startReceiving: 已在接收中")
            return
        }
        val ep = inputEndpoint!!
        val bufferSize = maxOf(ep.maxPacketSize, RECV_BUFFER_SIZE)
        EngineLog.i(TAG, "startReceiving: IN=0x${ep.address.toString(16)} bufferSize=$bufferSize timeout=${RECV_TIMEOUT_MS}ms")
        receivingJob = scope.launch {
            val buffer = ByteArray(bufferSize)
            var timeoutCount = 0
            var consecutiveErrors = 0
            while (isActive && isConnected() && isReceiving.get()) {
                try {
                    val result = connection?.bulkTransfer(inputEndpoint, buffer, buffer.size, RECV_TIMEOUT_MS) ?: -1
                    when {
                        result > 0 -> {
                            EngineLog.i(TAG, "startReceiving: bulkTransfer result=$result")
                            consecutiveErrors = 0
                            timeoutCount = 0
                            lastReceiveTime = System.currentTimeMillis()
                            processReceivedData(buffer.copyOf(result))
                        }
                        result == -1 -> {
                            timeoutCount++
                            if (accumulatedData.size() > 0 && (timeoutCount % 10 == 0 || accumulatedData.size() > 512 || System.currentTimeMillis() - lastReceiveTime > 300)) {
                                flushAccumulatedData()
                            }
                            if (timeoutCount == 1 || timeoutCount % 50 == 0) {
                                EngineLog.d(TAG, "startReceiving: IN 超时累计 $timeoutCount")
                            }
                        }
                        else -> EngineLog.w(TAG, "startReceiving: bulkTransfer error=$result")
                    }
                } catch (e: Exception) {
                    consecutiveErrors++
                    EngineLog.e(TAG, "startReceiving: ${e.message} 连续错误=$consecutiveErrors", e)
                    if (consecutiveErrors >= 5) {
                        callback?.onError("VCP接收连续错误")
                        break
                    }
                    delay(100)
                }
            }
            flushAccumulatedData()
            isReceiving.set(false)
            EngineLog.d(TAG, "startReceiving: 已退出")
        }
    }

    private fun processReceivedData(data: ByteArray) {
        accumulatedData.write(data, 0, data.size)
        val accumulated = accumulatedData.toByteArray()
        var lastNewLine = -1
        for (i in accumulated.indices) {
            if (accumulated[i] == 0x0A.toByte() || accumulated[i] == 0x0D.toByte()) {
                if (i > lastNewLine + 1) {
                    val line = accumulated.copyOfRange(lastNewLine + 1, i)
                    if (line.isNotEmpty()) dispatchReceived(line)
                }
                lastNewLine = i
            }
        }
        if (accumulated.size > 1024 || (accumulated.isNotEmpty() && System.currentTimeMillis() - lastReceiveTime > 500)) {
            flushAccumulatedData()
            return
        }
        if (lastNewLine >= 0 && lastNewLine < accumulated.size - 1) {
            accumulatedData.reset()
            accumulatedData.write(accumulated, lastNewLine + 1, accumulated.size - lastNewLine - 1)
        }
    }

    private fun dispatchReceived(data: ByteArray) {
        try {
            val text = String(data, Charsets.UTF_8)
            if (text.any { it.isLetterOrDigit() || it.isWhitespace() || it.isISOControl() }) {
                callback?.onTextDataReceived(text)
            } else {
                callback?.onBinaryDataReceived(data)
            }
        } catch (_: Exception) {
            callback?.onBinaryDataReceived(data)
        }
    }

    private fun flushAccumulatedData() {
        if (accumulatedData.size() == 0) return
        val data = accumulatedData.toByteArray()
        accumulatedData.reset()
        dispatchReceived(data)
    }

    override fun stopReceiving() {
        isReceiving.set(false)
        receivingJob?.cancel()
        receivingJob = null
        accumulatedData.reset()
    }

    fun clearBuffer() = accumulatedData.reset()
}
