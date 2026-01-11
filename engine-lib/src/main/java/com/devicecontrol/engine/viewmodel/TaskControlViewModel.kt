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
    
    fun loadTask(taskId: Long) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId)
            _task.value = task
            
            if (task != null) {
                // 加载或创建任务执行记录
                var execution = taskRepository.getTaskExecutionByTaskId(taskId)
                if (execution == null) {
                    execution = TaskExecution(
                        taskId = taskId,
                        currentGearRatioIndex = 0
                    )
                    taskRepository.insertOrUpdateTaskExecution(execution)
                }
                _taskExecution.value = execution
                
                // 加载当前配置项
                loadCurrentConfigItem(task, execution.currentGearRatioIndex)
                
                // 更新任务索引显示
                updateTaskIndex(task, execution.currentGearRatioIndex)
                
                // 更新导航按钮状态
                updateNavigationButtons(task, execution.currentGearRatioIndex)
            }
        }
    }
    
    private fun loadCurrentConfigItem(task: Task, index: Int) {
        viewModelScope.launch {
            val model = engineRepository.getModelWithConfigItemsById(task.modelId)
            if (model != null && index < task.gearRatios.size) {
                val gearRatio = task.gearRatios[index]
                val configItem = model.configItems.find { it.gearRatio == gearRatio }
                _currentConfigItem.value = configItem
                
                // 更新显示信息
                val displayText = "${task.modelName} | ${configItem?.position ?: ""} | 点动"
                _displayInfo.value = displayText
            }
        }
    }
    
    private fun updateTaskIndex(task: Task, index: Int) {
        val total = task.gearRatios.size
        _taskIndex.value = "任务 ${index + 1}/$total"
    }
    
    private fun updateNavigationButtons(task: Task, index: Int) {
        _canGoPrevious.value = index > 0
        _canGoNext.value = index < task.gearRatios.size - 1
    }
    
    fun goToPreviousTask() {
        val execution = _taskExecution.value ?: return
        val task = _task.value ?: return
        
        if (execution.currentGearRatioIndex > 0) {
            val newIndex = execution.currentGearRatioIndex - 1
            updateTaskIndex(task, newIndex)
            updateNavigationButtons(task, newIndex)
            loadCurrentConfigItem(task, newIndex)
            
            viewModelScope.launch {
                val updatedExecution = execution.copy(currentGearRatioIndex = newIndex)
                taskRepository.updateTaskExecution(updatedExecution)
                _taskExecution.value = updatedExecution
            }
        }
    }
    
    fun goToNextTask() {
        val execution = _taskExecution.value ?: return
        val task = _task.value ?: return
        
        if (execution.currentGearRatioIndex < task.gearRatios.size - 1) {
            val newIndex = execution.currentGearRatioIndex + 1
            updateTaskIndex(task, newIndex)
            updateNavigationButtons(task, newIndex)
            loadCurrentConfigItem(task, newIndex)
            
            viewModelScope.launch {
                val updatedExecution = execution.copy(currentGearRatioIndex = newIndex)
                taskRepository.updateTaskExecution(updatedExecution)
                _taskExecution.value = updatedExecution
            }
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
            taskRepository.updateTaskExecution(updated)
        }
    }
}
