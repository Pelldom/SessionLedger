package press.pelldom.sessionledger.mobile.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.first
import press.pelldom.sessionledger.mobile.billing.BillingEngine
import press.pelldom.sessionledger.mobile.data.db.AppDatabase
import press.pelldom.sessionledger.mobile.settings.SettingsRepository

class CsvExporter {

    companion object {
        private const val TAG = "SL_EXPORT"
        // MediaStore Downloads relative paths are "Download/<subdir>" (typically ending with a slash when read back).
        const val EXPORT_RELATIVE_PATH: String = "Download/SessionLedger"
        const val EXPORT_MIME_TYPE: String = "text/csv"
    }

    suspend fun exportEndedSessions(
        db: AppDatabase,
        settingsRepo: SettingsRepository,
        startFilter: Long? = null,
        endFilter: Long? = null,
        categoryFilter: String? = null,
        categoryFilters: Set<String>? = null,
        context: Context
    ): Uri {
        val sessions = db.sessionDao()
            // Archived sessions are excluded from exports by default.
            .observeActiveEndedSessionsNewestFirst()
            .first()
            .asSequence()
            .filter { s ->
                val inStart = startFilter?.let { s.startTimeMs >= it } ?: true
                val inEnd = endFilter?.let { s.startTimeMs < it } ?: true
                val inCategory = when {
                    categoryFilters != null -> categoryFilters.contains(s.categoryId)
                    categoryFilter != null -> s.categoryId == categoryFilter
                    else -> true
                }
                inStart && inEnd && inCategory
            }
            .toList()

        val zoneId = ZoneId.systemDefault()
        val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
        val fileStampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm", Locale.US)

        // Timesheet-friendly CSV (one row per session).
        val header = listOf(
            "Date",
            "Start Time",
            "End Time",
            "Duration (hh:mm)",
            "Category",
            "Notes",
            "Hourly Rate",
            "Billable Amount"
        ).joinToString(separator = ",")

        val csv = buildString {
            appendLine(header)

            for (session in sessions) {
                val category = db.categoryDao().getById(session.categoryId)
                val settings = settingsRepo.observeGlobalSettings().first()
                val billing = BillingEngine.calculateForEndedSession(session, category, settings)

                val startZdt = Instant.ofEpochMilli(session.startTimeMs).atZone(zoneId)
                val endZdt = Instant.ofEpochMilli(requireNotNull(session.endTimeMs)).atZone(zoneId)

                val date = startZdt.toLocalDate().format(dateFormatter)
                val startTime = startZdt.toLocalTime().format(timeFormatter)
                val endTime = endZdt.toLocalTime().format(timeFormatter)
                val duration = formatDurationHhMm(billing.trackedSeconds)

                val currency = billing.resolved.currency
                val hourlyRate = moneyWithCurrency(currency, billing.resolved.ratePerHour)
                val billed = moneyWithCurrency(currency, billing.finalCost)

                val values = listOf(
                    csvEscape(date),
                    csvEscape(startTime),
                    csvEscape(endTime),
                    csvEscape(duration),
                    csvEscape(category?.name ?: "Uncategorized"),
                    csvEscape(""), // Notes (empty for now)
                    csvEscape(hourlyRate),
                    csvEscape(billed)
                )

                appendLine(values.joinToString(separator = ","))
            }
        }

        val nowLocal = LocalDateTime.now(zoneId)
        val fileName = "SessionLedger_Export_${nowLocal.format(fileStampFormatter)}.csv"
        val relativePath = EXPORT_RELATIVE_PATH

        // Scoped storage friendly: write into Downloads/SessionLedger via MediaStore.
        // This produces a user-visible file without requesting broad storage permissions on modern Android.
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, EXPORT_MIME_TYPE)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val uri = resolver.insert(collection, values)
        Log.d(TAG, "Insert into MediaStore uri=$uri")
        if (uri == null) {
            Log.d(TAG, "Insert returned null")
            throw IllegalStateException("Export failed: could not create file.")
        }

        try {
            val out = resolver.openOutputStream(uri, "w")
                ?: throw IllegalStateException("Export failed: could not open output stream.")
            Log.d(TAG, "Open OutputStream ok")
            out.use { stream ->
                val bytes = csv.toByteArray(Charsets.UTF_8)
                Log.d(TAG, "Write bytes count=${bytes.size}")
                stream.write(bytes)
                stream.flush()
            }

            val finalizeValues = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
            val updated = resolver.update(uri, finalizeValues, null, null)
            Log.d(TAG, "Finalize IS_PENDING=0 updateCount=$updated")
            if (updated <= 0) {
                throw IllegalStateException("Export failed: could not finalize file.")
            }
        } catch (t: Throwable) {
            // Best effort cleanup so we don't leave a pending/partial row behind.
            try {
                resolver.delete(uri, null, null)
            } catch (_: Throwable) {
                // ignore cleanup failure
            }
            throw t
        }

        return uri
    }

    private fun money2(value: Double): String = String.format(Locale.US, "%.2f", value)

    private fun moneyWithCurrency(currency: String, value: Double): String = "${currency.uppercase(Locale.US)} ${money2(value)}"

    private fun formatDurationHhMm(trackedSeconds: Long): String {
        val totalMinutes = (trackedSeconds.coerceAtLeast(0L) / 60L)
        val hours = totalMinutes / 60L
        val minutes = totalMinutes % 60L
        return String.format(Locale.US, "%02d:%02d", hours, minutes)
    }

    private fun csvEscape(raw: String): String {
        val needsQuotes = raw.contains(',') || raw.contains('"') || raw.contains('\n') || raw.contains('\r')
        if (!needsQuotes) return raw
        val escaped = raw.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}

