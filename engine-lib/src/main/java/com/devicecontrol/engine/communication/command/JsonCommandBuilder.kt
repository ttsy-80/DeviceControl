package com.devicecontrol.engine.communication.command

import com.devicecontrol.engine.data.model.OperationMode
import com.devicecontrol.engine.data.model.RotationDirection
import com.devicecontrol.engine.data.model.TaskExecution
import com.google.gson.Gson

/**
 * 按业务约定的 JSON 结构组帧，供 [CommandSendMode.TEXT_JSON] 时 sendText 下发
 */
object JsonCommandBuilder {
    private val gson = Gson()

    private fun dir(d: RotationDirection) = if (d == RotationDirection.FORWARD) "forward" else "reverse"
    private fun mode(m: OperationMode) = if (m == OperationMode.JOG) "jog" else "continuous"

    /** 1. 启动 */
    fun start(exec: TaskExecution): String = gson.toJson(mapOf(
        "command" to "start",
        "operationMode" to mode(exec.operationMode),
        "rotationDirection" to dir(exec.rotationDirection),
        "speed" to exec.speed,
        "jogInterval" to exec.jogInterval,
        "continuousCycles" to exec.continuousCycles
    ))

    /** 2. 暂停 */
    fun pause(): String = gson.toJson(mapOf("command" to "pause"))

    /** 3. 点动 */
    fun jog(exec: TaskExecution): String = gson.toJson(mapOf(
        "command" to "jog",
        "rotationDirection" to dir(exec.rotationDirection),
        "speed" to exec.speed,
        "jogInterval" to exec.jogInterval
    ))

    /** 4. 连续 */
    fun continuous(exec: TaskExecution): String = gson.toJson(mapOf(
        "command" to "continuous",
        "rotationDirection" to dir(exec.rotationDirection),
        "speed" to exec.speed,
        "continuousCycles" to exec.continuousCycles
    ))

    /** 5. 速度 */
    fun speed(exec: TaskExecution): String = gson.toJson(mapOf(
        "command" to "speed",
        "operationMode" to mode(exec.operationMode),
        "rotationDirection" to dir(exec.rotationDirection),
        "speed" to exec.speed,
        "jogInterval" to exec.jogInterval,
        "continuousCycles" to exec.continuousCycles
    ))

    /** 6. 正转 */
    fun forward(exec: TaskExecution): String = gson.toJson(mapOf(
        "command" to "forward",
        "operationMode" to mode(exec.operationMode),
        "speed" to exec.speed,
        "jogInterval" to exec.jogInterval,
        "continuousCycles" to exec.continuousCycles
    ))

    /** 7. 反转 */
    fun reverse(exec: TaskExecution): String = gson.toJson(mapOf(
        "command" to "reverse",
        "operationMode" to mode(exec.operationMode),
        "speed" to exec.speed,
        "jogInterval" to exec.jogInterval,
        "continuousCycles" to exec.continuousCycles,
    ))

    /** 8. 记录（需返回当前位置给业务层） */
    fun record(): String = gson.toJson(mapOf("command" to "record"))

    /** 9. 回查 */
    fun playback(exec: TaskExecution, position: Int, playbackSpeed: Double): String = gson.toJson(mapOf(
        "command" to "playback",
        "position" to position,
        "playbackSpeed" to playbackSpeed,
        "operationMode" to mode(exec.operationMode),
        "rotationDirection" to dir(exec.rotationDirection),
        "jogInterval" to exec.jogInterval,
        "continuousCycles" to exec.continuousCycles
    ))
}
