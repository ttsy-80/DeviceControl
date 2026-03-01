package com.devicecontrol.engine.log

/**
 * 引擎库统一日志入口
 * - 默认使用系统 Log；可通过 [setLogger] 外接自定义实现
 */
object EngineLog {
    private const val DEFAULT_TAG = "Engine"

    @Volatile
    private var logger: EngineLogger = DefaultEngineLogger(DEFAULT_TAG)

    /** 设置自定义日志实现；传 null 恢复为系统 Log */
    @JvmStatic
    fun setLogger(custom: EngineLogger?) {
        logger = custom ?: DefaultEngineLogger(DEFAULT_TAG)
    }

    @JvmStatic
    fun d(tag: String, message: String) = logger.d(tag, message)

    @JvmStatic
    fun i(tag: String, message: String) = logger.i(tag, message)

    @JvmStatic
    fun w(tag: String, message: String) = logger.w(tag, message)

    @JvmStatic
    fun e(tag: String, message: String) = logger.e(tag, message)

    @JvmStatic
    fun e(tag: String, message: String, throwable: Throwable?) = logger.e(tag, message, throwable)
}
