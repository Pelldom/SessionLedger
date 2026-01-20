package press.pelldom.sessionledger.wear.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import press.pelldom.sessionledger.wear.MainActivity
import press.pelldom.sessionledger.wear.R

class SessionLedgerOngoingService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val state = intent?.getStringExtra(EXTRA_STATE) ?: STATE_RUNNING
        startOrUpdateForeground(state)
        return START_NOT_STICKY
    }

    private fun startOrUpdateForeground(state: String) {
        ensureChannel()

        val requestedIcon = when (state) {
            STATE_PAUSED -> R.drawable.sessionledger_mark_c_notification
            else -> R.drawable.sessionledger_mark_c_monochrome
        }

        val contentText = when (state) {
            STATE_PAUSED -> "Session paused"
            else -> "Session running"
        }

        val launchIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val notification = buildNotification(
                iconRes = requestedIcon,
                contentText = contentText,
                contentIntent = contentIntent
            )
            // Use the 2-arg startForeground to avoid requiring a specific foreground service type.
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            // Defensive fallback: never crash due to icon/resource issues. Try monochrome for all states.
            try {
                val fallbackNotification = buildNotification(
                    iconRes = R.drawable.sessionledger_mark_c_monochrome,
                    contentText = contentText,
                    contentIntent = contentIntent
                )
                startForeground(NOTIFICATION_ID, fallbackNotification)
            } catch (_: Exception) {
                // Last resort: fail closed (no indicator) but keep the app alive.
                stopSelf()
            }
        }
    }

    private fun buildNotification(
        iconRes: Int,
        contentText: String,
        contentIntent: PendingIntent
    ): android.app.Notification {
        // Ensure the icon resource exists; this throws Resources.NotFoundException if missing/invalid.
        @Suppress("DEPRECATION")
        resources.getDrawable(iconRes)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle("SessionLedger")
            .setContentText(contentText)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = nm.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Session Activity",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setSound(null, null)
            enableVibration(false)
            vibrationPattern = longArrayOf(0L)
            setShowBadge(false)
        }

        nm.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "session_activity"
        private const val NOTIFICATION_ID = 1001

        private const val EXTRA_STATE = "state"
        private const val STATE_RUNNING = "RUNNING"
        private const val STATE_PAUSED = "PAUSED"

        fun start(context: Context, isPaused: Boolean) {
            val intent = Intent(context, SessionLedgerOngoingService::class.java).apply {
                putExtra(EXTRA_STATE, if (isPaused) STATE_PAUSED else STATE_RUNNING)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, SessionLedgerOngoingService::class.java))
        }
    }
}

