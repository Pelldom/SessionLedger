package press.pelldom.sessionledger.wear.complication

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.MonochromaticImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import press.pelldom.sessionledger.wear.MainActivity
import press.pelldom.sessionledger.wear.R

class SessionLedgerComplicationService : ComplicationDataSourceService() {
    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        listener.onComplicationData(buildData(request.complicationType))
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? = buildData(type)

    private fun buildData(type: ComplicationType): ComplicationData? {
        val contentDescription =
            PlainComplicationText.Builder(getString(R.string.complication_content_description)).build()

        val icon = Icon.createWithResource(this, R.drawable.ic_sessionledger_complication)
        val mono = MonochromaticImage.Builder(icon).build()

        val tapIntent = Intent(this, MainActivity::class.java).apply {
            // Bring app to foreground if already running.
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val tapAction = PendingIntent.getActivity(
            this,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return when (type) {
            ComplicationType.MONOCHROMATIC_IMAGE -> {
                MonochromaticImageComplicationData.Builder(mono, contentDescription)
                    .setTapAction(tapAction)
                    .build()
            }

            ComplicationType.SHORT_TEXT -> {
                val text = PlainComplicationText.Builder(getString(R.string.complication_short_text)).build()
                ShortTextComplicationData.Builder(text, contentDescription)
                    .setMonochromaticImage(mono)
                    .setTapAction(tapAction)
                    .build()
            }

            else -> null
        }
    }
}

