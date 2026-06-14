package com.devicecontrol.engine.v2.connection

import android.content.Context
import com.devicecontrol.engine.communication.link.BleGattSlcanManager
import com.devicecontrol.engine.communication.link.SlcanDataLink
import com.devicecontrol.engine.communication.link.UsbSlcanDataLink
import com.devicecontrol.engine.communication.protocol.SlcanTransport
import com.devicecontrol.engine.lifecycle.SlcanEmergencyClose
import com.devicecontrol.engine.log.EngineLog
import com.devicecontrol.engine.usbserial.UsbSerialVcpCallback
import com.devicecontrol.engine.usbserial.UsbSerialVcpManager

/**
 * V2 SLCAN 连接策略：检测/指令下发优先 USB，无 USB 时使用蓝牙。
 * 设置页可单独建立 BLE 连接；检测页进入时自动尝试 USB → BLE。
 */
object V2SlcanConnectionManager {

    private const val TAG = "V2SlcanConn"

    private lateinit var appContext: Context
    private var usbManager: UsbSerialVcpManager? = null
    private var bleManager: BleGattSlcanManager? = null

    @Volatile
    private var activeTransport: V2ConnectionTransport = V2ConnectionTransport.NONE

    fun init(context: Context) {
        appContext = context.applicationContext
        if (bleManager == null) {
            bleManager = BleGattSlcanManager(appContext)
        }
    }

    fun requireInit() {
        check(::appContext.isInitialized) { "V2SlcanConnectionManager.init(context) 未调用" }
    }

    fun bleManager(): BleGattSlcanManager {
        requireInit()
        return bleManager!!
    }

    fun activeTransport(): V2ConnectionTransport = activeTransport

    fun activeLink(): SlcanDataLink? = when (activeTransport) {
        V2ConnectionTransport.USB -> usbManager?.let { UsbSlcanDataLink(it) }
        V2ConnectionTransport.BLUETOOTH -> bleManager
        V2ConnectionTransport.NONE -> null
    }

    fun isLinkConnected(): Boolean = activeLink()?.isConnected() == true

    fun createTransport(): SlcanTransport = object : SlcanTransport {
        override fun send(data: String): Boolean = when (activeTransport) {
            V2ConnectionTransport.USB -> usbManager?.sendTextLine(data) ?: false
            V2ConnectionTransport.BLUETOOTH -> bleManager?.sendTextLine(data) ?: false
            V2ConnectionTransport.NONE -> false
        }
    }

    /**
     * 检测页：USB 优先，否则使用已连接/已记忆的 BLE。
     */
    fun connectPreferred(linkCallback: UsbSerialVcpCallback) {
        requireInit()
        val usb = ensureUsbManager()
        val usbDevices = usb.getAvailableDevices()
        if (usbDevices.isNotEmpty()) {
            EngineLog.i(TAG, "connectPreferred: 使用 USB (${usbDevices.first().deviceName})")
//            disconnectBleTransportOnly()
            activeTransport = V2ConnectionTransport.USB
            usb.connect(usbDevices.first(), wrapCallback(V2ConnectionTransport.USB, linkCallback))
            return
        }

        EngineLog.i(TAG, "connectPreferred: 无 USB，尝试蓝牙")
        val ble = ensureBleManager()
        if (ble.isConnected()) {
            EngineLog.i(TAG, "connectPreferred: 蓝牙已连接")
            activeTransport = V2ConnectionTransport.BLUETOOTH
            bindEmergencyClose()
            V2ConnectionRepository.update(V2ConnectionState.CONNECTED, V2ConnectionTransport.BLUETOOTH)
            linkCallback.onConnect(true)
            return
        }
        if (ble.connectSaved(wrapCallback(V2ConnectionTransport.BLUETOOTH, linkCallback))) {
            EngineLog.i(TAG, "connectPreferred: 蓝牙已连接，保存的蓝牙")
            activeTransport = V2ConnectionTransport.BLUETOOTH
            return
        }
        EngineLog.w(TAG, "connectPreferred: USB/BLE 均不可用")
        activeTransport = V2ConnectionTransport.NONE
        V2ConnectionRepository.update(V2ConnectionState.DISCONNECTED, V2ConnectionTransport.NONE)
        linkCallback.onError("未找到 USB 设备，请先在设置中连接蓝牙")
    }

    /** 设置页：连接指定 BLE 设备（会断开 USB）。 */
    fun connectBleDevice(address: String, linkCallback: UsbSerialVcpCallback) {
        requireInit()
        releaseUsb()
        activeTransport = V2ConnectionTransport.BLUETOOTH
        ensureBleManager().connect(address, wrapCallback(V2ConnectionTransport.BLUETOOTH, linkCallback))
    }

    fun startBleScan(onUpdated: (List<BleGattSlcanManager.BleDeviceInfo>) -> Unit) {
        requireInit()
        ensureBleManager().startScan(onUpdated)
    }

    fun stopBleScan() {
        bleManager?.stopScan()
    }

    /** 检测页退出：关闭 SLCAN 后释放 USB；保留设置页建立的 BLE。 */
    fun releaseInspectionSession() {
        if (activeTransport == V2ConnectionTransport.USB) {
            usbManager?.disconnect()
            usbManager?.release()
            usbManager = null
            activeTransport = V2ConnectionTransport.NONE
            SlcanEmergencyClose.unbind()
            if (bleManager?.isConnected() == true) {
                activeTransport = V2ConnectionTransport.BLUETOOTH
                bindEmergencyClose()
                V2ConnectionRepository.update(V2ConnectionState.CONNECTED, V2ConnectionTransport.BLUETOOTH)
            } else {
                V2ConnectionRepository.update(V2ConnectionState.DISCONNECTED, V2ConnectionTransport.NONE)
            }
        }
    }

    fun releaseAll() {
        stopBleScan()
        releaseUsb()
        bleManager?.release()
        activeTransport = V2ConnectionTransport.NONE
        SlcanEmergencyClose.unbind()
        V2ConnectionRepository.update(V2ConnectionState.DISCONNECTED, V2ConnectionTransport.NONE)
    }

    private fun ensureUsbManager(): UsbSerialVcpManager {
        if (usbManager == null) {
            usbManager = UsbSerialVcpManager(appContext)
        }
        return usbManager!!
    }

    private fun ensureBleManager(): BleGattSlcanManager {
        if (bleManager == null) {
            bleManager = BleGattSlcanManager(appContext)
        }
        return bleManager!!
    }

    private fun releaseUsb() {
        usbManager?.release()
        usbManager = null
        if (activeTransport == V2ConnectionTransport.USB) {
            activeTransport = V2ConnectionTransport.NONE
        }
    }

    private fun disconnectBleTransportOnly() {
        if (activeTransport == V2ConnectionTransport.BLUETOOTH) {
            bleManager?.disconnect()
        }
    }

    private fun bindEmergencyClose() {
        activeLink()?.let { SlcanEmergencyClose.bind(it) }
    }

    private fun wrapCallback(
        transport: V2ConnectionTransport,
        delegate: UsbSerialVcpCallback,
    ): UsbSerialVcpCallback = object : UsbSerialVcpCallback {
        override fun onConnect(isConnect: Boolean) {
            if (isConnect) {
                activeTransport = transport
                bindEmergencyClose()
                V2ConnectionRepository.update(
                    V2ConnectionState.CONNECTED,
                    transport,
                )
            } else {
                if (activeTransport == transport) {
                    activeTransport = V2ConnectionTransport.NONE
                    SlcanEmergencyClose.unbind()
                    V2ConnectionRepository.update(
                        V2ConnectionState.DISCONNECTED,
                        V2ConnectionTransport.NONE,
                    )
                }
            }
            delegate.onConnect(isConnect)
        }

        override fun onDataReceived(data: ByteArray) {
            delegate.onDataReceived(data)
        }

        override fun onError(error: String) {
            EngineLog.e(TAG, "link error ($transport): $error")
            if (activeTransport == transport) {
                activeTransport = V2ConnectionTransport.NONE
                SlcanEmergencyClose.unbind()
                V2ConnectionRepository.update(
                    V2ConnectionState.DISCONNECTED,
                    V2ConnectionTransport.NONE,
                )
            }
            delegate.onError(error)
        }
    }
}
