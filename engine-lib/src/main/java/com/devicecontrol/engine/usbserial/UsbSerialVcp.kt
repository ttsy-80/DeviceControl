package com.devicecontrol.engine.usbserial

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import com.devicecontrol.engine.communication.strategy.VcpCommunicationStrategy
import com.devicecontrol.engine.log.EngineLog
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * 基于 usb-serial-for-android 的 USB VCP（虚拟串口）通讯实现。
 * 不依赖项目中 [com.devicecontrol.engine.communication] 下的策略或 Transport，仅使用：
 * - Android [UsbManager] / [UsbDevice]
 * - 库 [UsbSerialProber]、[UsbSerialPort] 进行发现、打开、读写。
 *
 * 支持 CDC/ACM、FTDI、CH340、Cp21xx 等常见 USB 串口芯片。
 *
 * 使用步骤：
 * 1. [getAvailableDevices] 获取库支持的设备列表（需先有 USB 权限）
 * 2. [connect] 打开设备并设置波特率等参数
 * 3. [startReceiving] 开始后台接收
 * 4. [sendText] / [sendBinary] 发送数据
 * 5. [stopReceiving]、[disconnect] 断开
 */
class UsbSerialVcp(private val context: Context) {

    companion object {
        private const val TAG = "UsbSerialVcp"
        private const val CDC_REQUEST_TYPE = 0x21
        private const val CDC_SET_LINE_CODING = 0x20
        private const val CDC_SET_CONTROL_LINE_STATE = 0x22
        private const val RAW_CDC_RECV_TIMEOUT_MS = 2000
        private const val RAW_CDC_RECV_BUFFER_SIZE = 4096
    }

    private val usbManager: UsbManager =
        context.getSystemService(Context.USB_SERVICE) as UsbManager

    private var connection: UsbDeviceConnection? = null
    private var port: UsbSerialPort? = null
    private var callback: UsbSerialVcpCallback? = null
    private var receiveJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    /** Raw CDC 回退路径：库打开失败时使用（如控制接口无 endpoint 的设备） */
    private var rawCdcControlInterface: UsbInterface? = null
    private var rawCdcDataInterface: UsbInterface? = null
    private var rawCdcIn: UsbEndpoint? = null
    private var rawCdcOut: UsbEndpoint? = null
    private var rawCdcControlId: Int = 0

    /** 波特率，仅在未连接时可修改，默认 115200 */
    var baudRate: Int = 115200
        set(value) {
            if (!isConnected()) field = value
        }

    /** 读超时（ms），默认 500 */
    var readTimeoutMs: Int = 500
        set(value) { if (value > 0) field = value }

    /** 写超时（ms），默认 2000 */
    var writeTimeoutMs: Int = 2000
        set(value) { if (value > 0) field = value }

    /**
     * 为 true 时，[sendText] 会在末尾自动追加换行符（默认 `\n`）。
     * 很多嵌入式设备以换行作为命令结束符，收到完整行后才回包。
     */
    var appendNewlineToText: Boolean = false
    /** [appendNewlineToText] 为 true 时使用的换行符，默认 `\n`，可改为 `\r\n`。 */
    var newlineSuffix: String = "\n"

    /**
     * 获取当前被 usb-serial-for-android 支持的 USB 设备列表。
     * 调用前需确保已对目标设备授予 USB 权限（如 [UsbManager.requestPermission]）。
     */
    fun getAvailableDevices(): List<UsbDevice> {
        EngineLog.d(TAG, "getAvailableDevices: 开始扫描")
        val drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        val list = drivers.map { it.device }.distinct()
        EngineLog.d(TAG, "getAvailableDevices: 扫到 ${list.size} 个设备")
        return list
    }

    /**
     * 检查设备是否被库支持（可作为 VCP 打开）。
     */
    fun isDeviceSupported(device: UsbDevice): Boolean {
        val drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        val supported = drivers.any { it.device == device }
        EngineLog.d(TAG, "isDeviceSupported: device=${device.deviceName} supported=$supported")
        return supported
    }

    /**
     * 连接指定 USB 设备并打开第一个可用的串口。
     * @param device 由 [getAvailableDevices] 得到或 [UsbManager.getDeviceList] 中取得
     * @param cb 数据与错误回调，可为 null
     * @return 是否连接并打开串口成功
     */
    fun connect(device: UsbDevice, cb: UsbSerialVcpCallback?): Boolean {
        EngineLog.d(TAG, "connect: device=${device.deviceName}, vid=${device.vendorId.toString(16)}, pid=${device.productId.toString(16)}, baudRate=$baudRate")
        if (isConnected()) {
            EngineLog.w(TAG, "connect: 当前已连接，请先断开")
            cb?.onError("已连接其他设备，请先断开")
            return false
        }
        callback = cb
        val drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        val driver = drivers.find { it.device == device }
            ?: run {
                EngineLog.w(TAG, "connect: 未找到设备对应驱动 vid=${device.vendorId.toString(16)} pid=${device.productId.toString(16)}")
                cb?.onError("该设备不被 usb-serial-for-android 支持（VID=${device.vendorId.toString(16)} PID=${device.productId.toString(16)}）")
                return false
            }
        EngineLog.d(TAG, "connect: 已找到驱动，尝试 openDevice")
        val conn = usbManager.openDevice(device)
            ?: run {
                EngineLog.e(TAG, "connect: 无法打开设备连接（权限或占用）")
                cb?.onError("无法打开设备连接（权限或占用）")
                return false
            }
        connection = conn
        val ports = driver.ports
        EngineLog.d(TAG, "connect: 串口数量=${ports.size}")
        if (ports.isEmpty()) {
            EngineLog.w(TAG, "connect: 该设备没有可用串口")
            conn.close()
            connection = null
            cb?.onError("该设备没有可用串口")
            return false
        }
        val cdc = tryOpenRawCdc(device, conn)
        if (cdc) {
            return true
        }
        for (idx in ports.indices) {
            val p = ports[idx]
            try {
                EngineLog.d(TAG, "connect: 尝试打开 port[$idx]")
                p.open(conn)
                p.setParameters(
                    baudRate,
                    UsbSerialPort.DATABITS_8,
                    UsbSerialPort.STOPBITS_1,
                    UsbSerialPort.PARITY_NONE
                )
                port = p
                EngineLog.i(TAG, "connect: 成功 portIndex=$idx baudRate=$baudRate")
                return true
            } catch (e: Exception) {
                EngineLog.d(TAG, "connect: port[$idx] 打开失败 ${e.message}，尝试下一个")
                try { p.close() } catch (_: Exception) { }
                if (idx == ports.lastIndex) {
                    EngineLog.w(TAG, "connect: 库所有 port 均打开失败 ${e.message}，尝试 Raw CDC 回退")
                    val fallbackOk = tryOpenRawCdc(device, conn)
                    if (fallbackOk) {
                        EngineLog.i(TAG, "connect: Raw CDC 回退成功 baudRate=$baudRate")
                        return true
                    }
                    EngineLog.e(TAG, "connect: 所有 port 均打开失败且 Raw CDC 回退失败", e)
                    conn.close()
                    connection = null
                    cb?.onError("无法打开串口: ${e.message}")
                    return false
                }
            }
        }
        EngineLog.e(TAG, "connect: 无法打开串口")
        connection = null
        cb?.onError("无法打开串口")
        return false
    }

    /** 是否为 CDC 设备（有 COMM + CDC_DATA 接口），可走 Raw CDC 回退 */
    private fun isCdcDevice(device: UsbDevice): Boolean {
        var hasComm = false
        var hasData = false
        for (i in 0 until device.interfaceCount) {
            val c = device.getInterface(i).interfaceClass
            if (c == UsbConstants.USB_CLASS_COMM) hasComm = true
            if (c == UsbConstants.USB_CLASS_CDC_DATA) hasData = true
        }
        return hasComm && hasData
    }

    /**
     * Raw CDC 打开：claim 控制/数据接口，SET_LINE_CODING，用 bulk 端点收发。
     * 适用于库打开失败的情况（如控制接口无 endpoint 导致 getEndpoint(0) 越界）。
     *
     * 注意：库在 p.open(conn) 时可能已 claim 了控制接口，随后在 getEndpoint(0) 崩溃，
     * 接口仍被该 connection 占用，导致此处 claim 失败。因此先释放设备上所有接口再 claim。
     */
    private fun tryOpenRawCdc(device: UsbDevice, conn: UsbDeviceConnection): Boolean {
        if (!isCdcDevice(device)) {
            EngineLog.d(TAG, "tryOpenRawCdc: 非 CDC 设备，跳过")
            return false
        }
        EngineLog.d(TAG, "tryOpenRawCdc: 开始")
        // 库在 port.open(conn) 失败前可能已 claim 了控制接口，先全部释放再 claim（与 VcpCommunicationStrategy 行为一致：使用未被占用的 connection）
        for (i in 0 until device.interfaceCount) {
            try {
                conn.releaseInterface(device.getInterface(i))
                EngineLog.d(TAG, "tryOpenRawCdc: 已释放接口 index=$i")
            } catch (_: Exception) { }
        }
        var ctrlIface: UsbInterface? = null
        var dataIface: UsbInterface? = null
        var inEp: UsbEndpoint? = null
        var outEp: UsbEndpoint? = null
        EngineLog.d(TAG, "tryOpenRawCdc: interfaceCount=${device.interfaceCount} vid=${device.vendorId} pid=${device.productId}")
        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            EngineLog.d(TAG, "tryOpenRawCdc: iface index=$i id=${iface.id} class=${iface.interfaceClass} endpointCount=${iface.endpointCount}")
            when (iface.interfaceClass) {
                UsbConstants.USB_CLASS_COMM -> {
                    if (ctrlIface == null) {
                        if (conn.claimInterface(iface, true)) {
                            ctrlIface = iface
                            rawCdcControlId = iface.id
                            EngineLog.d(TAG, "tryOpenRawCdc: 已 claim 控制接口 id=${iface.id}")
                        } else {
                            EngineLog.w(TAG, "tryOpenRawCdc: claim 控制接口失败 index=$i")
                        }
                    }
                }
                UsbConstants.USB_CLASS_CDC_DATA -> {
                    if (dataIface == null) {
                        if (conn.claimInterface(iface, true)) {
                            dataIface = iface
                            try {
                                conn.setInterface(iface)
                                EngineLog.d(TAG, "tryOpenRawCdc: setInterface(数据接口) 已调用")
                            } catch (e: Exception) {
                                EngineLog.d(TAG, "tryOpenRawCdc: setInterface 跳过 ${e.message}")
                            }
                            // 部分设备需切到 alternate setting 1 才能收到 IN 数据（参考 VcpCommunicationStrategy）
                            try {
                                conn.setInterface(iface)
                                EngineLog.d(TAG, "tryOpenRawCdc: setInterface(数据接口, 1) 已调用")
                            } catch (e: Exception) {
                                EngineLog.d(TAG, "tryOpenRawCdc: setInterface(iface,1) 跳过 ${e.message}")
                            }
                            for (j in 0 until iface.endpointCount) {
                                val ep = iface.getEndpoint(j)
                                when (ep.direction) {
                                    UsbConstants.USB_DIR_IN -> if (inEp == null) inEp = ep
                                    UsbConstants.USB_DIR_OUT -> if (outEp == null) outEp = ep
                                }
                            }
                            EngineLog.d(TAG, "tryOpenRawCdc: 已 claim 数据接口 in=${inEp != null} out=${outEp != null}")
                        } else {
                            EngineLog.w(TAG, "tryOpenRawCdc: claim 数据接口失败 index=$i")
                        }
                    }
                }
            }
        }
        if (ctrlIface == null || dataIface == null || inEp == null || outEp == null) {
            EngineLog.w(TAG, "tryOpenRawCdc: 缺少接口或端点 ctrl=${ctrlIface != null} data=${dataIface != null} in=${inEp != null} out=${outEp != null}")
            ctrlIface?.let { conn.releaseInterface(it) }
            dataIface?.let { conn.releaseInterface(it) }
            return false
        }
        val lineCoding = byteArrayOf(
            (baudRate and 0xFF).toByte(),
            (baudRate shr 8 and 0xFF).toByte(),
            (baudRate shr 16 and 0xFF).toByte(),
            (baudRate shr 24 and 0xFF).toByte(),
            0, 0, 8
        )
        val r1 = conn.controlTransfer(CDC_REQUEST_TYPE, CDC_SET_LINE_CODING, 0, rawCdcControlId, lineCoding, lineCoding.size, 5000)
        val r2 = conn.controlTransfer(CDC_REQUEST_TYPE, CDC_SET_CONTROL_LINE_STATE, 0x03, rawCdcControlId, null, 0, 5000)
        EngineLog.d(TAG, "tryOpenRawCdc: SET_LINE_CODING=$r1 SET_CONTROL_LINE_STATE=$r2")
        Thread.sleep(50)
        rawCdcControlInterface = ctrlIface
        rawCdcDataInterface = dataIface
        rawCdcIn = inEp
        rawCdcOut = outEp
        return true
    }

    /**
     * 断开连接并释放串口与 USB 连接。
     */
    fun disconnect() {
        EngineLog.d(TAG, "disconnect: 开始")
        stopReceiving()
        if (port != null) {
            try {
                port?.close()
                EngineLog.d(TAG, "disconnect: port 已关闭")
            } catch (e: IOException) {
                EngineLog.w(TAG, "disconnect: port.close() 异常 ${e.message}")
            }
            port = null
        } else {
            rawCdcDataInterface?.let { connection?.releaseInterface(it) }
            rawCdcControlInterface?.let { connection?.releaseInterface(it) }
            EngineLog.d(TAG, "disconnect: Raw CDC 接口已释放")
            rawCdcControlInterface = null
            rawCdcDataInterface = null
            rawCdcIn = null
            rawCdcOut = null
        }
        connection?.close()
        connection = null
        callback = null
        EngineLog.d(TAG, "disconnect: 完成")
    }

    fun isConnected(): Boolean =
        port?.isOpen == true || (connection != null && rawCdcIn != null && rawCdcOut != null)

    /**
     * 发送字符串（UTF-8 编码）。
     * 若 [appendNewlineToText] 为 true，会在末尾追加 [newlineSuffix]（默认 `\n`），便于设备按行回包。
     */
    fun sendText(text: String): Boolean {
        val s = if (appendNewlineToText) text + newlineSuffix else text
        return sendBinary(s.toByteArray(Charsets.UTF_8))
    }

    /**
     * 发送一行字符串（等价于在末尾加 `\n` 再发送）。
     * 多数嵌入式串口以换行作为命令结束，收到完整行后才回执，建议用此方法或设置 [appendNewlineToText]=true。
     */
    fun sendTextLine(text: String): Boolean = sendBinary((text + "\r\n").toByteArray(Charsets.UTF_8))

    /**
     * 发送二进制数据。
     */
    fun sendBinary(data: ByteArray): Boolean {
        val p = port
        if (p != null) {
            if (!p.isOpen) {
                EngineLog.w(TAG, "sendBinary: 串口未打开")
                callback?.onError("串口未打开")
                return false
            }
            return try {
                val result =  p.write(data, writeTimeoutMs)
                EngineLog.d(TAG, "sendBinary: 成功 len=${data.size} result:$result")
                true
            } catch (e: IOException) {
                EngineLog.e(TAG, "sendBinary: 发送失败 ${e.message}", e)
                callback?.onError("发送失败: ${e.message}")
                false
            }
        }
        val outEp = rawCdcOut
        val conn = connection
        if (outEp == null || conn == null) {
            EngineLog.w(TAG, "sendBinary: 设备未连接")
            callback?.onError("设备未连接")
            return false
        }
        val maxPacket = outEp.maxPacketSize
        var offset = 0
        while (offset < data.size) {
            val len = minOf(maxPacket, data.size - offset)
            val result = conn.bulkTransfer(outEp, data.copyOfRange(offset, offset + len), len, writeTimeoutMs)
            if (result < 0) {
                EngineLog.w(TAG, "sendBinary: Raw CDC bulkTransfer 失败 result=$result")
                callback?.onError("发送失败 result=$result")
                return false
            }
            offset += len
        }
        EngineLog.d(TAG, "sendBinary: Raw CDC 成功 len=${data.size}")
        return true
    }

    /**
     * 开始后台接收；数据通过 [UsbSerialVcpCallback.onDataReceived] 回调。
     */
    fun startReceiving() {
        if (receiveJob?.isActive == true) {
            EngineLog.d(TAG, "startReceiving: 已在接收中，跳过")
            return
        }
        val p = port
        if (p != null) {
            if (!p.isOpen) {
                EngineLog.w(TAG, "startReceiving: 串口未打开")
                callback?.onError("串口未打开，无法接收")
                return
            }
            EngineLog.d(TAG, "startReceiving: 启动接收循环(库) readTimeoutMs=$readTimeoutMs")
            receiveJob = scope.launch {
                val buffer = ByteArray(4096)
                while (isActive && p.isOpen) {
                    try {
                        val n = p.read(buffer, readTimeoutMs)
                        if (n > 0) {
                            EngineLog.d(TAG, "startReceiving: 读到 $n 字节")
                            callback?.onDataReceived(buffer.copyOf(n))
                        }
                    } catch (e: IOException) {
                        if (isActive) {
                            EngineLog.e(TAG, "startReceiving: 接收异常 ${e.message}", e)
                            callback?.onError("接收异常: ${e.message}")
                        }
                        break
                    }
                }
                EngineLog.d(TAG, "startReceiving: 接收循环已退出")
            }
            return
        }
        val inEp = rawCdcIn
        val conn = connection
        if (inEp == null || conn == null) {
            EngineLog.w(TAG, "startReceiving: port/raw 均不可用，跳过")
            callback?.onError("串口未打开，无法接收")
            return
        }
        EngineLog.d(TAG, "startReceiving: 启动接收循环(Raw CDC) timeout=$RAW_CDC_RECV_TIMEOUT_MS maxPacketSize:${inEp.maxPacketSize}")
        receiveJob = scope.launch {
            val buffer = ByteArray(RAW_CDC_RECV_BUFFER_SIZE.coerceAtLeast(inEp.maxPacketSize))
            var printCount = 0
            while (isActive && connection != null && rawCdcIn != null) {
                try {
                    val n = conn.bulkTransfer(inEp, buffer, buffer.size, RAW_CDC_RECV_TIMEOUT_MS)
                    if (n > 0) {
                        EngineLog.d(TAG, "startReceiving: Raw CDC 读到 $n 字节")
                        callback?.onDataReceived(buffer.copyOf(n))
                    } else {
                        printCount++
                        if (printCount == 1 || printCount % 20 == 0) {
                            EngineLog.w(TAG, "startReceiving: Raw CDC 读到 $n 字节 printCount:$printCount")
                        }
                    }
                } catch (e: IOException) {
                    if (isActive) {
                        EngineLog.e(TAG, "startReceiving: Raw CDC 接收异常 ${e.message}", e)
                        callback?.onError("接收异常: ${e.message}")
                    }
                    break
                }
            }
            EngineLog.d(TAG, "startReceiving: Raw CDC 接收循环已退出")
        }
    }

    /**
     * 停止后台接收。
     */
    fun stopReceiving() {
        EngineLog.d(TAG, "stopReceiving")
        receiveJob?.cancel()
        receiveJob = null
    }
}
