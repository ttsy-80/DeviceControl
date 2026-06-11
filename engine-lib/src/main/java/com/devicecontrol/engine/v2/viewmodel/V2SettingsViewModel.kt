package com.devicecontrol.engine.v2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicecontrol.engine.communication.link.BleGattSlcanManager
import com.devicecontrol.engine.usbserial.UsbSerialVcpCallback
import com.devicecontrol.engine.v2.connection.V2ConnectionRepository
import com.devicecontrol.engine.v2.connection.V2ConnectionState
import com.devicecontrol.engine.v2.connection.V2SlcanConnectionManager
import com.devicecontrol.engine.v2.log.V2Log
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class V2SettingsPage {
    BLUETOOTH,
    LANGUAGE,
    UPDATE,
    ABOUT,
    MANUAL,
}

/**
 * 2.0 设置域：蓝牙扫描/连接对接 [BleGattSlcanManager]。
 */
class V2SettingsViewModel : ViewModel() {

    private val _currentPage = MutableLiveData(V2SettingsPage.BLUETOOTH)
    val currentPage: LiveData<V2SettingsPage> = _currentPage

    private val _bluetoothDevices = MutableLiveData<List<BleGattSlcanManager.BleDeviceInfo>>(emptyList())
    val bluetoothDevices: LiveData<List<BleGattSlcanManager.BleDeviceInfo>> = _bluetoothDevices

    private val _bluetoothScanning = MutableLiveData(false)
    val bluetoothScanning: LiveData<Boolean> = _bluetoothScanning

    private val _bluetoothScanEmpty = MutableLiveData(false)
    val bluetoothScanEmpty: LiveData<Boolean> = _bluetoothScanEmpty

    private val _selectedDeviceIndex = MutableLiveData(0)
    val selectedDeviceIndex: LiveData<Int> = _selectedDeviceIndex

    private val _connecting = MutableLiveData(false)
    val connecting: LiveData<Boolean> = _connecting

    private val _connectSuccess = MutableLiveData(false)
    val connectSuccess: LiveData<Boolean> = _connectSuccess

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private var scanTimeoutJob: Job? = null

    val appVersion: String = "2.0.0-ui"
    val lastInspectionLabel: String = "上次检测时间：—"

    fun selectPage(page: V2SettingsPage) {
        V2Log.i(TAG, "selectPage=$page")
        _currentPage.value = page
    }

    fun selectDevice(index: Int) {
        V2Log.i(TAG, "selectDevice index=$index")
        _selectedDeviceIndex.value = index
    }

    fun refreshBluetoothScan() {
        scanTimeoutJob?.cancel()
        _errorMessage.value = null
        _bluetoothScanEmpty.value = false
        _bluetoothDevices.value = emptyList()
        if (!V2SlcanConnectionManager.bleManager().isBluetoothAvailable()) {
            _bluetoothScanning.value = false
            _errorMessage.value = "请先开启系统蓝牙"
            return
        }
        _bluetoothScanning.value = true
        V2SlcanConnectionManager.startBleScan { devices ->
            _bluetoothDevices.postValue(devices)
            if (devices.isNotEmpty()) {
                _bluetoothScanEmpty.postValue(false)
                onBluetoothDevicesFound()
            }
        }
        scanTimeoutJob = viewModelScope.launch {
            delay(SCAN_TIMEOUT_MS)
            finishBluetoothScanTimedOut()
        }
    }

    fun stopBluetoothScan() {
        scanTimeoutJob?.cancel()
        scanTimeoutJob = null
        V2SlcanConnectionManager.stopBleScan()
        _bluetoothScanning.value = false
    }

    /** 确认连接：建立 BLE GATT 并完成 SLCAN 握手。 */
    fun confirmBluetoothConnect() {
        val idx = _selectedDeviceIndex.value ?: 0
        val device = _bluetoothDevices.value?.getOrNull(idx)
        val address = device?.address
        if (address.isNullOrBlank()) {
            _errorMessage.value = "请先选择蓝牙设备"
            return
        }
        stopBluetoothScan()
        _connecting.value = true
        _errorMessage.value = null
        V2SlcanConnectionManager.connectBleDevice(address, object : UsbSerialVcpCallback {
            override fun onConnect(isConnect: Boolean) {
                _connecting.postValue(false)
                _connectSuccess.postValue(isConnect)
                V2ConnectionRepository.update(if (isConnect)V2ConnectionState.CONNECTED else V2ConnectionState.DISCONNECTED)
                V2Log.i(TAG, "confirmBluetoothConnect result:$isConnect")
            }

            override fun onDataReceived(data: ByteArray) {

            }

            override fun onError(error: String) {
                _connecting.postValue(false)
                _errorMessage.postValue(error)
            }
        })
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun consumeConnectSuccess() {
        _connectSuccess.value = false
    }

    fun applyLanguage(languageCode: String) {
        V2Log.i(TAG, "applyLanguage code=$languageCode (UI placeholder)")
    }

    fun checkUpdate() {
        V2Log.i(TAG, "checkUpdate clicked (placeholder)")
    }

    override fun onCleared() {
        stopBluetoothScan()
        super.onCleared()
    }

    private fun onBluetoothDevicesFound() {
        if (scanTimeoutJob == null) return
        scanTimeoutJob?.cancel()
        scanTimeoutJob = null
//        V2SlcanConnectionManager.stopBleScan()
        _bluetoothScanning.postValue(false)
        V2Log.i(TAG, "BLE scan found device(s), timeout cancelled")
    }

    private fun finishBluetoothScanTimedOut() {
        scanTimeoutJob = null
        V2SlcanConnectionManager.stopBleScan()
        _bluetoothScanning.value = false
        if (_bluetoothDevices.value.isNullOrEmpty()) {
            _bluetoothScanEmpty.value = true
            V2Log.i(TAG, "BLE scan timeout: no devices found")
        } else {
            V2Log.i(TAG, "BLE scan timeout: ${_bluetoothDevices.value?.size} device(s)")
        }
    }

    companion object {
        private const val TAG = "SettingsVM"
        private const val SCAN_TIMEOUT_MS = 30_000L
    }
}
