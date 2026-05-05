package com.mert.paticat.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mert.paticat.R
import com.mert.paticat.ui.components.marshmallow.PillowCard
import com.mert.paticat.ui.theme.PremiumBlue
import com.mert.paticat.ui.theme.PremiumPeach
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SummaryDashboard(
    steps: Int,
    stepGoal: Int,
    calories: Int,
    calorieGoal: Int,
    water: Int,
    waterGoal: Int,
) {
    PillowCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 20.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.stats_summary_title),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DashboardStatItem(
                    label = stringResource(R.string.stats_steps),
                    value = NumberFormat.getNumberInstance(Locale.getDefault()).format(steps),
                    progress = (steps.toFloat() / stepGoal).coerceIn(0f, 1f),
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Default.DirectionsWalk,
                )
                DashboardStatItem(
                    label = stringResource(R.string.unit_kcal),
                    value = "$calories",
                    progress = (calories.toFloat() / calorieGoal).coerceIn(0f, 1f),
                    color = PremiumPeach,
                    icon = Icons.Default.LocalFireDepartment,
                )
                DashboardStatItem(
                    label = stringResource(R.string.unit_ml),
                    value = "$water",
                    progress = (water.toFloat() / waterGoal).coerceIn(0f, 1f),
                    color = PremiumBlue,
                    icon = Icons.Default.LocalDrink,
                )
            }
        }
    }
}

@Composable
fun DashboardStatItem(label: String, value: String, progress: Float, color: Color, icon: ImageVector) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000),
        label = "progress",
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(86.dp),
                color = color.copy(alpha = 0.12f),
                strokeWidth = 9.dp,
                strokeCap = StrokeCap.Round,
                trackColor = Color.Transparent,
            )
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(86.dp),
                color = color,
                strokeWidth = 9.dp,
                strokeCap = StrokeCap.Round,
                trackColor = Color.Transparent,
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = value,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    lineHeight = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
