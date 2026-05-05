package com.mert.paticat.ui.components

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.mert.paticat.R
import com.mert.paticat.ui.components.marshmallow.PillowCard
import com.mert.paticat.ui.theme.Dimensions

/**
 * Marshmallow-shelled native ad. Outer surface is a PillowCard; the AdMob view
 * inflate (`populateNativeAdView`) is preserved verbatim — do NOT modify.
 */
@Composable
fun NativeAdCard(
    nativeAd: NativeAd?,
    modifier: Modifier = Modifier,
) {
    if (nativeAd == null) {
        PillowCard(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(Dimensions.radiusL),
            contentPadding = 12.dp,
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        return
    }

    val label = stringResource(R.string.ad_label)
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val ctaColor = MaterialTheme.colorScheme.primary.toArgb()
    val ctaTextColor = MaterialTheme.colorScheme.onPrimary.toArgb()
    val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()

    PillowCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimensions.radiusL),
        contentPadding = 12.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                shape = RoundedCornerShape(Dimensions.radiusS),
                modifier = Modifier.padding(bottom = 6.dp),
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }

            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    val adView = LayoutInflater.from(ctx)
                        .inflate(R.layout.ad_unified_home, null) as NativeAdView
                    populateNativeAdView(
                        nativeAd = nativeAd,
                        adView = adView,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor,
                        ctaColor = ctaColor,
                        ctaTextColor = ctaTextColor,
                        surfaceColor = surfaceColor,
                    )
                    adView
                },
                update = { adView ->
                    populateNativeAdView(
                        nativeAd = nativeAd,
                        adView = adView,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor,
                        ctaColor = ctaColor,
                        ctaTextColor = ctaTextColor,
                        surfaceColor = surfaceColor,
                    )
                },
            )
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
    surfaceColor: Int,
) {
    adView.setBackgroundColor(surfaceColor)

    adView.mediaView = adView.findViewById(R.id.ad_media)
    adView.headlineView = adView.findViewById(R.id.ad_headline)
    adView.bodyView = adView.findViewById(R.id.ad_body)
    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
    adView.iconView = adView.findViewById(R.id.ad_app_icon)
    adView.starRatingView = adView.findViewById(R.id.ad_stars)
    adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

    (adView.headlineView as TextView).apply {
        text = nativeAd.headline
        setTextColor(textColor)
    }
    nativeAd.mediaContent?.let { adView.mediaView?.setMediaContent(it) }

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
        (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
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

    adView.setNativeAd(nativeAd)
}
