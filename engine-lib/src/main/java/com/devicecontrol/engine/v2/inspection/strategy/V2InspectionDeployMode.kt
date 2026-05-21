package com.devicecontrol.engine.v2.inspection.strategy

/** 应用发布阶段：开发中可离线调试 UI/状态；发布中需连接设备且指令成功后才落库。 */
enum class V2InspectionDeployMode {
    /** 开发中：不下发真实 CAN，不校验连接，状态变更直接同步数据库。 */
    DEVELOPMENT,
    /** 发布中：校验连接，仅指令执行成功后更新状态并落库。 */
    RELEASE,
}
