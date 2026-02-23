package com.mert.paticat.ui.components

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// --- Particle Data Models ---

enum class ParticleType {
    HEART, STAR, WATER_DROPLET
}

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val initialLife: Float,
    val type: ParticleType,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val rotationSpeed: Float
) {
    fun update(deltaTime: Float) {
        x += vx * deltaTime
        y += vy * deltaTime
        // Apply gravity depending on type
        if (type == ParticleType.WATER_DROPLET) {
            vy += 800f * deltaTime // Water falls
        } else {
            vy -= 100f * deltaTime // Hearts/Stars float up a bit
        }
        life -= deltaTime
    }

    val alpha: Float
        get() = (life / initialLife).coerceIn(0f, 1f)
}

class ParticleSystemState {
    val particles = mutableStateListOf<Particle>()

    fun emit(
        x: Float,
        y: Float,
        count: Int,
        type: ParticleType,
        color: Color,
        sizeRange: ClosedFloatingPointRange<Float> = 10f..30f,
        speedRange: ClosedFloatingPointRange<Float> = 50f..200f
    ) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
            val speed = Random.nextFloat() * (speedRange.endInclusive - speedRange.start) + speedRange.start
            
            // For water droplets, prefer upward initial velocity (like splashing)
            val adjustedAngle = if (type == ParticleType.WATER_DROPLET) {
                Random.nextFloat() * Math.PI.toFloat() + Math.PI.toFloat() // Top half
            } else angle

            val vx = cos(adjustedAngle) * speed
            val vy = sin(adjustedAngle) * speed
            val life = Random.nextFloat() * 1.5f + 0.5f // 0.5 to 2.0 seconds

            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    life = life,
                    initialLife = life,
                    type = type,
                    color = color,
                    size = Random.nextFloat() * (sizeRange.endInclusive - sizeRange.start) + sizeRange.start,
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = (Random.nextFloat() - 0.5f) * 360f
                )
            )
        }
    }

    fun update(deltaTime: Float) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.update(deltaTime)
            if (p.life <= 0f) {
                iterator.remove()
            }
        }
    }
}

@Composable
fun rememberParticleSystem(): ParticleSystemState {
    return remember { ParticleSystemState() }
}

@Composable
fun ParticleSystemCanvas(
    state: ParticleSystemState,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true
) {
    var lastFrameTime by remember { mutableLongStateOf(-1L) }

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            lastFrameTime = -1L
            return@LaunchedEffect
        }

        // Use snapshotFlow to observe if particles exist without restarting the effect on EVERY count change
        androidx.compose.runtime.snapshotFlow { state.particles.isNotEmpty() }
            .collect { hasParticles ->
                if (hasParticles) {
                    while (isActive && state.particles.isNotEmpty() && isVisible) {
                        withInfiniteAnimationFrameMillis { frameTime ->
                            if (lastFrameTime != -1L) {
                                val deltaTime = (frameTime - lastFrameTime) / 1000f
                                state.update(deltaTime.coerceAtMost(0.1f))
                            }
                            lastFrameTime = frameTime
                        }
                    }
                } else {
                    lastFrameTime = -1L
                }
            }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        state.particles.forEach { particle ->
            translate(left = particle.x, top = particle.y) {
                val alphaColor = particle.color.copy(alpha = particle.alpha)
                
                when (particle.type) {
                    ParticleType.HEART -> drawHeart(particle.size, alphaColor)
                    ParticleType.STAR -> drawStar(particle.size, alphaColor)
                    ParticleType.WATER_DROPLET -> drawWaterDroplet(particle.size, alphaColor)
                }
            }
        }
    }
}

// --- Cached Paths for Performance ---
// To prevent massive Garbage Collection stutters, we pre-calculate paths for a base size of 100f
// and simply scale them during the draw phase instead of creating new Paths every frame.

private object ParticlePaths {
    val heartPath = Path().apply {
        val size = 100f
        val width = size
        val height = size
        moveTo(width / 2, height / 5)
        cubicTo(width / 8, -height / 4, -width / 2, height / 2, width / 2, height)
        cubicTo(width * 1.5f, height / 2, width * 7/8f, -height / 4, width / 2, height / 5)
        close()
    }

    val starPath = Path().apply {
        val size = 100f
        val center = size / 2
        var currentAngle = -Math.PI / 2
        val rot = Math.PI / 5
        val outerRadius = size / 2
        val innerRadius = size / 4

        moveTo(
            (center + cos(currentAngle) * outerRadius).toFloat(),
            (center + sin(currentAngle) * outerRadius).toFloat()
        )

        for (i in 0 until 5) {
            currentAngle += rot
            lineTo(
                (center + cos(currentAngle) * innerRadius).toFloat(),
                (center + sin(currentAngle) * innerRadius).toFloat()
            )
            currentAngle += rot
            lineTo(
                (center + cos(currentAngle) * outerRadius).toFloat(),
                (center + sin(currentAngle) * outerRadius).toFloat()
            )
        }
        close()
    }

    val waterDropletPath = Path().apply {
        val size = 100f
        moveTo(size / 2, 0f) // Top tip
        quadraticBezierTo(size, size * 0.7f, size / 2, size) // Right curve down to bottom
        quadraticBezierTo(0f, size * 0.7f, size / 2, 0f) // Left curve up to top
        close()
    }
}

// --- Drawing Utilities ---

fun DrawScope.drawHeart(size: Float, color: Color) {
    val scale = size / 100f
    scale(scale = scale, pivot = Offset.Zero) {
        drawPath(path = ParticlePaths.heartPath, color = color)
    }
}

fun DrawScope.drawStar(size: Float, color: Color) {
    val scale = size / 100f
    translate(left = -size/2, top = -size/2) {
        scale(scale = scale, pivot = Offset.Zero) {
            drawPath(path = ParticlePaths.starPath, color = color)
        }
    }
}

fun DrawScope.drawWaterDroplet(size: Float, color: Color) {
    val scale = size / 100f
    translate(left = -size/2, top = -size/2) {
        scale(scale = scale, pivot = Offset.Zero) {
            drawPath(path = ParticlePaths.waterDropletPath, color = color)
        }
    }
}
