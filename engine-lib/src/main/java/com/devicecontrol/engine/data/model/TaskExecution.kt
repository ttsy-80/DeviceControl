package com.devicecontrol.engine.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TaskStatus {
    STOPPED,
    RUNNING,
    PAUSED
}

enum class RotationDirection {
    FORWARD,
    REVERSE
}

enum class OperationMode {
    JOG,
    CONTINUOUS
}

@Entity(
    tableName = "task_executions",
    indices = [Index(value = ["taskId", "gearRatioIndex"], unique = true)]
)
data class TaskExecution(
    @PrimaryKey(autoGenerate = true)
    val executionId: Long = 0,
    val taskId: Long,
    val gearRatioIndex: Int = 0, // 每个子任务的索引，用于区分同一个任务的不同子任务
    val status: TaskStatus = TaskStatus.STOPPED,
    val torque: Double = 0.0,
    val speed: Double = 0.0,
    val rotationDirection: RotationDirection = RotationDirection.FORWARD,
    val operationMode: OperationMode = OperationMode.JOG,
    val progress: Int = 0
)
