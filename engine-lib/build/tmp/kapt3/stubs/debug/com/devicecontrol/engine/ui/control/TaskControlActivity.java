package com.devicecontrol.engine.ui.control;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u001c\u001a\u00020\u001d2\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0014J\b\u0010 \u001a\u00020\u001dH\u0002J\b\u0010!\u001a\u00020\u001dH\u0002J\u0010\u0010\"\u001a\u00020\u001d2\u0006\u0010#\u001a\u00020$H\u0002J\u0018\u0010%\u001a\u00020\u001d2\u0006\u0010&\u001a\u00020\'2\u0006\u0010(\u001a\u00020\'H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0005\u001a\u00020\u00068BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\t\u0010\n\u001a\u0004\b\u0007\u0010\bR\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\r\u001a\u00020\u000e8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0011\u0010\n\u001a\u0004\b\u000f\u0010\u0010R\u001b\u0010\u0012\u001a\u00020\u00138BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0016\u0010\n\u001a\u0004\b\u0014\u0010\u0015R\u001b\u0010\u0017\u001a\u00020\u00188BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001b\u0010\n\u001a\u0004\b\u0019\u0010\u001a\u00a8\u0006)"}, d2 = {"Lcom/devicecontrol/engine/ui/control/TaskControlActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "Lcom/devicecontrol/engine/databinding/ActivityTaskControlBinding;", "database", "Lcom/devicecontrol/engine/data/database/AppDatabase;", "getDatabase", "()Lcom/devicecontrol/engine/data/database/AppDatabase;", "database$delegate", "Lkotlin/Lazy;", "decimalFormat", "Ljava/text/DecimalFormat;", "engineRepository", "Lcom/devicecontrol/engine/data/repository/EngineRepository;", "getEngineRepository", "()Lcom/devicecontrol/engine/data/repository/EngineRepository;", "engineRepository$delegate", "taskRepository", "Lcom/devicecontrol/engine/data/repository/TaskRepository;", "getTaskRepository", "()Lcom/devicecontrol/engine/data/repository/TaskRepository;", "taskRepository$delegate", "viewModel", "Lcom/devicecontrol/engine/viewmodel/TaskControlViewModel;", "getViewModel", "()Lcom/devicecontrol/engine/viewmodel/TaskControlViewModel;", "viewModel$delegate", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "setupListeners", "setupObservers", "updateButtonStates", "execution", "Lcom/devicecontrol/engine/data/model/TaskExecution;", "updateTorqueSpeedDisplay", "torque", "", "speed", "engine-lib_debug"})
public final class TaskControlActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.devicecontrol.engine.databinding.ActivityTaskControlBinding binding;
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy database$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy taskRepository$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy engineRepository$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy viewModel$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final java.text.DecimalFormat decimalFormat = null;
    
    public TaskControlActivity() {
        super();
    }
    
    private final com.devicecontrol.engine.data.database.AppDatabase getDatabase() {
        return null;
    }
    
    private final com.devicecontrol.engine.data.repository.TaskRepository getTaskRepository() {
        return null;
    }
    
    private final com.devicecontrol.engine.data.repository.EngineRepository getEngineRepository() {
        return null;
    }
    
    private final com.devicecontrol.engine.viewmodel.TaskControlViewModel getViewModel() {
        return null;
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    private final void setupObservers() {
    }
    
    private final void updateTorqueSpeedDisplay(double torque, double speed) {
    }
    
    private final void updateButtonStates(com.devicecontrol.engine.data.model.TaskExecution execution) {
    }
    
    private final void setupListeners() {
    }
}