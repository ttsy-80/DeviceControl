package com.devicecontrol.engine.v2.inspection.strategy

/**
 * 检测页操作策略入口。默认 [V2InspectionDeployMode.DEVELOPMENT] 便于无设备联调；
 * 上架前在 Application 或构建脚本中设为 [V2InspectionDeployMode.RELEASE]。
 */
object V2InspectionOperationStrategyProvider {

    @Volatile
    var deployMode: V2InspectionDeployMode = V2InspectionDeployMode.RELEASE

    val strategy: V2InspectionOperationStrategy
        get() = when (deployMode) {
            V2InspectionDeployMode.DEVELOPMENT -> V2DevelopmentInspectionOperationStrategy
            V2InspectionDeployMode.RELEASE -> V2ReleaseInspectionOperationStrategy
        }

    fun requiresDeviceConnection(): Boolean = strategy.requiresDeviceConnection
}
