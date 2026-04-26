@file:OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
package com.mert.paticat.ui.screens.games

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mert.paticat.R
import com.mert.paticat.ui.screens.cat.CatViewModel
import com.mert.paticat.ui.screens.cat.GameType
import com.mert.paticat.ui.screens.cat.GameUiState
import com.mert.paticat.ui.screens.cat.MiniGameState
import com.mert.paticat.ui.screens.cat.RockPaperScissors
import com.mert.paticat.ui.theme.*
import com.mert.paticat.ui.components.NativeAdCard
import com.mert.paticat.ui.components.bounceClick
import com.mert.paticat.ui.components.pulsate
import com.mert.paticat.ui.components.EntranceAnimation
import com.mert.paticat.ui.components.glowPulse
import com.mert.paticat.ui.components.shimmerEffect
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch

private const val GAME_ROOM_LEVEL_LOCK_DISABLED = false

// ════════════════════════════════════════════════════════════════════
//  GAMES SCREEN – Main Entry
// ════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun GamesScreen(
    onBackClick: () -> Unit,
    catViewModel: CatViewModel = hiltViewModel()
) {
    val uiState by catViewModel.uiState.collectAsStateWithLifecycle()
    val gameState by catViewModel.gameUiState.collectAsStateWithLifecycle()
    val playerChoice by catViewModel.playerChoice.collectAsStateWithLifecycle()
    val catChoice by catViewModel.catChoice.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var previewGame by remember { mutableStateOf<GameType?>(null) }

    val effectiveCatLevel = if (GAME_ROOM_LEVEL_LOCK_DISABLED) Int.MAX_VALUE else uiState.cat.level

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            catViewModel.clearMessage()
        }
    }

    // ── Game Dialog ──
    if (gameState.activeGame != null) {
        Dialog(
            onDismissRequest = { catViewModel.closeMiniGame() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val dialogGradient = when (gameState.activeGame) {
                GameType.RPS    -> Brush.verticalGradient(listOf(GameVibrantRPS.copy(alpha = 0.12f), Color.White))
                GameType.SLOTS  -> Brush.verticalGradient(listOf(GameVibrantSlots.copy(alpha = 0.12f), Color.White))
                GameType.MEMORY -> Brush.verticalGradient(listOf(GameVibrantMemory.copy(alpha = 0.12f), Color.White))
                GameType.REFLEX -> Brush.verticalGradient(listOf(GameVibrantReflex.copy(alpha = 0.12f), Color.White))
                GameType.CATCH  -> Brush.verticalGradient(listOf(GameVibrantCatch.copy(alpha = 0.12f), Color.White))
                else            -> Brush.verticalGradient(listOf(Color.White, Color.White))
            }
            Card(
                shape = RoundedCornerShape(40.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 24.dp)
                    .fillMaxWidth(0.96f)
            ) {
                Column(
                    modifier = Modifier
                        .background(dialogGradient)
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (gameState.activeGame) {
                        GameType.RPS    -> RockPaperScissorsGame(gameState, uiState, playerChoice, catChoice, catViewModel)
                        GameType.SLOTS  -> SlotsGame(gameState, catViewModel)
                        GameType.MEMORY -> MemoryGame(gameState, catViewModel)
                        GameType.REFLEX -> ReflexGame(gameState, catViewModel)
                        GameType.CATCH  -> CatchGame(gameState, catViewModel)
                        else            -> {}
                    }
                }
            }
        }
    }

    // Auto-close preview when a real game starts
    LaunchedEffect(gameState.activeGame) {
        if (gameState.activeGame != null) previewGame = null
    }

    // Pre-game overview overlay
    previewGame?.let { game ->
        PreGameOverlay(
            gameType = game,
            catLevel = effectiveCatLevel,
            catEnergy = uiState.cat.energy,
            onPlay = {
                catViewModel.startGame(
                    game,
                    ignoreLevelLock = GAME_ROOM_LEVEL_LOCK_DISABLED,
                    ignoreEnergyLimit = GAME_ROOM_LEVEL_LOCK_DISABLED
                )
                previewGame = null
            },
            onDismiss = { previewGame = null }
        )
    }

    // ── Main Screen ──
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(bottom = 100.dp))
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 0.dp, bottom = 120.dp)
        ) {
            // ── Arcade Header ──
            item(span = { GridItemSpan(2) }) {
                GamesScreenHeader(
                    catEnergy = uiState.cat.energy,
                    onBackClick = onBackClick
                )
            }

            // ── Game Cards ──
            item {
                EntranceAnimation(delay = 0) {
                    GameCard(
                        emoji = "✊",
                        title = stringResource(R.string.game_rps_title),
                        description = stringResource(R.string.game_rps_desc),
                        energyCost = GameType.RPS.energyCost,
                        catEnergy = uiState.cat.energy,
                        catLevel = effectiveCatLevel,
                        minLevel = GameType.RPS.minLevel,
                        gradientColors = listOf(GameVibrantRPS, GameVibrantRPSLight),
                        accentColor = GameVibrantRPS,
                        textColor = Color.White,
                        onClick = { previewGame = GameType.RPS }
                    )
                }
            }
            item {
                EntranceAnimation(delay = 80) {
                    GameCard(
                        emoji = "🎰",
                        title = stringResource(R.string.game_slots_title),
                        description = stringResource(R.string.game_slots_desc),
                        energyCost = GameType.SLOTS.energyCost,
                        catEnergy = uiState.cat.energy,
                        catLevel = effectiveCatLevel,
                        minLevel = GameType.SLOTS.minLevel,
                        gradientColors = listOf(GameVibrantSlots, GameVibrantSlotsLight),
                        accentColor = GameVibrantSlots,
                        textColor = Color.White,
                        onClick = { previewGame = GameType.SLOTS }
                    )
                }
            }
            item {
                EntranceAnimation(delay = 160) {
                    GameCard(
                        emoji = "🧠",
                        title = stringResource(R.string.game_memory_title),
                        description = stringResource(R.string.game_memory_desc),
                        energyCost = GameType.MEMORY.energyCost,
                        catEnergy = uiState.cat.energy,
                        catLevel = effectiveCatLevel,
                        minLevel = GameType.MEMORY.minLevel,
                        gradientColors = listOf(GameVibrantMemory, GameVibrantMemoryLight),
                        accentColor = GameVibrantMemory,
                        textColor = Color.White,
                        onClick = { previewGame = GameType.MEMORY }
                    )
                }
            }
            item {
                EntranceAnimation(delay = 240) {
                    GameCard(
                        emoji = "⚡",
                        title = stringResource(R.string.game_reflex_title),
                        description = stringResource(R.string.game_reflex_desc),
                        energyCost = GameType.REFLEX.energyCost,
                        catEnergy = uiState.cat.energy,
                        catLevel = effectiveCatLevel,
                        minLevel = GameType.REFLEX.minLevel,
                        gradientColors = listOf(GameVibrantReflex, GameVibrantReflexLight),
                        accentColor = GameVibrantReflex,
                        textColor = Color.White,
                        onClick = { previewGame = GameType.REFLEX }
                    )
                }
            }
            item {
                EntranceAnimation(delay = 320) {
                    GameCard(
                        emoji = "\uD83E\uDDF3",
                        title = stringResource(R.string.game_catch_title),
                        description = stringResource(R.string.game_catch_desc),
                        energyCost = GameType.CATCH.energyCost,
                        catEnergy = uiState.cat.energy,
                        catLevel = effectiveCatLevel,
                        minLevel = GameType.CATCH.minLevel,
                        gradientColors = listOf(GameVibrantCatch, GameVibrantCatchLight),
                        accentColor = GameVibrantCatch,
                        textColor = Color.White,
                        onClick = { previewGame = GameType.CATCH }
                    )
                }
            }

            // ── Ad Card ──
            item(span = { GridItemSpan(2) }) {
                Column {
                    Spacer(modifier = Modifier.height(4.dp))
                    NativeAdCard(nativeAd = uiState.nativeAd)
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  GAMES SCREEN HEADER – Arcade lobby banner
// ════════════════════════════════════════════════════════════════════

@Composable
private fun GamesScreenHeader(
    catEnergy: Int,
    onBackClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "header_float")
    // Single shared phase — each particle derives its own offset from this
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp, bottom = 4.dp)
    ) {
        // Gradient banner background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF3A1C8A), Color(0xFF1A0A4A))
                    )
                )
        ) {
            // Floating sparkle particles — each offset by different phase
            val sparklePositions = listOf(
                Triple(0.12f, 12.dp, 0f),
                Triple(0.30f, 28.dp, 1.05f),
                Triple(0.50f, 10.dp, 2.09f),
                Triple(0.65f, 30.dp, 3.14f),
                Triple(0.78f, 14.dp, 4.19f),
                Triple(0.90f, 26.dp, 5.24f)
            )
            sparklePositions.forEachIndexed { i, (xFrac, topPad, phaseOffset) ->
                val dy = kotlin.math.sin(phase + phaseOffset) * 7f
                Text(
                    "✨",
                    fontSize = (10 + i % 3 * 3).sp,
                    color = Color.White.copy(alpha = 0.55f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = (xFrac * 320).dp, top = topPad)
                        .graphicsLayer { translationY = dy }
                )
            }
        }

        // Back button + title + energy badge overlaid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.btn_back),
                    tint = Color.White
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.games_title),
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    stringResource(R.string.games_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            Surface(
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(50.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚡", fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "$catEnergy",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  GAME CARD  –  Arcade gradient + glow + shimmer
// ════════════════════════════════════════════════════════════════════

@Composable
private fun GameCard(
    emoji: String,
    title: String,
    description: String,
    energyCost: Int,
    catEnergy: Int,
    catLevel: Int,
    minLevel: Int,
    gradientColors: List<Color>,
    accentColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isLocked = catLevel < minLevel
    val isEnabled = !isLocked && (GAME_ROOM_LEVEL_LOCK_DISABLED || catEnergy >= energyCost)

    val difficultyStars = when {
        energyCost <= 8  -> 1
        energyCost <= 12 -> 2
        else             -> 3
    }

    val infiniteTransition = rememberInfiniteTransition(label = "float_$emoji")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emoji_float"
    )

    val deepBottomColor = gradientColors.getOrElse(1) { gradientColors[0] }.darken(0.14f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .let { m -> if (isEnabled) m.glowPulse(accentColor, 0.25f, 0.55f) else m }
            .graphicsLayer { alpha = if (isLocked) 1f else if (isEnabled) 1f else 0.6f }
            .bounceClick {
                if (!isLocked) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            }
            .shadow(
                elevation = if (isEnabled) 12.dp else 2.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = accentColor.copy(alpha = 0.5f)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 170.dp)
                .background(
                    brush = Brush.verticalGradient(listOf(gradientColors[0], deepBottomColor)),
                    shape = RoundedCornerShape(28.dp)
                )
        ) {
            // Main content — blurred when locked
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .let { m -> if (isLocked) m.blur(2.dp) else m },
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Emoji box with shimmer
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(68.dp)
                        .background(Color.White.copy(alpha = 0.25f), CircleShape)
                        .shimmerEffect()
                        .graphicsLayer { translationY = floatOffset }
                ) {
                    Text(emoji, fontSize = 34.sp)
                }

                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor,
                    lineHeight = 20.sp
                )

                Surface(
                    color = Color.White.copy(alpha = if (isEnabled) 0.30f else 0.18f),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Text(
                        if (isEnabled) "⚡ $energyCost" else "⚡ $catEnergy/$energyCost",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (!isEnabled) MaterialTheme.colorScheme.error else textColor
                    )
                }
            }

            // Decorative corner highlight
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        Brush.radialGradient(listOf(Color.White.copy(alpha = 0.20f), Color.Transparent)),
                        CircleShape
                    )
            )

            // Star difficulty — top right
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(3) { i ->
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if (i < difficultyStars) Color(0xFFFFD600) else Color.White.copy(alpha = 0.3f)
                    )
                }
            }

            // Locked overlay
            if (isLocked) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.Black.copy(alpha = 0.52f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔒", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Sv. $minLevel",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  SHARED – Pill Badge & Result Overlay
// ════════════════════════════════════════════════════════════════════

@Composable
fun GameBadge(text: String, color: Color, textColor: Color = color) {
    Surface(
        color = color.copy(alpha = 0.25f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GameResultOverlay(gameState: MiniGameState, reward: com.mert.paticat.ui.screens.cat.MiniGameReward?) {
    val resultEmoji = when (gameState) {
        MiniGameState.RESULT_WIN  -> "🎉"
        MiniGameState.RESULT_LOSE -> "😿"
        MiniGameState.RESULT_DRAW -> "🤝"
        else                      -> ""
    }
    val resultText = when (gameState) {
        MiniGameState.RESULT_WIN  -> stringResource(R.string.result_win)
        MiniGameState.RESULT_LOSE -> stringResource(R.string.result_lose)
        MiniGameState.RESULT_DRAW -> stringResource(R.string.result_draw)
        else                      -> ""
    }
    val resultColor = when (gameState) {
        MiniGameState.RESULT_WIN  -> GameVibrantMemory
        MiniGameState.RESULT_LOSE -> GameVibrantRPS
        else                      -> Color(0xFF1565C0)
    }
    val resultTextColor = when (gameState) {
        MiniGameState.RESULT_WIN  -> Color(0xFF004D40)
        MiniGameState.RESULT_LOSE -> Color(0xFF880E4F)
        else                      -> Color(0xFF0D47A1)
    }

    // Bouncy entrance for the emoji
    var emojiVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { emojiVisible = true }

    // Staggered bouncing stars for WIN
    val starScales = remember { List(3) { Animatable(0f) } }
    LaunchedEffect(gameState) {
        if (gameState == MiniGameState.RESULT_WIN) {
            val scope = this
            starScales.forEachIndexed { i, anim ->
                scope.launch {
                    anim.snapTo(0f)
                    kotlinx.coroutines.delay(i * 120L)
                    anim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
                    anim.animateTo(0.85f, tween(300))
                    anim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Bouncing stars on WIN
        if (gameState == MiniGameState.RESULT_WIN) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                starScales.forEach { scale ->
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp).graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                        },
                        tint = Color(0xFFFFD600)
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = emojiVisible,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)) + fadeIn()
        ) {
            Text(resultEmoji, fontSize = 64.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            resultText,
            style = MaterialTheme.typography.headlineSmall,
            color = resultTextColor,
            fontWeight = FontWeight.Black
        )
        if (gameState == MiniGameState.RESULT_WIN) {
            Text(
                "Harika! 🎊",
                style = MaterialTheme.typography.bodyLarge,
                color = resultTextColor.copy(alpha = 0.75f),
                fontWeight = FontWeight.SemiBold
            )
        }

        if (reward != null && (reward.gold > 0 || reward.happy > 0 || reward.xp > 0)) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = resultColor.copy(alpha = 0.2f),
                border = BorderStroke(1.5.dp, resultColor.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        stringResource(R.string.rewards_won),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelMedium,
                        color = resultTextColor.copy(alpha = 0.8f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (reward.gold > 0)
                            EntranceAnimation(delay = 100) {
                                RewardPill("+${reward.gold} 🪙", Color(0xFFFFD600), Color(0xFF4E342E))
                            }
                        if (reward.happy > 0)
                            EntranceAnimation(delay = 200) {
                                RewardPill("+${reward.happy} 💖", GameVibrantRPS, Color.White)
                            }
                        if (reward.xp > 0)
                            EntranceAnimation(delay = 300) {
                                RewardPill("+${reward.xp} ⭐", GameVibrantCatch, Color.White)
                            }
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardPill(text: String, bgColor: Color, textColor: Color) {
    Surface(
        color = bgColor.copy(alpha = 0.88f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.5.dp, bgColor)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
    }
}

/** Shared styled action buttons for game result states. */
@Composable
fun GameActionButtons(
    accentColor: Color,
    onPlayAgain: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Play Again — gradient pill
        Box(
            modifier = Modifier
                .weight(1.6f)
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(50.dp), spotColor = accentColor.copy(alpha = 0.5f))
                .clip(RoundedCornerShape(50.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(accentColor, accentColor.darken(0.18f))
                    )
                )
                .bounceClick {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onPlayAgain()
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null,
                    modifier = Modifier.size(20.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.btn_play_again),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
        // Close — outlined icon pill
        Box(
            modifier = Modifier
                .height(56.dp)
                .width(56.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.12f))
                .bounceClick {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClose()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.btn_close),
                modifier = Modifier.size(22.dp), tint = accentColor)
        }
    }
}

fun Color.darken(factor: Float): Color = Color(
    red   = (red   * (1 - factor)).coerceIn(0f, 1f),
    green = (green * (1 - factor)).coerceIn(0f, 1f),
    blue  = (blue  * (1 - factor)).coerceIn(0f, 1f),
    alpha = alpha
)

// ════════════════════════════════════════════════════════════════════
//  RPS GAME
// ════════════════════════════════════════════════════════════════════

@Composable
fun RockPaperScissorsGame(
    gameState: GameUiState,
    catUiState: com.mert.paticat.ui.screens.cat.CatUiState,
    playerChoice: RockPaperScissors?,
    catChoice: RockPaperScissors?,
    viewModel: CatViewModel
) {
    val isCountingDown = playerChoice != null && gameState.miniGameState == MiniGameState.PLAYING
    val shakeOffset = remember { Animatable(0f) }
    var countdownNum by remember { mutableStateOf(3) }

    LaunchedEffect(isCountingDown) {
        if (isCountingDown) {
            countdownNum = 3
            repeat(3) {
                shakeOffset.animateTo(18f, tween(120, easing = FastOutSlowInEasing))
                shakeOffset.animateTo(-18f, tween(120, easing = FastOutSlowInEasing))
            }
            shakeOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
        } else {
            shakeOffset.snapTo(0f)
        }
    }

    LaunchedEffect(isCountingDown, countdownNum) {
        if (isCountingDown && countdownNum > 0) {
            kotlinx.coroutines.delay(180)
            countdownNum = (countdownNum - 1).coerceAtLeast(0)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("✊📄✂️", fontSize = 30.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.game_rps_header),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFB5294E)
        )

        val statusText = when {
            isCountingDown -> stringResource(R.string.game_rps_countdown)
            gameState.miniGameState != MiniGameState.PLAYING -> ""
            else -> stringResource(R.string.game_win_reward)
        }
        if (statusText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                statusText,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB5294E).copy(alpha = 0.65f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Battle Arena ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = GamePastelPinkLight.copy(alpha = 0.6f),
            border = BorderStroke(1.5.dp, GamePastelPink.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BattleAvatar(
                    label = stringResource(R.string.game_you),
                    emoji = if (isCountingDown) "🤜" else when (playerChoice) {
                        RockPaperScissors.ROCK     -> "🪨"
                        RockPaperScissors.PAPER    -> "📄"
                        RockPaperScissors.SCISSORS -> "✂️"
                        null                       -> "👤"
                    },
                    color = GamePastelBlue,
                    textColor = Color(0xFF1565C0),
                    rotation = if (isCountingDown) shakeOffset.value else 0f
                )

                // VS bubble – pulsating
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .pulsate(duration = 900),
                    shape = CircleShape,
                    color = GamePastelPink,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            stringResource(R.string.game_vs),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFFB5294E)
                        )
                    }
                }

                BattleAvatar(
                    label = catUiState.cat.name,
                    emoji = if (isCountingDown) "🤛" else when (catChoice) {
                        RockPaperScissors.ROCK     -> "🪨"
                        RockPaperScissors.PAPER    -> "📄"
                        RockPaperScissors.SCISSORS -> "✂️"
                        null                       -> "😺"
                    },
                    color = GamePastelPink,
                    textColor = Color(0xFFB5294E),
                    rotation = if (isCountingDown) -shakeOffset.value else 0f
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Interaction / Result ──
        if (gameState.miniGameState != MiniGameState.PLAYING) {
            EntranceAnimation { GameResultOverlay(gameState.miniGameState, gameState.lastReward) }
            Spacer(modifier = Modifier.height(16.dp))
            GameActionButtons(
                accentColor = GamePastelPink,
                onPlayAgain = {
                    viewModel.startGame(
                        GameType.RPS,
                        ignoreLevelLock = GAME_ROOM_LEVEL_LOCK_DISABLED,
                        ignoreEnergyLimit = GAME_ROOM_LEVEL_LOCK_DISABLED
                    )
                },
                onClose = { viewModel.closeMiniGame() }
            )
        } else if (!isCountingDown) {
            Text(
                stringResource(R.string.game_win_reward),
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFB5294E).copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RPSChoiceCard("🪨", stringResource(R.string.game_choice_rock), RockPaperScissors.ROCK,     Modifier.weight(1f)) { viewModel.playRPS(it) }
                RPSChoiceCard("📄", stringResource(R.string.game_choice_paper), RockPaperScissors.PAPER,    Modifier.weight(1f)) { viewModel.playRPS(it) }
                RPSChoiceCard("✂️", stringResource(R.string.game_choice_scissors), RockPaperScissors.SCISSORS, Modifier.weight(1f)) { viewModel.playRPS(it) }
            }
        } else {
            // Animated 3-2-1 countdown
            Box(contentAlignment = Alignment.Center, modifier = Modifier.height(80.dp)) {
                androidx.compose.animation.AnimatedContent(
                    targetState = countdownNum,
                    transitionSpec = {
                        (scaleIn(spring(dampingRatio = Spring.DampingRatioLowBouncy)) + fadeIn(tween(80))) with
                            (scaleOut(tween(120)) + fadeOut(tween(80)))
                    },
                    label = "countdown"
                ) { num ->
                    Text(
                        if (num <= 0) stringResource(R.string.game_countdown_go) else num.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = GameVibrantRPS
                    )
                }
            }
        }
    }
}

@Composable
private fun BattleAvatar(
    label: String,
    emoji: String,
    color: Color,
    textColor: Color,
    rotation: Float
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .size(84.dp)
                .graphicsLayer { rotationZ = rotation },
            shape = CircleShape,
            color = color.copy(alpha = 0.35f),
            border = BorderStroke(2.5.dp, color.copy(alpha = 0.55f)),
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 40.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = textColor
        )
    }
}

@Composable
private fun RPSChoiceCard(
    emoji: String,
    label: String,
    choice: RockPaperScissors,
    modifier: Modifier = Modifier,
    onClick: (RockPaperScissors) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val gradientColor = when (choice) {
        RockPaperScissors.ROCK     -> Color(0xFFFF6B6B)
        RockPaperScissors.PAPER    -> Color(0xFF4ECDC4)
        RockPaperScissors.SCISSORS -> Color(0xFFFFE66D)
    }
    val textCol = when (choice) {
        RockPaperScissors.SCISSORS -> Color(0xFF7A5800)
        else -> Color.White
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .shadow(10.dp, RoundedCornerShape(24.dp), spotColor = gradientColor.copy(alpha = 0.5f))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(gradientColor, gradientColor.darken(0.2f))
                    )
                )
                .bounceClick {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick(choice)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 36.sp)
        }
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFB5294E)
        )
    }
}

// ════════════════════════════════════════════════════════════════════
//  SLOTS GAME
// ════════════════════════════════════════════════════════════════════

@Composable
fun SlotsGame(
    uiState: GameUiState,
    viewModel: CatViewModel
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🎰", fontSize = 38.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.game_slots_header),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFB5510B)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.game_slots_instruction),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFB5510B).copy(alpha = 0.65f)
        )
        Spacer(modifier = Modifier.height(20.dp))

        // ── Slot Machine ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = GamePastelPeachLight.copy(alpha = 0.7f),
            border = BorderStroke(1.5.dp, GamePastelPeach.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isJackpot = !uiState.isSpinning &&
                        uiState.slotResults.size == 3 &&
                        uiState.slotResults.all { it == uiState.slotResults[0] } &&
                        uiState.miniGameState == MiniGameState.PLAYING

                    uiState.slotResults.forEachIndexed { index, emoji ->
                        SpinningReel(
                            targetEmoji = emoji,
                            isSpinning = uiState.isSpinning,
                            delayMillis = index * 120,
                            isJackpot = isJackpot,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (uiState.miniGameState == MiniGameState.PLAYING) {
                    val isIdle = !uiState.isSpinning
                    val pulseScale = remember { Animatable(1f) }
                    LaunchedEffect(isIdle) {
                        if (isIdle) {
                            while (true) {
                                pulseScale.animateTo(1.04f, tween(700, easing = FastOutSlowInEasing))
                                pulseScale.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
                            }
                        } else {
                            pulseScale.snapTo(1f)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .graphicsLayer { scaleX = pulseScale.value; scaleY = pulseScale.value }
                            .shadow(10.dp, RoundedCornerShape(50.dp), spotColor = GameVibrantSlots.copy(alpha = 0.5f))
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                if (isIdle)
                                    Brush.horizontalGradient(listOf(GameVibrantSlots, Color(0xFFFF9800)))
                                else
                                    Brush.horizontalGradient(listOf(GameVibrantSlotsLight, GameVibrantSlotsLight))
                            )
                            .bounceClick { if (isIdle) viewModel.spinSlots() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isSpinning) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.5.dp,
                                    color = Color(0xFFB5510B)
                                )
                                Text(
                                    stringResource(R.string.game_slots_spinning),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color(0xFFB5510B)
                                )
                            }
                        } else {
                            Text(
                                stringResource(R.string.btn_spin),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        if (uiState.miniGameState != MiniGameState.PLAYING) {
            Spacer(modifier = Modifier.height(20.dp))
            EntranceAnimation { GameResultOverlay(uiState.miniGameState, uiState.lastReward) }
            Spacer(modifier = Modifier.height(16.dp))
            GameActionButtons(
                accentColor = GamePastelPeach,
                onPlayAgain = {
                    viewModel.startGame(
                        GameType.SLOTS,
                        ignoreLevelLock = GAME_ROOM_LEVEL_LOCK_DISABLED,
                        ignoreEnergyLimit = GAME_ROOM_LEVEL_LOCK_DISABLED
                    )
                },
                onClose = { viewModel.closeMiniGame() }
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SpinningReel(
    targetEmoji: String,
    isSpinning: Boolean,
    delayMillis: Int,
    isJackpot: Boolean = false,
    modifier: Modifier = Modifier
) {
    val emojis = listOf("🍒", "🍋", "🍉", "🍇", "💎", "7️⃣", "🔔", "⭐")
    var currentEmoji by remember { mutableStateOf(targetEmoji) }
    var internalIsSpinning by remember { mutableStateOf(false) }

    LaunchedEffect(isSpinning) {
        if (isSpinning) {
            internalIsSpinning = true
            while (internalIsSpinning) {
                currentEmoji = emojis.random()
                kotlinx.coroutines.delay(80)
            }
        } else {
            kotlinx.coroutines.delay(delayMillis.toLong())
            internalIsSpinning = false
            currentEmoji = targetEmoji
        }
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (internalIsSpinning) GameVibrantSlots.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.92f),
        modifier = modifier
            .let { m -> if (isJackpot) m.glowPulse(GamePastelYellow, 0.4f, 0.85f) else m },
        border = BorderStroke(
            2.5.dp,
            if (isJackpot) GamePastelYellow
            else if (internalIsSpinning) GameVibrantSlots
            else GameVibrantSlotsLight.copy(alpha = 0.5f)
        ),
        shadowElevation = if (internalIsSpinning) 12.dp else if (isJackpot) 16.dp else 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            androidx.compose.animation.AnimatedContent(
                targetState = currentEmoji,
                transitionSpec = {
                    if (internalIsSpinning) {
                        (androidx.compose.animation.slideInVertically { it } +
                            androidx.compose.animation.fadeIn(tween(40))) with
                            (androidx.compose.animation.slideOutVertically { -it } +
                                androidx.compose.animation.fadeOut(tween(40)))
                    } else {
                        (androidx.compose.animation.scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) +
                            androidx.compose.animation.fadeIn()) with
                            androidx.compose.animation.fadeOut(tween(80))
                    }
                },
                label = "reel_emoji"
            ) { emoji ->
                Text(emoji, fontSize = 36.sp)
            }
        }
    }
}


// ════════════════════════════════════════════════════════════════════
//  MEMORY GAME
// ════════════════════════════════════════════════════════════════════

@Composable
fun MemoryGame(
    uiState: GameUiState,
    viewModel: CatViewModel
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🧠", fontSize = 38.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.game_memory_header),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1B6B3A)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            GameBadge(
                stringResource(R.string.game_memory_moves, uiState.memoryMoves),
                GamePastelBlue,
                Color(0xFF1565C0)
            )
            GameBadge(
                stringResource(R.string.game_memory_pairs, uiState.memoryMatchedPairs),
                GamePastelMint,
                Color(0xFF1B6B3A)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.miniGameState == MiniGameState.PRE_GAME -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    color = GamePastelMintLight.copy(alpha = 0.7f),
                    border = BorderStroke(1.5.dp, GamePastelMint.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "🐱❓🐱",
                            fontSize = 48.sp,
                            modifier = Modifier.pulsate()
                        )
                        Text(
                            stringResource(R.string.game_msg_match_two),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF1B6B3A),
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.startMemoryGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = GamePastelMint),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(50.dp)
                                .bounceClick { viewModel.startMemoryGame() }
                        ) {
                            Text(
                                stringResource(R.string.btn_start_game),
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1B6B3A)
                            )
                        }
                    }
                }
            }
            uiState.miniGameState == MiniGameState.PLAYING -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.memoryCards.size) { index ->
                        val card = uiState.memoryCards[index]
                        FlipCard(
                            card = card,
                            isWrong = index in uiState.memoryMismatchIndices,
                            onClick = {
                                if (!card.isFlipped && !card.isMatched && uiState.memoryFlippedIndices.size < 2) {
                                    viewModel.flipMemoryCard(index)
                                }
                            }
                        )
                    }
                }
                // Pairs progress bar
                val totalPairs = uiState.memoryCards.size / 2
                val matchedProgress by animateFloatAsState(
                    targetValue = if (totalPairs > 0) uiState.memoryMatchedPairs.toFloat() / totalPairs else 0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "pairs_progress"
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { matchedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(50.dp)),
                    color = GameVibrantMemory,
                    trackColor = GamePastelMintLight.copy(alpha = 0.5f)
                )
            }
            else -> {
                EntranceAnimation { GameResultOverlay(uiState.miniGameState, uiState.lastReward) }
                Spacer(modifier = Modifier.height(16.dp))
                GameActionButtons(
                    accentColor = GamePastelMint,
                    onPlayAgain = {
                        viewModel.startGame(
                            GameType.MEMORY,
                            ignoreLevelLock = GAME_ROOM_LEVEL_LOCK_DISABLED,
                            ignoreEnergyLimit = GAME_ROOM_LEVEL_LOCK_DISABLED
                        )
                    },
                    onClose = { viewModel.closeMiniGame() }
                )
            }
        }
    }
}

@Composable
fun FlipCard(
    card: com.mert.paticat.ui.screens.cat.MemoryCard,
    isWrong: Boolean = false,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped || card.isMatched) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_flip"
    )
    val isBackVisible = rotation <= 90f

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = !card.isFlipped && !card.isMatched) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isBackVisible) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(18.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(GamePastelMint, GameVibrantMemory.copy(alpha = 0.7f))
                            ),
                            RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐾", fontSize = 24.sp, modifier = Modifier.pulsate(scaleRange = 0.92f..1.08f, duration = 1200))
                }
            }
        } else {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f },
                shape = RoundedCornerShape(18.dp),
                color = when {
                    card.isMatched -> GamePastelMint.copy(alpha = 0.4f)
                    isWrong        -> Color(0xFFFFCDD2)
                    else           -> Color.White.copy(alpha = 0.85f)
                },
                border = BorderStroke(
                    2.dp,
                    when {
                        card.isMatched -> GamePastelMint
                        isWrong        -> Color(0xFFE57373)
                        else           -> GamePastelMintLight
                    }
                ),
                shadowElevation = if (card.isMatched) 0.dp else 3.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(card.emoji, fontSize = 24.sp)
                    if (card.isMatched) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = GamePastelYellow,
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.TopEnd)
                                .padding(2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  REFLEX GAME
// ════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ReflexGame(
    uiState: GameUiState,
    viewModel: CatViewModel
) {
    val roundTimerProgress = remember { Animatable(1f) }

    LaunchedEffect(uiState.reflexRound, uiState.reflexIsWaiting) {
        roundTimerProgress.snapTo(1f)
        if (uiState.miniGameState == MiniGameState.PLAYING && !uiState.reflexIsWaiting && uiState.reflexRound > 0) {
            val timeoutMs = (2000L - (uiState.reflexRound * 100L)).coerceAtLeast(800L)
            roundTimerProgress.animateTo(0f, animationSpec = tween(timeoutMs.toInt(), easing = LinearEasing))
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("⚡", fontSize = 38.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.game_reflex_header),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF5B2D8E)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            GameBadge(
                stringResource(R.string.game_reflex_score, uiState.reflexScore),
                GamePastelYellow,
                Color(0xFF7A5C00)
            )
            GameBadge(
                stringResource(R.string.game_reflex_round, uiState.reflexRound, uiState.reflexMaxRounds),
                GamePastelLavender,
                Color(0xFF5B2D8E)
            )
        }

        if (uiState.miniGameState == MiniGameState.PLAYING && !uiState.reflexIsWaiting && uiState.reflexRound > 0) {
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { roundTimerProgress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50.dp)),
                color = when {
                    roundTimerProgress.value > 0.5f -> GameVibrantReflex
                    roundTimerProgress.value > 0.25f -> GameVibrantSlotsLight
                    else -> Color(0xFFEF5350)
                },
                trackColor = GamePastelLavLight.copy(alpha = 0.4f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.miniGameState == MiniGameState.PRE_GAME -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    color = GamePastelLavLight.copy(alpha = 0.7f),
                    border = BorderStroke(1.5.dp, GamePastelLavender.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "⚡🐾",
                            fontSize = 48.sp,
                            modifier = Modifier.pulsate()
                        )
                        Text(
                            stringResource(R.string.game_reflex_ready),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF5B2D8E),
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.startReflexGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = GamePastelLavender),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(50.dp)
                                .bounceClick { viewModel.startReflexGame() }
                        ) {
                            Text(
                                stringResource(R.string.btn_start_game),
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF5B2D8E)
                            )
                        }
                    }
                }
            }
            uiState.miniGameState == MiniGameState.PLAYING -> {
                LaunchedEffect(uiState.miniGameState) {
                    if (uiState.reflexTargets.isEmpty() && uiState.reflexRound == 0) {
                        kotlinx.coroutines.delay(1000)
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(32.dp),
                    color = GamePastelLavLight.copy(alpha = 0.6f),
                    border = BorderStroke(1.5.dp, GamePastelLavender.copy(alpha = 0.4f))
                ) {
                    Box {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (row in 0..3) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    for (col in 0..3) {
                                        val target = uiState.reflexTargets.find {
                                            it.row == row && it.col == col && it.isVisible
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Empty cell
                                            Surface(
                                                modifier = Modifier.fillMaxSize(),
                                                shape = RoundedCornerShape(16.dp),
                                                color = GamePastelLavender.copy(alpha = 0.15f)
                                            ) {}

                                            // Target with super-bouncy spring
                                            androidx.compose.animation.AnimatedVisibility(
                                                visible = target != null,
                                                enter = scaleIn(
                                                    spring(
                                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                                        stiffness = Spring.StiffnessLow
                                                    )
                                                ) + fadeIn(tween(80)),
                                                exit = scaleOut(tween(120)) + fadeOut(tween(80))
                                            ) {
                                                target?.let { t ->
                                                    val targetColor = when {
                                                        uiState.reflexRound >= 8 -> Color(0xFFEF5350) // red
                                                        uiState.reflexRound >= 5 -> Color(0xFFFF9800) // orange
                                                        else -> GamePastelLavender // lavender
                                                    }
                                                    val targetBorderColor = when {
                                                        uiState.reflexRound >= 8 -> Color(0xFFFFCDD2)
                                                        uiState.reflexRound >= 5 -> Color(0xFFFFE0B2)
                                                        else -> GamePastelLavLight
                                                    }
                                                    Surface(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .bounceClick { viewModel.tapReflexTarget(t.id) },
                                                        shape = RoundedCornerShape(16.dp),
                                                        color = targetColor,
                                                        shadowElevation = 8.dp,
                                                        border = BorderStroke(2.dp, targetBorderColor)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(t.emoji, fontSize = 24.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Waiting overlay
                        if (uiState.reflexIsWaiting) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(GamePastelLavLight.copy(alpha = 0.75f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "⚡",
                                        fontSize = 48.sp,
                                        modifier = Modifier.pulsate()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        stringResource(R.string.game_reflex_ready),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF5B2D8E)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                EntranceAnimation { GameResultOverlay(uiState.miniGameState, uiState.lastReward) }
                Spacer(modifier = Modifier.height(16.dp))
                GameActionButtons(
                    accentColor = GamePastelLavender,
                    onPlayAgain = {
                        viewModel.startGame(
                            GameType.REFLEX,
                            ignoreLevelLock = GAME_ROOM_LEVEL_LOCK_DISABLED,
                            ignoreEnergyLimit = GAME_ROOM_LEVEL_LOCK_DISABLED
                        )
                    },
                    onClose = { viewModel.closeMiniGame() }
                )
             }
         }
     }
 }

 // ════════════════════════════════════════════════════════════════════
 //  CATCH GAME
 // ════════════════════════════════════════════════════════════════════

 private data class FallingItem(
     val id: Int,
     val iconRes: Int,
     val isBomb: Boolean,
     val x: Float,          // 0f–1f of arena width
     var y: Float,          // 0f–1f of arena height
     val speed: Float       // fraction of height per second
 )

 @Composable
 fun CatchGame(
     uiState: GameUiState,
     viewModel: CatViewModel
 ) {
     val catchBlue   = Color(0xFF42A5F5)
     val catchBlueDk = Color(0xFF0D47A1)
     val catchBlueLt = Color(0xFFBBDEFB)

     when {
         // ── PRE-GAME ──
         uiState.miniGameState == MiniGameState.PRE_GAME -> {
             Column(horizontalAlignment = Alignment.CenterHorizontally) {
                 Icon(
                     painter = painterResource(id = R.drawable.ic_game_basket),
                     contentDescription = null,
                     modifier = Modifier.size(48.dp),
                     tint = Color.Unspecified
                 )
                 Spacer(Modifier.height(4.dp))
                 Text(
                     stringResource(R.string.game_catch_pre_title),
                     style = MaterialTheme.typography.titleMedium,
                     fontWeight = FontWeight.ExtraBold,
                     color = catchBlueDk
                 )
                 Spacer(Modifier.height(16.dp))
                 Surface(
                     modifier = Modifier.fillMaxWidth(),
                     shape = RoundedCornerShape(32.dp),
                     color = catchBlueLt.copy(alpha = 0.7f),
                     border = BorderStroke(1.5.dp, catchBlue.copy(alpha = 0.5f))
                 ) {
                     Column(
                         modifier = Modifier.padding(28.dp),
                         horizontalAlignment = Alignment.CenterHorizontally,
                         verticalArrangement = Arrangement.spacedBy(12.dp)
                     ) {
                         Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                             Icon(painterResource(R.drawable.ic_game_apple), null, Modifier.size(28.dp), tint = Color.Unspecified)
                             Icon(painterResource(R.drawable.ic_game_fish), null, Modifier.size(28.dp), tint = Color.Unspecified)
                             Icon(painterResource(R.drawable.ic_game_yarn), null, Modifier.size(28.dp), tint = Color.Unspecified)
                             Icon(painterResource(R.drawable.ic_game_flower), null, Modifier.size(28.dp), tint = Color.Unspecified)
                         }
                         Text(
                             stringResource(R.string.game_catch_instruction),
                             style = MaterialTheme.typography.bodyMedium,
                             color = catchBlueDk,
                             textAlign = TextAlign.Center
                         )
                         Surface(
                             color = catchBlue.copy(alpha = 0.15f),
                             shape = RoundedCornerShape(50.dp),
                             border = BorderStroke(1.dp, catchBlue.copy(alpha = 0.4f))
                         ) {
                             Text(
                                 stringResource(R.string.game_catch_tip),
                                 modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                 style = MaterialTheme.typography.labelSmall,
                                 fontWeight = FontWeight.Bold,
                                 color = catchBlueDk
                             )
                         }
                         Button(
                             onClick = { viewModel.startCatchGame() },
                             colors = ButtonDefaults.buttonColors(containerColor = catchBlue),
                             shape = RoundedCornerShape(50.dp),
                             modifier = Modifier.fillMaxWidth(0.6f).height(50.dp)
                         ) {
                             Text(stringResource(R.string.btn_start_game), fontWeight = FontWeight.ExtraBold, color = Color.White)
                         }
                     }
                 }
             }
         }

         // ── PLAYING ──
         uiState.miniGameState == MiniGameState.PLAYING -> {
             CatchGameArena(
                 onGameOver = { score -> viewModel.finishCatchGame(score) }
             )
         }

         // ── RESULT ──
         else -> {
             EntranceAnimation { GameResultOverlay(uiState.miniGameState, uiState.lastReward) }
             Spacer(Modifier.height(16.dp))
             GameActionButtons(
                 accentColor = catchBlue,
                onPlayAgain = {
                    viewModel.startGame(
                        GameType.CATCH,
                        ignoreLevelLock = GAME_ROOM_LEVEL_LOCK_DISABLED,
                        ignoreEnergyLimit = GAME_ROOM_LEVEL_LOCK_DISABLED
                    )
                },
                 onClose     = { viewModel.closeMiniGame() }
             )
         }
     }
 }

 /** The actual gameplay arena – runs a pure-Compose 60fps game loop. */
 @OptIn(ExperimentalAnimationApi::class)
 @Composable
 private fun CatchGameArena(onGameOver: (score: Int) -> Unit) {
     val catchBlue   = Color(0xFF42A5F5)
     val catchBlueDk = Color(0xFF0D47A1)
     val haptic      = LocalHapticFeedback.current

     var score   by remember { mutableStateOf(0) }
     var lives   by remember { mutableStateOf(3) }
     var running by remember { mutableStateOf(true) }
     var basketX by remember { mutableStateOf(0.5f) }
     var items      by remember { mutableStateOf(listOf<FallingItem>()) }
     var nextItemId by remember { mutableStateOf(0) }
     var arenaSize  by remember { mutableStateOf(IntSize.Zero) }
     var elapsedSeconds by remember { mutableStateOf(0f) }

     val goodEmojis = remember { listOf(R.drawable.ic_game_apple, R.drawable.ic_game_fish, R.drawable.ic_game_yarn, R.drawable.ic_game_flower) }

     // ── Game loop ──
     LaunchedEffect(running) {
         if (!running) return@LaunchedEffect
         var lastFrameNanos = System.nanoTime()
         var spawnTimer = 0f
         var elapsed = 0f

         while (running) {
             val nowNanos = withFrameNanos { it }
             val dt = ((nowNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.1f)
             lastFrameNanos = nowNanos
             elapsed += dt
             elapsedSeconds = elapsed

             // Spawn items
             spawnTimer -= dt
             if (spawnTimer <= 0f) {
                 val difficulty = (1f + elapsed / 20f).coerceAtMost(2.5f)
                 val speed = (0.18f + kotlin.random.Random.nextFloat() * 0.14f) * difficulty
                 val isBomb = kotlin.random.Random.nextFloat() < (0.25f + elapsed / 120f).coerceAtMost(0.42f)
                 items = items + FallingItem(
                     id    = nextItemId++,
                     iconRes = if (isBomb) R.drawable.ic_game_bomb else goodEmojis.random(),
                     isBomb = isBomb,
                     x     = 0.06f + kotlin.random.Random.nextFloat() * 0.88f,
                     y     = -0.05f,
                     speed = speed
                 )
                 spawnTimer = (0.9f - elapsed / 60f).coerceAtLeast(0.32f)
             }

             // Move + collision
             val basketW = 0.16f
             val basketY = 0.87f
             val newItems = mutableListOf<FallingItem>()
             var livesLost = 0

             for (item in items) {
                 val ny = item.y + item.speed * dt
                 val inBasketX = item.x >= basketX - basketW / 2 && item.x <= basketX + basketW / 2
                 val inBasketY = ny >= basketY - 0.06f && ny <= basketY + 0.12f
                 when {
                     inBasketX && inBasketY -> {
                         haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                         if (item.isBomb) livesLost++ else score++
                     }
                     ny > 1.12f -> { /* Yere düşürünce can gitmez */ }
                     else -> newItems += item.copy(y = ny)
                 }
             }

             items = newItems
             lives = (lives - livesLost).coerceAtLeast(0)

             if (lives <= 0 || elapsed >= 30f) {
                 running = false
                 onGameOver(score)
             }
         }
     }

     Column(horizontalAlignment = Alignment.CenterHorizontally) {
         // HUD
         Row(
             modifier = Modifier.fillMaxWidth(),
             horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
         ) {
             // Animated score badge
             Surface(
                 color = catchBlue.copy(alpha = 0.25f),
                 shape = RoundedCornerShape(50.dp),
                 border = BorderStroke(1.dp, catchBlue.copy(alpha = 0.4f))
             ) {
                 Row(
                     modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                     verticalAlignment = Alignment.CenterVertically,
                     horizontalArrangement = Arrangement.spacedBy(4.dp)
                 ) {
                     Icon(Icons.Filled.TrackChanges, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0D47A1))
                     androidx.compose.animation.AnimatedContent(
                         targetState = score,
                         transitionSpec = {
                             (slideInVertically { -it } + fadeIn(tween(150))) with
                                 (slideOutVertically { it } + fadeOut(tween(100)))
                         },
                         label = "score_anim"
                     ) { s ->
                         Text(
                             "$s",
                             style = MaterialTheme.typography.labelSmall,
                             fontWeight = FontWeight.ExtraBold,
                             color = catchBlueDk
                         )
                     }
                 }
             }
             val heartsText = "❤️".repeat(lives.coerceIn(0, 3))
             GameBadge(heartsText.ifEmpty { "💀" }, Color(0xFFEF9A9A), Color(0xFFB71C1C))
         }

         // 30s timer bar
         val timerProgress = (1f - elapsedSeconds / 30f).coerceIn(0f, 1f)
         Spacer(Modifier.height(6.dp))
         LinearProgressIndicator(
             progress = { timerProgress },
             modifier = Modifier
                 .fillMaxWidth()
                 .height(5.dp)
                 .clip(RoundedCornerShape(50.dp)),
             color = when {
                 timerProgress > 0.5f -> Color(0xFF66BB6A)
                 timerProgress > 0.25f -> Color(0xFFFF9800)
                 else -> Color(0xFFEF5350)
             },
             trackColor = catchBlue.copy(alpha = 0.2f)
         )

         Spacer(Modifier.height(8.dp))

         // Arena
         Box(
             modifier = Modifier
                 .fillMaxWidth()
                 .aspectRatio(0.85f)
                 .clip(RoundedCornerShape(28.dp))
                 .background(
                     Brush.verticalGradient(
                         listOf(Color(0xFF1A237E), Color(0xFF1565C0), Color(0xFF42A5F5))
                     )
                 )
                 .onSizeChanged { arenaSize = it }
                 .pointerInput(Unit) {
                     detectHorizontalDragGestures { _, dragAmount ->
                         if (arenaSize.width > 0) {
                             basketX = (basketX + dragAmount / arenaSize.width).coerceIn(0.13f, 0.87f)
                         }
                     }
                 }
         ) {
             // Decorative background stars
             repeat(10) { i ->
                 val sx = remember(i) { kotlin.random.Random.nextFloat() }
                 val sy = remember(i) { kotlin.random.Random.nextFloat() * 0.80f }
                 Box(
                     Modifier
                         .fillMaxSize()
                         .graphicsLayer {
                             translationX = sx * size.width
                             translationY = sy * size.height
                         }
                 ) {
                     Text("✦", fontSize = (7 + i % 5).sp, color = Color.White.copy(alpha = 0.25f))
                 }
             }

             // Falling items
             items.forEach { item ->
                 Box(
                     Modifier
                         .fillMaxSize()
                         .graphicsLayer {
                             translationX = item.x * size.width - 20.dp.toPx()
                             translationY = item.y * size.height - 20.dp.toPx()
                         }
                 ) {
                     Icon(
                         painter = painterResource(id = item.iconRes),
                         contentDescription = null,
                         modifier = Modifier.size(36.dp),
                         tint = Color.Unspecified
                     )
                 }
             }

             // Basket
             Box(
                 Modifier
                     .fillMaxSize()
                     .graphicsLayer {
                         // Center the emoji (48sp ~ 48dp -> offset by ~24dp)
                         translationX = basketX * size.width - 24.dp.toPx()
                         translationY = 0.82f * size.height
                     }
             ) {
                 Icon(
                     painter = painterResource(id = R.drawable.ic_game_basket),
                     contentDescription = null,
                     modifier = Modifier.size(48.dp),
                     tint = Color.Unspecified
                 )
             }
         }

         Spacer(Modifier.height(8.dp))
         Text(
             stringResource(R.string.game_catch_hint),
             style = MaterialTheme.typography.labelSmall,
             color = catchBlueDk.copy(alpha = 0.6f),
             fontWeight = FontWeight.SemiBold
         )
     }
 }

// ════════════════════════════════════════════════════════════════════
//  PRE-GAME OVERLAY  – shown when a game card is tapped
// ════════════════════════════════════════════════════════════════════

private data class GamePreviewConfig(
    val emoji: String,
    val title: String,
    val description: String,
    val rewardHint: String,
    val gradientTop: Color,
    val gradientBottom: Color,
    val textColor: Color,
    val buttonColor: Color
)

@Composable
private fun previewConfigFor(gameType: GameType): GamePreviewConfig = when (gameType) {
    GameType.RPS -> GamePreviewConfig(
        emoji = "✊", title = stringResource(R.string.game_rps_title),
        description = stringResource(R.string.game_preview_rps_desc),
        rewardHint = stringResource(R.string.game_reward_rps),
        gradientTop = GameVibrantRPS, gradientBottom = GameVibrantRPSLight,
        textColor = Color.White, buttonColor = Color(0xFFB5003A)
    )
    GameType.SLOTS -> GamePreviewConfig(
        emoji = "🎰", title = stringResource(R.string.game_slots_title),
        description = stringResource(R.string.game_preview_slots_desc),
        rewardHint = stringResource(R.string.game_reward_slots),
        gradientTop = GameVibrantSlots, gradientBottom = GameVibrantSlotsLight,
        textColor = Color.White, buttonColor = Color(0xFFBF3600)
    )
    GameType.MEMORY -> GamePreviewConfig(
        emoji = "🧠", title = stringResource(R.string.game_memory_title),
        description = stringResource(R.string.game_preview_memory_desc),
        rewardHint = stringResource(R.string.game_reward_memory),
        gradientTop = GameVibrantMemory, gradientBottom = GameVibrantMemoryLight,
        textColor = Color.White, buttonColor = Color(0xFF00796B)
    )
    GameType.REFLEX -> GamePreviewConfig(
        emoji = "⚡", title = stringResource(R.string.game_reflex_title),
        description = stringResource(R.string.game_preview_reflex_desc),
        rewardHint = stringResource(R.string.game_reward_reflex),
        gradientTop = GameVibrantReflex, gradientBottom = GameVibrantReflexLight,
        textColor = Color.White, buttonColor = Color(0xFF4A1DB5)
    )
    GameType.CATCH -> GamePreviewConfig(
        emoji = "🧺", title = stringResource(R.string.game_catch_title),
        description = stringResource(R.string.game_catch_instruction),
        rewardHint = stringResource(R.string.game_reward_catch),
        gradientTop = GameVibrantCatch, gradientBottom = GameVibrantCatchLight,
        textColor = Color.White, buttonColor = Color(0xFF015F8A)
    )
}

@Composable
private fun PreGameOverlay(
    gameType: GameType,
    catLevel: Int,
    catEnergy: Int,
    onPlay: () -> Unit,
    onDismiss: () -> Unit
) {
    val cfg = previewConfigFor(gameType)
    val isLocked  = catLevel < gameType.minLevel
    val hasEnergy = GAME_ROOM_LEVEL_LOCK_DISABLED || catEnergy >= gameType.energyCost
    val canPlay   = !isLocked && hasEnergy

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
            ) { it / 2 } + fadeIn(tween(220))
        ) {
            Card(
                shape = RoundedCornerShape(36.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 32.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(listOf(cfg.gradientTop, cfg.gradientBottom)),
                            RoundedCornerShape(36.dp)
                        )
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Animated big emoji
                    var emojiVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(120)
                        emojiVisible = true
                    }
                    AnimatedVisibility(
                        visible = emojiVisible,
                        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioLowBouncy)) + fadeIn()
                    ) {
                        Text(cfg.emoji, fontSize = 72.sp)
                    }

                    Text(
                        cfg.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = cfg.textColor,
                        textAlign = TextAlign.Center
                    )

                    // Description card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.22f)
                    ) {
                        Text(
                            cfg.description,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = cfg.textColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text(
                                "⚡ ${gameType.energyCost}",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (!hasEnergy && !isLocked) Color(0xFFFFCDD2) else cfg.textColor
                            )
                        }
                        Surface(
                            color = Color.White.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text(
                                "🏆 ${cfg.rewardHint}",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = cfg.textColor
                            )
                        }
                    }

                    // Lock / low-energy warning
                    if (!canPlay) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Black.copy(alpha = 0.18f)
                        ) {
                            Text(
                                if (isLocked) stringResource(R.string.game_pregame_locked, gameType.minLevel)
                                else          stringResource(R.string.game_pregame_low_energy, catEnergy, gameType.energyCost),
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = cfg.textColor.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onPlay,
                        enabled = canPlay,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cfg.buttonColor,
                            disabledContainerColor = cfg.buttonColor.copy(alpha = 0.45f)
                        ),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .bounceClick { if (canPlay) onPlay() }
                    ) {
                        Text(
                            stringResource(R.string.btn_play_game),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            stringResource(R.string.btn_dismiss),
                            color = cfg.textColor.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
