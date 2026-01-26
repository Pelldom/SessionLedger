package press.pelldom.sessionledger.mobile.wear

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log

/**
 * Helper for checking Wear OS companion app installation status
 * and opening Play Store to install it.
 */
object WearAppInstaller {
    private const val TAG = "WearAppInstaller"
    private const val WEAR_APP_PACKAGE = "press.pelldom.sessionledger"
    
    /**
     * Checks if the Wear OS companion app is installed.
     * Note: Since mobile and wear apps share the same package name,
     * this checks the local device. For connected watches, use WearDetectionHelper.
     */
    fun isWearAppInstalled(context: Context): Boolean {
        return try {
            val packageManager = context.packageManager
            packageManager.getPackageInfo(WEAR_APP_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        } catch (e: Exception) {
            Log.w(TAG, "Error checking wear app installation: ${e.message}")
            false
        }
    }
    
    /**
     * Opens Play Store to install the Wear OS companion app.
     * Uses market:// intent with fallback to https:// URL.
     * Returns true if intent was launched successfully.
     */
    fun openPlayStoreForWearApp(context: Context): Boolean {
        return try {
            // Try market:// intent first (Play Store app)
            val marketUri = Uri.parse("market://details?id=$WEAR_APP_PACKAGE&pcampaignid=wear")
            val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            try {
                context.startActivity(marketIntent)
                Log.d(TAG, "Opened Play Store app for wear app")
                return true
            } catch (e: Exception) {
                // Play Store app not available, try web URL
                Log.d(TAG, "Play Store app not available, trying web URL")
            }
            
            // Fallback to web Play Store
            val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$WEAR_APP_PACKAGE")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            context.startActivity(webIntent)
            Log.d(TAG, "Opened Play Store web for wear app")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to open Play Store: ${e.message}", e)
            false
        }
    }
}
