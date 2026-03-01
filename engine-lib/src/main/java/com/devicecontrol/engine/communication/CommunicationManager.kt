package com.devicecontrol.engine.communication

import com.devicecontrol.engine.communication.protocol.CanOpenMessage
import com.devicecontrol.engine.log.EngineLog
import com.devicecontrol.engine.communication.protocol.CanOpenProtocol
import com.devicecontrol.engine.communication.protocol.CanUsbProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 统一通讯管理：业务层只关心连接、发送数据、接收数据
 * 连接层由 [CommunicationTransport] 泛化，可插拔 USB、WiFi 等实现
 */
class CommunicationManager private constructor() {

    companion object {
        private const val TAG = "CommMgr"
        @Volatile
        private var INSTANCE: CommunicationManager? = null
        fun getInstance(): CommunicationManager =
            INSTANCE ?: synchronized(this) { INSTANCE ?: CommunicationManager().also { INSTANCE = it } }
    }

    private var currentTransport: CommunicationTransport? = null
    private var dataCallback: DataCallback? = null

    /** CAN Open 解析后的消息回调（可选） */
    interface CanOpenMessageCallback {
        fun onCanOpenMessageReceived(message: CanOpenMessage)
    }

    private var canOpenMessageCallback: CanOpenMessageCallback? = null

    private val _defaultState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState>
        get() = currentTransport?.getConnectionState() ?: _defaultState.asStateFlow()

    /** 包装回调：先尝试解析为 CAN Open，再转发给业务 */
    private val wrappedCallback = object : DataCallback {
        override fun onTextDataReceived(data: String) {
            val messages = CanOpenProtocol.parseFromData(data.toByteArray(Charsets.US_ASCII))
            if (messages.isNotEmpty()) {
                messages.forEach { canOpenMessageCallback?.onCanOpenMessageReceived(it) }
            } else {
                dataCallback?.onTextDataReceived(data)
            }
        }
        override fun onBinaryDataReceived(data: ByteArray) {
            val messages = CanOpenProtocol.parseFromData(data)
            if (messages.isNotEmpty()) {
                messages.forEach { canOpenMessageCallback?.onCanOpenMessageReceived(it) }
            } else {
                dataCallback?.onBinaryDataReceived(data)
            }
        }
        override fun onError(error: String) {
            dataCallback?.onError(error)
        }
    }

    /** 设置当前连接方式（USB / WiFi 等） */
    fun setTransport(transport: CommunicationTransport?) {
        if (currentTransport?.isConnected() == true) {
            EngineLog.w(TAG, "setTransport: 已连接，请先断开")
            dataCallback?.onError("请先断开当前连接再切换传输方式")
            return
        }
        currentTransport?.release()
        currentTransport = transport
        if (transport == null) _defaultState.value = ConnectionState.Disconnected
        EngineLog.i(TAG, "setTransport: ${if (transport != null) "已设置" else "已清除"}")
    }

    /** 当前使用的传输（如 USB 运输实例，便于扩展协议如 setProtocol） */
    fun getCurrentTransport(): CommunicationTransport? = currentTransport

    fun getAvailableTargets(): List<ConnectTarget> =
        currentTransport?.getAvailableTargets() ?: emptyList()

    fun refreshAvailableTargets() {
        currentTransport?.refreshAvailableTargets()
        EngineLog.d(TAG, "refreshAvailableTargets: 当前目标数=${getAvailableTargets().size}")
    }

    fun setDataCallback(callback: DataCallback?) {
        dataCallback = callback
    }

    fun setCanOpenMessageCallback(callback: CanOpenMessageCallback?) {
        canOpenMessageCallback = callback
    }

//    /** 连接：由业务传入 [ConnectTarget]（如从列表选择） */
//    fun connect(target: ConnectTarget, callback: DataCallback? = null) {
//        if (callback != null) dataCallback = callback
//        val transport = currentTransport
//        if (transport == null) {
//            dataCallback?.onError("未设置传输方式，请先 setTransport(UsbCommunicationTransport 或 WiFi 实现)")
//            return
//        }
//        val toUse = if (dataCallback != null || canOpenMessageCallback != null) wrappedCallback else null
//        transport.connect(target, toUse)
//    }

    /**
     * 扫描并连接：先刷新可用目标，若扫到设备则直接连接第一个
     * @param callback 可选，若传入则同时作为本次连接的数据回调
     * @return true 已发起连接（扫到至少一个目标），false 未设置传输或未扫到设备
     */
    fun scanAndConnect(callback: DataCallback? = null): Boolean {
        if (callback != null) dataCallback = callback
        val transport = currentTransport
        if (transport == null) {
            EngineLog.e(TAG, "scanAndConnect: 未设置传输方式")
            dataCallback?.onError("未设置传输方式，请先 setTransport(...)")
            return false
        }
        refreshAvailableTargets()
        val targets = getAvailableTargets()
        val first = targets.firstOrNull()
        if (first == null) {
            EngineLog.w(TAG, "scanAndConnect: 未扫描到设备")
            dataCallback?.onError("未扫描到设备")
            return false
        }
        EngineLog.i(TAG, "scanAndConnect: 扫到 ${targets.size} 个目标，连接第一个")
        val toUse = if (dataCallback != null || canOpenMessageCallback != null) wrappedCallback else null
        transport.connect(first, toUse)
        return true
    }

    fun disconnect() {
        currentTransport?.disconnect()
        EngineLog.i(TAG, "disconnect: 已断开")
    }

    fun isConnected(): Boolean = currentTransport?.isConnected() ?: false

    fun sendText(text: String): Boolean {
        return if (currentTransport?.isConnected() == true) {
            val ok = currentTransport!!.sendText(text)
            EngineLog.d(TAG, "sendText: ${if (ok) "ok" else "fail"}, len=${text.length}")
            ok
        } else {
            EngineLog.w(TAG, "sendText: 未连接")
            dataCallback?.onError("未连接")
            false
        }
    }

    fun sendBinary(data: ByteArray): Boolean {
        return if (currentTransport?.isConnected() == true) {
            val ok = currentTransport!!.sendBinary(data)
            EngineLog.d(TAG, "sendBinary: ${if (ok) "ok" else "fail"}, len=${data.size}")
            ok
        } else {
            EngineLog.w(TAG, "sendBinary: 未连接")
            dataCallback?.onError("未连接")
            false
        }
    }

    // ========== CAN Open / CANUSB：统一走 sendText，与传输无关 ==========

    fun sendCanOpenMessage(message: CanOpenMessage): Boolean =
        sendText(message.toProtocolString())

    fun sendCanOpenMessage(canId: Int, dlc: Int, data: ByteArray): Boolean =
        try {
            sendCanOpenMessage(CanOpenMessage(canId, dlc, data))
        } catch (e: Exception) {
            dataCallback?.onError("构建CAN Open消息失败: ${e.message}")
            false
        }

    fun sendCanOpenMessageFromHex(canId: Int, dataHex: String): Boolean =
        try {
            sendCanOpenMessage(CanOpenProtocol.buildFromHex(canId, dataHex))
        } catch (e: Exception) {
            dataCallback?.onError("构建CAN Open消息失败: ${e.message}")
            false
        }

    fun setCanBaudRate(baudRate: CanUsbProtocol.CanBaudRate): Boolean =
        sendText(CanUsbProtocol.setBaudRate(baudRate))

    fun openCanChannel(): Boolean = sendText(CanUsbProtocol.openCan())
    fun closeCanChannel(): Boolean = sendText(CanUsbProtocol.closeCan())
    fun getCanUsbVersion(): Boolean = sendText(CanUsbProtocol.getVersion())
    fun getCanUsbSerialNumber(): Boolean = sendText(CanUsbProtocol.getSerialNumber())
    fun readCanUsbErrorFlags(): Boolean = sendText(CanUsbProtocol.readErrorFlags())
    fun setCanUsbTimeStamp(enabled: Boolean): Boolean = sendText(CanUsbProtocol.setTimeStamp(enabled))

    fun sendStandardCanFrame(canId: Int, dlc: Int, data: ByteArray): Boolean =
        try {
            sendText(CanUsbProtocol.sendStandardFrame(canId, dlc, data))
        } catch (e: Exception) {
            dataCallback?.onError("发送标准CAN帧失败: ${e.message}")
            false
        }

    fun sendExtendedCanFrame(canId: Int, dlc: Int, data: ByteArray): Boolean =
        try {
            sendText(CanUsbProtocol.sendExtendedFrame(canId, dlc, data))
        } catch (e: Exception) {
            dataCallback?.onError("发送扩展CAN帧失败: ${e.message}")
            false
        }

    fun sendRtrFrame(canId: Int, dlc: Int, extended: Boolean = false): Boolean =
        try {
            sendText(CanUsbProtocol.sendRtrFrame(canId, dlc, extended))
        } catch (e: Exception) {
            dataCallback?.onError("发送RTR帧失败: ${e.message}")
            false
        }

    fun release() {
        EngineLog.i(TAG, "release: 释放资源")
        disconnect()
        currentTransport?.release()
        currentTransport = null
        _defaultState.value = ConnectionState.Disconnected
    }
}
