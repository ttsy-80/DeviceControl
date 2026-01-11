package com.devicecontrol.engine.ui.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000b\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0019\u001a\u00020\u001a2\b\u0010\u001b\u001a\u0004\u0018\u00010\u001cH\u0014J\b\u0010\u001d\u001a\u00020\u001aH\u0002J\b\u0010\u001e\u001a\u00020\u001aH\u0002J\b\u0010\u001f\u001a\u00020\u001aH\u0002J\u0010\u0010 \u001a\u00020\u001a2\u0006\u0010!\u001a\u00020\"H\u0002J\u0010\u0010#\u001a\u00020\u001a2\u0006\u0010$\u001a\u00020%H\u0002J\u0010\u0010&\u001a\u00020\u001a2\u0006\u0010\'\u001a\u00020(H\u0002J\u0010\u0010)\u001a\u00020\u001a2\u0006\u0010!\u001a\u00020\"H\u0002J\b\u0010*\u001a\u00020\u001aH\u0002J\u0012\u0010+\u001a\u00020\u001a2\b\u0010,\u001a\u0004\u0018\u00010-H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0007\u001a\u00020\b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000b\u0010\f\u001a\u0004\b\t\u0010\nR\u000e\u0010\r\u001a\u00020\u000eX\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u000f\u001a\u00020\u00108BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0013\u0010\f\u001a\u0004\b\u0011\u0010\u0012R\u001b\u0010\u0014\u001a\u00020\u00158BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0018\u0010\f\u001a\u0004\b\u0016\u0010\u0017\u00a8\u0006."}, d2 = {"Lcom/devicecontrol/engine/ui/model/ModelManagementActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "adapter", "Lcom/devicecontrol/engine/ui/model/ModelListAdapter;", "binding", "Lcom/devicecontrol/engine/databinding/ActivityModelManagementBinding;", "database", "Lcom/devicecontrol/engine/data/database/AppDatabase;", "getDatabase", "()Lcom/devicecontrol/engine/data/database/AppDatabase;", "database$delegate", "Lkotlin/Lazy;", "layoutManager", "Landroidx/recyclerview/widget/GridLayoutManager;", "repository", "Lcom/devicecontrol/engine/data/repository/EngineRepository;", "getRepository", "()Lcom/devicecontrol/engine/data/repository/EngineRepository;", "repository$delegate", "viewModel", "Lcom/devicecontrol/engine/viewmodel/ModelManagementViewModel;", "getViewModel", "()Lcom/devicecontrol/engine/viewmodel/ModelManagementViewModel;", "viewModel$delegate", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "setupListeners", "setupObservers", "setupRecyclerView", "showConfigItemDialog", "modelId", "", "showConfigItemDialogForNewModel", "modelName", "", "showDeleteConfigItemDialog", "configItem", "Lcom/devicecontrol/engine/data/model/ConfigItem;", "showDeleteModelDialog", "showNewModelDialog", "updateCurrentModelDisplay", "model", "Lcom/devicecontrol/engine/data/model/EngineModelWithConfigItems;", "engine-lib_debug"})
public final class ModelManagementActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.devicecontrol.engine.databinding.ActivityModelManagementBinding binding;
    private com.devicecontrol.engine.ui.model.ModelListAdapter adapter;
    private androidx.recyclerview.widget.GridLayoutManager layoutManager;
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy database$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy repository$delegate = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.Lazy viewModel$delegate = null;
    
    public ModelManagementActivity() {
        super();
    }
    
    private final com.devicecontrol.engine.data.database.AppDatabase getDatabase() {
        return null;
    }
    
    private final com.devicecontrol.engine.data.repository.EngineRepository getRepository() {
        return null;
    }
    
    private final com.devicecontrol.engine.viewmodel.ModelManagementViewModel getViewModel() {
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
    
    private final void updateCurrentModelDisplay(com.devicecontrol.engine.data.model.EngineModelWithConfigItems model) {
    }
    
    private final void setupListeners() {
    }
    
    private final void showNewModelDialog() {
    }
    
    private final void showConfigItemDialogForNewModel(java.lang.String modelName) {
    }
    
    private final void showConfigItemDialog(long modelId) {
    }
    
    private final void showDeleteConfigItemDialog(com.devicecontrol.engine.data.model.ConfigItem configItem) {
    }
    
    private final void showDeleteModelDialog(long modelId) {
    }
}