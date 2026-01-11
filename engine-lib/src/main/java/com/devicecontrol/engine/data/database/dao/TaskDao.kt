package com.devicecontrol.engine.data.database.dao

import androidx.room.*
import com.devicecontrol.engine.data.model.Task
import com.devicecontrol.engine.data.model.TaskExecution
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long
    
    @Delete
    suspend fun deleteTask(task: Task)
    
    @Query("SELECT * FROM task_executions WHERE taskId = :taskId")
    suspend fun getTaskExecutionByTaskId(taskId: Long): TaskExecution?
    
    @Query("SELECT * FROM task_executions WHERE taskId = :taskId")
    fun getTaskExecutionByTaskIdFlow(taskId: Long): Flow<TaskExecution?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskExecution(execution: TaskExecution): Long
    
    @Update
    suspend fun updateTaskExecution(execution: TaskExecution)
    
    @Query("DELETE FROM task_executions WHERE taskId = :taskId")
    suspend fun deleteTaskExecutionByTaskId(taskId: Long)
}
