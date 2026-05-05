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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mert.paticat.R
import com.mert.paticat.domain.model.Cat
import com.mert.paticat.domain.model.ShopCategory
import com.mert.paticat.domain.model.ShopItem
import com.mert.paticat.ui.components.CircularStatIndicator
import com.mert.paticat.ui.components.ParticleSystemCanvas
import com.mert.paticat.ui.components.ParticleSystemState
import com.mert.paticat.ui.components.ParticleType
import com.mert.paticat.ui.components.rememberMotionEnabled
import com.mert.paticat.ui.components.rememberParticleSystem
import com.mert.paticat.ui.components.marshmallow.ActionPillButton
import com.mert.paticat.ui.components.marshmallow.ChipPill
import com.mert.paticat.ui.components.marshmallow.MarshmallowDialog
import com.mert.paticat.ui.components.marshmallow.MarshmallowTabs
import com.mert.paticat.ui.components.marshmallow.PillowCard
import com.mert.paticat.ui.components.marshmallow.SectionHeader2
import com.mert.paticat.ui.components.marshmallow.SoftSheet
import com.mert.paticat.ui.components.marshmallow.TintedPillowCard
import com.mert.paticat.ui.components.marshmallow.breath
import com.mert.paticat.ui.components.marshmallow.pillowPress
import com.mert.paticat.ui.components.marshmallow.softEntrance
import com.mert.paticat.ui.theme.AccentGold
import com.mert.paticat.ui.theme.Dimensions
import com.mert.paticat.ui.theme.MoodHappyGradient
import com.mert.paticat.ui.theme.MoodHungryGradient
import com.mert.paticat.ui.theme.MoodNeutralGradient
import com.mert.paticat.ui.theme.MoodSadGradient
import com.mert.paticat.ui.theme.MoodSleepGradient
import com.mert.paticat.ui.theme.MoodTiredGradient
import com.mert.paticat.ui.theme.PremiumBlue
import com.mert.paticat.ui.theme.PremiumMint
import com.mert.paticat.ui.theme.PremiumPeach
import com.mert.paticat.ui.theme.PremiumPink
import com.mert.paticat.utils.SoundManager
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, FlowPreview::class)
@Composable
fun CatScreen(
    onNavigate: (String) -> Unit,
    viewModel: CatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val motionEnabled = rememberMotionEnabled()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val soundManager = remember { SoundManager(context) }
    val particleSystem = rememberParticleSystem()
    var catCenter by remember { mutableStateOf(Offset.Zero) }
    var lastClickTime by remember { mutableLongStateOf(0L) }
    var catClicks by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    val petResult by viewModel.petResult.collectAsStateWithLifecycle()
    var showHeart by remember { mutableStateOf(false) }
    var heartTrigger by remember { mutableIntStateOf(0) }
    LaunchedEffect(petResult) {
        if (petResult == true) { showHeart = true; heartTrigger++ }
    }
    LaunchedEffect(heartTrigger) {
        if (heartTrigger > 0) { delay(1000); showHeart = false }
    }

    val isSleeping = viewModel.isCatSleeping()
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(isSleeping) {
        while (isSleeping) { delay(1000); tick++ }
    }

    DisposableEffect(Unit) { onDispose { soundManager.release() } }

    var showShop by remember { mutableStateOf(false) }
    var showBoosterDialog by remember { mutableStateOf(false) }
    var boosterRefreshTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(showBoosterDialog) {
        while (showBoosterDialog) { delay(1000); boosterRefreshTick++ }
    }

    // Debounced 500ms — prevents ad reload thrash on network state flicker.
    LaunchedEffect(Unit) {
        snapshotFlow {
            Triple(
                uiState.isNetworkAvailable,
                uiState.dailyGoldAdsRemaining,
                uiState.foodAdState is AdState.Idle
            )
        }
            .debounce(500L)
            .distinctUntilChanged()
            .collectLatest { (online, remaining, idle) ->
                if (online && remaining > 0 && idle) viewModel.loadFoodAd()
            }
    }
    // Debounced 500ms — prevents sleep ad reload thrash on network/sleep flicker.
    LaunchedEffect(Unit) {
        snapshotFlow {
            listOf(
                isSleeping,
                uiState.isNetworkAvailable,
                uiState.sleepAdState is AdState.Idle,
                uiState.sleepAdsRemaining
            )
        }
            .debounce(500L)
            .distinctUntilChanged()
            .collectLatest {
                if (isSleeping &&
                    uiState.sleepAdsRemaining > 0 &&
                    uiState.isNetworkAvailable &&
                    uiState.sleepAdState is AdState.Idle
                ) {
                    viewModel.loadSleepAd()
                }
            }
    }

    // Surface ad failures via snackbar.
    LaunchedEffect(uiState.adError) {
        uiState.adError?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearAdError()
        }
    }

    if (uiState.showGoldTutorial) {
        GoldTutorialDialog(onDismiss = { viewModel.dismissGoldTutorial() })
    }

    if (showBoosterDialog) {
        BoosterDialog(viewModel = viewModel, tick = boosterRefreshTick, onDismiss = { showBoosterDialog = false })
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
                            text = uiState.cat.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Text(
                            text = stringResource(
                                R.string.cat_level_badge,
                                uiState.cat.level,
                                stringResource(Cat.getLevelTitleResId(uiState.cat.level)),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
                actions = {
                    GoldAdChip(
                        foodAdState = uiState.foodAdState,
                        adsRemaining = uiState.dailyGoldAdsRemaining,
                        isNetworkAvailable = uiState.isNetworkAvailable,
                        onClick = { (context as? Activity)?.let { viewModel.showFoodAd(it) } },
                    )
                    val animatedCoins by animateIntAsState(targetValue = uiState.cat.coins, label = "coins")
                    ChipPill(
                        text = animatedCoins.toString(),
                        leadingEmoji = "🪙",
                        backgroundColor = AccentGold.copy(alpha = 0.16f),
                        contentColor = AccentGold,
                        onClick = { viewModel.showGoldStatus() },
                    )
                    Spacer(Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        containerColor = Color.Transparent,
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Cat visual hero
                Box(modifier = Modifier.softEntrance(delayMillis = 0)) {
                    CatVisualHero(
                        moodGradient = moodGradient(uiState.cat, isSleeping),
                        moodEmoji = uiState.moodEmoji,
                        moodLabel = stringResource(uiState.moodTextResId),
                        catImageRes = getCatImageResource(uiState.cat, isSleeping),
                        catName = uiState.cat.name,
                        isSleeping = isSleeping,
                        showHeart = showHeart,
                        motionEnabled = motionEnabled,
                        activeBoostersExist = viewModel.getActiveBoosters().isNotEmpty(),
                        firstBoosterEmoji = viewModel.getActiveBoosters().firstOrNull()?.emoji ?: "",
                        onPositioned = { coords ->
                            val bounds = coords.boundsInWindow()
                            catCenter = Offset(bounds.center.x, bounds.center.y)
                        },
                        onCatClick = {
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
                        onBoosterClick = { showBoosterDialog = true },
                        onShopClick = { showShop = true },
                    )
                }

                Spacer(Modifier.height(12.dp))

                @Suppress("UNUSED_EXPRESSION") boosterRefreshTick
                ActiveBoostStrip(
                    boosters = viewModel.getActiveBoosters(),
                    formatRemaining = { viewModel.getBoosterRemainingTime(it) },
                    onClick = { showBoosterDialog = true },
                )

                Spacer(Modifier.height(16.dp))

                Box(modifier = Modifier.softEntrance(delayMillis = 80)) {
                    CatStatsCard(
                        hunger = uiState.cat.hunger,
                        energy = uiState.cat.energy,
                        happiness = uiState.cat.happiness,
                        levelProgress = uiState.cat.levelProgress,
                        xp = uiState.cat.xp.toInt(),
                        xpForNextLevel = uiState.cat.xpForNextLevel.toInt(),
                        level = uiState.cat.level,
                    )
                }

                Spacer(Modifier.height(20.dp))

                Box(modifier = Modifier.softEntrance(delayMillis = 140).fillMaxWidth()) {
                    CatActionRow(
                        isSleeping = isSleeping,
                        catEnergy = uiState.cat.energy,
                        catHunger = uiState.cat.hunger,
                        sleepAdState = uiState.sleepAdState,
                        sleepAdsRemaining = uiState.sleepAdsRemaining,
                        isNetworkAvailable = uiState.isNetworkAvailable,
                        sleepRemaining = remember(tick) { viewModel.getSleepRemainingTime() },
                        onFeed = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showShop = true
                        },
                        onPlay = {
                            val canPlay = uiState.cat.energy >= GameType.values().minOf { it.energyCost }
                            if (canPlay) onNavigate(com.mert.paticat.ui.navigation.Screen.Games.route)
                            else viewModel.setMessage(context.getString(R.string.home_low_energy_snackbar))
                        },
                        onSleep = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.sleepCat()
                        },
                        onShowSleepAd = {
                            (context as? Activity)?.let { viewModel.showSleepAd(it) }
                        },
                    )
                }

                Spacer(Modifier.height(20.dp))

                Box(modifier = Modifier.softEntrance(delayMillis = 200).fillMaxWidth()) {
                    InventorySection(
                        uiState = uiState,
                        viewModel = viewModel,
                        particleSystem = particleSystem,
                        catCenter = catCenter,
                        haptic = haptic,
                        onOpenShop = { showShop = true },
                    )
                }

                Spacer(Modifier.height(120.dp))
            }

            ParticleSystemCanvas(state = particleSystem, modifier = Modifier.fillMaxSize())

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
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text("🐱", fontSize = 64.sp)
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                }
            }

            ShopSheet(
                visible = showShop,
                uiState = uiState,
                viewModel = viewModel,
                onDismiss = { showShop = false },
            )
        }
    }
}

// ============================ CAT VISUAL HERO ============================

@Composable
private fun CatVisualHero(
    moodGradient: List<Color>,
    moodEmoji: String,
    moodLabel: String,
    catImageRes: Int,
    catName: String,
    isSleeping: Boolean,
    showHeart: Boolean,
    motionEnabled: Boolean,
    activeBoostersExist: Boolean,
    firstBoosterEmoji: String,
    onPositioned: (androidx.compose.ui.layout.LayoutCoordinates) -> Unit,
    onCatClick: () -> Unit,
    onBoosterClick: () -> Unit,
    onShopClick: () -> Unit,
) {
    TintedPillowCard(
        gradient = moodGradient,
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
        contentPadding = 0.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Mood badge
            ChipPill(
                text = moodLabel,
                leadingEmoji = moodEmoji,
                backgroundColor = Color.White.copy(alpha = 0.55f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
            )

            // Cat image
            Crossfade(
                targetState = catImageRes,
                animationSpec = tween(if (motionEnabled) 600 else 0),
                label = "catMood",
            ) { resId ->
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = stringResource(R.string.cat_image_description, catName),
                    modifier = Modifier
                        .size(290.dp)
                        .breath(amplitude = 0.025f, periodMillis = 2200)
                        .onGloballyPositioned(onPositioned)
                        .pillowPress(enabled = !isSleeping, onClick = onCatClick),
                    contentScale = ContentScale.Fit,
                )
            }

            // Floating heart
            AnimatedVisibility(
                visible = showHeart,
                enter = fadeIn(tween(if (motionEnabled) 200 else 0)),
                exit = fadeOut(tween(if (motionEnabled) 600 else 0)) +
                    slideOutVertically(tween(if (motionEnabled) 600 else 0)) { -it },
                modifier = Modifier.align(Alignment.TopCenter),
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = stringResource(R.string.icon_favorite),
                    modifier = Modifier
                        .padding(top = 28.dp)
                        .size(48.dp),
                    tint = PremiumPink,
                )
            }

            // Booster + Shop FABs
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (activeBoostersExist) {
                    FloatingFab(
                        emoji = firstBoosterEmoji.takeIf { it.isNotEmpty() } ?: "✨",
                        backgroundColor = PremiumBlue.copy(alpha = 0.25f),
                        contentDescription = stringResource(R.string.icon_booster_fab),
                        onClick = onBoosterClick,
                    )
                }
                FloatingFab(
                    emoji = "🛒",
                    backgroundColor = AccentGold.copy(alpha = 0.25f),
                    contentDescription = stringResource(R.string.icon_shop_fab),
                    onClick = onShopClick,
                )
            }
        }
    }
}

@Composable
private fun FloatingFab(
    emoji: String,
    backgroundColor: Color,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val cd = contentDescription
    Box(
        modifier = Modifier
            .size(Dimensions.touchTargetMin)
            .clip(CircleShape)
            .background(backgroundColor)
            .pillowPress(onClick = onClick)
            .semantics { this.contentDescription = cd },
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = 22.sp)
    }
}

// ============================ STATS CARD ============================

@Composable
private fun CatStatsCard(
    hunger: Int,
    energy: Int,
    happiness: Int,
    levelProgress: Float,
    xp: Int,
    xpForNextLevel: Int,
    level: Int,
) {
    PillowCard(modifier = Modifier.fillMaxWidth(), contentPadding = 18.dp) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                val animatedHunger by animateIntAsState(targetValue = hunger, label = "hunger")
                val animatedEnergy by animateIntAsState(targetValue = energy, label = "energy")
                val animatedHappy by animateIntAsState(targetValue = happiness, label = "happiness")
                CircularStatItem(Icons.Filled.Fastfood, stringResource(R.string.cat_stat_hunger), animatedHunger, PremiumPeach)
                CircularStatItem(Icons.Filled.Bolt, stringResource(R.string.cat_stat_energy), animatedEnergy, PremiumBlue)
                CircularStatItem(Icons.Filled.Favorite, stringResource(R.string.cat_stat_happiness), animatedHappy, PremiumPink)
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Filled.Star, null, modifier = Modifier.size(14.dp), tint = AccentGold)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(AccentGold.copy(alpha = 0.16f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(levelProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                Brush.horizontalGradient(listOf(AccentGold, AccentGold.copy(alpha = 0.7f))),
                            ),
                    )
                }
                Text(
                    text = stringResource(R.string.cat_xp_label, xp, xpForNextLevel),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(6.dp))
            val isMax = level >= 50
            val xpRemaining = (xpForNextLevel - xp).coerceAtLeast(0)
            Text(
                text = if (isMax)
                    "⭐ ${stringResource(R.string.cat_xp_max_level)}"
                else
                    "✨ ${stringResource(R.string.cat_xp_remaining, xpRemaining)}",
                style = MaterialTheme.typography.labelMedium,
                color = AccentGold.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CircularStatItem(icon: ImageVector, label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularStatIndicator(
            progress = value / 100f,
            color = color,
            size = 70.dp,
            strokeWidth = 7.dp,
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(24.dp), tint = color)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.unit_percentage, value),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ============================ ACTION ROW ============================

@Composable
private fun CatActionRow(
    isSleeping: Boolean,
    catEnergy: Int,
    catHunger: Int,
    sleepAdState: AdState,
    sleepAdsRemaining: Int,
    isNetworkAvailable: Boolean,
    sleepRemaining: String,
    onFeed: () -> Unit,
    onPlay: () -> Unit,
    onSleep: () -> Unit,
    onShowSleepAd: () -> Unit,
) {
    if (isSleeping) {
        SleepingStateCard(
            sleepRemaining = sleepRemaining,
            sleepAdState = sleepAdState,
            sleepAdsRemaining = sleepAdsRemaining,
            isNetworkAvailable = isNetworkAvailable,
            onShowSleepAd = onShowSleepAd,
        )
        return
    }

    val canPlay = catEnergy >= GameType.values().minOf { it.energyCost }
    val sleepEnabled = catEnergy < 40

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CatActionTile(
            modifier = Modifier.weight(1f),
            emoji = "🍖",
            label = stringResource(R.string.cat_action_feed),
            accent = PremiumPeach,
            enabled = !isSleeping && catHunger < 95,
            onClick = onFeed,
        )
        CatActionTile(
            modifier = Modifier.weight(1f),
            emoji = "🎮",
            label = stringResource(R.string.cat_action_play),
            accent = MaterialTheme.colorScheme.primary,
            enabled = canPlay,
            onClick = onPlay,
        )
        CatActionTile(
            modifier = Modifier.weight(1f),
            emoji = "😴",
            label = stringResource(R.string.cat_action_sleep),
            accent = PremiumBlue,
            enabled = sleepEnabled,
            onClick = onSleep,
        )
    }
}

@Composable
private fun CatActionTile(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    accent: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    PillowCard(
        modifier = modifier.heightIn(min = 92.dp),
        backgroundColor = if (enabled) accent.copy(alpha = 0.16f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentPadding = 12.dp,
        onClick = if (enabled) onClick else null,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                // WCAG: disabled label uses onSurface.alpha=0.5 against surfaceVariant
                // background — keeps AA contrast in both light + dark themes (vs the
                // prior onSurfaceVariant which was borderline on dark surfaceVariant).
                color = if (enabled) accent
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
    }
}

// ============================ SLEEPING STATE ============================

@Composable
private fun SleepingStateCard(
    sleepRemaining: String,
    sleepAdState: AdState,
    sleepAdsRemaining: Int,
    isNetworkAvailable: Boolean,
    onShowSleepAd: () -> Unit,
) {
    val adReady = sleepAdState is AdState.Loaded && isNetworkAvailable && sleepAdsRemaining > 0
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TintedPillowCard(
            gradient = MoodSleepGradient,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 28.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Bedtime, null, modifier = Modifier.size(34.dp), tint = Color.White)
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.cat_sleeping_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = sleepRemaining,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
            }
        }

        ActionPillButton(
            text = "${stringResource(R.string.cat_ad_reduce_sleep)} ($sleepAdsRemaining)",
            leadingEmoji = "📺",
            onClick = onShowSleepAd,
            enabled = adReady,
            backgroundColor = PremiumPeach,
            modifier = Modifier.fillMaxWidth(),
        )

        val statusText = when {
            !isNetworkAvailable -> stringResource(R.string.cat_no_internet)
            sleepAdsRemaining <= 0 -> stringResource(R.string.cat_ad_limit_reached)
            sleepAdState is AdState.Loading -> stringResource(R.string.cat_ad_loading)
            sleepAdState is AdState.Error -> stringResource(R.string.cat_ad_error)
            else -> null
        }
        if (statusText != null && !adReady) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = if (!isNetworkAvailable || sleepAdState is AdState.Error)
                    MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ============================ INVENTORY ============================

@Composable
private fun InventorySection(
    uiState: CatUiState,
    viewModel: CatViewModel,
    particleSystem: ParticleSystemState,
    catCenter: Offset,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    onOpenShop: () -> Unit,
) {
    val isSleeping = viewModel.isCatSleeping()
    val ownedItems = uiState.inventory.filter { it.value > 0 }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader2(
            title = stringResource(R.string.inventory_section_title),
            leadingEmoji = "🎒",
        )

        if (ownedItems.isEmpty()) {
            PillowCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                contentPadding = 22.dp,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("📦", fontSize = 36.sp)
                    Text(
                        text = stringResource(R.string.inventory_empty_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    ActionPillButton(
                        text = stringResource(R.string.shop_open_button),
                        leadingEmoji = "🛒",
                        onClick = onOpenShop,
                        backgroundColor = AccentGold,
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
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
                                color = PremiumPink,
                            )
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.feedCatWithItem(item)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryChipCard(
    item: ShopItem,
    quantity: Int,
    isSleeping: Boolean,
    canFeed: Boolean,
    onFeed: () -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    PillowCard(
        modifier = Modifier.width(144.dp),
        backgroundColor = primary.copy(alpha = 0.10f),
        contentPadding = 12.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(item.emoji, fontSize = 26.sp)
            }
            Text(
                text = stringResource(item.nameResId),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "x$quantity",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = primary,
            )
            if (isSleeping && quantity > 0) {
                Text(
                    text = stringResource(R.string.feed_disabled_sleeping),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                )
            }
            ActionPillButton(
                text = stringResource(R.string.feed_button),
                onClick = onFeed,
                enabled = canFeed && quantity > 0,
                backgroundColor = primary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ============================ ACTIVE BOOST STRIP ============================

@Composable
private fun ActiveBoostStrip(
    boosters: List<CatViewModel.BoosterInfo>,
    formatRemaining: (Long) -> String,
    onClick: () -> Unit,
) {
    AnimatedVisibility(visible = boosters.isNotEmpty()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(boosters) { booster ->
                ChipPill(
                    text = "2x ${formatRemaining(booster.expiresAt)}",
                    leadingEmoji = booster.emoji,
                    backgroundColor = PremiumBlue.copy(alpha = 0.16f),
                    contentColor = PremiumBlue,
                    onClick = onClick,
                )
            }
        }
    }
}

// ============================ TOP BAR ============================

@Composable
private fun GoldAdChip(
    foodAdState: AdState,
    adsRemaining: Int,
    isNetworkAvailable: Boolean,
    onClick: () -> Unit,
) {
    if (adsRemaining <= 0) return
    val adReady = foodAdState is AdState.Loaded && isNetworkAvailable
    Row(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(Dimensions.radiusS))
            .background(if (adReady) PremiumMint else PremiumMint.copy(alpha = 0.4f))
            .pillowPress(enabled = adReady, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (foodAdState) {
            AdState.Loading -> CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
            AdState.Error -> Icon(
                Icons.Filled.Warning, null,
                modifier = Modifier.size(12.dp), tint = Color.White,
            )
            else -> {
                Icon(
                    Icons.Filled.OndemandVideo, null,
                    modifier = Modifier.size(12.dp), tint = Color.White,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "+${ShopItem.GOLD_PER_AD} 🪙 ($adsRemaining)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

// ============================ SHOP SHEET ============================

private enum class ShopTab { FOOD, BOOST, INVENTORY }

@Composable
private fun ShopSheet(
    visible: Boolean,
    uiState: CatUiState,
    viewModel: CatViewModel,
    onDismiss: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(ShopTab.FOOD) }

    SoftSheet(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionHeader2(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.shop_section_title),
                    leadingEmoji = "🛒",
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.icon_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            MarshmallowTabs(
                selectedIndex = selectedTab.ordinal,
                items = listOf(
                    stringResource(R.string.shop_tab_food),
                    stringResource(R.string.shop_tab_boost),
                    stringResource(R.string.shop_tab_inventory),
                ),
                onSelect = { selectedTab = ShopTab.values()[it] },
                modifier = Modifier.fillMaxWidth(),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                when (selectedTab) {
                    ShopTab.FOOD -> ShopFoodSection(uiState, viewModel)
                    ShopTab.BOOST -> ShopBoostSection(uiState, viewModel)
                    ShopTab.INVENTORY -> ShopInventorySection(uiState, viewModel)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ShopFoodSection(uiState: CatUiState, viewModel: CatViewModel) {
    val visibleItems = ShopItem.ALL.filter { it.category != ShopCategory.BOOST }
    var confirmingItem by remember { mutableStateOf<ShopItem?>(null) }

    if (visibleItems.isNotEmpty() && uiState.cat.coins < visibleItems.minOf { it.price }) {
        PillowCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = AccentGold.copy(alpha = 0.12f),
            contentPadding = 14.dp,
        ) {
            Text(
                text = stringResource(R.string.cat_earn_gold_hint),
                style = MaterialTheme.typography.bodySmall,
                color = AccentGold,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    visibleItems.chunked(2).forEach { rowItems ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            rowItems.forEach { item ->
                Box(modifier = Modifier.weight(1f)) {
                    ShopItemCard(
                        item = item,
                        gold = uiState.cat.coins,
                        ownedQty = uiState.inventory[item] ?: 0,
                        onBuy = { confirmingItem = item },
                    )
                }
            }
            if (rowItems.size < 2) Spacer(Modifier.weight(1f))
        }
    }

    confirmingItem?.let { item ->
        ShopConfirmDialog(
            item = item,
            onConfirm = { viewModel.buyFood(item); confirmingItem = null },
            onDismiss = { confirmingItem = null }
        )
    }
}

@Composable
private fun ShopBoostSection(uiState: CatUiState, viewModel: CatViewModel) {
    var confirmingItem by remember { mutableStateOf<ShopItem?>(null) }
    // VM-scoped tick — replaces per-Composable while(true) polling loops.
    val boostsRemaining by viewModel.boostTimeRemaining.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ActiveBoostSummary(remaining = boostsRemaining)
        ShopItem.byCategory(ShopCategory.BOOST).forEach { item ->
            val boostExpiry = when (item.id) {
                ShopItem.ID_XP_MULTIPLIER -> uiState.xpBoostExpiresAt
                ShopItem.ID_COMBO_MULTIPLIER -> uiState.comboBoostExpiresAt
                else -> uiState.stepBoostExpiresAt
            }
            BoostItemCard(
                item = item,
                gold = uiState.cat.coins,
                boostExpiresAt = boostExpiry,
                remainingForItem = boostsRemaining.firstOrNull { kindFor(item.id) == it.kind },
                onBuy = { confirmingItem = item },
            )
        }
    }

    confirmingItem?.let { item ->
        ShopConfirmDialog(
            item = item,
            onConfirm = { viewModel.buyFood(item); confirmingItem = null },
            onDismiss = { confirmingItem = null }
        )
    }
}

@Composable
private fun ShopConfirmDialog(
    item: com.mert.paticat.domain.model.ShopItem,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val itemName = stringResource(item.nameResId)
    MarshmallowDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.buy_confirm_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "${item.emoji} $itemName",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.buy_confirm_message, itemName, item.price),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                com.mert.paticat.ui.components.marshmallow.ActionPillButton(
                    text = stringResource(R.string.btn_cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                com.mert.paticat.ui.components.marshmallow.ActionPillButton(
                    text = stringResource(R.string.btn_confirm),
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ShopInventorySection(uiState: CatUiState, viewModel: CatViewModel) {
    val ownedItems = uiState.inventory.filter { it.value > 0 }
    val isSleeping = viewModel.isCatSleeping()

    if (ownedItems.isEmpty()) {
        PillowCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 22.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("🎒", fontSize = 36.sp)
                Text(
                    text = stringResource(R.string.shop_inventory_empty_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = stringResource(R.string.inventory_empty_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(ownedItems.toList()) { (item, qty) ->
                InventoryChipCard(
                    item = item,
                    quantity = qty,
                    isSleeping = isSleeping,
                    canFeed = !isSleeping && uiState.cat.hunger < 95,
                    onFeed = { viewModel.feedCatWithItem(item) },
                )
            }
        }
    }
}

private fun kindFor(itemId: String): CatViewModel.BoostKind = when (itemId) {
    ShopItem.ID_XP_MULTIPLIER -> CatViewModel.BoostKind.XP
    ShopItem.ID_COMBO_MULTIPLIER -> CatViewModel.BoostKind.COMBO
    else -> CatViewModel.BoostKind.STEP
}

@Composable
private fun ActiveBoostSummary(remaining: List<CatViewModel.BoostRemaining>) {
    // No while(true) polling here — `remaining` is pushed by the VM-scoped flow.
    val labelStep = stringResource(R.string.shop_boost_label_gold)
    val labelXp = stringResource(R.string.shop_boost_label_xp)
    val labelCombo = stringResource(R.string.shop_boost_label_combo)

    PillowCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = PremiumBlue.copy(alpha = 0.10f),
        contentPadding = 14.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.shop_active_boosts_title),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = PremiumBlue,
            )
            if (remaining.isEmpty()) {
                Text(
                    text = stringResource(R.string.shop_no_active_boosts),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                remaining.forEach { boost ->
                    val safeDiff = boost.remainingMs()
                    val hours = (safeDiff / 3_600_000L).toInt()
                    val mins = ((safeDiff % 3_600_000L) / 60_000L).toInt()
                    val timeStr = if (hours > 0) {
                        stringResource(R.string.time_fmt_hm, hours, mins)
                    } else {
                        stringResource(R.string.time_fmt_m, mins)
                    }
                    val label = when (boost.kind) {
                        CatViewModel.BoostKind.STEP -> labelStep
                        CatViewModel.BoostKind.XP -> labelXp
                        CatViewModel.BoostKind.COMBO -> labelCombo
                    }
                    Text(
                        text = "${boost.emoji} $label · $timeStr",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ShopItemCard(item: ShopItem, gold: Int, ownedQty: Int, onBuy: () -> Unit) {
    val canAfford = gold >= item.price
    val inventoryFull = ownedQty >= ShopItem.MAX_INVENTORY_PER_ITEM
    val canBuy = canAfford && !inventoryFull
    PillowCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 16.dp,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(item.emoji, fontSize = 34.sp)
            }
            Text(
                text = stringResource(item.nameResId),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            val statText = if (item.category == ShopCategory.ENERGY)
                "+${item.energyBoost} ⚡ +${item.hungerRestore} 🍖"
            else
                "+${item.hungerRestore} 🍖 +${item.happinessBoost} 😊"
            Text(
                text = statText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (inventoryFull) {
                Text(
                    text = stringResource(R.string.shop_inventory_full_label, ownedQty, ShopItem.MAX_INVENTORY_PER_ITEM),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.ExtraBold,
                )
            } else {
                ActionPillButton(
                    text = "${item.price} 🪙",
                    onClick = onBuy,
                    enabled = canBuy,
                    backgroundColor = AccentGold,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun BoostItemCard(
    item: ShopItem,
    gold: Int,
    boostExpiresAt: Long,
    remainingForItem: CatViewModel.BoostRemaining?,
    onBuy: () -> Unit,
) {
    // No while(true) polling here — `remainingForItem` is pushed by VM-scoped flow.
    val isActive = remainingForItem != null && remainingForItem.remainingMs() > 0
    val remainingText = if (isActive && remainingForItem != null) {
        val diffMs = remainingForItem.remainingMs()
        val hours = (diffMs / 3600000).toInt()
        val mins = ((diffMs % 3600000) / 60000).toInt()
        val timeStr = if (hours > 0) {
            stringResource(R.string.time_fmt_hm, hours, mins)
        } else {
            stringResource(R.string.time_fmt_m, mins)
        }
        stringResource(R.string.shop_boost_active, timeStr)
    } else null

    PillowCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 16.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(item.emoji, fontSize = 32.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(item.nameResId), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(
                    stringResource(item.descResId),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (remainingText != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(remainingText, style = MaterialTheme.typography.labelSmall, color = AccentGold, fontWeight = FontWeight.ExtraBold)
                }
            }
            ActionPillButton(
                text = "⚡ ${item.price}",
                onClick = onBuy,
                enabled = !isActive && gold >= item.price,
                backgroundColor = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

// ============================ HELPERS ============================

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

private fun moodGradient(cat: Cat, isSleeping: Boolean): List<Color> = when {
    isSleeping -> MoodSleepGradient
    cat.hunger < 30 -> MoodHungryGradient
    cat.energy < 30 && cat.happiness > 50 -> MoodSadGradient
    cat.energy < 30 -> MoodTiredGradient
    cat.happiness >= 80 -> MoodHappyGradient
    cat.happiness < 40 -> MoodSadGradient
    else -> MoodNeutralGradient
}

// ============================ LEGACY EXPORT (for GamesScreen) ============================

@Composable
fun GameChoiceBtn(emoji: String, choice: RockPaperScissors, onClick: (RockPaperScissors) -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pillowPress(onClick = { onClick(choice) }),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = 28.sp)
    }
}

// ============================ DIALOGS ============================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GoldTutorialDialog(onDismiss: () -> Unit) {
    val pages = listOf(
        Pair(stringResource(R.string.gold_tutorial_title_1), stringResource(R.string.gold_tutorial_desc_1)),
        Pair(stringResource(R.string.gold_tutorial_title_2), stringResource(R.string.gold_tutorial_desc_2)),
        Pair(stringResource(R.string.gold_tutorial_title_3), stringResource(R.string.gold_tutorial_desc_3)),
    )
    val pageIcons = listOf(Icons.Filled.MonetizationOn, Icons.Filled.OndemandVideo, Icons.Filled.ShoppingCart)
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    MarshmallowDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedVisibility(visible = pagerState.currentPage == 0) {
                ChipPill(
                    text = stringResource(R.string.gold_tutorial_bonus),
                    leadingEmoji = "✨",
                    backgroundColor = AccentGold.copy(alpha = 0.18f),
                    contentColor = AccentGold,
                    modifier = Modifier.padding(bottom = 14.dp),
                )
            }

            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(
                                when (page) {
                                    0 -> AccentGold.copy(alpha = 0.16f)
                                    1 -> PremiumMint.copy(alpha = 0.16f)
                                    else -> PremiumBlue.copy(alpha = 0.16f)
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = pageIcons[page],
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = when (page) { 0 -> AccentGold; 1 -> PremiumMint; else -> PremiumBlue },
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = pages[page].first,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = pages[page].second,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                repeat(pages.size) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val color = if (isSelected) AccentGold
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    val width by animateDpAsState(if (isSelected) 26.dp else 8.dp, label = "dot")
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(color)
                            .size(width = width, height = 8.dp),
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            val isLastPage = pagerState.currentPage == pages.size - 1
            ActionPillButton(
                text = if (isLastPage) stringResource(R.string.gold_tutorial_btn_done)
                else stringResource(R.string.gold_tutorial_btn_next),
                onClick = {
                    if (isLastPage) onDismiss()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = AccentGold,
            )
        }
    }
}

@Composable
fun BoosterDialog(viewModel: CatViewModel, tick: Int, onDismiss: () -> Unit) {
    @Suppress("UNUSED_EXPRESSION") tick
    val activeBoosters = viewModel.getActiveBoosters()
    if (activeBoosters.isEmpty()) {
        onDismiss()
        return
    }

    MarshmallowDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.booster_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                activeBoosters.forEach { booster ->
                    PillowCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = PremiumBlue.copy(alpha = 0.10f),
                        contentPadding = 14.dp,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    imageVector = when (booster.emoji) {
                                        "👟" -> Icons.Filled.DirectionsRun
                                        "⭐" -> Icons.Filled.Star
                                        else -> Icons.Filled.AutoAwesome
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                    tint = PremiumBlue,
                                )
                                Text(
                                    text = booster.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Text(
                                text = viewModel.getBoosterRemainingTime(booster.expiresAt),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = PremiumBlue,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            ActionPillButton(
                text = stringResource(R.string.dialog_close),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = AccentGold,
            )
        }
    }
}
