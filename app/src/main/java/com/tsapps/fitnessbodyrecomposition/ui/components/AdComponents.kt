package com.tsapps.fitnessbodyrecomposition.ui.components

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

// Google AdMob Test Ad Unit IDs
// Replace with your real Ad Unit IDs from admob.google.com before publishing
object AdUnitIds {
    // Test IDs (safe for development)
    const val BANNER_TEST = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL_TEST = "ca-app-pub-3940256099942544/1033173712"
    const val APP_OPEN_TEST = "ca-app-pub-3940256099942544/9257395921"
    
    // TODO: Replace these with your real Ad Unit IDs from AdMob dashboard
    const val DASHBOARD_BANNER = BANNER_TEST
    const val PROGRESS_BANNER = BANNER_TEST
    const val NUTRITION_BANNER = BANNER_TEST
    const val STEPS_BANNER = BANNER_TEST
    const val WORKOUT_COMPLETE_INTERSTITIAL = INTERSTITIAL_TEST
    const val MEAL_LOGGED_INTERSTITIAL = INTERSTITIAL_TEST
    const val APP_OPEN_AD = APP_OPEN_TEST
}

/**
 * Reusable Banner Ad Composable.
 * Displays a standard 320x50 banner ad.
 */
@Composable
fun BannerAd(
    adUnitId: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

/**
 * Helper object for loading and showing Interstitial (full-screen) ads.
 */
object InterstitialAdHelper {
    private var interstitialAd: InterstitialAd? = null

    /**
     * Preloads an interstitial ad so it's ready to show instantly.
     * Call this early (e.g., when a screen loads) so the ad is ready at transition time.
     */
    fun loadAd(context: Context, adUnitId: String) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitialAd = null
            }
        })
    }

    /**
     * Shows the interstitial ad if one is loaded.
     * @param activity The current Activity context.
     * @param onAdDismissed Callback when the ad is closed (use this for navigation).
     */
    fun showAd(activity: Activity, onAdDismissed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    onAdDismissed()
                }
            }
            ad.show(activity)
        } else {
            // Ad not loaded, just proceed
            onAdDismissed()
        }
    }
}

/**
 * Manager for App Open Ads.
 */
object AppOpenAdManager {
    private var appOpenAd: com.google.android.gms.ads.appopen.AppOpenAd? = null
    private var isLoadingAd = false

    fun loadAd(context: Context) {
        if (isLoadingAd || appOpenAd != null) return
        isLoadingAd = true
        val request = AdRequest.Builder().build()
        com.google.android.gms.ads.appopen.AppOpenAd.load(
            context,
            AdUnitIds.APP_OPEN_AD,
            request,
            object : com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: com.google.android.gms.ads.appopen.AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                }
            }
        )
    }

    fun showAdIfAvailable(activity: Activity, onAdDismissed: () -> Unit = {}) {
        appOpenAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    loadAd(activity)
                    onAdDismissed()
                }
            }
            ad.show(activity)
        } ?: run {
            onAdDismissed()
        }
    }
}
