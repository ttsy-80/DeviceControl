package com.devicecontrol.engine.communication.protocol

/**
 * CAN Open 协议解析器
 * 用于解析和构建CAN Open协议消息
 */
object CanOpenProtocol {
    
    /**
     * 从协议字符串解析CAN Open消息
     * 
     * @param protocolString 协议字符串，格式：t{ID}{DLC}{DATA}\r
     *                       示例：t60182B40600001000000\r
     * @return 解析后的CanOpenMessage，如果格式不正确返回null
     */
    fun parse(protocolString: String): CanOpenMessage? {
        try {
            // 移除可能的换行符和空白字符
            val trimmed = protocolString.trim()
            
            // 检查格式：必须以 't' 开头
            if (!trimmed.startsWith("t")) {
                return null
            }
            
            // 移除 't' 前缀和可能的 '\r' 或 '\n' 后缀
            val content = trimmed.removePrefix("t").trimEnd('\r', '\n')
            
            if (content.length < 4) {
                // 至少需要：ID(3位) + DLC(1位) = 4位
                return null
            }
            
            // 解析DLC（最后一位数字）
            val dlcChar = content.last()
            val dlc = dlcChar.toString().toIntOrNull() ?: return null
            
            if (dlc !in 0..8) {
                return null
            }
            
            // 移除DLC，得到ID和DATA部分
            val idAndData = content.dropLast(1)
            
            // 计算ID长度（至少3位，但可能更长）
            // ID通常是3位十六进制（11位CAN ID）或8位十六进制（29位扩展帧）
            // 数据长度 = DLC * 2（每个字节2个十六进制字符）
            val dataLength = dlc * 2
            
            if (idAndData.length < 3 + dataLength) {
                return null
            }
            
            // 提取ID（前3位或更多）
            // 尝试先取3位，如果还有数据则可能是扩展帧
            val idHex = if (idAndData.length > 3 + dataLength) {
                // 扩展帧，ID可能是8位
                val possibleIdLength = idAndData.length - dataLength
                idAndData.substring(0, possibleIdLength)
            } else {
                // 标准帧，ID是3位
                idAndData.substring(0, 3)
            }
            
            val canId = idHex.toIntOrNull(16) ?: return null
            
            // 提取数据部分
            val dataHex = idAndData.substring(idHex.length)
            
            if (dataHex.length != dataLength) {
                return null
            }
            
            // 将十六进制字符串转换为字节数组
            val data = ByteArray(dlc) { index ->
                val hexByte = dataHex.substring(index * 2, (index + 1) * 2)
                hexByte.toInt(16).toByte()
            }
            
            return CanOpenMessage(canId, dlc, data)
            
        } catch (e: Exception) {
            return null
        }
    }
    
    /**
     * 从接收到的原始数据中解析CAN Open消息
     * 支持文本格式和二进制格式
     * 
     * @param data 接收到的数据
     * @return 解析后的CanOpenMessage列表（可能包含多条消息）
     */
    fun parseFromData(data: ByteArray): List<CanOpenMessage> {
        val messages = mutableListOf<CanOpenMessage>()
        
        try {
            // 尝试作为文本解析
            val text = String(data, Charsets.US_ASCII)
            
            // 按回车符分割（可能包含多条消息）
            val lines = text.split('\r', '\n').filter { it.isNotBlank() }
            
            for (line in lines) {
                val message = parse(line)
                if (message != null) {
                    messages.add(message)
                }
            }
        } catch (e: Exception) {
            // 如果解析失败，返回空列表
        }
        
        return messages
    }
    
    /**
     * 构建CAN Open消息
     * 
     * @param canId CAN ID
     * @param dlc 数据长度（0-8）
     * @param data 数据字节数组
     * @return CanOpenMessage对象
     */
    fun build(canId: Int, dlc: Int, data: ByteArray): CanOpenMessage {
        return CanOpenMessage(canId, dlc, data)
    }
    
    /**
     * 构建CAN Open消息（从十六进制字符串）
     * 
     * @param canId CAN ID
     * @param dataHex 数据的十六进制字符串（例如："2B40600001000000"）
     * @return CanOpenMessage对象
     */
    fun buildFromHex(canId: Int, dataHex: String): CanOpenMessage {
        val dlc = dataHex.length / 2
        require(dlc in 0..8) { "数据长度必须在0-8字节之间" }
        
        val data = ByteArray(dlc) { index ->
            val hexByte = dataHex.substring(index * 2, (index + 1) * 2)
            hexByte.toInt(16).toByte()
        }
        
        return CanOpenMessage(canId, dlc, data)
    }
    
    /**
     * 验证协议字符串格式是否正确
     * 
     * @param protocolString 协议字符串
     * @return 是否为有效格式
     */
    fun isValidFormat(protocolString: String): Boolean {
        return parse(protocolString) != null
    }
}
