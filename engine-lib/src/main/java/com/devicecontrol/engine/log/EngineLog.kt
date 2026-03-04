package com.devicecontrol.engine.log

import android.content.Context

/**
 * 引擎库统一日志入口
 * - 主输出：默认系统 Log；可通过 [setLogger] 外接自定义实现
 * - 文件输出：通过 [setFileLogger] 开启后，与主输出同时生效（互不覆盖）
 */
object EngineLog {
    private const val DEFAULT_TAG = "Engine"

    @Volatile
    private var mainLogger: EngineLogger = DefaultEngineLogger(DEFAULT_TAG)

    @Volatile
    private var fileLogger: FileEngineLogger? = null

    /** 设置自定义主日志实现；传 null 恢复为系统 Log。不影响已开启的文件日志。 */
    @JvmStatic
    fun setLogger(custom: EngineLogger?) {
        mainLogger = custom ?: DefaultEngineLogger(DEFAULT_TAG)
    }

    /**
     * 开启写入文件的日志：在 [Context.getFilesDir]/[logSubDir] 下按日生成 engine_yyyyMMdd.log。
     * 与 [setLogger] 的主输出同时生效，不替换主 logger。
     * 若 [dualToLogcat] 为 true（默认），文件 logger 内部也会打 Logcat。
     * 多次调用会先关闭之前的文件句柄并替换为新的。
     */
    @JvmStatic
    fun setFileLogger(
        context: Context,
        logSubDir: String = "logs",
        dualToLogcat: Boolean = false,
    ) {
        fileLogger?.close()
        fileLogger = FileEngineLogger(context, logSubDir, dualToLogcat)
    }

    /** 关闭文件日志；主 logger 不变。 */
    @JvmStatic
    fun closeFileLogger() {
        fileLogger?.close()
        fileLogger = null
    }

    @JvmStatic
    fun d(tag: String, message: String) {
        mainLogger.d(tag, message)
        fileLogger?.d(tag, message)
    }

    @JvmStatic
    fun i(tag: String, message: String) {
        mainLogger.i(tag, message)
        fileLogger?.i(tag, message)
    }

    @JvmStatic
    fun w(tag: String, message: String) {
        mainLogger.w(tag, message)
        fileLogger?.w(tag, message)
    }

    @JvmStatic
    fun e(tag: String, message: String) {
        mainLogger.e(tag, message)
        fileLogger?.e(tag, message)
    }

    @JvmStatic
    fun e(tag: String, message: String, throwable: Throwable?) {
        mainLogger.e(tag, message, throwable)
        fileLogger?.e(tag, message, throwable)
    }
}
