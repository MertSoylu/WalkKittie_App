package com.mert.paticat.ui.components

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.mert.paticat.R

/**
 * A beautiful Native Ad card that blends into the app's UI.
 */
@Composable
fun NativeAdCard(
    nativeAd: NativeAd?,
    modifier: Modifier = Modifier
) {
    if (nativeAd != null) {
        val loadedAd = nativeAd
        val label = stringResource(R.string.ad_label)
        val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
        val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
        val ctaColor = MaterialTheme.colorScheme.primary.toArgb()
        val ctaTextColor = MaterialTheme.colorScheme.onPrimary.toArgb()
        val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()

        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Badge
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { ctx ->
                        val adView = LayoutInflater.from(ctx).inflate(R.layout.ad_unified_home, null) as NativeAdView
                        populateNativeAdView(
                            nativeAd = loadedAd,
                            adView = adView,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            ctaColor = ctaColor,
                            ctaTextColor = ctaTextColor,
                            surfaceColor = surfaceColor
                        )
                        adView
                    },
                    update = { adView ->
                        populateNativeAdView(
                            nativeAd = loadedAd,
                            adView = adView,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            ctaColor = ctaColor,
                            ctaTextColor = ctaTextColor,
                            surfaceColor = surfaceColor
                        )
                    }
                )
            }
        }
    }
}

private fun populateNativeAdView(
    nativeAd: NativeAd,
    adView: NativeAdView,
    textColor: Int,
    secondaryTextColor: Int,
    ctaColor: Int,
    ctaTextColor: Int,
    surfaceColor: Int
) {
    adView.setBackgroundColor(surfaceColor)

    // Set the media view.
    adView.mediaView = adView.findViewById(R.id.ad_media)

    // Set other ad assets.
    adView.headlineView = adView.findViewById(R.id.ad_headline)
    adView.bodyView = adView.findViewById(R.id.ad_body)
    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
    adView.iconView = adView.findViewById(R.id.ad_app_icon)
    adView.starRatingView = adView.findViewById(R.id.ad_stars)
    adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

    // The headline and mediaContent are guaranteed to be in every NativeAd.
    (adView.headlineView as TextView).apply {
        text = nativeAd.headline
        setTextColor(textColor)
    }
    nativeAd.mediaContent?.let { adView.mediaView?.setMediaContent(it) }

    // These assets aren't guaranteed to be in every NativeAd, so it's important to
    // check before trying to display them.
    if (nativeAd.body == null) {
        adView.bodyView?.visibility = View.INVISIBLE
    } else {
        adView.bodyView?.visibility = View.VISIBLE
        (adView.bodyView as TextView).apply {
            text = nativeAd.body
            setTextColor(secondaryTextColor)
        }
    }

    if (nativeAd.callToAction == null) {
        adView.callToActionView?.visibility = View.INVISIBLE
    } else {
        adView.callToActionView?.visibility = View.VISIBLE
        (adView.callToActionView as Button).apply {
            text = nativeAd.callToAction
            setTextColor(ctaTextColor)
            setBackgroundColor(ctaColor)
        }
    }

    if (nativeAd.icon == null) {
        adView.iconView?.visibility = View.GONE
    } else {
        (adView.iconView as ImageView).setImageDrawable(
            nativeAd.icon?.drawable
        )
        adView.iconView?.visibility = View.VISIBLE
    }

    if (nativeAd.advertiser == null) {
        adView.advertiserView?.visibility = View.INVISIBLE
    } else {
        (adView.advertiserView as TextView).apply {
            text = nativeAd.advertiser
            setTextColor(secondaryTextColor)
        }
        adView.advertiserView?.visibility = View.VISIBLE
    }

    // This method tells the Google Mobile Ads SDK that you have finished populating your
    // native ad view with this native ad.
    adView.setNativeAd(nativeAd)
}
