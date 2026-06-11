package com.devicecontrol.engine.v2.connection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.devicecontrol.engine.v2.log.V2Log

/**
 * 2.0 全局连接态单一数据源（与《技术方案设计-2.0》§3.1 一致）。
 * 各 Activity 通过 [connectionState] 订阅，禁止各页自行维护不一致状态。
 */
object V2ConnectionRepository {

    private const val TAG = "Connection"

    private val _connectionState = MutableLiveData(V2ConnectionState.DISCONNECTED)
    private val _activeTransport = MutableLiveData(V2ConnectionTransport.NONE)

    /** 当前连接态，默认未连接 */
    val connectionState: LiveData<V2ConnectionState> = _connectionState

    /** 当前物理链路：USB / 蓝牙 */
    val activeTransport: LiveData<V2ConnectionTransport> = _activeTransport

    fun update(state: V2ConnectionState) {
        update(state, _activeTransport.value ?: V2ConnectionTransport.NONE)
    }

    /**
     * 更新连接态与链路类型（联调 USB/BLE 时由通讯层调用）。
     */
    fun update(state: V2ConnectionState, transport: V2ConnectionTransport) {
        if (_connectionState.value != state) {
            V2Log.i(TAG, "connectionState -> $state transport=$transport")
            _connectionState.postValue(state)
        }
        if (_activeTransport.value != transport) {
            _activeTransport.postValue(transport)
        }
    }

    fun isConnected(): Boolean = _connectionState.value == V2ConnectionState.CONNECTED

    fun currentTransport(): V2ConnectionTransport =
        _activeTransport.value ?: V2ConnectionTransport.NONE
}
