# Chat Outputs - Step 2 (Data Layer) - Staging

These are implementation-ready code blocks produced in chat, staged here so nothing is lost.
When the Android Studio project exists under `/android/`, migrate these into proper `.kt` files.

## Enums

```kotlin
enum class SessionState { RUNNING, PAUSED, ENDED }

enum class RoundingMode { EXACT, SIX_MINUTE }

enum class RoundingDirection { UP, NEAREST, DOWN }

/** Where a setting/value came from when resolving precedence. */
enum class SourceType { SESSION, CATEGORY, GLOBAL, NONE }
```

## Room entities

### CategoryEntity
```kotlin
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,               // UUID string
    val name: String,
    val archived: Boolean = false,

    // Defaults for sessions in this category (nullable = fall back to global)
    val defaultHourlyRate: Double? = null,
    val roundingMode: RoundingMode? = null,            // nullable override
    val roundingDirection: RoundingDirection? = null,  // nullable override

    // Minimums (nullable = none at this level)
    val minBillableSeconds: Long? = null,
    val minChargeAmount: Double? = null,

    // Audit
    val createdAtMs: Long,
    val updatedAtMs: Long,
)
```

### SessionEntity
```kotlin
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,               // UUID string

    val startTimeMs: Long,
    val endTimeMs: Long? = null,              // null while active
    val state: SessionState,

    val pausedTotalMs: Long = 0L,
    val lastStateChangeTimeMs: Long,

    val categoryId: String? = null,
    val notes: String? = null,

    // Per-session overrides (nullable = fall back to category/global)
    val hourlyRateOverride: Double? = null,
    val roundingModeOverride: RoundingMode? = null,
    val roundingDirectionOverride: RoundingDirection? = null,
    val minBillableSecondsOverride: Long? = null,
    val minChargeAmountOverride: Double? = null,

    // Audit
    val createdOnDevice: String,              // "phone" | "watch"
    val updatedAtMs: Long,
)
```

## Global settings model (DataStore-backed)

```kotlin
data class GlobalSettings(
    val defaultCurrency: String = "CAD",
    val defaultHourlyRate: Double,

    val defaultRoundingMode: RoundingMode,
    val defaultRoundingDirection: RoundingDirection,

    val minBillableSeconds: Long? = null,
    val minChargeAmount: Double? = null,

    val lastUsedCategoryId: String? = null,
)
```

## Billing engine API (pure Kotlin)

```kotlin
data class ResolvedBillingConfig(
    val ratePerHour: Double,
    val rateSource: SourceType,

    val roundingMode: RoundingMode,
    val roundingDirection: RoundingDirection?, // null if EXACT
    val roundingSource: SourceType,

    val minTimeSeconds: Long,
    val minTimeSource: SourceType,

    val minCharge: Double,
    val minChargeSource: SourceType,

    val currency: String,
)

data class BillingResult(
    val trackedSeconds: Long,
    val roundedSeconds: Long,
    val billableSeconds: Long,

    val costPreMinCharge: Double,
    val finalCost: Double,

    val resolved: ResolvedBillingConfig,
)

object BillingEngine {

    fun resolveConfig(
        session: SessionEntity,
        category: CategoryEntity?,
        settings: GlobalSettings
    ): ResolvedBillingConfig

    /**
     * Computes billing for an ENDED session.
     * Assumes session.endTimeMs is non-null and session.state == ENDED.
     */
    fun calculateForEndedSession(
        session: SessionEntity,
        category: CategoryEntity?,
        settings: GlobalSettings
    ): BillingResult
}
```

## Room: converters + database + DAOs + repositories

### RoomConverters
```kotlin
import androidx.room.TypeConverter

class RoomConverters {

    @TypeConverter
    fun sessionStateToString(value: SessionState): String = value.name

    @TypeConverter
    fun stringToSessionState(value: String): SessionState = SessionState.valueOf(value)

    @TypeConverter
    fun roundingModeToString(value: RoundingMode?): String? = value?.name

    @TypeConverter
    fun stringToRoundingMode(value: String?): RoundingMode? =
        value?.let { RoundingMode.valueOf(it) }

    @TypeConverter
    fun roundingDirectionToString(value: RoundingDirection?): String? = value?.name

    @TypeConverter
    fun stringToRoundingDirection(value: String?): RoundingDirection? =
        value?.let { RoundingDirection.valueOf(it) }
}
```

### AppDatabase
```kotlin
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [CategoryEntity::class, SessionEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun sessionDao(): SessionDao
}
```

### CategoryDao
```kotlin
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE archived = 0 ORDER BY name COLLATE NOCASE ASC")
    fun observeActiveCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY archived ASC, name COLLATE NOCASE ASC")
    fun observeAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: CategoryEntity)

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("UPDATE categories SET archived = 1, updatedAtMs = :updatedAtMs WHERE id = :id")
    suspend fun archive(id: String, updatedAtMs: Long)

    @Query("UPDATE categories SET archived = 0, updatedAtMs = :updatedAtMs WHERE id = :id")
    suspend fun unarchive(id: String, updatedAtMs: Long)
}
```

### SessionDao
```kotlin
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    // Active session (at most one). Enforced by app logic in v1.
    @Query("SELECT * FROM sessions WHERE state != 'ENDED' ORDER BY startTimeMs DESC LIMIT 1")
    fun observeActiveSession(): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE state != 'ENDED' ORDER BY startTimeMs DESC LIMIT 1")
    suspend fun getActiveSession(): SessionEntity?

    @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SessionEntity?

    @Query("SELECT * FROM sessions WHERE state = 'ENDED' ORDER BY startTimeMs DESC")
    fun observeEndedSessionsNewestFirst(): Flow<List<SessionEntity>>

    @Query("""
        SELECT * FROM sessions
        WHERE state = 'ENDED'
          AND startTimeMs >= :startMs
          AND startTimeMs < :endMs
        ORDER BY startTimeMs ASC
    """)
    suspend fun getEndedSessionsInRange(startMs: Long, endMs: Long): List<SessionEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(session: SessionEntity)

    @Update
    suspend fun update(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteById(id: String)
}
```

### CategoryRepository (skeleton)
```kotlin
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val categoryDao: CategoryDao
) {
    fun observeActiveCategories(): Flow<List<CategoryEntity>> = categoryDao.observeActiveCategories()

    suspend fun getById(id: String): CategoryEntity? = categoryDao.getById(id)

    suspend fun createCategory(nowMs: Long, id: String, name: String): CategoryEntity {
        val entity = CategoryEntity(
            id = id,
            name = name,
            archived = false,
            createdAtMs = nowMs,
            updatedAtMs = nowMs
        )
        categoryDao.insert(entity)
        return entity
    }

    suspend fun updateCategory(entity: CategoryEntity) = categoryDao.update(entity)

    suspend fun archive(id: String, nowMs: Long) = categoryDao.archive(id, nowMs)
}
```

### SessionRepository (state machine enforcement)
```kotlin
import kotlinx.coroutines.flow.Flow
import kotlin.math.max

class SessionRepository(
    private val sessionDao: SessionDao
) {
    fun observeActiveSession(): Flow<SessionEntity?> = sessionDao.observeActiveSession()

    suspend fun startSession(
        nowMs: Long,
        sessionId: String,
        categoryId: String?,
        createdOnDevice: String, // "phone" | "watch"
        notes: String? = null
    ): SessionEntity {
        val existing = sessionDao.getActiveSession()
        require(existing == null) { "An active session already exists." }

        val session = SessionEntity(
            id = sessionId,
            startTimeMs = nowMs,
            endTimeMs = null,
            state = SessionState.RUNNING,
            pausedTotalMs = 0L,
            lastStateChangeTimeMs = nowMs,
            categoryId = categoryId,
            notes = notes,
            createdOnDevice = createdOnDevice,
            updatedAtMs = nowMs
        )
        sessionDao.insert(session)
        return session
    }

    suspend fun pauseActiveSession(nowMs: Long): SessionEntity {
        val s = sessionDao.getActiveSession() ?: error("No active session.")
        require(s.state == SessionState.RUNNING) { "Session is not running." }

        val updated = s.copy(
            state = SessionState.PAUSED,
            lastStateChangeTimeMs = nowMs,
            updatedAtMs = nowMs
        )
        sessionDao.update(updated)
        return updated
    }

    suspend fun resumeActiveSession(nowMs: Long): SessionEntity {
        val s = sessionDao.getActiveSession() ?: error("No active session.")
        require(s.state == SessionState.PAUSED) { "Session is not paused." }

        // Add time spent paused since last state change
        val pausedDelta = max(0L, nowMs - s.lastStateChangeTimeMs)

        val updated = s.copy(
            state = SessionState.RUNNING,
            pausedTotalMs = s.pausedTotalMs + pausedDelta,
            lastStateChangeTimeMs = nowMs,
            updatedAtMs = nowMs
        )
        sessionDao.update(updated)
        return updated
    }

    suspend fun endActiveSession(nowMs: Long): SessionEntity {
        val s = sessionDao.getActiveSession() ?: error("No active session.")

        val finalPausedTotalMs = if (s.state == SessionState.PAUSED) {
            val pausedDelta = max(0L, nowMs - s.lastStateChangeTimeMs)
            s.pausedTotalMs + pausedDelta
        } else {
            s.pausedTotalMs
        }

        val updated = s.copy(
            state = SessionState.ENDED,
            endTimeMs = nowMs,
            pausedTotalMs = finalPausedTotalMs,
            lastStateChangeTimeMs = nowMs,
            updatedAtMs = nowMs
        )
        sessionDao.update(updated)
        return updated
    }
}
```

## DataStore settings

### Settings keys
```kotlin
import androidx.datastore.preferences.core.*

object SettingsKeys {
    val DEFAULT_CURRENCY = stringPreferencesKey("default_currency") // "CAD"
    val DEFAULT_HOURLY_RATE = doublePreferencesKey("default_hourly_rate")

    val DEFAULT_ROUNDING_MODE = stringPreferencesKey("default_rounding_mode") // EXACT | SIX_MINUTE
    val DEFAULT_ROUNDING_DIRECTION = stringPreferencesKey("default_rounding_direction") // UP | NEAREST | DOWN

    val MIN_BILLABLE_SECONDS = longPreferencesKey("min_billable_seconds") // nullable if absent
    val MIN_CHARGE_AMOUNT = doublePreferencesKey("min_charge_amount") // nullable if absent

    val LAST_USED_CATEGORY_ID = stringPreferencesKey("last_used_category_id") // nullable
}
```

### SettingsRepository (skeleton)
```kotlin
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    fun observeGlobalSettings(): Flow<GlobalSettings> {
        return dataStore.data.map { prefs ->
            val currency = prefs[SettingsKeys.DEFAULT_CURRENCY] ?: "CAD"

            val rate = prefs[SettingsKeys.DEFAULT_HOURLY_RATE]
                ?: 0.0 // We'll set a sensible default in onboarding/settings UI later

            val roundingMode = (prefs[SettingsKeys.DEFAULT_ROUNDING_MODE] ?: RoundingMode.SIX_MINUTE.name)
                .let { RoundingMode.valueOf(it) }

            val roundingDir = (prefs[SettingsKeys.DEFAULT_ROUNDING_DIRECTION] ?: RoundingDirection.UP.name)
                .let { RoundingDirection.valueOf(it) }

            GlobalSettings(
                defaultCurrency = currency,
                defaultHourlyRate = rate,
                defaultRoundingMode = roundingMode,
                defaultRoundingDirection = roundingDir,
                minBillableSeconds = prefs[SettingsKeys.MIN_BILLABLE_SECONDS],
                minChargeAmount = prefs[SettingsKeys.MIN_CHARGE_AMOUNT],
                lastUsedCategoryId = prefs[SettingsKeys.LAST_USED_CATEGORY_ID]
            )
        }
    }

    suspend fun setLastUsedCategoryId(categoryId: String?) {
        dataStore.edit { prefs ->
            if (categoryId == null) prefs.remove(SettingsKeys.LAST_USED_CATEGORY_ID)
            else prefs[SettingsKeys.LAST_USED_CATEGORY_ID] = categoryId
        }
    }

    suspend fun setDefaultCurrencyCad() {
        dataStore.edit { prefs -> prefs[SettingsKeys.DEFAULT_CURRENCY] = "CAD" }
    }
}
```
