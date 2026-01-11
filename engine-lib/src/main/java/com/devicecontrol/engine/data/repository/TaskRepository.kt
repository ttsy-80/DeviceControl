package com.devicecontrol.engine.data.repository

import com.devicecontrol.engine.data.database.dao.TaskDao
import com.devicecontrol.engine.data.model.Task
import com.devicecontrol.engine.data.model.TaskExecution
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
    
    fun getTaskExecutionByTaskIdFlow(taskId: Long): Flow<TaskExecution?> {
        return taskDao.getTaskExecutionByTaskIdFlow(taskId)
    }
    
    suspend fun insertOrUpdateTaskExecution(execution: TaskExecution): Long {
        val existing = taskDao.getTaskExecutionByTaskId(execution.taskId)
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
}
