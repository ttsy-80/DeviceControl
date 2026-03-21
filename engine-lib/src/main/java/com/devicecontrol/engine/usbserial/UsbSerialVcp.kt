package com.devicecontrol.engine.usbserial

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import com.devicecontrol.engine.log.EngineLog
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.io.IOException

/**
 * 基于 usb-serial-for-android 的 USB VCP（虚拟串口）通讯实现。
 * 不依赖项目中 [com.devicecontrol.engine.communication] 下的策略或 Transport，仅使用：
 * - Android [UsbManager] / [UsbDevice]
 * - 库 [UsbSerialProber]、[UsbSerialPort] 及 [SerialInputOutputManager] 进行稳健的数据收发。
 *
 * 支持 CDC/ACM、FTDI、CH340、Cp21xx 等常见 USB 串口芯片。
 *
 * 使用步骤：
 * 1. [getAvailableDevices] 获取库支持的设备列表（需先有 USB 权限）
 * 2. [connect] 打开设备并设置波特率等参数
 * 3. [startReceiving] 启动 [SerialInputOutputManager] 异步监听数据
 * 4. [sendText] / [sendBinary] 发送数据
 * 5. [stopReceiving]、[disconnect] 断开
 */
class UsbSerialVcp(private val context: Context) : SerialInputOutputManager.Listener {

    companion object {
        private const val TAG = "UsbSerialVcp"
    }

    private val usbManager: UsbManager =
        context.getSystemService(Context.USB_SERVICE) as UsbManager

    private var connection: UsbDeviceConnection? = null
    private var port: UsbSerialPort? = null
    private var callback: UsbSerialVcpCallback? = null

    private var ioManager: SerialInputOutputManager? = null

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
                cb?.onError("无法打开设备连接（可能无权限或被占用）")
                return false
            }
            
        this.connection = conn
        val ports = driver.ports
        EngineLog.d(TAG, "connect: 串口数量=${ports.size}")
        
        if (ports.isEmpty()) {
            EngineLog.w(TAG, "connect: 该设备没有可用串口")
            conn.close()
            connection = null
            cb?.onError("该设备没有可用串口")
            return false
        }

        // 按顺序尝试打开所有支持的串口端口
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
                try {
                    p.dtr = true
                    p.rts = true
                } catch (e: Exception) {
                    EngineLog.w(TAG, "connect: 设置 DTR/RTS 失败, 忽略它")
                }
                
                port = p
                EngineLog.i(TAG, "connect: 成功 portIndex=$idx baudRate=$baudRate")
                return true
            } catch (e: Exception) {
                EngineLog.d(TAG, "connect: port[$idx] 打开失败 ${e.message}，尝试下一个")
                try { p.close() } catch (_: Exception) { }
            }
        }
        
        // 所有的 port 均打开失败
        EngineLog.e(TAG, "connect: 所有端口均无法成功打开")
        conn.close()
        connection = null
        cb?.onError("无法成功打开串口")
        return false
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
                EngineLog.d(TAG, "disconnect: port 已释放关闭")
            } catch (e: IOException) {
                EngineLog.w(TAG, "disconnect: port.close() 异常 ${e.message}")
            }
            port = null
        }
        
        connection?.close()
        connection = null
        callback = null
        EngineLog.d(TAG, "disconnect: 彻底断开完成")
    }

    fun isConnected(): Boolean = port?.isOpen == true

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
    fun sendTextLine(text: String): Boolean = sendBinary((text + "\r").toByteArray(Charsets.UTF_8))

    /**
     * 发送二进制数据。
     */
    fun sendBinary(data: ByteArray): Boolean {
        val p = port
        if (p == null || !p.isOpen) {
            EngineLog.w(TAG, "sendBinary: 串口未打开")
            callback?.onError("发送失败：串口未打开")
            return false
        }
        
        return try {
            // 直接由端口同步写入数据，库会自己处理分包与握手
            p.write(data, writeTimeoutMs)
            EngineLog.d(TAG, "sendBinary: 成功发送 len=${data.size}")
            true
        } catch (e: IOException) {
            EngineLog.e(TAG, "sendBinary: 发送失败 ${e.message}", e)
            callback?.onError("数据发送失败: ${e.message}")
            false
        }
    }

    /**
     * 开始后台异步接收数据。
     * 数据与异常直接通过 [SerialInputOutputManager.Listener] 的实现回传。
     */
    fun startReceiving() {
        if (ioManager != null) {
            EngineLog.d(TAG, "startReceiving: 已经在接收数据中，由于被重复调用跳过")
            return
        }
        val p = port
        if (p == null || !p.isOpen) {
            EngineLog.w(TAG, "startReceiving: 串口未打开无法启动接收")
            callback?.onError("串口未打开，无法启动接收")
            return
        }

        EngineLog.d(TAG, "startReceiving: 启动 SerialInputOutputManager readTimeoutMs=$readTimeoutMs")
        
        // 实例化官方推荐的 IO 管理器以安全管理读取与写入回调的后备线程
        ioManager = SerialInputOutputManager(p, this).apply {
            readTimeout = readTimeoutMs
            start()
        }
    }

    /**
     * 停止后台接收。
     */
    fun stopReceiving() {
        if (ioManager != null) {
            EngineLog.d(TAG, "stopReceiving: 停止 SerialInputOutputManager")
            ioManager?.stop()
            ioManager = null
        }
    }

    // ================== SerialInputOutputManager.Listener ==================

    override fun onNewData(data: ByteArray) {
        val bytesRead = data.size
        if (bytesRead > 0) {
            callback?.onDataReceived(data)
        }
    }

    override fun onRunError(e: Exception) {
        EngineLog.e(TAG, "onRunError: SerialInputOutputManager 抛出异常退出了 ${e.message}", e)
        callback?.onError("设备断开或读取异常: ${e.message}")
    }
}
