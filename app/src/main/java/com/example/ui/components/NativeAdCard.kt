package com.example.ui.components

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.theme.BorderLight
import com.example.ui.theme.LightSurface
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

const val ADMOB_NATIVE_AD_UNIT_ID = "ca-app-pub-8755082798616406/6875545823"

@Composable
fun NativeAdCard(
    modifier: Modifier = Modifier,
    adUnitId: String = ADMOB_NATIVE_AD_UNIT_ID
) {
    val context = LocalContext.current
    var loadedNativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var adFailedToLoad by remember { mutableStateOf(false) }

    LaunchedEffect(adUnitId) {
        try {
            val adLoader = AdLoader.Builder(context, adUnitId)
                .forNativeAd { ad: NativeAd ->
                    loadedNativeAd?.destroy()
                    loadedNativeAd = ad
                    adFailedToLoad = false
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.w("NativeAdCard", "AdMob Native ad failed to load: ${loadAdError.message} (code: ${loadAdError.code})")
                        adFailedToLoad = true
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                        .build()
                )
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            Log.e("NativeAdCard", "Exception loading Native Ad: ${e.message}", e)
            adFailedToLoad = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            loadedNativeAd?.destroy()
        }
    }

    // Only display when successfully loaded without errors
    val ad = loadedNativeAd
    if (ad != null && !adFailedToLoad) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            AndroidView(
                factory = { ctx ->
                    val view = LayoutInflater.from(ctx).inflate(R.layout.item_native_ad, null) as NativeAdView
                    populateNativeAdView(ad, view)
                    view
                },
                update = { view ->
                    populateNativeAdView(ad, view)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
    val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
    val bodyView = adView.findViewById<TextView>(R.id.ad_body)
    val ctaView = adView.findViewById<Button>(R.id.ad_call_to_action)
    val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
    val advertiserView = adView.findViewById<TextView>(R.id.ad_advertiser)
    val mediaView = adView.findViewById<MediaView>(R.id.ad_media)

    // Headline
    headlineView.text = nativeAd.headline ?: "Sponsored"
    adView.headlineView = headlineView

    // Media
    adView.mediaView = mediaView

    // Body
    if (nativeAd.body.isNullOrBlank()) {
        bodyView.visibility = View.GONE
    } else {
        bodyView.visibility = View.VISIBLE
        bodyView.text = nativeAd.body
    }
    adView.bodyView = bodyView

    // Call to Action
    if (nativeAd.callToAction.isNullOrBlank()) {
        ctaView.visibility = View.GONE
    } else {
        ctaView.visibility = View.VISIBLE
        ctaView.text = nativeAd.callToAction
    }
    adView.callToActionView = ctaView

    // Icon
    val icon = nativeAd.icon
    if (icon?.drawable != null) {
        iconView.visibility = View.VISIBLE
        iconView.setImageDrawable(icon.drawable)
    } else {
        iconView.visibility = View.GONE
    }
    adView.iconView = iconView

    // Advertiser
    if (nativeAd.advertiser.isNullOrBlank()) {
        advertiserView.visibility = View.GONE
    } else {
        advertiserView.visibility = View.VISIBLE
        advertiserView.text = nativeAd.advertiser
    }
    adView.advertiserView = advertiserView

    adView.setNativeAd(nativeAd)
}
