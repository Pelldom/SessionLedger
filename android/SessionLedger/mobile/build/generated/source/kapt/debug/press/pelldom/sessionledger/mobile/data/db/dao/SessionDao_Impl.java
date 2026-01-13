package press.pelldom.sessionledger.mobile.data.db.dao;

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
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
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
import press.pelldom.sessionledger.mobile.billing.RoundingDirection;
import press.pelldom.sessionledger.mobile.billing.RoundingMode;
import press.pelldom.sessionledger.mobile.billing.SessionState;
import press.pelldom.sessionledger.mobile.data.db.RoomConverters;
import press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SessionDao_Impl implements SessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SessionEntity> __insertionAdapterOfSessionEntity;

  private final RoomConverters __roomConverters = new RoomConverters();

  private final EntityDeletionOrUpdateAdapter<SessionEntity> __updateAdapterOfSessionEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public SessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSessionEntity = new EntityInsertionAdapter<SessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `sessions` (`id`,`startTimeMs`,`endTimeMs`,`state`,`pausedTotalMs`,`lastStateChangeTimeMs`,`categoryId`,`notes`,`hourlyRateOverride`,`roundingModeOverride`,`roundingDirectionOverride`,`minBillableSecondsOverride`,`minChargeAmountOverride`,`createdOnDevice`,`updatedAtMs`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SessionEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        statement.bindLong(2, entity.getStartTimeMs());
        if (entity.getEndTimeMs() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getEndTimeMs());
        }
        final String _tmp = __roomConverters.sessionStateToString(entity.getState());
        if (_tmp == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, _tmp);
        }
        statement.bindLong(5, entity.getPausedTotalMs());
        statement.bindLong(6, entity.getLastStateChangeTimeMs());
        if (entity.getCategoryId() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getCategoryId());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getNotes());
        }
        if (entity.getHourlyRateOverride() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getHourlyRateOverride());
        }
        final String _tmp_1 = __roomConverters.roundingModeToString(entity.getRoundingModeOverride());
        if (_tmp_1 == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, _tmp_1);
        }
        final String _tmp_2 = __roomConverters.roundingDirectionToString(entity.getRoundingDirectionOverride());
        if (_tmp_2 == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, _tmp_2);
        }
        if (entity.getMinBillableSecondsOverride() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getMinBillableSecondsOverride());
        }
        if (entity.getMinChargeAmountOverride() == null) {
          statement.bindNull(13);
        } else {
          statement.bindDouble(13, entity.getMinChargeAmountOverride());
        }
        if (entity.getCreatedOnDevice() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getCreatedOnDevice());
        }
        statement.bindLong(15, entity.getUpdatedAtMs());
      }
    };
    this.__updateAdapterOfSessionEntity = new EntityDeletionOrUpdateAdapter<SessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `sessions` SET `id` = ?,`startTimeMs` = ?,`endTimeMs` = ?,`state` = ?,`pausedTotalMs` = ?,`lastStateChangeTimeMs` = ?,`categoryId` = ?,`notes` = ?,`hourlyRateOverride` = ?,`roundingModeOverride` = ?,`roundingDirectionOverride` = ?,`minBillableSecondsOverride` = ?,`minChargeAmountOverride` = ?,`createdOnDevice` = ?,`updatedAtMs` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SessionEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        statement.bindLong(2, entity.getStartTimeMs());
        if (entity.getEndTimeMs() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getEndTimeMs());
        }
        final String _tmp = __roomConverters.sessionStateToString(entity.getState());
        if (_tmp == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, _tmp);
        }
        statement.bindLong(5, entity.getPausedTotalMs());
        statement.bindLong(6, entity.getLastStateChangeTimeMs());
        if (entity.getCategoryId() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getCategoryId());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getNotes());
        }
        if (entity.getHourlyRateOverride() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getHourlyRateOverride());
        }
        final String _tmp_1 = __roomConverters.roundingModeToString(entity.getRoundingModeOverride());
        if (_tmp_1 == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, _tmp_1);
        }
        final String _tmp_2 = __roomConverters.roundingDirectionToString(entity.getRoundingDirectionOverride());
        if (_tmp_2 == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, _tmp_2);
        }
        if (entity.getMinBillableSecondsOverride() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getMinBillableSecondsOverride());
        }
        if (entity.getMinChargeAmountOverride() == null) {
          statement.bindNull(13);
        } else {
          statement.bindDouble(13, entity.getMinChargeAmountOverride());
        }
        if (entity.getCreatedOnDevice() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getCreatedOnDevice());
        }
        statement.bindLong(15, entity.getUpdatedAtMs());
        if (entity.getId() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getId());
        }
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sessions WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final SessionEntity session, final Continuation<? super Unit> arg1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSessionEntity.insert(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, arg1);
  }

  @Override
  public Object update(final SessionEntity session, final Continuation<? super Unit> arg1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSessionEntity.handle(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, arg1);
  }

  @Override
  public Object deleteById(final String id, final Continuation<? super Unit> arg1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, id);
        }
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
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, arg1);
  }

  @Override
  public Flow<SessionEntity> observeActiveSession() {
    final String _sql = "SELECT * FROM sessions WHERE state != 'ENDED' ORDER BY startTimeMs DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sessions"}, new Callable<SessionEntity>() {
      @Override
      @Nullable
      public SessionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfStartTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeMs");
          final int _cursorIndexOfEndTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeMs");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfPausedTotalMs = CursorUtil.getColumnIndexOrThrow(_cursor, "pausedTotalMs");
          final int _cursorIndexOfLastStateChangeTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "lastStateChangeTimeMs");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfHourlyRateOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "hourlyRateOverride");
          final int _cursorIndexOfRoundingModeOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "roundingModeOverride");
          final int _cursorIndexOfRoundingDirectionOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "roundingDirectionOverride");
          final int _cursorIndexOfMinBillableSecondsOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "minBillableSecondsOverride");
          final int _cursorIndexOfMinChargeAmountOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "minChargeAmountOverride");
          final int _cursorIndexOfCreatedOnDevice = CursorUtil.getColumnIndexOrThrow(_cursor, "createdOnDevice");
          final int _cursorIndexOfUpdatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtMs");
          final SessionEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final long _tmpStartTimeMs;
            _tmpStartTimeMs = _cursor.getLong(_cursorIndexOfStartTimeMs);
            final Long _tmpEndTimeMs;
            if (_cursor.isNull(_cursorIndexOfEndTimeMs)) {
              _tmpEndTimeMs = null;
            } else {
              _tmpEndTimeMs = _cursor.getLong(_cursorIndexOfEndTimeMs);
            }
            final SessionState _tmpState;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfState)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfState);
            }
            _tmpState = __roomConverters.stringToSessionState(_tmp);
            final long _tmpPausedTotalMs;
            _tmpPausedTotalMs = _cursor.getLong(_cursorIndexOfPausedTotalMs);
            final long _tmpLastStateChangeTimeMs;
            _tmpLastStateChangeTimeMs = _cursor.getLong(_cursorIndexOfLastStateChangeTimeMs);
            final String _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getString(_cursorIndexOfCategoryId);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final Double _tmpHourlyRateOverride;
            if (_cursor.isNull(_cursorIndexOfHourlyRateOverride)) {
              _tmpHourlyRateOverride = null;
            } else {
              _tmpHourlyRateOverride = _cursor.getDouble(_cursorIndexOfHourlyRateOverride);
            }
            final RoundingMode _tmpRoundingModeOverride;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfRoundingModeOverride)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfRoundingModeOverride);
            }
            _tmpRoundingModeOverride = __roomConverters.stringToRoundingMode(_tmp_1);
            final RoundingDirection _tmpRoundingDirectionOverride;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfRoundingDirectionOverride)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfRoundingDirectionOverride);
            }
            _tmpRoundingDirectionOverride = __roomConverters.stringToRoundingDirection(_tmp_2);
            final Long _tmpMinBillableSecondsOverride;
            if (_cursor.isNull(_cursorIndexOfMinBillableSecondsOverride)) {
              _tmpMinBillableSecondsOverride = null;
            } else {
              _tmpMinBillableSecondsOverride = _cursor.getLong(_cursorIndexOfMinBillableSecondsOverride);
            }
            final Double _tmpMinChargeAmountOverride;
            if (_cursor.isNull(_cursorIndexOfMinChargeAmountOverride)) {
              _tmpMinChargeAmountOverride = null;
            } else {
              _tmpMinChargeAmountOverride = _cursor.getDouble(_cursorIndexOfMinChargeAmountOverride);
            }
            final String _tmpCreatedOnDevice;
            if (_cursor.isNull(_cursorIndexOfCreatedOnDevice)) {
              _tmpCreatedOnDevice = null;
            } else {
              _tmpCreatedOnDevice = _cursor.getString(_cursorIndexOfCreatedOnDevice);
            }
            final long _tmpUpdatedAtMs;
            _tmpUpdatedAtMs = _cursor.getLong(_cursorIndexOfUpdatedAtMs);
            _result = new SessionEntity(_tmpId,_tmpStartTimeMs,_tmpEndTimeMs,_tmpState,_tmpPausedTotalMs,_tmpLastStateChangeTimeMs,_tmpCategoryId,_tmpNotes,_tmpHourlyRateOverride,_tmpRoundingModeOverride,_tmpRoundingDirectionOverride,_tmpMinBillableSecondsOverride,_tmpMinChargeAmountOverride,_tmpCreatedOnDevice,_tmpUpdatedAtMs);
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
  public Object getActiveSession(final Continuation<? super SessionEntity> arg0) {
    final String _sql = "SELECT * FROM sessions WHERE state != 'ENDED' ORDER BY startTimeMs DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SessionEntity>() {
      @Override
      @Nullable
      public SessionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfStartTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeMs");
          final int _cursorIndexOfEndTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeMs");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfPausedTotalMs = CursorUtil.getColumnIndexOrThrow(_cursor, "pausedTotalMs");
          final int _cursorIndexOfLastStateChangeTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "lastStateChangeTimeMs");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfHourlyRateOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "hourlyRateOverride");
          final int _cursorIndexOfRoundingModeOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "roundingModeOverride");
          final int _cursorIndexOfRoundingDirectionOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "roundingDirectionOverride");
          final int _cursorIndexOfMinBillableSecondsOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "minBillableSecondsOverride");
          final int _cursorIndexOfMinChargeAmountOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "minChargeAmountOverride");
          final int _cursorIndexOfCreatedOnDevice = CursorUtil.getColumnIndexOrThrow(_cursor, "createdOnDevice");
          final int _cursorIndexOfUpdatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtMs");
          final SessionEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final long _tmpStartTimeMs;
            _tmpStartTimeMs = _cursor.getLong(_cursorIndexOfStartTimeMs);
            final Long _tmpEndTimeMs;
            if (_cursor.isNull(_cursorIndexOfEndTimeMs)) {
              _tmpEndTimeMs = null;
            } else {
              _tmpEndTimeMs = _cursor.getLong(_cursorIndexOfEndTimeMs);
            }
            final SessionState _tmpState;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfState)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfState);
            }
            _tmpState = __roomConverters.stringToSessionState(_tmp);
            final long _tmpPausedTotalMs;
            _tmpPausedTotalMs = _cursor.getLong(_cursorIndexOfPausedTotalMs);
            final long _tmpLastStateChangeTimeMs;
            _tmpLastStateChangeTimeMs = _cursor.getLong(_cursorIndexOfLastStateChangeTimeMs);
            final String _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getString(_cursorIndexOfCategoryId);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final Double _tmpHourlyRateOverride;
            if (_cursor.isNull(_cursorIndexOfHourlyRateOverride)) {
              _tmpHourlyRateOverride = null;
            } else {
              _tmpHourlyRateOverride = _cursor.getDouble(_cursorIndexOfHourlyRateOverride);
            }
            final RoundingMode _tmpRoundingModeOverride;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfRoundingModeOverride)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfRoundingModeOverride);
            }
            _tmpRoundingModeOverride = __roomConverters.stringToRoundingMode(_tmp_1);
            final RoundingDirection _tmpRoundingDirectionOverride;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfRoundingDirectionOverride)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfRoundingDirectionOverride);
            }
            _tmpRoundingDirectionOverride = __roomConverters.stringToRoundingDirection(_tmp_2);
            final Long _tmpMinBillableSecondsOverride;
            if (_cursor.isNull(_cursorIndexOfMinBillableSecondsOverride)) {
              _tmpMinBillableSecondsOverride = null;
            } else {
              _tmpMinBillableSecondsOverride = _cursor.getLong(_cursorIndexOfMinBillableSecondsOverride);
            }
            final Double _tmpMinChargeAmountOverride;
            if (_cursor.isNull(_cursorIndexOfMinChargeAmountOverride)) {
              _tmpMinChargeAmountOverride = null;
            } else {
              _tmpMinChargeAmountOverride = _cursor.getDouble(_cursorIndexOfMinChargeAmountOverride);
            }
            final String _tmpCreatedOnDevice;
            if (_cursor.isNull(_cursorIndexOfCreatedOnDevice)) {
              _tmpCreatedOnDevice = null;
            } else {
              _tmpCreatedOnDevice = _cursor.getString(_cursorIndexOfCreatedOnDevice);
            }
            final long _tmpUpdatedAtMs;
            _tmpUpdatedAtMs = _cursor.getLong(_cursorIndexOfUpdatedAtMs);
            _result = new SessionEntity(_tmpId,_tmpStartTimeMs,_tmpEndTimeMs,_tmpState,_tmpPausedTotalMs,_tmpLastStateChangeTimeMs,_tmpCategoryId,_tmpNotes,_tmpHourlyRateOverride,_tmpRoundingModeOverride,_tmpRoundingDirectionOverride,_tmpMinBillableSecondsOverride,_tmpMinChargeAmountOverride,_tmpCreatedOnDevice,_tmpUpdatedAtMs);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, arg0);
  }

  @Override
  public Object getById(final String id, final Continuation<? super SessionEntity> arg1) {
    final String _sql = "SELECT * FROM sessions WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (id == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, id);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SessionEntity>() {
      @Override
      @Nullable
      public SessionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfStartTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeMs");
          final int _cursorIndexOfEndTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeMs");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfPausedTotalMs = CursorUtil.getColumnIndexOrThrow(_cursor, "pausedTotalMs");
          final int _cursorIndexOfLastStateChangeTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "lastStateChangeTimeMs");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfHourlyRateOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "hourlyRateOverride");
          final int _cursorIndexOfRoundingModeOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "roundingModeOverride");
          final int _cursorIndexOfRoundingDirectionOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "roundingDirectionOverride");
          final int _cursorIndexOfMinBillableSecondsOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "minBillableSecondsOverride");
          final int _cursorIndexOfMinChargeAmountOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "minChargeAmountOverride");
          final int _cursorIndexOfCreatedOnDevice = CursorUtil.getColumnIndexOrThrow(_cursor, "createdOnDevice");
          final int _cursorIndexOfUpdatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtMs");
          final SessionEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final long _tmpStartTimeMs;
            _tmpStartTimeMs = _cursor.getLong(_cursorIndexOfStartTimeMs);
            final Long _tmpEndTimeMs;
            if (_cursor.isNull(_cursorIndexOfEndTimeMs)) {
              _tmpEndTimeMs = null;
            } else {
              _tmpEndTimeMs = _cursor.getLong(_cursorIndexOfEndTimeMs);
            }
            final SessionState _tmpState;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfState)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfState);
            }
            _tmpState = __roomConverters.stringToSessionState(_tmp);
            final long _tmpPausedTotalMs;
            _tmpPausedTotalMs = _cursor.getLong(_cursorIndexOfPausedTotalMs);
            final long _tmpLastStateChangeTimeMs;
            _tmpLastStateChangeTimeMs = _cursor.getLong(_cursorIndexOfLastStateChangeTimeMs);
            final String _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getString(_cursorIndexOfCategoryId);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final Double _tmpHourlyRateOverride;
            if (_cursor.isNull(_cursorIndexOfHourlyRateOverride)) {
              _tmpHourlyRateOverride = null;
            } else {
              _tmpHourlyRateOverride = _cursor.getDouble(_cursorIndexOfHourlyRateOverride);
            }
            final RoundingMode _tmpRoundingModeOverride;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfRoundingModeOverride)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfRoundingModeOverride);
            }
            _tmpRoundingModeOverride = __roomConverters.stringToRoundingMode(_tmp_1);
            final RoundingDirection _tmpRoundingDirectionOverride;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfRoundingDirectionOverride)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfRoundingDirectionOverride);
            }
            _tmpRoundingDirectionOverride = __roomConverters.stringToRoundingDirection(_tmp_2);
            final Long _tmpMinBillableSecondsOverride;
            if (_cursor.isNull(_cursorIndexOfMinBillableSecondsOverride)) {
              _tmpMinBillableSecondsOverride = null;
            } else {
              _tmpMinBillableSecondsOverride = _cursor.getLong(_cursorIndexOfMinBillableSecondsOverride);
            }
            final Double _tmpMinChargeAmountOverride;
            if (_cursor.isNull(_cursorIndexOfMinChargeAmountOverride)) {
              _tmpMinChargeAmountOverride = null;
            } else {
              _tmpMinChargeAmountOverride = _cursor.getDouble(_cursorIndexOfMinChargeAmountOverride);
            }
            final String _tmpCreatedOnDevice;
            if (_cursor.isNull(_cursorIndexOfCreatedOnDevice)) {
              _tmpCreatedOnDevice = null;
            } else {
              _tmpCreatedOnDevice = _cursor.getString(_cursorIndexOfCreatedOnDevice);
            }
            final long _tmpUpdatedAtMs;
            _tmpUpdatedAtMs = _cursor.getLong(_cursorIndexOfUpdatedAtMs);
            _result = new SessionEntity(_tmpId,_tmpStartTimeMs,_tmpEndTimeMs,_tmpState,_tmpPausedTotalMs,_tmpLastStateChangeTimeMs,_tmpCategoryId,_tmpNotes,_tmpHourlyRateOverride,_tmpRoundingModeOverride,_tmpRoundingDirectionOverride,_tmpMinBillableSecondsOverride,_tmpMinChargeAmountOverride,_tmpCreatedOnDevice,_tmpUpdatedAtMs);
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
  public Flow<List<SessionEntity>> observeEndedSessionsNewestFirst() {
    final String _sql = "SELECT * FROM sessions WHERE state = 'ENDED' ORDER BY startTimeMs DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"sessions"}, new Callable<List<SessionEntity>>() {
      @Override
      @NonNull
      public List<SessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfStartTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeMs");
          final int _cursorIndexOfEndTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeMs");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfPausedTotalMs = CursorUtil.getColumnIndexOrThrow(_cursor, "pausedTotalMs");
          final int _cursorIndexOfLastStateChangeTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "lastStateChangeTimeMs");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfHourlyRateOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "hourlyRateOverride");
          final int _cursorIndexOfRoundingModeOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "roundingModeOverride");
          final int _cursorIndexOfRoundingDirectionOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "roundingDirectionOverride");
          final int _cursorIndexOfMinBillableSecondsOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "minBillableSecondsOverride");
          final int _cursorIndexOfMinChargeAmountOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "minChargeAmountOverride");
          final int _cursorIndexOfCreatedOnDevice = CursorUtil.getColumnIndexOrThrow(_cursor, "createdOnDevice");
          final int _cursorIndexOfUpdatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtMs");
          final List<SessionEntity> _result = new ArrayList<SessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SessionEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final long _tmpStartTimeMs;
            _tmpStartTimeMs = _cursor.getLong(_cursorIndexOfStartTimeMs);
            final Long _tmpEndTimeMs;
            if (_cursor.isNull(_cursorIndexOfEndTimeMs)) {
              _tmpEndTimeMs = null;
            } else {
              _tmpEndTimeMs = _cursor.getLong(_cursorIndexOfEndTimeMs);
            }
            final SessionState _tmpState;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfState)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfState);
            }
            _tmpState = __roomConverters.stringToSessionState(_tmp);
            final long _tmpPausedTotalMs;
            _tmpPausedTotalMs = _cursor.getLong(_cursorIndexOfPausedTotalMs);
            final long _tmpLastStateChangeTimeMs;
            _tmpLastStateChangeTimeMs = _cursor.getLong(_cursorIndexOfLastStateChangeTimeMs);
            final String _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getString(_cursorIndexOfCategoryId);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final Double _tmpHourlyRateOverride;
            if (_cursor.isNull(_cursorIndexOfHourlyRateOverride)) {
              _tmpHourlyRateOverride = null;
            } else {
              _tmpHourlyRateOverride = _cursor.getDouble(_cursorIndexOfHourlyRateOverride);
            }
            final RoundingMode _tmpRoundingModeOverride;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfRoundingModeOverride)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfRoundingModeOverride);
            }
            _tmpRoundingModeOverride = __roomConverters.stringToRoundingMode(_tmp_1);
            final RoundingDirection _tmpRoundingDirectionOverride;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfRoundingDirectionOverride)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfRoundingDirectionOverride);
            }
            _tmpRoundingDirectionOverride = __roomConverters.stringToRoundingDirection(_tmp_2);
            final Long _tmpMinBillableSecondsOverride;
            if (_cursor.isNull(_cursorIndexOfMinBillableSecondsOverride)) {
              _tmpMinBillableSecondsOverride = null;
            } else {
              _tmpMinBillableSecondsOverride = _cursor.getLong(_cursorIndexOfMinBillableSecondsOverride);
            }
            final Double _tmpMinChargeAmountOverride;
            if (_cursor.isNull(_cursorIndexOfMinChargeAmountOverride)) {
              _tmpMinChargeAmountOverride = null;
            } else {
              _tmpMinChargeAmountOverride = _cursor.getDouble(_cursorIndexOfMinChargeAmountOverride);
            }
            final String _tmpCreatedOnDevice;
            if (_cursor.isNull(_cursorIndexOfCreatedOnDevice)) {
              _tmpCreatedOnDevice = null;
            } else {
              _tmpCreatedOnDevice = _cursor.getString(_cursorIndexOfCreatedOnDevice);
            }
            final long _tmpUpdatedAtMs;
            _tmpUpdatedAtMs = _cursor.getLong(_cursorIndexOfUpdatedAtMs);
            _item = new SessionEntity(_tmpId,_tmpStartTimeMs,_tmpEndTimeMs,_tmpState,_tmpPausedTotalMs,_tmpLastStateChangeTimeMs,_tmpCategoryId,_tmpNotes,_tmpHourlyRateOverride,_tmpRoundingModeOverride,_tmpRoundingDirectionOverride,_tmpMinBillableSecondsOverride,_tmpMinChargeAmountOverride,_tmpCreatedOnDevice,_tmpUpdatedAtMs);
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
  public Object getEndedSessionsInRange(final long startMs, final long endMs,
      final Continuation<? super List<SessionEntity>> arg2) {
    final String _sql = "\n"
            + "        SELECT * FROM sessions\n"
            + "        WHERE state = 'ENDED'\n"
            + "          AND startTimeMs >= ?\n"
            + "          AND startTimeMs < ?\n"
            + "        ORDER BY startTimeMs ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startMs);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endMs);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SessionEntity>>() {
      @Override
      @NonNull
      public List<SessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfStartTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeMs");
          final int _cursorIndexOfEndTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeMs");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfPausedTotalMs = CursorUtil.getColumnIndexOrThrow(_cursor, "pausedTotalMs");
          final int _cursorIndexOfLastStateChangeTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "lastStateChangeTimeMs");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfHourlyRateOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "hourlyRateOverride");
          final int _cursorIndexOfRoundingModeOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "roundingModeOverride");
          final int _cursorIndexOfRoundingDirectionOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "roundingDirectionOverride");
          final int _cursorIndexOfMinBillableSecondsOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "minBillableSecondsOverride");
          final int _cursorIndexOfMinChargeAmountOverride = CursorUtil.getColumnIndexOrThrow(_cursor, "minChargeAmountOverride");
          final int _cursorIndexOfCreatedOnDevice = CursorUtil.getColumnIndexOrThrow(_cursor, "createdOnDevice");
          final int _cursorIndexOfUpdatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtMs");
          final List<SessionEntity> _result = new ArrayList<SessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SessionEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final long _tmpStartTimeMs;
            _tmpStartTimeMs = _cursor.getLong(_cursorIndexOfStartTimeMs);
            final Long _tmpEndTimeMs;
            if (_cursor.isNull(_cursorIndexOfEndTimeMs)) {
              _tmpEndTimeMs = null;
            } else {
              _tmpEndTimeMs = _cursor.getLong(_cursorIndexOfEndTimeMs);
            }
            final SessionState _tmpState;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfState)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfState);
            }
            _tmpState = __roomConverters.stringToSessionState(_tmp);
            final long _tmpPausedTotalMs;
            _tmpPausedTotalMs = _cursor.getLong(_cursorIndexOfPausedTotalMs);
            final long _tmpLastStateChangeTimeMs;
            _tmpLastStateChangeTimeMs = _cursor.getLong(_cursorIndexOfLastStateChangeTimeMs);
            final String _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getString(_cursorIndexOfCategoryId);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final Double _tmpHourlyRateOverride;
            if (_cursor.isNull(_cursorIndexOfHourlyRateOverride)) {
              _tmpHourlyRateOverride = null;
            } else {
              _tmpHourlyRateOverride = _cursor.getDouble(_cursorIndexOfHourlyRateOverride);
            }
            final RoundingMode _tmpRoundingModeOverride;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfRoundingModeOverride)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfRoundingModeOverride);
            }
            _tmpRoundingModeOverride = __roomConverters.stringToRoundingMode(_tmp_1);
            final RoundingDirection _tmpRoundingDirectionOverride;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfRoundingDirectionOverride)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfRoundingDirectionOverride);
            }
            _tmpRoundingDirectionOverride = __roomConverters.stringToRoundingDirection(_tmp_2);
            final Long _tmpMinBillableSecondsOverride;
            if (_cursor.isNull(_cursorIndexOfMinBillableSecondsOverride)) {
              _tmpMinBillableSecondsOverride = null;
            } else {
              _tmpMinBillableSecondsOverride = _cursor.getLong(_cursorIndexOfMinBillableSecondsOverride);
            }
            final Double _tmpMinChargeAmountOverride;
            if (_cursor.isNull(_cursorIndexOfMinChargeAmountOverride)) {
              _tmpMinChargeAmountOverride = null;
            } else {
              _tmpMinChargeAmountOverride = _cursor.getDouble(_cursorIndexOfMinChargeAmountOverride);
            }
            final String _tmpCreatedOnDevice;
            if (_cursor.isNull(_cursorIndexOfCreatedOnDevice)) {
              _tmpCreatedOnDevice = null;
            } else {
              _tmpCreatedOnDevice = _cursor.getString(_cursorIndexOfCreatedOnDevice);
            }
            final long _tmpUpdatedAtMs;
            _tmpUpdatedAtMs = _cursor.getLong(_cursorIndexOfUpdatedAtMs);
            _item = new SessionEntity(_tmpId,_tmpStartTimeMs,_tmpEndTimeMs,_tmpState,_tmpPausedTotalMs,_tmpLastStateChangeTimeMs,_tmpCategoryId,_tmpNotes,_tmpHourlyRateOverride,_tmpRoundingModeOverride,_tmpRoundingDirectionOverride,_tmpMinBillableSecondsOverride,_tmpMinChargeAmountOverride,_tmpCreatedOnDevice,_tmpUpdatedAtMs);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, arg2);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
