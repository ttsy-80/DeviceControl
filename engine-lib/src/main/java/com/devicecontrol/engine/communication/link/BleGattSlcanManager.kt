package com.devicecontrol.engine.communication.link

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.devicecontrol.engine.log.EngineLog
import com.devicecontrol.engine.usbserial.UsbSerialVcpCallback
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * BLE 透传 SLCAN 链路（对齐 docs/index.html）：
 * Service 0xABF0、Characteristic 0xABF1，设备名 BLE-AP，命令以 \\r\\n 结尾。
 */
@SuppressLint("MissingPermission")
class BleGattSlcanManager(
    private val context: Context,
) : SlcanDataLink {

    data class BleDeviceInfo(
        val name: String,
        val address: String,
    ) {
        val displayName: String get() = name.ifBlank { address }
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    private var callback: UsbSerialVcpCallback? = null
    private var gatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null

    private val discovered = ConcurrentHashMap<String, BleDeviceInfo>()
    private var scanCallback: ScanCallback? = null
    private var scanListener: ((List<BleDeviceInfo>) -> Unit)? = null

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun isConnected(): Boolean = gatt != null && writeCharacteristic != null

    fun getSavedAddress(): String? = prefs.getString(KEY_LAST_ADDRESS, null)?.takeIf { it.isNotBlank() }

    fun getDiscoveredDevices(): List<BleDeviceInfo> =
        discovered.values.sortedBy { it.displayName.lowercase() }

    fun isBluetoothAvailable(): Boolean {
        val adapter = bluetoothAdapter() ?: return false
        return adapter.isEnabled
    }

    fun startScan(onUpdated: (List<BleDeviceInfo>) -> Unit) {
        val adapter = bluetoothAdapter()
        if (adapter == null || !adapter.isEnabled) {
            onUpdated(emptyList())
            callback?.onError("蓝牙未开启")
            return
        }
        stopScan()
        discovered.clear()
        scanListener = onUpdated
        val cb = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device ?: return
                val name = device.name.orEmpty()
                if (name.isBlank()) {
                    return
                }
                if (name.isNotBlank() && !name.contains(TARGET_NAME, ignoreCase = true)) {
                    return
                }
                EngineLog.i(TAG, "BLE scan success address=${device.address} name:${device.name} alias:${device.alias}")
                val info = BleDeviceInfo(name = name.ifBlank { TARGET_NAME }, address = device.address)
                discovered[device.address] = info
                notifyScanUpdated()
            }

            override fun onScanFailed(errorCode: Int) {
                EngineLog.e(TAG, "BLE scan failed code=$errorCode")
                scanListener?.invoke(getDiscoveredDevices())
            }
        }
        scanCallback = cb
        adapter.bluetoothLeScanner?.startScan(cb)
            ?: run {
                EngineLog.e(TAG, "BluetoothLeScanner unavailable")
                onUpdated(emptyList())
            }
        EngineLog.i(TAG, "BLE scan started")
    }

    fun stopScan() {
        scanCallback?.let { cb ->
            bluetoothAdapter()?.bluetoothLeScanner?.stopScan(cb)
        }
        scanCallback = null
        scanListener = null
    }

    fun connect(address: String, linkCallback: UsbSerialVcpCallback) {
        val adapter = bluetoothAdapter()
        if (adapter == null || !adapter.isEnabled) {
            linkCallback.onError("蓝牙未开启")
            return
        }
        disconnect()
        callback = linkCallback
        val device = adapter.getRemoteDevice(address)
        EngineLog.i(TAG, "BLE connect address=$address name=${device.name}")
        gatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            @Suppress("DEPRECATION")
            device.connectGatt(context, false, gattCallback)
        }
    }

    fun connectSaved(linkCallback: UsbSerialVcpCallback): Boolean {
        val address = getSavedAddress() ?: return false
        connect(address, linkCallback)
        return true
    }

    override fun sendText(text: String): Boolean {
        val char = writeCharacteristic ?: return false
        val g = gatt ?: return false
        return writeBytes(g, char, text.toByteArray(Charsets.UTF_8))
    }

    override fun sendTextLine(text: String): Boolean {
        return sendText(text + LINE_SUFFIX)
    }

    override fun disconnect() {
        EngineLog.i(TAG, "BLE disconnect")
        writeCharacteristic = null
        gatt?.close()
        gatt = null
        callback?.onConnect(false)
        callback = null
    }

    override fun release() {
        stopScan()
        disconnect()
    }

    private fun notifyScanUpdated() {
        val list = getDiscoveredDevices()
        mainHandler.post { scanListener?.invoke(list) }
    }

    private fun bluetoothAdapter(): BluetoothAdapter? {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        return manager?.adapter
    }

    private fun writeBytes(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        payload: ByteArray,
    ): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeCharacteristic(
                characteristic,
                payload,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
            ) == BluetoothGatt.GATT_SUCCESS
        } else {
            @Suppress("DEPRECATION")
            characteristic.value = payload
            @Suppress("DEPRECATION")
            gatt.writeCharacteristic(characteristic)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                EngineLog.i(TAG, "BLE GATT connected, discoverServices")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                EngineLog.i(TAG, "BLE GATT disconnected status=$status")
                writeCharacteristic = null
                callback?.onConnect(false)
                if (status == 133) {
                    callback?.onError("连接超时")
                }
                if (this@BleGattSlcanManager.gatt == gatt) {
                    this@BleGattSlcanManager.gatt = null
                }
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                callback?.onError("BLE 服务发现失败: $status")
                return
            }
            val service = gatt.getService(SERVICE_UUID)
            val characteristic = service?.getCharacteristic(CHAR_UUID)
            if (characteristic == null) {
                callback?.onError("未找到 BLE 透传特征值")
                disconnect()
                return
            }
            writeCharacteristic = characteristic
            gatt.setCharacteristicNotification(characteristic, true)
            val cccd = characteristic.getDescriptor(CCCD_UUID)
            if (cccd != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(cccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    @Suppress("DEPRECATION")
                    cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    gatt.writeDescriptor(cccd)
                }
            }
            prefs.edit().putString(KEY_LAST_ADDRESS, gatt.device.address).apply()
            EngineLog.i(TAG, "BLE 透传就绪 address=${gatt.device.address}")
            callback?.onConnect(true)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
        ) {
            if (value.isNotEmpty()) {
                callback?.onDataReceived(value)
            }
        }

        @Deprecated("Deprecated in API 33")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
        ) {
            @Suppress("DEPRECATION")
            val value = characteristic.value
            if (value?.isEmpty() != true) {
                callback?.onDataReceived(value)
            }
        }
    }

    companion object {
        private const val TAG = "BleGattSlcan"
        private const val PREFS_NAME = "v2_ble_link"
        private const val KEY_LAST_ADDRESS = "last_device_address"
        private const val TARGET_NAME = "BLE-AP"
//        private const val LINE_SUFFIX = "\r\n"
        private const val LINE_SUFFIX = "\r"

        val SERVICE_UUID: UUID = UUID.fromString("0000ABF0-0000-1000-8000-00805F9B34FB")
        val CHAR_UUID: UUID = UUID.fromString("0000ABF1-0000-1000-8000-00805F9B34FB")
        private val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
    }
}
