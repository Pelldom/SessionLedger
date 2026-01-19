package press.pelldom.sessionledger.mobile.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import press.pelldom.sessionledger.mobile.data.db.dao.CategoryDao
import press.pelldom.sessionledger.mobile.data.db.dao.SessionDao
import press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity
import press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity

@Database(
    entities = [CategoryEntity::class, SessionEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sessionledger.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            ensureDefaultCategory(db)
                        }
                    })
                    .build()
                    .also { instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Categories: add isDefault
                db.execSQL("ALTER TABLE categories ADD COLUMN isDefault INTEGER NOT NULL DEFAULT 0")

                // Ensure exactly one default "Uncategorized" category (stable UUID).
                ensureDefaultCategory(db)

                // Sessions: rebuild table to make categoryId NOT NULL and default to Uncategorized.
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sessions_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        startTimeMs INTEGER NOT NULL,
                        endTimeMs INTEGER,
                        state TEXT NOT NULL,
                        pausedTotalMs INTEGER NOT NULL,
                        lastStateChangeTimeMs INTEGER NOT NULL,
                        categoryId TEXT NOT NULL,
                        notes TEXT,
                        hourlyRateOverride REAL,
                        roundingModeOverride TEXT,
                        roundingDirectionOverride TEXT,
                        minBillableSecondsOverride INTEGER,
                        minChargeAmountOverride REAL,
                        createdOnDevice TEXT NOT NULL,
                        updatedAtMs INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    INSERT INTO sessions_new (
                        id, startTimeMs, endTimeMs, state, pausedTotalMs, lastStateChangeTimeMs,
                        categoryId, notes, hourlyRateOverride, roundingModeOverride, roundingDirectionOverride,
                        minBillableSecondsOverride, minChargeAmountOverride, createdOnDevice, updatedAtMs
                    )
                    SELECT
                        id, startTimeMs, endTimeMs, state, pausedTotalMs, lastStateChangeTimeMs,
                        COALESCE(categoryId, '${DefaultCategory.UNCATEGORIZED_ID}') AS categoryId,
                        notes, hourlyRateOverride, roundingModeOverride, roundingDirectionOverride,
                        minBillableSecondsOverride, minChargeAmountOverride, createdOnDevice, updatedAtMs
                    FROM sessions
                    """.trimIndent()
                )

                db.execSQL("DROP TABLE sessions")
                db.execSQL("ALTER TABLE sessions_new RENAME TO sessions")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Sessions: add archiving columns (default: not archived)
                db.execSQL("ALTER TABLE sessions ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sessions ADD COLUMN archivedAtMillis INTEGER")
            }
        }

        private fun ensureDefaultCategory(db: SupportSQLiteDatabase) {
            // Mark all non-default first.
            db.execSQL("UPDATE categories SET isDefault = 0")

            // Insert if missing.
            db.execSQL(
                """
                INSERT OR IGNORE INTO categories (
                    id, name, isDefault, archived,
                    defaultHourlyRate, roundingMode, roundingDirection,
                    minBillableSeconds, minChargeAmount,
                    createdAtMs, updatedAtMs
                )
                VALUES (
                    '${DefaultCategory.UNCATEGORIZED_ID}',
                    '${DefaultCategory.UNCATEGORIZED_NAME}',
                    1,
                    0,
                    NULL, NULL, NULL,
                    NULL, NULL,
                    (strftime('%s','now') * 1000),
                    (strftime('%s','now') * 1000)
                )
                """.trimIndent()
            )

            // Ensure the default row is marked default and not archived.
            db.execSQL(
                """
                UPDATE categories
                SET isDefault = 1, archived = 0
                WHERE id = '${DefaultCategory.UNCATEGORIZED_ID}'
                """.trimIndent()
            )
        }
    }
}

