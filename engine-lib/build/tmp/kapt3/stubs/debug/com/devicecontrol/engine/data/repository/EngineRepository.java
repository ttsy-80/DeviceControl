package com.devicecontrol.engine.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\tJ\u0016\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rH\u0086@\u00a2\u0006\u0002\u0010\u000eJ\u0012\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00110\u0010J\u0016\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0086@\u00a2\u0006\u0002\u0010\u0017J\u001c\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\b0\u00112\u0006\u0010\u0015\u001a\u00020\u0016H\u0086@\u00a2\u0006\u0002\u0010\u0017J\u0018\u0010\u0019\u001a\u0004\u0018\u00010\r2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086@\u00a2\u0006\u0002\u0010\u001cJ\u0018\u0010\u001d\u001a\u0004\u0018\u00010\u00122\u0006\u0010\u0015\u001a\u00020\u0016H\u0086@\u00a2\u0006\u0002\u0010\u0017J\u0016\u0010\u001e\u001a\u00020\u00162\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\tJ\u001e\u0010\u001f\u001a\u00020\u00162\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010 J\u0016\u0010!\u001a\u00020\u000b2\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\""}, d2 = {"Lcom/devicecontrol/engine/data/repository/EngineRepository;", "", "engineDao", "Lcom/devicecontrol/engine/data/database/dao/EngineDao;", "(Lcom/devicecontrol/engine/data/database/dao/EngineDao;)V", "deleteConfigItem", "", "configItem", "Lcom/devicecontrol/engine/data/model/ConfigItem;", "(Lcom/devicecontrol/engine/data/model/ConfigItem;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteModel", "", "model", "Lcom/devicecontrol/engine/data/model/EngineModel;", "(Lcom/devicecontrol/engine/data/model/EngineModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllModelsWithConfigItems", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/devicecontrol/engine/data/model/EngineModelWithConfigItems;", "getConfigItemCount", "", "modelId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getConfigItemsByModelId", "getModelByName", "name", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getModelWithConfigItemsById", "insertConfigItem", "insertModelWithConfigItem", "(Lcom/devicecontrol/engine/data/model/EngineModel;Lcom/devicecontrol/engine/data/model/ConfigItem;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateConfigItem", "engine-lib_debug"})
public final class EngineRepository {
    @org.jetbrains.annotations.NotNull
    private final com.devicecontrol.engine.data.database.dao.EngineDao engineDao = null;
    
    public EngineRepository(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.database.dao.EngineDao engineDao) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.devicecontrol.engine.data.model.EngineModelWithConfigItems>> getAllModelsWithConfigItems() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getModelWithConfigItemsById(long modelId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.devicecontrol.engine.data.model.EngineModelWithConfigItems> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getModelByName(@org.jetbrains.annotations.NotNull
    java.lang.String name, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.devicecontrol.engine.data.model.EngineModel> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getConfigItemsByModelId(long modelId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.devicecontrol.engine.data.model.ConfigItem>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object insertModelWithConfigItem(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.model.EngineModel model, @org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.model.ConfigItem configItem, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object insertConfigItem(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.model.ConfigItem configItem, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object deleteConfigItem(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.model.ConfigItem configItem, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object deleteModel(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.model.EngineModel model, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getConfigItemCount(long modelId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object updateConfigItem(@org.jetbrains.annotations.NotNull
    com.devicecontrol.engine.data.model.ConfigItem configItem, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}