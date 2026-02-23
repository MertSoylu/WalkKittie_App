package com.mert.paticat.data.ads

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Real Native Ad Unit ID
    var nativeAdUnitId: String = com.mert.paticat.BuildConfig.NATIVE_AD_ID
    
    // Rewarded Ad Unit IDs
    val FOOD_AD_ID = com.mert.paticat.BuildConfig.FOOD_AD_ID
    val SLEEP_AD_ID = com.mert.paticat.BuildConfig.SLEEP_AD_ID 

    private val _nativeAd = MutableStateFlow<NativeAd?>(null)
    val nativeAd: StateFlow<NativeAd?> = _nativeAd.asStateFlow()
    
    // Track when the ad was loaded to refresh expired ads (Native ads expire after ~60 mins)
    private var lastAdLoadTime: Long = 0
    private val AD_EXPIRATION_TIME = 50 * 60 * 1000L // 50 minutes
    
    // Retry logic
    private var retryCount = 0
    private val MAX_RETRIES = 3
    private val BASE_RETRY_DELAY = 1000L // 1s (Daha hızlı retry için)
    private var isLoading = false
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    fun initialize() {
        MobileAds.initialize(context) { 
            loadNativeAd()
        }
    }

    fun loadNativeAd() {
        val currentTime = System.currentTimeMillis()
        val isExpired = (currentTime - lastAdLoadTime) > AD_EXPIRATION_TIME
        
        // Use Test ID for Debug builds
        if (com.mert.paticat.BuildConfig.DEBUG) {
            nativeAdUnitId = "ca-app-pub-3940256099942544/2247696110"
        }

        // If ad exists and is not expired, keep using it
        if (_nativeAd.value != null && !isExpired) return
        
        // Prevent concurrent loading
        if (isLoading) return
        isLoading = true

        val adLoader = AdLoader.Builder(context, nativeAdUnitId)
            .forNativeAd { ad: NativeAd ->
                // Destroy old ad to prevent memory leaks
                _nativeAd.value?.destroy()
                
                _nativeAd.value = ad
                lastAdLoadTime = System.currentTimeMillis()
                retryCount = 0
                isLoading = false
                if (com.mert.paticat.BuildConfig.DEBUG) {
                    android.util.Log.d("AdManager", "Native Ad Loaded Successfully")
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    isLoading = false
                    if (com.mert.paticat.BuildConfig.DEBUG) {
                        android.util.Log.e("AdManager", "Native Ad Failed to Load: ${adError.code} - ${adError.message}")
                    }
                    
                    // Retry with exponential backoff
                    if (retryCount < MAX_RETRIES) {
                        val delayMs = (BASE_RETRY_DELAY * (1 shl retryCount)) 
                        retryCount++
                        handler.postDelayed({ loadNativeAd() }, delayMs)
                    }
                }
            })
            .build()

        adLoader.loadAd(getAdRequest())
    }

    // --- Rewarded Ad Logic ---
    fun loadRewardedAd(
        adUnitId: String, 
        currentRetry: Int = 0,
        onAdLoaded: (RewardedAd) -> Unit,
        onAdFailed: (LoadAdError?) -> Unit
    ) {
        RewardedAd.load(
            context,
            adUnitId,
            getAdRequest(),
            object : RewardedAdLoadCallback() {
                 override fun onAdFailedToLoad(adError: LoadAdError) {
                    if (currentRetry < MAX_RETRIES) {
                        val delayMs = (BASE_RETRY_DELAY * (1 shl currentRetry))
                        handler.postDelayed({
                            loadRewardedAd(adUnitId, currentRetry + 1, onAdLoaded, onAdFailed)
                        }, delayMs)
                    } else {
                        onAdFailed(adError)
                    }
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    onAdLoaded(ad)
                }
            }
        )
    }

    fun getAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }
}
