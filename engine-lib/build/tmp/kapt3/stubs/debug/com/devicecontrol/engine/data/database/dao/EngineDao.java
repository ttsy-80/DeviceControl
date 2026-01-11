package com.devicecontrol.engine.data.database.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\t\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\u000b\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\rH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u0016\u0010\u000f\u001a\u00020\u00032\u0006\u0010\u0010\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0014\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\u00130\u0012H\'J\u0014\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00130\u0012H\'J\u0016\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0010\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u001c\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00050\u00132\u0006\u0010\u0010\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u001c\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00130\u00122\u0006\u0010\u0010\u001a\u00020\tH\'J\u0018\u0010\u001a\u001a\u0004\u0018\u00010\r2\u0006\u0010\u0010\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0018\u0010\u001b\u001a\u0004\u0018\u00010\r2\u0006\u0010\u001c\u001a\u00020\u001dH\u00a7@\u00a2\u0006\u0002\u0010\u001eJ\u0018\u0010\u001f\u001a\u0004\u0018\u00010\u00152\u0006\u0010\u0010\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010 \u001a\u00020\t2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001c\u0010!\u001a\u00020\u00032\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00050\u0013H\u00a7@\u00a2\u0006\u0002\u0010#J\u0016\u0010$\u001a\u00020\t2\u0006\u0010\f\u001a\u00020\rH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u0016\u0010%\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006&"}, d2 = {"Lcom/devicecontrol/engine/data/database/dao/EngineDao;", "", "deleteConfigItem", "", "configItem", "Lcom/devicecontrol/engine/data/model/ConfigItem;", "(Lcom/devicecontrol/engine/data/model/ConfigItem;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteConfigItemById", "configItemId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteModel", "model", "Lcom/devicecontrol/engine/data/model/EngineModel;", "(Lcom/devicecontrol/engine/data/model/EngineModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteModelById", "modelId", "getAllModels", "Lkotlinx/coroutines/flow/Flow;", "", "getAllModelsWithConfigItems", "Lcom/devicecontrol/engine/data/model/EngineModelWithConfigItems;", "getConfigItemCount", "", "getConfigItemsByModelId", "getConfigItemsByModelIdFlow", "getModelById", "getModelByName", "name", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getModelWithConfigItemsById", "insertConfigItem", "insertConfigItems", "configItems", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertModel", "updateConfigItem", "engine-lib_debug"})
@androidx.room.Dao
public abstract interface EngineDao {
    
    @androidx.room.Query(value = "SELECT * FROM engine_models ORDER BY name ASC")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.devicecontrol.engine.data.model.EngineModel>> getAllModels();
    
    @androidx.room.Query(value = "SELECT * FROM engine_models WHERE id = :modelId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getModelById(long modelId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.devicecontrol.engine.data.model.EngineModel> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM engine_models WHERE name = :name")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getModelByName(@org.jetbrains.annotations.NotNull
    java.lang.String name, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.devicecontrol.engine.data.model.EngineModel> $completion);
    
    @androidx.room.Transaction
    @androidx.room.Query(value = "SELECT * FROM engine_models ORDER BY name ASC")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.devicecontrol.engine.data.model.EngineModelWithConfigItems>> getAllModelsWithConfigItems();
    
    @androidx.room.Transaction
    @androidx.room.Query(value = "SELECT * FROM engine_models WHERE id = :modelId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getModelWithConfigItemsById(long modelId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.devicecontrol.engine.data.model.EngineModelWithConfigItems> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object insertModel(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.model.EngineModel model, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object insertConfigItem(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.model.ConfigItem configItem, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object insertConfigItems(@org.jetbrains.annotations.NotNull
    java.util.List<com.devicecontrol.engine.data.model.ConfigItem> configItems, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM config_items WHERE modelId = :modelId ORDER BY gearRatio ASC")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getConfigItemsByModelId(long modelId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.devicecontrol.engine.data.model.ConfigItem>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM config_items WHERE modelId = :modelId ORDER BY gearRatio ASC")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.devicecontrol.engine.data.model.ConfigItem>> getConfigItemsByModelIdFlow(long modelId);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM config_items WHERE modelId = :modelId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getConfigItemCount(long modelId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @androidx.room.Delete
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteModel(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.model.EngineModel model, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteConfigItem(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.model.ConfigItem configItem, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Update
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object updateConfigItem(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.model.ConfigItem configItem, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM config_items WHERE id = :configItemId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteConfigItemById(long configItemId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM engine_models WHERE id = :modelId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteModelById(long modelId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}