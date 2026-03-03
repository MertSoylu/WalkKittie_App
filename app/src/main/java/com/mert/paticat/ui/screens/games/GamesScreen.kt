package com.mert.paticat.ui.screens.games

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.mert.paticat.ui.components.GameActionButtons

// ════════════════════════════════════════════════════════════════════
//  GAMES SCREEN – Main Entry
// ════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
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
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 24.dp)
                    .fillMaxWidth(0.96f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (gameState.activeGame) {
                        GameType.RPS -> RockPaperScissorsGame(gameState, uiState, playerChoice, catChoice, catViewModel)
                        GameType.SLOTS -> SlotsGame(gameState, catViewModel)
                        GameType.MEMORY -> MemoryGame(gameState, catViewModel)
                        GameType.REFLEX -> ReflexGame(gameState, catViewModel)
                        else -> {}
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
                    // Energy badge
                    Surface(
                        color = PremiumBlue.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚡", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${uiState.cat.energy}",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = PremiumBlue
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
                        accentColor = PremiumPink,
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
                        accentColor = PremiumPeach,
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
                        accentColor = PremiumMint,
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
                        accentColor = AccentGold,
                        onClick = { catViewModel.startGame(GameType.REFLEX) }
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
//  GAME CARD (Selection grid item)
// ════════════════════════════════════════════════════════════════════

@Composable
private fun GameCard(
    emoji: String,
    title: String,
    description: String,
    energyCost: Int,
    catEnergy: Int,
    accentColor: Color,
    onClick: () -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val isEnabled = catEnergy >= energyCost

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = if (isEnabled) 1f else 0.5f }
            .bounceClick {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Emoji circle
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(emoji, fontSize = 26.sp)
                }
            }

            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp,
                maxLines = 2
            )

            // Cost badge — shows current/required energy when disabled
            Surface(
                color = if (isEnabled) accentColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    if (isEnabled) "⚡ ${stringResource(R.string.cost_energy, energyCost)}"
                    else "⚡ $catEnergy/$energyCost",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) accentColor else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  SHARED – Badge & Result Overlay
// ════════════════════════════════════════════════════════════════════

@Composable
fun GameBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GameResultOverlay(gameState: MiniGameState, reward: com.mert.paticat.ui.screens.cat.MiniGameReward?) {
    val resultEmoji = when (gameState) {
        MiniGameState.RESULT_WIN -> "🎉"
        MiniGameState.RESULT_LOSE -> "😿"
        MiniGameState.RESULT_DRAW -> "🤝"
        else -> ""
    }
    val resultText = when (gameState) {
        MiniGameState.RESULT_WIN -> stringResource(R.string.result_win)
        MiniGameState.RESULT_LOSE -> stringResource(R.string.result_lose)
        MiniGameState.RESULT_DRAW -> stringResource(R.string.result_draw)
        else -> ""
    }
    val resultColor = when (gameState) {
        MiniGameState.RESULT_WIN -> PremiumMint
        MiniGameState.RESULT_LOSE -> PremiumPink
        else -> PremiumBlue
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(resultEmoji, fontSize = 48.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            resultText,
            style = MaterialTheme.typography.headlineSmall,
            color = resultColor,
            fontWeight = FontWeight.Black
        )

        if (reward != null && (reward.gold > 0 || reward.happy > 0 || reward.xp > 0)) {
            Spacer(modifier = Modifier.height(16.dp))

            // Reward chips in a card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = resultColor.copy(alpha = 0.06f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        stringResource(R.string.rewards_won),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (reward.gold > 0) EntranceAnimation(delay = 100) { GameBadge("+${reward.gold} 🪙", AccentGold) }
                        if (reward.happy > 0) EntranceAnimation(delay = 200) { GameBadge("+${reward.happy} 💖", PremiumPink) }
                        if (reward.xp > 0) EntranceAnimation(delay = 300) { GameBadge("+${reward.xp} ⭐", PremiumBlue) }
                    }
                }
            }
        }
    }
}

fun Color.darken(factor: Float): Color = Color(
    red = (red * (1 - factor)).coerceIn(0f, 1f),
    green = (green * (1 - factor)).coerceIn(0f, 1f),
    blue = (blue * (1 - factor)).coerceIn(0f, 1f),
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
    val shakeOffset = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(isCountingDown) {
        if (isCountingDown) {
            for (i in 0..2) {
                shakeOffset.animateTo(15f, tween(150, easing = FastOutSlowInEasing))
                shakeOffset.animateTo(-15f, tween(150, easing = FastOutSlowInEasing))
            }
            shakeOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
        } else {
            shakeOffset.snapTo(0f)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Title
        Text("✊📄✂️", fontSize = 28.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.game_rps_header),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Status subtitle
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Battle Arena ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player
                BattleAvatar(
                    label = stringResource(R.string.game_you),
                    emoji = if (isCountingDown) "🤜" else when (playerChoice) {
                        RockPaperScissors.ROCK -> "🪨"
                        RockPaperScissors.PAPER -> "📄"
                        RockPaperScissors.SCISSORS -> "✂️"
                        null -> "👤"
                    },
                    color = PremiumBlue,
                    rotation = if (isCountingDown) shakeOffset.value else 0f
                )

                // VS
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.pulsate(duration = 800)
                    ) {
                        Text(
                            stringResource(R.string.game_vs),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Cat
                BattleAvatar(
                    label = catUiState.cat.name,
                    emoji = if (isCountingDown) "🤛" else when (catChoice) {
                        RockPaperScissors.ROCK -> "🪨"
                        RockPaperScissors.PAPER -> "📄"
                        RockPaperScissors.SCISSORS -> "✂️"
                        null -> "😺"
                    },
                    color = PremiumPink,
                    rotation = if (isCountingDown) -shakeOffset.value else 0f
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Interaction / Result ──
        if (gameState.miniGameState != MiniGameState.PLAYING) {
            EntranceAnimation { GameResultOverlay(gameState.miniGameState, gameState.lastReward) }
            Spacer(modifier = Modifier.height(16.dp))
            GameActionButtons(
                accentColor = PremiumPink,
                onPlayAgain = { viewModel.startGame(GameType.RPS) },
                onClose = { viewModel.closeMiniGame() }
            )
        } else if (!isCountingDown) {
            // Choice buttons
            Text(
                stringResource(R.string.game_win_reward),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RPSChoiceCard("🪨", RockPaperScissors.ROCK, Modifier.weight(1f)) { viewModel.playRPS(it) }
                RPSChoiceCard("📄", RockPaperScissors.PAPER, Modifier.weight(1f)) { viewModel.playRPS(it) }
                RPSChoiceCard("✂️", RockPaperScissors.SCISSORS, Modifier.weight(1f)) { viewModel.playRPS(it) }
            }
        } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.height(80.dp)) {
                Text(
                    "...",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BattleAvatar(
    label: String,
    emoji: String,
    color: Color,
    rotation: Float
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer { rotationZ = rotation },
            shape = RoundedCornerShape(20.dp),
            color = color.copy(alpha = 0.12f),
            border = BorderStroke(2.dp, color.copy(alpha = 0.3f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 40.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = color
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
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .bounceClick { onClick(choice) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(emoji, fontSize = 36.sp)
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
        Text("🎰", fontSize = 36.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.game_slots_header),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.game_slots_instruction),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        // ── Slot Machine ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = PremiumPeach.copy(alpha = 0.08f)
            ),
            border = BorderStroke(1.5.dp, PremiumPeach.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Reels
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

                // Spin button or result
                if (uiState.miniGameState == MiniGameState.PLAYING) {
                    Button(
                        onClick = { viewModel.spinSlots() },
                        enabled = !uiState.isSpinning,
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumPeach),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            if (uiState.isSpinning) stringResource(R.string.game_slots_spinning)
                            else stringResource(R.string.btn_spin),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
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
                accentColor = PremiumPeach,
                onPlayAgain = { viewModel.startGame(GameType.SLOTS) },
                onClose = { viewModel.closeMiniGame() }
            )
        }
    }
}

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
                kotlinx.coroutines.delay(50)
            }
        } else {
            kotlinx.coroutines.delay(delayMillis.toLong())
            internalIsSpinning = false
            currentEmoji = targetEmoji
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .then(if (internalIsSpinning) Modifier.graphicsLayer { rotationX = 8f } else Modifier),
        border = BorderStroke(
            2.dp,
            if (internalIsSpinning) PremiumPeach.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        ),
        shadowElevation = if (internalIsSpinning) 6.dp else 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                currentEmoji,
                fontSize = 36.sp,
                modifier = Modifier.graphicsLayer {
                    if (internalIsSpinning) { scaleY = 1.2f; alpha = 0.8f }
                }
            )
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
        Text("🧠", fontSize = 36.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.game_memory_header),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            GameBadge(
                stringResource(R.string.game_memory_moves, uiState.memoryMoves),
                PremiumBlue
            )
            GameBadge(
                stringResource(R.string.game_memory_pairs, uiState.memoryMatchedPairs),
                PremiumMint
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.miniGameState == MiniGameState.PRE_GAME -> {
                // Pre-game card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PremiumMint.copy(alpha = 0.06f)
                    ),
                    border = BorderStroke(1.dp, PremiumMint.copy(alpha = 0.15f))
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.startMemoryGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = PremiumMint),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(48.dp)
                        ) {
                            Text(
                                stringResource(R.string.btn_start_game),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
            uiState.miniGameState == MiniGameState.PLAYING -> {
                // Card Grid
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
                    accentColor = PremiumMint,
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
        animationSpec = tween(400, easing = FastOutSlowInEasing),
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
            // Card back
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(14.dp),
                color = PremiumMint.copy(alpha = 0.15f),
                border = BorderStroke(1.5.dp, PremiumMint.copy(alpha = 0.4f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🐾", fontSize = 20.sp, modifier = Modifier.alpha(0.6f))
                }
            }
        } else {
            // Card front
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f },
                shape = RoundedCornerShape(14.dp),
                color = when {
                    card.isMatched -> PremiumMint.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                border = BorderStroke(
                    1.5.dp,
                    if (card.isMatched) PremiumMint.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                ),
                shadowElevation = if (card.isMatched) 0.dp else 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(card.emoji, fontSize = 24.sp)
                    if (card.isMatched) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = AccentGold,
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

@Composable
fun ReflexGame(
    uiState: GameUiState,
    viewModel: CatViewModel
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("⚡", fontSize = 36.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.game_reflex_header),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            GameBadge(
                stringResource(R.string.game_reflex_score, uiState.reflexScore),
                AccentGold
            )
            GameBadge(
                stringResource(R.string.game_reflex_round, uiState.reflexRound, uiState.reflexMaxRounds),
                PremiumBlue
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.miniGameState == MiniGameState.PRE_GAME -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AccentGold.copy(alpha = 0.06f)
                    ),
                    border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.15f))
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.startReflexGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(48.dp)
                        ) {
                            Text(
                                stringResource(R.string.btn_start_game),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
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

                // 4x4 Grid
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AccentGold.copy(alpha = 0.06f)
                    ),
                    border = BorderStroke(1.5.dp, AccentGold.copy(alpha = 0.2f))
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
                                                .padding(3.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Empty cell background
                                            Surface(
                                                modifier = Modifier.fillMaxSize(),
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                            ) {}

                                            // Target
                                            androidx.compose.animation.AnimatedVisibility(
                                                visible = target != null,
                                                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) + fadeIn(tween(100)),
                                                exit = scaleOut(tween(150)) + fadeOut(tween(100))
                                            ) {
                                                target?.let { t ->
                                                    Surface(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .bounceClick { viewModel.tapReflexTarget(t.id) },
                                                        shape = RoundedCornerShape(12.dp),
                                                        color = AccentGold,
                                                        shadowElevation = 6.dp,
                                                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
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
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "⚡",
                                        fontSize = 44.sp,
                                        modifier = Modifier.pulsate()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        stringResource(R.string.game_reflex_ready),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
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
                    accentColor = AccentGold,
                    onPlayAgain = { viewModel.startGame(GameType.REFLEX) },
                    onClose = { viewModel.closeMiniGame() }
                )
            }
        }
    }
}
