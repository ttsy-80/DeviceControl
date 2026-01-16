package com.devicecontrol.engine.data.repository

import com.devicecontrol.engine.data.database.dao.TaskDao
import com.devicecontrol.engine.data.model.Task
import com.devicecontrol.engine.data.model.TaskExecution
import com.devicecontrol.engine.data.model.TaskRecord
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    
    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
    }
    
    suspend fun getTaskById(taskId: Long): Task? {
        return taskDao.getTaskById(taskId)
    }
    
    suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task)
    }
    
    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
        // 删除关联的执行记录
        taskDao.deleteTaskExecutionByTaskId(task.id)
    }
    
    suspend fun getTaskExecutionByTaskId(taskId: Long): TaskExecution? {
        return taskDao.getTaskExecutionByTaskId(taskId)
    }
    
    suspend fun getTaskExecutionByTaskIdAndIndex(taskId: Long, gearRatioIndex: Int): TaskExecution? {
        return taskDao.getTaskExecutionByTaskIdAndIndex(taskId, gearRatioIndex)
    }
    
    fun getTaskExecutionByTaskIdFlow(taskId: Long): Flow<TaskExecution?> {
        return taskDao.getTaskExecutionByTaskIdFlow(taskId)
    }
    
    fun getTaskExecutionByTaskIdAndIndexFlow(taskId: Long, gearRatioIndex: Int): Flow<TaskExecution?> {
        return taskDao.getTaskExecutionByTaskIdAndIndexFlow(taskId, gearRatioIndex)
    }
    
    suspend fun insertOrUpdateTaskExecution(execution: TaskExecution): Long {
        val existing = taskDao.getTaskExecutionByTaskIdAndIndex(execution.taskId, execution.gearRatioIndex)
        return if (existing != null) {
            taskDao.updateTaskExecution(execution)
            existing.executionId
        } else {
            taskDao.insertTaskExecution(execution)
        }
    }
    
    suspend fun updateTaskExecution(execution: TaskExecution) {
        taskDao.updateTaskExecution(execution)
    }
    
    // TaskRecord相关方法
    suspend fun getTaskRecordsByTaskIdAndIndex(taskId: Long, gearRatioIndex: Int): List<TaskRecord> {
        return taskDao.getTaskRecordsByTaskIdAndIndex(taskId, gearRatioIndex)
    }
    
    fun getTaskRecordsByTaskIdAndIndexFlow(taskId: Long, gearRatioIndex: Int): Flow<List<TaskRecord>> {
        return taskDao.getTaskRecordsByTaskIdAndIndexFlow(taskId, gearRatioIndex)
    }
    
    suspend fun getTaskRecordById(recordId: Long): TaskRecord? {
        return taskDao.getTaskRecordById(recordId)
    }
    
    suspend fun insertTaskRecord(record: TaskRecord): Long {
        // 获取当前最大记录号
        val maxRecordNumber = taskDao.getMaxRecordNumber(record.taskId, record.gearRatioIndex) ?: 0
        val newRecord = record.copy(recordNumber = maxRecordNumber + 1)
        return taskDao.insertTaskRecord(newRecord)
    }
    
    suspend fun deleteTaskRecord(record: TaskRecord) {
        taskDao.deleteTaskRecord(record)
    }
    
    suspend fun deleteTaskRecordsByTaskIdAndIndex(taskId: Long, gearRatioIndex: Int) {
        taskDao.deleteTaskRecordsByTaskIdAndIndex(taskId, gearRatioIndex)
    }
}
