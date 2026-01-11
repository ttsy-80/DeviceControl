package com.devicecontrol.engine.data.model

import androidx.room.Entity
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

@Entity(tableName = "task_executions")
data class TaskExecution(
    @PrimaryKey(autoGenerate = true)
    val executionId: Long = 0,
    val taskId: Long,
    val currentGearRatioIndex: Int = 0,
    val status: TaskStatus = TaskStatus.STOPPED,
    val torque: Double = 0.0,
    val speed: Double = 0.0,
    val rotationDirection: RotationDirection = RotationDirection.FORWARD,
    val operationMode: OperationMode = OperationMode.JOG,
    val progress: Int = 0
)
