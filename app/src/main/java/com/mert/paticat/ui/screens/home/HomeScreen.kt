package com.mert.paticat.ui.screens.home

import androidx.compose.ui.platform.LocalContext

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mert.paticat.ui.components.*
import com.mert.paticat.ui.navigation.Screen
import com.mert.paticat.ui.theme.*
import com.mert.paticat.domain.model.Mission
import com.mert.paticat.utils.SoundManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.mert.paticat.R
import java.text.NumberFormat
import java.util.Locale

import androidx.compose.foundation.lazy.rememberLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isVisible: Boolean,
    onNavigate: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val mContext = LocalContext.current
    val particleSystem = rememberParticleSystem()
    var particleTarget by remember { mutableStateOf(Offset.Zero) }
    val haptic = LocalHapticFeedback.current
    val soundManager = remember { SoundManager(mContext) }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }
    
    val scrollState = rememberLazyListState()
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            scrollState.scrollToItem(0)
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        snackbarHost = { 
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 100.dp)
            ) 
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.app_name),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.home_subtitle),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    // Notification icon removed
                }
            )
        },
        containerColor = Color.Transparent, // Transparent to show animated background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp), // Screen padding
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                
                // 1. Cat Hero Card
                item {
                    val isSleeping = uiState.cat.isSleeping
                    GlassCatHeroCard(
                        catName = uiState.cat.name,
                        level = uiState.cat.level,
                        hunger = uiState.cat.hunger,
                        happiness = uiState.cat.happiness,
                        energy = uiState.cat.energy,
                        isSleeping = isSleeping,
                        onFeedClick = {
                            // Add hearts when feeding
                            particleSystem.emit(
                                x = particleTarget.x,
                                y = particleTarget.y - 100f,
                                count = 10,
                                type = ParticleType.HEART,
                                color = Color(0xFFFF4081)
                            )
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.feedCat()
                        },
                        onCatClick = {
                            onNavigate(com.mert.paticat.ui.navigation.Screen.Cat.route)
                        },
                        onBoxClick = {
                            onNavigate(com.mert.paticat.ui.navigation.Screen.Cat.route)
                        },
                        onPositioned = { coordinates ->
                            val bounds = coordinates.boundsInWindow()
                            particleTarget = Offset(bounds.center.x, bounds.center.y)
                        }
                    )
                }
                
                // 2. Summary Dashboard
                item {
                    SummaryDashboard(
                        steps = uiState.todayStats.steps,
                        stepGoal = uiState.stepGoal,
                        calories = uiState.todayStats.caloriesBurned,
                        calorieGoal = (uiState.stepGoal / 20).coerceAtLeast(200),
                        water = uiState.todayStats.waterMl,
                        waterGoal = uiState.waterGoal
                    )
                }
                
                // 3. Water Tracker
                item {
                    var waterAddTarget by remember { mutableStateOf(Offset.Zero) }
                    
                    WaterTrackingCard(
                        current = uiState.todayStats.waterMl,
                        goal = uiState.waterGoal,
                        onAdd = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            particleSystem.emit(
                                x = waterAddTarget.x,
                                y = waterAddTarget.y,
                                count = 20,
                                type = ParticleType.WATER_DROPLET,
                                color = Color(0xFF42A5F5)
                            )
                            viewModel.addWater(it)
                        },
                        canUndo = uiState.lastAddedWater != null,
                        onUndo = { viewModel.undoWater() },
                        onPositioned = { coordinates ->
                            val bounds = coordinates.boundsInWindow()
                            waterAddTarget = Offset(bounds.center.x, bounds.center.y)
                        }
                    )
                }
                
                // 3.5 Native Ad (Stationary)
                item {
                    // Uses pre-loaded ad from UI state to prevent flickering
                    NativeAdCard(nativeAd = uiState.nativeAd)
                }
                
                // 4. Mission Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SectionHeader(
                            title = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.home_daily_missions_title),
                            badgeText = "${uiState.todayMissions.count { it.isCompleted }}/${uiState.todayMissions.size}"
                        )
                        
                        if (uiState.todayMissions.isEmpty()) {
                            EmptyMissionState()
                        } else {
                            uiState.todayMissions.forEach { mission ->
                                GlassMissionItem(
                                    mission = mission,
                                    liveSteps = uiState.todayStats.steps,
                                    liveWater = uiState.todayStats.waterMl
                                )
                            }
                        }
                        
                        // Button to see all games/missions
                        val scope = rememberCoroutineScope()
                        Button(
                            onClick = {
                                if (uiState.cat.energy >= 5) {
                                    onNavigate(com.mert.paticat.ui.navigation.Screen.Games.route)
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(mContext.getString(com.mert.paticat.R.string.home_low_energy_snackbar))
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.home_more_missions_btn), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(110.dp)) } // Padding for bottom bar
            }
            
            // Draw particles on top of everything
            ParticleSystemCanvas(
                state = particleSystem,
                isVisible = isVisible,
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            )
        } // End of outer Box
    }
}

// --- NEW COMPONENTS ---

@Composable
fun GlassCatHeroCard(
    catName: String,
    level: Int,
    hunger: Int,
    happiness: Int,
    energy: Int,
    onFeedClick: () -> Unit,
    onCatClick: () -> Unit = {},
    onBoxClick: () -> Unit = {},
    isSleeping: Boolean = false,
    onPositioned: (androidx.compose.ui.layout.LayoutCoordinates) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBoxClick() }
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.05f))
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(32.dp))
            .padding(24.dp)
            .onGloballyPositioned(onPositioned)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Stats
            Column(
                modifier = Modifier.weight(1.5f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val levelTitleStr = androidx.compose.ui.res.stringResource(
                    com.mert.paticat.domain.model.Cat.getLevelTitleResId(level)
                )
                Text(
                    text = "$catName (Lvl $level - $levelTitleStr)",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
                
                // Status Bars
                StatusBarMini(icon = "🍖", value = hunger, color = PremiumPeach)
                StatusBarMini(icon = "⚡", value = energy, color = PremiumBlue)
                StatusBarMini(icon = "❤️", value = happiness, color = PremiumPink)
            }
            
            // Avatar Emoji
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Determine Cat Emoji based on mood
                val emojiRes = when {
                    isSleeping -> "😴"
                    hunger < 30 -> "😿"
                    energy < 30 -> if (happiness > 50) "😻" else "😿" // matches CatScreen tiredness visual logic loosely
                    happiness >= 80 -> "😻"
                    happiness < 40 -> "😿"
                    else -> "😸"
                }

                Text(
                    text = emojiRes,
                    fontSize = 72.sp,
                    modifier = Modifier
                        .pulsate()
                        .clickable(enabled = !isSleeping) { onCatClick() }
                )
            }
        }
        Icon(
            Icons.Default.ChevronRight, 
            contentDescription = null, 
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
fun StatusBarMini(icon: String, value: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 10.sp)
        Spacer(modifier = Modifier.width(4.dp))
        val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
            targetValue = value / 100f,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000)
        )
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.width(50.dp).height(4.dp).clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "%$value",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun GlassMissionItem(mission: Mission, liveSteps: Int = 0, liveWater: Int = 0) {
    val displayValue = when (mission.type) {
        com.mert.paticat.domain.model.MissionType.STEPS -> kotlin.math.max(mission.currentValue, liveSteps)
        com.mert.paticat.domain.model.MissionType.WATER -> kotlin.math.max(mission.currentValue, liveWater)
        else -> mission.currentValue
    }
    val isCompleted = displayValue >= mission.targetValue
    val itemAlpha = if (isCompleted) 0.6f else 1f
    
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Resolve Title
    var titleResId = getMissionStringId(mission.title)
    if (titleResId == 0) {
        titleResId = context.resources.getIdentifier(mission.title, "string", context.packageName)
    }
    val displayTitle = if (titleResId != 0) context.getString(titleResId) else mission.title
    
    // Resolve and Format Description
    var descResId = getMissionStringId(mission.description)
    if (descResId == 0) {
        descResId = context.resources.getIdentifier(mission.description, "string", context.packageName)
    }
    val displayDesc = if (descResId != 0) {
        try {
            // Format if it has %d placeholder
            context.getString(descResId, mission.targetValue)
        } catch (e: Exception) {
            context.getString(descResId)
        }
    } else mission.description

    Surface(
        modifier = Modifier.fillMaxWidth()
            .alpha(itemAlpha)
            .clickable { 
                 // Feedback for clickability since it looks like a card
                 val msg = if (isCompleted) 
                     context.getString(com.mert.paticat.R.string.mission_completed_feedback)
                 else 
                     context.getString(com.mert.paticat.R.string.mission_progress_feedback)
                 
                 // Note: we don't have a direct snackbar reference here, 
                 // but we can use Toast or just let the ripple show.
                 // Actually, better to just have the ripple to satisfy the 'looks clickable' feel
                 // without adding too much noise, OR add a callback.
                 // Let's just add the ripple for now.
            },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if(isCompleted) 0.2f else 0.4f),
        border = if(!isCompleted) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge
            val iconColor = when(mission.type) {
                com.mert.paticat.domain.model.MissionType.STEPS -> PremiumBlue
                com.mert.paticat.domain.model.MissionType.WATER -> PremiumBlue
                else -> PremiumPink
            }
            
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                         if(isCompleted) SuccessGreen.copy(alpha=0.1f) 
                         else iconColor.copy(alpha=0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                 if(isCompleted) {
                     Icon(Icons.Default.Check, null, tint = SuccessGreen)
                 } else {
                     val icon = when(mission.type) {
                         com.mert.paticat.domain.model.MissionType.STEPS -> Icons.Default.DirectionsWalk
                         com.mert.paticat.domain.model.MissionType.WATER -> Icons.Default.LocalDrink
                         else -> Icons.Default.Star
                     }
                     Icon(icon, null, tint = iconColor)
                 }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    displayTitle, 
                    fontWeight = FontWeight.Black, 
                    color = if(isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha=0.5f) else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if(isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
                
                Text(
                    displayDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                
                if(!isCompleted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (displayValue.toFloat() / mission.targetValue).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = iconColor,
                        trackColor = iconColor.copy(alpha = 0.1f),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "$displayValue / ${mission.targetValue}",
                         style = MaterialTheme.typography.labelSmall,
                         fontWeight = FontWeight.Bold,
                         color = iconColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Reward Badges
            Column(horizontalAlignment = Alignment.End) {
                if (!isCompleted) {
                    Surface(
                        color = AccentGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "+${mission.xpReward} XP",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = AccentGold
                        )
                    }
                    if (mission.foodPointReward > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = PremiumPink.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "+${mission.foodPointReward} 🍖",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = PremiumPink
                            )
                        }
                    }
                } else {
                    Icon(Icons.Default.DoneAll, null, tint = SuccessGreen.copy(alpha=0.5f))
                }
            }
        }
    }
}

@Composable
fun SummaryDashboard(steps: Int, stepGoal: Int, calories: Int, calorieGoal: Int, water: Int, waterGoal: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha=0.05f))
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
                label = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.stats_steps),
                value = NumberFormat.getNumberInstance(Locale.getDefault()).format(steps),
                progress = (steps.toFloat() / stepGoal).coerceIn(0f, 1f),
                color = MaterialTheme.colorScheme.primary,
                icon = Icons.Default.DirectionsWalk
            )
            
            // Calories
            DashboardStatItem(
                label = "kcal",
                value = "$calories",
                progress = (calories.toFloat() / calorieGoal).coerceIn(0f, 1f),
                color = MaterialTheme.colorScheme.secondary,
                icon = Icons.Default.LocalFireDepartment
            )
            
            // Water
            DashboardStatItem(
                label = "ml",
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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(64.dp),
                color = color.copy(alpha = 0.1f),
                strokeWidth = 6.dp,
                strokeCap = StrokeCap.Round
            )
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(64.dp),
                color = color,
                strokeWidth = 6.dp,
                strokeCap = StrokeCap.Round
            )
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(value, fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}



@Composable
fun WaterTrackingCard(
    current: Int,
    goal: Int,
    onAdd: (Int) -> Unit,
    canUndo: Boolean = false,
    onUndo: () -> Unit = {},
    onPositioned: (androidx.compose.ui.layout.LayoutCoordinates) -> Unit = {}
) {
    // Simple sleek design
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha=0.1f)),
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned(onPositioned)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.home_water_tracking_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (canUndo) {
                        FilledIconButton(
                            onClick = onUndo,
                            modifier = Modifier.size(32.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = PremiumBlue.copy(alpha = 0.15f),
                                contentColor = PremiumBlue
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.btn_undo),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        "${((current.toFloat()/goal)*100).toInt()}%", 
                        fontWeight = FontWeight.Black, 
                        color = PremiumBlue
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Spacer(modifier = Modifier.height(12.dp))
            
            // Water Wave Animation Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(CircleShape)
                    .background(PremiumBlue.copy(alpha = 0.1f))
            ) {
                 val targetProgress = (current.toFloat() / goal).coerceIn(0f, 1f)
                 val animatedProgress by animateFloatAsState(
                     targetValue = targetProgress,
                     animationSpec = tween(1000, easing = FastOutSlowInEasing),
                     label = "waterProgress"
                 )
                 
                 WaterWaveAnimation(
                     progress = animatedProgress, 
                     color = PremiumBlue
                 )
                 
                 // Text over wave
                 val percent = (animatedProgress * 100).toInt()
                 Text(
                     text = "${current}ml / ${goal}ml",
                     fontSize = 10.sp,
                     color = if(animatedProgress > 0.5f) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                     modifier = Modifier.align(Alignment.Center),
                     fontWeight = FontWeight.Bold
                 )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                listOf(200, 300, 500).forEach { amount ->
                    Surface(
                        shape = CircleShape,
                        color = PremiumBlue.copy(alpha = 0.1f),
                        modifier = Modifier
                            .bounceClick { onAdd(amount) }
                    ) {
                        Text(
                            "+$amount", 
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = PremiumBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, badgeText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Text(
                badgeText, 
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.primary, 
                fontWeight = FontWeight.Black, 
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun EmptyMissionState() {
     Surface(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.home_all_missions_completed), 
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WaterWaveAnimation(progress: Float, color: Color, isVisible: Boolean = true) {
    if (!isVisible) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            if (progress > 0) {
                drawRect(color = color, size = size.copy(width = size.width * progress))
            }
        }
        return
    }
    // Only run wave transition if needed (between 0 and 1, and visible)
    val shouldAnimate = isVisible && progress > 0f && progress < 1f
    
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveOffset by if (shouldAnimate) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "waveOffset"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val fillWidth = width * progress

        if (progress > 0) {
            val path = androidx.compose.ui.graphics.Path()
            val waveAmplitude = 4.dp.toPx()
            val waveFrequency = 1.5 * Math.PI / height // Frequency based on height for vertical wave

            path.moveTo(0f, 0f)
            path.lineTo(fillWidth, 0f)

            // Draw vertical wave at the front edge
            for (y in 0..height.toInt() step 5) {
                val relativeY = y.toFloat()
                val phase = waveOffset * 2 * Math.PI
                // x = baseFill + amplitude * sin(ky + phase)
                val x = fillWidth + waveAmplitude * kotlin.math.sin((waveFrequency * relativeY) + phase).toFloat()
                path.lineTo(x, relativeY)
            }

            path.lineTo(0f, height)
            path.close()

            drawPath(
                path = path,
                color = color
            )
            
            // Add a lighter second wave for depth
            val path2 = androidx.compose.ui.graphics.Path()
            path2.moveTo(0f, 0f)
            path2.lineTo(fillWidth - 4.dp.toPx(), 0f)
            for (y in 0..height.toInt() step 5) {
                val relativeY = y.toFloat()
                val phase = (waveOffset + 0.5f) * 2 * Math.PI
                val x = (fillWidth - 4.dp.toPx()) + (waveAmplitude * 0.8f) * kotlin.math.sin((waveFrequency * relativeY) + phase).toFloat()
                path2.lineTo(x, relativeY)
            }
            path2.lineTo(0f, height)
            path2.close()
            
            drawPath(
                path = path2,
                color = color.copy(alpha = 0.5f)
            )
        }
    }
}

private fun getMissionStringId(key: String): Int {
    return when(key) {
        "mission_steps_tier1_title" -> com.mert.paticat.R.string.mission_steps_tier1_title
        "mission_steps_tier1_desc" -> com.mert.paticat.R.string.mission_steps_tier1_desc
        "mission_steps_tier2_title" -> com.mert.paticat.R.string.mission_steps_tier2_title
        "mission_steps_tier2_desc" -> com.mert.paticat.R.string.mission_steps_tier2_desc
        "mission_steps_tier3_title" -> com.mert.paticat.R.string.mission_steps_tier3_title
        "mission_steps_tier3_desc" -> com.mert.paticat.R.string.mission_steps_tier3_desc
        "mission_steps_tier4_title" -> com.mert.paticat.R.string.mission_steps_tier4_title
        "mission_steps_tier4_desc" -> com.mert.paticat.R.string.mission_steps_tier4_desc
        else -> 0
    }
}


