package com.devicecontrol.engine.communication.protocol

/**
 * CANUSB协议命令封装类
 * 
 * 根据LAWICEL CANUSB手册实现
 * CANUSB是一个CAN到USB的转换器，通过VCP（虚拟串口）进行通信
 * 
 * 参考文档：CANUSB Manual v1.0D
 * 网址：www.canusb.com
 */
object CanUsbProtocol {
    
    /**
     * CAN波特率枚举
     * 对应CANUSB的S命令
     */
    enum class CanBaudRate(val value: Int) {
        BPS_10K(0),
        BPS_20K(1),
        BPS_50K(2),
        BPS_100K(3),
        BPS_125K(4),
        BPS_250K(5),
        BPS_500K(6),
        BPS_800K(7),
        BPS_1M(8);
        
        companion object {
            fun fromValue(value: Int): CanBaudRate? {
                return values().find { it.value == value }
            }
        }
    }
    
    /**
     * 设置CAN波特率
     * 命令格式：S{baudrate}[CR]
     * 示例：S6[CR] 设置500Kbps
     * 
     * @param baudRate CAN波特率
     * @return 命令字符串
     */
    fun setBaudRate(baudRate: CanBaudRate): String {
        return "S${baudRate.value}\r"
    }
    
    /**
     * 打开CAN通道
     * 命令格式：O[CR]
     * 必须在设置波特率后调用
     * 
     * @return 命令字符串
     */
    fun openCan(): String {
        return "O\r"
    }
    
    /**
     * 关闭CAN通道
     * 命令格式：C[CR]
     * 
     * @return 命令字符串
     */
    fun closeCan(): String {
        return "C\r"
    }
    
    /**
     * 获取版本号
     * 命令格式：V[CR]
     * 返回格式：V{硬件版本}{软件版本}[CR]
     * 示例：V1013[CR] 表示硬件版本1.0，软件版本1.3
     * 
     * @return 命令字符串
     */
    fun getVersion(): String {
        return "V\r"
    }
    
    /**
     * 获取序列号
     * 命令格式：N[CR]
     * 返回格式：N{序列号}[CR]
     * 示例：NA123[CR]
     * 
     * @return 命令字符串
     */
    fun getSerialNumber(): String {
        return "N\r"
    }
    
    /**
     * 读取错误标志
     * 命令格式：F[CR]
     * 返回格式：F{错误标志}[CR] 或 BELL (Ascii 7) 表示错误
     * 
     * @return 命令字符串
     */
    fun readErrorFlags(): String {
        return "F\r"
    }
    
    /**
     * 设置时间戳开关
     * 命令格式：Z{0|1}[CR]
     * Z0 - 关闭时间戳（默认）
     * Z1 - 开启时间戳
     * 
     * @param enabled 是否启用时间戳
     * @return 命令字符串
     */
    fun setTimeStamp(enabled: Boolean): String {
        return "Z${if (enabled) 1 else 0}\r"
    }
    
    /**
     * 发送标准CAN帧（11位ID）
     * 命令格式：t{ID}{DLC}{DATA}[CR]
     * 示例：t10021133[CR] 表示ID=0x100，DLC=2，数据=0x1133
     * 
     * @param canId CAN ID（11位，0-0x7FF）
     * @param dlc 数据长度（0-8）
     * @param data 数据字节数组
     * @return 命令字符串
     */
    fun sendStandardFrame(canId: Int, dlc: Int, data: ByteArray): String {
        require(canId in 0..0x7FF) { "标准帧CAN ID必须在0-0x7FF之间" }
        require(dlc in 0..8) { "DLC必须在0-8之间" }
        require(data.size == dlc) { "数据长度必须等于DLC" }
        
        val idHex = canId.toString(16).uppercase().padStart(3, '0')
        val dlcStr = dlc.toString()
        val dataHex = data.joinToString("") { byte ->
            byte.toUByte().toString(16).uppercase().padStart(2, '0')
        }
        
        return "t$idHex$dlcStr$dataHex\r"
    }
    
    /**
     * 发送扩展CAN帧（29位ID）
     * 命令格式：T{ID}{DLC}{DATA}[CR]
     * 示例：T1234567821133[CR] 表示ID=0x12345678，DLC=2，数据=0x1133
     * 
     * @param canId CAN ID（29位，0-0x1FFFFFFF）
     * @param dlc 数据长度（0-8）
     * @param data 数据字节数组
     * @return 命令字符串
     */
    fun sendExtendedFrame(canId: Int, dlc: Int, data: ByteArray): String {
        require(canId in 0..0x1FFFFFFF) { "扩展帧CAN ID必须在0-0x1FFFFFFF之间" }
        require(dlc in 0..8) { "DLC必须在0-8之间" }
        require(data.size == dlc) { "数据长度必须等于DLC" }
        
        val idHex = canId.toString(16).uppercase().padStart(8, '0')
        val dlcStr = dlc.toString()
        val dataHex = data.joinToString("") { byte ->
            byte.toUByte().toString(16).uppercase().padStart(2, '0')
        }
        
        return "T$idHex$dlcStr$dataHex\r"
    }
    
    /**
     * 发送RTR（Remote Transmission Request）帧
     * 命令格式：r{ID}{DLC}[CR] 或 R{ID}{DLC}[CR]
     * r - 标准帧RTR
     * R - 扩展帧RTR
     * 
     * @param canId CAN ID
     * @param dlc 数据长度（0-8）
     * @param extended 是否为扩展帧
     * @return 命令字符串
     */
    fun sendRtrFrame(canId: Int, dlc: Int, extended: Boolean = false): String {
        require(dlc in 0..8) { "DLC必须在0-8之间" }
        
        val prefix = if (extended) "R" else "r"
        val idHex = if (extended) {
            canId.toString(16).uppercase().padStart(8, '0')
        } else {
            require(canId in 0..0x7FF) { "标准帧CAN ID必须在0-0x7FF之间" }
            canId.toString(16).uppercase().padStart(3, '0')
        }
        val dlcStr = dlc.toString()
        
        return "$prefix$idHex$dlcStr\r"
    }
    
    /**
     * 解析接收到的CAN帧
     * 接收格式：t{ID}{DLC}{DATA}[CR] 或 T{ID}{DLC}{DATA}[CR]
     * 如果启用了时间戳，格式为：t{ID}{DLC}{DATA}{TIMESTAMP}[CR]
     * 
     * @param data 接收到的数据字符串
     * @return 解析后的CanOpenMessage，如果格式不正确返回null
     */
    fun parseReceivedFrame(data: String): CanOpenFrame? {
        try {
            val trimmed = data.trim()
            if (trimmed.isEmpty()) return null
            
            // 检查是标准帧还是扩展帧
            val isExtended = trimmed[0] == 'T'
            if (trimmed[0] != 't' && !isExtended) {
                return null
            }
            
            val content = trimmed.substring(1).trimEnd('\r', '\n')
            
            // 检查是否有时间戳（最后4个字符可能是时间戳）
            val hasTimeStamp = content.length >= 4 && content.length % 2 == 0
            
            // 提取DLC（最后一位数字）
            val dlcChar = content[content.length - (if (hasTimeStamp) 5 else 1)]
            val dlc = dlcChar.toString().toIntOrNull() ?: return null
            
            if (dlc !in 0..8) return null
            
            // 计算数据长度
            val dataLength = dlc * 2
            val timeStampLength = if (hasTimeStamp && content.length >= dataLength + 4) 4 else 0
            
            // 提取ID部分
            val idLength = if (isExtended) 8 else 3
            val remainingLength = content.length - timeStampLength - 1 // -1 for DLC
            
            if (remainingLength < idLength + dataLength) return null
            
            val idHex = content.substring(0, idLength)
            val canId = idHex.toIntOrNull(16) ?: return null
            
            // 提取数据部分
            val dataHex = content.substring(idLength, idLength + dataLength)
            val frameData = ByteArray(dlc) { index ->
                val hexByte = dataHex.substring(index * 2, (index + 1) * 2)
                hexByte.toInt(16).toByte()
            }
            
            // 提取时间戳（如果有）
            val timeStamp = if (timeStampLength > 0) {
                val timeStampHex = content.substring(content.length - timeStampLength)
                timeStampHex.toIntOrNull(16)
            } else {
                null
            }
            
            return CanOpenFrame(
                canId = canId,
                dlc = dlc,
                data = frameData,
                extended = isExtended,
                rtr = false,
                timeStamp = timeStamp
            )
            
        } catch (e: Exception) {
            return null
        }
    }
    
    /**
     * CAN帧数据类
     */
    data class CanOpenFrame(
        val canId: Int,
        val dlc: Int,
        val data: ByteArray,
        val extended: Boolean = false,
        val rtr: Boolean = false,
        val timeStamp: Int? = null
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            
            other as CanOpenFrame
            
            if (canId != other.canId) return false
            if (dlc != other.dlc) return false
            if (!data.contentEquals(other.data)) return false
            if (extended != other.extended) return false
            if (rtr != other.rtr) return false
            if (timeStamp != other.timeStamp) return false
            
            return true
        }
        
        override fun hashCode(): Int {
            var result = canId
            result = 31 * result + dlc
            result = 31 * result + data.contentHashCode()
            result = 31 * result + extended.hashCode()
            result = 31 * result + rtr.hashCode()
            result = 31 * result + (timeStamp ?: 0)
            return result
        }
        
        override fun toString(): String {
            val idStr = if (extended) {
                "0x${canId.toString(16).uppercase().padStart(8, '0')}"
            } else {
                "0x${canId.toString(16).uppercase().padStart(3, '0')}"
            }
            val dataStr = data.joinToString(" ") { 
                it.toUByte().toString(16).uppercase().padStart(2, '0') 
            }
            val timeStr = timeStamp?.let { " (TS: $it)" } ?: ""
            return "CanOpenFrame(ID=$idStr, DLC=$dlc, Data=[$dataStr], Extended=$extended, RTR=$rtr$timeStr)"
        }
    }
}
