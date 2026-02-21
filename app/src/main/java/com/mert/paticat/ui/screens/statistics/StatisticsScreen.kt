package com.mert.paticat.ui.screens.statistics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mert.paticat.data.local.entity.MealEntity
import com.mert.paticat.ui.components.*
import com.mert.paticat.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Calorie adding removed
    var selectedCategory by remember { mutableIntStateOf(0) } // 0: Activity, 1: Water, 2: History
    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.stats_title), fontWeight = FontWeight.Black, fontSize = 24.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. CATEGORY TABS
            EntranceAnimation {
                PremiumTabSelector(
                    options = listOf(
                        androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.stats_tab_activity),
                        androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.stats_water), // Was Nutrition
                        androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.stats_tab_history)
                    ),
                    selectedIndex = selectedCategory,
                    onSelect = { selectedCategory = it }
                )
            }
            
            // 3. CONTENT BASED ON TAB
            AnimatedVisibility(visible = selectedCategory == 0) {
                ActivityContent(uiState, numberFormat)
            }
            
            AnimatedVisibility(visible = selectedCategory == 1) {
                HydrationContent(
                    uiState = uiState, 
                    onAddWater = { viewModel.addWater(it) },
                    canUndo = uiState.lastAddedWater != null,
                    onUndo = { uiState.lastAddedWater?.let { viewModel.removeWater(it) } }
                )
            }
            
            AnimatedVisibility(visible = selectedCategory == 2) {
                HistoryContent(uiState, viewModel)
            }
            
            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}



@Composable
fun ActivityContent(uiState: StatisticsUiState, numberFormat: NumberFormat) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Steps Logic
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                     Column {
                         Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.stats_total_steps), style = MaterialTheme.typography.labelMedium)
                         Text(numberFormat.format(uiState.todayStats.steps), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                     }
                     Icon(Icons.Default.DirectionsWalk, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { (uiState.todayStats.steps.toFloat() / uiState.stepGoal).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )
                Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.chart_label_goal, numberFormat.format(uiState.stepGoal)), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            }
        }
        
        // Grid for Distance and Burned
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PremiumStatCard(
                androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.stats_distance),
                String.format("%.1f km", uiState.todayStats.distanceKm),
                Icons.Default.TrendingUp,
                MaterialTheme.colorScheme.secondary,
                Modifier.weight(1f)
            )
            PremiumStatCard(
                androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.stats_burned),
                "${uiState.todayStats.caloriesBurned} kcal",
                Icons.Default.LocalFireDepartment,
                MaterialTheme.colorScheme.primary,
                Modifier.weight(1f)
            )
        }
        
        // Native Ad
        NativeAdCard(nativeAd = uiState.nativeAd)
        
        // Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
             Column(modifier = Modifier.padding(20.dp)) {
                 Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.stats_weekly_activity), fontWeight = FontWeight.Bold)
                 Spacer(modifier = Modifier.height(16.dp))
                 if (uiState.chartData.isNotEmpty()) {
                     AnimatedWeeklyBarChart(
                        data = uiState.chartData,
                        labels = uiState.chartLabels,
                        maxValue = (uiState.chartData.maxOrNull() ?: 10000) + 1000,
                        barColor = MaterialTheme.colorScheme.primary,
                        goalValue = uiState.stepGoal
                    )
                 } else {
                     Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.stats_chart_no_data))
                 }
             }
        }
    }
}

@Composable
fun HydrationContent(
    uiState: StatisticsUiState, 
    onAddWater: (Int) -> Unit,
    canUndo: Boolean = false,
    onUndo: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Hydration Card
        Card(
             modifier = Modifier.fillMaxWidth(),
             shape = RoundedCornerShape(24.dp),
             colors = CardDefaults.cardColors(containerColor = PremiumBlue.copy(alpha = 0.05f)),
             border = androidx.compose.foundation.BorderStroke(1.dp, PremiumBlue.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                     Column {
                         Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.stats_water), fontWeight = FontWeight.Bold, color = PremiumBlue)
                         Text("${uiState.todayStats.waterMl} / ${uiState.waterGoal} ml", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = PremiumBlue)
                     }
                     Icon(Icons.Default.LocalDrink, null, tint = PremiumBlue, modifier = Modifier.size(40.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Animated Water Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PremiumBlue.copy(alpha = 0.1f))
                ) {
                    val progress = (uiState.todayStats.waterMl.toFloat() / uiState.waterGoal).coerceIn(0f, 1f)
                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = tween(1000, easing = FastOutSlowInEasing),
                        label = "water_progress"
                    )
                    
                    // Simple blue fill with wave-like feel (reusing if possible or local implementation)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(PremiumBlue, PremiumBlue.copy(alpha = 0.8f))
                                )
                            )
                    )
                    
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        modifier = Modifier.align(Alignment.Center),
                        color = if (progress > 0.5f) Color.White else PremiumBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    listOf(200, 300, 500).forEach { amount ->
                        Button(
                            onClick = { onAddWater(amount) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PremiumBlue,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+$amount", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    // Undo button
                    if (canUndo) {
                        IconButton(
                            onClick = onUndo,
                            modifier = Modifier
                                .size(40.dp)
                                .background(PremiumBlue.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                Icons.Default.Undo,
                                contentDescription = "Undo",
                                tint = PremiumBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryContent(uiState: StatisticsUiState, viewModel: StatisticsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PremiumTabSelector(
            options = listOf(
                androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.stats_weekly),
                androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.stats_monthly)
            ),
            selectedIndex = if (uiState.selectedRange == StatsRange.WEEKLY) 0 else 1,
            onSelect = { 
                viewModel.selectRange(if (it == 0) StatsRange.WEEKLY else StatsRange.MONTHLY)
            }
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
             Column(modifier = Modifier.padding(20.dp)) {
                 Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.stats_general_performance), fontWeight = FontWeight.Bold)
                 Spacer(modifier = Modifier.height(16.dp))
                 
                 Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                     Column(horizontalAlignment = Alignment.CenterHorizontally) {
                         Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.stats_best_day), style = MaterialTheme.typography.bodySmall)
                         Text("${uiState.detailedStats.bestDaySteps}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                     }
                     Column(horizontalAlignment = Alignment.CenterHorizontally) {
                         Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.stats_daily_avg), style = MaterialTheme.typography.bodySmall)
                         Text("${uiState.detailedStats.avgSteps}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                     }
                 }
                 
                 Spacer(modifier = Modifier.height(20.dp))
                 if (uiState.chartData.isNotEmpty()) {
                     AnimatedWeeklyBarChart(
                        data = uiState.chartData,
                        labels = uiState.chartLabels,
                        maxValue = (uiState.chartData.maxOrNull() ?: 10000) + 1000,
                        barColor = MaterialTheme.colorScheme.primary,
                        goalValue = uiState.stepGoal
                    )
                 }
             }
        }
    }
}


@Composable
fun MiniLegend(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// MealItem removed

@Composable
fun PremiumTabSelector(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(Color.LightGray.copy(alpha = 0.2f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, text ->
            val isSelected = selectedIndex == index
            val bgColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            val textColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50.dp))
                    .background(bgColor)
                    .clickable { onSelect(index) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text, fontWeight = FontWeight.Bold, color = textColor)
            }
        }
    }
}

@Composable
fun PremiumStatCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = color, overflow = TextOverflow.Ellipsis, maxLines = 1)
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// AddMealDialog removed
