package press.pelldom.sessionledger.mobile.ui.export

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import press.pelldom.sessionledger.mobile.data.db.AppDatabase
import press.pelldom.sessionledger.mobile.export.CsvExporter
import press.pelldom.sessionledger.mobile.settings.SettingsRepository
import press.pelldom.sessionledger.mobile.settings.dataStore

class ExportViewModel : ViewModel() {

    fun exportAll(context: Context) {
        viewModelScope.launch {
            CsvExporter().exportEndedSessions(
                db = AppDatabase.getInstance(context),
                settingsRepo = SettingsRepository(context.dataStore),
                startFilter = null,
                endFilter = null,
                categoryFilter = null,
                context = context
            )
        }
    }
}

