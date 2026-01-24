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
    val speed: Double = 0.0, // 当前实际速度（会被速度+/速度-修改）
    val rotationDirection: RotationDirection = RotationDirection.FORWARD,
    val operationMode: OperationMode = OperationMode.JOG,
    val progress: Int = 0,
    val speedStep: Double = 1.0, // 速度调整步长（配置项，默认1.0分钟/圈）
    val continuousCycles: Int = 1, // 连续循环圈数（配置项，默认1圈）
    val jogInterval: Int = 1, // 点动间隔（配置项，默认1秒）
    val playbackSpeed: Double = 1.0 // 回溯速度（配置项，默认1秒/圈）
)
