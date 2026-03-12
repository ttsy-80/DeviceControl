package com.devicecontrol.engine.communication.transport

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Build
import com.devicecontrol.engine.communication.CommunicationTransport
import com.devicecontrol.engine.communication.ConnectTarget
import com.devicecontrol.engine.communication.ConnectionState
import com.devicecontrol.engine.communication.DataCallback
import com.devicecontrol.engine.communication.UsbCommunicationStrategy
import com.devicecontrol.engine.communication.UsbDataCallback
import com.devicecontrol.engine.communication.UsbProtocol
import com.devicecontrol.engine.communication.model.UsbDeviceInfo
import com.devicecontrol.engine.communication.strategy.HidCommunicationStrategy
import com.devicecontrol.engine.communication.strategy.VcpCommunicationStrategy
import com.devicecontrol.engine.communication.strategy.VspCommunicationStrategy
import com.devicecontrol.engine.log.EngineLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * USB 连接层实现：实现 [CommunicationTransport]，封装 USB 权限、HID/VCP 策略
 * 业务层通过 [CommunicationManager] + [ConnectTarget.Usb] 使用，后续可并列增加 WiFi 等
 */
class UsbCommunicationTransport(private val context: Context) : CommunicationTransport {

    companion object {
        private const val TAG = "UsbTransport"
        private const val ACTION_USB_PERMISSION = "com.devicecontrol.engine.USB_PERMISSION"
    }

    private val usbManager: UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var currentStrategy: UsbCommunicationStrategy? = null
    private var currentDevice: UsbDevice? = null
    private var currentConnection: UsbDeviceConnection? = null
    private var dataCallback: DataCallback? = null

    var currentProtocol: UsbProtocol = UsbProtocol.VCP
        set(value) {
            if (isConnected()) return
            field = value
            refreshAvailableTargets("currentProtocol")
        }

    /** VSP 策略使用的波特率（仅 currentProtocol == VSP 时生效），默认 115200 */
    var vspBaudRate: Int = 115200
        set(value) { if (!isConnected()) field = value }

    /** VCP 策略使用的波特率（仅 currentProtocol == VCP 时生效），默认 115200 */
    var vcpBaudRate: Int = 115200
        set(value) { if (!isConnected()) field = value }

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    private val _availableTargets = MutableStateFlow<List<ConnectTarget>>(emptyList())

    /** 连接成功后的回调（由 CommunicationManager 设置，用于 CAN-USB 自动初始化等） */
    var onConnectedListener: (() -> Unit)? = null

    // API 31+ 需 FLAG_MUTABLE 才能收到权限结果；API 34+ 要求 PendingIntent 使用显式 Intent，故 setPackage
    private val permissionIntent: PendingIntent = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val intent = Intent(ACTION_USB_PERMISSION).apply { setPackage(context.packageName) }
            PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> PendingIntent.getBroadcast(
            context,
            0,
            Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        else -> @Suppress("DEPRECATION") PendingIntent.getBroadcast(
            context,
            0,
            Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
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
            EngineLog.i(TAG, "usbPermissionReceiver.onReceive: action=${intent.action}, thread=${Thread.currentThread().name}")
            if (ACTION_USB_PERMISSION != intent.action) {
                EngineLog.d(TAG, "usbPermissionReceiver: 忽略非权限广播 action=${intent.action}")
                return
            }
            val device = intent.getUsbDeviceExtra()
            val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
            EngineLog.i(TAG, "usbPermissionReceiver: device=${device?.deviceName ?: "null"}, vid=${device?.vendorId}, pid=${device?.productId}, granted=$granted")
            synchronized(this) {
                if (granted) {
                    if (device == null) {
                        EngineLog.e(TAG, "usbPermissionReceiver: 权限已授予但 Intent 中无 UsbDevice，无法连接")
                        dataCallback?.onError("USB 权限回调无设备信息")
                        _connectionState.value = ConnectionState.Disconnected
                    } else {
                        EngineLog.i(TAG, "usbPermissionReceiver: 权限已授予，开始 connectToDevice")
                        connectToDevice(device)
                    }
                } else {
                    EngineLog.w(TAG, "usbPermissionReceiver: USB权限被用户拒绝 device=${device?.deviceName}")
                    dataCallback?.onError("USB权限被拒绝")
                    _connectionState.value = ConnectionState.Disconnected
                }
            }
        }
    }

    private val usbDeviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> onDeviceAttached(intent.getUsbDeviceExtra())
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    if (intent.getUsbDeviceExtra() == currentDevice) {
                        EngineLog.i(TAG, "当前USB设备已拔出，断开连接")
                        disconnect()
                    }
                    refreshAvailableTargets("ACTION_USB_DEVICE_DETACHED")
                }
            }
        }
    }

    private fun onDeviceAttached(device: UsbDevice?) {
        if (device == null) return
        if (isConnected()) {
            refreshAvailableTargets("ACTION_USB_DEVICE_ATTACHED")
            return
        }
        refreshAvailableTargets("ACTION_USB_DEVICE_ATTACHED")
        val target = _availableTargets.value.find { it is ConnectTarget.Usb && (it as ConnectTarget.Usb).deviceInfo.deviceName == device.deviceName }
        if (target != null) {
            EngineLog.i(TAG, "ACTION_USB_DEVICE_ATTACHED: 未连接，连接新插入设备 ${device.deviceName}")
            connect(target, dataCallback)
        }
    }

    private fun asUsbCallback(callback: DataCallback?): UsbDataCallback? = callback?.let {
        object : UsbDataCallback {
            override fun onTextDataReceived(data: String) = it.onTextDataReceived(data)
            override fun onBinaryDataReceived(data: ByteArray) = it.onBinaryDataReceived(data)
            override fun onError(error: String) = it.onError(error)
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
    }

    override fun getAvailableTargets(): List<ConnectTarget> = _availableTargets.value

    override fun refreshAvailableTargets() {
        refreshAvailableTargets("outer")
    }

    private fun refreshAvailableTargets(method: String) {
        val strategy = when (currentProtocol) {
            UsbProtocol.HID -> HidCommunicationStrategy()
            UsbProtocol.VCP -> VcpCommunicationStrategy()
            UsbProtocol.VSP -> VspCommunicationStrategy().apply { baudRate = vspBaudRate }
        }
        val list = usbManager.deviceList.values
            .filter { strategy.isDeviceSupported(it) }
            .map { device ->
                ConnectTarget.Usb(
                    UsbDeviceInfo(
                        device = device,
                        deviceName = device.deviceName,
                        vendorId = device.vendorId,
                        productId = device.productId,
                        deviceClass = device.deviceClass,
                        supportedProtocols = listOf(currentProtocol.name)
                    )
                )
            }
        _availableTargets.value = list
        EngineLog.d(TAG, "refreshAvailableTargets method:$method protocol=${currentProtocol.name}, 数量=${list.size}")
    }

    override fun connect(target: ConnectTarget, callback: DataCallback?) {
        when (target) {
            is ConnectTarget.Usb -> {
                dataCallback = callback
                val device = target.deviceInfo.device
                EngineLog.i(TAG, "connect: device=${device.deviceName}, vid=${device.vendorId}, pid=${device.productId}")
                if (usbManager.hasPermission(device)) {
                    connectToDevice(device)
                } else {
                    EngineLog.d(TAG, "connect: 请求USB权限")
                    _connectionState.value = ConnectionState.RequestingPermission
                    usbManager.requestPermission(device, permissionIntent)
                }
            }
            is ConnectTarget.Wifi -> {
                EngineLog.w(TAG, "connect: USB传输不支持WiFi目标")
                callback?.onError("USB 传输不支持 WiFi 目标，请使用 WiFi 传输实现")
            }
        }
    }

    private fun connectToDevice(device: UsbDevice) {
        try {
            _connectionState.value = ConnectionState.Connecting
            val strategy = when (currentProtocol) {
                UsbProtocol.HID -> HidCommunicationStrategy()
                UsbProtocol.VCP -> VcpCommunicationStrategy().apply {
                    baudRate = vcpBaudRate
                    setPortParams(vcpBaudRate)
                }
                UsbProtocol.VSP -> VspCommunicationStrategy().apply { baudRate = vspBaudRate }
            }
            if (!strategy.isDeviceSupported(device)) {
                EngineLog.w(TAG, "connectToDevice: 设备不支持${currentProtocol.name}")
                dataCallback?.onError("设备不支持${currentProtocol.name}协议")
                _connectionState.value = ConnectionState.Disconnected
                return
            }
            val connection = usbManager.openDevice(device)
            if (connection == null) {
                EngineLog.e(TAG, "connectToDevice: 无法打开设备连接")
                dataCallback?.onError("无法打开设备连接")
                _connectionState.value = ConnectionState.Disconnected
                return
            }
            val cb = asUsbCallback(dataCallback)
            if (strategy.connect(usbManager, device, connection, cb)) {
                currentStrategy = strategy
                currentDevice = device
                currentConnection = connection
                onConnectedListener?.invoke()
                strategy.startReceiving()
                _connectionState.value = ConnectionState.Connected("USB-${currentProtocol.name}")
                EngineLog.i(TAG, "connectToDevice: 已连接 USB-${currentProtocol.name}")
            } else {
                connection.close()
                EngineLog.e(TAG, "connectToDevice: ${currentProtocol.name}策略连接失败")
                dataCallback?.onError("${currentProtocol.name}连接失败")
                _connectionState.value = ConnectionState.Disconnected
            }
        } catch (e: Exception) {
            EngineLog.e(TAG, "connectToDevice: 异常", e)
            dataCallback?.onError("连接异常: ${e.message}")
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    override fun disconnect() {
        EngineLog.d(TAG, "disconnect")
        currentStrategy?.disconnect()
        currentStrategy = null
        currentDevice = null
        currentConnection = null
        _connectionState.value = ConnectionState.Disconnected
    }

    override fun isConnected(): Boolean = currentStrategy?.isConnected() ?: false

    override fun sendText(text: String): Boolean {
        return if (currentStrategy?.isConnected() == true) {
            currentStrategy!!.sendText(text)
        } else {
            dataCallback?.onError("设备未连接")
            false
        }
    }

    override fun sendBinary(data: ByteArray): Boolean {
        return if (currentStrategy?.isConnected() == true) {
            currentStrategy!!.sendBinary(data)
        } else {
            dataCallback?.onError("设备未连接")
            false
        }
    }

    override fun getConnectionState(): StateFlow<ConnectionState> = _connectionState.asStateFlow()

    override fun getAvailableTargetsState(): StateFlow<List<ConnectTarget>> = _availableTargets.asStateFlow()

    override fun release() {
        EngineLog.d(TAG, "release: 注销广播")
        disconnect()
        try {
            context.unregisterReceiver(usbPermissionReceiver)
            context.unregisterReceiver(usbDeviceReceiver)
        } catch (e: Exception) {
            EngineLog.w(TAG, "release: 注销Receiver异常 ${e.message}")
        }
    }

    /** USB 专用：当前连接设备 */
    fun getCurrentDevice(): UsbDevice? = currentDevice
}
