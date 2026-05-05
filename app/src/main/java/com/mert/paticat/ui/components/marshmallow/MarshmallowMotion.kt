package com.mert.paticat.ui.components.marshmallow

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mert.paticat.ui.components.rememberMotionEnabled
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.launch

/**
 * Marshmallow motion tokens — spring specs that define the feel of the new UI.
 *
 * Naming:
 *  - PrimaryEnter: standard entrance for cards/sheets/list items.
 *  - Bouncy: pet/heart/celebration moments — overshoot welcome.
 *  - Gentle: state crossfades, color/size transitions where bounce would be jarring.
 *  - Press: pressed-down feedback (must settle in <250ms).
 */
object MarshmallowSpring {
    val PrimaryEnter: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
    val Bouncy: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    )
    val Gentle: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )
    val Press: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium,
    )
}

/**
 * Soft entrance for any composable: scale 0.92 → 1.0 + alpha 0 → 1, staggered by [delayMillis].
 * Falls back to no animation when reduce-motion is enabled (rememberMotionEnabled = false).
 */
@Composable
fun Modifier.softEntrance(
    visible: Boolean = true,
    delayMillis: Int = 0,
): Modifier = composed {
    val motionEnabled = rememberMotionEnabled()
    if (!motionEnabled) return@composed this

    val scale = remember { Animatable(if (visible) 0.92f else 1f) }
    val alpha = remember { Animatable(if (visible) 0f else 1f) }

    LaunchedEffect(visible) {
        if (visible) {
            kotlinx.coroutines.delay(delayMillis.toLong())
            kotlinx.coroutines.coroutineScope {
                launch {
                    scale.animateTo(1f, MarshmallowSpring.PrimaryEnter)
                }
                launch {
                    alpha.animateTo(1f, tween(durationMillis = 320, easing = LinearEasing))
                }
            }
        }
    }

    this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
        this.alpha = alpha.value
    }
}

/**
 * Pillow press: scale 0.96 on press, spring back on release.
 */
@Composable
fun Modifier.pillowPress(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val motionEnabled = rememberMotionEnabled()
    val pressed = remember { mutableStateOf(false) }
    val target = if (pressed.value && motionEnabled) 0.96f else 1f
    val scale = remember { Animatable(1f) }
    // Latest onClick ref — pointerInput captures the closure once, so a plain
    // lambda would freeze whatever values it captured on first composition.
    val currentOnClick = rememberUpdatedState(onClick)

    LaunchedEffect(target) {
        scale.animateTo(target, MarshmallowSpring.Press)
    }

    this
        .scale(scale.value)
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            detectTapGestures(
                onPress = {
                    pressed.value = true
                    val released = tryAwaitRelease()
                    pressed.value = false
                    if (released) currentOnClick.value()
                }
            )
        }
}

/**
 * Slow breathing scale: 1.0 ↔ 1.04 over [periodMillis]. Used on idle hero cat / logo.
 */
@Composable
fun Modifier.breath(
    enabled: Boolean = true,
    periodMillis: Int = 2400,
    amplitude: Float = 0.04f,
): Modifier = composed {
    val motionEnabled = rememberMotionEnabled() && enabled
    if (!motionEnabled) return@composed this

    val infinite = rememberInfiniteTransition(label = "breath")
    val s by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1f + amplitude,
        animationSpec = infiniteRepeatable(
            animation = tween(periodMillis / 2, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_s",
    )
    this.scale(s)
}

/**
 * Floaty drift: subtle vertical sine bob. Used on emoji decorations / small icons.
 */
@Composable
fun Modifier.floaty(
    enabled: Boolean = true,
    periodMillis: Int = 3200,
    amplitudeDp: Float = 4f,
    phase: Float = 0f,
): Modifier = composed {
    val motionEnabled = rememberMotionEnabled() && enabled
    if (!motionEnabled) return@composed this

    val infinite = rememberInfiniteTransition(label = "floaty")
    val t by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(periodMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "floaty_t",
    )
    val density = androidx.compose.ui.platform.LocalDensity.current
    val offsetPx = with(density) {
        (sin(t + phase) * amplitudeDp).dp.roundToPx()
    }
    this.offset { IntOffset(0, offsetPx) }
}
