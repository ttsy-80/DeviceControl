package com.devicecontrol.engine.data.database;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \u00072\u00020\u0001:\u0001\u0007B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&\u00a8\u0006\b"}, d2 = {"Lcom/devicecontrol/engine/data/database/AppDatabase;", "Landroidx/room/RoomDatabase;", "()V", "engineDao", "Lcom/devicecontrol/engine/data/database/dao/EngineDao;", "taskDao", "Lcom/devicecontrol/engine/data/database/dao/TaskDao;", "Companion", "engine-lib_debug"})
@androidx.room.Database(entities = {com.devicecontrol.engine.data.model.EngineModel.class, com.devicecontrol.engine.data.model.ConfigItem.class, com.devicecontrol.engine.data.model.Task.class, com.devicecontrol.engine.data.model.TaskExecution.class}, version = 2, exportSchema = false)
@androidx.room.TypeConverters(value = {com.devicecontrol.engine.data.database.Converters.class})
public abstract class AppDatabase extends androidx.room.RoomDatabase {
    @kotlin.jvm.Volatile
    @org.jetbrains.annotations.Nullable
    private static volatile com.devicecontrol.engine.data.database.AppDatabase INSTANCE;
    @org.jetbrains.annotations.NotNull
    public static final com.devicecontrol.engine.data.database.AppDatabase.Companion Companion = null;
    
    public AppDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public abstract com.devicecontrol.engine.data.database.dao.EngineDao engineDao();
    
    @org.jetbrains.annotations.NotNull
    public abstract com.devicecontrol.engine.data.database.dao.TaskDao taskDao();
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u0007R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/devicecontrol/engine/data/database/AppDatabase$Companion;", "", "()V", "INSTANCE", "Lcom/devicecontrol/engine/data/database/AppDatabase;", "getDatabase", "context", "Landroid/content/Context;", "engine-lib_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.devicecontrol.engine.data.database.AppDatabase getDatabase(@org.jetbrains.annotations.NotNull
        android.content.Context context) {
            return null;
        }
    }
}