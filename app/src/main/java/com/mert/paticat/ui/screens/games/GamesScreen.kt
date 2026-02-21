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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mert.paticat.ui.screens.cat.CatViewModel
import com.mert.paticat.ui.screens.cat.GameChoiceBtn
import com.mert.paticat.ui.screens.cat.GameType
import com.mert.paticat.ui.screens.cat.MiniGameState
import com.mert.paticat.ui.screens.cat.RockPaperScissors
import com.mert.paticat.ui.theme.*
import com.mert.paticat.ui.components.NativeAdCard
import com.mert.paticat.ui.components.bounceClick
import com.mert.paticat.ui.components.pulsate
import com.mert.paticat.ui.components.AnimatedBackground
import com.mert.paticat.ui.components.EntranceAnimation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    onBackClick: () -> Unit,
    catViewModel: CatViewModel = hiltViewModel()
) {
    val uiState by catViewModel.uiState.collectAsStateWithLifecycle()
    val playerChoice by catViewModel.playerChoice.collectAsStateWithLifecycle()
    val catChoice by catViewModel.catChoice.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            catViewModel.clearMessage()
        }
    }

    // Game Dialog Wrapper
    if (uiState.activeGame != null) {
        Dialog(
            onDismissRequest = { catViewModel.closeMiniGame() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) Color.Black.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.95f)
                ),
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(0.95f)
            ) {
                 Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logic based on Active Game
                     when(uiState.activeGame) {
                        GameType.RPS -> RockPaperScissorsGame(uiState, playerChoice, catChoice, catViewModel)
                        GameType.SLOTS -> SlotsGame(uiState, catViewModel)
                        GameType.MEMORY -> MemoryGame(uiState, catViewModel)
                        GameType.REFLEX -> ReflexGame(uiState, catViewModel)
                        else -> {}
                    }
                }
            }
        }
    }

    val isDark = isSystemInDarkTheme()

    Box(modifier = Modifier.fillMaxSize()) {
        // Full screen background matching dark/light mode
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDark) {
                        Brush.verticalGradient(
                            colors = listOf(Color.Black, Color.Black)
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(PremiumPink.copy(alpha = 0.8f), PremiumBlue.copy(alpha = 0.8f))
                        )
                    }
                )
        ) {
            // Background particles
            AnimatedBackground()
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
                    title = { Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.games_title), fontWeight = FontWeight.Bold, color = TextOnPremium) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = TextOnPremium)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.games_subtitle), 
                    color = TextOnPremium.copy(alpha = 0.8f), 
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // RSP
                    item {
                        EntranceAnimation(delay = 0) {
                            ModernGameCard(
                                title = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.game_rps_title),
                                cost = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.cost_energy, GameType.RPS.energyCost),
                                icon = Icons.Default.Casino,
                                gradientColors = listOf(PremiumPinkLight, PremiumPink),
                                onClick = { catViewModel.startGame(GameType.RPS) }
                            )
                        }
                    }
                    
                    // Slots
                    item {
                        EntranceAnimation(delay = 100) {
                            ModernGameCard(
                                title = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.game_slots_title),
                                cost = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.cost_energy, GameType.SLOTS.energyCost),
                                icon = Icons.Default.Casino,
                                gradientColors = listOf(PremiumPeachLight, PremiumPeach),
                                 onClick = { catViewModel.startGame(GameType.SLOTS) }
                            )
                        }
                    }

                    // Memory
                    item {
                        EntranceAnimation(delay = 200) {
                            ModernGameCard(
                                title = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.game_memory_title),
                                cost = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.cost_energy, GameType.MEMORY.energyCost),
                                icon = Icons.Default.GridView,
                                 gradientColors = listOf(PremiumMintLight, PremiumMint),
                                onClick = { catViewModel.startGame(GameType.MEMORY) }
                            )
                        }
                    }

                    // Reflex
                    item {
                        EntranceAnimation(delay = 300) {
                            ModernGameCard(
                                title = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.game_reflex_title),
                                cost = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.cost_energy, GameType.REFLEX.energyCost),
                                icon = Icons.Default.TouchApp,
                                gradientColors = listOf(AccentGold.copy(alpha=0.7f), AccentGold),
                                onClick = { catViewModel.startGame(GameType.REFLEX) }
                            )
                        }
                    }
                    
                    // Ad Card (Span full width)
                    item(span = { GridItemSpan(2) }) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            NativeAdCard(nativeAd = uiState.nativeAd)
                            Spacer(modifier = Modifier.height(110.dp))
                        }
                    }
                }
            }
        }
    }
}

// --- Specific Game Composables ---

@Composable
fun RockPaperScissorsGame(uiState: com.mert.paticat.ui.screens.cat.CatUiState, playerChoice: RockPaperScissors?, catChoice: RockPaperScissors?, viewModel: CatViewModel) {
    // Animation States
    val isCountingDown = playerChoice != null && uiState.miniGameState == MiniGameState.PLAYING
    
    // Shake Animation
    val shakeOffset = remember { androidx.compose.animation.core.Animatable(0f) }
    
    LaunchedEffect(isCountingDown) {
        if (isCountingDown) {
            // Shake 3 times (Rock... Paper... Scissors...)
            for (i in 0..2) {
                shakeOffset.animateTo(15f, animationSpec = tween(150, easing = FastOutSlowInEasing))
                shakeOffset.animateTo(-15f, animationSpec = tween(150, easing = FastOutSlowInEasing))
            }
            shakeOffset.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessMedium))
        } else {
            shakeOffset.snapTo(0f)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.game_rps_header),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PremiumPink
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Status Text
        val statusText = when {
            isCountingDown -> androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.game_rps_countdown)
            uiState.miniGameState != MiniGameState.PLAYING -> "" // Result handled in overlay
            else -> androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.game_win_reward)
        }
        
        Text(statusText, style = MaterialTheme.typography.bodyMedium, color = TextMediumEmphasis)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Battle Arena
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Side
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                // Shake Logic: Rotate or Translate? Rotation looks better for hands.
                // Using rotation for "hand" like movement
                val rotation = if (isCountingDown) shakeOffset.value else 0f
                
                Surface(
                    shape = CircleShape, 
                    color = if (isCountingDown) PremiumBlue.copy(alpha = 0.6f) else PremiumBlue.copy(alpha = 0.4f), 
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer { rotationZ = rotation },
                    border = androidx.compose.foundation.BorderStroke(3.dp, PremiumBlue.copy(alpha = 0.6f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                         // While counting down, show Rock (Fist) representing "ready"
                         val displayEmoji = if (isCountingDown) "🤜" else when(playerChoice) {
                                RockPaperScissors.ROCK -> "🪨"
                                RockPaperScissors.PAPER -> "📄"
                                RockPaperScissors.SCISSORS -> "✂️"
                                null -> "👤"
                            }
                         
                         Text(
                            text = displayEmoji,
                            fontSize = 48.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.game_you), fontWeight = FontWeight.Bold, color = PremiumBlue)
            }
            
            // VS Badge
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.05f))
                    .pulsate(duration = 800),
                contentAlignment = Alignment.Center
            ) {
                 Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.game_vs), fontWeight = FontWeight.Black, color = PremiumPink, fontSize = 20.sp)
            }
            
            // Cat Side
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                // Mirror shake for cat
                val rotation = if (isCountingDown) -shakeOffset.value else 0f
                
                Surface(
                    shape = CircleShape, 
                    color = if (isCountingDown) PremiumPink.copy(alpha = 0.6f) else PremiumPink.copy(alpha = 0.4f), 
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer { rotationZ = rotation },
                     border = androidx.compose.foundation.BorderStroke(3.dp, PremiumPink.copy(alpha = 0.6f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                         // Cat also shows Fist "🤛" during countdown
                         val displayEmoji = if (isCountingDown) "🤛" else when(catChoice) {
                                RockPaperScissors.ROCK -> "🪨"
                                RockPaperScissors.PAPER -> "📄"
                                RockPaperScissors.SCISSORS -> "✂️" // Mirror scissors? No need
                                null -> "😺"
                            }
                            
                         Text(
                            text = displayEmoji,
                            fontSize = 48.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(uiState.cat.name, fontWeight = FontWeight.Bold, color = PremiumPink)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Interaction / Result Area
        // Interaction / Result Area
        if (uiState.miniGameState != MiniGameState.PLAYING) {
            // Show Result with Entrance Animation
            EntranceAnimation {
                GameResultOverlay(uiState.miniGameState, uiState.lastReward)
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Play Again / Close Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.startGame(GameType.RPS) },
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumPink),
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.btn_play_again))
                }
                OutlinedButton(
                    onClick = { viewModel.closeMiniGame() },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.btn_close))
                }
            }
        } else if (!isCountingDown) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.height(100.dp)) {
                // Show Choices
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GameChoiceBtn("🪨", RockPaperScissors.ROCK) { viewModel.playRPS(it) }
                    GameChoiceBtn("📄", RockPaperScissors.PAPER) { viewModel.playRPS(it) }
                    GameChoiceBtn("✂️", RockPaperScissors.SCISSORS) { viewModel.playRPS(it) }
                }
            }
        } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.height(100.dp)) {
                // Counting down...
                Text("...", style = MaterialTheme.typography.displayMedium, color = TextMediumEmphasis)
            }
        }
    }
}



@Composable
fun ModernGameCard(
    title: String,
    cost: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .bounceClick { 
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                onClick() 
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors))
        ) {
            // Glassmorphism circle decoration
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-40).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            )
             Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-20).dp, y = 20.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon Header with Glow
                Surface(
                    color = Color.White.copy(alpha = 0.25f),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                // Text Content
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         Surface(
                            color = Color.Black.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚡ $cost", // Added emoji directly for visual punch
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // "Play" Arrow
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.TouchApp, // Or PlayArrow if available
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Badge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text, 
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color.darken(0.1f),
            fontWeight = FontWeight.Bold
        )
    }
}

// Helper extension to darken color
@Composable
fun GameResultOverlay(gameState: MiniGameState, reward: com.mert.paticat.ui.screens.cat.MiniGameReward?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        val resultText = when(gameState) {
             MiniGameState.RESULT_WIN -> androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.result_win)
             MiniGameState.RESULT_LOSE -> androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.result_lose)
             MiniGameState.RESULT_DRAW -> androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.result_draw)
             else -> ""
         }
         val resultColor = when(gameState) {
             MiniGameState.RESULT_WIN -> PremiumMint
             MiniGameState.RESULT_LOSE -> PremiumPink
             else -> PremiumBlue
         }
         
         Text(resultText, style = MaterialTheme.typography.headlineMedium, color = resultColor, fontWeight = FontWeight.Black)
         
         if (reward != null && (reward.mp > 0 || reward.happy > 0 || reward.xp > 0)) {
             Spacer(modifier = Modifier.height(16.dp))
             Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.rewards_won), fontWeight = FontWeight.Bold)
             Spacer(modifier = Modifier.height(8.dp))
             
             Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                 if (reward.mp > 0) EntranceAnimation(delay = 100) { Badge(text = "+${reward.mp} MP", color = PremiumMint) }
                 if (reward.happy > 0) EntranceAnimation(delay = 200) { Badge(text = "+${reward.happy} 💖", color = PremiumPink) }
                 if (reward.xp > 0) EntranceAnimation(delay = 300) { Badge(text = "+${reward.xp} XP", color = AccentGold) }
             }
         }
     }
 }

// Helper extension to darken color
fun Color.darken(factor: Float): Color {
    return Color(
        red = (this.red * (1 - factor)).coerceIn(0f, 1f),
        green = (this.green * (1 - factor)).coerceIn(0f, 1f),
        blue = (this.blue * (1 - factor)).coerceIn(0f, 1f),
        alpha = this.alpha
    )
}

@Composable
fun SlotsGame(uiState: com.mert.paticat.ui.screens.cat.CatUiState, viewModel: CatViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.game_slots_header),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PremiumPeach
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.game_slots_instruction),
            style = MaterialTheme.typography.bodyMedium,
            color = TextMediumEmphasis
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Slot Machine Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(PremiumPeach.copy(alpha = 0.4f))
                .border(2.dp, PremiumPeach.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp), // Spacing between reels
                verticalAlignment = Alignment.CenterVertically
            ) {
                uiState.slotResults.forEachIndexed { index, emoji ->
                    SpinningReel(
                        targetEmoji = emoji,
                        isSpinning = uiState.isSpinning,
                        delayMillis = index * 100, // Staggered stop effect
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f) // Equal width and square shape
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (uiState.miniGameState == MiniGameState.PLAYING) {
            Button(
                onClick = { viewModel.spinSlots() },
                colors = ButtonDefaults.buttonColors(containerColor = PremiumPeach),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .bounceClick { viewModel.spinSlots() }, // Add bounce click
                shape = RoundedCornerShape(16.dp),
                enabled = !uiState.isSpinning
            ) {
                Text(
                    if (uiState.isSpinning) androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.game_slots_spinning) 
                    else androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.btn_spin),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        } else {
            EntranceAnimation {
                GameResultOverlay(uiState.miniGameState, uiState.lastReward)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.startGame(GameType.SLOTS) },
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumPeach),
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.btn_play_again))
                }
                OutlinedButton(
                    onClick = { viewModel.closeMiniGame() },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.btn_close))
                }
            }
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
    
    // Internal spinning state to handle staggered stop
    var internalIsSpinning by remember { mutableStateOf(false) }

    LaunchedEffect(isSpinning) {
        if (isSpinning) {
            internalIsSpinning = true
            // Spin loop
            while (internalIsSpinning) {
                currentEmoji = emojis.random()
                kotlinx.coroutines.delay(50) // Speed of spin
            }
        } else {
            // Wait for delay before stopping
            kotlinx.coroutines.delay(delayMillis.toLong())
            internalIsSpinning = false
            currentEmoji = targetEmoji
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSystemInDarkTheme()) Color(0xFF2C2C2C) else Color.White,
        modifier = modifier
            .then(if (internalIsSpinning) Modifier.graphicsLayer { rotationX = 10f } else Modifier), // Slight tilt when spinning
        border = androidx.compose.foundation.BorderStroke(2.dp, if(internalIsSpinning) PremiumPeach.copy(alpha=0.5f) else Color.Transparent),
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = currentEmoji,
                fontSize = 32.sp,
                modifier = Modifier.graphicsLayer {
                    // Blur effect simulation (scale slightly Y)
                    if (internalIsSpinning) {
                        scaleY = 1.2f
                        alpha = 0.8f
                    }
                }
            )
        }
    }
}

// ===== MEMORY GAME =====
@Composable
fun MemoryGame(uiState: com.mert.paticat.ui.screens.cat.CatUiState, viewModel: CatViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.game_memory_header),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PremiumMint
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Badge(
                text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.game_memory_moves, uiState.memoryMoves),
                color = PremiumBlue
            )
            Badge(
                text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.game_memory_pairs, uiState.memoryMatchedPairs),
                color = PremiumMint
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.miniGameState == MiniGameState.PRE_GAME) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "🐱❓🐱",
                        fontSize = 48.sp,
                        modifier = Modifier.pulsate()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.game_msg_match_two),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.startMemoryGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumMint),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(56.dp)
                    ) {
                        Text(
                            androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.btn_start_game),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }
            }
        } else if (uiState.miniGameState == MiniGameState.PLAYING) {
            // 4x3 Card Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp),
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
        } else {
            EntranceAnimation {
                GameResultOverlay(uiState.miniGameState, uiState.lastReward)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.startGame(GameType.MEMORY) },
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumMint),
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.btn_play_again))
                }
                OutlinedButton(
                    onClick = { viewModel.closeMiniGame() },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.btn_close))
                }
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
            // Back of the card (Pattern)
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(12.dp),
                color = PremiumMint.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(2.dp, PremiumMint.copy(alpha=0.8f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.QuestionMark, 
                        contentDescription = null, 
                        tint = PremiumMint,
                        modifier = Modifier.size(24.dp).alpha(0.5f)
                    )
                }
            }
        } else {
            // Front of the card (Hidden content)
             Surface(
                modifier = Modifier.fillMaxSize()
                    .graphicsLayer { rotationY = 180f }, // Correct content orientation
                shape = RoundedCornerShape(12.dp),
                color = when {
                    card.isMatched -> PremiumMint.copy(alpha = 0.4f)
                    isSystemInDarkTheme() -> Color(0xFF2C2C2C)
                    else -> Color.White
                },
                border = androidx.compose.foundation.BorderStroke(2.dp, if(card.isMatched) PremiumMint else PremiumBlue.copy(alpha=0.2f)),
                shadowElevation = if(card.isMatched) 0.dp else 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = card.emoji, 
                        fontSize = 28.sp
                    )
                    
                    if (card.isMatched) {
                        // Success indicator
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(16.dp).align(Alignment.TopEnd).padding(2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ===== REFLEX GAME =====
@Composable
fun ReflexGame(uiState: com.mert.paticat.ui.screens.cat.CatUiState, viewModel: CatViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.game_reflex_header),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AccentGold
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Badge(
                text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.game_reflex_score, uiState.reflexScore),
                color = AccentGold
            )
            Badge(
                text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.game_reflex_round, uiState.reflexRound, uiState.reflexMaxRounds),
                color = PremiumBlue
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.miniGameState == MiniGameState.PRE_GAME) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AccentGold.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .border(2.dp, AccentGold.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "⚡🐾",
                        fontSize = 48.sp,
                        modifier = Modifier.pulsate()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.game_reflex_ready),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.startReflexGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(56.dp)
                    ) {
                        Text(
                            androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.btn_start_game),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }
            }
        } else if (uiState.miniGameState == MiniGameState.PLAYING) {
            
            // Reflex Game target spawn delay (simulating countdown initially)
            LaunchedEffect(uiState.miniGameState) {
                if (uiState.miniGameState == MiniGameState.PLAYING && uiState.reflexTargets.isEmpty() && uiState.reflexRound == 0) {
                    // Initial wait so the target doesn't appear instantaneously
                    kotlinx.coroutines.delay(1000)
                }
            }
            
            // 4x4 Grid Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AccentGold.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .border(2.dp, AccentGold.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
            ) {
                // Grid lines
                Column(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (row in 0..3) {
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
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
                                    // Target
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = target != null,
                                        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) + fadeIn(tween(100)),
                                        exit = scaleOut(tween(150)) + fadeOut(tween(100))
                                    ) {
                                        target?.let { t ->
                                            Surface(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .bounceClick { viewModel.tapReflexTarget(t.id) },
                                                shape = CircleShape,
                                                color = AccentGold,
                                                shadowElevation = 6.dp,
                                                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(t.emoji, fontSize = 28.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // "Hazır ol" overlay when waiting
                if (uiState.reflexIsWaiting) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
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
                                androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.game_reflex_ready),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            EntranceAnimation {
                GameResultOverlay(uiState.miniGameState, uiState.lastReward)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.startGame(GameType.REFLEX) },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.btn_play_again))
                }
                OutlinedButton(
                    onClick = { viewModel.closeMiniGame() },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.btn_close))
                }
            }
        }
    }
}
