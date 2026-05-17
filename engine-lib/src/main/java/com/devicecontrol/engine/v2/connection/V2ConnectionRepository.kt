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

    /** 当前连接态，默认未连接 */
    val connectionState: LiveData<V2ConnectionState> = _connectionState

    /**
     * 更新连接态（联调 USB/BLE 时由通讯层调用）。
     */
    fun update(state: V2ConnectionState) {
        if (_connectionState.value == state) return
        V2Log.i(TAG, "connectionState -> $state")
        _connectionState.value = state
    }

    fun isConnected(): Boolean = _connectionState.value == V2ConnectionState.CONNECTED
}
