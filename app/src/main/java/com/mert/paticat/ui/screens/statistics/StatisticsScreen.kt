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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mert.paticat.R
import com.mert.paticat.domain.model.InteractionSummary
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
    var selectedCategory by remember { mutableIntStateOf(0) }
    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.stats_title),
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    )
                },
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // CATEGORY TABS (4 tabs)
            EntranceAnimation {
                PremiumTabSelector(
                    options = listOf(
                        stringResource(R.string.stats_tab_activity),
                        stringResource(R.string.stats_water),
                        stringResource(R.string.stats_tab_cat_care),
                        stringResource(R.string.stats_tab_history)
                    ),
                    selectedIndex = selectedCategory,
                    onSelect = { selectedCategory = it }
                )
            }

            // CONTENT BASED ON TAB
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
                CatCareContent(uiState, viewModel)
            }

            AnimatedVisibility(visible = selectedCategory == 3) {
                HistoryContent(uiState, viewModel)
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

// ==================== SHARED HELPERS ====================

@Composable
fun GradientStatCard(
    gradient: Brush,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(gradient)
            .shadow(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            content = content
        )
    }
}

@Composable
fun GradientIconBox(
    icon: ImageVector,
    color: Color,
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0.12f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(iconSize))
    }
}

// ==================== ACTIVITY TAB ====================

@Composable
fun ActivityContent(uiState: StatisticsUiState, numberFormat: NumberFormat) {
    val stepColor = MaterialTheme.colorScheme.primary

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Hero Steps Card — gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            stepColor.copy(alpha = 0.18f),
                            stepColor.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            stringResource(R.string.stats_total_steps),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            numberFormat.format(uiState.todayStats.steps),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = stepColor
                        )
                    }
                    // Circular progress around icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(72.dp)
                    ) {
                        val animProg by animateFloatAsState(
                            targetValue = (uiState.todayStats.steps.toFloat() / uiState.stepGoal).coerceIn(0f, 1f),
                            animationSpec = tween(1200, easing = FastOutSlowInEasing),
                            label = "step_circle"
                        )
                        CircularProgressIndicator(
                            progress = { animProg },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 6.dp,
                            color = stepColor,
                            trackColor = stepColor.copy(alpha = 0.12f),
                            strokeCap = StrokeCap.Round
                        )
                        Icon(
                            Icons.Default.DirectionsWalk,
                            null,
                            tint = stepColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Gradient progress bar
                BeautifulProgressBar(
                    progress = (uiState.todayStats.steps.toFloat() / uiState.stepGoal).coerceIn(0f, 1f),
                    label = "",
                    currentValue = "",
                    targetValue = "",
                    color = stepColor
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.chart_label_goal, numberFormat.format(uiState.stepGoal)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Distance & Calories grid
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SoftStatCard(
                title = stringResource(R.string.stats_distance),
                value = String.format("%.1f km", uiState.todayStats.distanceKm),
                icon = Icons.Default.TrendingUp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            SoftStatCard(
                title = stringResource(R.string.stats_burned),
                value = "${uiState.todayStats.caloriesBurned} kcal",
                icon = Icons.Default.LocalFireDepartment,
                color = PremiumPeach,
                modifier = Modifier.weight(1f)
            )
        }

        // Native Ad
        NativeAdCard(nativeAd = uiState.nativeAd)

        // Weekly Chart Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GradientIconBox(Icons.Default.BarChart, stepColor, size = 36.dp, iconSize = 18.dp)
                    Text(
                        stringResource(R.string.stats_weekly_activity),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (uiState.chartData.isNotEmpty()) {
                    AnimatedWeeklyBarChart(
                        data = uiState.chartData,
                        labels = uiState.chartLabels,
                        maxValue = uiState.stepGoal.coerceAtLeast(1),
                        barColor = stepColor,
                        goalValue = uiState.stepGoal
                    )
                } else {
                    Text(
                        stringResource(R.string.stats_chart_no_data),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SoftStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(color.copy(alpha = 0.15f), color.copy(alpha = 0.05f))
                )
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            GradientIconBox(icon, color, size = 44.dp, iconSize = 22.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                value,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = color,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Keep legacy PremiumStatCard alias for any other usages
@Composable
fun PremiumStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) = SoftStatCard(title, value, icon, color, modifier)

// ==================== HYDRATION TAB ====================

@Composable
fun HydrationContent(
    uiState: StatisticsUiState,
    onAddWater: (Int) -> Unit,
    canUndo: Boolean = false,
    onUndo: () -> Unit = {}
) {
    val waterColor = MaterialTheme.colorScheme.secondary

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Hero Water Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(waterColor.copy(alpha = 0.18f), waterColor.copy(alpha = 0.04f))
                    )
                )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            stringResource(R.string.stats_water),
                            style = MaterialTheme.typography.labelLarge,
                            color = waterColor.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${uiState.todayStats.waterMl}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = waterColor
                        )
                        Text(
                            "/ ${uiState.waterGoal} ml",
                            style = MaterialTheme.typography.bodyMedium,
                            color = waterColor.copy(alpha = 0.7f)
                        )
                    }
                    // Water drop ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(72.dp)
                    ) {
                        val animProg by animateFloatAsState(
                            targetValue = (uiState.todayStats.waterMl.toFloat() / uiState.waterGoal).coerceIn(0f, 1f),
                            animationSpec = tween(1200, easing = FastOutSlowInEasing),
                            label = "water_circle"
                        )
                        CircularProgressIndicator(
                            progress = { animProg },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 6.dp,
                            color = waterColor,
                            trackColor = waterColor.copy(alpha = 0.12f),
                            strokeCap = StrokeCap.Round
                        )
                        Icon(
                            Icons.Default.LocalDrink,
                            null,
                            tint = waterColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Taller animated progress bar
                val progress = (uiState.todayStats.waterMl.toFloat() / uiState.waterGoal).coerceIn(0f, 1f)
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(1000, easing = FastOutSlowInEasing),
                    label = "water_progress"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(waterColor.copy(alpha = 0.10f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(waterColor, waterColor.copy(alpha = 0.7f))
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            color = if (progress > 0.45f) MaterialTheme.colorScheme.onPrimary else waterColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${uiState.todayStats.waterMl} / ${uiState.waterGoal} ml",
                            color = if (progress > 0.65f) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else waterColor.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Add water buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(200, 300, 500).forEach { amount ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(waterColor, waterColor.copy(alpha = 0.75f))
                                    )
                                )
                                .clickable { onAddWater(amount) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    "${amount}ml",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    if (canUndo) {
                        IconButton(
                            onClick = onUndo,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(waterColor.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                Icons.Default.Undo,
                                contentDescription = "Undo",
                                tint = waterColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== CAT CARE TAB ====================

@Composable
fun CatCareContent(uiState: StatisticsUiState, viewModel: StatisticsViewModel) {
    val primary = MaterialTheme.colorScheme.primary
    val summary = uiState.catCareSummary

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Range selector
        PremiumTabSelector(
            options = listOf(
                stringResource(R.string.stats_range_daily),
                stringResource(R.string.stats_range_weekly),
                stringResource(R.string.stats_range_monthly)
            ),
            selectedIndex = when (uiState.catCareRange) {
                StatsRange.DAILY -> 0
                StatsRange.WEEKLY -> 1
                StatsRange.MONTHLY -> 2
            },
            onSelect = {
                viewModel.selectCatCareRange(
                    when (it) {
                        0 -> StatsRange.DAILY
                        1 -> StatsRange.WEEKLY
                        else -> StatsRange.MONTHLY
                    }
                )
            }
        )

        // Care Score Card
        CareScoreCard(summary.careScore, primary)

        // Summary Cards Grid
        CareInteractionSummaryGrid(summary)

        // Game Breakdown Card
        if (summary.totalGames > 0) {
            GameBreakdownCard(summary)
        }

        // Interaction Chart
        if (uiState.careChartData.isNotEmpty() && uiState.catCareRange != StatsRange.DAILY) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GradientIconBox(Icons.Default.Favorite, primary, size = 36.dp, iconSize = 18.dp)
                        Text(
                            stringResource(R.string.stats_care_chart_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    AnimatedWeeklyBarChart(
                        data = uiState.careChartData,
                        labels = uiState.careChartLabels,
                        maxValue = (uiState.careChartData.maxOrNull() ?: 10) + 5,
                        barColor = primary
                    )
                }
            }
        }

        // Empty state
        if (summary.totalInteractions == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🐱", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.stats_care_no_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CareScoreCard(score: Int, accentColor: Color) {
    val ratingText = when {
        score >= 80 -> "🌟 Mükemmel!"
        score >= 60 -> "😊 Çok İyi!"
        score >= 40 -> "👍 İyi"
        score >= 20 -> "💪 Gelişiyor"
        else -> "🐾 Başlangıç"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(accentColor.copy(alpha = 0.15f), accentColor.copy(alpha = 0.04f))
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.stats_care_score),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(R.string.stats_care_score_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        ratingText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Circular progress
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(88.dp)
            ) {
                val animatedProgress by animateFloatAsState(
                    targetValue = score / 100f,
                    animationSpec = tween(1200, easing = FastOutSlowInEasing),
                    label = "care_score_progress"
                )
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 9.dp,
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.12f),
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$score",
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp,
                        color = accentColor
                    )
                    Text(
                        "/100",
                        fontSize = 10.sp,
                        color = accentColor.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun CareInteractionSummaryGrid(summary: InteractionSummary) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CareStatMiniCard(
                emoji = "🍖",
                label = stringResource(R.string.stats_care_feed_count),
                count = summary.feedCount,
                color = primary,
                modifier = Modifier.weight(1f)
            )
            CareStatMiniCard(
                emoji = "🎮",
                label = stringResource(R.string.stats_care_game_count),
                count = summary.totalGames,
                color = secondary,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CareStatMiniCard(
                emoji = "😴",
                label = stringResource(R.string.stats_care_sleep_count),
                count = summary.sleepCount,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            CareStatMiniCard(
                emoji = "💖",
                label = stringResource(R.string.stats_care_pet_count),
                count = summary.petCount,
                color = PremiumPink,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CareStatMiniCard(
    emoji: String,
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(color.copy(alpha = 0.14f), color.copy(alpha = 0.03f))
                )
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Emoji in a soft circle background
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 26.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "$count",
                fontWeight = FontWeight.Black,
                fontSize = 26.sp,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GameBreakdownCard(summary: InteractionSummary) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GradientIconBox(Icons.Default.SportsEsports, primary, size = 36.dp, iconSize = 18.dp)
                Text(
                    stringResource(R.string.stats_care_game_breakdown),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            val gameItems = listOf(
                Triple("✊", stringResource(R.string.stats_game_rps), summary.gameRpsCount),
                Triple("🎰", stringResource(R.string.stats_game_slots), summary.gameSlotsCount),
                Triple("🧠", stringResource(R.string.stats_game_memory), summary.gameMemoryCount),
                Triple("⚡", stringResource(R.string.stats_game_reflex), summary.gameReflexCount)
            ).filter { it.third > 0 }

            val maxCount = gameItems.maxOfOrNull { it.third } ?: 1

            gameItems.forEachIndexed { idx, (emoji, name, count) ->
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(emoji, fontSize = 20.sp)
                            Text(
                                name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            stringResource(R.string.stats_times, count),
                            fontWeight = FontWeight.Bold,
                            color = primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    // Mini progress bar per game
                    val barProgress by animateFloatAsState(
                        targetValue = count.toFloat() / maxCount,
                        animationSpec = tween(800, easing = FastOutSlowInEasing),
                        label = "game_bar_$idx"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(primary.copy(alpha = 0.10f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(barProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(primary, primary.copy(alpha = 0.6f))
                                    )
                                )
                        )
                    }
                }
                if (idx < gameItems.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// ==================== HISTORY TAB ====================

@Composable
fun HistoryContent(uiState: StatisticsUiState, viewModel: StatisticsViewModel) {
    val historyColor = PremiumPurple

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PremiumTabSelector(
            options = listOf(
                stringResource(R.string.stats_weekly),
                stringResource(R.string.stats_monthly)
            ),
            selectedIndex = if (uiState.selectedRange == StatsRange.MONTHLY) 1 else 0,
            onSelect = {
                viewModel.selectRange(if (it == 0) StatsRange.WEEKLY else StatsRange.MONTHLY)
            }
        )

        if (uiState.dateRangeLabel.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(20.dp))
                    .background(historyColor.copy(alpha = 0.10f))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = uiState.dateRangeLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = historyColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Top 3 performance metrics — individual cards
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HistoryMetricCard(
                icon = Icons.Default.EmojiEvents,
                label = stringResource(R.string.stats_best_day),
                value = "${uiState.detailedStats.bestDaySteps}",
                color = AccentGold,
                modifier = Modifier.weight(1f)
            )
            HistoryMetricCard(
                icon = Icons.Default.TrendingUp,
                label = stringResource(R.string.stats_daily_avg),
                value = "${uiState.detailedStats.avgSteps}",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            HistoryMetricCard(
                icon = Icons.Default.CheckCircle,
                label = stringResource(R.string.stats_goal_success),
                value = "${uiState.detailedStats.completionRate}%",
                color = PremiumMint,
                modifier = Modifier.weight(1f)
            )
        }

        // Total stats card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(historyColor.copy(alpha = 0.10f), historyColor.copy(alpha = 0.02f))
                    )
                )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    stringResource(R.string.stats_general_performance),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    HistoryStatItem(
                        icon = "👟",
                        label = stringResource(R.string.stats_total_steps),
                        value = "${uiState.detailedStats.totalSteps}"
                    )
                    HistoryStatItem(
                        icon = "🔥",
                        label = stringResource(R.string.stats_burned),
                        value = "${uiState.detailedStats.totalCaloriesBurned} kcal"
                    )
                    HistoryStatItem(
                        icon = "💧",
                        label = stringResource(R.string.stats_water),
                        value = "${uiState.detailedStats.totalWater} ml"
                    )
                }
            }
        }

        // Chart card
        if (uiState.chartData.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GradientIconBox(Icons.Default.BarChart, historyColor, size = 36.dp, iconSize = 18.dp)
                        Text(
                            stringResource(R.string.stats_weekly_activity),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    AnimatedWeeklyBarChart(
                        data = uiState.chartData,
                        labels = uiState.chartLabels,
                        maxValue = uiState.stepGoal.coerceAtLeast(1),
                        barColor = historyColor,
                        goalValue = uiState.stepGoal
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryMetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(color.copy(alpha = 0.16f), color.copy(alpha = 0.04f))
                )
            )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GradientIconBox(icon, color, size = 40.dp, iconSize = 20.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = color,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
fun HistoryStatItem(icon: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ==================== LEGACY / REUSABLE ====================

@Composable
fun MiniLegend(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
