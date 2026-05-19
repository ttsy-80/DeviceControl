package com.devicecontrol.engine.v2.inspection

import com.devicecontrol.engine.data.model.TaskExecution

/**
 * V2 步进器 UI 参数 → v1 [TaskExecution] 字段。
 *
 * 速度类 UI 单位为 °/秒（角速度），换算为 v1 使用的秒/圈：secPerRev = 360 / degPerSec。
 */
object V2ModeSettingsMapper {

    fun applyAutoToExecution(
        snapshot: V2ModeSettingsSnapshot,
        current: TaskExecution,
    ): TaskExecution {
        val speedDeg = snapshot.get("auto_continuous")
            .takeIf { it > 0 }
            ?: snapshot.get("base_jog_speed")
        val speedSec = degPerSecToSecPerRev(speedDeg, current.speed)
        return current.copy(
            operationMode = com.devicecontrol.engine.data.model.OperationMode.JOG,
            speed = speedSec,
            jogInterval = snapshot.get("jog_hold", current.jogInterval.toDouble()).toInt().coerceAtLeast(0),
            continuousCycles = snapshot.get("auto_turns", current.continuousCycles.toDouble())
                .toInt()
                .coerceAtLeast(1),
            playbackSpeed = degPerSecToSecPerRev(
                snapshot.get("reverse_speed", current.playbackSpeed),
                current.playbackSpeed,
            ),
        )
    }

    fun applyManualToExecution(
        snapshot: V2ModeSettingsSnapshot,
        current: TaskExecution,
    ): TaskExecution {
        val speedDeg = snapshot.get("manual_continuous")
            .takeIf { it > 0 }
            ?: snapshot.get("manual_jog_angle")
        val speedSec = degPerSecToSecPerRev(speedDeg, current.speed)
        return current.copy(
            speed = speedSec,
            speedStep = degPerSecToSecPerRev(
                snapshot.get("manual_speed_step", current.speedStep),
                current.speedStep,
            ).coerceAtLeast(1.0),
            playbackSpeed = current.playbackSpeed,
        )
    }

    /** °/秒 → 秒/圈；非法或过小则回退 [fallbackSecPerRev] */
    fun degPerSecToSecPerRev(degPerSec: Double, fallbackSecPerRev: Double): Double {
        if (degPerSec <= 0.01) return fallbackSecPerRev.coerceAtLeast(1.0)
        return (360.0 / degPerSec).coerceAtLeast(1.0)
    }
}
