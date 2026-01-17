package press.pelldom.sessionledger.mobile.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import press.pelldom.sessionledger.mobile.billing.RoundingDirection
import press.pelldom.sessionledger.mobile.billing.RoundingMode

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String, // UUID string
    val name: String,
    val isDefault: Boolean = false,
    val archived: Boolean = false,

    // Defaults for sessions in this category (nullable = fall back to global)
    val defaultHourlyRate: Double? = null,
    val roundingMode: RoundingMode? = null, // nullable override
    val roundingDirection: RoundingDirection? = null, // nullable override

    // Minimums (nullable = none at this level)
    val minBillableSeconds: Long? = null,
    val minChargeAmount: Double? = null,

    // Audit
    val createdAtMs: Long,
    val updatedAtMs: Long,
)

