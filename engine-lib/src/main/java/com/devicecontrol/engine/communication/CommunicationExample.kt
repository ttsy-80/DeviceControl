package com.devicecontrol.engine.communication

import android.content.Context
import com.devicecontrol.engine.communication.transport.UsbCommunicationTransport

/**
 * 统一通讯使用示例：业务层只关心连接、发送、接收
 *
 * 推荐使用 [CommunicationManager.scanAndConnect] 一步完成扫描+连接（扫到设备则连第一个）。
 * 使用 [CommunicationManager] + [ConnectTarget] 后，切换 USB / WiFi 只需更换 [CommunicationTransport]。
 */
object CommunicationExample {

    /** 一步扫描并连接：扫到设备则直接连第一个 */
    fun scanAndConnectExample(context: Context) {
        val manager = CommunicationManager.getInstance()
        val usbTransport = UsbCommunicationTransport(context)
        manager.setTransport(usbTransport)
        manager.setDataCallback(object : DataCallback {
            override fun onTextDataReceived(data: String) {}
            override fun onBinaryDataReceived(data: ByteArray) {}
            override fun onError(error: String) {}
        })
        val started = manager.scanAndConnect()
        if (started) {
            // 已发起连接（若需权限会走回调；连接成功后 isConnected() 为 true）
        }
        // 发送示例
        if (manager.isConnected()) {
            manager.sendText("Hello")
            manager.sendBinary(byteArrayOf(0x01, 0x02))
        }
        manager.disconnect()
        manager.release()
    }

//    /** 分步：先刷新列表，再手动选择目标连接 */
//    fun businessLayerUsage(context: Context) {
//        val manager = CommunicationManager.getInstance()
//        val usbTransport = UsbCommunicationTransport(context)
//        manager.setTransport(usbTransport)
//        manager.setDataCallback(object : DataCallback {
//            override fun onTextDataReceived(data: String) {}
//            override fun onBinaryDataReceived(data: ByteArray) {}
//            override fun onError(error: String) {}
//        })
//        manager.refreshAvailableTargets()
//        val targets = manager.getAvailableTargets()
//        val target = targets.firstOrNull()
//        if (target != null) {
//            manager.connect(target)
//        }
//        if (manager.isConnected()) {
//            manager.sendText("Hello")
//            manager.sendBinary(byteArrayOf(0x01, 0x02))
//        }
//        manager.disconnect()
//        manager.release()
//    }
}
