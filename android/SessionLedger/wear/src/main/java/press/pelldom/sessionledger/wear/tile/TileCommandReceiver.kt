package press.pelldom.sessionledger.wear.tile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import press.pelldom.sessionledger.wear.comms.WearCommandSender

/**
 * BroadcastReceiver for handling tile button actions.
 * 
 * This receiver is called when tile buttons are clicked.
 * It launches a coroutine to send the command via WearCommandSender.
 */
class TileCommandReceiver : BroadcastReceiver() {
    private val tag = "TileCommandReceiver"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val ACTION_START = "press.pelldom.sessionledger.wear.tile.ACTION_START"
        const val ACTION_PAUSE = "press.pelldom.sessionledger.wear.tile.ACTION_PAUSE"
        const val ACTION_RESUME = "press.pelldom.sessionledger.wear.tile.ACTION_RESUME"
        const val ACTION_STOP = "press.pelldom.sessionledger.wear.tile.ACTION_STOP"
        
        const val EXTRA_CATEGORY_ID = "category_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_START -> {
                val categoryId = intent.getStringExtra(EXTRA_CATEGORY_ID)
                scope.launch {
                    Log.d(tag, "Tile Start command received (categoryId=$categoryId)")
                    WearCommandSender.sendStart(context, categoryId)
                }
            }
            ACTION_PAUSE -> {
                scope.launch {
                    Log.d(tag, "Tile Pause command received")
                    WearCommandSender.sendPause(context)
                }
            }
            ACTION_RESUME -> {
                scope.launch {
                    Log.d(tag, "Tile Resume command received")
                    WearCommandSender.sendResume(context)
                }
            }
            ACTION_STOP -> {
                scope.launch {
                    Log.d(tag, "Tile Stop command received")
                    WearCommandSender.sendStop(context)
                }
            }
            else -> {
                Log.w(tag, "Unknown action: ${intent.action}")
            }
        }
    }
}
