package com.devicecontrol.engine.communication.protocol

import com.devicecontrol.engine.log.EngineLog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException

// ==================== 传输接口 ====================

/**
 * SLCAN 上游传输接口。
 * 由外部实现（VCP、TCP 等），SlcanManager 通过它发送 ASCII 指令。
 */
interface SlcanTransport {
    fun send(data: String): Boolean
}

// ==================== 数据结构 ====================

/**
 * CAN 帧（标准帧 11 位 ID，扩展帧 29 位 ID）
 */
data class CanFrame(
    val id: Int,
    val data: ByteArray,
    val extended: Boolean = false
) {
    val dlc: Int get() = data.size

    /** 编码为 SLCAN 发送格式：t{ID3}{DLC}{DATA}\r 或 T{ID8}{DLC}{DATA}\r */
    fun toSlcan(): String {
        val prefix = if (extended) "T" else "t"
        val idHex = if (extended) {
            id.toString(16).uppercase().padStart(8, '0')
        } else {
            id.toString(16).uppercase().padStart(3, '0')
        }
        val dataHex = data.joinToString("") {
            it.toUByte().toString(16).uppercase().padStart(2, '0')
        }
        return "$prefix$idHex${dlc}$dataHex\r"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CanFrame) return false
        return id == other.id && data.contentEquals(other.data) && extended == other.extended
    }

    override fun hashCode(): Int = id * 31 + data.contentHashCode()

    override fun toString(): String {
        val idStr = if (extended) "0x${id.toString(16).uppercase().padStart(8, '0')}"
        else "0x${id.toString(16).uppercase().padStart(3, '0')}"
        val dataStr = data.joinToString(" ") { it.toUByte().toString(16).uppercase().padStart(2, '0') }
        return "CanFrame($idStr [$dlc] $dataStr)"
    }

    companion object {
        /**
         * 从 SLCAN 接收行解析 CAN 帧。
         * 格式：t{ID3}{DLC}{DATA} 或 T{ID8}{DLC}{DATA}
         * @return 解析成功返回 CanFrame，否则 null
         */
        fun fromSlcan(line: String): CanFrame? {
            if (line.isEmpty()) return null
            val prefix = line[0]
            val isExtended = when (prefix) {
                't' -> false
                'T' -> true
                else -> return null
            }
            val idLen = if (isExtended) 8 else 3
            val content = line.substring(1)
            if (content.length < idLen + 1) return null

            val idHex = content.substring(0, idLen)
            val canId = idHex.toIntOrNull(16) ?: return null
            val dlc = content[idLen].toString().toIntOrNull() ?: return null
            if (dlc !in 0..8) return null

            val dataStart = idLen + 1
            val dataHex = content.substring(dataStart, minOf(dataStart + dlc * 2, content.length))
            if (dataHex.length != dlc * 2) return null

            val data = ByteArray(dlc) { i ->
                dataHex.substring(i * 2, i * 2 + 2).toIntOrNull(16)?.toByte() ?: return null
            }
            return CanFrame(canId, data, isExtended)
        }

        /** 构造 CANOpen SDO 写请求帧（Expedited，4 字节数据） */
        fun sdoWrite(nodeId: Int, index: Int, subIndex: Int, value: Int): CanFrame {
            val cobId = 0x600 + nodeId
            val data = ByteArray(8)
            data[0] = 0x23.toByte()              // SDO 写命令（4 字节 expedited）
            data[1] = (index and 0xFF).toByte()
            data[2] = (index shr 8 and 0xFF).toByte()
            data[3] = (subIndex and 0xFF).toByte()
            data[4] = (value and 0xFF).toByte()
            data[5] = (value shr 8 and 0xFF).toByte()
            data[6] = (value shr 16 and 0xFF).toByte()
            data[7] = (value shr 24 and 0xFF).toByte()
            return CanFrame(cobId, data)
        }

        /** 构造 CANOpen SDO 写请求帧（Expedited，2 字节数据） */
        fun sdoWrite2B(nodeId: Int, index: Int, subIndex: Int, value: Int): CanFrame {
            val cobId = 0x600 + nodeId
            val data = ByteArray(8)
            data[0] = 0x2B.toByte()              // SDO 写命令（2 字节 expedited）
            data[1] = (index and 0xFF).toByte()
            data[2] = (index shr 8 and 0xFF).toByte()
            data[3] = (subIndex and 0xFF).toByte()
            data[4] = (value and 0xFF).toByte()
            data[5] = (value shr 8 and 0xFF).toByte()
            data[6] = 0
            data[7] = 0
            return CanFrame(cobId, data)
        }

        /** 构造 CANOpen SDO 写请求帧（Expedited，1 字节数据） */
        fun sdoWrite1B(nodeId: Int, index: Int, subIndex: Int, value: Int): CanFrame {
            val cobId = 0x600 + nodeId
            val data = ByteArray(8)
            data[0] = 0x2F.toByte()              // SDO 写命令（1 字节 expedited）
            data[1] = (index and 0xFF).toByte()
            data[2] = (index shr 8 and 0xFF).toByte()
            data[3] = (subIndex and 0xFF).toByte()
            data[4] = (value and 0xFF).toByte()
            data[5] = 0
            data[6] = 0
            data[7] = 0
            return CanFrame(cobId, data)
        }

        /** 构造 CANOpen SDO 读请求帧 */
        fun sdoRead(nodeId: Int, index: Int, subIndex: Int): CanFrame {
            val cobId = 0x600 + nodeId
            val data = ByteArray(8)
            data[0] = 0x40.toByte()              // SDO 读命令
            data[1] = (index and 0xFF).toByte()
            data[2] = (index shr 8 and 0xFF).toByte()
            data[3] = (subIndex and 0xFF).toByte()
            return CanFrame(cobId, data)
        }
    }
}

/**
 * SLCAN 请求描述：发什么帧、期望什么响应、怎么校验、怎么提取值。
 */
data class SlcanRequest(
    /** 要发送的 CAN 帧 */
    val frame: CanFrame,

    /** 期望响应帧的 CAN ID，null 表示只关心 SLCAN ACK */
    val expectedResponseId: Int? = null,

    /** 校验响应帧，返回 null=通过，返回 String=失败原因 */
    val validator: ((CanFrame) -> String?)? = null,

    /** 从响应帧提取业务值 */
    val extractor: ((CanFrame) -> Map<String, Any>)? = null,

    /** 可读描述（日志、错误信息用） */
    val label: String = ""
)

/**
 * 执行结果：成功/失败 + 提取的值 + 错误原因。
 */
data class SlcanResult(
    val success: Boolean,
    val values: Map<String, Any> = emptyMap(),
    val error: String? = null,
    val failedIndex: Int = -1
) {
    companion object {
        fun ok(values: Map<String, Any> = emptyMap()) = SlcanResult(true, values)
        fun fail(reason: String, failedIndex: Int = -1) = SlcanResult(false, error = reason, failedIndex = failedIndex)
    }
}

// ==================== SLCAN 命令工具 ====================

/** SLCAN 协议命令（LAWICEL CANUSB 手册） */
object SlcanCommands {
    /** CAN 波特率枚举 */
    enum class BaudRate(val code: Int) {
        BPS_10K(0),
        BPS_20K(1),
        BPS_50K(2),
        BPS_100K(3),
        BPS_125K(4),
        BPS_250K(5),
        BPS_500K(6),
        BPS_800K(7),
        BPS_1M(8),
    }

    fun setBaudRate(rate: BaudRate): String = "S${rate.code}\r"
    fun open(): String = "O\r"
    fun close(): String = "C\r"
    fun version(): String = "V\r"
    fun serialNumber(): String = "N\r"
    fun errorFlags(): String = "F\r"
    fun timestamp(on: Boolean): String = "Z${if (on) 1 else 0}\r"
}

// ==================== SlcanManager ====================

/**
 * SLCAN 协议执行引擎（传输无关）。
 *
 * - 通过 [SlcanTransport] 发送 ASCII 指令
 * - 通过 [feedData] 接收上游喂入的原始字节
 * - 内部行缓冲按 `\r` 或 `\x07` 分帧
 * - 提供 [execute] 批量请求-响应，支持超时与校验
 */
class SlcanManager(private val transport: SlcanTransport) {

    companion object {
        private const val TAG = "SlcanMgr"
        private const val BELL = '\u0007'

        // ---- SDO 校验工具 ----

        /**
         * 标准 SDO 写确认校验器：响应首字节应为 0x60。
         * 若为 0x80 则为 SDO Abort，提取 abort code。
         */
        val sdoWriteValidator: (CanFrame) -> String? = { resp ->
            when (resp.data[0].toInt() and 0xFF) {
                0x60 -> null  // 写确认 OK
                0x80 -> {
                    val abortCode = (resp.data[4].toInt() and 0xFF) or
                            ((resp.data[5].toInt() and 0xFF) shl 8) or
                            ((resp.data[6].toInt() and 0xFF) shl 16) or
                            ((resp.data[7].toInt() and 0xFF) shl 24)
                    "SDO Abort: 0x${abortCode.toString(16).uppercase()}"
                }
                else -> "SDO 响应异常: 首字节=0x${resp.data[0].toUByte().toString(16)}"
            }
        }

        /**
         * 从 SDO 读响应中提取 4 字节小端整数值。
         */
        fun sdoReadExtractor(key: String): (CanFrame) -> Map<String, Any> = { resp ->
            val value = (resp.data[4].toInt() and 0xFF) or
                    ((resp.data[5].toInt() and 0xFF) shl 8) or
                    ((resp.data[6].toInt() and 0xFF) shl 16) or
                    ((resp.data[7].toInt() and 0xFF) shl 24)
            mapOf(key to value)
        }

        /**
         * SDO 读响应校验器：首字节应为 0x43（4字节）/ 0x4B（2字节）/ 0x4F（1字节）。
         */
        val sdoReadValidator: (CanFrame) -> String? = { resp ->
            val cmd = resp.data[0].toInt() and 0xFF
            if (cmd in listOf(0x42, 0x43, 0x4B, 0x4F)) null
            else if (cmd == 0x80) {
                val abortCode = (resp.data[4].toInt() and 0xFF) or
                        ((resp.data[5].toInt() and 0xFF) shl 8) or
                        ((resp.data[6].toInt() and 0xFF) shl 16) or
                        ((resp.data[7].toInt() and 0xFF) shl 24)
                "SDO 读取被拒绝: Abort=0x${abortCode.toString(16).uppercase()}"
            } else "SDO 读响应异常: 首字节=0x${resp.data[0].toUByte().toString(16)}"
        }
    }

    enum class State { IDLE, READY, ERROR }

    @Volatile
    var state: State = State.IDLE
        private set

    // ---- 行缓冲 ----
    private val lineBuffer = StringBuilder()

    // ---- 请求-响应同步 ----
    // pendingAck: SLCAN 层 ACK（\r=true, BELL=false）
    private var pendingAck: CompletableDeferred<Boolean>? = null
    // pendingFrame: 等待的 CAN 帧响应
    private var pendingFrameId: Int? = null
    private var pendingFrame: CompletableDeferred<CanFrame?>? = null

    // 串行化指令发送，防止并发
    private val sendMutex = Mutex()

    /**
     * 上游收到原始字节后调用此方法。
     * 内部按 `\r` 和 `\x07` 分帧并驱动请求-响应状态机。
     */
    fun feedData(data: ByteArray) {
        val text = String(data, Charsets.US_ASCII)
        for (ch in text) {
            when (ch) {
                '\r' -> {
                    val line = lineBuffer.toString()
                    lineBuffer.clear()
                    processLine(line)
                }
                BELL -> {
                    lineBuffer.clear()
                    EngineLog.w(TAG, "feedData: 收到 BELL (设备拒绝)")
                    pendingAck?.complete(false)
                }
                '\n' -> { /* 忽略 LF */ }
                else -> lineBuffer.append(ch)
            }
        }
    }

    private fun processLine(line: String) {
        if (line.isEmpty()) {
            // 空行 = 纯 \r = SLCAN ACK (OK)
            EngineLog.d(TAG, "processLine: ACK (OK)")
            pendingAck?.complete(true)
            return
        }

        // 尝试解析为 CAN 帧
        val frame = CanFrame.fromSlcan(line)
        if (frame != null) {
            EngineLog.d(TAG, "processLine: 收到 CAN 帧 $frame")
            // 先回复 ACK（SLCAN 也会对数据帧回 \r）
            pendingAck?.complete(true)
            // 如果正在等待某个 CAN ID 的响应
            if (pendingFrameId != null && frame.id == pendingFrameId) {
                pendingFrame?.complete(frame)
            }
            return
        }

        // 其它响应（V1013, NA123, F00 等）
        EngineLog.d(TAG, "processLine: 响应 '$line'")
        pendingAck?.complete(true)
    }

    // ---- 底层发送与等待 ----

    /**
     * 发送一条原始 SLCAN 命令并等待 ACK。
     * @return true=ACK(\r), false=BELL/超时
     */
    private suspend fun sendRaw(cmd: String, timeoutMs: Long): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        pendingAck = deferred

        if (!transport.send(cmd)) {
            pendingAck = null
            return false
        }

        return try {
            withTimeout(timeoutMs) { deferred.await() }
        } catch (e: TimeoutCancellationException) {
            EngineLog.w(TAG, "sendRaw: 超时 cmd=${cmd.trim()}")
            false
        } finally {
            pendingAck = null
        }
    }

    /**
     * 发送 CAN 帧后等待指定 CAN ID 的响应帧。
     * @return 响应帧，超时/未收到返回 null
     */
    private suspend fun sendAndWaitFrame(
        frame: CanFrame,
        expectedId: Int,
        timeoutMs: Long
    ): CanFrame? {
        val frameDeferred = CompletableDeferred<CanFrame?>()
        pendingFrameId = expectedId
        pendingFrame = frameDeferred

        // 先发送帧（也会等 SLCAN ACK）
        val ackOk = sendRaw(frame.toSlcan(), timeoutMs)
        if (!ackOk) {
            pendingFrameId = null
            pendingFrame = null
            return null
        }

        // 等 CAN 响应帧
        return try {
            withTimeout(timeoutMs) { frameDeferred.await() }
        } catch (e: TimeoutCancellationException) {
            EngineLog.w(TAG, "sendAndWaitFrame: 等待响应帧超时 expectedId=0x${expectedId.toString(16)}")
            null
        } finally {
            pendingFrameId = null
            pendingFrame = null
        }
    }

    // ---- 公开 API ----

    /**
     * SLCAN 握手初始化：C → S{n} → O
     */
    suspend fun init(
        baudRate: SlcanCommands.BaudRate = SlcanCommands.BaudRate.BPS_500K,
        timeoutMs: Long = 2000
    ): SlcanResult = sendMutex.withLock {
        EngineLog.i(TAG, "init: 开始握手 baudRate=$baudRate")

        // 1. 先关闭通道（防残留状态）
        sendRaw(SlcanCommands.close(), timeoutMs)
        // 忽略关闭的结果（可能本来就没开）

        // 2. 设置波特率
        if (!sendRaw(SlcanCommands.setBaudRate(baudRate), timeoutMs)) {
            state = State.ERROR
            return SlcanResult.fail("设置波特率失败")
        }

        // 3. 打开通道
        if (!sendRaw(SlcanCommands.open(), timeoutMs)) {
            state = State.ERROR
            return SlcanResult.fail("打开 CAN 通道失败")
        }

        state = State.READY
        EngineLog.i(TAG, "init: 握手完成，状态 READY")
        SlcanResult.ok()
    }

    /**
     * 执行一组 [SlcanRequest]，逐条发送、等响应、校验、提取值。
     * 全部成功返回合并的值，任一失败立即返回错误。
     */
    suspend fun execute(
        requests: List<SlcanRequest>,
        perCmdTimeoutMs: Long = 2000
    ): SlcanResult = sendMutex.withLock {
        if (state != State.READY) {
            return SlcanResult.fail("SLCAN 未就绪（当前状态: $state）")
        }

        val allValues = mutableMapOf<String, Any>()

        for ((index, req) in requests.withIndex()) {
            EngineLog.d(TAG, "execute[$index]: ${req.label} → ${req.frame}")

            if (req.expectedResponseId != null) {
                // 需要等 CAN 响应帧
                val resp = sendAndWaitFrame(req.frame, req.expectedResponseId, perCmdTimeoutMs)
                if (resp == null) {
                    return SlcanResult.fail(
                        "未收到响应: ${req.label}",
                        failedIndex = index
                    )
                }

                // 校验
                val validateErr = req.validator?.invoke(resp)
                if (validateErr != null) {
                    return SlcanResult.fail(validateErr, failedIndex = index)
                }

                // 提取值
                req.extractor?.invoke(resp)?.let { allValues.putAll(it) }
            } else {
                // 只需要 SLCAN ACK
                val ackOk = sendRaw(req.frame.toSlcan(), perCmdTimeoutMs)
                if (!ackOk) {
                    return SlcanResult.fail(
                        "指令被拒绝或超时: ${req.label}",
                        failedIndex = index
                    )
                }
            }
        }

        SlcanResult.ok(allValues)
    }

    /**
     * 快捷方法：发送单帧并等待 SLCAN ACK。
     */
    suspend fun sendFrame(frame: CanFrame, timeoutMs: Long = 2000): SlcanResult =
        execute(listOf(SlcanRequest(frame, label = "单帧发送")), timeoutMs)

    /**
     * 快捷方法：发送原始 SLCAN 命令（非 CAN 帧，如 V\r 查版本）。
     */
    suspend fun sendSlcanCommand(cmd: String, timeoutMs: Long = 2000): SlcanResult =
        sendMutex.withLock {
            val ok = sendRaw(cmd, timeoutMs)
            if (ok) SlcanResult.ok() else SlcanResult.fail("SLCAN 命令失败: ${cmd.trim()}")
        }

    /**
     * 关闭 CAN 通道。
     */
    suspend fun close(timeoutMs: Long = 2000): SlcanResult = sendMutex.withLock {
        val ok = sendRaw(SlcanCommands.close(), timeoutMs)
        state = State.IDLE
        if (ok) SlcanResult.ok() else SlcanResult.fail("关闭通道失败")
    }
}
