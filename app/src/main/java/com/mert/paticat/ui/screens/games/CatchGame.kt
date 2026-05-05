@file:OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
package com.mert.paticat.ui.screens.games

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mert.paticat.R
import com.mert.paticat.ui.components.EntranceAnimation
import com.mert.paticat.ui.components.marshmallow.ActionPillButton
import com.mert.paticat.ui.components.marshmallow.ChipPill
import com.mert.paticat.ui.components.marshmallow.PillowCard
import com.mert.paticat.ui.components.marshmallow.breath
import com.mert.paticat.ui.screens.cat.CatViewModel
import com.mert.paticat.ui.screens.cat.GameType
import com.mert.paticat.ui.screens.cat.GameUiState
import com.mert.paticat.ui.screens.cat.MiniGameState
import com.mert.paticat.ui.theme.CatchHazardColor
import com.mert.paticat.ui.theme.CatchSafeColor
import com.mert.paticat.ui.theme.GameVibrantCatch
import com.mert.paticat.ui.theme.GameVibrantCatchDark
import com.mert.paticat.ui.theme.GameVibrantCatchLight

// ════════════════════════════════════════════════════════════════════
//  CATCH GAME
//  State hoisted into GameDelegate.catchGameState (StateFlow). The
//  composable is pure: it observes state, dispatches input events, and
//  drives the per-frame tick via the ViewModel.
// ════════════════════════════════════════════════════════════════════

@Composable
fun CatchGame(
    uiState: GameUiState,
    viewModel: CatViewModel
) {
    val catchBlue   = GameVibrantCatch
    val catchBlueDk = GameVibrantCatchDark
    val catchBlueLt = GameVibrantCatchLight

    val isResultState = uiState.miniGameState == MiniGameState.RESULT_WIN ||
        uiState.miniGameState == MiniGameState.RESULT_LOSE ||
        uiState.miniGameState == MiniGameState.RESULT_DRAW

    when {
        isResultState -> {
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
                onClose = { viewModel.closeMiniGame() }
            )
        }
        uiState.miniGameState == MiniGameState.PLAYING -> {
            CatchGameArena(viewModel = viewModel)
        }
        // PRE_GAME + any other (IDLE) state — show start UI as fallback.
        else -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_game_basket),
                    contentDescription = stringResource(R.string.icon_play_cta),
                    modifier = Modifier
                        .size(48.dp)
                        .breath(amplitude = 0.06f),
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
                PillowCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    backgroundColor = catchBlueLt.copy(alpha = 0.65f),
                    border = BorderStroke(1.5.dp, catchBlue.copy(alpha = 0.5f)),
                    contentPadding = 28.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val cdItem = stringResource(R.string.cd_catch_falling_item)
                            Icon(
                                painterResource(R.drawable.ic_game_apple),
                                contentDescription = cdItem,
                                modifier = Modifier.size(28.dp),
                                tint = Color.Unspecified,
                            )
                            Icon(
                                painterResource(R.drawable.ic_game_fish),
                                contentDescription = cdItem,
                                modifier = Modifier.size(28.dp),
                                tint = Color.Unspecified,
                            )
                            Icon(
                                painterResource(R.drawable.ic_game_yarn),
                                contentDescription = cdItem,
                                modifier = Modifier.size(28.dp),
                                tint = Color.Unspecified,
                            )
                            Icon(
                                painterResource(R.drawable.ic_game_flower),
                                contentDescription = cdItem,
                                modifier = Modifier.size(28.dp),
                                tint = Color.Unspecified,
                            )
                        }
                        Text(
                            stringResource(R.string.game_catch_instruction),
                            style = MaterialTheme.typography.bodyMedium,
                            color = catchBlueDk,
                            textAlign = TextAlign.Center
                        )
                        ChipPill(
                            text = stringResource(R.string.game_catch_tip),
                            backgroundColor = catchBlue.copy(alpha = 0.18f),
                            contentColor = catchBlueDk
                        )
                        ActionPillButton(
                            text = stringResource(R.string.btn_start_game),
                            onClick = { viewModel.startCatchGame() },
                            modifier = Modifier.fillMaxWidth(0.7f),
                            backgroundColor = catchBlue,
                            contentColor = Color.White
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun CatchGameArena(viewModel: CatViewModel) {
    val catchBlue   = GameVibrantCatch
    val catchBlueDk = GameVibrantCatchDark
    val haptic      = LocalHapticFeedback.current
    val density     = LocalDensity.current

    // State now lives in GameDelegate — composable is read-only + dispatcher.
    val state by viewModel.catchGameState.collectAsStateWithLifecycle()

    var arenaSize by remember { mutableStateOf(IntSize.Zero) }
    val paddleWidthPx = with(density) { 48.dp.toPx() }

    // Drive the simulation at frame rate via the ViewModel — no game state in Composable.
    LaunchedEffect(state.running) {
        if (!state.running) return@LaunchedEffect
        var lastFrameNanos = System.nanoTime()
        while (state.running) {
            val nowNanos = withFrameNanos { it }
            val dt = ((nowNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.1f)
            lastFrameNanos = nowNanos
            viewModel.catchTick(dt)
        }
    }

    // Trigger haptic on catch/miss without owning state.
    LaunchedEffect(state.lastCatchHapticId) {
        if (state.lastCatchHapticId > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
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
                    Icon(Icons.Filled.TrackChanges, contentDescription = null, modifier = Modifier.size(16.dp), tint = catchBlueDk)
                    AnimatedContent(
                        targetState = state.score,
                        transitionSpec = {
                            (slideInVertically { -it } + fadeIn(tween(150))) togetherWith
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
            val heartsText = "❤️".repeat(state.lives.coerceIn(0, 3))
            GameBadge(heartsText.ifEmpty { "💀" }, Color(0xFFEF9A9A), Color(0xFFB71C1C))
        }

        val timerProgress = (1f - state.elapsedSeconds / 30f).coerceIn(0f, 1f)
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { timerProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(50.dp)),
            color = when {
                timerProgress > 0.5f -> CatchSafeColor
                timerProgress > 0.25f -> Color(0xFFFF9800)
                else -> CatchHazardColor
            },
            trackColor = catchBlue.copy(alpha = 0.2f)
        )

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.85f)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFE5EC),
                            Color(0xFFCDE7FF),
                            Color(0xFFA8CDF0)
                        )
                    )
                )
                .onSizeChanged { arenaSize = it }
                .pointerInput(Unit) {
                    // Drag bounds clamped inside delegate using arena + paddle widths
                    // — fixes prior overshoot where paddleX could lock at edge.
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (arenaSize.width > 0) {
                            viewModel.moveCatchPaddle(
                                dx = dragAmount,
                                arenaWidthPx = arenaSize.width.toFloat(),
                                paddleWidthPx = paddleWidthPx,
                            )
                        }
                    }
                }
        ) {
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
                    Text("✦", fontSize = (7 + i % 5).sp, color = Color.White.copy(alpha = 0.55f))
                }
            }

            val cdItem = stringResource(R.string.cd_catch_falling_item)
            state.items.forEach { item ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = item.x * size.width - 20.dp.toPx()
                            translationY = item.y * size.height - 20.dp.toPx()
                        }
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (item.isBomb) R.drawable.ic_game_bomb else falconIconForItem(item.id),
                        ),
                        contentDescription = cdItem,
                        modifier = Modifier
                            .size(36.dp)
                            .semantics { contentDescription = cdItem },
                        tint = Color.Unspecified
                    )
                }
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = state.paddleX * size.width - 24.dp.toPx()
                        translationY = 0.82f * size.height
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_game_basket),
                    contentDescription = stringResource(R.string.icon_play_cta),
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

/**
 * Stable per-item resource picker — uses item id so the same falling item
 * always renders with the same icon across recompositions.
 */
private fun falconIconForItem(id: Int): Int {
    val good = intArrayOf(
        R.drawable.ic_game_apple,
        R.drawable.ic_game_fish,
        R.drawable.ic_game_yarn,
        R.drawable.ic_game_flower,
    )
    return good[(id % good.size + good.size) % good.size]
}
