package press.pelldom.sessionledger.mobile.billing

import org.junit.Assert.assertEquals
import org.junit.Test
import press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity
import press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity
import press.pelldom.sessionledger.mobile.settings.GlobalSettings

class BillingEngineTest {

    @Test
    fun testGlobalDefaultsRoundingUp() {
        // 17 minutes raw tracked -> 18 minutes billable when rounding UP to 6-minute blocks.
        val session = endedSession(minutesTracked = 17)
        val settings = GlobalSettings(
            defaultCurrency = "CAD",
            defaultHourlyRate = 120.0,
            defaultRoundingMode = RoundingMode.SIX_MINUTE,
            defaultRoundingDirection = RoundingDirection.UP,
            minBillableSeconds = null,
            minChargeAmount = null,
            lastUsedCategoryId = null
        )

        val result = BillingEngine.calculateForEndedSession(
            session = session,
            category = null,
            settings = settings
        )

        assertEquals(18L * 60L, result.billableSeconds)
        assertEquals((18.0 / 60.0) * 120.0, result.finalCost, 1e-9)
    }

    @Test
    fun testCategoryMinTime() {
        val category = categoryWithMinTimeSeconds(minTimeSeconds = 3600L)
        val session = endedSession(minutesTracked = 12, categoryId = category.id)
        val settings = GlobalSettings(
            defaultCurrency = "CAD",
            defaultHourlyRate = 100.0,
            defaultRoundingMode = RoundingMode.EXACT,
            defaultRoundingDirection = RoundingDirection.UP,
            minBillableSeconds = null,
            minChargeAmount = null,
            lastUsedCategoryId = null
        )

        val result = BillingEngine.calculateForEndedSession(session, category, settings)

        assertEquals(3600L, result.billableSeconds)
    }

    @Test
    fun testCategoryMinCharge() {
        // Raw 20 minutes at $150/hr -> $50, but category min charge bumps final to $100.
        val category = categoryWithMinCharge(minCharge = 100.0)
        val session = endedSession(minutesTracked = 20, categoryId = category.id)
        val settings = GlobalSettings(
            defaultCurrency = "CAD",
            defaultHourlyRate = 150.0,
            defaultRoundingMode = RoundingMode.EXACT,
            defaultRoundingDirection = RoundingDirection.UP,
            minBillableSeconds = null,
            minChargeAmount = null,
            lastUsedCategoryId = null
        )

        val result = BillingEngine.calculateForEndedSession(session, category, settings)

        assertEquals(100.0, result.finalCost, 1e-9)
    }

    @Test
    fun testSessionOverridesAndCategoryMinTime() {
        // Session overrides rate & rounding, but category min time still applies.
        val category = categoryWithMinTimeSeconds(minTimeSeconds = 1800L) // 30 minutes
        val session = endedSession(
            minutesTracked = 5,
            categoryId = category.id,
            hourlyRateOverride = 250.0,
            roundingModeOverride = RoundingMode.EXACT
        )
        val settings = GlobalSettings(
            defaultCurrency = "CAD",
            defaultHourlyRate = 50.0,
            defaultRoundingMode = RoundingMode.SIX_MINUTE,
            defaultRoundingDirection = RoundingDirection.UP,
            minBillableSeconds = null,
            minChargeAmount = null,
            lastUsedCategoryId = null
        )

        val result = BillingEngine.calculateForEndedSession(session, category, settings)

        assertEquals(1800L, result.billableSeconds)
    }

    @Test
    fun testMinTimeAndMinCharge() {
        // Both min time and min charge exist. Session exceeds min time, but min charge is higher than computed cost.
        val session = endedSession(minutesTracked = 40)
        val settings = GlobalSettings(
            defaultCurrency = "CAD",
            defaultHourlyRate = 100.0,
            defaultRoundingMode = RoundingMode.EXACT,
            defaultRoundingDirection = RoundingDirection.UP,
            minBillableSeconds = 1800L, // 30 minutes
            minChargeAmount = 80.0,
            lastUsedCategoryId = null
        )

        val result = BillingEngine.calculateForEndedSession(session, category = null, settings = settings)

        // 40 min @ $100/hr => $66.666..., min charge bumps to $80.
        assertEquals(40L * 60L, result.billableSeconds)
        assertEquals(80.0, result.finalCost, 1e-9)
    }

    private fun endedSession(
        minutesTracked: Long,
        categoryId: String? = null,
        hourlyRateOverride: Double? = null,
        roundingModeOverride: RoundingMode? = null
    ): SessionEntity {
        val startTimeMs = 0L
        val endTimeMs = minutesTracked * 60L * 1000L
        return SessionEntity(
            id = "session-1",
            startTimeMs = startTimeMs,
            endTimeMs = endTimeMs,
            state = SessionState.ENDED,
            pausedTotalMs = 0L,
            lastStateChangeTimeMs = endTimeMs,
            categoryId = categoryId,
            notes = null,
            hourlyRateOverride = hourlyRateOverride,
            roundingModeOverride = roundingModeOverride,
            roundingDirectionOverride = null,
            minBillableSecondsOverride = null,
            minChargeAmountOverride = null,
            createdOnDevice = "phone",
            updatedAtMs = endTimeMs
        )
    }

    private fun categoryWithMinTimeSeconds(minTimeSeconds: Long): CategoryEntity {
        return CategoryEntity(
            id = "cat-1",
            name = "Category",
            archived = false,
            defaultHourlyRate = null,
            roundingMode = null,
            roundingDirection = null,
            minBillableSeconds = minTimeSeconds,
            minChargeAmount = null,
            createdAtMs = 0L,
            updatedAtMs = 0L
        )
    }

    private fun categoryWithMinCharge(minCharge: Double): CategoryEntity {
        return CategoryEntity(
            id = "cat-1",
            name = "Category",
            archived = false,
            defaultHourlyRate = null,
            roundingMode = null,
            roundingDirection = null,
            minBillableSeconds = null,
            minChargeAmount = minCharge,
            createdAtMs = 0L,
            updatedAtMs = 0L
        )
    }
}

