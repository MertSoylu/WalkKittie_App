package com.mert.paticat.ui.screens.cat

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mert.paticat.R
import com.mert.paticat.domain.model.Cat
import com.mert.paticat.domain.model.ShopItem
import com.mert.paticat.ui.components.*
import com.mert.paticat.ui.theme.*
import com.mert.paticat.utils.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatScreen(
    onNavigate: (String) -> Unit,
    viewModel: CatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val soundManager = remember { SoundManager(context) }
    var lastClickTime by remember { mutableStateOf(0L) }
    var catClicks by remember { mutableStateOf(0) }
    val particleSystem = rememberParticleSystem()
    var catCenter by remember { mutableStateOf(Offset.Zero) }

    // Heart animation
    val petResult by viewModel.petResult.collectAsStateWithLifecycle()
    var showHeart by remember { mutableStateOf(false) }
    var heartTrigger by remember { mutableStateOf(0) }
    LaunchedEffect(petResult) {
        if (petResult == true) {
            showHeart = true
            heartTrigger++
        }
    }
    LaunchedEffect(heartTrigger) {
        if (heartTrigger > 0) {
            delay(800)
            showHeart = false
        }
    }

    // Sleep ticker
    val isSleeping = viewModel.isCatSleeping()
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(isSleeping) {
        if (isSleeping) { while (true) { delay(1000); tick++ } }
    }

    DisposableEffect(Unit) { onDispose { soundManager.release() } }

    // Tab state
    var selectedTab by remember { mutableIntStateOf(0) }

    // Ad preloading
    LaunchedEffect(uiState.isNetworkAvailable, uiState.foodAdState, uiState.dailyGoldAdsRemaining) {
        if (uiState.dailyGoldAdsRemaining > 0 && uiState.isNetworkAvailable && uiState.foodAdState is AdState.Idle) {
            viewModel.loadFoodAd()
        }
    }
    LaunchedEffect(uiState.cat.energy, isSleeping, uiState.isNetworkAvailable, uiState.sleepAdState) {
        if ((isSleeping || uiState.cat.energy < 30) && uiState.isNetworkAvailable && uiState.sleepAdState is AdState.Idle) {
            viewModel.loadSleepAd()
        }
    }

    // Gold Tutorial Dialog
    if (uiState.showGoldTutorial) {
        GoldTutorialDialog(onDismiss = { viewModel.dismissGoldTutorial() })
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
                            uiState.cat.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        // Level badge as subtitle
                        Text(
                            stringResource(
                                R.string.cat_level_badge,
                                uiState.cat.level,
                                stringResource(Cat.getLevelTitleResId(uiState.cat.level))
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                actions = {
                    // Gold Ad Button
                    val foodAdState = uiState.foodAdState
                    val adsRemaining = uiState.dailyGoldAdsRemaining
                    val adReady = foodAdState is AdState.Loaded && uiState.isNetworkAvailable && adsRemaining > 0

                    if (adsRemaining > 0) {
                        Surface(
                            color = if (adReady) PremiumMint else PremiumMint.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .then(
                                    if (adReady) Modifier.bounceClick {
                                        (context as? Activity)?.let { viewModel.showFoodAd(it) }
                                    } else Modifier
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when (foodAdState) {
                                    AdState.Loading -> CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    AdState.Error -> Text("⚠️", fontSize = 11.sp)
                                    else -> {
                                        Text("📺", fontSize = 11.sp)
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            "+${ShopItem.GOLD_PER_AD} 🪙 ($adsRemaining)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Gold Coin Display
                    val animatedCoins by animateIntAsState(targetValue = uiState.cat.coins, label = "coins")
                    Surface(
                        color = AccentGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🪙", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                animatedCoins.toString(),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = AccentGold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ==================== CAT VISUAL AREA ====================
                Box(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Radial gradient background
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    val catSleeping = viewModel.isCatSleeping()
                    val currentImageRes = getCatImageResource(uiState.cat, catSleeping)

                    Crossfade(
                        targetState = currentImageRes,
                        animationSpec = tween(600),
                        label = "catMood"
                    ) { resId ->
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = null,
                            modifier = Modifier
                                .size(260.dp)
                                .onGloballyPositioned { coords ->
                                    val bounds = coords.boundsInWindow()
                                    catCenter = Offset(bounds.center.x, bounds.center.y)
                                }
                                .clickable(enabled = !catSleeping) {
                                    val now = System.currentTimeMillis()
                                    if (now - lastClickTime < 500) catClicks++ else catClicks = 1
                                    lastClickTime = now
                                    if (catClicks > 3) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        catClicks = 0
                                    } else {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        soundManager.playPurr()
                                    }
                                    viewModel.petCat()
                                },
                            contentScale = ContentScale.Fit
                        )
                    }

                    // Floating Heart
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showHeart,
                        enter = fadeIn(tween(200)),
                        exit = fadeOut(tween(600)) + slideOutVertically(tween(600)) { -it },
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        Text("❤️", fontSize = 48.sp, modifier = Modifier.padding(bottom = 16.dp))
                    }
                }

                // Mood Text
                Text(
                    "${uiState.moodEmoji} ${stringResource(uiState.moodTextResId)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ==================== CAT STATS (Circular) ====================
                EntranceAnimation {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                                    )
                                )
                            )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val animatedHunger by animateIntAsState(targetValue = uiState.cat.hunger, label = "hunger")
                                val animatedEnergy by animateIntAsState(targetValue = uiState.cat.energy, label = "energy")
                                val animatedHappy by animateIntAsState(targetValue = uiState.cat.happiness, label = "happiness")

                                CircularStatItem(
                                    emoji = "🍖",
                                    label = stringResource(R.string.cat_stat_hunger),
                                    value = animatedHunger,
                                    color = PremiumPeach
                                )
                                CircularStatItem(
                                    emoji = "⚡",
                                    label = stringResource(R.string.cat_stat_energy),
                                    value = animatedEnergy,
                                    color = PremiumBlue
                                )
                                CircularStatItem(
                                    emoji = "💖",
                                    label = stringResource(R.string.cat_stat_happiness),
                                    value = animatedHappy,
                                    color = PremiumPink
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // XP row — label + gradient bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("⭐", fontSize = 14.sp)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(AccentGold.copy(alpha = 0.15f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(uiState.cat.levelProgress)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(AccentGold, AccentGold.copy(alpha = 0.65f))
                                                )
                                            )
                                    )
                                }
                                Text(
                                    stringResource(
                                        R.string.cat_xp_label,
                                        uiState.cat.xp.toInt(),
                                        uiState.cat.xpForNextLevel.toInt()
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            // XP remaining micro-hint
                            Spacer(modifier = Modifier.height(6.dp))
                            val isMaxLevel = uiState.cat.level >= 50
                            val xpRemaining = (uiState.cat.xpForNextLevel - uiState.cat.xp).coerceAtLeast(0).toInt()
                            val animatedXpRemaining by animateIntAsState(
                                targetValue = xpRemaining,
                                animationSpec = tween(800),
                                label = "xpRemaining"
                            )
                            Text(
                                text = if (isMaxLevel)
                                    "⭐ ${stringResource(R.string.cat_xp_max_level)}"
                                else
                                    "✨ ${stringResource(R.string.cat_xp_remaining, animatedXpRemaining)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = AccentGold.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ==================== TAB SELECTOR ====================
                EntranceAnimation(delay = 100) {
                    PremiumTabSelector(
                        options = listOf(
                            stringResource(R.string.cat_tab_my_cat),
                            stringResource(R.string.cat_tab_shop)
                        ),
                        selectedIndex = selectedTab,
                        onSelect = { selectedTab = it }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ==================== TAB CONTENT ====================
                AnimatedVisibility(visible = selectedTab == 0) {
                    MyCatTabContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        tick = tick,
                        context = context,
                        haptic = haptic,
                        onNavigate = onNavigate
                    )
                }

                AnimatedVisibility(visible = selectedTab == 1) {
                    ShopTabContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        particleSystem = particleSystem,
                        catCenter = catCenter,
                        haptic = haptic
                    )
                }

                Spacer(modifier = Modifier.height(110.dp))
            }

            // Particle overlay
            ParticleSystemCanvas(
                state = particleSystem,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// ==================== CIRCULAR STAT ITEM ====================

@Composable
private fun CircularStatItem(
    emoji: String,
    label: String,
    value: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularStatIndicator(
            progress = value / 100f,
            color = color,
            size = 68.dp,
            strokeWidth = 6.dp
        ) {
            Text(emoji, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            stringResource(R.string.unit_percentage, value),
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==================== MY CAT TAB ====================

@Composable
private fun MyCatTabContent(
    uiState: CatUiState,
    viewModel: CatViewModel,
    tick: Int,
    context: android.content.Context,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    onNavigate: (String) -> Unit
) {
    val isCatSleeping = viewModel.isCatSleeping()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (isCatSleeping) {
            // ===== SLEEPING STATE =====
            SleepingStateCard(uiState, viewModel, tick, context)
        } else {
            // ===== PLAY BUTTON CARD =====
            val minEnergy = GameType.values().minOf { it.energyCost }
            val canPlay = uiState.cat.energy >= minEnergy
            ActionCard(
                emoji = "🎮",
                title = stringResource(R.string.cat_action_play),
                subtitle = if (canPlay) stringResource(R.string.cat_play_room)
                           else stringResource(R.string.cat_play_room_disabled),
                accentColor = MaterialTheme.colorScheme.primary,
                enabled = canPlay,
                onClick = {
                    if (canPlay) {
                        onNavigate(com.mert.paticat.ui.navigation.Screen.Games.route)
                    } else {
                        viewModel.setMessage(context.getString(R.string.home_low_energy_snackbar))
                    }
                }
            )

            // ===== SLEEP BUTTON CARD =====
            val sleepEnabled = uiState.cat.energy < 40
            ActionCard(
                emoji = "💤",
                title = stringResource(R.string.cat_action_sleep),
                subtitle = if (sleepEnabled)
                    stringResource(R.string.cat_full_energy)
                else
                    stringResource(R.string.cat_not_tired_info, uiState.cat.energy),
                accentColor = PremiumBlue,
                enabled = sleepEnabled,
                onClick = {
                    haptic.performHapticFeedback(
                        if (sleepEnabled) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
                    )
                    viewModel.sleepCat()
                }
            )
        }
    }
}

// ==================== ACTION CARD (PLAY / SLEEP) ====================

@Composable
private fun ActionCard(
    emoji: String,
    title: String,
    subtitle: String,
    accentColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val displayAlpha = if (enabled) 1f else 0.5f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        accentColor.copy(alpha = if (enabled) 0.15f else 0.05f),
                        accentColor.copy(alpha = if (enabled) 0.04f else 0.01f)
                    )
                )
            )
            .bounceClick { if (enabled) onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gradient emoji circle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                accentColor.copy(alpha = if (enabled) 0.30f else 0.10f),
                                accentColor.copy(alpha = if (enabled) 0.10f else 0.03f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    emoji,
                    fontSize = 28.sp,
                    color = if (enabled) Color.Unspecified else Color.White.copy(alpha = 0.4f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            // Arrow indicator
            Text(
                "›",
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = accentColor.copy(alpha = displayAlpha)
            )
        }
    }
}

// ==================== SLEEPING STATE CARD ====================

@Composable
private fun SleepingStateCard(
    uiState: CatUiState,
    viewModel: CatViewModel,
    tick: Int,
    context: android.content.Context
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Sleep countdown card — soft blue gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Moon icon in gradient circle
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.30f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💤", fontSize = 26.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    stringResource(R.string.cat_sleeping_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                val remainingTime = remember(tick) { viewModel.getSleepRemainingTime() }
                Text(
                    remainingTime,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Watch ad button — gradient pill
        val sleepAdState = uiState.sleepAdState
        val adReady = sleepAdState is AdState.Loaded && uiState.isNetworkAvailable

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (adReady)
                        Brush.horizontalGradient(listOf(PremiumPeach, PremiumPeach.copy(alpha = 0.75f)))
                    else
                        Brush.horizontalGradient(listOf(PremiumPeach.copy(alpha = 0.3f), PremiumPeach.copy(alpha = 0.15f)))
                )
                .then(
                    if (adReady) Modifier.bounceClick {
                        (context as? Activity)?.let { viewModel.showSleepAd(it) }
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("📺", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    stringResource(R.string.cat_ad_reduce_sleep),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (adReady) Color.White else Color.White.copy(alpha = 0.5f)
                )
            }
        }

        // Status text
        val statusText = when {
            !uiState.isNetworkAvailable -> stringResource(R.string.cat_no_internet)
            sleepAdState is AdState.Loading -> stringResource(R.string.cat_ad_loading)
            sleepAdState is AdState.Error -> stringResource(R.string.cat_ad_error)
            else -> null
        }
        if (statusText != null && !adReady) {
            Text(
                statusText,
                style = MaterialTheme.typography.labelSmall,
                color = if (!uiState.isNetworkAvailable || sleepAdState is AdState.Error)
                    MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ==================== SHOP TAB ====================

@Composable
private fun ShopTabContent(
    uiState: CatUiState,
    viewModel: CatViewModel,
    particleSystem: com.mert.paticat.ui.components.ParticleSystemState,
    catCenter: Offset,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    val isSleeping = viewModel.isCatSleeping()
    val ownedItems = uiState.inventory.filter { it.value > 0 }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // ===== INVENTORY =====
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) { Text("🎒", fontSize = 15.sp) }
            Text(
                stringResource(R.string.inventory_section_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (ownedItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Text(
                    stringResource(R.string.inventory_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(20.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ownedItems.toList()) { (item, qty) ->
                    InventoryChipCard(
                        item = item,
                        quantity = qty,
                        isSleeping = isSleeping,
                        canFeed = !isSleeping && uiState.cat.hunger < 95,
                        onFeed = {
                            particleSystem.emit(
                                x = catCenter.x,
                                y = catCenter.y - 100f,
                                count = 15,
                                type = ParticleType.HEART,
                                color = PremiumPink
                            )
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.feedCatWithItem(item)
                        }
                    )
                }
            }
        }

        // ===== SHOP =====
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                AccentGold.copy(alpha = 0.35f),
                                AccentGold.copy(alpha = 0.10f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) { Text("🛒", fontSize = 15.sp) }
            Text(
                stringResource(R.string.shop_section_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Gold hint when broke
        if (uiState.cat.coins < ShopItem.ALL.minOf { it.price }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AccentGold.copy(alpha = 0.08f))
            ) {
                Text(
                    stringResource(R.string.cat_earn_gold_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentGold,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        ShopItem.ALL.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) {
                        ShopItemCard(
                            item = item,
                            gold = uiState.cat.coins,
                            ownedQty = uiState.inventory[item] ?: 0,
                            onBuy = { viewModel.buyFood(item) }
                        )
                    }
                }
                if (rowItems.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ==================== INVENTORY CHIP CARD ====================

@Composable
private fun InventoryChipCard(
    item: ShopItem,
    quantity: Int,
    isSleeping: Boolean,
    canFeed: Boolean,
    onFeed: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        primary.copy(alpha = 0.12f),
                        primary.copy(alpha = 0.03f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Emoji in soft circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.emoji, fontSize = 26.sp)
            }
            Text(
                stringResource(item.nameResId),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "x$quantity",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = primary
            )
            if (isSleeping && quantity > 0) {
                Text(
                    stringResource(R.string.feed_disabled_sleeping),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
            // Gradient feed button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (canFeed && quantity > 0)
                            Brush.horizontalGradient(listOf(primary, primary.copy(alpha = 0.7f)))
                        else
                            Brush.horizontalGradient(listOf(primary.copy(alpha = 0.3f), primary.copy(alpha = 0.15f)))
                    )
                    .then(if (canFeed && quantity > 0) Modifier.clickable { onFeed() } else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.feed_button),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (canFeed && quantity > 0) Color.White else Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// ==================== SHOP ITEM CARD ====================

@Composable
private fun ShopItemCard(
    item: ShopItem,
    gold: Int,
    ownedQty: Int,
    onBuy: () -> Unit
) {
    val canAfford = gold >= item.price
    val inventoryFull = ownedQty >= ShopItem.MAX_INVENTORY_PER_ITEM
    val canBuy = canAfford && !inventoryFull

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    if (canBuy)
                        listOf(AccentGold.copy(alpha = 0.14f), AccentGold.copy(alpha = 0.03f))
                    else
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                )
            )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Emoji in soft gold circle
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (canBuy) AccentGold.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(item.emoji, fontSize = 28.sp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                stringResource(item.nameResId),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                "+${item.hungerRestore} 🍖 +${item.happinessBoost} 😊",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (inventoryFull) {
                Text(
                    stringResource(R.string.shop_inventory_full_label, ownedQty, ShopItem.MAX_INVENTORY_PER_ITEM),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            } else {
                // Gradient buy button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (canAfford)
                                Brush.horizontalGradient(listOf(AccentGold, AccentGold.copy(alpha = 0.70f)))
                            else
                                Brush.horizontalGradient(listOf(AccentGold.copy(alpha = 0.30f), AccentGold.copy(alpha = 0.15f)))
                        )
                        .then(if (canAfford) Modifier.clickable { onBuy() } else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${item.price} 🪙",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (canAfford) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// ==================== CAT IMAGE RESOLVER ====================

@Composable
fun getCatImageResource(cat: Cat, isSleeping: Boolean): Int {
    if (isSleeping) return R.drawable.cat_sleep
    if (cat.hunger < 25) return R.drawable.cat_hungry
    if (cat.energy < 25) {
        return if (cat.happiness >= 45) R.drawable.cat_tired_happy else R.drawable.cat_tired_sad
    }
    if (cat.happiness >= 80) return R.drawable.cat_excited
    if (cat.happiness >= 45) return R.drawable.cat_happy
    return R.drawable.cat_sad
}

// ==================== LEGACY COMPOSABLES (kept for GamesScreen import compatibility) ====================

@Composable
fun GameChoiceBtn(emoji: String, choice: RockPaperScissors, onClick: (RockPaperScissors) -> Unit) {
    Surface(
        modifier = Modifier
            .size(64.dp)
            .bounceClick { onClick(choice) },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 28.sp)
        }
    }
}

// ==================== GOLD TUTORIAL DIALOG ====================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GoldTutorialDialog(onDismiss: () -> Unit) {
    val pages = listOf(
        Triple(stringResource(R.string.gold_tutorial_title_1), stringResource(R.string.gold_tutorial_desc_1), "🪙"),
        Triple(stringResource(R.string.gold_tutorial_title_2), stringResource(R.string.gold_tutorial_desc_2), "📺"),
        Triple(stringResource(R.string.gold_tutorial_title_3), stringResource(R.string.gold_tutorial_desc_3), "🛒")
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Bonus badge on first page
                AnimatedVisibility(visible = pagerState.currentPage == 0) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = AccentGold.copy(alpha = 0.15f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.gold_tutorial_bonus),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold
                        )
                    }
                }

                // Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    when (page) {
                                        0 -> AccentGold.copy(alpha = 0.12f)
                                        1 -> PremiumMint.copy(alpha = 0.12f)
                                        else -> PremiumBlue.copy(alpha = 0.12f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(pages[page].third, fontSize = 52.sp)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = pages[page].first,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = pages[page].second,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Pager dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(pages.size) { iteration ->
                        val isSelected = pagerState.currentPage == iteration
                        val color = if (isSelected) AccentGold else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        val width by animateDpAsState(if (isSelected) 24.dp else 8.dp, label = "dot")
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(color)
                                .size(width = width, height = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Next / Done button
                val isLastPage = pagerState.currentPage == pages.size - 1
                Button(
                    onClick = {
                        if (isLastPage) {
                            onDismiss()
                        } else {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                ) {
                    Text(
                        text = if (isLastPage) stringResource(R.string.gold_tutorial_btn_done)
                               else stringResource(R.string.gold_tutorial_btn_next),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
