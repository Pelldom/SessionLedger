package press.pelldom.sessionledger.mobile.ui.session

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import press.pelldom.sessionledger.mobile.data.db.dao.SessionDao
import press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity

class SessionViewModel(
    private val sessionDao: SessionDao,
    private val appContext: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val repo = SessionRepository(sessionDao = sessionDao, appContext = appContext)

    private val _activeSession = MutableStateFlow<SessionEntity?>(null)
    val activeSession: StateFlow<SessionEntity?> = _activeSession

    init {
        scope.launch {
            sessionDao.observeActiveSession()
                .distinctUntilChanged()
                .collect {
                    _activeSession.value = it
                }
        }
    }

    fun startSession() {
        scope.launch { repo.startSession() }
    }

    fun pauseSession() {
        scope.launch { repo.pauseSession() }
    }

    fun resumeSession() {
        scope.launch { repo.resumeSession() }
    }

    fun endSession() {
        scope.launch { repo.endSession() }
    }

    override fun onCleared() {
        super.onCleared()
        scope.coroutineContext.cancel()
    }
}

