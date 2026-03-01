package com.devicecontrol.engine.communication

/**
 * 连接状态：与具体传输方式无关，业务层据此更新 UI
 */
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object RequestingPermission : ConnectionState()
    object Connecting : ConnectionState()
    data class Connected(val transportName: String) : ConnectionState()
}
