package com.mert.paticat.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mert.paticat.R
import com.mert.paticat.domain.model.Cat
import com.mert.paticat.ui.theme.PremiumPink
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LevelUpDialog(
    newLevel: Int,
    onDismiss: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val particleSystem = rememberParticleSystem()

    val confettiColors = listOf(
        Color.Red, Color(0xFFFFD600), Color.Green, Color.Blue, Color.Magenta,
        Color(0xFFFF6D00), Color(0xFF00E5FF), PremiumPink
    )

    LaunchedEffect(Unit) {
        showDialog = true
        // 6 bursts of 60 particles each from 3 positions, mixing STAR and HEART
        for (i in 0..5) {
            delay(280)
            val xPos = listOf(150f, 400f, 700f).random()
            particleSystem.emit(
                x = xPos,
                y = 900f,
                count = 60,
                type = if (i % 2 == 0) ParticleType.STAR else ParticleType.HEART,
                color = confettiColors.random()
            )
        }
    }

    // Rotating sun rays
    val infiniteTransition = rememberInfiniteTransition(label = "sun_rays")
    val rayAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ray_angle"
    )

    // Level number counter: 0 → newLevel
    val animatedLevel by animateIntAsState(
        targetValue = if (showDialog) newLevel else 0,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "level_counter"
    )

    if (showDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            ParticleSystemCanvas(state = particleSystem, modifier = Modifier.fillMaxSize())

            AnimatedVisibility(
                visible = showDialog,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialScale = 0.6f
                ),
                exit = scaleOut(animationSpec = tween(300))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.level_up_title),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Trophy + rotating sun rays
                        Box(
                            modifier = Modifier.size(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Sun rays via Canvas
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val center = Offset(size.width / 2, size.height / 2)
                                val rayCount = 12
                                val outerR = size.width / 2f
                                val innerR = outerR * 0.62f
                                rotate(rayAngle, pivot = center) {
                                    repeat(rayCount) { i ->
                                        val angle = (i * 360f / rayCount) * (Math.PI / 180f).toFloat()
                                        val x1 = center.x + innerR * cos(angle)
                                        val y1 = center.y + innerR * sin(angle)
                                        val x2 = center.x + outerR * cos(angle)
                                        val y2 = center.y + outerR * sin(angle)
                                        drawLine(
                                            color = Color(0xFFFFD600).copy(alpha = 0.45f),
                                            start = Offset(x1, y1),
                                            end = Offset(x2, y2),
                                            strokeWidth = 6f
                                        )
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.EmojiEvents,
                                    contentDescription = stringResource(R.string.level_up_badge_icon),
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Level $animatedLevel",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val levelTitleRes = Cat.getLevelTitleResId(newLevel)
                        Text(
                            text = androidx.compose.ui.res.stringResource(levelTitleRes),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = PremiumPink,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Text(
                            text = stringResource(R.string.level_up_message),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                                .bounceClick {
                                    showDialog = false
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.level_up_button),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            ParticleSystemCanvas(state = particleSystem, modifier = Modifier.fillMaxSize())
        }
    }
}
