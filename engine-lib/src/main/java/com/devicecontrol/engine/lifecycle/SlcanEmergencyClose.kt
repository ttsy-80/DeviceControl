package com.devicecontrol.engine.lifecycle

import com.devicecontrol.engine.communication.link.SlcanDataLink
import com.devicecontrol.engine.communication.protocol.SlcanCommands
import com.devicecontrol.engine.usbserial.UsbSerialVcpManager

/**
 * 进程即将因未捕获 Java 异常退出时，尽量同步下发 SLCAN 关闭通道 [SlcanCommands.close]，
 * 避免适配器/CAN 侧长时间保持打开态。
 */
object SlcanEmergencyClose {

    @Volatile
    private var boundLink: SlcanDataLink? = null

    @Deprecated("Use bind(SlcanDataLink)", ReplaceWith("bind(UsbSlcanDataLink(usb))"))
    fun bind(usb: UsbSerialVcpManager) {
        boundLink = object : SlcanDataLink {
            override fun isConnected(): Boolean = usb.isConnected()
            override fun sendTextLine(text: String): Boolean = usb.sendTextLine(text)
            override fun sendText(text: String): Boolean = usb.sendText(text)
            override fun disconnect() = usb.disconnect()
            override fun release() = usb.release()
        }
    }

    fun bind(link: SlcanDataLink) {
        boundLink = link
    }

    fun unbind() {
        boundLink = null
    }

    fun trySendCloseCommand() {
        val link = boundLink ?: return
        try {
            link.sendText(SlcanCommands.close())
        } catch (_: Throwable) {
            // 崩溃路径下不再抛异常
        }
    }

    @Synchronized
    fun installUncaughtExceptionHandler() {
        if (installedPrevious != null) return
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        installedPrevious = previous
        Thread.setDefaultUncaughtExceptionHandler { thread, e ->
            try {
                trySendCloseCommand()
            } catch (_: Throwable) {
            }
            previous?.uncaughtException(thread, e)
        }
    }

    private var installedPrevious: Thread.UncaughtExceptionHandler? = null
}
