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
}

