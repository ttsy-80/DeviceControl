package com.devicecontrol.engine.log

/**
 * 引擎库日志接口，可外接自定义实现（如写文件、上报等）
 * 未设置时使用 [DefaultEngineLogger]（系统 android.util.Log）
 */
interface EngineLogger {
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String)
    fun e(tag: String, message: String)
    fun e(tag: String, message: String, throwable: Throwable?)
}
