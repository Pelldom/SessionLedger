package press.pelldom.sessionledger.mobile.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.first
import press.pelldom.sessionledger.mobile.billing.BillingEngine
import press.pelldom.sessionledger.mobile.billing.RoundingMode
import press.pelldom.sessionledger.mobile.billing.SourceType
import press.pelldom.sessionledger.mobile.data.db.AppDatabase
import press.pelldom.sessionledger.mobile.settings.SettingsRepository

class CsvExporter {

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
            .observeEndedSessionsNewestFirst()
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
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)
        val fileStampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm", Locale.US)

        val header = listOf(
            "session_id",
            "category_id",
            "category_name",
            "notes",
            "start_local",
            "end_local",
            "state",
            "paused_seconds",
            "tracked_seconds",
            "rate_used_per_hour",
            "rate_source",
            "rounding_mode_used",
            "rounding_direction_used",
            "rounding_source",
            "min_time_seconds_used",
            "min_time_source",
            "min_charge_used",
            "min_charge_source",
            "rounded_seconds",
            "billable_seconds",
            "cost_pre_min_charge",
            "final_cost",
            "currency",
            "created_on_device",
            "updated_at_local"
        ).joinToString(separator = ",")

        val csv = buildString {
            appendLine(header)

            for (session in sessions) {
                val category = db.categoryDao().getById(session.categoryId)
                val settings = settingsRepo.observeGlobalSettings().first()
                val billing = BillingEngine.calculateForEndedSession(session, category, settings)

                val startLocal = formatLocal(session.startTimeMs, zoneId, dateTimeFormatter)
                val endLocal = formatLocal(requireNotNull(session.endTimeMs), zoneId, dateTimeFormatter)
                val updatedAtLocal = formatLocal(session.updatedAtMs, zoneId, dateTimeFormatter)

                val roundingDirectionUsed = when (billing.resolved.roundingMode) {
                    RoundingMode.EXACT -> "none"
                    RoundingMode.SIX_MINUTE -> billing.resolved.roundingDirection?.name?.lowercase(Locale.US) ?: "none"
                }

                val values = listOf(
                    csvEscape(session.id),
                    csvEscape(session.categoryId),
                    csvEscape(category?.name ?: ""),
                    csvEscape(session.notes ?: ""),
                    csvEscape(startLocal),
                    csvEscape(endLocal),
                    csvEscape(session.state.name),
                    (session.pausedTotalMs / 1000L).toString(),
                    billing.trackedSeconds.toString(),
                    money2(billing.resolved.ratePerHour),
                    csvEscape(sourceToCsv(billing.resolved.rateSource)),
                    csvEscape(roundingModeToCsv(billing.resolved.roundingMode)),
                    csvEscape(roundingDirectionUsed),
                    csvEscape(sourceToCsv(billing.resolved.roundingSource)),
                    billing.resolved.minTimeSeconds.toString(),
                    csvEscape(sourceToCsv(billing.resolved.minTimeSource)),
                    money2(billing.resolved.minCharge),
                    csvEscape(sourceToCsv(billing.resolved.minChargeSource)),
                    billing.roundedSeconds.toString(),
                    billing.billableSeconds.toString(),
                    money2(billing.costPreMinCharge),
                    money2(billing.finalCost),
                    csvEscape(billing.resolved.currency),
                    csvEscape(session.createdOnDevice),
                    csvEscape(updatedAtLocal)
                )

                appendLine(values.joinToString(separator = ","))
            }
        }

        val nowLocal = LocalDateTime.now(zoneId)
        val fileName = "SessionLedger_Export_${nowLocal.format(fileStampFormatter)}.csv"
        val relativePath = Environment.DIRECTORY_DOWNLOADS + "/SessionLedger/"

        // Scoped storage friendly: write into Downloads/SessionLedger via MediaStore.
        // This produces a user-visible file without requesting broad storage permissions on modern Android.
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            if (Build.VERSION.SDK_INT >= 29) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val uri = resolver.insert(collection, values)
            ?: throw IllegalStateException("Unable to create export file in Downloads.")

        try {
            resolver.openOutputStream(uri, "w")?.use { out ->
                out.write(csv.toByteArray(Charsets.UTF_8))
            } ?: throw IllegalStateException("Unable to open export file for writing.")
        } finally {
            if (Build.VERSION.SDK_INT >= 29) {
                val finalizeValues = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
                resolver.update(uri, finalizeValues, null, null)
            }
        }

        return uri
    }

    private fun formatLocal(
        epochMs: Long,
        zoneId: ZoneId,
        formatter: DateTimeFormatter
    ): String {
        val zdt = Instant.ofEpochMilli(epochMs).atZone(zoneId)
        return zdt.format(formatter)
    }

    private fun roundingModeToCsv(mode: RoundingMode): String = when (mode) {
        RoundingMode.EXACT -> "exact"
        RoundingMode.SIX_MINUTE -> "six_minute"
    }

    private fun sourceToCsv(source: SourceType): String = when (source) {
        SourceType.SESSION -> "session"
        SourceType.CATEGORY -> "category"
        SourceType.GLOBAL -> "global"
        SourceType.NONE -> "none"
    }

    private fun money2(value: Double): String = String.format(Locale.US, "%.2f", value)

    private fun csvEscape(raw: String): String {
        val needsQuotes = raw.contains(',') || raw.contains('"') || raw.contains('\n') || raw.contains('\r')
        if (!needsQuotes) return raw
        val escaped = raw.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}

