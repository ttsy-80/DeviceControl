package com.devicecontrol.engine.data.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.RelationUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.devicecontrol.engine.data.model.ConfigItem;
import com.devicecontrol.engine.data.model.EngineModel;
import com.devicecontrol.engine.data.model.EngineModelWithConfigItems;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class EngineDao_Impl implements EngineDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<EngineModel> __insertionAdapterOfEngineModel;

  private final EntityInsertionAdapter<ConfigItem> __insertionAdapterOfConfigItem;

  private final EntityDeletionOrUpdateAdapter<EngineModel> __deletionAdapterOfEngineModel;

  private final EntityDeletionOrUpdateAdapter<ConfigItem> __deletionAdapterOfConfigItem;

  private final SharedSQLiteStatement __preparedStmtOfDeleteConfigItemById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteModelById;

  public EngineDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEngineModel = new EntityInsertionAdapter<EngineModel>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `engine_models` (`id`,`name`) VALUES (nullif(?, 0),?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EngineModel entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
      }
    };
    this.__insertionAdapterOfConfigItem = new EntityInsertionAdapter<ConfigItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `config_items` (`id`,`modelId`,`gearRatio`,`position`,`bladeCount`,`jogCount`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ConfigItem entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getModelId());
        statement.bindDouble(3, entity.getGearRatio());
        if (entity.getPosition() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getPosition());
        }
        statement.bindLong(5, entity.getBladeCount());
        statement.bindLong(6, entity.getJogCount());
      }
    };
    this.__deletionAdapterOfEngineModel = new EntityDeletionOrUpdateAdapter<EngineModel>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `engine_models` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EngineModel entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__deletionAdapterOfConfigItem = new EntityDeletionOrUpdateAdapter<ConfigItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `config_items` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ConfigItem entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteConfigItemById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM config_items WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteModelById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM engine_models WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertModel(final EngineModel model, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfEngineModel.insertAndReturnId(model);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertConfigItem(final ConfigItem configItem,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfConfigItem.insertAndReturnId(configItem);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertConfigItems(final List<ConfigItem> configItems,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfConfigItem.insert(configItems);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteModel(final EngineModel model, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfEngineModel.handle(model);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteConfigItem(final ConfigItem configItem,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfConfigItem.handle(configItem);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteConfigItemById(final long configItemId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteConfigItemById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, configItemId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteConfigItemById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteModelById(final long modelId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteModelById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, modelId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteModelById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<EngineModel>> getAllModels() {
    final String _sql = "SELECT * FROM engine_models ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"engine_models"}, new Callable<List<EngineModel>>() {
      @Override
      @NonNull
      public List<EngineModel> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final List<EngineModel> _result = new ArrayList<EngineModel>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EngineModel _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            _item = new EngineModel(_tmpId,_tmpName);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getModelById(final long modelId,
      final Continuation<? super EngineModel> $completion) {
    final String _sql = "SELECT * FROM engine_models WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, modelId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<EngineModel>() {
      @Override
      @Nullable
      public EngineModel call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final EngineModel _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            _result = new EngineModel(_tmpId,_tmpName);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getModelByName(final String name,
      final Continuation<? super EngineModel> $completion) {
    final String _sql = "SELECT * FROM engine_models WHERE name = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (name == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, name);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<EngineModel>() {
      @Override
      @Nullable
      public EngineModel call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final EngineModel _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            _result = new EngineModel(_tmpId,_tmpName);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<EngineModelWithConfigItems>> getAllModelsWithConfigItems() {
    final String _sql = "SELECT * FROM engine_models ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"config_items",
        "engine_models"}, new Callable<List<EngineModelWithConfigItems>>() {
      @Override
      @NonNull
      public List<EngineModelWithConfigItems> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
            final LongSparseArray<ArrayList<ConfigItem>> _collectionConfigItems = new LongSparseArray<ArrayList<ConfigItem>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionConfigItems.containsKey(_tmpKey)) {
                _collectionConfigItems.put(_tmpKey, new ArrayList<ConfigItem>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipconfigItemsAscomDevicecontrolEngineDataModelConfigItem(_collectionConfigItems);
            final List<EngineModelWithConfigItems> _result = new ArrayList<EngineModelWithConfigItems>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final EngineModelWithConfigItems _item;
              final EngineModel _tmpModel;
              final long _tmpId;
              _tmpId = _cursor.getLong(_cursorIndexOfId);
              final String _tmpName;
              if (_cursor.isNull(_cursorIndexOfName)) {
                _tmpName = null;
              } else {
                _tmpName = _cursor.getString(_cursorIndexOfName);
              }
              _tmpModel = new EngineModel(_tmpId,_tmpName);
              final ArrayList<ConfigItem> _tmpConfigItemsCollection;
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              _tmpConfigItemsCollection = _collectionConfigItems.get(_tmpKey_1);
              _item = new EngineModelWithConfigItems(_tmpModel,_tmpConfigItemsCollection);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getModelWithConfigItemsById(final long modelId,
      final Continuation<? super EngineModelWithConfigItems> $completion) {
    final String _sql = "SELECT * FROM engine_models WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, modelId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, true, _cancellationSignal, new Callable<EngineModelWithConfigItems>() {
      @Override
      @Nullable
      public EngineModelWithConfigItems call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
            final LongSparseArray<ArrayList<ConfigItem>> _collectionConfigItems = new LongSparseArray<ArrayList<ConfigItem>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionConfigItems.containsKey(_tmpKey)) {
                _collectionConfigItems.put(_tmpKey, new ArrayList<ConfigItem>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipconfigItemsAscomDevicecontrolEngineDataModelConfigItem(_collectionConfigItems);
            final EngineModelWithConfigItems _result;
            if (_cursor.moveToFirst()) {
              final EngineModel _tmpModel;
              final long _tmpId;
              _tmpId = _cursor.getLong(_cursorIndexOfId);
              final String _tmpName;
              if (_cursor.isNull(_cursorIndexOfName)) {
                _tmpName = null;
              } else {
                _tmpName = _cursor.getString(_cursorIndexOfName);
              }
              _tmpModel = new EngineModel(_tmpId,_tmpName);
              final ArrayList<ConfigItem> _tmpConfigItemsCollection;
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              _tmpConfigItemsCollection = _collectionConfigItems.get(_tmpKey_1);
              _result = new EngineModelWithConfigItems(_tmpModel,_tmpConfigItemsCollection);
            } else {
              _result = null;
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
            _statement.release();
          }
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getConfigItemsByModelId(final long modelId,
      final Continuation<? super List<ConfigItem>> $completion) {
    final String _sql = "SELECT * FROM config_items WHERE modelId = ? ORDER BY gearRatio ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, modelId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ConfigItem>>() {
      @Override
      @NonNull
      public List<ConfigItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfModelId = CursorUtil.getColumnIndexOrThrow(_cursor, "modelId");
          final int _cursorIndexOfGearRatio = CursorUtil.getColumnIndexOrThrow(_cursor, "gearRatio");
          final int _cursorIndexOfPosition = CursorUtil.getColumnIndexOrThrow(_cursor, "position");
          final int _cursorIndexOfBladeCount = CursorUtil.getColumnIndexOrThrow(_cursor, "bladeCount");
          final int _cursorIndexOfJogCount = CursorUtil.getColumnIndexOrThrow(_cursor, "jogCount");
          final List<ConfigItem> _result = new ArrayList<ConfigItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ConfigItem _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpModelId;
            _tmpModelId = _cursor.getLong(_cursorIndexOfModelId);
            final double _tmpGearRatio;
            _tmpGearRatio = _cursor.getDouble(_cursorIndexOfGearRatio);
            final String _tmpPosition;
            if (_cursor.isNull(_cursorIndexOfPosition)) {
              _tmpPosition = null;
            } else {
              _tmpPosition = _cursor.getString(_cursorIndexOfPosition);
            }
            final int _tmpBladeCount;
            _tmpBladeCount = _cursor.getInt(_cursorIndexOfBladeCount);
            final int _tmpJogCount;
            _tmpJogCount = _cursor.getInt(_cursorIndexOfJogCount);
            _item = new ConfigItem(_tmpId,_tmpModelId,_tmpGearRatio,_tmpPosition,_tmpBladeCount,_tmpJogCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ConfigItem>> getConfigItemsByModelIdFlow(final long modelId) {
    final String _sql = "SELECT * FROM config_items WHERE modelId = ? ORDER BY gearRatio ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, modelId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"config_items"}, new Callable<List<ConfigItem>>() {
      @Override
      @NonNull
      public List<ConfigItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfModelId = CursorUtil.getColumnIndexOrThrow(_cursor, "modelId");
          final int _cursorIndexOfGearRatio = CursorUtil.getColumnIndexOrThrow(_cursor, "gearRatio");
          final int _cursorIndexOfPosition = CursorUtil.getColumnIndexOrThrow(_cursor, "position");
          final int _cursorIndexOfBladeCount = CursorUtil.getColumnIndexOrThrow(_cursor, "bladeCount");
          final int _cursorIndexOfJogCount = CursorUtil.getColumnIndexOrThrow(_cursor, "jogCount");
          final List<ConfigItem> _result = new ArrayList<ConfigItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ConfigItem _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpModelId;
            _tmpModelId = _cursor.getLong(_cursorIndexOfModelId);
            final double _tmpGearRatio;
            _tmpGearRatio = _cursor.getDouble(_cursorIndexOfGearRatio);
            final String _tmpPosition;
            if (_cursor.isNull(_cursorIndexOfPosition)) {
              _tmpPosition = null;
            } else {
              _tmpPosition = _cursor.getString(_cursorIndexOfPosition);
            }
            final int _tmpBladeCount;
            _tmpBladeCount = _cursor.getInt(_cursorIndexOfBladeCount);
            final int _tmpJogCount;
            _tmpJogCount = _cursor.getInt(_cursorIndexOfJogCount);
            _item = new ConfigItem(_tmpId,_tmpModelId,_tmpGearRatio,_tmpPosition,_tmpBladeCount,_tmpJogCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getConfigItemCount(final long modelId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM config_items WHERE modelId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, modelId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private void __fetchRelationshipconfigItemsAscomDevicecontrolEngineDataModelConfigItem(
      @NonNull final LongSparseArray<ArrayList<ConfigItem>> _map) {
    if (_map.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchLongSparseArray(_map, true, (map) -> {
        __fetchRelationshipconfigItemsAscomDevicecontrolEngineDataModelConfigItem(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`modelId`,`gearRatio`,`position`,`bladeCount`,`jogCount` FROM `config_items` WHERE `modelId` IN (");
    final int _inputSize = _map.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (int i = 0; i < _map.size(); i++) {
      final long _item = _map.keyAt(i);
      _stmt.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "modelId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfModelId = 1;
      final int _cursorIndexOfGearRatio = 2;
      final int _cursorIndexOfPosition = 3;
      final int _cursorIndexOfBladeCount = 4;
      final int _cursorIndexOfJogCount = 5;
      while (_cursor.moveToNext()) {
        final long _tmpKey;
        _tmpKey = _cursor.getLong(_itemKeyIndex);
        final ArrayList<ConfigItem> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final ConfigItem _item_1;
          final long _tmpId;
          _tmpId = _cursor.getLong(_cursorIndexOfId);
          final long _tmpModelId;
          _tmpModelId = _cursor.getLong(_cursorIndexOfModelId);
          final double _tmpGearRatio;
          _tmpGearRatio = _cursor.getDouble(_cursorIndexOfGearRatio);
          final String _tmpPosition;
          if (_cursor.isNull(_cursorIndexOfPosition)) {
            _tmpPosition = null;
          } else {
            _tmpPosition = _cursor.getString(_cursorIndexOfPosition);
          }
          final int _tmpBladeCount;
          _tmpBladeCount = _cursor.getInt(_cursorIndexOfBladeCount);
          final int _tmpJogCount;
          _tmpJogCount = _cursor.getInt(_cursorIndexOfJogCount);
          _item_1 = new ConfigItem(_tmpId,_tmpModelId,_tmpGearRatio,_tmpPosition,_tmpBladeCount,_tmpJogCount);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }
}
