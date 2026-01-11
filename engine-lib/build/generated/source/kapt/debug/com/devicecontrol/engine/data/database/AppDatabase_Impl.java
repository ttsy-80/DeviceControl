package com.devicecontrol.engine.data.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.devicecontrol.engine.data.database.dao.EngineDao;
import com.devicecontrol.engine.data.database.dao.EngineDao_Impl;
import com.devicecontrol.engine.data.database.dao.TaskDao;
import com.devicecontrol.engine.data.database.dao.TaskDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile EngineDao _engineDao;

  private volatile TaskDao _taskDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `engine_models` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `config_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `modelId` INTEGER NOT NULL, `gearRatio` REAL NOT NULL, `position` TEXT NOT NULL, `bladeCount` INTEGER NOT NULL, `jogCount` INTEGER NOT NULL, FOREIGN KEY(`modelId`) REFERENCES `engine_models`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_config_items_modelId` ON `config_items` (`modelId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `tasks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `modelId` INTEGER NOT NULL, `modelName` TEXT NOT NULL, `gearRatios` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `task_executions` (`executionId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `taskId` INTEGER NOT NULL, `currentGearRatioIndex` INTEGER NOT NULL, `status` TEXT NOT NULL, `torque` REAL NOT NULL, `speed` REAL NOT NULL, `rotationDirection` TEXT NOT NULL, `operationMode` TEXT NOT NULL, `progress` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '4e24ccb4f1c8a7b78cb1837e5c1665c5')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `engine_models`");
        db.execSQL("DROP TABLE IF EXISTS `config_items`");
        db.execSQL("DROP TABLE IF EXISTS `tasks`");
        db.execSQL("DROP TABLE IF EXISTS `task_executions`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsEngineModels = new HashMap<String, TableInfo.Column>(2);
        _columnsEngineModels.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEngineModels.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEngineModels = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesEngineModels = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoEngineModels = new TableInfo("engine_models", _columnsEngineModels, _foreignKeysEngineModels, _indicesEngineModels);
        final TableInfo _existingEngineModels = TableInfo.read(db, "engine_models");
        if (!_infoEngineModels.equals(_existingEngineModels)) {
          return new RoomOpenHelper.ValidationResult(false, "engine_models(com.devicecontrol.engine.data.model.EngineModel).\n"
                  + " Expected:\n" + _infoEngineModels + "\n"
                  + " Found:\n" + _existingEngineModels);
        }
        final HashMap<String, TableInfo.Column> _columnsConfigItems = new HashMap<String, TableInfo.Column>(6);
        _columnsConfigItems.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConfigItems.put("modelId", new TableInfo.Column("modelId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConfigItems.put("gearRatio", new TableInfo.Column("gearRatio", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConfigItems.put("position", new TableInfo.Column("position", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConfigItems.put("bladeCount", new TableInfo.Column("bladeCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConfigItems.put("jogCount", new TableInfo.Column("jogCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysConfigItems = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysConfigItems.add(new TableInfo.ForeignKey("engine_models", "CASCADE", "NO ACTION", Arrays.asList("modelId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesConfigItems = new HashSet<TableInfo.Index>(1);
        _indicesConfigItems.add(new TableInfo.Index("index_config_items_modelId", false, Arrays.asList("modelId"), Arrays.asList("ASC")));
        final TableInfo _infoConfigItems = new TableInfo("config_items", _columnsConfigItems, _foreignKeysConfigItems, _indicesConfigItems);
        final TableInfo _existingConfigItems = TableInfo.read(db, "config_items");
        if (!_infoConfigItems.equals(_existingConfigItems)) {
          return new RoomOpenHelper.ValidationResult(false, "config_items(com.devicecontrol.engine.data.model.ConfigItem).\n"
                  + " Expected:\n" + _infoConfigItems + "\n"
                  + " Found:\n" + _existingConfigItems);
        }
        final HashMap<String, TableInfo.Column> _columnsTasks = new HashMap<String, TableInfo.Column>(4);
        _columnsTasks.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTasks.put("modelId", new TableInfo.Column("modelId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTasks.put("modelName", new TableInfo.Column("modelName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTasks.put("gearRatios", new TableInfo.Column("gearRatios", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTasks = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTasks = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTasks = new TableInfo("tasks", _columnsTasks, _foreignKeysTasks, _indicesTasks);
        final TableInfo _existingTasks = TableInfo.read(db, "tasks");
        if (!_infoTasks.equals(_existingTasks)) {
          return new RoomOpenHelper.ValidationResult(false, "tasks(com.devicecontrol.engine.data.model.Task).\n"
                  + " Expected:\n" + _infoTasks + "\n"
                  + " Found:\n" + _existingTasks);
        }
        final HashMap<String, TableInfo.Column> _columnsTaskExecutions = new HashMap<String, TableInfo.Column>(9);
        _columnsTaskExecutions.put("executionId", new TableInfo.Column("executionId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTaskExecutions.put("taskId", new TableInfo.Column("taskId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTaskExecutions.put("currentGearRatioIndex", new TableInfo.Column("currentGearRatioIndex", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTaskExecutions.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTaskExecutions.put("torque", new TableInfo.Column("torque", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTaskExecutions.put("speed", new TableInfo.Column("speed", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTaskExecutions.put("rotationDirection", new TableInfo.Column("rotationDirection", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTaskExecutions.put("operationMode", new TableInfo.Column("operationMode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTaskExecutions.put("progress", new TableInfo.Column("progress", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTaskExecutions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTaskExecutions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTaskExecutions = new TableInfo("task_executions", _columnsTaskExecutions, _foreignKeysTaskExecutions, _indicesTaskExecutions);
        final TableInfo _existingTaskExecutions = TableInfo.read(db, "task_executions");
        if (!_infoTaskExecutions.equals(_existingTaskExecutions)) {
          return new RoomOpenHelper.ValidationResult(false, "task_executions(com.devicecontrol.engine.data.model.TaskExecution).\n"
                  + " Expected:\n" + _infoTaskExecutions + "\n"
                  + " Found:\n" + _existingTaskExecutions);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "4e24ccb4f1c8a7b78cb1837e5c1665c5", "0dc1e5c21c43b000567e4f711c084132");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "engine_models","config_items","tasks","task_executions");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `engine_models`");
      _db.execSQL("DELETE FROM `config_items`");
      _db.execSQL("DELETE FROM `tasks`");
      _db.execSQL("DELETE FROM `task_executions`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(EngineDao.class, EngineDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TaskDao.class, TaskDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public EngineDao engineDao() {
    if (_engineDao != null) {
      return _engineDao;
    } else {
      synchronized(this) {
        if(_engineDao == null) {
          _engineDao = new EngineDao_Impl(this);
        }
        return _engineDao;
      }
    }
  }

  @Override
  public TaskDao taskDao() {
    if (_taskDao != null) {
      return _taskDao;
    } else {
      synchronized(this) {
        if(_taskDao == null) {
          _taskDao = new TaskDao_Impl(this);
        }
        return _taskDao;
      }
    }
  }
}
