package press.pelldom.sessionledger.mobile.billing

enum class SessionState { RUNNING, PAUSED, ENDED }

enum class RoundingMode { EXACT, SIX_MINUTE }

enum class RoundingDirection { UP, NEAREST, DOWN }

/** Where a setting/value came from when resolving precedence. */
enum class SourceType { SESSION, CATEGORY, GLOBAL, NONE }

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

