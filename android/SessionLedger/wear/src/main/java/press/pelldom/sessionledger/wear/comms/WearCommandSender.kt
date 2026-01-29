package press.pelldom.sessionledger.wear.comms

import android.content.Context
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import press.pelldom.sessionledger.wear.datalayer.WearSessionPaths
import java.util.concurrent.ExecutionException

/**
 * Stateless command sender for Wear OS to Phone communication.
 * 
 * Used by both the Wear UI and the Wear Tile to send session control commands.
 * Sends messages to ALL connected nodes and awaits completion.
 */
object WearCommandSender {
    private const val TAG = "WearCommandSender"

    /**
     * Sends a START command with optional category ID.
     * @param context Application context
     * @param categoryId Optional category ID to use for the session
     * @return true if sent successfully to at least one node, false otherwise
     */
    suspend fun sendStart(context: Context, categoryId: String? = null): Boolean {
        val payload = categoryId?.toByteArray(Charsets.UTF_8) ?: ByteArray(0)
        return sendCommand(context, WearSessionPaths.START, payload)
    }

    /**
     * Sends a PAUSE command.
     * @return true if sent successfully to at least one node, false otherwise
     */
    suspend fun sendPause(context: Context): Boolean {
        return sendCommand(context, WearSessionPaths.PAUSE)
    }

    /**
     * Sends a RESUME command.
     * @return true if sent successfully to at least one node, false otherwise
     */
    suspend fun sendResume(context: Context): Boolean {
        return sendCommand(context, WearSessionPaths.RESUME)
    }

    /**
     * Sends a STOP/END command.
     * @return true if sent successfully to at least one node, false otherwise
     */
    suspend fun sendStop(context: Context): Boolean {
        return sendCommand(context, WearSessionPaths.END)
    }

    /**
     * Internal method to send a command to all connected nodes.
     * @param context Application context
     * @param path Command path (e.g., "/session/start")
     * @param payload Optional payload bytes
     * @return true if sent successfully to at least one node, false otherwise
     */
    private suspend fun sendCommand(context: Context, path: String, payload: ByteArray = ByteArray(0)): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val nodeClient: NodeClient = Wearable.getNodeClient(context)
                val messageClient: MessageClient = Wearable.getMessageClient(context)
                
                val nodes = try {
                    Tasks.await(nodeClient.connectedNodes)
                } catch (e: ExecutionException) {
                    val cause = e.cause
                    if (cause is ApiException && cause.statusCode == 4000) {
                        Log.d(TAG, "No connected nodes (TARGET_NODE_NOT_CONNECTED) for command: $path")
                        return@withContext false
                    }
                    Log.w(TAG, "Error getting connected nodes: ${e.message}", e)
                    return@withContext false
                } catch (e: ApiException) {
                    if (e.statusCode == 4000) {
                        Log.d(TAG, "No connected nodes (TARGET_NODE_NOT_CONNECTED) for command: $path")
                        return@withContext false
                    }
                    Log.w(TAG, "API error getting connected nodes: ${e.statusCode} - ${e.message}")
                    return@withContext false
                } catch (e: Exception) {
                    Log.w(TAG, "Error getting connected nodes: ${e.message}", e)
                    return@withContext false
                }
                
                if (nodes.isEmpty()) {
                    Log.w(TAG, "No connected nodes available for command: $path")
                    return@withContext false
                }

                var successCount = 0
                for (node in nodes) {
                    try {
                        Tasks.await(messageClient.sendMessage(node.id, path, payload))
                        successCount++
                        Log.d(TAG, "Sent command $path to node: ${node.id}")
                    } catch (e: ExecutionException) {
                        // Unwrap ApiException from ExecutionException
                        val cause = e.cause
                        if (cause is ApiException) {
                            when (cause.statusCode) {
                                4000 -> { // TARGET_NODE_NOT_CONNECTED
                                    Log.d(TAG, "Node ${node.id} not connected, skipping command $path")
                                }
                                else -> {
                                    Log.w(TAG, "API error sending command $path to node ${node.id}: ${cause.statusCode} - ${cause.message}")
                                }
                            }
                        } else {
                            Log.w(TAG, "Failed to send command $path to node ${node.id}: ${e.message}", e)
                        }
                    } catch (e: ApiException) {
                        when (e.statusCode) {
                            4000 -> { // TARGET_NODE_NOT_CONNECTED
                                Log.d(TAG, "Node ${nodes.indexOf(node)} not connected, skipping command $path")
                            }
                            else -> {
                                Log.w(TAG, "API error sending command $path: ${e.statusCode} - ${e.message}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to send command $path to node ${node.id}: ${e.message}", e)
                    }
                }

                val success = successCount > 0
                if (!success) {
                    Log.w(TAG, "Failed to send command $path to any connected node")
                }
                success
            } catch (e: Exception) {
                Log.e(TAG, "Error sending command $path: ${e.message}", e)
                false
            }
        }
    }
}
