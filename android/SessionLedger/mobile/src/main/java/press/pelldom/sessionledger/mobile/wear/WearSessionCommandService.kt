package press.pelldom.sessionledger.mobile.wear

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import press.pelldom.sessionledger.mobile.data.db.AppDatabase
import press.pelldom.sessionledger.mobile.ui.session.SessionRepository

/**
 * Phone-side Wear Data Layer message listener.
 *
 * Watch sends commands only; phone is the source of truth.
 */
class SessionLedgerWearService : WearableListenerService() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val tag = "SessionLedgerWear"

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val db = AppDatabase.getInstance(this)
        val repo = SessionRepository(
            sessionDao = db.sessionDao(),
            categoryDao = db.categoryDao(),
            appContext = applicationContext
        )

        Log.d(tag, "Received command: ${messageEvent.path}")

        scope.launch {
            when (messageEvent.path) {
                WearSessionPaths.START -> {
                    val categoryId = messageEvent.data
                        ?.takeIf { it.isNotEmpty() }
                        ?.toString(Charsets.UTF_8)
                    repo.startSession(categoryId = categoryId, createdOnDevice = "watch")
                }
                WearSessionPaths.PAUSE -> repo.pauseSession()
                WearSessionPaths.RESUME -> repo.resumeSession()
                WearSessionPaths.END -> repo.endSession()
            }
        }
    }
}

