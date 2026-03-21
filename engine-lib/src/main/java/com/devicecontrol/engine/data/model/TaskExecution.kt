package com.devicecontrol.engine.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** 任务执行状态：停止 / 运行中 / 暂停 */
enum class TaskStatus {
    STOPPED,
    RUNNING,
    PAUSED
}

/** 旋转方向：正转 / 反转 */
enum class RotationDirection {
    FORWARD,
    REVERSE
}

/** 运行模式：点动 / 连续 */
enum class OperationMode {
    JOG,
    CONTINUOUS
}

/**
 * 任务执行状态实体（运行时状态，按子任务维度存储）。
 *
 * 与 [Task] 通过 [taskId] 关联；同一任务下不同变速比/子任务用 [gearRatioIndex] 区分。
 * 表内 `(taskId, gearRatioIndex)` 唯一，避免同一子任务重复行。
 * 对应 Room 表 `task_executions`。
 */
@Entity(
    tableName = "task_executions",
    indices = [Index(value = ["taskId", "gearRatioIndex"], unique = true)]
)
data class TaskExecution(
    /** 主键，自增 */
    @PrimaryKey(autoGenerate = true)
    val executionId: Long = 0,
    /** 所属任务 ID */
    val taskId: Long,
    /** 子任务索引，与 [TaskRecord.gearRatioIndex]、[Task.configItemIds] 顺序对应 */
    val gearRatioIndex: Int = 0,
    /** 当前执行状态 */
    val status: TaskStatus = TaskStatus.STOPPED,
    /** 扭矩 */
    val torque: Double = 0.0,
    /** 当前实际速度（可被速度 +/- 调整） */
    val speed: Double = 0.0,
    /** 旋转方向 */
    val rotationDirection: RotationDirection = RotationDirection.FORWARD,
    /** 点动或连续模式 */
    val operationMode: OperationMode = OperationMode.JOG,
    /** 进度（业务含义由上层约定） */
    val progress: Int = 0,
    /** 速度调整步长，默认 1.0（单位：分钟/圈，与业务一致） */
    val speedStep: Double = 1.0,
    /** 连续模式下的循环圈数，默认 1 */
    val continuousCycles: Int = 1,
    /** 点动间隔（秒），默认 1 */
    val jogInterval: Int = 1,
    /** 回溯速度，默认 1.0（单位：秒/圈，与业务一致） */
    val playbackSpeed: Double = 1.0
)
