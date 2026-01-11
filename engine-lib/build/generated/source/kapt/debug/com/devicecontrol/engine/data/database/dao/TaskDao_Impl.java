package com.devicecontrol.engine.data.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.devicecontrol.engine.data.database.Converters;
import com.devicecontrol.engine.data.model.OperationMode;
import com.devicecontrol.engine.data.model.RotationDirection;
import com.devicecontrol.engine.data.model.Task;
import com.devicecontrol.engine.data.model.TaskExecution;
import com.devicecontrol.engine.data.model.TaskStatus;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
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
public final class TaskDao_Impl implements TaskDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Task> __insertionAdapterOfTask;

  private final Converters __converters = new Converters();

  private final EntityInsertionAdapter<TaskExecution> __insertionAdapterOfTaskExecution;

  private final EntityDeletionOrUpdateAdapter<Task> __deletionAdapterOfTask;

  private final EntityDeletionOrUpdateAdapter<TaskExecution> __updateAdapterOfTaskExecution;

  private final SharedSQLiteStatement __preparedStmtOfDeleteTaskExecutionByTaskId;

  public TaskDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTask = new EntityInsertionAdapter<Task>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `tasks` (`id`,`modelId`,`modelName`,`gearRatios`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Task entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getModelId());
        if (entity.getModelName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getModelName());
        }
        final String _tmp = __converters.fromDoubleList(entity.getGearRatios());
        if (_tmp == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, _tmp);
        }
      }
    };
    this.__insertionAdapterOfTaskExecution = new EntityInsertionAdapter<TaskExecution>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `task_executions` (`executionId`,`taskId`,`gearRatioIndex`,`status`,`torque`,`speed`,`rotationDirection`,`operationMode`,`progress`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TaskExecution entity) {
        statement.bindLong(1, entity.getExecutionId());
        statement.bindLong(2, entity.getTaskId());
        statement.bindLong(3, entity.getGearRatioIndex());
        statement.bindString(4, __TaskStatus_enumToString(entity.getStatus()));
        statement.bindDouble(5, entity.getTorque());
        statement.bindDouble(6, entity.getSpeed());
        statement.bindString(7, __RotationDirection_enumToString(entity.getRotationDirection()));
        statement.bindString(8, __OperationMode_enumToString(entity.getOperationMode()));
        statement.bindLong(9, entity.getProgress());
      }
    };
    this.__deletionAdapterOfTask = new EntityDeletionOrUpdateAdapter<Task>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `tasks` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Task entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfTaskExecution = new EntityDeletionOrUpdateAdapter<TaskExecution>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `task_executions` SET `executionId` = ?,`taskId` = ?,`gearRatioIndex` = ?,`status` = ?,`torque` = ?,`speed` = ?,`rotationDirection` = ?,`operationMode` = ?,`progress` = ? WHERE `executionId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TaskExecution entity) {
        statement.bindLong(1, entity.getExecutionId());
        statement.bindLong(2, entity.getTaskId());
        statement.bindLong(3, entity.getGearRatioIndex());
        statement.bindString(4, __TaskStatus_enumToString(entity.getStatus()));
        statement.bindDouble(5, entity.getTorque());
        statement.bindDouble(6, entity.getSpeed());
        statement.bindString(7, __RotationDirection_enumToString(entity.getRotationDirection()));
        statement.bindString(8, __OperationMode_enumToString(entity.getOperationMode()));
        statement.bindLong(9, entity.getProgress());
        statement.bindLong(10, entity.getExecutionId());
      }
    };
    this.__preparedStmtOfDeleteTaskExecutionByTaskId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM task_executions WHERE taskId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertTask(final Task task, final Continuation<? super Long> arg1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTask.insertAndReturnId(task);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, arg1);
  }

  @Override
  public Object insertTaskExecution(final TaskExecution execution,
      final Continuation<? super Long> arg1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTaskExecution.insertAndReturnId(execution);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, arg1);
  }

  @Override
  public Object deleteTask(final Task task, final Continuation<? super Unit> arg1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfTask.handle(task);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, arg1);
  }

  @Override
  public Object updateTaskExecution(final TaskExecution execution,
      final Continuation<? super Unit> arg1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTaskExecution.handle(execution);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, arg1);
  }

  @Override
  public Object deleteTaskExecutionByTaskId(final long taskId,
      final Continuation<? super Unit> arg1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteTaskExecutionByTaskId.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, taskId);
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
          __preparedStmtOfDeleteTaskExecutionByTaskId.release(_stmt);
        }
      }
    }, arg1);
  }

  @Override
  public Flow<List<Task>> getAllTasks() {
    final String _sql = "SELECT * FROM tasks ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tasks"}, new Callable<List<Task>>() {
      @Override
      @NonNull
      public List<Task> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfModelId = CursorUtil.getColumnIndexOrThrow(_cursor, "modelId");
          final int _cursorIndexOfModelName = CursorUtil.getColumnIndexOrThrow(_cursor, "modelName");
          final int _cursorIndexOfGearRatios = CursorUtil.getColumnIndexOrThrow(_cursor, "gearRatios");
          final List<Task> _result = new ArrayList<Task>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Task _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpModelId;
            _tmpModelId = _cursor.getLong(_cursorIndexOfModelId);
            final String _tmpModelName;
            if (_cursor.isNull(_cursorIndexOfModelName)) {
              _tmpModelName = null;
            } else {
              _tmpModelName = _cursor.getString(_cursorIndexOfModelName);
            }
            final List<Double> _tmpGearRatios;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfGearRatios)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfGearRatios);
            }
            _tmpGearRatios = __converters.toDoubleList(_tmp);
            _item = new Task(_tmpId,_tmpModelId,_tmpModelName,_tmpGearRatios);
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
  public Object getTaskById(final long taskId, final Continuation<? super Task> arg1) {
    final String _sql = "SELECT * FROM tasks WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, taskId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Task>() {
      @Override
      @Nullable
      public Task call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfModelId = CursorUtil.getColumnIndexOrThrow(_cursor, "modelId");
          final int _cursorIndexOfModelName = CursorUtil.getColumnIndexOrThrow(_cursor, "modelName");
          final int _cursorIndexOfGearRatios = CursorUtil.getColumnIndexOrThrow(_cursor, "gearRatios");
          final Task _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpModelId;
            _tmpModelId = _cursor.getLong(_cursorIndexOfModelId);
            final String _tmpModelName;
            if (_cursor.isNull(_cursorIndexOfModelName)) {
              _tmpModelName = null;
            } else {
              _tmpModelName = _cursor.getString(_cursorIndexOfModelName);
            }
            final List<Double> _tmpGearRatios;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfGearRatios)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfGearRatios);
            }
            _tmpGearRatios = __converters.toDoubleList(_tmp);
            _result = new Task(_tmpId,_tmpModelId,_tmpModelName,_tmpGearRatios);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, arg1);
  }

  @Override
  public Object getTaskExecutionByTaskIdAndIndex(final long taskId, final int gearRatioIndex,
      final Continuation<? super TaskExecution> arg2) {
    final String _sql = "SELECT * FROM task_executions WHERE taskId = ? AND gearRatioIndex = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, taskId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, gearRatioIndex);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TaskExecution>() {
      @Override
      @Nullable
      public TaskExecution call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfExecutionId = CursorUtil.getColumnIndexOrThrow(_cursor, "executionId");
          final int _cursorIndexOfTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "taskId");
          final int _cursorIndexOfGearRatioIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "gearRatioIndex");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTorque = CursorUtil.getColumnIndexOrThrow(_cursor, "torque");
          final int _cursorIndexOfSpeed = CursorUtil.getColumnIndexOrThrow(_cursor, "speed");
          final int _cursorIndexOfRotationDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "rotationDirection");
          final int _cursorIndexOfOperationMode = CursorUtil.getColumnIndexOrThrow(_cursor, "operationMode");
          final int _cursorIndexOfProgress = CursorUtil.getColumnIndexOrThrow(_cursor, "progress");
          final TaskExecution _result;
          if (_cursor.moveToFirst()) {
            final long _tmpExecutionId;
            _tmpExecutionId = _cursor.getLong(_cursorIndexOfExecutionId);
            final long _tmpTaskId;
            _tmpTaskId = _cursor.getLong(_cursorIndexOfTaskId);
            final int _tmpGearRatioIndex;
            _tmpGearRatioIndex = _cursor.getInt(_cursorIndexOfGearRatioIndex);
            final TaskStatus _tmpStatus;
            _tmpStatus = __TaskStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final double _tmpTorque;
            _tmpTorque = _cursor.getDouble(_cursorIndexOfTorque);
            final double _tmpSpeed;
            _tmpSpeed = _cursor.getDouble(_cursorIndexOfSpeed);
            final RotationDirection _tmpRotationDirection;
            _tmpRotationDirection = __RotationDirection_stringToEnum(_cursor.getString(_cursorIndexOfRotationDirection));
            final OperationMode _tmpOperationMode;
            _tmpOperationMode = __OperationMode_stringToEnum(_cursor.getString(_cursorIndexOfOperationMode));
            final int _tmpProgress;
            _tmpProgress = _cursor.getInt(_cursorIndexOfProgress);
            _result = new TaskExecution(_tmpExecutionId,_tmpTaskId,_tmpGearRatioIndex,_tmpStatus,_tmpTorque,_tmpSpeed,_tmpRotationDirection,_tmpOperationMode,_tmpProgress);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, arg2);
  }

  @Override
  public Flow<TaskExecution> getTaskExecutionByTaskIdAndIndexFlow(final long taskId,
      final int gearRatioIndex) {
    final String _sql = "SELECT * FROM task_executions WHERE taskId = ? AND gearRatioIndex = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, taskId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, gearRatioIndex);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"task_executions"}, new Callable<TaskExecution>() {
      @Override
      @Nullable
      public TaskExecution call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfExecutionId = CursorUtil.getColumnIndexOrThrow(_cursor, "executionId");
          final int _cursorIndexOfTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "taskId");
          final int _cursorIndexOfGearRatioIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "gearRatioIndex");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTorque = CursorUtil.getColumnIndexOrThrow(_cursor, "torque");
          final int _cursorIndexOfSpeed = CursorUtil.getColumnIndexOrThrow(_cursor, "speed");
          final int _cursorIndexOfRotationDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "rotationDirection");
          final int _cursorIndexOfOperationMode = CursorUtil.getColumnIndexOrThrow(_cursor, "operationMode");
          final int _cursorIndexOfProgress = CursorUtil.getColumnIndexOrThrow(_cursor, "progress");
          final TaskExecution _result;
          if (_cursor.moveToFirst()) {
            final long _tmpExecutionId;
            _tmpExecutionId = _cursor.getLong(_cursorIndexOfExecutionId);
            final long _tmpTaskId;
            _tmpTaskId = _cursor.getLong(_cursorIndexOfTaskId);
            final int _tmpGearRatioIndex;
            _tmpGearRatioIndex = _cursor.getInt(_cursorIndexOfGearRatioIndex);
            final TaskStatus _tmpStatus;
            _tmpStatus = __TaskStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final double _tmpTorque;
            _tmpTorque = _cursor.getDouble(_cursorIndexOfTorque);
            final double _tmpSpeed;
            _tmpSpeed = _cursor.getDouble(_cursorIndexOfSpeed);
            final RotationDirection _tmpRotationDirection;
            _tmpRotationDirection = __RotationDirection_stringToEnum(_cursor.getString(_cursorIndexOfRotationDirection));
            final OperationMode _tmpOperationMode;
            _tmpOperationMode = __OperationMode_stringToEnum(_cursor.getString(_cursorIndexOfOperationMode));
            final int _tmpProgress;
            _tmpProgress = _cursor.getInt(_cursorIndexOfProgress);
            _result = new TaskExecution(_tmpExecutionId,_tmpTaskId,_tmpGearRatioIndex,_tmpStatus,_tmpTorque,_tmpSpeed,_tmpRotationDirection,_tmpOperationMode,_tmpProgress);
          } else {
            _result = null;
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
  public Object getTaskExecutionByTaskId(final long taskId,
      final Continuation<? super TaskExecution> arg1) {
    final String _sql = "SELECT * FROM task_executions WHERE taskId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, taskId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TaskExecution>() {
      @Override
      @Nullable
      public TaskExecution call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfExecutionId = CursorUtil.getColumnIndexOrThrow(_cursor, "executionId");
          final int _cursorIndexOfTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "taskId");
          final int _cursorIndexOfGearRatioIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "gearRatioIndex");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTorque = CursorUtil.getColumnIndexOrThrow(_cursor, "torque");
          final int _cursorIndexOfSpeed = CursorUtil.getColumnIndexOrThrow(_cursor, "speed");
          final int _cursorIndexOfRotationDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "rotationDirection");
          final int _cursorIndexOfOperationMode = CursorUtil.getColumnIndexOrThrow(_cursor, "operationMode");
          final int _cursorIndexOfProgress = CursorUtil.getColumnIndexOrThrow(_cursor, "progress");
          final TaskExecution _result;
          if (_cursor.moveToFirst()) {
            final long _tmpExecutionId;
            _tmpExecutionId = _cursor.getLong(_cursorIndexOfExecutionId);
            final long _tmpTaskId;
            _tmpTaskId = _cursor.getLong(_cursorIndexOfTaskId);
            final int _tmpGearRatioIndex;
            _tmpGearRatioIndex = _cursor.getInt(_cursorIndexOfGearRatioIndex);
            final TaskStatus _tmpStatus;
            _tmpStatus = __TaskStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final double _tmpTorque;
            _tmpTorque = _cursor.getDouble(_cursorIndexOfTorque);
            final double _tmpSpeed;
            _tmpSpeed = _cursor.getDouble(_cursorIndexOfSpeed);
            final RotationDirection _tmpRotationDirection;
            _tmpRotationDirection = __RotationDirection_stringToEnum(_cursor.getString(_cursorIndexOfRotationDirection));
            final OperationMode _tmpOperationMode;
            _tmpOperationMode = __OperationMode_stringToEnum(_cursor.getString(_cursorIndexOfOperationMode));
            final int _tmpProgress;
            _tmpProgress = _cursor.getInt(_cursorIndexOfProgress);
            _result = new TaskExecution(_tmpExecutionId,_tmpTaskId,_tmpGearRatioIndex,_tmpStatus,_tmpTorque,_tmpSpeed,_tmpRotationDirection,_tmpOperationMode,_tmpProgress);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, arg1);
  }

  @Override
  public Flow<TaskExecution> getTaskExecutionByTaskIdFlow(final long taskId) {
    final String _sql = "SELECT * FROM task_executions WHERE taskId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, taskId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"task_executions"}, new Callable<TaskExecution>() {
      @Override
      @Nullable
      public TaskExecution call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfExecutionId = CursorUtil.getColumnIndexOrThrow(_cursor, "executionId");
          final int _cursorIndexOfTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "taskId");
          final int _cursorIndexOfGearRatioIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "gearRatioIndex");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTorque = CursorUtil.getColumnIndexOrThrow(_cursor, "torque");
          final int _cursorIndexOfSpeed = CursorUtil.getColumnIndexOrThrow(_cursor, "speed");
          final int _cursorIndexOfRotationDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "rotationDirection");
          final int _cursorIndexOfOperationMode = CursorUtil.getColumnIndexOrThrow(_cursor, "operationMode");
          final int _cursorIndexOfProgress = CursorUtil.getColumnIndexOrThrow(_cursor, "progress");
          final TaskExecution _result;
          if (_cursor.moveToFirst()) {
            final long _tmpExecutionId;
            _tmpExecutionId = _cursor.getLong(_cursorIndexOfExecutionId);
            final long _tmpTaskId;
            _tmpTaskId = _cursor.getLong(_cursorIndexOfTaskId);
            final int _tmpGearRatioIndex;
            _tmpGearRatioIndex = _cursor.getInt(_cursorIndexOfGearRatioIndex);
            final TaskStatus _tmpStatus;
            _tmpStatus = __TaskStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final double _tmpTorque;
            _tmpTorque = _cursor.getDouble(_cursorIndexOfTorque);
            final double _tmpSpeed;
            _tmpSpeed = _cursor.getDouble(_cursorIndexOfSpeed);
            final RotationDirection _tmpRotationDirection;
            _tmpRotationDirection = __RotationDirection_stringToEnum(_cursor.getString(_cursorIndexOfRotationDirection));
            final OperationMode _tmpOperationMode;
            _tmpOperationMode = __OperationMode_stringToEnum(_cursor.getString(_cursorIndexOfOperationMode));
            final int _tmpProgress;
            _tmpProgress = _cursor.getInt(_cursorIndexOfProgress);
            _result = new TaskExecution(_tmpExecutionId,_tmpTaskId,_tmpGearRatioIndex,_tmpStatus,_tmpTorque,_tmpSpeed,_tmpRotationDirection,_tmpOperationMode,_tmpProgress);
          } else {
            _result = null;
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __TaskStatus_enumToString(@NonNull final TaskStatus _value) {
    switch (_value) {
      case STOPPED: return "STOPPED";
      case RUNNING: return "RUNNING";
      case PAUSED: return "PAUSED";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private String __RotationDirection_enumToString(@NonNull final RotationDirection _value) {
    switch (_value) {
      case FORWARD: return "FORWARD";
      case REVERSE: return "REVERSE";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private String __OperationMode_enumToString(@NonNull final OperationMode _value) {
    switch (_value) {
      case JOG: return "JOG";
      case CONTINUOUS: return "CONTINUOUS";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private TaskStatus __TaskStatus_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "STOPPED": return TaskStatus.STOPPED;
      case "RUNNING": return TaskStatus.RUNNING;
      case "PAUSED": return TaskStatus.PAUSED;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }

  private RotationDirection __RotationDirection_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "FORWARD": return RotationDirection.FORWARD;
      case "REVERSE": return RotationDirection.REVERSE;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }

  private OperationMode __OperationMode_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "JOG": return OperationMode.JOG;
      case "CONTINUOUS": return OperationMode.CONTINUOUS;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
