package com.devicecontrol.engine.communication

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Build
import com.devicecontrol.engine.communication.model.UsbDeviceInfo
import com.devicecontrol.engine.communication.strategy.HidCommunicationStrategy
import com.devicecontrol.engine.communication.strategy.VcpCommunicationStrategy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * USB通信管理器
 * 统一管理不同的USB通信策略（HID、VCP等）
 * 采用单例模式，提供全局访问
 * 
 * 使用说明：
 * 1. 默认使用VCP通信协议
 * 2. 通过 setProtocol() 方法可以切换通信协议（HID 或 VCP）
 * 3. refreshAvailableDevices() 只显示支持当前设置协议（默认VCP）的设备
 * 4. 调用 connect() 方法连接设备，将使用当前设置的协议
 * 5. 每次只能使用一个连接策略（要么HID，要么VCP）
 * 6. 切换协议后会自动刷新可用设备列表
 */
class UsbCommunicationManager private constructor(private val context: Context) {
    
    companion object {
        private const val ACTION_USB_PERMISSION = "com.devicecontrol.engine.USB_PERMISSION"
        
        @Volatile
        private var INSTANCE: UsbCommunicationManager? = null
        
        /**
         * 获取USB通信管理器实例
         */
        fun getInstance(context: Context): UsbCommunicationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UsbCommunicationManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val usbManager: UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var currentStrategy: UsbCommunicationStrategy? = null
    private var currentDevice: UsbDevice? = null
    private var currentConnection: UsbDeviceConnection? = null
    private var dataCallback: UsbDataCallback? = null
    
    /**
     * 当前使用的通信协议，默认为VCP
     */
    private var currentProtocol: UsbProtocol = UsbProtocol.VCP
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _availableDevices = MutableStateFlow<List<UsbDeviceInfo>>(emptyList())
    val availableDevices: StateFlow<List<UsbDeviceInfo>> = _availableDevices.asStateFlow()
    
    private val permissionIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    } else {
        @Suppress("DEPRECATION")
        PendingIntent.getBroadcast(
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
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    val device = intent.getUsbDeviceExtra()
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.let { connectToDevice(it) }
                    } else {
                        dataCallback?.onError("USB权限被拒绝")
                        _connectionState.value = ConnectionState.Disconnected
                    }
                }
            }
        }
    }
    
    private val usbDeviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device = intent.getUsbDeviceExtra()
                    device?.let { refreshAvailableDevices() }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device = intent.getUsbDeviceExtra()
                    if (device == currentDevice) {
                        disconnect()
                    }
                    refreshAvailableDevices()
                }
            }
        }
    }
    
    init {
        // 注册USB权限和设备广播接收器
        val permissionFilter = IntentFilter().apply {
            addAction(ACTION_USB_PERMISSION)
        }
        
        val deviceFilter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        
        // Android 12 (API 31) 及以上版本需要指定 RECEIVER_EXPORTED 或 RECEIVER_NOT_EXPORTED
        // RECEIVER_NOT_EXPORTED 常量在 API 33 引入，但值为 0，可以直接使用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // API 33+ 使用常量，API 31-32 使用常量值 0 (RECEIVER_NOT_EXPORTED = 0)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Context.RECEIVER_NOT_EXPORTED
            } else {
                0 // RECEIVER_NOT_EXPORTED 的值
            }
            context.registerReceiver(usbPermissionReceiver, permissionFilter, flags)
            context.registerReceiver(usbDeviceReceiver, deviceFilter, flags)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(usbPermissionReceiver, permissionFilter)
            @Suppress("DEPRECATION")
            context.registerReceiver(usbDeviceReceiver, deviceFilter)
        }
        
        refreshAvailableDevices()
    }
    
    /**
     * 刷新可用设备列表
     * 只显示支持当前设置协议（默认VCP）的设备
     */
    fun refreshAvailableDevices() {
        val deviceList = mutableListOf<UsbDeviceInfo>()
        val deviceMap = usbManager.deviceList
        
        // 根据当前设置的协议创建对应的策略
        val strategy = when (currentProtocol) {
            UsbProtocol.HID -> HidCommunicationStrategy()
            UsbProtocol.VCP -> VcpCommunicationStrategy()
        }
        
        for (device in deviceMap.values) {
            // 只检查当前协议是否支持该设备
            if (strategy.isDeviceSupported(device)) {
                deviceList.add(
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
        }
        
        _availableDevices.value = deviceList
    }
    
    /**
     * 设置通信协议
     * @param protocol 要使用的协议（HID 或 VCP）
     * 注意：如果当前已连接，需要先断开连接才能切换协议
     * 切换协议后会自动刷新可用设备列表
     */
    fun setProtocol(protocol: UsbProtocol) {
        if (isConnected()) {
            dataCallback?.onError("请先断开当前连接，再切换协议")
            return
        }
        
        val protocolChanged = currentProtocol != protocol
        currentProtocol = protocol
        
        // 如果协议发生变化，刷新设备列表
        if (protocolChanged) {
            refreshAvailableDevices()
        }
    }
    
    /**
     * 获取当前设置的通信协议
     */
    fun getProtocol(): UsbProtocol {
        return currentProtocol
    }
    
    /**
     * 设置数据回调
     */
    fun setDataCallback(callback: UsbDataCallback?) {
        this.dataCallback = callback
    }
    
    /**
     * 连接到指定设备
     * 使用当前设置的通信协议（通过 setProtocol() 设置，默认为VCP）
     * 
     * @param device USB设备
     * @param callback 数据回调（可选，如果为null则使用之前设置的回调）
     */
    fun connect(device: UsbDevice, callback: UsbDataCallback? = null) {
        if (callback != null) {
            this.dataCallback = callback
        }
        
        // 检查是否已有权限
        if (usbManager.hasPermission(device)) {
            connectToDevice(device)
        } else {
            // 请求权限
            _connectionState.value = ConnectionState.RequestingPermission
            usbManager.requestPermission(device, permissionIntent)
        }
    }
    
    /**
     * 连接到设备（内部方法，已获得权限）
     * 使用当前设置的协议进行连接
     */
    private fun connectToDevice(device: UsbDevice) {
        try {
            _connectionState.value = ConnectionState.Connecting
            
            // 根据当前设置的协议创建对应的策略
            val strategy = when (currentProtocol) {
                UsbProtocol.HID -> HidCommunicationStrategy()
                UsbProtocol.VCP -> VcpCommunicationStrategy()
            }
            
            // 检查设备是否支持该协议
            if (!strategy.isDeviceSupported(device)) {
                dataCallback?.onError("设备不支持${currentProtocol.name}协议")
                _connectionState.value = ConnectionState.Disconnected
                return
            }
            
            // 打开设备连接
            val connection = usbManager.openDevice(device)
            if (connection == null) {
                dataCallback?.onError("无法打开设备连接")
                _connectionState.value = ConnectionState.Disconnected
                return
            }
            
            // 使用指定策略连接
            if (strategy.connect(usbManager, device, connection, dataCallback)) {
                currentStrategy = strategy
                currentDevice = device
                currentConnection = connection
                strategy.startReceiving()
                _connectionState.value = ConnectionState.Connected(currentProtocol.name)
            } else {
                connection.close()
                dataCallback?.onError("${currentProtocol.name}协议连接失败")
                _connectionState.value = ConnectionState.Disconnected
            }
            
        } catch (e: Exception) {
            dataCallback?.onError("连接设备异常: ${e.message}")
            _connectionState.value = ConnectionState.Disconnected
        }
    }
    
    /**
     * 断开连接
     */
    fun disconnect() {
        currentStrategy?.disconnect()
        currentStrategy = null
        currentDevice = null
        currentConnection = null
        _connectionState.value = ConnectionState.Disconnected
    }
    
    /**
     * 发送文本数据
     */
    fun sendText(text: String): Boolean {
        return if (currentStrategy != null && currentStrategy!!.isConnected()) {
            currentStrategy!!.sendText(text)
        } else {
            dataCallback?.onError("设备未连接")
            false
        }
    }
    
    /**
     * 发送二进制数据
     */
    fun sendBinary(data: ByteArray): Boolean {
        return if (currentStrategy != null && currentStrategy!!.isConnected()) {
            currentStrategy!!.sendBinary(data)
        } else {
            dataCallback?.onError("设备未连接")
            false
        }
    }
    
    /**
     * 检查是否已连接
     */
    fun isConnected(): Boolean {
        return currentStrategy?.isConnected() ?: false
    }
    
    /**
     * 获取当前连接的设备
     */
    fun getCurrentDevice(): UsbDevice? {
        return currentDevice
    }
    
    /**
     * 释放资源（在不再使用时调用）
     */
    fun release() {
        disconnect()
        try {
            context.unregisterReceiver(usbPermissionReceiver)
            context.unregisterReceiver(usbDeviceReceiver)
        } catch (e: Exception) {
            // 忽略注销错误
        }
    }
    
    /**
     * 连接状态
     */
    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object RequestingPermission : ConnectionState()
        object Connecting : ConnectionState()
        /**
         * 已连接状态
         * @param protocol 当前使用的协议名称（HID 或 VCP）
         */
        data class Connected(val protocol: String) : ConnectionState()
    }
}
