package com.devicecontrol.engine.communication

import android.content.Context
import com.devicecontrol.engine.communication.transport.UsbCommunicationTransport

/**
 * 统一通讯使用示例：业务层只关心连接、发送、接收
 *
 * 使用 [CommunicationManager] + [ConnectTarget] 后，切换 USB / WiFi 只需更换 [CommunicationTransport]：
 * - USB：setTransport(UsbCommunicationTransport(context))
 * - WiFi：setTransport(WifiCommunicationTransport())（后续实现）
 */
object CommunicationExample {

    fun businessLayerUsage(context: Context) {
        val manager = CommunicationManager.getInstance()

        // 1. 选择连接方式（USB / 后续 WiFi）
        val usbTransport = UsbCommunicationTransport(context)
        manager.setTransport(usbTransport)

        // 2. 设置接收与错误回调
        manager.setDataCallback(object : DataCallback {
            override fun onTextDataReceived(data: String) {}
            override fun onBinaryDataReceived(data: ByteArray) {}
            override fun onError(error: String) {}
        })

        // 3. 刷新并获取可用目标
        manager.refreshAvailableTargets()
        val targets = manager.getAvailableTargets()

        // 4. 连接（任选一个目标，如第一个 USB 设备）
        val target = targets.filterIsInstance<ConnectTarget.Usb>().firstOrNull()
        if (target != null) {
            manager.connect(target)
        }

        // 5. 发送
        if (manager.isConnected()) {
            manager.sendText("Hello")
            manager.sendBinary(byteArrayOf(0x01, 0x02))
        }

        // 6. 断开与释放
        manager.disconnect()
        manager.release()
    }
}
