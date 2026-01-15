package press.pelldom.sessionledger.mobile.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import press.pelldom.sessionledger.mobile.billing.RoundingDirection
import press.pelldom.sessionledger.mobile.billing.RoundingMode
import press.pelldom.sessionledger.mobile.billing.SessionState

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String, // UUID string

    val startTimeMs: Long,
    val endTimeMs: Long? = null, // null while active
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
    val createdOnDevice: String, // "phone" | "watch"
    val updatedAtMs: Long,
) {
    // Step 2B naming aliases (no schema change)
    val startTimeMillis: Long get() = startTimeMs
    val endTimeMillis: Long? get() = endTimeMs
    val pausedDurationMillis: Long get() = pausedTotalMs
}

