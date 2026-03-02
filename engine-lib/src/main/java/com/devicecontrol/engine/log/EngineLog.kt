package com.devicecontrol.engine.log

import android.content.Context

/**
 * 引擎库统一日志入口
 * - 默认使用系统 Log；可通过 [setLogger] 外接自定义实现
 * - 可通过 [setFileLogger] 开启同时写入应用私有目录的日志文件（按日滚动）
 */
object EngineLog {
    private const val DEFAULT_TAG = "Engine"

    @Volatile
    private var logger: EngineLogger = DefaultEngineLogger(DEFAULT_TAG)

    @Volatile
    private var fileLogger: FileEngineLogger? = null

    /** 设置自定义日志实现；传 null 恢复为系统 Log */
    @JvmStatic
    fun setLogger(custom: EngineLogger?) {
        logger = custom ?: DefaultEngineLogger(DEFAULT_TAG)
    }

    /**
     * 开启写入文件的日志：在 [Context.getFilesDir]/[logSubDir] 下按日生成 engine_yyyyMMdd.log。
     * 若 [dualToLogcat] 为 true（默认），同时输出到系统 Log。
     * 多次调用会先关闭之前的文件句柄并替换为新的。
     */
    @JvmStatic
    fun setFileLogger(
        context: Context,
        logSubDir: String = "logs",
        dualToLogcat: Boolean = true
    ) {
        fileLogger?.close()
        val fl = FileEngineLogger(context, logSubDir, dualToLogcat)
        fileLogger = fl
        logger = fl
    }

    /** 关闭文件日志并恢复为仅使用系统 Log */
    @JvmStatic
    fun closeFileLogger() {
        fileLogger?.close()
        fileLogger = null
        logger = DefaultEngineLogger(DEFAULT_TAG)
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
