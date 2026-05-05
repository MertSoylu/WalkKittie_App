package com.mert.paticat.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mert.paticat.R
import com.mert.paticat.ui.components.EmptyMissionState
import com.mert.paticat.ui.components.GlassCatHeroCard
import com.mert.paticat.ui.components.GlassMissionItem
import com.mert.paticat.ui.components.NativeAdCard
import com.mert.paticat.ui.components.ParticleSystemCanvas
import com.mert.paticat.ui.components.ParticleType
import com.mert.paticat.ui.components.SummaryDashboard
import com.mert.paticat.ui.components.WaterTrackingCard
import com.mert.paticat.ui.components.marshmallow.ActionPillButton
import com.mert.paticat.ui.components.marshmallow.ChipPill
import com.mert.paticat.ui.components.marshmallow.PillowCard
import com.mert.paticat.ui.components.marshmallow.SectionHeader2
import com.mert.paticat.ui.components.marshmallow.softEntrance
import com.mert.paticat.ui.components.rememberParticleSystem
import com.mert.paticat.ui.navigation.Screen
import com.mert.paticat.ui.theme.Dimensions
import com.mert.paticat.ui.theme.PremiumBlue
import com.mert.paticat.ui.theme.PremiumPeach
import com.mert.paticat.ui.theme.PremiumPink
import com.mert.paticat.utils.SoundManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isVisible: Boolean,
    onNavigate: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val mContext = LocalContext.current
    val particleSystem = rememberParticleSystem()
    var particleTarget by remember { mutableStateOf(Offset.Zero) }
    val haptic = LocalHapticFeedback.current
    val soundManager = remember { SoundManager(mContext) }

    DisposableEffect(Unit) {
        onDispose { soundManager.release() }
    }

    val scrollState = rememberLazyListState()

    LaunchedEffect(isVisible) {
        if (isVisible) scrollState.scrollToItem(0)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            // showSnackbar suspends until dismissed/timed-out, so clearError runs only after dismissal.
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(bottom = 100.dp))
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = stringResource(R.string.home_subtitle),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        containerColor = Color.Transparent,
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = Dimensions.homeScreenHorizontalPadding),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item(key = "top_spacer") { Spacer(modifier = Modifier.height(Dimensions.spaceXs)) }

                // Cat Hero
                item(key = "cat_hero_${uiState.cat.id}") {
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
                    Box(modifier = Modifier.softEntrance(delayMillis = 0)) {
                        GlassCatHeroCard(
                            catName = uiState.cat.name,
                            level = uiState.cat.level,
                            hunger = uiState.cat.hunger,
                            happiness = uiState.cat.happiness,
                            energy = uiState.cat.energy,
                            isSleeping = isSleeping,
                            onFeedClick = { onNavigate(Screen.Cat.route) },
                            onCatClick = { onNavigate(Screen.Cat.route) },
                            onBoxClick = { onNavigate(Screen.Cat.route) },
                            catImageRes = catImageRes,
                            onPositioned = { coordinates ->
                                val bounds = coordinates.boundsInWindow()
                                particleTarget = Offset(bounds.center.x, bounds.center.y)
                            },
                        )
                    }
                }

                // Cat Room CTA
                item(key = "cat_room_cta_${uiState.cat.id}") {
                    Box(modifier = Modifier.softEntrance(delayMillis = 60)) {
                        HomeCatRoomCta(
                            hunger = uiState.cat.hunger,
                            energy = uiState.cat.energy,
                            happiness = uiState.cat.happiness,
                            onClick = { onNavigate(Screen.Cat.route) },
                        )
                    }
                }

                // Streak + Distance Mini Cards
                item(key = "streak_distance_${uiState.cat.id}") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .softEntrance(delayMillis = 120),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        val animatedStreak by animateIntAsState(
                            targetValue = uiState.currentStreak,
                            animationSpec = tween(600, easing = FastOutSlowInEasing),
                            label = "streak",
                        )
                        StatMiniCard(
                            modifier = Modifier.weight(1f),
                            color = PremiumPeach,
                            icon = Icons.Default.LocalFireDepartment,
                            value = "$animatedStreak",
                            label = stringResource(R.string.streak_current),
                        )
                        val animatedDistance by animateFloatAsState(
                            targetValue = uiState.todayStats.distanceKm.toFloat(),
                            animationSpec = tween(800, easing = FastOutSlowInEasing),
                            label = "distance",
                        )
                        StatMiniCard(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primary,
                            icon = Icons.Default.LocationOn,
                            value = String.format("%.2f km", animatedDistance),
                            label = stringResource(R.string.stats_distance),
                        )
                    }
                }

                // Summary Dashboard
                item(key = "summary_dashboard_${uiState.cat.id}") {
                    Box(modifier = Modifier.softEntrance(delayMillis = 180)) {
                        SummaryDashboard(
                            steps = uiState.todayStats.steps,
                            stepGoal = uiState.stepGoal,
                            calories = uiState.todayStats.caloriesBurned,
                            calorieGoal = (uiState.stepGoal / 20).coerceAtLeast(200),
                            water = uiState.todayStats.waterMl,
                            waterGoal = uiState.waterGoal,
                        )
                    }
                }

                // Water Tracker
                item(key = "water_intake_${uiState.cat.id}") {
                    var waterAddTarget by remember { mutableStateOf(Offset.Zero) }
                    Box(modifier = Modifier.softEntrance(delayMillis = 240)) {
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
                                    color = PremiumBlue,
                                )
                                viewModel.addWater(it)
                            },
                            canUndo = uiState.lastAddedWater != null,
                            onUndo = { viewModel.undoWater() },
                            onPositioned = { coordinates ->
                                val bounds = coordinates.boundsInWindow()
                                waterAddTarget = Offset(bounds.center.x, bounds.center.y)
                            },
                        )
                    }
                }

                // Native Ad
                item(key = "native_ad") {
                    Box(modifier = Modifier.softEntrance(delayMillis = 280)) {
                        NativeAdCard(nativeAd = uiState.nativeAd)
                    }
                }

                // Mission Section header
                item(key = "missions_header_${uiState.cat.id}") {
                    Box(modifier = Modifier.softEntrance(delayMillis = 320)) {
                        SectionHeader2(
                            title = stringResource(R.string.home_daily_missions_title),
                            leadingEmoji = "🎯",
                            trailing = {
                                ChipPill(
                                    text = "${uiState.todayMissions.count { it.isCompleted }}/${uiState.todayMissions.size}",
                                    backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                    contentColor = MaterialTheme.colorScheme.primary,
                                )
                            },
                        )
                    }
                }

                if (uiState.todayMissions.isEmpty()) {
                    item(key = "missions_empty", contentType = "empty") {
                        EmptyMissionState()
                    }
                } else {
                    items(
                        items = uiState.todayMissions,
                        key = { it.id },
                        contentType = { "mission" },
                    ) { mission ->
                        GlassMissionItem(
                            mission = mission,
                            liveSteps = uiState.todayStats.steps,
                            liveWater = uiState.todayStats.waterMl,
                            onMissionClick = viewModel::onMissionTap,
                        )
                    }
                }

                item(key = "missions_more_btn") {
                    ActionPillButton(
                        text = stringResource(R.string.home_more_missions_btn),
                        onClick = {
                            if (viewModel.onMoreMissionsClick()) {
                                onNavigate(Screen.Games.route)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingEmoji = "🎮",
                    )
                }

                item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(Dimensions.bottomNavClearance)) }
            }

            ParticleSystemCanvas(
                state = particleSystem,
                isVisible = isVisible,
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )

            AnimatedVisibility(
                visible = uiState.isLoading,
                enter = fadeIn(),
                exit = fadeOut(animationSpec = tween(400)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Dimensions.spaceMedium),
                    ) {
                        val loadingCatCd = stringResource(R.string.loading_cat_icon)
                        Text(
                            text = "🐱",
                            fontSize = 64.sp,
                            modifier = Modifier.semantics { contentDescription = loadingCatCd },
                        )
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMiniCard(
    modifier: Modifier = Modifier,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
) {
    PillowCard(
        modifier = modifier,
        contentPadding = 16.dp,
        backgroundColor = color.copy(alpha = 0.10f),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
            }
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = color,
            )
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
private fun HomeCatRoomCta(
    hunger: Int,
    energy: Int,
    happiness: Int,
    onClick: () -> Unit,
) {
    val accent = when {
        hunger < 30 -> PremiumPeach
        energy < 30 -> PremiumBlue
        happiness < 45 -> PremiumPink
        else -> MaterialTheme.colorScheme.primary
    }

    PillowCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = accent.copy(alpha = 0.10f),
        contentPadding = 16.dp,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            val pawCd = stringResource(R.string.icon_paw)
            Text(
                text = "🐾",
                fontSize = 28.sp,
                modifier = Modifier.semantics { contentDescription = pawCd },
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.cat_room_hint),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.cat_room_cta),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                )
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = stringResource(R.string.icon_play_cta),
                tint = accent,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
