package com.mert.paticat.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.mert.paticat.ui.theme.PremiumBlue
import com.mert.paticat.ui.theme.PremiumPeach
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SummaryDashboard(steps: Int, stepGoal: Int, calories: Int, calorieGoal: Int, water: Int, waterGoal: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Steps
            DashboardStatItem(
                label = stringResource(R.string.stats_steps),
                value = NumberFormat.getNumberInstance(Locale.getDefault()).format(steps),
                progress = (steps.toFloat() / stepGoal).coerceIn(0f, 1f),
                color = MaterialTheme.colorScheme.primary,
                icon = Icons.Default.DirectionsWalk
            )

            // Calories
            DashboardStatItem(
                label = stringResource(R.string.unit_kcal),
                value = "$calories",
                progress = (calories.toFloat() / calorieGoal).coerceIn(0f, 1f),
                color = PremiumPeach,
                icon = Icons.Default.LocalFireDepartment
            )

            // Water
            DashboardStatItem(
                label = stringResource(R.string.unit_ml),
                value = "$water",
                progress = (water.toFloat() / waterGoal).coerceIn(0f, 1f),
                color = PremiumBlue,
                icon = Icons.Default.LocalDrink
            )
        }
    }
}

@Composable
fun DashboardStatItem(label: String, value: String, progress: Float, color: Color, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(80.dp),
                color = color.copy(alpha = 0.1f),
                strokeWidth = 8.dp,
                strokeCap = StrokeCap.Round
            )
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(80.dp),
                color = color,
                strokeWidth = 8.dp,
                strokeCap = StrokeCap.Round
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    value,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    label,
                    fontSize = 11.sp,
                    lineHeight = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
