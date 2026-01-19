package press.pelldom.sessionledger.mobile.appsettings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import press.pelldom.sessionledger.mobile.settings.dataStore

class AppSettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppSettingsRepository(app.dataStore)

    val settings: StateFlow<AppSettings> = repo.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings()
    )

    fun setUseSystemDefaults(value: Boolean) {
        viewModelScope.launch { repo.setUseSystemDefaults(value) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repo.setThemeMode(mode) }
    }
}

