package com.devicecontrol.engine.v2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devicecontrol.engine.v2.connection.V2ConnectionRepository
import com.devicecontrol.engine.v2.connection.V2ConnectionState
import com.devicecontrol.engine.v2.log.V2Log

enum class V2SettingsPage {
    BLUETOOTH,
    LANGUAGE,
    UPDATE,
    ABOUT,
    MANUAL,
}

/**
 * 2.0 设置域 UI 状态（P2～P5）。蓝牙确认连接仅更新展示态，后续对接真实 BLE。
 */
class V2SettingsViewModel : ViewModel() {

    private val _currentPage = MutableLiveData(V2SettingsPage.BLUETOOTH)
    val currentPage: LiveData<V2SettingsPage> = _currentPage

    private val _bluetoothDevices = MutableLiveData(
        listOf("设备001", "设备002", "设备003", "设备004", "设备005"),
    )
    val bluetoothDevices: LiveData<List<String>> = _bluetoothDevices

    private val _selectedDeviceIndex = MutableLiveData(0)
    val selectedDeviceIndex: LiveData<Int> = _selectedDeviceIndex

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

    /** 确认连接：UI 阶段将全局连接态置为已连接 */
    fun confirmBluetoothConnect() {
        val idx = _selectedDeviceIndex.value ?: 0
        val name = _bluetoothDevices.value?.getOrNull(idx) ?: "unknown"
        V2Log.i(TAG, "confirmBluetoothConnect device=$name")
        V2ConnectionRepository.update(V2ConnectionState.CONNECTED)
    }

    fun applyLanguage(languageCode: String) {
        V2Log.i(TAG, "applyLanguage code=$languageCode (UI placeholder)")
    }

    fun checkUpdate() {
        V2Log.i(TAG, "checkUpdate clicked (placeholder)")
    }

    companion object {
        private const val TAG = "SettingsVM"
    }
}
