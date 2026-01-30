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
        val pendingResult = goAsync()
        when (intent.action) {
            ACTION_START -> {
                val categoryId = intent.getStringExtra(EXTRA_CATEGORY_ID)
                Log.d(tag, "Tile Start command received (categoryId=$categoryId), sending to phone...")
                scope.launch {
                    try {
                        val success = WearCommandSender.sendStart(context, categoryId)
                        if (success) {
                            Log.d(tag, "Tile Start command sent successfully")
                        } else {
                            Log.w(tag, "Tile Start command failed to send")
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            ACTION_PAUSE -> {
                Log.d(tag, "Tile Pause command received, sending to phone...")
                scope.launch {
                    try {
                        val success = WearCommandSender.sendPause(context)
                        if (success) {
                            Log.d(tag, "Tile Pause command sent successfully")
                        } else {
                            Log.w(tag, "Tile Pause command failed to send")
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            ACTION_RESUME -> {
                Log.d(tag, "Tile Resume command received, sending to phone...")
                scope.launch {
                    try {
                        val success = WearCommandSender.sendResume(context)
                        if (success) {
                            Log.d(tag, "Tile Resume command sent successfully")
                        } else {
                            Log.w(tag, "Tile Resume command failed to send")
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            ACTION_STOP -> {
                Log.d(tag, "Tile Stop command received, sending to phone...")
                scope.launch {
                    try {
                        val success = WearCommandSender.sendStop(context)
                        if (success) {
                            Log.d(tag, "Tile Stop command sent successfully")
                        } else {
                            Log.w(tag, "Tile Stop command failed to send")
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            else -> {
                Log.w(tag, "Unknown action: ${intent.action}")
                pendingResult.finish()
            }
        }
    }
}
