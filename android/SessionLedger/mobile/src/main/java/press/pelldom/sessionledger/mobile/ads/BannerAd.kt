package press.pelldom.sessionledger.mobile.ads

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdListener
import press.pelldom.sessionledger.mobile.AppConfig

/**
 * Always-visible banner ad slot.
 *
 * - Adaptive size
 * - Loads once per app launch (per composition lifetime)
 * - Reserved fixed space (height = adaptive height) so content never overlays the ad
 */
@Composable
fun BannerAd(
    enabled: Boolean = true
) {
    if (!enabled) return

    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    // Compute ad width in dp for adaptive sizing.
    val adWidthDp = configuration.screenWidthDp
    val adSize = remember(adWidthDp) {
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)
    }

    // Initialize AdMob once per app launch.
    LaunchedEffect(Unit) {
        if (AppConfig.ADS_ENABLED) {
            MobileAds.initialize(context) { /* no-op */ }
        }
    }

    val adView = remember(adSize) {
        AdView(context).apply {
            setAdSize(adSize)
            adUnitId = AdMobIds.BANNER_AD_UNIT_ID
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    LaunchedEffect(adView) {
        adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                // Show nothing; keep layout stable via AdView's own measured size.
            }
        }
        adView.loadAd(AdRequest.Builder().build())
    }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        factory = { adView }
    )
}

