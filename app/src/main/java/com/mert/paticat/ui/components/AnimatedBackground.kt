package com.mert.paticat.ui.components

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlin.random.Random

data class AnimParticle(
    var x: Float,
    var y: Float,
    var radius: Float,
    var speedX: Float,
    var speedY: Float,
    var baseAlpha: Float,
    var phase: Float,
    var phaseSpeed: Float
)

@Composable
fun AnimatedBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 60,
    particleColor: Color? = null
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    var particles by remember { mutableStateOf(listOf<AnimParticle>()) }
    var lastFrameTime by remember { mutableLongStateOf(-1L) }
    
    val defaultColor = MaterialTheme.colorScheme.primary
    val colorToUse = particleColor ?: defaultColor

    LaunchedEffect(size) {
        if (size.width > 0 && size.height > 0 && particles.isEmpty()) {
            val newParticles = List(particleCount) {
                AnimParticle(
                    x = Random.nextFloat() * size.width,
                    y = Random.nextFloat() * size.height,
                    radius = Random.nextFloat() * 14f + 6f, // 6 to 20
                    speedX = Random.nextFloat() * 30f - 15f, // Faster drift
                    speedY = Random.nextFloat() * -50f - 20f, // Faster upward drift
                    baseAlpha = Random.nextFloat() * 0.4f + 0.2f, // 0.2 to 0.6
                    phase = Random.nextFloat() * 100f,
                    phaseSpeed = Random.nextFloat() * 4f + 2f // Faster twinkle
                )
            }
            particles = newParticles
        }
    }

    var trigger by remember { mutableStateOf(0f) }

    LaunchedEffect(particles) {
        if (particles.isNotEmpty()) {
            while (true) {
                withInfiniteAnimationFrameMillis { frameTime ->
                    if (lastFrameTime != -1L) {
                        val deltaTime = (frameTime - lastFrameTime) / 1000f
                        
                        particles.forEach { p ->
                            p.x += p.speedX * deltaTime
                            p.y += p.speedY * deltaTime
                            p.phase += p.phaseSpeed * deltaTime
                            
                            // Loop around edges
                            if (p.x < -p.radius * 2) p.x = size.width + p.radius * 2
                            else if (p.x > size.width + p.radius * 2) p.x = -p.radius * 2
                            
                            if (p.y < -p.radius * 2) p.y = size.height + p.radius * 2
                            else if (p.y > size.height + p.radius * 2) p.y = -p.radius * 2
                        }
                    }
                    lastFrameTime = frameTime
                    trigger = frameTime.toFloat() // Force Canvas redraw
                }
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
    ) {
        val t = trigger // Read the trigger state
        particles.forEach { particle ->
            // Add a slight sine wave to alpha for twinkling effect
            val twinkle = (Math.sin(particle.phase.toDouble()).toFloat() * 0.5f + 0.5f) * 0.5f + 0.5f // 0.5 to 1.0 variation
            val currentAlpha = (particle.baseAlpha * twinkle).coerceIn(0f, 1f)
            
            drawCircle(
                color = colorToUse.copy(alpha = currentAlpha),
                radius = particle.radius,
                center = Offset(particle.x, particle.y)
            )
        }
    }
}
