package com.mert.paticat.ui.screens.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mert.paticat.R
import com.mert.paticat.ui.components.EntranceAnimation
import com.mert.paticat.ui.components.marshmallow.ActionPillButton
import com.mert.paticat.ui.components.marshmallow.MarshmallowSpring
import com.mert.paticat.ui.components.marshmallow.PillowCard
import com.mert.paticat.ui.components.marshmallow.breath
import com.mert.paticat.ui.components.pulsate
import com.mert.paticat.ui.screens.cat.CatViewModel
import com.mert.paticat.ui.screens.cat.GameType
import com.mert.paticat.ui.screens.cat.GameUiState
import com.mert.paticat.ui.screens.cat.MiniGameState
import com.mert.paticat.ui.theme.GamePastelBlue
import com.mert.paticat.ui.theme.GamePastelMint
import com.mert.paticat.ui.theme.GamePastelMintLight
import com.mert.paticat.ui.theme.GamePastelYellow
import com.mert.paticat.ui.theme.GameVibrantMemory
import com.mert.paticat.ui.theme.GameVibrantMemoryDark

// ════════════════════════════════════════════════════════════════════
//  MEMORY GAME
// ════════════════════════════════════════════════════════════════════

@Composable
fun MemoryGame(
    uiState: GameUiState,
    viewModel: CatViewModel
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🧠", fontSize = 38.sp, modifier = Modifier.breath(amplitude = 0.05f))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.game_memory_header),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = GameVibrantMemoryDark
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
                GameVibrantMemoryDark
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val isResultState = uiState.miniGameState == MiniGameState.RESULT_WIN ||
            uiState.miniGameState == MiniGameState.RESULT_LOSE ||
            uiState.miniGameState == MiniGameState.RESULT_DRAW

        when {
            isResultState -> {
                EntranceAnimation { GameResultOverlay(uiState.miniGameState, uiState.lastReward) }
                Spacer(modifier = Modifier.height(16.dp))
                GameActionButtons(
                    accentColor = GameVibrantMemory,
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
                val totalPairs = uiState.memoryCards.size / 2
                val matchedProgress by animateFloatAsState(
                    targetValue = if (totalPairs > 0) uiState.memoryMatchedPairs.toFloat() / totalPairs else 0f,
                    animationSpec = MarshmallowSpring.Bouncy,
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
            // PRE_GAME + any other (IDLE) state — show start UI as fallback.
            else -> {
                PillowCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    backgroundColor = GamePastelMintLight.copy(alpha = 0.65f),
                    border = BorderStroke(1.5.dp, GamePastelMint.copy(alpha = 0.5f)),
                    contentPadding = 32.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
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
                            color = GameVibrantMemoryDark,
                            textAlign = TextAlign.Center
                        )
                        ActionPillButton(
                            text = stringResource(R.string.btn_start_game),
                            onClick = { viewModel.startMemoryGame() },
                            modifier = Modifier.fillMaxWidth(0.7f),
                            backgroundColor = GameVibrantMemory,
                            contentColor = Color.White
                        )
                    }
                }
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
        animationSpec = MarshmallowSpring.Bouncy,
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
