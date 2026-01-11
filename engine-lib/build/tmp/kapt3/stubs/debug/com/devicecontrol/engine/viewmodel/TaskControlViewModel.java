package com.devicecontrol.engine.viewmodel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000l\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\b\n\u0002\b\t\n\u0002\u0010\u0002\n\u0002\b\t\n\u0002\u0010\t\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010&\u001a\u00020\'J\u0006\u0010(\u001a\u00020\'J\u0006\u0010)\u001a\u00020\'J\u0006\u0010*\u001a\u00020\'J\u0006\u0010+\u001a\u00020\'J\u0006\u0010,\u001a\u00020\'J\u0018\u0010-\u001a\u00020\'2\u0006\u0010 \u001a\u00020\u00102\u0006\u0010.\u001a\u00020\u001dH\u0002J\u000e\u0010/\u001a\u00020\'2\u0006\u00100\u001a\u000201J\u0018\u00102\u001a\u00020\'2\u0006\u00100\u001a\u0002012\u0006\u00103\u001a\u00020\u001dH\u0002J\u0006\u00104\u001a\u00020\'J\u0006\u00105\u001a\u00020\'J\u0006\u00106\u001a\u00020\'J\u0006\u00107\u001a\u00020\'J\u001c\u00108\u001a\u00020\'2\u0012\u00109\u001a\u000e\u0012\u0004\u0012\u00020\u0012\u0012\u0004\u0012\u00020\u00120:H\u0002J\u0018\u0010;\u001a\u00020\'2\u0006\u0010 \u001a\u00020\u00102\u0006\u0010.\u001a\u00020\u001dH\u0002J\u0018\u0010<\u001a\u00020\'2\u0006\u0010 \u001a\u00020\u00102\u0006\u0010.\u001a\u00020\u001dH\u0002J\u0010\u0010=\u001a\u00020\'2\u0006\u0010>\u001a\u00020?H\u0002R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\f0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0011\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00120\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u000e0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\t0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0017\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\t0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0017R\u0019\u0010\u001a\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\f0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0017R\u000e\u0010\u001c\u001a\u00020\u001dX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0017R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010 \u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u0017R\u0019\u0010\"\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00120\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u0017R\u0017\u0010$\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u0017R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006@"}, d2 = {"Lcom/devicecontrol/engine/viewmodel/TaskControlViewModel;", "Landroidx/lifecycle/ViewModel;", "taskRepository", "Lcom/devicecontrol/engine/data/repository/TaskRepository;", "engineRepository", "Lcom/devicecontrol/engine/data/repository/EngineRepository;", "(Lcom/devicecontrol/engine/data/repository/TaskRepository;Lcom/devicecontrol/engine/data/repository/EngineRepository;)V", "_canGoNext", "Landroidx/lifecycle/MutableLiveData;", "", "_canGoPrevious", "_currentConfigItem", "Lcom/devicecontrol/engine/data/model/ConfigItem;", "_displayInfo", "", "_task", "Lcom/devicecontrol/engine/data/model/Task;", "_taskExecution", "Lcom/devicecontrol/engine/data/model/TaskExecution;", "_taskIndex", "canGoNext", "Landroidx/lifecycle/LiveData;", "getCanGoNext", "()Landroidx/lifecycle/LiveData;", "canGoPrevious", "getCanGoPrevious", "currentConfigItem", "getCurrentConfigItem", "currentGearRatioIndex", "", "displayInfo", "getDisplayInfo", "task", "getTask", "taskExecution", "getTaskExecution", "taskIndex", "getTaskIndex", "decreaseSpeed", "", "decreaseTorque", "goToNextTask", "goToPreviousTask", "increaseSpeed", "increaseTorque", "loadCurrentConfigItem", "index", "loadTask", "taskId", "", "loadTaskExecution", "gearRatioIndex", "stop", "toggleOperationMode", "toggleRotationDirection", "toggleStartPause", "updateExecution", "update", "Lkotlin/Function1;", "updateNavigationButtons", "updateTaskIndex", "updateTaskStatus", "status", "Lcom/devicecontrol/engine/data/model/TaskStatus;", "engine-lib_debug"})
public final class TaskControlViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final com.devicecontrol.engine.data.repository.TaskRepository taskRepository = null;
    @org.jetbrains.annotations.NotNull
    private final com.devicecontrol.engine.data.repository.EngineRepository engineRepository = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<com.devicecontrol.engine.data.model.Task> _task = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.LiveData<com.devicecontrol.engine.data.model.Task> task = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<com.devicecontrol.engine.data.model.TaskExecution> _taskExecution = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.LiveData<com.devicecontrol.engine.data.model.TaskExecution> taskExecution = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<com.devicecontrol.engine.data.model.ConfigItem> _currentConfigItem = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.LiveData<com.devicecontrol.engine.data.model.ConfigItem> currentConfigItem = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<java.lang.String> _displayInfo = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.LiveData<java.lang.String> displayInfo = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<java.lang.String> _taskIndex = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.LiveData<java.lang.String> taskIndex = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<java.lang.Boolean> _canGoPrevious = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.LiveData<java.lang.Boolean> canGoPrevious = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<java.lang.Boolean> _canGoNext = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.LiveData<java.lang.Boolean> canGoNext = null;
    private int currentGearRatioIndex = 0;
    
    public TaskControlViewModel(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.repository.TaskRepository taskRepository, @org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.repository.EngineRepository engineRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<com.devicecontrol.engine.data.model.Task> getTask() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<com.devicecontrol.engine.data.model.TaskExecution> getTaskExecution() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<com.devicecontrol.engine.data.model.ConfigItem> getCurrentConfigItem() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<java.lang.String> getDisplayInfo() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<java.lang.String> getTaskIndex() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<java.lang.Boolean> getCanGoPrevious() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<java.lang.Boolean> getCanGoNext() {
        return null;
    }
    
    public final void loadTask(long taskId) {
    }
    
    private final void loadTaskExecution(long taskId, int gearRatioIndex) {
    }
    
    private final void loadCurrentConfigItem(com.devicecontrol.engine.data.model.Task task, int index) {
    }
    
    private final void updateTaskIndex(com.devicecontrol.engine.data.model.Task task, int index) {
    }
    
    private final void updateNavigationButtons(com.devicecontrol.engine.data.model.Task task, int index) {
    }
    
    public final void goToPreviousTask() {
    }
    
    public final void goToNextTask() {
    }
    
    public final void toggleStartPause() {
    }
    
    public final void stop() {
    }
    
    public final void toggleRotationDirection() {
    }
    
    public final void toggleOperationMode() {
    }
    
    public final void increaseTorque() {
    }
    
    public final void decreaseTorque() {
    }
    
    public final void increaseSpeed() {
    }
    
    public final void decreaseSpeed() {
    }
    
    private final void updateTaskStatus(com.devicecontrol.engine.data.model.TaskStatus status) {
    }
    
    private final void updateExecution(kotlin.jvm.functions.Function1<? super com.devicecontrol.engine.data.model.TaskExecution, com.devicecontrol.engine.data.model.TaskExecution> update) {
    }
}