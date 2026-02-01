package com.devicecontrol.engine.data.database.dao

import androidx.room.*
import com.devicecontrol.engine.data.model.Task
import com.devicecontrol.engine.data.model.TaskExecution
import com.devicecontrol.engine.data.model.TaskRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?
    
    @Query("SELECT * FROM tasks WHERE modelId = :modelId AND id != :excludeTaskId ORDER BY id DESC")
    suspend fun getTasksByModelId(modelId: Long, excludeTaskId: Long = -1): List<Task>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long
    
    @Delete
    suspend fun deleteTask(task: Task)
    
    @Query("SELECT * FROM task_executions WHERE taskId = :taskId AND gearRatioIndex = :gearRatioIndex")
    suspend fun getTaskExecutionByTaskIdAndIndex(taskId: Long, gearRatioIndex: Int): TaskExecution?
    
    @Query("SELECT * FROM task_executions WHERE taskId = :taskId AND gearRatioIndex = :gearRatioIndex")
    fun getTaskExecutionByTaskIdAndIndexFlow(taskId: Long, gearRatioIndex: Int): Flow<TaskExecution?>
    
    @Query("SELECT * FROM task_executions WHERE taskId = :taskId")
    suspend fun getTaskExecutionByTaskId(taskId: Long): TaskExecution?
    
    @Query("SELECT * FROM task_executions WHERE taskId = :taskId")
    fun getTaskExecutionByTaskIdFlow(taskId: Long): Flow<TaskExecution?>
    
    @Query("SELECT * FROM task_executions WHERE taskId IN (SELECT id FROM tasks WHERE modelId = :modelId AND id != :excludeTaskId) ORDER BY taskId DESC, gearRatioIndex ASC LIMIT 1")
    suspend fun getLatestTaskExecutionByModelId(modelId: Long, excludeTaskId: Long = -1): TaskExecution?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskExecution(execution: TaskExecution): Long
    
    @Update
    suspend fun updateTaskExecution(execution: TaskExecution)
    
    @Query("DELETE FROM task_executions WHERE taskId = :taskId")
    suspend fun deleteTaskExecutionByTaskId(taskId: Long)
    
    // TaskRecord相关方法
    @Query("SELECT * FROM task_records WHERE taskId = :taskId AND gearRatioIndex = :gearRatioIndex ORDER BY recordNumber ASC")
    suspend fun getTaskRecordsByTaskIdAndIndex(taskId: Long, gearRatioIndex: Int): List<TaskRecord>
    
    @Query("SELECT * FROM task_records WHERE taskId = :taskId AND gearRatioIndex = :gearRatioIndex ORDER BY recordNumber ASC")
    fun getTaskRecordsByTaskIdAndIndexFlow(taskId: Long, gearRatioIndex: Int): Flow<List<TaskRecord>>
    
    @Query("SELECT * FROM task_records WHERE recordId = :recordId")
    suspend fun getTaskRecordById(recordId: Long): TaskRecord?
    
    @Query("SELECT MAX(recordNumber) FROM task_records WHERE taskId = :taskId AND gearRatioIndex = :gearRatioIndex")
    suspend fun getMaxRecordNumber(taskId: Long, gearRatioIndex: Int): Int?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskRecord(record: TaskRecord): Long
    
    @Delete
    suspend fun deleteTaskRecord(record: TaskRecord)
    
    @Query("DELETE FROM task_records WHERE taskId = :taskId AND gearRatioIndex = :gearRatioIndex")
    suspend fun deleteTaskRecordsByTaskIdAndIndex(taskId: Long, gearRatioIndex: Int)
}
