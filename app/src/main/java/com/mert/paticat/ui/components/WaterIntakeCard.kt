package com.mert.paticat.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mert.paticat.R
import com.mert.paticat.ui.components.marshmallow.PillowCard
import com.mert.paticat.ui.components.marshmallow.pillowPress
import com.mert.paticat.ui.theme.PremiumBlue
import com.mert.paticat.ui.theme.PremiumBlueDark

/**
 * Marshmallow water tracker — pillow shell, liquid wave bar, +200/+300/+500 chips.
 */
@Composable
fun WaterTrackingCard(
    current: Int,
    goal: Int,
    onAdd: (Int) -> Unit,
    canUndo: Boolean = false,
    onUndo: () -> Unit = {},
    onPositioned: (LayoutCoordinates) -> Unit = {},
) {
    PillowCard(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned(onPositioned),
        backgroundColor = PremiumBlue.copy(alpha = 0.10f),
        contentPadding = 18.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.home_water_tracking_title),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (canUndo) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PremiumBlue.copy(alpha = 0.18f))
                                .pillowPress(onClick = onUndo),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = stringResource(R.string.btn_undo),
                                tint = PremiumBlue,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = "${((current.toFloat() / goal) * 100).toInt()}%",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = PremiumBlueDark,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(CircleShape)
                    .background(PremiumBlue.copy(alpha = 0.16f)),
            ) {
                val targetProgress = (current.toFloat() / goal).coerceIn(0f, 1f)
                val animatedProgress by animateFloatAsState(
                    targetValue = targetProgress,
                    animationSpec = tween(1000),
                    label = "waterProgress",
                )
                WaterWaveAnimation(progress = animatedProgress, color = PremiumBlue)

                Text(
                    text = "$current${stringResource(R.string.unit_ml)} / $goal${stringResource(R.string.unit_ml)}",
                    fontSize = 12.sp,
                    color = if (animatedProgress > 0.3f) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center),
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                listOf(200, 300, 500).forEach { amount ->
                    WaterChip(
                        amount = amount,
                        modifier = Modifier.weight(1f),
                        onClick = { onAdd(amount) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WaterChip(
    amount: Int,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp))
            .background(PremiumBlue.copy(alpha = 0.18f))
            .pillowPress(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "+$amount",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = PremiumBlueDark,
            )
            Text(
                text = stringResource(R.string.unit_ml),
                fontSize = 11.sp,
                color = PremiumBlue.copy(alpha = 0.75f),
            )
        }
    }
}

@Composable
fun WaterWaveAnimation(progress: Float, color: Color, isVisible: Boolean = true) {
    if (!isVisible) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (progress > 0) {
                drawRect(color = color, size = size.copy(width = size.width * progress))
            }
        }
        return
    }
    val shouldAnimate = isVisible && progress > 0f && progress < 1f

    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveOffset by if (shouldAnimate) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "waveOffset",
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val fillWidth = width * progress

        if (progress > 0) {
            val path = Path()
            val waveAmplitude = 4.dp.toPx()
            val waveFrequency = 1.5 * Math.PI / height

            path.moveTo(0f, 0f)
            path.lineTo(fillWidth, 0f)

            for (y in 0..height.toInt() step 5) {
                val relativeY = y.toFloat()
                val phase = waveOffset * 2 * Math.PI
                val x = fillWidth + waveAmplitude * kotlin.math.sin((waveFrequency * relativeY) + phase).toFloat()
                path.lineTo(x, relativeY)
            }

            path.lineTo(0f, height)
            path.close()
            drawPath(path = path, color = color)

            val path2 = Path()
            path2.moveTo(0f, 0f)
            path2.lineTo(fillWidth - 4.dp.toPx(), 0f)
            for (y in 0..height.toInt() step 5) {
                val relativeY = y.toFloat()
                val phase = (waveOffset + 0.5f) * 2 * Math.PI
                val x = (fillWidth - 4.dp.toPx()) + (waveAmplitude * 0.8f) * kotlin.math.sin((waveFrequency * relativeY) + phase).toFloat()
                path2.lineTo(x, relativeY)
            }
            path2.lineTo(0f, height)
            path2.close()
            drawPath(path = path2, color = color.copy(alpha = 0.5f))
        }
    }
}

/**
 * Legacy: compact card kept for any existing call sites.
 */
@Suppress("unused")
@Composable
fun WaterIntakeCard(
    currentWaterMl: Int,
    goalMl: Int = 2000,
    onAddWater: (Int) -> Unit,
    canUndo: Boolean = false,
    onUndoWater: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") modifier: Modifier = Modifier,
) {
    WaterTrackingCard(
        current = currentWaterMl,
        goal = goalMl,
        onAdd = onAddWater,
        canUndo = canUndo,
        onUndo = onUndoWater,
        onPositioned = {},
    )
}

@Composable
fun EmptyMissionState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Text("✨", fontSize = 36.sp)
        Text(
            text = stringResource(R.string.home_all_missions_completed),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
    }
}
