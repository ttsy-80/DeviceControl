package com.devicecontrol.engine.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\t\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\tJ\u0012\u0010\n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\f0\u000bJ\u0018\u0010\r\u001a\u0004\u0018\u00010\b2\u0006\u0010\u000e\u001a\u00020\u000fH\u0086@\u00a2\u0006\u0002\u0010\u0010J\u0018\u0010\u0011\u001a\u0004\u0018\u00010\u00122\u0006\u0010\u000e\u001a\u00020\u000fH\u0086@\u00a2\u0006\u0002\u0010\u0010J \u0010\u0013\u001a\u0004\u0018\u00010\u00122\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0014\u001a\u00020\u0015H\u0086@\u00a2\u0006\u0002\u0010\u0016J\u001e\u0010\u0017\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00120\u000b2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0014\u001a\u00020\u0015J\u0016\u0010\u0018\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00120\u000b2\u0006\u0010\u000e\u001a\u00020\u000fJ\u0016\u0010\u0019\u001a\u00020\u000f2\u0006\u0010\u001a\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u001bJ\u0016\u0010\u001c\u001a\u00020\u000f2\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\tJ\u0016\u0010\u001d\u001a\u00020\u00062\u0006\u0010\u001a\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u001bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001e"}, d2 = {"Lcom/devicecontrol/engine/data/repository/TaskRepository;", "", "taskDao", "Lcom/devicecontrol/engine/data/database/dao/TaskDao;", "(Lcom/devicecontrol/engine/data/database/dao/TaskDao;)V", "deleteTask", "", "task", "Lcom/devicecontrol/engine/data/model/Task;", "(Lcom/devicecontrol/engine/data/model/Task;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllTasks", "Lkotlinx/coroutines/flow/Flow;", "", "getTaskById", "taskId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getTaskExecutionByTaskId", "Lcom/devicecontrol/engine/data/model/TaskExecution;", "getTaskExecutionByTaskIdAndIndex", "gearRatioIndex", "", "(JILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getTaskExecutionByTaskIdAndIndexFlow", "getTaskExecutionByTaskIdFlow", "insertOrUpdateTaskExecution", "execution", "(Lcom/devicecontrol/engine/data/model/TaskExecution;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertTask", "updateTaskExecution", "engine-lib_debug"})
public final class TaskRepository {
    @org.jetbrains.annotations.NotNull
    private final com.devicecontrol.engine.data.database.dao.TaskDao taskDao = null;
    
    public TaskRepository(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.database.dao.TaskDao taskDao) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.devicecontrol.engine.data.model.Task>> getAllTasks() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getTaskById(long taskId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.devicecontrol.engine.data.model.Task> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object insertTask(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.model.Task task, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object deleteTask(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.model.Task task, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getTaskExecutionByTaskId(long taskId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.devicecontrol.engine.data.model.TaskExecution> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getTaskExecutionByTaskIdAndIndex(long taskId, int gearRatioIndex, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.devicecontrol.engine.data.model.TaskExecution> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<com.devicecontrol.engine.data.model.TaskExecution> getTaskExecutionByTaskIdFlow(long taskId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<com.devicecontrol.engine.data.model.TaskExecution> getTaskExecutionByTaskIdAndIndexFlow(long taskId, int gearRatioIndex) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object insertOrUpdateTaskExecution(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.model.TaskExecution execution, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object updateTaskExecution(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.model.TaskExecution execution, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}