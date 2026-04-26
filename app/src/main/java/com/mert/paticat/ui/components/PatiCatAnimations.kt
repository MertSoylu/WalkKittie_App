package com.mert.paticat.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import android.provider.Settings

@Composable
fun rememberMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) > 0f
        }.getOrDefault(true)
    }
}

@Composable
fun EntranceAnimation(
    modifier: Modifier = Modifier,
    delay: Int = 0,
    withScale: Boolean = false,
    content: @Composable () -> Unit
) {
    val motionEnabled = rememberMotionEnabled()
    if (!motionEnabled) {
        Box(modifier = modifier) { content() }
        return
    }

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        visible = true
    }

    val enter = if (withScale) {
        fadeIn(animationSpec = tween(500)) +
            slideInVertically(initialOffsetY = { 50 }, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)) +
            scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))
    } else {
        fadeIn(animationSpec = tween(500)) +
            slideInVertically(initialOffsetY = { 50 }, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
    }

    AnimatedVisibility(
        visible = visible,
        enter = enter,
        modifier = modifier
    ) {
        content()
    }
}

fun Modifier.bounceClick(
    scaleDown: Float = 0.95f,
    onClick: () -> Unit
) = composed {
    val motionEnabled = rememberMotionEnabled()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (motionEnabled && isPressed) scaleDown else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "bounce"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

fun Modifier.pulsate(
    scaleRange: ClosedFloatingPointRange<Float> = 0.98f..1.02f,
    duration: Int = 1000
) = composed {
    val motionEnabled = rememberMotionEnabled()
    if (!motionEnabled) return@composed this

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    val isVisible = lifecycleState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)

    if (!isVisible) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = scaleRange.start,
        targetValue = scaleRange.endInclusive,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

fun Modifier.shimmerEffect() = composed {
    val motionEnabled = rememberMotionEnabled()
    if (!motionEnabled) return@composed this

    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )
    this.drawWithContent {
        drawContent()
        val brushWidth = size.width * 0.45f
        val x = progress * (size.width + brushWidth) - brushWidth
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.38f), Color.Transparent),
                start = Offset(x, 0f),
                end = Offset(x + brushWidth, size.height)
            ),
            blendMode = BlendMode.Screen
        )
    }
}

fun Modifier.glowPulse(
    color: Color,
    minAlpha: Float = 0.3f,
    maxAlpha: Float = 0.7f
) = composed {
    val motionEnabled = rememberMotionEnabled()
    if (!motionEnabled) return@composed this

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    val isVisible = lifecycleState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)
    if (!isVisible) return@composed this

    val transition = rememberInfiniteTransition(label = "glow")
    val alpha by transition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    this.drawBehind {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = alpha * 0.55f), Color.Transparent),
                center = Offset(size.width / 2f, size.height / 2f),
                radius = maxOf(size.width, size.height) * 0.75f
            )
        )
    }
}
