package com.devicecontrol.engine.v2.inspection

import com.devicecontrol.engine.communication.protocol.CANOpenHelper
import com.devicecontrol.engine.communication.protocol.SlcanRequest
import com.devicecontrol.engine.data.model.RotationDirection
import com.devicecontrol.engine.log.EngineLog
import com.devicecontrol.engine.data.model.V2AutoInspectionConfig
import com.devicecontrol.engine.v2.viewmodel.V2UiOperationMode

/**
 * V2 检测 CAN 指令构建入口：入参为 [V2InspectionCommandContext]（含自动/手动配置实体）。
 *
 * 嵌入式同事可在此根据 `ctx.autoConfig` / `ctx.manualConfig` 字段实现协议，无需再经 TaskExecution 映射。
 */
object V2InspectionCommandDispatcher {

    private const val TAG = "V2InspectionCmd"
    private const val MIN_SPEED_SEC_PER_REV = 1.0
    private const val CAN_VELOCITY_SCALE_DIVISOR = 1857.0

    fun buildRequests(
        cmd: String,
        ctx: V2InspectionCommandContext,
        realGearRatio: Double,
    ): List<SlcanRequest> {
        val speedSec = resolveSpeedSecPerRev(ctx).coerceAtLeast(MIN_SPEED_SEC_PER_REV)
        val canVelocity = canVelocityFromSecPerRev(speedSec, realGearRatio)
        val isForward = ctx.execution.rotationDirection == RotationDirection.FORWARD
        val signedVelocity = if (isForward) canVelocity else -canVelocity

        EngineLog.d(
            TAG,
            "buildRequests cmd=$cmd uiMode=${ctx.uiMode} speedSec=$speedSec model=${ctx.modelName} pos=${ctx.position}",
        )

        return when {
            cmd.startsWith("START") -> CANOpenHelper.startSpeedMode(signedVelocity)
            cmd.startsWith("PAUSE") -> CANOpenHelper.stop()
            cmd.startsWith("CONTINUOUS") -> CANOpenHelper.startSpeedMode(signedVelocity)
            cmd.startsWith("SPEED_") -> CANOpenHelper.changeVelocity(signedVelocity)
            cmd.startsWith("FORWARD") -> CANOpenHelper.reverseDirection(signedVelocity)
            cmd.startsWith("REVERSE") -> CANOpenHelper.reverseDirection(signedVelocity)
            else -> emptyList()
        }
    }

    fun resolveSpeedSecPerRev(ctx: V2InspectionCommandContext): Double {
        ctx.speedSecOverride?.let { return it.coerceAtLeast(MIN_SPEED_SEC_PER_REV) }
        // 运行速度以 TaskExecution.speed（秒/圈）为准，加速/减速与状态栏均读此字段
        if (ctx.execution.speed >= MIN_SPEED_SEC_PER_REV) {
            return ctx.execution.speed
        }
        return when (ctx.uiMode) {
            V2UiOperationMode.AUTO -> {
                if (ctx.autoConfig.autoContinuous > 0.01) {
                    degPerSecToSecPerRev(ctx.autoConfig.autoContinuous)
                } else {
                    degPerMinToSecPerRev(resolveBaseJogSpeedDegPerMin(ctx))
                }
            }
            V2UiOperationMode.MANUAL -> {
                val degPerSec = ctx.manualConfig.manualContinuous.takeIf { it > 0.01 }
                    ?: ctx.manualConfig.manualJogAngle
                degPerSecToSecPerRev(degPerSec)
            }
        }
    }

    /** 基础点动速度（度/分钟），[V2AutoInspectionConfig.baseJogSpeed]。 */
    fun resolveBaseJogSpeedDegPerMin(ctx: V2InspectionCommandContext): Double =
        ctx.autoConfig.baseJogSpeed.coerceIn(
            V2AutoInspectionConfig.MIN_BASE_JOG_SPEED,
            V2AutoInspectionConfig.MAX_BASE_JOG_SPEED,
        )

    /** 自动模式点动步进角度（度），[V2AutoInspectionConfig.autoJogAngle]。 */
    fun resolveJogStepDegrees(ctx: V2InspectionCommandContext): Double =
        ctx.autoConfig.autoJogAngle.coerceIn(
            V2AutoInspectionConfig.MIN_AUTO_JOG_ANGLE,
            V2AutoInspectionConfig.MAX_AUTO_JOG_ANGLE,
        )

    fun resolveSpeedStepSecPerRev(ctx: V2InspectionCommandContext): Double {
        val degPerSec = when (ctx.uiMode) {
            V2UiOperationMode.AUTO -> ctx.autoConfig.autoSpeedStep
            V2UiOperationMode.MANUAL -> ctx.manualConfig.manualSpeedStep
        }
        return degPerSecToSecPerRev(degPerSec).coerceAtLeast(1.0)
    }

    fun resolveJogHoldSec(ctx: V2InspectionCommandContext): Int =
        when (ctx.uiMode) {
            V2UiOperationMode.AUTO -> ctx.autoConfig.jogHoldSec.toInt().coerceAtLeast(0)
            V2UiOperationMode.MANUAL -> ctx.execution.jogInterval.coerceAtLeast(0)
        }

    fun resolveAutoTurns(ctx: V2InspectionCommandContext): Int =
        ctx.autoConfig.autoTurns.toInt().coerceAtLeast(1)

    fun resolveJogCycles(ctx: V2InspectionCommandContext): Int =
        when (ctx.uiMode) {
            V2UiOperationMode.AUTO -> resolveAutoTurns(ctx)
            V2UiOperationMode.MANUAL -> ctx.execution.continuousCycles.coerceAtLeast(1)
        }

    fun resolvePlaybackSpeedSecPerRev(ctx: V2InspectionCommandContext): Double =
        degPerSecToSecPerRev(ctx.autoConfig.reverseSpeed)

    private fun degPerSecToSecPerRev(degPerSec: Double): Double {
        if (degPerSec <= 0.01) return MIN_SPEED_SEC_PER_REV
        return (360.0 / degPerSec).coerceAtLeast(MIN_SPEED_SEC_PER_REV)
    }

    /** 度/分钟 → 秒/圈（一圈 360°）。 */
    private fun degPerMinToSecPerRev(degPerMin: Double): Double {
        val d = degPerMin.coerceAtLeast(1.0)
        return (21_600.0 / d).coerceAtLeast(MIN_SPEED_SEC_PER_REV)
    }

    private fun canVelocityFromSecPerRev(secPerRev: Double, realGearRatio: Double): Int {
        val s = secPerRev.coerceAtLeast(MIN_SPEED_SEC_PER_REV)
        val factor = 60.0 / s
        return (factor * realGearRatio * 512.0 * 65536.0 / CAN_VELOCITY_SCALE_DIVISOR).toInt()
    }
}
