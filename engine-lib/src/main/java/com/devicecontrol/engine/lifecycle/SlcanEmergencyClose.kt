package com.devicecontrol.engine.lifecycle

import com.devicecontrol.engine.communication.protocol.SlcanCommands
import com.devicecontrol.engine.usbserial.UsbSerialVcpManager

/**
 * 进程即将因未捕获 Java 异常退出时，尽量同步下发 SLCAN 关闭通道 [SlcanCommands.close]，
 * 避免适配器/CAN 侧长时间保持打开态。
 *
 * **限制**（需与产品/现场说明一致）：
 * - 仅对 **Java/Kotlin 未捕获异常** 生效；**Native 崩溃、ANR 强杀、系统回收、用户划掉任务** 等通常走不到这里。
 * - 不等待适配器回包，只做「尽力而为」的一次写串口；若 USB 已断或未连接则无效。
 * - 若还需停电机，需在驱动器侧配安全策略或另做看门狗，不能单靠 App。
 */
object SlcanEmergencyClose {

    @Volatile
    private var boundVcp: UsbSerialVcpManager? = null

    private var installedPrevious: Thread.UncaughtExceptionHandler? = null

    /** 任务控制页创建 USB 管理器后绑定；正常退出前 [unbind]。 */
    fun bind(usb: UsbSerialVcpManager) {
        boundVcp = usb
    }

    fun unbind() {
        boundVcp = null
    }

    /**
     * 同步发送关闭通道命令（与 [SlcanTransport] 使用相同字节序列，用 [UsbSerialVcpManager.sendText] 避免再追加换行）。
     */
    fun trySendCloseCommand() {
        val usb = boundVcp ?: return
        try {
            usb.sendText(SlcanCommands.close())
        } catch (_: Throwable) {
            // 崩溃路径下不再抛异常
        }
    }

    /**
     * 在 [android.app.Application.onCreate] 中调用一次，链接系统默认的崩溃处理器。
     */
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
}
