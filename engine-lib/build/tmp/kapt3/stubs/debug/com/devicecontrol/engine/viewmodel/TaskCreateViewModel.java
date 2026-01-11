package com.devicecontrol.engine.viewmodel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010\u001a\u001a\u00020\u001bJ<\u0010\u001c\u001a\u00020\u001b2\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u001e0\t2\u0012\u0010\u001f\u001a\u000e\u0012\u0004\u0012\u00020\u001e\u0012\u0004\u0012\u00020\u001b0 2\u0012\u0010!\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u001b0 J\b\u0010\"\u001a\u00020\u001bH\u0002J\u0010\u0010#\u001a\u00020\u001b2\b\u0010$\u001a\u0004\u0018\u00010\u000eR\u001a\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\f0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000e0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0014\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\f0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0013R\u001d\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\t0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0013R\u0019\u0010\u0018\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000e0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0013R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006%"}, d2 = {"Lcom/devicecontrol/engine/viewmodel/TaskCreateViewModel;", "Landroidx/lifecycle/ViewModel;", "engineRepository", "Lcom/devicecontrol/engine/data/repository/EngineRepository;", "taskRepository", "Lcom/devicecontrol/engine/data/repository/TaskRepository;", "(Lcom/devicecontrol/engine/data/repository/EngineRepository;Lcom/devicecontrol/engine/data/repository/TaskRepository;)V", "_availableGearRatios", "Landroidx/lifecycle/MutableLiveData;", "", "Lcom/devicecontrol/engine/data/model/ConfigItem;", "_errorMessage", "", "_modelsWithConfigItems", "Lcom/devicecontrol/engine/data/model/EngineModelWithConfigItems;", "_selectedModel", "availableGearRatios", "Landroidx/lifecycle/LiveData;", "getAvailableGearRatios", "()Landroidx/lifecycle/LiveData;", "errorMessage", "getErrorMessage", "modelsWithConfigItems", "getModelsWithConfigItems", "selectedModel", "getSelectedModel", "clearError", "", "createTask", "selectedConfigItemIds", "", "onSuccess", "Lkotlin/Function1;", "onError", "loadModels", "selectModel", "model", "engine-lib_debug"})
public final class TaskCreateViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final com.devicecontrol.engine.data.repository.EngineRepository engineRepository = null;
    @org.jetbrains.annotations.NotNull
    private final com.devicecontrol.engine.data.repository.TaskRepository taskRepository = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<java.util.List<com.devicecontrol.engine.data.model.EngineModelWithConfigItems>> _modelsWithConfigItems = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.LiveData<java.util.List<com.devicecontrol.engine.data.model.EngineModelWithConfigItems>> modelsWithConfigItems = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<com.devicecontrol.engine.data.model.EngineModelWithConfigItems> _selectedModel = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.LiveData<com.devicecontrol.engine.data.model.EngineModelWithConfigItems> selectedModel = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<java.util.List<com.devicecontrol.engine.data.model.ConfigItem>> _availableGearRatios = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.LiveData<java.util.List<com.devicecontrol.engine.data.model.ConfigItem>> availableGearRatios = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<java.lang.String> _errorMessage = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.LiveData<java.lang.String> errorMessage = null;
    
    public TaskCreateViewModel(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.repository.EngineRepository engineRepository, @org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.repository.TaskRepository taskRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<java.util.List<com.devicecontrol.engine.data.model.EngineModelWithConfigItems>> getModelsWithConfigItems() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<com.devicecontrol.engine.data.model.EngineModelWithConfigItems> getSelectedModel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<java.util.List<com.devicecontrol.engine.data.model.ConfigItem>> getAvailableGearRatios() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<java.lang.String> getErrorMessage() {
        return null;
    }
    
    private final void loadModels() {
    }
    
    public final void selectModel(@org.jetbrains.annotations.Nullable
    com.devicecontrol.engine.data.model.EngineModelWithConfigItems model) {
    }
    
    public final void createTask(@org.jetbrains.annotations.NotNull
    java.util.List<java.lang.Long> selectedConfigItemIds, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.Long, kotlin.Unit> onSuccess, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onError) {
    }
    
    public final void clearError() {
    }
}