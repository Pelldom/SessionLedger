package press.pelldom.sessionledger.mobile.wear

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import press.pelldom.sessionledger.mobile.data.db.AppDatabase
import press.pelldom.sessionledger.mobile.ui.session.SessionViewModel

/**
 * Phone-side Wear Data Layer message listener.
 *
 * Watch sends commands only; phone is the source of truth.
 */
class WearSessionCommandService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val dao = AppDatabase.getInstance(this).sessionDao()
        val vm = SessionViewModel(sessionDao = dao, appContext = applicationContext)

        when (messageEvent.path) {
            WearSessionPaths.START -> vm.startSession()
            WearSessionPaths.PAUSE -> vm.pauseSession()
            WearSessionPaths.RESUME -> vm.resumeSession()
            WearSessionPaths.END -> vm.endSession()
        }
    }
}

