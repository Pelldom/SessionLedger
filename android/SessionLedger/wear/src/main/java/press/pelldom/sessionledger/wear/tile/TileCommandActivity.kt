package press.pelldom.sessionledger.wear.tile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity

private const val TAG = "TileCommandActivity"

/**
 * Small helper to avoid code duplication across tile command activities.
 */
private fun ComponentActivity.sendTileCommandAndFinish(action: String) {
    Log.d(TAG, "sendTileCommandAndFinish: action=$action, packageName=$packageName")
    val broadcastIntent = Intent(action).apply {
        setPackage(packageName)
    }
    Log.d(TAG, "Sending broadcast: $action (package=$packageName)")
    sendBroadcast(broadcastIntent)
    Log.d(TAG, "Broadcast sent, finishing activity")
    finish()
}

/**
 * Launched from the Tile when the user taps [Start].
 */
class TileStartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "TileStartActivity.onCreate() called")
        sendTileCommandAndFinish(TileCommandReceiver.ACTION_START)
    }
}

/**
 * Launched from the Tile when the user taps [Pause].
 */
class TilePauseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sendTileCommandAndFinish(TileCommandReceiver.ACTION_PAUSE)
    }
}

/**
 * Launched from the Tile when the user taps [Resume].
 */
class TileResumeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sendTileCommandAndFinish(TileCommandReceiver.ACTION_RESUME)
    }
}

/**
 * Launched from the Tile when the user taps [Stop].
 */
class TileStopActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sendTileCommandAndFinish(TileCommandReceiver.ACTION_STOP)
    }
}
