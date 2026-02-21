package com.mert.paticat.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mert.paticat.ui.theme.AccentGold
import com.mert.paticat.ui.theme.PastelBlue
import com.mert.paticat.ui.theme.PastelMint
import com.mert.paticat.ui.theme.PastelPeach
import com.mert.paticat.ui.theme.PastelPink

/**
 * Beautiful animated bar chart for weekly data.
 */
@Composable
fun AnimatedWeeklyBarChart(
    data: List<Int>,
    labels: List<String>,
    maxValue: Int = 10000,
    barColor: Color = PastelPink,
    goalValue: Int? = null,
    modifier: Modifier = Modifier
) {
    require(data.size == labels.size) { "Data and labels must have the same size" }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEachIndexed { index, value ->
                    AnimatedBarWithLabel(
                        value = value,
                        maxValue = maxValue,
                        label = labels[index],
                        barColor = if (goalValue != null && value >= goalValue) PastelMint else barColor,
                        isToday = index == data.lastIndex
                    )
                }
            }
            
            // Goal line indicator
            goalValue?.let { goal ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(PastelMint)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.chart_label_goal, formatNumber(goal)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedBarWithLabel(
    value: Int,
    maxValue: Int,
    label: String,
    barColor: Color,
    isToday: Boolean
) {
    var animatedHeight by remember { mutableFloatStateOf(0f) }
    val heightAnimation by animateFloatAsState(
        targetValue = animatedHeight,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "bar_height"
    )
    
    LaunchedEffect(value) {
        animatedHeight = (value.toFloat() / maxValue).coerceIn(0f, 1f)
    }
    
    val maxHeight = 140.dp
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(40.dp)
    ) {
        // Value on top
        Text(
            text = formatNumber(value),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            fontSize = 9.sp,
            color = if (isToday) barColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Bar
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(maxHeight),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(barColor.copy(alpha = 0.15f))
            )
            
            // Animated fill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(heightAnimation)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(barColor, barColor.copy(alpha = 0.7f))
                        )
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isToday) barColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Circular progress chart with multiple rings.
 */
@Composable
fun MultiRingProgressChart(
    progressList: List<RingData>,
    size: Dp = 180.dp,
    strokeWidth: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    val animatedProgressList = progressList.mapIndexed { index, ring ->
        var animatedProgress by remember { mutableFloatStateOf(0f) }
        val progressAnimation by animateFloatAsState(
            targetValue = animatedProgress,
            animationSpec = tween(durationMillis = 1000 + (index * 200)),
            label = "ring_$index"
        )
        
        LaunchedEffect(ring.progress) {
            animatedProgress = ring.progress
        }
        
        ring.copy(progress = progressAnimation)
    }
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidthPx = strokeWidth.toPx()
            val gap = strokeWidthPx * 1.1f
            
            animatedProgressList.forEachIndexed { index, ring ->
                val radius = (size.toPx() / 2) - (strokeWidthPx / 2) - (index * gap)
                val arcSize = radius * 2
                val topLeft = Offset((size.toPx() - arcSize) / 2, (size.toPx() - arcSize) / 2)
                
                // Background arc
                drawArc(
                    color = ring.color.copy(alpha = 0.2f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
                
                // Progress arc
                drawArc(
                    color = ring.color,
                    startAngle = -90f,
                    sweepAngle = 360f * ring.progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }
    }
}

data class RingData(
    val progress: Float,
    val color: Color,
    val label: String
)

/**
 * Line chart for trends.
 */
@Composable
fun AnimatedLineChart(
    data: List<Int>,
    labels: List<String>,
    lineColor: Color = PastelBlue,
    fillGradient: Boolean = true,
    modifier: Modifier = Modifier
) {
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val progressAnimation by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "line_progress"
    )
    
    LaunchedEffect(data) {
        animatedProgress = 1f
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val maxValue = data.maxOrNull() ?: 1
            val minValue = data.minOrNull() ?: 0
            val valueRange = (maxValue - minValue).coerceAtLeast(1)
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height - 30.dp.toPx()
                val pointCount = (data.size * progressAnimation).toInt().coerceAtLeast(1)
                
                if (data.isEmpty()) return@Canvas
                
                val points = data.take(pointCount).mapIndexed { index, value ->
                    val x = (index.toFloat() / (data.size - 1).coerceAtLeast(1)) * width
                    val y = height - ((value - minValue).toFloat() / valueRange) * height
                    Offset(x, y)
                }
                
                if (points.size < 2) return@Canvas
                
                // Draw gradient fill
                if (fillGradient) {
                    val fillPath = Path().apply {
                        moveTo(points.first().x, height)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(points.last().x, height)
                        close()
                    }
                    
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineColor.copy(alpha = 0.3f),
                                lineColor.copy(alpha = 0.05f)
                            )
                        )
                    )
                }
                
                // Draw line
                val linePath = Path().apply {
                    points.forEachIndexed { index, point ->
                        if (index == 0) moveTo(point.x, point.y)
                        else lineTo(point.x, point.y)
                    }
                }
                
                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                
                // Draw points
                points.forEach { point ->
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                }
            }
            
            // Labels at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

/**
 * Horizontal progress bar with animation.
 */
@Composable
fun BeautifulProgressBar(
    progress: Float,
    label: String,
    currentValue: String,
    targetValue: String,
    color: Color = PastelPink,
    modifier: Modifier = Modifier
) {
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val progressAnimation by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )
    
    LaunchedEffect(progress) {
        animatedProgress = progress.coerceIn(0f, 1f)
    }
    
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$currentValue / $targetValue",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressAnimation)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(color, color.copy(alpha = 0.7f))
                        )
                    )
            )
        }
    }
}

/**
 * Format number with thousands separator or compact notation.
 * Uses compact format (1K, 10K) for numbers >= 1000 to prevent layout issues.
 */
private fun formatNumber(number: Int): String {
    return when {
        number >= 10000 -> "${number / 1000}K"
        number >= 1000 -> String.format("%.1fK", number / 1000f)
        else -> number.toString()
    }
}

/**
 * Calorie burn donut chart.
 */
@Composable
fun CalorieDonutChart(
    consumed: Int,
    burned: Int,
    goal: Int,
    size: Dp = 160.dp,
    modifier: Modifier = Modifier
) {
    val consumedProgress = (consumed.toFloat() / goal).coerceIn(0f, 1f)
    val burnedProgress = (burned.toFloat() / goal).coerceIn(0f, 1f)
    
    var animatedConsumed by remember { mutableFloatStateOf(0f) }
    var animatedBurned by remember { mutableFloatStateOf(0f) }
    
    val consumedAnimation by animateFloatAsState(
        targetValue = animatedConsumed,
        animationSpec = tween(durationMillis = 1000),
        label = "consumed"
    )
    val burnedAnimation by animateFloatAsState(
        targetValue = animatedBurned,
        animationSpec = tween(durationMillis = 1000, delayMillis = 200),
        label = "burned"
    )
    
    LaunchedEffect(consumed, burned) {
        animatedConsumed = consumedProgress
        animatedBurned = burnedProgress
    }
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 20.dp.toPx()
            val radius = (size.toPx() - strokeWidth) / 2
            val center = Offset(size.toPx() / 2, size.toPx() / 2)
            
            // Background
            drawCircle(
                color = Color.Gray.copy(alpha = 0.1f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            
            // Burned (green)
            drawArc(
                color = PastelMint,
                startAngle = -90f,
                sweepAngle = 360f * burnedAnimation,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // Consumed (orange) - smaller ring
            val innerRadius = radius - strokeWidth - 4.dp.toPx()
            drawArc(
                color = PastelPeach,
                startAngle = -90f,
                sweepAngle = 360f * consumedAnimation,
                useCenter = false,
                topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                size = Size(innerRadius * 2, innerRadius * 2),
                style = Stroke(width = strokeWidth * 0.7f, cap = StrokeCap.Round)
            )
        }
        
        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${burned}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PastelMint
            )
            Text(
                text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.stats_burned).lowercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
