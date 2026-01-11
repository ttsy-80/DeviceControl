package com.devicecontrol.engine.ui.task;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u001c\u001a\u00020\u001d2\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0014J\b\u0010 \u001a\u00020\u001dH\u0002J\b\u0010!\u001a\u00020\u001dH\u0002J\b\u0010\"\u001a\u00020\u001dH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0005\u001a\u00020\u00068BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\t\u0010\n\u001a\u0004\b\u0007\u0010\bR\u001b\u0010\u000b\u001a\u00020\f8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000f\u0010\n\u001a\u0004\b\r\u0010\u000eR\u000e\u0010\u0010\u001a\u00020\u0011X\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0012\u001a\u00020\u00138BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0016\u0010\n\u001a\u0004\b\u0014\u0010\u0015R\u001b\u0010\u0017\u001a\u00020\u00188BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001b\u0010\n\u001a\u0004\b\u0019\u0010\u001a\u00a8\u0006#"}, d2 = {"Lcom/devicecontrol/engine/ui/task/TaskCreateActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "Lcom/devicecontrol/engine/databinding/ActivityTaskCreateBinding;", "database", "Lcom/devicecontrol/engine/data/database/AppDatabase;", "getDatabase", "()Lcom/devicecontrol/engine/data/database/AppDatabase;", "database$delegate", "Lkotlin/Lazy;", "engineRepository", "Lcom/devicecontrol/engine/data/repository/EngineRepository;", "getEngineRepository", "()Lcom/devicecontrol/engine/data/repository/EngineRepository;", "engineRepository$delegate", "gearRatioAdapter", "Lcom/devicecontrol/engine/ui/task/GearRatioAdapter;", "taskRepository", "Lcom/devicecontrol/engine/data/repository/TaskRepository;", "getTaskRepository", "()Lcom/devicecontrol/engine/data/repository/TaskRepository;", "taskRepository$delegate", "viewModel", "Lcom/devicecontrol/engine/viewmodel/TaskCreateViewModel;", "getViewModel", "()Lcom/devicecontrol/engine/viewmodel/TaskCreateViewModel;", "viewModel$delegate", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "setupListeners", "setupObservers", "setupRecyclerView", "engine-lib_debug"})
public final class TaskCreateActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.devicecontrol.engine.databinding.ActivityTaskCreateBinding binding;
    private com.devicecontrol.engine.ui.task.GearRatioAdapter gearRatioAdapter;
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy database$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy engineRepository$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy taskRepository$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy viewModel$delegate = null;
    
    public TaskCreateActivity() {
        super();
    }
    
    private final com.devicecontrol.engine.data.database.AppDatabase getDatabase() {
        return null;
    }
    
    private final com.devicecontrol.engine.data.repository.EngineRepository getEngineRepository() {
        return null;
    }
    
    private final com.devicecontrol.engine.data.repository.TaskRepository getTaskRepository() {
        return null;
    }
    
    private final com.devicecontrol.engine.viewmodel.TaskCreateViewModel getViewModel() {
        return null;
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    private final void setupRecyclerView() {
    }
    
    private final void setupObservers() {
    }
    
    private final void setupListeners() {
    }
}