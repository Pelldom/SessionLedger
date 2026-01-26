package press.pelldom.sessionledger.wear.tile

import android.content.Context
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
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import press.pelldom.sessionledger.wear.MainActivity
import press.pelldom.sessionledger.wear.datalayer.WearSessionPaths
import java.util.concurrent.TimeUnit

/**
 * Wear OS Tile v1 for SessionLedger.
 * 
 * READ-ONLY status tile that displays current session state.
 * Opens SessionLedger Wear app on tap.
 */
class SessionLedgerTileService : TileService() {
    private val tag = "SessionLedgerTile"

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        Log.d(tag, "Tile request received")
        
        return Futures.immediateFuture(
            try {
                val sessionState = readSessionState()
                // Category name not available in session state, show "Uncategorized" as fallback
                val categoryName = "Uncategorized"
                
                // Convert tiles DeviceParameters to protolayout DeviceParameters
                val deviceParams = convertDeviceParameters(requestParams.deviceParameters!!)
                
                TileBuilders.Tile.Builder()
                    .setResourcesVersion("1")
                    .setTimeline(
                        androidx.wear.tiles.TimelineBuilders.Timeline.Builder()
                            .addTimelineEntry(
                                androidx.wear.tiles.TimelineBuilders.TimelineEntry.Builder()
                                    .setLayout(
                                        createTileLayout(
                                            sessionState = sessionState,
                                            categoryName = categoryName,
                                            deviceParams = deviceParams
                                        )
                                    )
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
                Log.w(tag, "Error creating tile: ${e.message}", e)
                // Return fallback tile on error
                val deviceParams = convertDeviceParameters(requestParams.deviceParameters!!)
                createFallbackTile(deviceParams)
            }
        )
    }
    
    private fun convertDeviceParameters(
        tilesParams: androidx.wear.tiles.DeviceParametersBuilders.DeviceParameters
    ): androidx.wear.tiles.DeviceParametersBuilders.DeviceParameters {
        // No conversion needed - use tiles DeviceParameters directly
        return tilesParams
    }

    override fun onResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> {
        return Futures.immediateFuture(
            ResourceBuilders.Resources.Builder()
                .setVersion("1")
                .addIdToImageMapping(
                    "ic_sessionledger_complication",
                    ResourceBuilders.ImageResource.Builder()
                        .setAndroidResourceByResId(
                            ResourceBuilders.AndroidImageResourceByResId.Builder()
                                .setResourceId(
                                    resources.getIdentifier(
                                        "ic_sessionledger_complication",
                                        "drawable",
                                        packageName
                                    )
                                )
                                .build()
                        )
                        .build()
                )
                .build()
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
            val dataItems = Tasks.await(dataClient.dataItems)
            
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

    private fun computeElapsedTime(sessionState: SessionState): String {
        if (!sessionState.hasActiveSession || sessionState.startTimeMillis == null) {
            return "00:00"
        }
        
        val now = System.currentTimeMillis()
        val effectiveEnd = when (sessionState.state) {
            "PAUSED" -> sessionState.lastStateChangeTimeMillis.takeIf { it > 0L } ?: sessionState.startTimeMillis
            else -> now
        }
        
        val elapsed = (effectiveEnd - sessionState.startTimeMillis) - sessionState.totalPausedMillis
        val totalSeconds = (elapsed / 1000L).coerceAtLeast(0L)
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        
        return if (hours > 0L) {
            String.format("%d:%02d", hours, minutes)
        } else {
            String.format("%02d", minutes)
        }
    }

    private fun createTileLayout(
        sessionState: SessionState,
        categoryName: String,
        deviceParams: androidx.wear.tiles.DeviceParametersBuilders.DeviceParameters
    ): LayoutElementBuilders.Layout {
        val openAppIntent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        
        val clickable = ModifiersBuilders.Clickable.Builder()
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
        
        return if (sessionState.hasActiveSession) {
            // STATE B: Active Session
            LayoutElementBuilders.Layout.Builder()
                .setRoot(
                    LayoutElementBuilders.Column.Builder()
                        .setModifiers(
                            ModifiersBuilders.Modifiers.Builder()
                                .setClickable(clickable)
                                .setBackground(
                                    ModifiersBuilders.Background.Builder()
                                        .setColor(
                                            ColorBuilders.ColorProp.Builder()
                                                .setArgb(0xFF000000.toInt()) // Black background
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
                            // App icon (centered)
                            LayoutElementBuilders.Image.Builder()
                                .setResourceId("ic_sessionledger_complication")
                                .setWidth(
                                    DimensionBuilders.DpProp.Builder()
                                        .setValue(32f)
                                        .build()
                                )
                                .setHeight(
                                    DimensionBuilders.DpProp.Builder()
                                        .setValue(32f)
                                        .build()
                                )
                                .setModifiers(
                                    ModifiersBuilders.Modifiers.Builder()
                                        .setPadding(
                                            ModifiersBuilders.Padding.Builder()
                                                .setBottom(
                                                    DimensionBuilders.DpProp.Builder()
                                                        .setValue(8f)
                                                        .build()
                                                )
                                                .build()
                                        )
                                        .build()
                                )
                                .build()
                        )
                        .addContent(
                            // Large elapsed time (HH:MM)
                            Text.Builder(
                                this@SessionLedgerTileService,
                                computeElapsedTime(sessionState)
                            )
                                .setTypography(Typography.TYPOGRAPHY_DISPLAY1)
                                .setColor(
                                            ColorBuilders.ColorProp.Builder()
                                        .setArgb(0xFFFFFFFF.toInt()) // White text
                                        .build()
                                )
                                .setMaxLines(1)
                                .setOverflow(LayoutElementBuilders.TEXT_OVERFLOW_ELLIPSIZE_END)
                                .build()
                        )
                        .addContent(
                            // Category name (truncated with ellipsis)
                            Text.Builder(
                                this@SessionLedgerTileService,
                                categoryName
                            )
                                .setTypography(Typography.TYPOGRAPHY_BODY2)
                                .setColor(
                                            ColorBuilders.ColorProp.Builder()
                                        .setArgb(0xFFFFFFFF.toInt()) // White text
                                        .build()
                                )
                                .setMaxLines(1)
                                .setOverflow(LayoutElementBuilders.TEXT_OVERFLOW_ELLIPSIZE_END)
                                .setModifiers(
                                    ModifiersBuilders.Modifiers.Builder()
                                        .setPadding(
                                            ModifiersBuilders.Padding.Builder()
                                                .setTop(
                                                    DimensionBuilders.DpProp.Builder()
                                                        .setValue(4f)
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
        } else {
            // STATE A: No Active Session
            LayoutElementBuilders.Layout.Builder()
                .setRoot(
                    LayoutElementBuilders.Column.Builder()
                        .setModifiers(
                            ModifiersBuilders.Modifiers.Builder()
                                .setClickable(clickable)
                                .setBackground(
                                    ModifiersBuilders.Background.Builder()
                                        .setColor(
                                            ColorBuilders.ColorProp.Builder()
                                                .setArgb(0xFF000000.toInt()) // Black background
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
                            // App icon (centered)
                            LayoutElementBuilders.Image.Builder()
                                .setResourceId("ic_sessionledger_complication")
                                .setWidth(
                                    DimensionBuilders.DpProp.Builder()
                                        .setValue(32f)
                                        .build()
                                )
                                .setHeight(
                                    DimensionBuilders.DpProp.Builder()
                                        .setValue(32f)
                                        .build()
                                )
                                .setModifiers(
                                    ModifiersBuilders.Modifiers.Builder()
                                        .setPadding(
                                            ModifiersBuilders.Padding.Builder()
                                                .setBottom(
                                                    DimensionBuilders.DpProp.Builder()
                                                        .setValue(8f)
                                                        .build()
                                                )
                                                .build()
                                        )
                                        .build()
                                )
                                .build()
                        )
                        .addContent(
                            // "No active session" text
                            Text.Builder(
                                this@SessionLedgerTileService,
                                "No active session"
                            )
                                .setTypography(Typography.TYPOGRAPHY_BODY1)
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
                )
                .build()
        }
    }

    private fun createFallbackTile(
        deviceParams: androidx.wear.tiles.DeviceParametersBuilders.DeviceParameters
    ): TileBuilders.Tile {
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
