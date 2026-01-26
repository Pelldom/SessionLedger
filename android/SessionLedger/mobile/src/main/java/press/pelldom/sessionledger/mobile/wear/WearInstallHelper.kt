package press.pelldom.sessionledger.mobile.wear

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper for installing the watch app via Play Store.
 * Opens Play Store on the phone, which will automatically offer to install
 * the watch companion app if a Wear OS device is connected.
 */
object WearInstallHelper {
    private const val TAG = "WearInstall"
    private const val WEAR_APP_PACKAGE = "press.pelldom.sessionledger"
    
    /**
     * Opens the Play Store listing for the watch app.
     * When opened on the phone, Play Store will automatically detect connected
     * Wear OS devices and offer to install the watch companion.
     * Fails silently if Play Store is unavailable.
     */
    suspend fun openPlayStoreOnWatch(context: Context): Boolean = withContext(Dispatchers.Main) {
        try {
            // Play Store deep link for the watch app package
            val playStoreUri = Uri.parse("market://details?id=$WEAR_APP_PACKAGE")
            val intent = Intent(Intent.ACTION_VIEW, playStoreUri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            context.startActivity(intent)
            Log.d(TAG, "Opened Play Store for watch app installation")
            return@withContext true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to open Play Store: ${e.message}", e)
            return@withContext false
        }
    }
}
