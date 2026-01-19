package press.pelldom.sessionledger.mobile.appsettings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useSystemDefaults: Boolean = true
)

class AppSettingsRepository(private val dataStore: DataStore<Preferences>) {
    private object Keys {
        val THEME_MODE = intPreferencesKey("app_theme_mode_v1") // 0=SYSTEM,1=LIGHT,2=DARK
        val USE_SYSTEM_DEFAULTS = booleanPreferencesKey("app_use_system_defaults_v1")
    }

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        val themeInt = prefs[Keys.THEME_MODE] ?: 0
        val theme = when (themeInt) {
            1 -> ThemeMode.LIGHT
            2 -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
        val useSystem = prefs[Keys.USE_SYSTEM_DEFAULTS] ?: true
        AppSettings(themeMode = theme, useSystemDefaults = useSystem)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = when (mode) {
                ThemeMode.SYSTEM -> 0
                ThemeMode.LIGHT -> 1
                ThemeMode.DARK -> 2
            }
        }
    }

    suspend fun setUseSystemDefaults(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.USE_SYSTEM_DEFAULTS] = value
        }
    }
}

