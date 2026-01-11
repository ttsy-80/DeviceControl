package com.devicecontrol.engine.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devicecontrol.engine.data.model.*
import com.devicecontrol.engine.data.repository.EngineRepository
import com.devicecontrol.engine.data.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskControlViewModel(
    private val taskRepository: TaskRepository,
    private val engineRepository: EngineRepository
) : ViewModel() {
    
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
    
    private val _canGoPrevious = MutableLiveData<Boolean>(false)
    val canGoPrevious: LiveData<Boolean> = _canGoPrevious
    
    private val _canGoNext = MutableLiveData<Boolean>(false)
    val canGoNext: LiveData<Boolean> = _canGoNext
    
    private var currentGearRatioIndex: Int = 0
    
    fun loadTask(taskId: Long) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId)
            android.util.Log.d("TaskControlViewModel", "Task loaded: id=$taskId, task=$task, configItemIds=${task?.configItemIds}, size=${task?.configItemIds?.size}")
            _task.value = task
            
            if (task != null) {
                // 从第一个子任务开始
                currentGearRatioIndex = 0
                loadTaskExecution(taskId, currentGearRatioIndex)
            }
        }
    }
    
    private fun loadTaskExecution(taskId: Long, configItemIndex: Int) {
        viewModelScope.launch {
            val task = _task.value ?: return@launch
            
            // 加载或创建该子任务的执行记录
            var execution = taskRepository.getTaskExecutionByTaskIdAndIndex(taskId, configItemIndex)
            if (execution == null) {
                execution = TaskExecution(
                    taskId = taskId,
                    gearRatioIndex = configItemIndex
                )
                taskRepository.insertOrUpdateTaskExecution(execution)
            }
            _taskExecution.value = execution
            
            // 加载当前配置项
            loadCurrentConfigItem(task, configItemIndex)
            
            // 更新任务索引显示
            updateTaskIndex(task, configItemIndex)
            
            // 更新导航按钮状态
            updateNavigationButtons(task, configItemIndex)
        }
    }
    
    private fun loadCurrentConfigItem(task: Task, index: Int) {
        viewModelScope.launch {
            val model = engineRepository.getModelWithConfigItemsById(task.modelId)
            if (model != null && index < task.configItemIds.size) {
                val configItemId = task.configItemIds[index]
                val configItem = model.configItems.find { it.id == configItemId }
                _currentConfigItem.value = configItem
                
                // 获取当前执行状态以显示操作模式
                val execution = _taskExecution.value
                val operationModeText = when (execution?.operationMode) {
                    OperationMode.JOG -> "点动"
                    OperationMode.CONTINUOUS -> "连续"
                    null -> "点动"
                }
                
                // 更新显示信息，包含操作模式
                val displayText = "${task.modelName} | ${configItem?.position ?: ""} | $operationModeText"
                _displayInfo.value = displayText
            }
        }
    }
    
    private fun updateTaskIndex(task: Task, index: Int) {
        val total = task.configItemIds.size
        _taskIndex.value = "任务 ${index + 1}/$total"
    }
    
    private fun updateNavigationButtons(task: Task, index: Int) {
        _canGoPrevious.value = index > 0
        _canGoNext.value = index < task.configItemIds.size - 1
    }
    
    fun goToPreviousTask() {
        val task = _task.value ?: return
        
        if (currentGearRatioIndex > 0) {
            currentGearRatioIndex--
            loadTaskExecution(task.id, currentGearRatioIndex)
        }
    }
    
    fun goToNextTask() {
        val task = _task.value ?: return
        
        if (currentGearRatioIndex < task.configItemIds.size - 1) {
            currentGearRatioIndex++
            loadTaskExecution(task.id, currentGearRatioIndex)
        }
    }
    
    fun toggleStartPause() {
        val execution = _taskExecution.value ?: return
        val newStatus = when (execution.status) {
            TaskStatus.STOPPED, TaskStatus.PAUSED -> TaskStatus.RUNNING
            TaskStatus.RUNNING -> TaskStatus.PAUSED
        }
        updateTaskStatus(newStatus)
    }
    
    fun stop() {
        updateTaskStatus(TaskStatus.STOPPED)
    }
    
    fun toggleRotationDirection() {
        val execution = _taskExecution.value ?: return
        val newDirection = when (execution.rotationDirection) {
            RotationDirection.FORWARD -> RotationDirection.REVERSE
            RotationDirection.REVERSE -> RotationDirection.FORWARD
        }
        updateExecution { it.copy(rotationDirection = newDirection) }
    }
    
    fun toggleOperationMode() {
        val execution = _taskExecution.value ?: return
        val newMode = when (execution.operationMode) {
            OperationMode.JOG -> OperationMode.CONTINUOUS
            OperationMode.CONTINUOUS -> OperationMode.JOG
        }
        updateExecution { it.copy(operationMode = newMode) }
        // 更新header显示
        val task = _task.value ?: return
        loadCurrentConfigItem(task, currentGearRatioIndex)
    }
    
    fun increaseTorque() {
        updateExecution { it.copy(torque = it.torque + 1.0) }
    }
    
    fun decreaseTorque() {
        val execution = _taskExecution.value ?: return
        if (execution.torque > 0) {
            updateExecution { it.copy(torque = execution.torque - 1.0) }
        }
    }
    
    fun increaseSpeed() {
        updateExecution { it.copy(speed = it.speed + 1.0) }
    }
    
    fun decreaseSpeed() {
        val execution = _taskExecution.value ?: return
        if (execution.speed > 0) {
            updateExecution { it.copy(speed = execution.speed - 1.0) }
        }
    }
    
    private fun updateTaskStatus(status: TaskStatus) {
        updateExecution { it.copy(status = status) }
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
