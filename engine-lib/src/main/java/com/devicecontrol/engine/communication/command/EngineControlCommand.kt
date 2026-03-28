package com.devicecontrol.engine.communication.command

/**
 * 任务控制指令构建：供 TaskControl 下发至通讯层
 * 格式为简单键值对，便于设备/网关解析或后续替换为 CAN 等协议
 */
object EngineControlCommand {

    private const val SEP = ","
    private const val KV = "="

    /** 发动机型号 */
    private fun model(v: String) = "MODEL${KV}$v"
    /** 位置 */
    private fun pos(v: String) = "POS${KV}$v"
    /** 方向：FORWARD / REVERSE */
    private fun dir(v: String) = "DIR${KV}$v"
    /** 模式：JOG / CONTINUOUS */
    private fun mode(v: String) = "MODE${KV}$v"
    /** 速度（分钟/圈，由上层将「秒/圈」换算后传入） */
    private fun speed(v: Double) = "SPEED${KV}$v"
    /** 点动间隔（秒） */
    private fun jogInt(v: Int) = "JOG_INT${KV}$v"
    /** 连续圈数 */
    private fun cycles(v: Int) = "CYCLES${KV}$v"
    /** 叶片序号 */
    private fun blade(v: Int) = "BLADE${KV}$v"
    /** 回溯速度（秒/圈） */
    private fun playbackSpeed(v: Double) = "PLAYBACK_SPEED${KV}$v"
    /** 记录ID */
    private fun recordId(v: Long) = "RECORD_ID${KV}$v"

    /**
     * 启动指令
     * @param modelName 发动机型号
     * @param position 位置（配置项）
     * @param forward 是否正转，false 为反转
     * @param jog 是否点动，false 为连续
     * @param speedConfig 当前速度（分钟/圈）
     * @param jogInterval 点动间隔（秒）
     * @param continuousCycles 连续圈数
     */
    fun start(
        modelName: String,
        position: String,
        forward: Boolean,
        jog: Boolean,
        speedConfig: Double,
        jogInterval: Int,
        continuousCycles: Int
    ): String {
        return "START" + SEP +
            model(modelName) + SEP +
            pos(position) + SEP +
            dir(if (forward) "FORWARD" else "REVERSE") + SEP +
            mode(if (jog) "JOG" else "CONTINUOUS") + SEP +
            speed(speedConfig) + SEP +
            jogInt(jogInterval) + SEP +
            cycles(continuousCycles) + "\n"
    }

    /**
     * 暂停指令（携带发动机信息）
     */
    fun pause(modelName: String, position: String): String {
        return "PAUSE" + SEP + model(modelName) + SEP + pos(position) + "\n"
    }

    /** 正转指令 */
    fun forward(modelName: String, position: String): String {
        return "FORWARD" + SEP + model(modelName) + SEP + pos(position) + "\n"
    }

    /** 反转指令 */
    fun reverse(modelName: String, position: String): String {
        return "REVERSE" + SEP + model(modelName) + SEP + pos(position) + "\n"
    }

    /** 点动指令 */
    fun jog(modelName: String, position: String): String {
        return "JOG" + SEP + model(modelName) + SEP + pos(position) + "\n"
    }

    /** 连续指令 */
    fun continuous(modelName: String, position: String): String {
        return "CONTINUOUS" + SEP + model(modelName) + SEP + pos(position) + "\n"
    }

    /** 速度+ 指令 */
    fun speedPlus(modelName: String, position: String): String {
        return "SPEED_PLUS" + SEP + model(modelName) + SEP + pos(position) + "\n"
    }

    /** 速度- 指令 */
    fun speedMinus(modelName: String, position: String): String {
        return "SPEED_MINUS" + SEP + model(modelName) + SEP + pos(position) + "\n"
    }

    /**
     * 记录指令
     * @param bladeNumber 当前记录叶片序号
     */
    fun record(modelName: String, position: String, bladeNumber: Int): String {
        return "RECORD" + SEP + model(modelName) + SEP + pos(position) + SEP + blade(bladeNumber) + "\n"
    }

    /**
     * 回放指令
     * @param playbackSpeedSecPerCircle 回溯速度（秒/圈）
     * @param recordId 记录ID
     */
    fun playback(
        modelName: String,
        position: String,
        playbackSpeedSecPerCircle: Double,
        recordId: Long
    ): String {
        return "PLAYBACK" + SEP +
            model(modelName) + SEP +
            pos(position) + SEP +
            playbackSpeed(playbackSpeedSecPerCircle) + SEP +
            recordId(recordId) + "\n"
    }
}
