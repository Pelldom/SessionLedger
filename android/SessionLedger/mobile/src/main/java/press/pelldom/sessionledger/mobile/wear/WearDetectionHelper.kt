package press.pelldom.sessionledger.mobile.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Wear OS detection status.
 */
enum class WearStatus {
    /** No Wear OS device is available/connected. */
    NO_WATCH_AVAILABLE,
    
    /** Wear OS device detected but watch app is not installed. */
    WATCH_DETECTED_NOT_INSTALLED,
    
    /** Wear OS device detected and watch app is installed. */
    WATCH_INSTALLED
}

/**
 * Safely detects Wear OS availability and watch app installation status.
 * All Wear API calls are guarded and will not crash if Wear OS is unavailable.
 */
object WearDetectionHelper {
    private const val TAG = "WearDetection"
    private const val WEAR_APP_PACKAGE = "press.pelldom.sessionledger"

    /**
     * Detects the current Wear OS status.
     * Returns NO_WATCH_AVAILABLE if any Wear API is unavailable or fails.
     */
    suspend fun detectWearStatus(context: Context): WearStatus = withContext(Dispatchers.IO) {
        try {
            // Check if Wear API is available
            val nodeClient: NodeClient = Wearable.getNodeClient(context)
            val dataClient: DataClient = Wearable.getDataClient(context)

            // Get connected nodes (watches)
            val nodes = Tasks.await(nodeClient.connectedNodes)
            if (nodes.isEmpty()) {
                Log.d(TAG, "No connected Wear OS nodes")
                return@withContext WearStatus.NO_WATCH_AVAILABLE
            }

            Log.d(TAG, "Found ${nodes.size} connected Wear OS node(s)")

            // Check if watch app is installed by looking for data items from the wear app
            // The wear app publishes data on known paths (categories, session state)
            // If we see data items on these paths, the app is likely installed
            try {
                val dataItems = Tasks.await(dataClient.getDataItems())
                val wearAppPaths = setOf(
                    WearSessionPaths.CATEGORIES,
                    WearSessionPaths.SESSION_STATE
                )
                
                val hasWearAppData = dataItems.any { item ->
                    val uri = item.uri
                    wearAppPaths.any { path -> uri.path == path }
                }
                
                if (hasWearAppData) {
                    Log.d(TAG, "Found data items from wear app - app appears installed")
                    return@withContext WearStatus.WATCH_INSTALLED
                } else {
                    Log.d(TAG, "No data items from wear app - showing install option")
                    return@withContext WearStatus.WATCH_DETECTED_NOT_INSTALLED
                }
            } catch (e: Exception) {
                Log.d(TAG, "Could not check data items: ${e.message}")
                // If we can't check data items, assume app might not be installed
                // Show install button - Play Store will handle "already installed" case
                return@withContext WearStatus.WATCH_DETECTED_NOT_INSTALLED
            }

        } catch (e: ApiException) {
            if (e.statusCode == ConnectionResult.API_UNAVAILABLE) {
                Log.d(TAG, "Wear API unavailable: ${e.message}")
            } else {
                Log.w(TAG, "Wear API error: ${e.message}", e)
            }
            return@withContext WearStatus.NO_WATCH_AVAILABLE
        } catch (e: Exception) {
            Log.w(TAG, "Error detecting Wear status: ${e.message}", e)
            return@withContext WearStatus.NO_WATCH_AVAILABLE
        }
    }
}
