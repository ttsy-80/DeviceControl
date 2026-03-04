package com.devicecontrol.engine.log

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 调试用日志缓存：供 TaskControl 等把 EngineLog 输出写入列表，便于在调试页回看通讯逻辑。
 * 线程安全；超过 [maxLines] 会丢弃最旧的一条。
 */
object DebugLogHolder {
    private const val maxLines = 2000
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private val lines = CopyOnWriteArrayList<String>()

    fun add(level: String, tag: String, message: String, throwable: Throwable? = null) {
        val ts = dateFormat.format(Date())
        val line = "$ts $level [$tag] $message"
        synchronized(lines) {
            lines.add(line)
            if (throwable != null) {
                lines.add(throwable.stackTraceToString().lines().joinToString("\n") { "  $it" })
            }
            while (lines.size > maxLines) lines.removeAt(0)
        }
    }

    /** 当前日志列表快照（用于调试页展示） */
    fun getLogs(): List<String> = synchronized(lines) { lines.toList() }

    /** 清空日志 */
    fun clear() = synchronized(lines) { lines.clear() }
}
