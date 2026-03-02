package com.devicecontrol.engine.log

import android.content.Context
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 将日志写入文件的 [EngineLogger] 实现。
 * - 日志目录：应用私有目录 [Context.getFilesDir]/[logSubDir]，默认子目录名为 "logs"
 * - 按日滚动：文件名 engine_yyyyMMdd.log，每日一个文件
 * - 可选同时输出到 Logcat：[dualToLogcat] 为 true 时同时使用 [DefaultEngineLogger]
 * - 文件写入在单独后台线程执行，不阻塞调用方（避免卡 UI / 通讯线程）
 *
 * @param context 用于获取应用私有存储路径，请传 Application 或 Activity 的 context
 * @param logSubDir 日志子目录名，相对 filesDir
 * @param dualToLogcat 是否同时输出到系统 Log（默认 true）
 */
class FileEngineLogger(
    private val context: Context,
    private val logSubDir: String = "logs",
    private val dualToLogcat: Boolean = true
) : EngineLogger {

    private val logcatDelegate: EngineLogger? = if (dualToLogcat) DefaultEngineLogger(DEFAULT_TAG) else null

    private val queue = LinkedBlockingQueue<LogEntry>()
    private val running = AtomicBoolean(true)
    private val writerThread = Thread(::runWriter, "EngineLog-Writer").apply { isDaemon = true; start() }

    private fun enqueue(level: String, tag: String, message: String, throwable: Throwable?) {
        if (!running.get()) return
        queue.offer(LogEntry(level, tag, message, throwable))
    }

    override fun d(tag: String, message: String) {
        logcatDelegate?.d(tag, message)
        enqueue("D", tag, message, null)
    }

    override fun i(tag: String, message: String) {
        logcatDelegate?.i(tag, message)
        enqueue("I", tag, message, null)
    }

    override fun w(tag: String, message: String) {
        logcatDelegate?.w(tag, message)
        enqueue("W", tag, message, null)
    }

    override fun e(tag: String, message: String) {
        logcatDelegate?.e(tag, message)
        enqueue("E", tag, message, null)
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        logcatDelegate?.e(tag, message, throwable)
        enqueue("E", tag, message, throwable)
    }

    private data class LogEntry(val level: String, val tag: String, val message: String, val throwable: Throwable?)

    @Volatile
    private var writer: Writer? = null

    @Volatile
    private var currentDateKey: String = ""

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val fileDateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)

    private fun runWriter() {
        while (running.get()) {
            try {
                val entry = queue.take()
                if (entry.level == POISON_LEVEL) break
                writeLine(entry)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                break
            } catch (e: Exception) {
                logcatDelegate?.e(TAG, "FileEngineLogger write failed: ${e.message}", e)
            }
        }
        drainAndWrite()
        closeWriter()
    }

    private fun drainAndWrite() {
        val remaining = mutableListOf<LogEntry>()
        queue.drainTo(remaining)
        remaining.filter { it.level != POISON_LEVEL }.forEach { writeLine(it) }
    }

    private fun writeLine(entry: LogEntry) {
        try {
            val dateKey = fileDateFormat.format(Date())
            if (writer == null || dateKey != currentDateKey) {
                closeWriter()
                val dir = File(context.filesDir, logSubDir)
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, "engine_$dateKey.log")
                writer = BufferedWriter(OutputStreamWriter(FileOutputStream(file, true), Charsets.UTF_8))
                currentDateKey = dateKey
            }
            val w = writer ?: return
            val ts = dateFormat.format(Date())
            w.write("$ts ${entry.level} [${entry.tag}] ${entry.message}\n")
            entry.throwable?.let {
                w.write(it.stackTraceToString())
                w.write("\n")
            }
            w.flush()
        } catch (e: Exception) {
            logcatDelegate?.e(TAG, "FileEngineLogger write failed: ${e.message}", e)
        }
    }

    private fun closeWriter() {
        try {
            writer?.close()
        } catch (_: Exception) { }
        writer = null
        currentDateKey = ""
    }

    /** 关闭文件句柄并停止写线程，释放资源。在 Application.onTerminate 或不再需要写文件时调用（可选） */
    fun close() {
        if (!running.compareAndSet(true, false)) return
        queue.offer(LogEntry(POISON_LEVEL, "", "", null))
        try {
            writerThread.join(2000)
        } catch (_: InterruptedException) { }
    }

    companion object {
        private const val DEFAULT_TAG = "Engine"
        private const val TAG = "FileEngineLogger"
        private const val POISON_LEVEL = ""
    }
}
