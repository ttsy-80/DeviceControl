package com.devicecontrol.engine.v2.connection

/**
 * 2.0 顶栏「已连接 / 未连接」展示态。
 * 后续可对接 USB/BLE 真实握手结果，当前 UI 阶段可与 1.0 通讯层解耦。
 */
enum class V2ConnectionState {
    CONNECTED,
    DISCONNECTED,
}
