package press.pelldom.sessionledger.mobile.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import press.pelldom.sessionledger.mobile.billing.RoundingDirection
import press.pelldom.sessionledger.mobile.billing.RoundingMode

data class GlobalSettings(
    val defaultCurrency: String = "CAD",
    val defaultHourlyRate: Double,

    val defaultRoundingMode: RoundingMode,
    val defaultRoundingDirection: RoundingDirection,

    val minBillableSeconds: Long? = null,
    val minChargeAmount: Double? = null,

    val lastUsedCategoryId: String? = null,
)

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

    suspend fun setDefaultHourlyRate(ratePerHour: Double) {
        dataStore.edit { prefs -> prefs[SettingsKeys.DEFAULT_HOURLY_RATE] = ratePerHour }
    }

    suspend fun setDefaultRoundingMode(mode: RoundingMode) {
        dataStore.edit { prefs -> prefs[SettingsKeys.DEFAULT_ROUNDING_MODE] = mode.name }
    }

    suspend fun setDefaultRoundingDirection(direction: RoundingDirection) {
        dataStore.edit { prefs -> prefs[SettingsKeys.DEFAULT_ROUNDING_DIRECTION] = direction.name }
    }

    suspend fun setMinBillableSeconds(value: Long?) {
        dataStore.edit { prefs ->
            if (value == null) prefs.remove(SettingsKeys.MIN_BILLABLE_SECONDS)
            else prefs[SettingsKeys.MIN_BILLABLE_SECONDS] = value
        }
    }

    suspend fun setMinChargeAmount(value: Double?) {
        dataStore.edit { prefs ->
            if (value == null) prefs.remove(SettingsKeys.MIN_CHARGE_AMOUNT)
            else prefs[SettingsKeys.MIN_CHARGE_AMOUNT] = value
        }
    }
}

