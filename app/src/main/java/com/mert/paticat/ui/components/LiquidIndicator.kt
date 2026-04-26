package com.mert.paticat.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.floor

fun DrawScope.drawLiquidIndicator(
    animatedOffset: Float,
    itemCount: Int,
    itemW: Float,
    cy: Float,
    maxRadius: Float,
    color: Color
) {
    val fromPage = floor(animatedOffset).toInt().coerceIn(0, itemCount - 1)
    val fraction = (animatedOffset - floor(animatedOffset)).toFloat().coerceIn(0f, 1f)
    val toPage = (fromPage + 1).coerceAtMost(itemCount - 1)

    val fromX = fromPage * itemW + itemW / 2f
    val toX = toPage * itemW + itemW / 2f

    when {
        fraction < 0.02f || fromPage == toPage -> {
            drawCircle(color, maxRadius, Offset(fromX, cy))
        }
        fraction > 0.98f -> {
            drawCircle(color, maxRadius, Offset(toX, cy))
        }
        fraction < 0.65f -> {
            // Phase 1: blob stretches — connected phase now lasts 65% of transition
            val t = fraction / 0.65f
            val r2 = maxRadius * liquidEase(t)
            val neckR = (maxRadius * 0.3f * (1f - t)).coerceAtLeast(1f)
            drawLiquidBlob(fromX, toX, maxRadius, r2, neckR, cy, color)
        }
        else -> {
            // Phase 2: droplet detaches — remaining 35%
            val t = (fraction - 0.65f) / 0.35f
            val r1 = maxRadius * (1f - liquidEase(t))
            if (r1 > 1f) {
                drawCircle(color, r1, Offset(fromX, cy))
            }
            drawCircle(color, maxRadius, Offset(toX, cy))
        }
    }
}

fun liquidEase(t: Float): Float {
    // Quintic ease-in-out — slower, more prolonged stretch
    val c = t * t * t * (t * (t * 6f - 15f) + 10f)
    return c
}

fun DrawScope.drawLiquidBlob(
    x1: Float, x2: Float,
    r1: Float, r2: Float,
    neckR: Float,
    cy: Float,
    color: Color
) {
    // Neck position weighted toward target
    val neckX = x1 + (x2 - x1) * (r2 / (r1 + r2))
    val ctrl = (x2 - x1) * 0.15f

    val path = Path().apply {
        moveTo(x1, cy - r1)
        cubicTo(
            x1 + (neckX - x1) * 0.5f, cy - r1,
            neckX - ctrl, cy - neckR,
            neckX, cy - neckR
        )
        cubicTo(
            neckX + ctrl, cy - neckR,
            x2 - (x2 - neckX) * 0.5f, cy - r2,
            x2, cy - r2
        )
        arcTo(
            rect = Rect(x2 - r2, cy - r2, x2 + r2, cy + r2),
            startAngleDegrees = -90f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )
        cubicTo(
            x2 - (x2 - neckX) * 0.5f, cy + r2,
            neckX + ctrl, cy + neckR,
            neckX, cy + neckR
        )
        cubicTo(
            neckX - ctrl, cy + neckR,
            x1 + (neckX - x1) * 0.5f, cy + r1,
            x1, cy + r1
        )
        arcTo(
            rect = Rect(x1 - r1, cy - r1, x1 + r1, cy + r1),
            startAngleDegrees = 90f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )
        close()
    }
    drawPath(path, color)
}
