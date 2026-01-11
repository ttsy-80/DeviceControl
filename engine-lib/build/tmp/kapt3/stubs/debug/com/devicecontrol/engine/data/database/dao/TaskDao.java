package com.devicecontrol.engine.data.database.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\t\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0014\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\r0\fH\'J\u0018\u0010\u000e\u001a\u0004\u0018\u00010\u00052\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0018\u0010\u000f\u001a\u0004\u0018\u00010\u00102\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ \u0010\u0011\u001a\u0004\u0018\u00010\u00102\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u0012\u001a\u00020\u0013H\u00a7@\u00a2\u0006\u0002\u0010\u0014J \u0010\u0015\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\f2\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u0012\u001a\u00020\u0013H\'J\u0018\u0010\u0016\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\f2\u0006\u0010\b\u001a\u00020\tH\'J\u0016\u0010\u0017\u001a\u00020\t2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0018\u001a\u00020\t2\u0006\u0010\u0019\u001a\u00020\u0010H\u00a7@\u00a2\u0006\u0002\u0010\u001aJ\u0016\u0010\u001b\u001a\u00020\u00032\u0006\u0010\u0019\u001a\u00020\u0010H\u00a7@\u00a2\u0006\u0002\u0010\u001a\u00a8\u0006\u001c"}, d2 = {"Lcom/devicecontrol/engine/data/database/dao/TaskDao;", "", "deleteTask", "", "task", "Lcom/devicecontrol/engine/data/model/Task;", "(Lcom/devicecontrol/engine/data/model/Task;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteTaskExecutionByTaskId", "taskId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllTasks", "Lkotlinx/coroutines/flow/Flow;", "", "getTaskById", "getTaskExecutionByTaskId", "Lcom/devicecontrol/engine/data/model/TaskExecution;", "getTaskExecutionByTaskIdAndIndex", "gearRatioIndex", "", "(JILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getTaskExecutionByTaskIdAndIndexFlow", "getTaskExecutionByTaskIdFlow", "insertTask", "insertTaskExecution", "execution", "(Lcom/devicecontrol/engine/data/model/TaskExecution;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateTaskExecution", "engine-lib_debug"})
@androidx.room.Dao
public abstract interface TaskDao {
    
    @androidx.room.Query(value = "SELECT * FROM tasks ORDER BY id DESC")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.devicecontrol.engine.data.model.Task>> getAllTasks();
    
    @androidx.room.Query(value = "SELECT * FROM tasks WHERE id = :taskId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getTaskById(long taskId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.devicecontrol.engine.data.model.Task> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object insertTask(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.model.Task task, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Delete
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteTask(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.model.Task task, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM task_executions WHERE taskId = :taskId AND gearRatioIndex = :gearRatioIndex")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getTaskExecutionByTaskIdAndIndex(long taskId, int gearRatioIndex, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.devicecontrol.engine.data.model.TaskExecution> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM task_executions WHERE taskId = :taskId AND gearRatioIndex = :gearRatioIndex")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<com.devicecontrol.engine.data.model.TaskExecution> getTaskExecutionByTaskIdAndIndexFlow(long taskId, int gearRatioIndex);
    
    @androidx.room.Query(value = "SELECT * FROM task_executions WHERE taskId = :taskId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getTaskExecutionByTaskId(long taskId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.devicecontrol.engine.data.model.TaskExecution> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM task_executions WHERE taskId = :taskId")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<com.devicecontrol.engine.data.model.TaskExecution> getTaskExecutionByTaskIdFlow(long taskId);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object insertTaskExecution(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.model.TaskExecution execution, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Update
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object updateTaskExecution(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.model.TaskExecution execution, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM task_executions WHERE taskId = :taskId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteTaskExecutionByTaskId(long taskId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}