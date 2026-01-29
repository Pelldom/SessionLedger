package press.pelldom.sessionledger.wear.tile

import android.content.Intent
import android.util.Log
import androidx.wear.tiles.ActionBuilders
import androidx.wear.tiles.DimensionBuilders
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.ModifiersBuilders
import androidx.wear.tiles.TypeBuilders
import androidx.wear.tiles.material.Text
import androidx.wear.tiles.material.Typography
import androidx.wear.tiles.ColorBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.google.android.gms.tasks.Tasks
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import press.pelldom.sessionledger.wear.MainActivity
import press.pelldom.sessionledger.wear.datalayer.WearSessionPaths
import press.pelldom.sessionledger.wear.tile.TileStartActivity
import press.pelldom.sessionledger.wear.tile.TilePauseActivity
import press.pelldom.sessionledger.wear.tile.TileResumeActivity
import press.pelldom.sessionledger.wear.tile.TileStopActivity
import java.util.concurrent.TimeUnit

/**
 * Wear OS Tile for SessionLedger.
 *
 * Low-power control surface:
 * - No elapsed time
 * - No timers or background work
 * - Commands sent only on explicit taps
 */
class SessionLedgerTileService : TileService() {
    private val tag = "SessionLedgerTile"
    private val executor = Executors.newSingleThreadExecutor()

    companion object {
        init {
            Log.d("SessionLedgerTile", "SessionLedgerTileService class loaded")
        }
    }

    init {
        Log.d(tag, "SessionLedgerTileService instance initialized")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "SessionLedgerTileService.onCreate() called")
    }

    override fun onDestroy() {
        Log.d(tag, "SessionLedgerTileService.onDestroy() called")
        executor.shutdown()
        super.onDestroy()
    }

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        Log.d(tag, "Tile request received")
        
        // Run readSessionState on background thread since it uses Tasks.await()
        return Futures.submit(Callable {
            try {
                val sessionState = readSessionState()
                val deviceParams = requestParams.deviceParameters!!
                
                TileBuilders.Tile.Builder()
                    .setResourcesVersion("1")
                    .setTimeline(
                        androidx.wear.tiles.TimelineBuilders.Timeline.Builder()
                            .addTimelineEntry(
                                androidx.wear.tiles.TimelineBuilders.TimelineEntry.Builder()
                                    .setLayout(createTileLayout(sessionState, deviceParams))
                                    .setValidity(
                                        androidx.wear.tiles.TimelineBuilders.TimeInterval.Builder()
                                            // Refresh every 5 minutes max, or on state changes
                                            .setStartMillis(System.currentTimeMillis())
                                            .setEndMillis(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5))
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build()
            } catch (e: Exception) {
                Log.e(tag, "Error creating tile: ${e.message}", e)
                e.printStackTrace()
                // Return fallback tile on error
                try {
                    createFallbackTile()
                } catch (e2: Exception) {
                    Log.e(tag, "Error creating fallback tile: ${e2.message}", e2)
                    e2.printStackTrace()
                    // Last resort: return minimal tile
                    TileBuilders.Tile.Builder()
                        .setResourcesVersion("1")
                        .setTimeline(
                            androidx.wear.tiles.TimelineBuilders.Timeline.Builder()
                                .addTimelineEntry(
                                    androidx.wear.tiles.TimelineBuilders.TimelineEntry.Builder()
                                        .setLayout(
                                            LayoutElementBuilders.Layout.Builder()
                                                .setRoot(
                                                    LayoutElementBuilders.Column.Builder()
                                                        .addContent(
                                                            Text.Builder(this@SessionLedgerTileService, "Error")
                                                                .setTypography(Typography.TYPOGRAPHY_BODY1)
                                                                .setColor(ColorBuilders.ColorProp.Builder().setArgb(0xFFFF0000.toInt()).build())
                                                                .build()
                                                        )
                                                        .build()
                                                )
                                                .build()
                                        )
                                        .build()
                                )
                                .build()
                        )
                        .build()
                }
            }
        }, executor)
    }
    
    override fun onResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> {
        return Futures.immediateFuture(
            try {
                val resourceId = resources.getIdentifier(
                    "ic_sessionledger_complication",
                    "drawable",
                    packageName
                )
                if (resourceId == 0) {
                    Log.w(tag, "Resource ic_sessionledger_complication not found")
                    ResourceBuilders.Resources.Builder().setVersion("1").build()
                } else {
                    ResourceBuilders.Resources.Builder()
                        .setVersion("1")
                        .addIdToImageMapping(
                            "ic_sessionledger_complication",
                            ResourceBuilders.ImageResource.Builder()
                                .setAndroidResourceByResId(
                                    ResourceBuilders.AndroidImageResourceByResId.Builder()
                                        .setResourceId(resourceId)
                                        .build()
                                )
                                .build()
                        )
                        .build()
                }
            } catch (e: Exception) {
                Log.e(tag, "Error in onResourcesRequest: ${e.message}", e)
                ResourceBuilders.Resources.Builder().setVersion("1").build()
            }
        )
    }

    private data class SessionState(
        val hasActiveSession: Boolean,
        val state: String, // "NONE", "RUNNING", "PAUSED"
        val startTimeMillis: Long?,
        val totalPausedMillis: Long,
        val lastStateChangeTimeMillis: Long
    )

    private fun readSessionState(): SessionState {
        return try {
            val dataClient: DataClient = Wearable.getDataClient(this)
            // Use a timeout to avoid hanging if Wear API is unavailable
            val dataItems = Tasks.await(dataClient.dataItems, 2, java.util.concurrent.TimeUnit.SECONDS)
            
            try {
                val sessionStateItem = dataItems.firstOrNull { 
                    it.uri.path == WearSessionPaths.SESSION_STATE 
                }
                
                if (sessionStateItem == null) {
                    Log.d(tag, "No session state data item found")
                    return SessionState(
                        hasActiveSession = false,
                        state = "NONE",
                        startTimeMillis = null,
                        totalPausedMillis = 0L,
                        lastStateChangeTimeMillis = 0L
                    )
                }
                
                val map = DataMapItem.fromDataItem(sessionStateItem).dataMap
                val rawState = map.getString(WearSessionPaths.KEY_STATE, "NONE")
                val startTime = if (map.containsKey(WearSessionPaths.KEY_START_TIME_MILLIS)) {
                    map.getLong(WearSessionPaths.KEY_START_TIME_MILLIS)
                } else {
                    null
                }
                val totalPaused = map.getLong(WearSessionPaths.KEY_TOTAL_PAUSED_MILLIS, 0L)
                val lastStateChange = map.getLong(WearSessionPaths.KEY_LAST_STATE_CHANGE_TIME_MILLIS, 0L)
                
                val hasActiveSession = rawState != "NONE" && startTime != null
                
                SessionState(
                    hasActiveSession = hasActiveSession,
                    state = rawState,
                    startTimeMillis = startTime,
                    totalPausedMillis = totalPaused,
                    lastStateChangeTimeMillis = lastStateChange
                )
            } finally {
                dataItems.release()
            }
        } catch (e: Exception) {
            Log.w(tag, "Error reading session state: ${e.message}", e)
            SessionState(
                hasActiveSession = false,
                state = "NONE",
                startTimeMillis = null,
                totalPausedMillis = 0L,
                lastStateChangeTimeMillis = 0L
            )
        }
    }

    private fun createButtonClickable(action: String): ModifiersBuilders.Clickable {
        // Each button uses a distinct helper activity wired to a specific command.
        val activityClassName = when (action) {
            TileCommandReceiver.ACTION_START -> TileStartActivity::class.java.name
            TileCommandReceiver.ACTION_PAUSE -> TilePauseActivity::class.java.name
            TileCommandReceiver.ACTION_RESUME -> TileResumeActivity::class.java.name
            TileCommandReceiver.ACTION_STOP -> TileStopActivity::class.java.name
            else -> TileStartActivity::class.java.name
        }
        return ModifiersBuilders.Clickable.Builder()
            .setId(action)
            .setOnClick(
                ActionBuilders.LaunchAction.Builder()
                    .setAndroidActivity(
                        ActionBuilders.AndroidActivity.Builder()
                            .setClassName(activityClassName)
                            .setPackageName(packageName)
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun createTileLayout(
        sessionState: SessionState,
        deviceParams: androidx.wear.tiles.DeviceParametersBuilders.DeviceParameters
    ): LayoutElementBuilders.Layout {
        // Status text at top
        val statusText = when {
            !sessionState.hasActiveSession -> "No Active Session"
            sessionState.state == "PAUSED" -> "Session Paused"
            else -> "Session Active"
        }

        // State-dependent control buttons
        val controlsRow = when {
            !sessionState.hasActiveSession -> {
                // NONE -> [Start]
                LayoutElementBuilders.Row.Builder()
                    .addContent(createButton("Start", TileCommandReceiver.ACTION_START))
                    .build()
            }
            sessionState.state == "PAUSED" -> {
                // PAUSED -> [Resume] [Stop]
                LayoutElementBuilders.Row.Builder()
                    .addContent(createButton("Resume", TileCommandReceiver.ACTION_RESUME))
                    .addContent(
                        LayoutElementBuilders.Spacer.Builder()
                            .setWidth(
                                DimensionBuilders.DpProp.Builder()
                                    .setValue(8f)
                                    .build()
                            )
                            .build()
                    )
                    .addContent(createButton("Stop", TileCommandReceiver.ACTION_STOP))
                    .build()
            }
            else -> {
                // RUNNING/ACTIVE -> [Pause] [Stop]
                LayoutElementBuilders.Row.Builder()
                    .addContent(createButton("Pause", TileCommandReceiver.ACTION_PAUSE))
                    .addContent(
                        LayoutElementBuilders.Spacer.Builder()
                            .setWidth(
                                DimensionBuilders.DpProp.Builder()
                                    .setValue(8f)
                                    .build()
                            )
                            .build()
                    )
                    .addContent(createButton("Stop", TileCommandReceiver.ACTION_STOP))
                    .build()
            }
        }

        // "Detail" link at bottom to open full SLw app
        val detailClickable = ModifiersBuilders.Clickable.Builder()
            .setId("detail")
            .setOnClick(
                ActionBuilders.LaunchAction.Builder()
                    .setAndroidActivity(
                        ActionBuilders.AndroidActivity.Builder()
                            .setClassName(MainActivity::class.java.name)
                            .setPackageName(packageName)
                            .build()
                    )
                    .build()
            )
            .build()

        val detailText = Text.Builder(
            this@SessionLedgerTileService,
            "Detail"
        )
            .setTypography(Typography.TYPOGRAPHY_BODY2)
            .setColor(
                ColorBuilders.ColorProp.Builder()
                    .setArgb(0xFFFFFFFF.toInt())
                    .build()
            )
            .setMaxLines(1)
            .setOverflow(LayoutElementBuilders.TEXT_OVERFLOW_ELLIPSIZE_END)
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setClickable(detailClickable)
                    .setPadding(
                        ModifiersBuilders.Padding.Builder()
                            .setTop(
                                DimensionBuilders.DpProp.Builder()
                                    .setValue(8f)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()

        // Foreground column (status, controls, detail)
        val foregroundColumn = LayoutElementBuilders.Column.Builder()
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setPadding(
                        ModifiersBuilders.Padding.Builder()
                            .setAll(
                                DimensionBuilders.DpProp.Builder()
                                    .setValue(16f)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .addContent(
                Text.Builder(this@SessionLedgerTileService, statusText)
                    .setTypography(Typography.TYPOGRAPHY_BODY2)
                    .setColor(
                        ColorBuilders.ColorProp.Builder()
                            .setArgb(0xFFFFFFFF.toInt())
                            .build()
                    )
                    .setMaxLines(1)
                    .setOverflow(LayoutElementBuilders.TEXT_OVERFLOW_ELLIPSIZE_END)
                    .build()
            )
            .addContent(
                LayoutElementBuilders.Spacer.Builder()
                    .setHeight(
                        DimensionBuilders.DpProp.Builder()
                            .setValue(8f)
                            .build()
                    )
                    .build()
            )
            .addContent(controlsRow)
            .addContent(
                LayoutElementBuilders.Spacer.Builder()
                    .setHeight(
                        DimensionBuilders.DpProp.Builder()
                            .setValue(8f)
                            .build()
                    )
                    .build()
            )
            .addContent(detailText)
            .build()

        // Background watermark icon - sized with 3-5dp buffer from screen edge
        // Icon viewport is 100x100, ring outer edge is at radius 50 (viewport edge)
        // Screen is ~80dp (radius 40dp), want outer edge at 35-37dp from center (3-5dp buffer)
        // Scale factor: 35/50 = 0.7 to 37/50 = 0.74
        // Using 0.72 (middle of range) = 80 * 0.72 = 57.6dp, rounded to 58dp
        // But since we need the ring visible, using ~280% (224dp) with slight reduction for buffer
        val screenSizeDp = 80f
        val watermarkSize = screenSizeDp * 2.8f // 280% of screen size with 3-5dp buffer from edge
        
        val watermarkImage = LayoutElementBuilders.Image.Builder()
            .setResourceId("ic_sessionledger_complication")
            .setWidth(
                DimensionBuilders.DpProp.Builder()
                    .setValue(watermarkSize)
                    .build()
            )
            .setHeight(
                DimensionBuilders.DpProp.Builder()
                    .setValue(watermarkSize)
                    .build()
            )
            .build()

        val rootBox = LayoutElementBuilders.Box.Builder()
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setBackground(
                        ModifiersBuilders.Background.Builder()
                            .setColor(
                                ColorBuilders.ColorProp.Builder()
                                    .setArgb(0xFF000000.toInt()) // Black background
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .addContent(watermarkImage)
            .addContent(foregroundColumn)
            .build()

        return LayoutElementBuilders.Layout.Builder()
            .setRoot(rootBox)
            .build()
    }

    private fun createButton(text: String, action: String): LayoutElementBuilders.LayoutElement {
        // Material 3 styled button with rounded corners
        val isPrimary = action == TileCommandReceiver.ACTION_START || action == TileCommandReceiver.ACTION_RESUME
        val backgroundColor = if (isPrimary) {
            0xFF6750A4.toInt() // Material 3 primary
        } else {
            0xFF625B71.toInt() // Material 3 secondary
        }
        
        // Material 3 buttons have rounded corners (typically 20dp radius for full buttons)
        val cornerRadius = 20f
        
        return LayoutElementBuilders.Box.Builder()
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setClickable(createButtonClickable(action))
                    .setBackground(
                        ModifiersBuilders.Background.Builder()
                            .setColor(
                                ColorBuilders.ColorProp.Builder()
                                    .setArgb(backgroundColor)
                                    .build()
                            )
                            .setCorner(
                                ModifiersBuilders.Corner.Builder()
                                    .setRadius(
                                        DimensionBuilders.DpProp.Builder()
                                            .setValue(cornerRadius)
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .setPadding(
                        ModifiersBuilders.Padding.Builder()
                            .setStart(
                                DimensionBuilders.DpProp.Builder()
                                    .setValue(16f)
                                    .build()
                            )
                            .setEnd(
                                DimensionBuilders.DpProp.Builder()
                                    .setValue(16f)
                                    .build()
                            )
                            .setTop(
                                DimensionBuilders.DpProp.Builder()
                                    .setValue(12f)
                                    .build()
                            )
                            .setBottom(
                                DimensionBuilders.DpProp.Builder()
                                    .setValue(12f)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .addContent(
                Text.Builder(
                    this@SessionLedgerTileService,
                    text
                )
                    .setTypography(Typography.TYPOGRAPHY_BUTTON)
                    .setColor(
                        ColorBuilders.ColorProp.Builder()
                            .setArgb(0xFFFFFFFF.toInt()) // White text
                            .build()
                    )
                    .setMaxLines(1)
                    .setOverflow(LayoutElementBuilders.TEXT_OVERFLOW_ELLIPSIZE_END)
                    .build()
            )
            .build()
    }

    private fun createFallbackTile(): TileBuilders.Tile {
        return TileBuilders.Tile.Builder()
            .setResourcesVersion("1")
            .setTimeline(
                androidx.wear.tiles.TimelineBuilders.Timeline.Builder()
                    .addTimelineEntry(
                        androidx.wear.tiles.TimelineBuilders.TimelineEntry.Builder()
                            .setLayout(
                                LayoutElementBuilders.Layout.Builder()
                                    .setRoot(
                                        LayoutElementBuilders.Column.Builder()
                                            .setModifiers(
                                                ModifiersBuilders.Modifiers.Builder()
                                                    .setBackground(
                                                        ModifiersBuilders.Background.Builder()
                                                            .setColor(
                                                                ColorBuilders.ColorProp.Builder()
                                                                    .setArgb(0xFF000000.toInt())
                                                                    .build()
                                                            )
                                                            .build()
                                                    )
                                                    .setPadding(
                                                        ModifiersBuilders.Padding.Builder()
                                                            .setAll(
                                                                DimensionBuilders.DpProp.Builder()
                                                                    .setValue(16f)
                                                                    .build()
                                                            )
                                                            .build()
                                                    )
                                                    .build()
                                            )
                                            .addContent(
                                                Text.Builder(
                                                    this@SessionLedgerTileService,
                                                    "No active session"
                                                )
                                                    .setTypography(Typography.TYPOGRAPHY_BODY1)
                                                    .setColor(
                                                        ColorBuilders.ColorProp.Builder()
                                                            .setArgb(0xFFFFFFFF.toInt())
                                                            .build()
                                                    )
                                                    .build()
                                            )
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }
}
