package com.mert.paticat.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mert.paticat.domain.model.CatMood

/**
 * Lottie animation URLs for different cat moods.
 * These are publicly available Lottie animations from LottieFiles.
 * In production, you would host your own or use raw resources.
 */
object CatAnimations {
    // Free cat animations from LottieFiles (replace with your own for production)
    const val IDLE = "https://lottie.host/4be08431-89b4-4c67-8a48-95f2c726c5f9/mCBzrTjEqt.json"
    const val HAPPY = "https://lottie.host/c3be5c9a-84f4-4b4f-840e-becd7c7df1f4/HYNhUTj1Dp.json"
    const val HUNGRY = "https://lottie.host/bd7a9d56-0a0c-4638-9ba7-0e6d48a1ac78/X5pEJvTwTU.json"
    const val SLEEPING = "https://lottie.host/f4a8b1d2-3c5e-4f6a-8b9c-1d2e3f4a5b6c/sleeping.json"
    const val EXCITED = "https://lottie.host/a1b2c3d4-e5f6-7890-abcd-ef1234567890/excited.json"
    
    fun getAnimationForMood(mood: CatMood): String {
        return when (mood) {
            CatMood.IDLE -> IDLE
            CatMood.HAPPY -> HAPPY
            CatMood.HUNGRY -> HUNGRY
            CatMood.SLEEPING -> SLEEPING
            CatMood.EXCITED -> EXCITED
        }
    }
}

/**
 * Lottie Cat Animation Component.
 * Displays animated cat based on current mood.
 */
@Composable
fun LottieCatAnimation(
    mood: CatMood,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    iterations: Int = LottieConstants.IterateForever
) {
    val animationUrl = CatAnimations.getAnimationForMood(mood)
    
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.Url(animationUrl)
    )
    
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        isPlaying = true,
        restartOnPlay = false
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Lottie animation from raw resource.
 * Use this when you have local animation files.
 */
@Composable
fun LottieRawAnimation(
    rawResId: Int,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    iterations: Int = LottieConstants.IterateForever
) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(rawResId)
    )
    
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        isPlaying = true,
        restartOnPlay = false
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Generic Lottie animation from URL.
 */
@Composable
fun LottieUrlAnimation(
    url: String,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    iterations: Int = LottieConstants.IterateForever,
    speed: Float = 1f
) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.Url(url)
    )
    
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        isPlaying = true,
        speed = speed
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.fillMaxSize()
        )
    }
}
