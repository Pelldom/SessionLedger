package press.pelldom.sessionledger.mobile.billing

import kotlin.math.max
import press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity
import press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity
import press.pelldom.sessionledger.mobile.settings.GlobalSettings

object BillingEngine {

    fun resolveConfig(
        session: SessionEntity,
        category: CategoryEntity?,
        settings: GlobalSettings
    ): ResolvedBillingConfig {
        val (ratePerHour, rateSource) = when {
            session.hourlyRateOverride != null -> session.hourlyRateOverride to SourceType.SESSION
            category?.defaultHourlyRate != null -> category.defaultHourlyRate to SourceType.CATEGORY
            else -> settings.defaultHourlyRate to SourceType.GLOBAL
        }

        val (roundingMode, roundingSource) = when {
            session.roundingModeOverride != null -> session.roundingModeOverride to SourceType.SESSION
            category?.roundingMode != null -> category.roundingMode to SourceType.CATEGORY
            else -> settings.defaultRoundingMode to SourceType.GLOBAL
        }

        val roundingDirection = if (roundingMode == RoundingMode.SIX_MINUTE) {
            when {
                session.roundingDirectionOverride != null -> session.roundingDirectionOverride
                category?.roundingDirection != null -> category.roundingDirection
                else -> settings.defaultRoundingDirection
            }
        } else {
            null
        }

        val (minTimeSeconds, minTimeSource) = when {
            session.minBillableSecondsOverride != null -> session.minBillableSecondsOverride to SourceType.SESSION
            category?.minBillableSeconds != null -> category.minBillableSeconds to SourceType.CATEGORY
            settings.minBillableSeconds != null -> settings.minBillableSeconds to SourceType.GLOBAL
            else -> 0L to SourceType.NONE
        }

        val (minCharge, minChargeSource) = when {
            session.minChargeAmountOverride != null -> session.minChargeAmountOverride to SourceType.SESSION
            category?.minChargeAmount != null -> category.minChargeAmount to SourceType.CATEGORY
            settings.minChargeAmount != null -> settings.minChargeAmount to SourceType.GLOBAL
            else -> 0.0 to SourceType.NONE
        }

        return ResolvedBillingConfig(
            ratePerHour = ratePerHour,
            rateSource = rateSource,
            roundingMode = roundingMode,
            roundingDirection = roundingDirection,
            roundingSource = roundingSource,
            minTimeSeconds = minTimeSeconds,
            minTimeSource = minTimeSource,
            minCharge = minCharge,
            minChargeSource = minChargeSource,
            currency = settings.defaultCurrency
        )
    }

    /**
     * Computes billing for an ENDED session.
     * Assumes session.endTimeMs is non-null and session.state == ENDED.
     */
    fun calculateForEndedSession(
        session: SessionEntity,
        category: CategoryEntity?,
        settings: GlobalSettings
    ): BillingResult {
        require(session.state == SessionState.ENDED) { "Session must be ENDED to calculate billing." }
        val endTimeMs = requireNotNull(session.endTimeMs) { "Ended session must have endTimeMs." }

        val resolved = resolveConfig(session = session, category = category, settings = settings)

        val trackedSeconds = max(
            0L,
            ((endTimeMs - session.startTimeMs) - session.pausedTotalMs) / 1000L
        )

        val roundedSeconds = when (resolved.roundingMode) {
            RoundingMode.EXACT -> trackedSeconds
            RoundingMode.SIX_MINUTE -> {
                val blockSeconds = 360L
                val direction = resolved.roundingDirection ?: RoundingDirection.NEAREST
                when (direction) {
                    RoundingDirection.UP ->
                        ((trackedSeconds + blockSeconds - 1) / blockSeconds) * blockSeconds
                    RoundingDirection.DOWN ->
                        (trackedSeconds / blockSeconds) * blockSeconds
                    RoundingDirection.NEAREST ->
                        ((trackedSeconds + (blockSeconds / 2)) / blockSeconds) * blockSeconds
                }
            }
        }

        val billableSeconds = max(roundedSeconds, resolved.minTimeSeconds)

        val costPreMinCharge = (billableSeconds / 3600.0) * resolved.ratePerHour
        val finalCost = max(costPreMinCharge, resolved.minCharge)

        return BillingResult(
            trackedSeconds = trackedSeconds,
            roundedSeconds = roundedSeconds,
            billableSeconds = billableSeconds,
            costPreMinCharge = costPreMinCharge,
            finalCost = finalCost,
            resolved = resolved
        )
    }
}

