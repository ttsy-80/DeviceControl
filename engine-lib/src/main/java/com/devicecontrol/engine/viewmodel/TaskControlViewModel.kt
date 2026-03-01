package com.devicecontrol.engine.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicecontrol.engine.data.model.ConfigItem
import com.devicecontrol.engine.data.model.OperationMode
import com.devicecontrol.engine.data.model.RotationDirection
import com.devicecontrol.engine.data.model.Task
import com.devicecontrol.engine.data.model.TaskExecution
import com.devicecontrol.engine.data.model.TaskRecord
import com.devicecontrol.engine.data.model.TaskStatus
import com.devicecontrol.engine.data.repository.EngineRepository
import com.devicecontrol.engine.data.repository.TaskRepository
import com.devicecontrol.engine.log.EngineLog
import kotlinx.coroutines.launch

class TaskControlViewModel(
    private val taskRepository: TaskRepository,
    private val engineRepository: EngineRepository
) : ViewModel() {

    companion object {
        private const val TAG = "TaskControlVM"
    }

    private val _task = MutableLiveData<Task?>()
    val task: LiveData<Task?> = _task
    
    private val _taskExecution = MutableLiveData<TaskExecution?>()
    val taskExecution: LiveData<TaskExecution?> = _taskExecution
    
    private val _currentConfigItem = MutableLiveData<ConfigItem?>()
    val currentConfigItem: LiveData<ConfigItem?> = _currentConfigItem
    
    private val _displayInfo = MutableLiveData<String>()
    val displayInfo: LiveData<String> = _displayInfo
    
    private val _taskIndex = MutableLiveData<String>()
    val taskIndex: LiveData<String> = _taskIndex
    
    private val _taskRecords = MutableLiveData<List<TaskRecord>>(emptyList())
    val taskRecords: LiveData<List<TaskRecord>> = _taskRecords
    
    private var currentGearRatioIndex: Int = 0
    
    fun loadTask(taskId: Long) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId)
            EngineLog.d(TAG, "loadTask: id=$taskId, configItemIds=${task?.configItemIds?.size}")
            _task.value = task
            
            if (task != null) {
                // 第一次进入页面时，重置所有子任务的状态为STOPPED
                resetAllTaskExecutionsStatus(taskId, task.configItemIds.size)
                
                // 从第一个子任务开始
                currentGearRatioIndex = 0
                loadTaskExecution(taskId, currentGearRatioIndex)
            }
        }
    }
    
    /**
     * 重置所有子任务的状态为STOPPED（仅在第一次进入页面时调用）
     */
    private fun resetAllTaskExecutionsStatus(taskId: Long, configItemCount: Int) {
        viewModelScope.launch {
            for (index in 0 until configItemCount) {
                val execution = taskRepository.getTaskExecutionByTaskIdAndIndex(taskId, index)
                if (execution != null && execution.status != TaskStatus.STOPPED) {
                    val resetExecution = execution.copy(status = TaskStatus.STOPPED)
                    taskRepository.insertOrUpdateTaskExecution(resetExecution)
                }
            }
        }
    }
    
    private fun loadTaskExecution(taskId: Long, configItemIndex: Int) {
        viewModelScope.launch {
            val task = _task.value ?: return@launch
            
            // 加载或创建该子任务的执行记录
            var execution = taskRepository.getTaskExecutionByTaskIdAndIndex(taskId, configItemIndex)
            if (execution == null) {
                // 检查是否有上一个任务的执行记录，用于复制配置
                val previousExecution = if (configItemIndex > 0) {
                    taskRepository.getTaskExecutionByTaskIdAndIndex(taskId, configItemIndex - 1)
                } else {
                    null
                }
                
                execution = TaskExecution(
                    taskId = taskId,
                    gearRatioIndex = configItemIndex,
                    speed = previousExecution?.speedStep ?: 1.0, // 默认速度使用speedStep的值
                    speedStep = previousExecution?.speedStep ?: 1.0, // 复制配置或使用默认值
                    continuousCycles = previousExecution?.continuousCycles ?: 1,
                    jogInterval = previousExecution?.jogInterval ?: 1,
                    playbackSpeed = previousExecution?.playbackSpeed ?: 1.0 // 复制回溯速度或使用默认值
                )
                taskRepository.insertOrUpdateTaskExecution(execution)
            }
            _taskExecution.value = execution
            
            // 加载当前配置项
            loadCurrentConfigItem(task, configItemIndex)
            
            // 更新任务索引显示
            updateTaskIndex(task, configItemIndex)
            
            // 加载记录列表
            loadTaskRecords(taskId, configItemIndex)
        }
    }
    
    private fun loadTaskRecords(taskId: Long, gearRatioIndex: Int) {
        viewModelScope.launch {
            val records = taskRepository.getTaskRecordsByTaskIdAndIndex(taskId, gearRatioIndex)
            _taskRecords.value = records
        }
    }
    
    private fun loadCurrentConfigItem(task: Task, index: Int) {
        viewModelScope.launch {
            val model = engineRepository.getModelWithConfigItemsById(task.modelId)
            if (model != null && index < task.configItemIds.size) {
                val configItemId = task.configItemIds[index]
                val configItem = model.configItems.find { it.id == configItemId }
                _currentConfigItem.value = configItem
                
                // 获取当前执行状态以显示操作模式和旋转方向
                val execution = _taskExecution.value
                val operationModeText = when (execution?.operationMode) {
                    OperationMode.JOG -> "点动"
                    OperationMode.CONTINUOUS -> "连续"
                    null -> "点动"
                }
                val rotationDirectionText = when (execution?.rotationDirection) {
                    RotationDirection.FORWARD -> "正转"
                    RotationDirection.REVERSE -> "反转"
                    null -> "正转"
                }
                
                // 更新显示信息，格式：型号 位置 操作模式
                val displayText = "${task.modelName} ${configItem?.position ?: ""} $operationModeText"
                _displayInfo.value = displayText
            }
        }
    }
    
    private fun updateTaskIndex(task: Task, index: Int) {
        val total = task.configItemIds.size
        _taskIndex.value = "任务: ${index + 1}/$total"
    }
    
    data class TaskItem(
        val index: Int,
        val displayName: String
    )
    
    fun getTaskItems(): List<TaskItem> {
        val task = _task.value ?: return emptyList()
        return task.configItemIds.mapIndexed { index, _ ->
            TaskItem(
                index = index,
                displayName = "任务 ${index + 1}/${task.configItemIds.size}"
            )
        }
    }
    
    fun getCurrentTaskIndex(): Int = currentGearRatioIndex
    
    fun switchToTask(index: Int) {
        val task = _task.value ?: return
        
        if (index >= 0 && index < task.configItemIds.size) {
            // 保存当前任务的状态
            val currentExecution = _taskExecution.value
            
            viewModelScope.launch {
                // 保存当前任务的状态
                if (currentExecution != null) {
                    taskRepository.insertOrUpdateTaskExecution(currentExecution)
                }
                
                // 切换到目标任务，不改变目标任务的状态或配置
                currentGearRatioIndex = index
                loadTaskExecution(task.id, currentGearRatioIndex)
            }
        }
    }
    
    fun start() {
        updateExecution { it.copy(status = TaskStatus.RUNNING) }
        // 更新header显示
        val task = _task.value ?: return
        loadCurrentConfigItem(task, currentGearRatioIndex)
    }
    
    fun pause() {
        updateExecution { it.copy(status = TaskStatus.PAUSED) }
        // 更新header显示
        val task = _task.value ?: return
        loadCurrentConfigItem(task, currentGearRatioIndex)
    }
    
    fun setForward() {
        updateExecution { it.copy(rotationDirection = RotationDirection.FORWARD) }
        // 更新header显示
        val task = _task.value ?: return
        loadCurrentConfigItem(task, currentGearRatioIndex)
    }
    
    fun setReverse() {
        updateExecution { it.copy(rotationDirection = RotationDirection.REVERSE) }
        // 更新header显示
        val task = _task.value ?: return
        loadCurrentConfigItem(task, currentGearRatioIndex)
    }
    
    fun setJog() {
        updateExecution { it.copy(operationMode = OperationMode.JOG) }
        // 更新header显示
        val task = _task.value ?: return
        loadCurrentConfigItem(task, currentGearRatioIndex)
    }
    
    fun setContinuous() {
        updateExecution { it.copy(operationMode = OperationMode.CONTINUOUS) }
        // 更新header显示
        val task = _task.value ?: return
        loadCurrentConfigItem(task, currentGearRatioIndex)
    }
    
    fun increaseSpeed() {
        val execution = _taskExecution.value ?: return
        // 使用配置的步长调整速度
        val newSpeed = execution.speed + execution.speedStep
        updateExecution { it.copy(speed = newSpeed) }
    }
    
    fun decreaseSpeed() {
        val execution = _taskExecution.value ?: return
        // 使用配置的步长调整速度，最小为0
        if (execution.speed >= execution.speedStep) {
            val newSpeed = execution.speed - execution.speedStep
            updateExecution { it.copy(speed = newSpeed) }
        } else {
            updateExecution { it.copy(speed = 0.0) }
        }
    }
    
    fun updateSettings(speedStep: Double, continuousCycles: Int, jogInterval: Int, playbackSpeed: Double) {
        updateExecution { 
            it.copy(
                speedStep = speedStep,
                continuousCycles = continuousCycles,
                jogInterval = jogInterval,
                playbackSpeed = playbackSpeed
            )
        }
    }
    
    // 获取当前配置项，用于后续对接指令
    fun getCurrentSettings(): Triple<Double, Int, Int>? {
        val execution = _taskExecution.value ?: return null
        return Triple(execution.speedStep, execution.continuousCycles, execution.jogInterval)
    }
    
    fun takePhoto() {
        // 拍照功能：无对应功能，暂时不实现
    }
    
    fun addRecord(position: Int, bladeNumber: Int) {
        val task = _task.value ?: return
        viewModelScope.launch {
            val record = TaskRecord(
                taskId = task.id,
                gearRatioIndex = currentGearRatioIndex,
                recordNumber = 0, // 会在Repository中自动设置
                position = position,
                bladeNumber = bladeNumber
            )
            taskRepository.insertTaskRecord(record)
            loadTaskRecords(task.id, currentGearRatioIndex)
        }
    }
    
    fun playbackRecord(record: TaskRecord) {
        // 回放功能：发送指令给电机
        // TODO: 实现电机通信逻辑
        viewModelScope.launch {
            val execution = _taskExecution.value
            val playbackSpeed = execution?.playbackSpeed ?: 1.0
            // 这里应该发送指令给电机，包含回溯速度参数
            // 暂时只记录日志
            android.util.Log.d("TaskControlViewModel", "Playback record: $record, playbackSpeed: $playbackSpeed 秒/圈")
        }
    }
    
    private fun updateExecution(update: (TaskExecution) -> TaskExecution) {
        val execution = _taskExecution.value ?: return
        val updated = update(execution)
        _taskExecution.value = updated
        
        viewModelScope.launch {
            taskRepository.insertOrUpdateTaskExecution(updated)
        }
    }
}

