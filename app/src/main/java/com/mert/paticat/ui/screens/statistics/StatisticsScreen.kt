package com.mert.paticat.ui.screens.statistics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.mert.paticat.ui.components.AnimatedWeeklyBarChart
import com.mert.paticat.ui.components.BeautifulProgressBar
import com.mert.paticat.ui.components.NativeAdCard
import com.mert.paticat.ui.components.marshmallow.ActionPillButton
import com.mert.paticat.ui.components.marshmallow.ChipPill
import com.mert.paticat.ui.components.marshmallow.MarshmallowTabs
import com.mert.paticat.ui.components.marshmallow.PillowCard
import com.mert.paticat.ui.components.marshmallow.pillowPress
import com.mert.paticat.ui.components.marshmallow.softEntrance
import com.mert.paticat.ui.theme.AccentGold
import com.mert.paticat.ui.theme.Dimensions
import com.mert.paticat.ui.theme.PremiumMint
import com.mert.paticat.ui.theme.PremiumPeach
import com.mert.paticat.ui.theme.PremiumPink
import com.mert.paticat.ui.theme.PremiumPurple
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedCategory by rememberSaveable { mutableIntStateOf(0) }
    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.stats_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimensions.screenPaddingHorizontal),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Spacer(Modifier.height(2.dp))
                Box(modifier = Modifier.softEntrance()) {
                    MarshmallowTabs(
                        selectedIndex = selectedCategory,
                        items = listOf(
                            stringResource(R.string.stats_tab_activity),
                            stringResource(R.string.stats_water),
                            stringResource(R.string.stats_tab_cat_care),
                            stringResource(R.string.stats_tab_history),
                        ),
                        onSelect = { selectedCategory = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                AnimatedVisibility(visible = selectedCategory == 0) {
                    ActivityContent(uiState, numberFormat)
                }
                AnimatedVisibility(visible = selectedCategory == 1) {
                    HydrationContent(
                        uiState = uiState,
                        onAddWater = { viewModel.addWater(it) },
                        canUndo = uiState.lastAddedWater != null,
                        onUndo = { uiState.lastAddedWater?.let { viewModel.removeWater(it) } },
                    )
                }
                AnimatedVisibility(visible = selectedCategory == 2) {
                    CatCareContent(uiState, viewModel)
                }
                AnimatedVisibility(visible = selectedCategory == 3) {
                    HistoryContent(uiState, viewModel)
                }

                Spacer(modifier = Modifier.height(Dimensions.bottomNavClearance))
            }
        }
    }
}

// ==================== HELPERS ====================

@Composable
private fun GradientIconBox(icon: ImageVector, color: Color, size: Dp = 44.dp, iconSize: Dp = 22.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(iconSize))
    }
}

// ==================== ACTIVITY ====================

@Composable
fun ActivityContent(uiState: StatisticsUiState, numberFormat: NumberFormat) {
    val stepColor = MaterialTheme.colorScheme.primary
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PillowCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = stepColor.copy(alpha = 0.12f),
            contentPadding = 22.dp,
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            stringResource(R.string.stats_total_steps),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = numberFormat.format(uiState.todayStats.steps),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = stepColor,
                        )
                    }
                    Box(modifier = Modifier.size(74.dp), contentAlignment = Alignment.Center) {
                        val animProg by animateFloatAsState(
                            targetValue = (uiState.todayStats.steps.toFloat() / uiState.stepGoal).coerceIn(0f, 1f),
                            animationSpec = tween(1200, easing = FastOutSlowInEasing),
                            label = "step_circle",
                        )
                        CircularProgressIndicator(
                            progress = { animProg },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 7.dp,
                            color = stepColor,
                            trackColor = stepColor.copy(alpha = 0.14f),
                            strokeCap = StrokeCap.Round,
                        )
                        Icon(Icons.Default.DirectionsWalk, null, tint = stepColor, modifier = Modifier.size(28.dp))
                    }
                }

                Spacer(Modifier.height(14.dp))

                BeautifulProgressBar(
                    progress = (uiState.todayStats.steps.toFloat() / uiState.stepGoal).coerceIn(0f, 1f),
                    label = "",
                    currentValue = "",
                    targetValue = "",
                    color = stepColor,
                )

                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.chart_label_goal, numberFormat.format(uiState.stepGoal)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SoftStatCard(
                title = stringResource(R.string.stats_distance),
                value = String.format("%.1f km", uiState.todayStats.distanceKm),
                icon = Icons.Default.TrendingUp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
            )
            SoftStatCard(
                title = stringResource(R.string.stats_burned),
                value = "${uiState.todayStats.caloriesBurned} kcal",
                icon = Icons.Default.LocalFireDepartment,
                color = PremiumPeach,
                modifier = Modifier.weight(1f),
            )
        }

        NativeAdCard(nativeAd = uiState.nativeAd)

        WeeklyChartCard(
            title = stringResource(R.string.stats_weekly_activity),
            color = stepColor,
            chartData = uiState.chartData,
            chartLabels = uiState.chartLabels,
            maxValue = uiState.stepGoal.coerceAtLeast(1),
            goalValue = uiState.stepGoal,
        )
    }
}

@Composable
fun SoftStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
) {
    PillowCard(
        modifier = modifier,
        backgroundColor = color.copy(alpha = 0.12f),
        contentPadding = 16.dp,
    ) {
        Column {
            GradientIconBox(icon, color, size = 44.dp, iconSize = 22.dp)
            Spacer(Modifier.height(10.dp))
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = color,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WeeklyChartCard(
    title: String,
    color: Color,
    chartData: List<Int>,
    chartLabels: List<String>,
    maxValue: Int,
    goalValue: Int? = null,
) {
    PillowCard(modifier = Modifier.fillMaxWidth(), contentPadding = 18.dp) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GradientIconBox(Icons.Default.BarChart, color, size = 36.dp, iconSize = 18.dp)
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(14.dp))
            if (chartData.isNotEmpty()) {
                AnimatedWeeklyBarChart(
                    data = chartData,
                    labels = chartLabels,
                    maxValue = maxValue,
                    barColor = color,
                    goalValue = goalValue,
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val chartEmptyCd = stringResource(R.string.empty_state_chart_emoji_cd)
                    Text(
                        text = "📊",
                        fontSize = 36.sp,
                        modifier = Modifier.semantics { contentDescription = chartEmptyCd },
                    )
                    Text(
                        text = stringResource(R.string.stats_chart_no_data),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

// ==================== HYDRATION ====================

@Composable
fun HydrationContent(
    uiState: StatisticsUiState,
    onAddWater: (Int) -> Unit,
    canUndo: Boolean = false,
    onUndo: () -> Unit = {},
) {
    val waterColor = MaterialTheme.colorScheme.secondary
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PillowCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = waterColor.copy(alpha = 0.12f),
            contentPadding = 22.dp,
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            stringResource(R.string.stats_water),
                            style = MaterialTheme.typography.labelLarge,
                            color = waterColor.copy(alpha = 0.85f),
                        )
                        Text(
                            text = "${uiState.todayStats.waterMl}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = waterColor,
                        )
                        Text(
                            text = "/ ${uiState.waterGoal} ml",
                            style = MaterialTheme.typography.bodyMedium,
                            color = waterColor.copy(alpha = 0.7f),
                        )
                    }
                    Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                        val animProg by animateFloatAsState(
                            targetValue = (uiState.todayStats.waterMl.toFloat() / uiState.waterGoal).coerceIn(0f, 1f),
                            animationSpec = tween(1200, easing = FastOutSlowInEasing),
                            label = "water_circle",
                        )
                        CircularProgressIndicator(
                            progress = { animProg },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 7.dp,
                            color = waterColor,
                            trackColor = waterColor.copy(alpha = 0.14f),
                            strokeCap = StrokeCap.Round,
                        )
                        Icon(Icons.Default.LocalDrink, null, tint = waterColor, modifier = Modifier.size(28.dp))
                    }
                }

                Spacer(Modifier.height(14.dp))

                val progress = (uiState.todayStats.waterMl.toFloat() / uiState.waterGoal).coerceIn(0f, 1f)
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(1000),
                    label = "water_progress",
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(waterColor.copy(alpha = 0.16f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(listOf(waterColor, waterColor.copy(alpha = 0.8f))),
                            ),
                    )
                    // TODO(A2): replace with dedicated onWaterFill / onWaterTrack tokens.
                    // Fill is solid waterColor → Color.White contrast holds across light+dark.
                    // Track is waterColor.alpha=0.16 → onSurface keeps WCAG AA on light theme
                    // where waterColor's mid-tone fails.
                    Text(
                        text = "${(progress * 100).toInt()}% · ${uiState.todayStats.waterMl}/${uiState.waterGoal} ml",
                        modifier = Modifier.align(Alignment.Center),
                        color = if (progress > 0.5f) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    listOf(200, 300, 500).forEach { amount ->
                        ActionPillButton(
                            text = "+${amount}",
                            onClick = { onAddWater(amount) },
                            backgroundColor = waterColor,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (canUndo) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(waterColor.copy(alpha = 0.18f))
                                .pillowPress(onClick = onUndo),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Undo, stringResource(R.string.undo_action), tint = waterColor, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

// ==================== CAT CARE ====================

@Composable
fun CatCareContent(uiState: StatisticsUiState, viewModel: StatisticsViewModel) {
    val primary = MaterialTheme.colorScheme.primary
    val summary = uiState.catCareSummary
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        MarshmallowTabs(
            selectedIndex = when (uiState.catCareRange) {
                StatsRange.DAILY -> 0
                StatsRange.WEEKLY -> 1
                StatsRange.MONTHLY -> 2
            },
            items = listOf(
                stringResource(R.string.stats_range_daily),
                stringResource(R.string.stats_range_weekly),
                stringResource(R.string.stats_range_monthly),
            ),
            onSelect = {
                viewModel.selectCatCareRange(
                    when (it) {
                        0 -> StatsRange.DAILY
                        1 -> StatsRange.WEEKLY
                        else -> StatsRange.MONTHLY
                    },
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )

        CareScoreCard(summary.careScore, primary)
        CareInteractionSummaryGrid(summary)
        if (summary.totalGames > 0) {
            GameBreakdownCard(summary)
        }
        if (uiState.careChartData.isNotEmpty() && uiState.catCareRange != StatsRange.DAILY) {
            WeeklyChartCard(
                title = stringResource(R.string.stats_care_chart_title),
                color = primary,
                chartData = uiState.careChartData,
                chartLabels = uiState.careChartLabels,
                maxValue = (uiState.careChartData.maxOrNull() ?: 10) + 5,
            )
        }
        if (summary.totalInteractions == 0) {
            PillowCard(modifier = Modifier.fillMaxWidth(), contentPadding = 28.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val catEmptyCd = stringResource(R.string.empty_state_cat_emoji_cd)
                    Text(
                        text = "🐱",
                        fontSize = 48.sp,
                        modifier = Modifier.semantics { contentDescription = catEmptyCd },
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.stats_care_no_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
fun CareScoreCard(score: Int, accentColor: Color) {
    val ratingText = when {
        score >= 80 -> stringResource(R.string.stats_care_rating_excellent)
        score >= 60 -> stringResource(R.string.stats_care_rating_very_good)
        score >= 40 -> stringResource(R.string.stats_care_rating_good)
        score >= 20 -> stringResource(R.string.stats_care_rating_improving)
        else -> stringResource(R.string.stats_care_rating_beginner)
    }

    PillowCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = accentColor.copy(alpha = 0.12f),
        contentPadding = 18.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.stats_care_score),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.stats_care_score_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                ChipPill(
                    text = ratingText,
                    backgroundColor = accentColor.copy(alpha = 0.14f),
                    contentColor = accentColor,
                )
            }
            Spacer(Modifier.width(12.dp))
            Box(modifier = Modifier.size(88.dp), contentAlignment = Alignment.Center) {
                val animatedProgress by animateFloatAsState(
                    targetValue = score / 100f,
                    animationSpec = tween(1200),
                    label = "care_progress",
                )
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 9.dp,
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.14f),
                    strokeCap = StrokeCap.Round,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = accentColor,
                    )
                    Text(
                        text = "/100",
                        fontSize = 10.sp,
                        color = accentColor.copy(alpha = 0.6f),
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CareStatMiniCard(Icons.Filled.Fastfood, stringResource(R.string.stats_care_feed_count), summary.feedCount, primary, Modifier.weight(1f))
            CareStatMiniCard(Icons.Filled.SportsEsports, stringResource(R.string.stats_care_game_count), summary.totalGames, secondary, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CareStatMiniCard(Icons.Filled.Bedtime, stringResource(R.string.stats_care_sleep_count), summary.sleepCount, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
            CareStatMiniCard(Icons.Filled.Favorite, stringResource(R.string.stats_care_pet_count), summary.petCount, PremiumPink, Modifier.weight(1f))
        }
    }
}

@Composable
fun CareStatMiniCard(icon: ImageVector, label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    PillowCard(
        modifier = modifier,
        backgroundColor = color.copy(alpha = 0.10f),
        contentPadding = 14.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GradientIconBox(icon, color, size = 50.dp, iconSize = 24.dp)
            Spacer(Modifier.height(8.dp))
            Text(text = "$count", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = color)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun GameBreakdownCard(summary: InteractionSummary) {
    val primary = MaterialTheme.colorScheme.primary
    PillowCard(modifier = Modifier.fillMaxWidth(), contentPadding = 18.dp) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GradientIconBox(Icons.Default.SportsEsports, primary, size = 36.dp, iconSize = 18.dp)
                Text(text = stringResource(R.string.stats_care_game_breakdown), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(14.dp))

            val gameItems = listOf(
                Triple("✊", stringResource(R.string.stats_game_rps), summary.gameRpsCount),
                Triple("🎰", stringResource(R.string.stats_game_slots), summary.gameSlotsCount),
                Triple("🧠", stringResource(R.string.stats_game_memory), summary.gameMemoryCount),
                Triple("⚡", stringResource(R.string.stats_game_reflex), summary.gameReflexCount),
                Triple("🧺", stringResource(R.string.stats_game_catch), summary.gameCatchCount),
            ).filter { it.third > 0 }

            val maxCount = gameItems.maxOfOrNull { it.third } ?: 1

            gameItems.forEachIndexed { idx, (emoji, name, count) ->
                Column(modifier = Modifier.padding(vertical = 5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(emoji, fontSize = 20.sp)
                            Text(name, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            text = stringResource(R.string.stats_times, count),
                            fontWeight = FontWeight.Bold,
                            color = primary,
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    val barProgress by animateFloatAsState(
                        targetValue = count.toFloat() / maxCount,
                        animationSpec = tween(800),
                        label = "game_bar_$idx",
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(primary.copy(alpha = 0.10f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(barProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(primary, primary.copy(alpha = 0.7f))),
                                ),
                        )
                    }
                }
                if (idx < gameItems.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                }
            }
        }
    }
}

// ==================== HISTORY ====================

@Composable
fun HistoryContent(uiState: StatisticsUiState, viewModel: StatisticsViewModel) {
    val historyColor = PremiumPurple
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        MarshmallowTabs(
            selectedIndex = if (uiState.selectedRange == StatsRange.MONTHLY) 1 else 0,
            items = listOf(
                stringResource(R.string.stats_weekly),
                stringResource(R.string.stats_monthly),
            ),
            onSelect = { viewModel.selectRange(if (it == 0) StatsRange.WEEKLY else StatsRange.MONTHLY) },
            modifier = Modifier.fillMaxWidth(),
        )

        if (uiState.dateRangeLabel.isNotEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                ChipPill(
                    text = uiState.dateRangeLabel,
                    backgroundColor = historyColor.copy(alpha = 0.12f),
                    contentColor = historyColor,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HistoryMetricCard(Icons.Default.EmojiEvents, stringResource(R.string.stats_best_day), "${uiState.detailedStats.bestDaySteps}", AccentGold, Modifier.weight(1f))
            HistoryMetricCard(Icons.Default.TrendingUp, stringResource(R.string.stats_daily_avg), "${uiState.detailedStats.avgSteps}", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            HistoryMetricCard(Icons.Default.CheckCircle, stringResource(R.string.stats_goal_success), "${uiState.detailedStats.completionRate}%", PremiumMint, Modifier.weight(1f))
        }

        PillowCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = historyColor.copy(alpha = 0.10f),
            contentPadding = 18.dp,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.stats_general_performance),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    HistoryStatItem("👟", stringResource(R.string.stats_total_steps), "${uiState.detailedStats.totalSteps}")
                    HistoryStatItem("🔥", stringResource(R.string.stats_burned), "${uiState.detailedStats.totalCaloriesBurned} kcal")
                    HistoryStatItem("💧", stringResource(R.string.stats_water), "${uiState.detailedStats.totalWater} ml")
                }
            }
        }

        if (uiState.chartData.isNotEmpty()) {
            WeeklyChartCard(
                title = stringResource(R.string.stats_weekly_activity),
                color = historyColor,
                chartData = uiState.chartData,
                chartLabels = uiState.chartLabels,
                maxValue = uiState.stepGoal.coerceAtLeast(1),
                goalValue = uiState.stepGoal,
            )
        }
    }
}

@Composable
fun HistoryMetricCard(icon: ImageVector, label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    PillowCard(
        modifier = modifier,
        backgroundColor = color.copy(alpha = 0.12f),
        contentPadding = 12.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GradientIconBox(icon, color, size = 40.dp, iconSize = 20.dp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = color,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}

@Composable
fun HistoryStatItem(icon: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

