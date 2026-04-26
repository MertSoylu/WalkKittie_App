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

    val scope = rememberCoroutineScope()
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
                    val catImageRes = when {
                        isSleeping -> R.drawable.cat_sleep
                        uiState.cat.hunger < 25 -> R.drawable.cat_hungry
                        uiState.cat.energy < 25 && uiState.cat.happiness >= 45 -> R.drawable.cat_tired_happy
                        uiState.cat.energy < 25 -> R.drawable.cat_tired_sad
                        uiState.cat.happiness >= 80 -> R.drawable.cat_excited
                        uiState.cat.happiness >= 45 -> R.drawable.cat_happy
                        else -> R.drawable.cat_sad
                    }
                    GlassCatHeroCard(
                        catName = uiState.cat.name,
                        level = uiState.cat.level,
                        hunger = uiState.cat.hunger,
                        happiness = uiState.cat.happiness,
                        energy = uiState.cat.energy,
                        isSleeping = isSleeping,
                        onFeedClick = {
                            // Navigate to Cat screen for feeding via shop
                            onNavigate(com.mert.paticat.ui.navigation.Screen.Cat.route)
                        },
                        onCatClick = {
                            onNavigate(com.mert.paticat.ui.navigation.Screen.Cat.route)
                        },
                        onBoxClick = {
                            onNavigate(com.mert.paticat.ui.navigation.Screen.Cat.route)
                        },
                        catImageRes = catImageRes,
                        onPositioned = { coordinates ->
                            val bounds = coordinates.boundsInWindow()
                            particleTarget = Offset(bounds.center.x, bounds.center.y)
                        }
                    )
                }

                // 1.5. Streak + Distance Mini Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Streak Card
                        EntranceAnimation(delay = 60) {
                            val animatedStreak by animateIntAsState(
                                targetValue = uiState.currentStreak,
                                animationSpec = tween(600, easing = FastOutSlowInEasing),
                                label = "streak_anim"
                            )
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = PremiumPeach.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(PremiumPeach.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = PremiumPeach,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Text(
                                        "$animatedStreak",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 26.sp,
                                        color = PremiumPeach
                                    )
                                    Text(
                                        androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.streak_current),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        // Distance Card
                        EntranceAnimation(delay = 120) {
                            val animatedDistance by animateFloatAsState(
                                targetValue = uiState.todayStats.distanceKm.toFloat(),
                                animationSpec = tween(800, easing = FastOutSlowInEasing),
                                label = "distance_anim"
                            )
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Text(
                                        String.format("%.2f km", animatedDistance),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 26.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.stats_distance),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
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
                                    liveWater = uiState.todayStats.waterMl,
                                    onMissionClick = { clickedMission ->
                                        val isCompleted = when (clickedMission.type) {
                                            com.mert.paticat.domain.model.MissionType.STEPS -> kotlin.math.max(clickedMission.currentValue, uiState.todayStats.steps) >= clickedMission.targetValue
                                            com.mert.paticat.domain.model.MissionType.WATER -> kotlin.math.max(clickedMission.currentValue, uiState.todayStats.waterMl) >= clickedMission.targetValue
                                            com.mert.paticat.domain.model.MissionType.GAME -> clickedMission.currentValue >= clickedMission.targetValue
                                            else -> clickedMission.isCompleted
                                        }
                                        val msg = if (isCompleted)
                                            mContext.getString(com.mert.paticat.R.string.mission_completed_feedback)
                                        else
                                            mContext.getString(com.mert.paticat.R.string.mission_progress_feedback)
                                        scope.launch { snackbarHostState.showSnackbar(msg) }
                                    }
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
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.home_more_missions_btn),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
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

            // Loading state overlay — fades out once data is ready
            AnimatedVisibility(
                visible = uiState.isLoading,
                enter = fadeIn(),
                exit = fadeOut(animationSpec = tween(400))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("🐱", fontSize = 64.sp)
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                }
            }
        } // End of outer Box
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
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = PremiumBlue.copy(alpha = 0.08f),
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned(onPositioned)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.home_water_tracking_title),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (canUndo) {
                        FilledIconButton(
                            onClick = onUndo,
                            modifier = Modifier.size(44.dp),
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
                        "${((current.toFloat() / goal) * 100).toInt()}%",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = PremiumBlueDark
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Water Wave Animation Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(CircleShape)
                    .background(PremiumBlue.copy(alpha = 0.12f))
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
                Text(
                    text = "${current}ml / ${goal}ml",
                    fontSize = 11.sp,
                    color = if (animatedProgress > 0.3f) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                listOf(200, 300, 500).forEach { amount ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PremiumBlue.copy(alpha = 0.15f),
                        modifier = Modifier
                            .heightIn(min = Dimensions.touchTargetMin)
                            .bounceClick { onAdd(amount) }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "+$amount",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = PremiumBlueDark
                            )
                            Text(
                                androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.unit_ml),
                                fontSize = 11.sp,
                                color = PremiumBlue.copy(alpha = 0.7f)
                            )
                        }
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
        Text(
            title,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            letterSpacing = (-0.3).sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ) {
            Text(
                badgeText,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun EmptyMissionState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.mission_completed),
            tint = SuccessGreen,
            modifier = Modifier.size(48.dp)
        )
        Text(
            androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.home_all_missions_completed),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
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
