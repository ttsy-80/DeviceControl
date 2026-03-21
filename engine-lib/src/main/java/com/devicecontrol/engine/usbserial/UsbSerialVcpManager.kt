package com.devicecontrol.engine.usbserial

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import com.devicecontrol.engine.log.EngineLog

/**
 * [UsbSerialVcp] 管理类：负责 USB 权限申请、设备列表刷新、连接/断开，并在各步骤使用 [EngineLog] 打日志。
 *
 * 使用步骤：
 * 1. 构造时传入 [Context]，会注册 USB 权限与设备插拔广播
 * 2. [getAvailableDevices] 获取当前可用的 VCP 设备列表
 * 3. [connect] 连接指定设备（若无权限会先请求，权限结果通过广播回调后再连接）
 * 4. [sendText] / [sendBinary] 发送，[startReceiving] 已由 [connect] 成功后自动调用
 * 5. [disconnect] 断开；[release] 释放资源（注销广播、断开连接）
 *
 * 设备拔出时会自动 [disconnect] 并打日志。
 */
class UsbSerialVcpManager(private val context: Context) {

    companion object {
        private const val TAG = "UsbSerialVcpMgr"
        private const val ACTION_USB_PERMISSION = "com.devicecontrol.engine.usbserial.USB_PERMISSION"
    }

    private val usbManager: UsbManager =
        context.getSystemService(Context.USB_SERVICE) as UsbManager

    private val vcp = UsbSerialVcp(context)

    private var pendingConnectDevice: UsbDevice? = null
    private var pendingCallback: UsbSerialVcpCallback? = null
    /** 当前已连接的设备（用于设备拔出时判断是否需断开） */
    private var currentDevice: UsbDevice? = null

    private val permissionIntent: PendingIntent by lazy {
        val intent = Intent(ACTION_USB_PERMISSION).apply {
            setPackage(context.packageName)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        PendingIntent.getBroadcast(context, 0, intent, flags)
    }

    @Suppress("DEPRECATION")
    private fun Intent.getUsbDeviceExtra(): UsbDevice? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
        } else {
            getParcelableExtra(UsbManager.EXTRA_DEVICE)
        }
    }

    private val usbPermissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            EngineLog.i(TAG, "onReceive permission: action=${intent.action}, thread=${Thread.currentThread().name}")
            if (intent.action != ACTION_USB_PERMISSION) {
                EngineLog.d(TAG, "忽略非权限广播 action=${intent.action}")
                return
            }
            val device = intent.getUsbDeviceExtra()
            val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
            EngineLog.i(TAG, "权限结果: device=${device?.deviceName ?: "null"}, vid=${device?.vendorId?.toString(16)}, pid=${device?.productId?.toString(16)}, granted=$granted")
            synchronized(this) {
                if (granted) {
                    if (device == null) {
                        EngineLog.e(TAG, "权限已授予但 Intent 中无 UsbDevice")
                        pendingCallback?.onError("USB 权限回调无设备信息")
                        clearPending()
                    } else {
                        EngineLog.i(TAG, "权限已授予，开始连接设备 ${device.deviceName}")
                        doConnect(device, pendingCallback)
                        clearPending()
                    }
                } else {
                    EngineLog.w(TAG, "USB 权限被用户拒绝 device=${device?.deviceName}")
                    pendingCallback?.onError("USB 权限被拒绝")
                    clearPending()
                }
            }
        }
    }

    private val usbDeviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device = intent.getUsbDeviceExtra()
                    EngineLog.i(TAG, "USB 设备接入: ${device?.deviceName ?: "null"}")
                    // don't auto connect
//                    if (device != null) {
//                        connect(device, pendingCallback)
//                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device = intent.getUsbDeviceExtra()
                    if (device != null && isCurrentDevice(device)) {
                        EngineLog.i(TAG, "当前连接设备已拔出，断开连接 device=${device.deviceName}")
                        disconnect()
                    }
                }
            }
        }
    }

    init {
        val permissionFilter = IntentFilter(ACTION_USB_PERMISSION)
        val deviceFilter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Context.RECEIVER_NOT_EXPORTED
            } else 0
            context.registerReceiver(usbPermissionReceiver, permissionFilter, flags)
            context.registerReceiver(usbDeviceReceiver, deviceFilter, flags)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(usbPermissionReceiver, permissionFilter)
            @Suppress("DEPRECATION")
            context.registerReceiver(usbDeviceReceiver, deviceFilter)
        }
        EngineLog.d(TAG, "init: 已注册 USB 权限与设备插拔广播")
    }

    private fun clearPending() {
        pendingConnectDevice = null
        pendingCallback = null
    }

    private fun isCurrentDevice(device: UsbDevice): Boolean {
        val cur = currentDevice ?: return false
        return cur.deviceName == device.deviceName
    }

    /** 波特率，连接前可修改；默认 115200 */
    var baudRate: Int
        get() = vcp.baudRate
        set(value) {
            vcp.baudRate = value
            EngineLog.d(TAG, "set baudRate=$value")
        }

    /** 为 true 时 [sendText] 自动在末尾加换行；多数嵌入式设备以换行作为命令结束才回包 */
    var appendNewlineToText: Boolean
        get() = vcp.appendNewlineToText
        set(value) { vcp.appendNewlineToText = value }
    /** 自动追加的换行符，默认 `\n` */
    var newlineSuffix: String
        get() = vcp.newlineSuffix
        set(value) { vcp.newlineSuffix = value }

    /**
     * 获取当前可用的 VCP 设备列表（仅包含 usb-serial-for-android 支持的设备）。
     * 无权限时列表可能为空。
     */
    fun getAvailableDevices(): List<UsbDevice> {
        val list = vcp.getAvailableDevices()
        EngineLog.d(TAG, "getAvailableDevices: 数量=${list.size}, devices=${list.map { "${it.deviceName}(vid=${it.vendorId.toString(16)}, pid=${it.productId.toString(16)})" }}")
        return list
    }

    /**
     * 刷新设备列表（与 [getAvailableDevices] 相同，仅语义上表示“刷新”；会打日志）。
     */
    fun refreshDevices() {
        EngineLog.d(TAG, "refreshDevices")
        getAvailableDevices()
    }

    /**
     * 扫描并连接：先刷新可用 VCP 设备列表，若扫到至少一个设备则连接第一个。
     * 参考 [com.devicecontrol.engine.communication.CommunicationManager.scanAndConnect] 的实现。
     *
     * @param callback 可选，若传入则作为本次连接的数据与错误回调
     * @return true 已发起连接（扫到至少一个设备，可能需等待用户授权后再真正连上），false 未扫到设备
     */
    fun scanAndConnect(callback: UsbSerialVcpCallback? = null): Boolean {
        EngineLog.d(TAG, "scanAndConnect: 开始扫描")
        val targets = getAvailableDevices()
        val first = targets.firstOrNull()
        if (first == null) {
            EngineLog.w(TAG, "scanAndConnect: 未扫描到设备")
            callback?.onError("未扫描到设备")
            return false
        }
        EngineLog.i(TAG, "scanAndConnect: 扫到 ${targets.size} 个设备，连接第一个 device=${first.deviceName}")
        connect(first, callback)
        return true
    }

    /**
     * 连接指定设备。若已有权限则直接连接；否则先请求权限，用户授权后在广播回调中再连接。
     * 连接成功后会自动 [startReceiving]。
     */
    fun connect(device: UsbDevice, callback: UsbSerialVcpCallback?) {
        EngineLog.i(TAG, "connect: device=${device.deviceName}, vid=${device.vendorId.toString(16)}, pid=${device.productId.toString(16)}")
        if (vcp.isConnected()) {
            EngineLog.w(TAG, "connect: 当前已连接，请先断开")
            callback?.onError("已连接其他设备，请先断开")
            return
        }
        if (!vcp.isDeviceSupported(device)) {
            EngineLog.w(TAG, "connect: 设备不被 VCP 支持")
            callback?.onError("该设备不被 usb-serial-for-android 支持")
            return
        }
        
        // 提前缓存供回调使用
        pendingConnectDevice = device
        pendingCallback = callback
        
        if (usbManager.hasPermission(device)) {
            EngineLog.d(TAG, "connect: 已有权限，直接连接")
            doConnect(device, callback)
            clearPending() // 直接连接后清理缓存
        } else {
            EngineLog.i(TAG, "connect: 无权限，请求 USB 权限")
            usbManager.requestPermission(device, permissionIntent)
            // 等待权限广播回来再做连接和清理
        }
    }

    private fun doConnect(device: UsbDevice, callback: UsbSerialVcpCallback?) {
        EngineLog.d(TAG, "doConnect: device=${device.deviceName}, baudRate=$baudRate")
        val ok = vcp.connect(device, callback)
        if (ok) {
            currentDevice = device
            EngineLog.i(TAG, "doConnect: 连接成功，启动接收")
            vcp.startReceiving()
            callback?.onConnect(true)
        } else {
            currentDevice = null
            EngineLog.e(TAG, "doConnect: 连接失败")
        }
    }

    /**
     * 断开连接。
     */
    fun disconnect() {
        EngineLog.i(TAG, "disconnect")
        vcp.disconnect()
        if (currentDevice != null) {
            pendingCallback?.onConnect(false)
        }
        currentDevice = null
        clearPending()
    }

    fun isConnected(): Boolean = vcp.isConnected()

    /**
     * 发送文本（UTF-8）。若 [appendNewlineToText] 为 true 会追加换行。
     */
    fun sendText(text: String): Boolean {
        EngineLog.d(TAG, "sendText: len=${text.length}")
        val ok = vcp.sendText(text)
        if (!ok) EngineLog.w(TAG, "sendText: 发送失败")
        return ok
    }

    /**
     * 发送一行（末尾加 `\n`）。多数嵌入式设备以换行作为命令结束才回执，收不到回包时可优先用此方法。
     */
    fun sendTextLine(text: String): Boolean {
        EngineLog.d(TAG, "sendTextLine: len=${text.length}")
        val ok = vcp.sendTextLine(text)
        if (!ok) EngineLog.w(TAG, "sendTextLine: 发送失败")
        return ok
    }

    /**
     * 发送二进制数据。
     */
    fun sendBinary(data: ByteArray): Boolean {
        EngineLog.d(TAG, "sendBinary: len=${data.size}")
        val ok = vcp.sendBinary(data)
        if (!ok) EngineLog.w(TAG, "sendBinary: 发送失败")
        return ok
    }

    /**
     * 开始接收（一般由 [connect] 成功后自动调用，无需手动调）。
     */
    fun startReceiving() {
        EngineLog.d(TAG, "startReceiving")
        vcp.startReceiving()
    }

    /**
     * 释放资源：注销广播、断开连接。应在 Activity/Fragment 销毁时调用。
     */
    fun release() {
        EngineLog.d(TAG, "release: 注销广播并断开")
        disconnect()
        try {
            context.unregisterReceiver(usbPermissionReceiver)
            context.unregisterReceiver(usbDeviceReceiver)
        } catch (e: Exception) {
            EngineLog.w(TAG, "release: 注销 Receiver 异常 ${e.message}")
        }
    }
}
