@file:OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
// Lobby + per-card UI for the Games screen. Per-game gameplay lives in
// RpsGame.kt / SlotsGame.kt / MemoryGame.kt / ReflexGame.kt / CatchGame.kt.
// Pre-game overlay lives in PreviewConfig.kt. Shared composables in GamesShared.kt.
package com.mert.paticat.ui.screens.games

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mert.paticat.R
import com.mert.paticat.ui.components.NativeAdCard
import com.mert.paticat.ui.components.glowPulse
import com.mert.paticat.ui.components.marshmallow.ChipPill
import com.mert.paticat.ui.components.marshmallow.MarshmallowSpring
import com.mert.paticat.ui.components.marshmallow.PillowCard
import com.mert.paticat.ui.components.marshmallow.TintedPillowCard
import com.mert.paticat.ui.components.marshmallow.softEntrance
import com.mert.paticat.ui.components.shimmerEffect
import com.mert.paticat.ui.screens.cat.CatViewModel
import com.mert.paticat.ui.screens.cat.GameType
import com.mert.paticat.ui.screens.cat.MiniGameState
import com.mert.paticat.ui.theme.GameVibrantCatch
import com.mert.paticat.ui.theme.GameVibrantMemory
import com.mert.paticat.ui.theme.GameVibrantRPS
import com.mert.paticat.ui.theme.GameVibrantRPSDark
import com.mert.paticat.ui.theme.GameVibrantReflex
import com.mert.paticat.ui.theme.GameVibrantSlots

// ════════════════════════════════════════════════════════════════════
//  GAMES SCREEN — Marshmallow lobby
// ════════════════════════════════════════════════════════════════════

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
    var showQuitConfirmDialog by remember { mutableStateOf(false) }

    val effectiveCatLevel = if (GAME_ROOM_LEVEL_LOCK_DISABLED) Int.MAX_VALUE else uiState.cat.level

    // G9: Intercept system back-press while a mini-game is active. Confirm quit (energy already spent).
    BackHandler(enabled = gameState.activeGame != null) {
        val s = gameState.miniGameState
        val isResult = s == MiniGameState.RESULT_WIN ||
            s == MiniGameState.RESULT_LOSE ||
            s == MiniGameState.RESULT_DRAW
        if (isResult) {
            catViewModel.closeMiniGame()
        } else {
            showQuitConfirmDialog = true
        }
    }

    if (showQuitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showQuitConfirmDialog = false },
            title = { Text(stringResource(R.string.game_quit_confirm_title)) },
            text = { Text(stringResource(R.string.game_quit_confirm_message)) },
            confirmButton = {
                Button(onClick = {
                    showQuitConfirmDialog = false
                    catViewModel.closeMiniGame()
                }) { Text(stringResource(R.string.game_quit_confirm_yes)) }
            },
            dismissButton = {
                TextButton(onClick = { showQuitConfirmDialog = false }) {
                    Text(stringResource(R.string.game_quit_confirm_no))
                }
            },
        )
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            catViewModel.clearMessage()
        }
    }

    // Stale RESULT cleanup: CatViewModel survives tab/back navigation, so a
    // previously finished game can leave miniGameState in RESULT_* with
    // activeGame set. Without this, GamesScreen would re-enter and pop a
    // GameDialog stuck on the "Play Again" UI.
    LaunchedEffect(Unit) {
        val s = gameState.miniGameState
        val isStaleResult = gameState.activeGame != null && (
            s == MiniGameState.RESULT_WIN ||
                s == MiniGameState.RESULT_LOSE ||
                s == MiniGameState.RESULT_DRAW
            )
        if (isStaleResult) catViewModel.closeMiniGame()
    }

    if (gameState.activeGame != null) {
        GameDialog(
            activeGame = gameState.activeGame!!,
            onDismiss = {
                // G1: Don't drop reward state on outside-tap. Result must be acknowledged.
                val s = gameState.miniGameState
                val isResult = s == MiniGameState.RESULT_WIN ||
                    s == MiniGameState.RESULT_LOSE ||
                    s == MiniGameState.RESULT_DRAW
                if (!isResult) {
                    catViewModel.closeMiniGame()
                }
            }
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

    LaunchedEffect(gameState.activeGame) {
        if (gameState.activeGame != null) previewGame = null
    }

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
            item(span = { GridItemSpan(2) }) {
                GamesScreenHeader(
                    catEnergy = uiState.cat.energy,
                    onBackClick = onBackClick
                )
            }

            // Data-driven lobby cards. See LOBBY_GAME_LIST in GameConstants.kt.
            itemsIndexed(
                items = LOBBY_GAME_LIST,
                key = { _, entry -> entry.id.name }
            ) { idx, entry ->
                val staggerDelay = GAME_CARD_STAGGER_DELAYS_MS.getOrElse(idx) { 0L }.toInt()
                GameCard(
                    delayMs = staggerDelay,
                    emoji = entry.emoji,
                    title = stringResource(entry.titleResId),
                    description = stringResource(entry.descResId),
                    rewardHint = stringResource(entry.rewardResId),
                    energyCost = entry.id.energyCost,
                    catEnergy = uiState.cat.energy,
                    catLevel = effectiveCatLevel,
                    minLevel = entry.id.minLevel,
                    gradientColors = entry.gradient,
                    accentColor = entry.accent,
                    onClick = { previewGame = entry.id }
                )
            }

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
//  GAME DIALOG — soft pillow shell around the per-game body
// ════════════════════════════════════════════════════════════════════

@Composable
private fun GameDialog(
    activeGame: GameType,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val tint = when (activeGame) {
        GameType.RPS    -> GameVibrantRPS
        GameType.SLOTS  -> GameVibrantSlots
        GameType.MEMORY -> GameVibrantMemory
        GameType.REFLEX -> GameVibrantReflex
        GameType.CATCH  -> GameVibrantCatch
        else            -> MaterialTheme.colorScheme.primary
    }
    Dialog(
        onDismissRequest = onDismiss,
        // G1: Block back-press / outside-tap from dismissing (esp. result state). BackHandler in screen handles it.
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        )
    ) {
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(MarshmallowSpring.PrimaryEnter, initialScale = 0.92f) + fadeIn()
        ) {
            PillowCard(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 28.dp)
                    .fillMaxWidth(0.96f),
                shape = RoundedCornerShape(36.dp),
                contentPadding = 22.dp,
                backgroundColor = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, tint.copy(alpha = 0.18f)),
                elevation = 22.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { content() }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  HEADER — pillow banner with floating sparkles
// ════════════════════════════════════════════════════════════════════

@Composable
private fun GamesScreenHeader(
    catEnergy: Int,
    onBackClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "header_float")
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
        TintedPillowCard(
            gradient = listOf(
                Color(0xFFFFE0EC),
                Color(0xFFFFD3E0),
                Color(0xFFFFEAD9)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp),
            shape = RoundedCornerShape(32.dp),
            contentPadding = 0.dp,
            elevation = 14.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val sparklePositions = listOf(
                    Triple(0.10f, 14.dp, 0f),
                    Triple(0.28f, 30.dp, 1.05f),
                    Triple(0.48f, 12.dp, 2.09f),
                    Triple(0.62f, 32.dp, 3.14f),
                    Triple(0.76f, 16.dp, 4.19f),
                    Triple(0.88f, 28.dp, 5.24f)
                )
                sparklePositions.forEachIndexed { i, (xFrac, topPad, phaseOffset) ->
                    val dy = kotlin.math.sin(phase + phaseOffset) * 6f
                    Text(
                        "✨",
                        fontSize = (10 + i % 3 * 3).sp,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = (xFrac * 320).dp, top = topPad)
                            .graphicsLayer { translationY = dy }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back),
                            tint = GameVibrantRPSDark
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.games_title),
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge,
                            color = GameVibrantRPSDark
                        )
                        Text(
                            stringResource(R.string.games_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = GameVibrantRPSDark.copy(alpha = 0.62f)
                        )
                    }
                    ChipPill(
                        text = "$catEnergy",
                        leadingEmoji = "⚡",
                        backgroundColor = Color.White.copy(alpha = 0.65f),
                        contentColor = GameVibrantRPSDark,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  GAME CARD — squircle tinted pillow with float emoji
// ════════════════════════════════════════════════════════════════════

/**
 * Squircle pillow card representing one mini-game on the lobby grid.
 *
 * Visual states:
 *  - Locked (catLevel < minLevel): card is dimmed and a 🔒 overlay covers it.
 *  - Low energy (energy < cost):   card stays interactive but the cost chip warns.
 *  - Enabled:                      glow pulses + bounce-click animates onClick.
 *
 * @param delayMs        Stagger delay (ms) for the soft entrance animation.
 * @param emoji          Floating emoji rendered inside the white circle.
 * @param title          Localized card title.
 * @param description    Short pitch (max 2 lines).
 * @param rewardHint     Localized reward summary (e.g. "+5–25 🪙").
 * @param energyCost     Cost paid on tap; sourced from GameType.energyCost.
 * @param catEnergy      Current cat energy (clamped 0..max).
 * @param catLevel       Effective cat level (overridden by GAME_ROOM_LEVEL_LOCK_DISABLED).
 * @param minLevel       Required cat level to unlock this card.
 * @param gradientColors Two-stop gradient colors (top, light variant).
 * @param accentColor    Single accent used for glow + lock overlay.
 * @param onClick        Invoked when card is enabled and tapped.
 */
@Composable
private fun GameCard(
    delayMs: Int,
    emoji: String,
    title: String,
    description: String,
    rewardHint: String,
    energyCost: Int,
    catEnergy: Int,
    catLevel: Int,
    minLevel: Int,
    gradientColors: List<Color>,
    accentColor: Color,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isLocked = catLevel < minLevel
    val isEnabled = !isLocked && (GAME_ROOM_LEVEL_LOCK_DISABLED || catEnergy >= energyCost)
    val textColor = Color.White

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

    val deepBottomColor = gradientColors.getOrElse(1) { gradientColors[0] }.darken(0.10f)

    Box(
        modifier = Modifier
            .softEntrance(delayMillis = delayMs)
            .let { m -> if (isEnabled) m.glowPulse(accentColor, 0.20f, 0.50f) else m }
            .graphicsLayer { alpha = if (isEnabled) 1f else 0.65f }
    ) {
        TintedPillowCard(
            gradient = listOf(gradientColors[0], deepBottomColor),
            shape = RoundedCornerShape(28.dp),
            contentPadding = 16.dp,
            elevation = if (isEnabled) 14.dp else 4.dp,
            onClick = if (!isLocked) {
                {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = GAME_CARD_MIN_HEIGHT)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White.copy(alpha = 0.30f), CircleShape)
                        .shimmerEffect()
                        .graphicsLayer { translationY = floatOffset }
                ) {
                    Text(emoji, fontSize = 32.sp)
                }

                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor,
                    lineHeight = 20.sp
                )

                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.86f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White.copy(alpha = if (isEnabled) 0.30f else 0.18f),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(
                            if (isEnabled) "⚡ $energyCost" else "⚡ $catEnergy/$energyCost",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (!isEnabled && !isLocked) Color(0xFFFFEAEA) else textColor
                        )
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.24f),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(
                            "🏆 $rewardHint",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                val starsDesc = stringResource(R.string.cd_difficulty_stars, difficultyStars, 3)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    // G13: Talkback reads the whole row as one entity. Per-star descriptions are null.
                    modifier = Modifier.semantics(mergeDescendants = true) {
                        contentDescription = starsDesc
                    }
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
            }
        }

        if (isLocked) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.Black.copy(alpha = 0.42f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔒", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    ChipPill(
                        text = stringResource(R.string.game_unlock_level, minLevel),
                        backgroundColor = Color.White.copy(alpha = 0.85f),
                        contentColor = Color(0xFF4E342E)
                    )
                }
            }
        }
    }
}
