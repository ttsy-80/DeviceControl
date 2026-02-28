package com.devicecontrol.engine.communication.protocol

/**
 * CAN Open 消息数据类
 * 用于封装CAN总线通信的消息格式
 * 
 * 协议格式：t{ID}{DLC}{DATA}\r
 * 示例：t60182B40600001000000\r
 * - t: 命令前缀（transmit/send）
 * - 601: CAN ID（11位或29位）
 * - 8: DLC（Data Length Code，数据长度，0-8）
 * - 2B40600001000000: CanOpen数据（十六进制，长度 = DLC * 2）
 * - \r: 回车符（carriage return）
 */
data class CanOpenMessage(
    /**
     * CAN ID（11位标准帧或29位扩展帧）
     */
    val canId: Int,
    
    /**
     * DLC (Data Length Code) - 数据长度，范围 0-8
     */
    val dlc: Int,
    
    /**
     * CanOpen数据（字节数组）
     */
    val data: ByteArray
) {
    init {
        require(dlc in 0..8) { "DLC必须在0-8之间" }
        require(data.size == dlc) { "数据长度必须等于DLC" }
    }
    
    /**
     * 将消息转换为协议格式字符串
     * 格式：t{ID}{DLC}{DATA}\r
     * 例如：t60182B40600001000000\r
     */
    fun toProtocolString(): String {
        // CAN ID转换为十六进制字符串（至少3位，不足补0）
        val idHex = canId.toString(16).uppercase().padStart(3, '0')
        
        // DLC转换为字符串
        val dlcStr = dlc.toString()
        
        // 数据转换为十六进制字符串（每个字节2个字符）
        val dataHex = data.joinToString("") { byte ->
            byte.toUByte().toString(16).uppercase().padStart(2, '0')
        }
        
        // 组合成协议格式：t{ID}{DLC}{DATA}\r
        return "t$idHex$dlcStr$dataHex\r"
    }
    
    /**
     * 将消息转换为字节数组（用于二进制发送）
     */
    fun toByteArray(): ByteArray {
        return toProtocolString().toByteArray(Charsets.US_ASCII)
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as CanOpenMessage
        
        if (canId != other.canId) return false
        if (dlc != other.dlc) return false
        if (!data.contentEquals(other.data)) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = canId
        result = 31 * result + dlc
        result = 31 * result + data.contentHashCode()
        return result
    }
    
    override fun toString(): String {
        return "CanOpenMessage(canId=0x${canId.toString(16).uppercase()}, dlc=$dlc, data=${data.joinToString(" ") { it.toUByte().toString(16).uppercase().padStart(2, '0') }})"
    }
}
