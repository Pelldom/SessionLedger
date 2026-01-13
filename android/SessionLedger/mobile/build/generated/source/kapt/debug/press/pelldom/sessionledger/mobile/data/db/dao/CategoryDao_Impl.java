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
import press.pelldom.sessionledger.mobile.data.db.RoomConverters;
import press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CategoryDao_Impl implements CategoryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CategoryEntity> __insertionAdapterOfCategoryEntity;

  private final RoomConverters __roomConverters = new RoomConverters();

  private final EntityDeletionOrUpdateAdapter<CategoryEntity> __updateAdapterOfCategoryEntity;

  private final SharedSQLiteStatement __preparedStmtOfArchive;

  private final SharedSQLiteStatement __preparedStmtOfUnarchive;

  public CategoryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCategoryEntity = new EntityInsertionAdapter<CategoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `categories` (`id`,`name`,`archived`,`defaultHourlyRate`,`roundingMode`,`roundingDirection`,`minBillableSeconds`,`minChargeAmount`,`createdAtMs`,`updatedAtMs`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CategoryEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        final int _tmp = entity.getArchived() ? 1 : 0;
        statement.bindLong(3, _tmp);
        if (entity.getDefaultHourlyRate() == null) {
          statement.bindNull(4);
        } else {
          statement.bindDouble(4, entity.getDefaultHourlyRate());
        }
        final String _tmp_1 = __roomConverters.roundingModeToString(entity.getRoundingMode());
        if (_tmp_1 == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, _tmp_1);
        }
        final String _tmp_2 = __roomConverters.roundingDirectionToString(entity.getRoundingDirection());
        if (_tmp_2 == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, _tmp_2);
        }
        if (entity.getMinBillableSeconds() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getMinBillableSeconds());
        }
        if (entity.getMinChargeAmount() == null) {
          statement.bindNull(8);
        } else {
          statement.bindDouble(8, entity.getMinChargeAmount());
        }
        statement.bindLong(9, entity.getCreatedAtMs());
        statement.bindLong(10, entity.getUpdatedAtMs());
      }
    };
    this.__updateAdapterOfCategoryEntity = new EntityDeletionOrUpdateAdapter<CategoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `categories` SET `id` = ?,`name` = ?,`archived` = ?,`defaultHourlyRate` = ?,`roundingMode` = ?,`roundingDirection` = ?,`minBillableSeconds` = ?,`minChargeAmount` = ?,`createdAtMs` = ?,`updatedAtMs` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CategoryEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        final int _tmp = entity.getArchived() ? 1 : 0;
        statement.bindLong(3, _tmp);
        if (entity.getDefaultHourlyRate() == null) {
          statement.bindNull(4);
        } else {
          statement.bindDouble(4, entity.getDefaultHourlyRate());
        }
        final String _tmp_1 = __roomConverters.roundingModeToString(entity.getRoundingMode());
        if (_tmp_1 == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, _tmp_1);
        }
        final String _tmp_2 = __roomConverters.roundingDirectionToString(entity.getRoundingDirection());
        if (_tmp_2 == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, _tmp_2);
        }
        if (entity.getMinBillableSeconds() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getMinBillableSeconds());
        }
        if (entity.getMinChargeAmount() == null) {
          statement.bindNull(8);
        } else {
          statement.bindDouble(8, entity.getMinChargeAmount());
        }
        statement.bindLong(9, entity.getCreatedAtMs());
        statement.bindLong(10, entity.getUpdatedAtMs());
        if (entity.getId() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getId());
        }
      }
    };
    this.__preparedStmtOfArchive = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE categories SET archived = 1, updatedAtMs = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUnarchive = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE categories SET archived = 0, updatedAtMs = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final CategoryEntity category, final Continuation<? super Unit> arg1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCategoryEntity.insert(category);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, arg1);
  }

  @Override
  public Object update(final CategoryEntity category, final Continuation<? super Unit> arg1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfCategoryEntity.handle(category);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, arg1);
  }

  @Override
  public Object archive(final String id, final long updatedAtMs,
      final Continuation<? super Unit> arg2) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfArchive.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, updatedAtMs);
        _argIndex = 2;
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
          __preparedStmtOfArchive.release(_stmt);
        }
      }
    }, arg2);
  }

  @Override
  public Object unarchive(final String id, final long updatedAtMs,
      final Continuation<? super Unit> arg2) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUnarchive.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, updatedAtMs);
        _argIndex = 2;
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
          __preparedStmtOfUnarchive.release(_stmt);
        }
      }
    }, arg2);
  }

  @Override
  public Flow<List<CategoryEntity>> observeActiveCategories() {
    final String _sql = "SELECT * FROM categories WHERE archived = 0 ORDER BY name COLLATE NOCASE ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"categories"}, new Callable<List<CategoryEntity>>() {
      @Override
      @NonNull
      public List<CategoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
          final int _cursorIndexOfDefaultHourlyRate = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultHourlyRate");
          final int _cursorIndexOfRoundingMode = CursorUtil.getColumnIndexOrThrow(_cursor, "roundingMode");
          final int _cursorIndexOfRoundingDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "roundingDirection");
          final int _cursorIndexOfMinBillableSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "minBillableSeconds");
          final int _cursorIndexOfMinChargeAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "minChargeAmount");
          final int _cursorIndexOfCreatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMs");
          final int _cursorIndexOfUpdatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtMs");
          final List<CategoryEntity> _result = new ArrayList<CategoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CategoryEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final boolean _tmpArchived;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfArchived);
            _tmpArchived = _tmp != 0;
            final Double _tmpDefaultHourlyRate;
            if (_cursor.isNull(_cursorIndexOfDefaultHourlyRate)) {
              _tmpDefaultHourlyRate = null;
            } else {
              _tmpDefaultHourlyRate = _cursor.getDouble(_cursorIndexOfDefaultHourlyRate);
            }
            final RoundingMode _tmpRoundingMode;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfRoundingMode)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfRoundingMode);
            }
            _tmpRoundingMode = __roomConverters.stringToRoundingMode(_tmp_1);
            final RoundingDirection _tmpRoundingDirection;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfRoundingDirection)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfRoundingDirection);
            }
            _tmpRoundingDirection = __roomConverters.stringToRoundingDirection(_tmp_2);
            final Long _tmpMinBillableSeconds;
            if (_cursor.isNull(_cursorIndexOfMinBillableSeconds)) {
              _tmpMinBillableSeconds = null;
            } else {
              _tmpMinBillableSeconds = _cursor.getLong(_cursorIndexOfMinBillableSeconds);
            }
            final Double _tmpMinChargeAmount;
            if (_cursor.isNull(_cursorIndexOfMinChargeAmount)) {
              _tmpMinChargeAmount = null;
            } else {
              _tmpMinChargeAmount = _cursor.getDouble(_cursorIndexOfMinChargeAmount);
            }
            final long _tmpCreatedAtMs;
            _tmpCreatedAtMs = _cursor.getLong(_cursorIndexOfCreatedAtMs);
            final long _tmpUpdatedAtMs;
            _tmpUpdatedAtMs = _cursor.getLong(_cursorIndexOfUpdatedAtMs);
            _item = new CategoryEntity(_tmpId,_tmpName,_tmpArchived,_tmpDefaultHourlyRate,_tmpRoundingMode,_tmpRoundingDirection,_tmpMinBillableSeconds,_tmpMinChargeAmount,_tmpCreatedAtMs,_tmpUpdatedAtMs);
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
  public Flow<List<CategoryEntity>> observeAllCategories() {
    final String _sql = "SELECT * FROM categories ORDER BY archived ASC, name COLLATE NOCASE ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"categories"}, new Callable<List<CategoryEntity>>() {
      @Override
      @NonNull
      public List<CategoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
          final int _cursorIndexOfDefaultHourlyRate = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultHourlyRate");
          final int _cursorIndexOfRoundingMode = CursorUtil.getColumnIndexOrThrow(_cursor, "roundingMode");
          final int _cursorIndexOfRoundingDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "roundingDirection");
          final int _cursorIndexOfMinBillableSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "minBillableSeconds");
          final int _cursorIndexOfMinChargeAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "minChargeAmount");
          final int _cursorIndexOfCreatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMs");
          final int _cursorIndexOfUpdatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtMs");
          final List<CategoryEntity> _result = new ArrayList<CategoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CategoryEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final boolean _tmpArchived;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfArchived);
            _tmpArchived = _tmp != 0;
            final Double _tmpDefaultHourlyRate;
            if (_cursor.isNull(_cursorIndexOfDefaultHourlyRate)) {
              _tmpDefaultHourlyRate = null;
            } else {
              _tmpDefaultHourlyRate = _cursor.getDouble(_cursorIndexOfDefaultHourlyRate);
            }
            final RoundingMode _tmpRoundingMode;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfRoundingMode)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfRoundingMode);
            }
            _tmpRoundingMode = __roomConverters.stringToRoundingMode(_tmp_1);
            final RoundingDirection _tmpRoundingDirection;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfRoundingDirection)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfRoundingDirection);
            }
            _tmpRoundingDirection = __roomConverters.stringToRoundingDirection(_tmp_2);
            final Long _tmpMinBillableSeconds;
            if (_cursor.isNull(_cursorIndexOfMinBillableSeconds)) {
              _tmpMinBillableSeconds = null;
            } else {
              _tmpMinBillableSeconds = _cursor.getLong(_cursorIndexOfMinBillableSeconds);
            }
            final Double _tmpMinChargeAmount;
            if (_cursor.isNull(_cursorIndexOfMinChargeAmount)) {
              _tmpMinChargeAmount = null;
            } else {
              _tmpMinChargeAmount = _cursor.getDouble(_cursorIndexOfMinChargeAmount);
            }
            final long _tmpCreatedAtMs;
            _tmpCreatedAtMs = _cursor.getLong(_cursorIndexOfCreatedAtMs);
            final long _tmpUpdatedAtMs;
            _tmpUpdatedAtMs = _cursor.getLong(_cursorIndexOfUpdatedAtMs);
            _item = new CategoryEntity(_tmpId,_tmpName,_tmpArchived,_tmpDefaultHourlyRate,_tmpRoundingMode,_tmpRoundingDirection,_tmpMinBillableSeconds,_tmpMinChargeAmount,_tmpCreatedAtMs,_tmpUpdatedAtMs);
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
  public Object getById(final String id, final Continuation<? super CategoryEntity> arg1) {
    final String _sql = "SELECT * FROM categories WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (id == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, id);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CategoryEntity>() {
      @Override
      @Nullable
      public CategoryEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "archived");
          final int _cursorIndexOfDefaultHourlyRate = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultHourlyRate");
          final int _cursorIndexOfRoundingMode = CursorUtil.getColumnIndexOrThrow(_cursor, "roundingMode");
          final int _cursorIndexOfRoundingDirection = CursorUtil.getColumnIndexOrThrow(_cursor, "roundingDirection");
          final int _cursorIndexOfMinBillableSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "minBillableSeconds");
          final int _cursorIndexOfMinChargeAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "minChargeAmount");
          final int _cursorIndexOfCreatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMs");
          final int _cursorIndexOfUpdatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtMs");
          final CategoryEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final boolean _tmpArchived;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfArchived);
            _tmpArchived = _tmp != 0;
            final Double _tmpDefaultHourlyRate;
            if (_cursor.isNull(_cursorIndexOfDefaultHourlyRate)) {
              _tmpDefaultHourlyRate = null;
            } else {
              _tmpDefaultHourlyRate = _cursor.getDouble(_cursorIndexOfDefaultHourlyRate);
            }
            final RoundingMode _tmpRoundingMode;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfRoundingMode)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfRoundingMode);
            }
            _tmpRoundingMode = __roomConverters.stringToRoundingMode(_tmp_1);
            final RoundingDirection _tmpRoundingDirection;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfRoundingDirection)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfRoundingDirection);
            }
            _tmpRoundingDirection = __roomConverters.stringToRoundingDirection(_tmp_2);
            final Long _tmpMinBillableSeconds;
            if (_cursor.isNull(_cursorIndexOfMinBillableSeconds)) {
              _tmpMinBillableSeconds = null;
            } else {
              _tmpMinBillableSeconds = _cursor.getLong(_cursorIndexOfMinBillableSeconds);
            }
            final Double _tmpMinChargeAmount;
            if (_cursor.isNull(_cursorIndexOfMinChargeAmount)) {
              _tmpMinChargeAmount = null;
            } else {
              _tmpMinChargeAmount = _cursor.getDouble(_cursorIndexOfMinChargeAmount);
            }
            final long _tmpCreatedAtMs;
            _tmpCreatedAtMs = _cursor.getLong(_cursorIndexOfCreatedAtMs);
            final long _tmpUpdatedAtMs;
            _tmpUpdatedAtMs = _cursor.getLong(_cursorIndexOfUpdatedAtMs);
            _result = new CategoryEntity(_tmpId,_tmpName,_tmpArchived,_tmpDefaultHourlyRate,_tmpRoundingMode,_tmpRoundingDirection,_tmpMinBillableSeconds,_tmpMinChargeAmount,_tmpCreatedAtMs,_tmpUpdatedAtMs);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
