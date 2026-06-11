package com.devicecontrol.engine.communication.link

import com.devicecontrol.engine.usbserial.UsbSerialVcpManager

/** [UsbSerialVcpManager] 的 [SlcanDataLink] 适配。 */
class UsbSlcanDataLink(
    private val manager: UsbSerialVcpManager,
) : SlcanDataLink {

    override fun isConnected(): Boolean = manager.isConnected()

    override fun sendTextLine(text: String): Boolean = manager.sendTextLine(text)

    override fun sendText(text: String): Boolean = manager.sendText(text)

    override fun disconnect() = manager.disconnect()

    override fun release() = manager.release()
}
