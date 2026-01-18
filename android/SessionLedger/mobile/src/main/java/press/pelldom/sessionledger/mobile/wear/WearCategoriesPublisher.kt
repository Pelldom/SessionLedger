package press.pelldom.sessionledger.mobile.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
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

    fun start(context: Context) {
        if (started) return
        started = true

        val appContext = context.applicationContext
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
        val dataClient = Wearable.getDataClient(context)
        val put = PutDataMapRequest.create(WearSessionPaths.CATEGORIES)
        put.dataMap.putStringArrayList(WearSessionPaths.KEY_CATEGORY_IDS, ids)
        put.dataMap.putStringArrayList(WearSessionPaths.KEY_CATEGORY_NAMES, names)
        put.dataMap.putLong("updatedAtMs", System.currentTimeMillis())

        Log.d(TAG, "Publish /categories: ${ids.size} items")
        Tasks.await(dataClient.putDataItem(put.asPutDataRequest().setUrgent()))
    }
}

