package com.devicecontrol.engine.v2.log

import com.devicecontrol.engine.log.EngineLog

/**
 * 2.0 模块统一日志：前缀 `V2/`，写入 [EngineLog]（含可选文件日志）。
 * 关键导航、连接态、控制占位操作均应调用本类，便于与 1.0 日志区分检索。
 */
object V2Log {
    private const val PREFIX = "V2"

    private fun fullTag(tag: String) = "$PREFIX/$tag"

    /** 调试：布局绑定、列表刷新等 */
    fun d(tag: String, message: String) {
        EngineLog.d(fullTag(tag), message)
    }

    /** 关键业务路径：页面进入、用户确认操作 */
    fun i(tag: String, message: String) {
        EngineLog.i(fullTag(tag), message)
    }

    fun w(tag: String, message: String) {
        EngineLog.w(fullTag(tag), message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            EngineLog.e(fullTag(tag), message, throwable)
        } else {
            EngineLog.e(fullTag(tag), message)
        }
    }
}
