package press.pelldom.sessionledger.mobile.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import press.pelldom.sessionledger.mobile.data.db.AppDatabase
import press.pelldom.sessionledger.mobile.data.db.DefaultCategory

object WearCategoriesPublisher {
    private const val TAG = "SessionLedgerWear"
    @Volatile private var started = false
    private var job: Job? = null

    /**
     * Checks if Wear API is available on this device.
     * Returns false if Wearable.API is unavailable (e.g., on phones without Wear OS).
     */
    private fun isWearApiAvailable(context: Context): Boolean {
        return try {
            // Try to get the NodeClient - this will throw ApiException if API is unavailable
            val nodeClient: NodeClient = Wearable.getNodeClient(context)
            // Try to access a property to trigger any lazy initialization that might fail
            // The API might not throw until we actually use it, so we check by attempting to get connected nodes
            // We don't await it, just check if the call is possible
            val task = nodeClient.connectedNodes
            // If we got here without exception, API is available
            true
        } catch (e: ApiException) {
            if (e.statusCode == ConnectionResult.API_UNAVAILABLE) {
                Log.d(TAG, "Wear API not available on this device")
                return false
            }
            // Other API exceptions - assume available but may fail later
            Log.w(TAG, "Wear API check returned ApiException: ${e.message}")
            true
        } catch (e: Exception) {
            // Any other exception means API is likely unavailable
            Log.d(TAG, "Wear API unavailable (exception: ${e.javaClass.simpleName}): ${e.message}")
            false
        }
    }

    fun start(context: Context) {
        if (started) return
        
        val appContext = context.applicationContext
        
        // Check Wear API availability BEFORE starting any coroutines
        if (!isWearApiAvailable(appContext)) {
            Log.d(TAG, "Wear API unavailable - not starting category publisher")
            return
        }
        
        started = true

        val db = AppDatabase.getInstance(appContext)
        val scope = CoroutineScope(Dispatchers.IO)

        job = scope.launch {
            db.categoryDao()
                .observeAllCategories()
                .distinctUntilChanged()
                .collect { cats ->
                    val ids = ArrayList<String>()
                    val names = ArrayList<String>()

                    // Always include Uncategorized.
                    val unc = cats.firstOrNull { it.id == DefaultCategory.UNCATEGORIZED_ID }
                    if (unc != null) {
                        ids.add(unc.id)
                        names.add(unc.name)
                    } else {
                        ids.add(DefaultCategory.UNCATEGORIZED_ID)
                        names.add(DefaultCategory.UNCATEGORIZED_NAME)
                    }

                    for (c in cats) {
                        if (c.id == DefaultCategory.UNCATEGORIZED_ID) continue
                        ids.add(c.id)
                        names.add(c.name)
                    }

                    publishOnce(appContext, ids, names)
                }
        }
    }

    private fun publishOnce(context: Context, ids: ArrayList<String>, names: ArrayList<String>) {
        try {
            val dataClient = Wearable.getDataClient(context)
            val put = PutDataMapRequest.create(WearSessionPaths.CATEGORIES)
            put.dataMap.putStringArrayList(WearSessionPaths.KEY_CATEGORY_IDS, ids)
            put.dataMap.putStringArrayList(WearSessionPaths.KEY_CATEGORY_NAMES, names)
            put.dataMap.putLong("updatedAtMs", System.currentTimeMillis())

            Log.d(TAG, "Publish /categories: ${ids.size} items")
            Tasks.await(dataClient.putDataItem(put.asPutDataRequest().setUrgent()))
        } catch (e: ApiException) {
            if (e.statusCode == ConnectionResult.API_UNAVAILABLE) {
                Log.d(TAG, "Wear API unavailable during publish - stopping publisher")
                // Stop the publisher if API becomes unavailable
                started = false
                job?.cancel()
                return
            }
            Log.w(TAG, "Wear API error during publish: ${e.message}", e)
        } catch (e: Exception) {
            Log.w(TAG, "Error publishing categories to Wear: ${e.message}", e)
        }
    }
}

