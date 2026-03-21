package com.devicecontrol.engine.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 任务运行过程记录实体（采样/检测结果等）。
 *
 * 绑定到具体任务 [taskId] 及子任务 [gearRatioIndex]，与 [TaskExecution] 子任务维度一致。
 * [recordNumber] 表示该子任务内的记录序号（通常从 1 递增）。
 * 对应 Room 表 `task_records`；[recordId] 唯一索引便于去重或同步。
 */
@Entity(
    tableName = "task_records",
    indices = [
        Index(value = ["taskId", "gearRatioIndex"]),
        Index(value = ["recordId"], unique = true)
    ]
)
data class TaskRecord(
    /** 主键，自增 */
    @PrimaryKey(autoGenerate = true)
    val recordId: Long = 0,
    /** 所属任务 ID */
    val taskId: Long,
    /** 子任务索引（与 [TaskExecution.gearRatioIndex] 一致） */
    val gearRatioIndex: Int,
    /** 该子任务内的记录序号，从 1 起自增 */
    val recordNumber: Int,
    /**
     * 位置数据（当前检测任务的位置；TODO 单位约定为 0.1 度，具体需与需求方确认）。
     */
    val position: Int,
    /** 当前叶片数（来自电机/设备回传） */
    val bladeNumber: Int,
    /** 记录创建时间戳（毫秒） */
    val createdAt: Long = System.currentTimeMillis()
)
