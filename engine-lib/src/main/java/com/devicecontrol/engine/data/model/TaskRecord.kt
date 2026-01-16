package com.devicecontrol.engine.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 任务记录实体
 * 记录绑定到特定的子任务（通过taskId和gearRatioIndex）
 */
@Entity(
    tableName = "task_records",
    indices = [
        Index(value = ["taskId", "gearRatioIndex"]),
        Index(value = ["recordId"], unique = true)
    ]
)
data class TaskRecord(
    @PrimaryKey(autoGenerate = true)
    val recordId: Long = 0,
    val taskId: Long,
    val gearRatioIndex: Int, // 子任务索引
    val recordNumber: Int, // 记录序号（在该子任务中的序号，从1开始自增）
    val position: Int, // 位置数据（当前检测任务的位置数据）
    val bladeNumber: Int, // 当前叶片数（电机回传的数据）
    val createdAt: Long = System.currentTimeMillis() // 创建时间戳
)

