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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize

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
                GameType.RPS    -> Brush.verticalGradient(listOf(GamePastelPinkLight, Color.White))
                GameType.SLOTS  -> Brush.verticalGradient(listOf(GamePastelPeachLight, Color.White))
                GameType.MEMORY -> Brush.verticalGradient(listOf(GamePastelMintLight, Color.White))
                GameType.REFLEX -> Brush.verticalGradient(listOf(GamePastelLavLight, Color.White))
                GameType.CATCH  -> Brush.verticalGradient(listOf(Color(0xFFE3F2FD), Color.White))
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

    // ── Main Screen ──
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(bottom = 100.dp))
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.games_title),
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            stringResource(R.string.games_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Energy badge – pill shape
                    Surface(
                        color = GamePastelBlue.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚡", fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${uiState.cat.energy}",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color(0xFF1565C0)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
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
            contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
        ) {
            // ── Game Cards ──
            item {
                EntranceAnimation(delay = 0) {
                    GameCard(
                        emoji = "✊",
                        title = stringResource(R.string.game_rps_title),
                        description = stringResource(R.string.game_rps_desc),
                        energyCost = GameType.RPS.energyCost,
                        catEnergy = uiState.cat.energy,
                        catLevel = uiState.cat.level,
                        minLevel = GameType.RPS.minLevel,
                        gradientColors = listOf(GamePastelPinkLight, GamePastelPink),
                        accentColor = GamePastelPink,
                        textColor = Color(0xFFB5294E),
                        onClick = { catViewModel.startGame(GameType.RPS) }
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
                        catLevel = uiState.cat.level,
                        minLevel = GameType.SLOTS.minLevel,
                        gradientColors = listOf(GamePastelPeachLight, GamePastelPeach),
                        accentColor = GamePastelPeach,
                        textColor = Color(0xFFB5510B),
                        onClick = { catViewModel.startGame(GameType.SLOTS) }
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
                        catLevel = uiState.cat.level,
                        minLevel = GameType.MEMORY.minLevel,
                        gradientColors = listOf(GamePastelMintLight, GamePastelMint),
                        accentColor = GamePastelMint,
                        textColor = Color(0xFF1B6B3A),
                        onClick = { catViewModel.startGame(GameType.MEMORY) }
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
                        catLevel = uiState.cat.level,
                        minLevel = GameType.REFLEX.minLevel,
                        gradientColors = listOf(GamePastelLavLight, GamePastelLavender),
                        accentColor = GamePastelLavender,
                        textColor = Color(0xFF5B2D8E),
                        onClick = { catViewModel.startGame(GameType.REFLEX) }
                    )
                }
            }
            item {
                EntranceAnimation(delay = 320) {
                    GameCard(
                        emoji = "\uD83E\uDDF3",
                        title = "Yakala!",
                        description = "Sepeti kaydır, iyi şeyleri topla, bombalardan kaç!",
                        energyCost = GameType.CATCH.energyCost,
                        catEnergy = uiState.cat.energy,
                        catLevel = uiState.cat.level,
                        minLevel = GameType.CATCH.minLevel,
                        gradientColors = listOf(Color(0xFFE3F2FD), Color(0xFF90CAF9)),
                        accentColor = Color(0xFF42A5F5),
                        textColor = Color(0xFF0D47A1),
                        onClick = { catViewModel.startGame(GameType.CATCH) }
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
//  GAME CARD  –  Pastel gradient + floating emoji
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
    val isEnabled = !isLocked && catEnergy >= energyCost

    // Floating animation for the emoji icon
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = if (isLocked) 0.45f else if (isEnabled) 1f else 0.55f }
            .bounceClick {
                if (!isLocked) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            }
            .shadow(elevation = if (isEnabled) 6.dp else 2.dp, shape = RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(gradientColors),
                    shape = RoundedCornerShape(32.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Floating emoji in a pill container
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.55f),
                    shadowElevation = 2.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.graphicsLayer { translationY = floatOffset }
                    ) {
                        Text(if (isLocked) "🔒" else emoji, fontSize = 30.sp)
                    }
                }

                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )

                Text(
                    if (isLocked) "Seviye $minLevel'de Açılır" else description,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.7f),
                    lineHeight = 16.sp,
                    maxLines = 2
                )

                // Cost badge – pill shape (invisible when locked to preserve card height)
                Surface(
                    modifier = Modifier.alpha(if (isLocked) 0f else 1f),
                    color = if (isEnabled) Color.White.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Text(
                        if (isEnabled) "⚡ ${stringResource(R.string.cost_energy, energyCost)}"
                        else "⚡ $catEnergy/$energyCost",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) textColor else MaterialTheme.colorScheme.error
                    )
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
        MiniGameState.RESULT_WIN  -> GamePastelMint
        MiniGameState.RESULT_LOSE -> GamePastelPink
        else                      -> GamePastelBlue
    }
    val resultTextColor = when (gameState) {
        MiniGameState.RESULT_WIN  -> Color(0xFF1B6B3A)
        MiniGameState.RESULT_LOSE -> Color(0xFFB5294E)
        else                      -> Color(0xFF1565C0)
    }

    // Bouncy entrance for the emoji
    var emojiVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { emojiVisible = true }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        AnimatedVisibility(
            visible = emojiVisible,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)) + fadeIn()
        ) {
            Text(resultEmoji, fontSize = 56.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            resultText,
            style = MaterialTheme.typography.headlineSmall,
            color = resultTextColor,
            fontWeight = FontWeight.Black
        )

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
                                RewardPill("+${reward.gold} 🪙", GamePastelYellow, Color(0xFF7A5C00))
                            }
                        if (reward.happy > 0)
                            EntranceAnimation(delay = 200) {
                                RewardPill("+${reward.happy} 💖", GamePastelPink, Color(0xFFB5294E))
                            }
                        if (reward.xp > 0)
                            EntranceAnimation(delay = 300) {
                                RewardPill("+${reward.xp} ⭐", GamePastelBlue, Color(0xFF1565C0))
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
        color = bgColor.copy(alpha = 0.45f),
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
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onPlayAgain,
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            modifier = Modifier
                .weight(1.3f)
                .height(52.dp)
                .bounceClick { onPlayAgain() },
            shape = RoundedCornerShape(50.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.btn_play_again),
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
        OutlinedButton(
            onClick = onClose,
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .bounceClick { onClose() },
            shape = RoundedCornerShape(50.dp),
            border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.5f)),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(20.dp), tint = accentColor)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.btn_close),
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1
            )
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

    LaunchedEffect(isCountingDown) {
        if (isCountingDown) {
            repeat(3) {
                shakeOffset.animateTo(18f, tween(120, easing = FastOutSlowInEasing))
                shakeOffset.animateTo(-18f, tween(120, easing = FastOutSlowInEasing))
            }
            shakeOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
        } else {
            shakeOffset.snapTo(0f)
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
                onPlayAgain = { viewModel.startGame(GameType.RPS) },
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RPSChoiceCard("🪨", RockPaperScissors.ROCK,     Modifier.weight(1f)) { viewModel.playRPS(it) }
                RPSChoiceCard("📄", RockPaperScissors.PAPER,    Modifier.weight(1f)) { viewModel.playRPS(it) }
                RPSChoiceCard("✂️", RockPaperScissors.SCISSORS, Modifier.weight(1f)) { viewModel.playRPS(it) }
            }
        } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.height(80.dp)) {
                Text("...", style = MaterialTheme.typography.displaySmall, color = GamePastelPink)
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
    choice: RockPaperScissors,
    modifier: Modifier = Modifier,
    onClick: (RockPaperScissors) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .bounceClick {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick(choice)
            },
        shape = RoundedCornerShape(28.dp),
        color = GamePastelPinkLight,
        border = BorderStroke(2.dp, GamePastelPink.copy(alpha = 0.5f)),
        shadowElevation = 5.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(emoji, fontSize = 38.sp)
        }
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
                    uiState.slotResults.forEachIndexed { index, emoji ->
                        SpinningReel(
                            targetEmoji = emoji,
                            isSpinning = uiState.isSpinning,
                            delayMillis = index * 120,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (uiState.miniGameState == MiniGameState.PLAYING) {
                    Button(
                        onClick = { viewModel.spinSlots() },
                        enabled = !uiState.isSpinning,
                        colors = ButtonDefaults.buttonColors(containerColor = GamePastelPeach),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .bounceClick { viewModel.spinSlots() },
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(
                            if (uiState.isSpinning) stringResource(R.string.game_slots_spinning)
                            else stringResource(R.string.btn_spin),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color(0xFFB5510B)
                        )
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
                onPlayAgain = { viewModel.startGame(GameType.SLOTS) },
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
        color = if (internalIsSpinning) GamePastelPeach.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.85f),
        modifier = modifier,
        border = BorderStroke(
            2.dp,
            if (internalIsSpinning) GamePastelPeach else GamePastelPeach.copy(alpha = 0.3f)
        ),
        shadowElevation = if (internalIsSpinning) 8.dp else 3.dp
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
                            onClick = {
                                if (!card.isFlipped && !card.isMatched && uiState.memoryFlippedIndices.size < 2) {
                                    viewModel.flipMemoryCard(index)
                                }
                            }
                        )
                    }
                }
            }
            else -> {
                EntranceAnimation { GameResultOverlay(uiState.miniGameState, uiState.lastReward) }
                Spacer(modifier = Modifier.height(16.dp))
                GameActionButtons(
                    accentColor = GamePastelMint,
                    onPlayAgain = { viewModel.startGame(GameType.MEMORY) },
                    onClose = { viewModel.closeMiniGame() }
                )
            }
        }
    }
}

@Composable
fun FlipCard(
    card: com.mert.paticat.ui.screens.cat.MemoryCard,
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
                color = GamePastelMint.copy(alpha = 0.4f),
                border = BorderStroke(2.dp, GamePastelMint.copy(alpha = 0.7f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🐾", fontSize = 22.sp, modifier = Modifier.alpha(0.65f))
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
                    else           -> Color.White.copy(alpha = 0.85f)
                },
                border = BorderStroke(
                    2.dp,
                    if (card.isMatched) GamePastelMint else GamePastelMintLight
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
                                                    Surface(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .bounceClick { viewModel.tapReflexTarget(t.id) },
                                                        shape = RoundedCornerShape(16.dp),
                                                        color = GamePastelLavender,
                                                        shadowElevation = 8.dp,
                                                        border = BorderStroke(2.dp, GamePastelLavLight)
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
                    onPlayAgain = { viewModel.startGame(GameType.REFLEX) },
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
                     "Yakala!",
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
                             "Sepeti sürükleyerek iyi şeyleri yakala!\n💣 Bombalardan kaç!",
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
                                 "3 can • ~30 sn • Puan 15+ = 🏆",
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
                             Text("Başla!", fontWeight = FontWeight.ExtraBold, color = Color.White)
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
                 onPlayAgain = { viewModel.startGame(GameType.CATCH) },
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
             GameBadge("⭐ $score puan", catchBlue, catchBlueDk)
             val heartsText = "❤️".repeat(lives.coerceIn(0, 3))
             GameBadge(heartsText.ifEmpty { "💀" }, Color(0xFFEF9A9A), Color(0xFFB71C1C))
         }

         Spacer(Modifier.height(10.dp))

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
             "Sepeti yatay sürükleyerek yönlendir!",
             style = MaterialTheme.typography.labelSmall,
             color = catchBlueDk.copy(alpha = 0.6f),
             fontWeight = FontWeight.SemiBold
         )
     }
 }
