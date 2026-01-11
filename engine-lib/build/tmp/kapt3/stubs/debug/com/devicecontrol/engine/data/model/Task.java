package com.devicecontrol.engine.data.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0010\u0006\n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B-\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b\u00a2\u0006\u0002\u0010\nJ\t\u0010\u0012\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\u0006H\u00c6\u0003J\u000f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\t0\bH\u00c6\u0003J7\u0010\u0016\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\u000e\b\u0002\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bH\u00c6\u0001J\u0013\u0010\u0017\u001a\u00020\u00182\b\u0010\u0019\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001a\u001a\u00020\u001bH\u00d6\u0001J\t\u0010\u001c\u001a\u00020\u0006H\u00d6\u0001R\u0017\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u000eR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011\u00a8\u0006\u001d"}, d2 = {"Lcom/devicecontrol/engine/data/model/Task;", "", "id", "", "modelId", "modelName", "", "gearRatios", "", "", "(JJLjava/lang/String;Ljava/util/List;)V", "getGearRatios", "()Ljava/util/List;", "getId", "()J", "getModelId", "getModelName", "()Ljava/lang/String;", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "", "toString", "engine-lib_debug"})
@androidx.room.Entity(tableName = "tasks")
@androidx.room.TypeConverters(value = {com.devicecontrol.engine.data.database.Converters.class})
public final class Task {
    @androidx.room.PrimaryKey(autoGenerate = true)
    private final long id = 0L;
    private final long modelId = 0L;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String modelName = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<java.lang.Double> gearRatios = null;
    
    public Task(long id, long modelId, @org.jetbrains.annotations.NotNull
    java.lang.String modelName, @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.Double> gearRatios) {
        super();
    }
    
    public final long getId() {
        return 0L;
    }
    
    public final long getModelId() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getModelName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.Double> getGearRatios() {
        return null;
    }
    
    public final long component1() {
        return 0L;
    }
    
    public final long component2() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<java.lang.Double> component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.devicecontrol.engine.data.model.Task copy(long id, long modelId, @org.jetbrains.annotations.NotNull
    java.lang.String modelName, @org.jetbrains.annotations.NotNull
    java.util.List<java.lang.Double> gearRatios) {
        return null;
    }
    
    @java.lang.Override
    public boolean equals(@org.jetbrains.annotations.Nullable
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public java.lang.String toString() {
        return null;
    }
}