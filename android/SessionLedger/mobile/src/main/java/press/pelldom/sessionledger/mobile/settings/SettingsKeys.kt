package press.pelldom.sessionledger.mobile.settings

import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SettingsKeys {
    val DEFAULT_CURRENCY = stringPreferencesKey("default_currency") // "CAD"
    val DEFAULT_HOURLY_RATE = doublePreferencesKey("default_hourly_rate")

    val DEFAULT_ROUNDING_MODE = stringPreferencesKey("default_rounding_mode") // EXACT | SIX_MINUTE
    val DEFAULT_ROUNDING_DIRECTION = stringPreferencesKey("default_rounding_direction") // UP | NEAREST | DOWN

    val MIN_BILLABLE_SECONDS = longPreferencesKey("min_billable_seconds") // nullable if absent
    val MIN_CHARGE_AMOUNT = doublePreferencesKey("min_charge_amount") // nullable if absent

    val LAST_USED_CATEGORY_ID = stringPreferencesKey("last_used_category_id") // nullable
}

