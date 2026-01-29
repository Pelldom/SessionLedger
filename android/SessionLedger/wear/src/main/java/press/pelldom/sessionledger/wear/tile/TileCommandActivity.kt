package press.pelldom.sessionledger.wear.tile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Small helper to avoid code duplication across tile command activities.
 */
private fun ComponentActivity.sendTileCommandAndFinish(action: String) {
    val broadcastIntent = Intent(action).apply {
        setPackage(packageName)
    }
    sendBroadcast(broadcastIntent)
    finish()
}

/**
 * Launched from the Tile when the user taps [Start].
 */
class TileStartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
