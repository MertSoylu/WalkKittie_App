@file:OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
package com.mert.paticat.ui.screens.games

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mert.paticat.R
import com.mert.paticat.ui.components.EntranceAnimation
import com.mert.paticat.ui.components.bounceClick
import com.mert.paticat.ui.components.marshmallow.ActionPillButton
import com.mert.paticat.ui.components.marshmallow.MarshmallowSpring
import com.mert.paticat.ui.components.marshmallow.PillowCard
import com.mert.paticat.ui.components.marshmallow.breath
import com.mert.paticat.ui.components.pulsate
import com.mert.paticat.ui.screens.cat.CatViewModel
import com.mert.paticat.ui.screens.cat.GameType
import com.mert.paticat.ui.screens.cat.GameUiState
import com.mert.paticat.ui.screens.cat.MiniGameState
import com.mert.paticat.ui.theme.GamePastelLavLight
import com.mert.paticat.ui.theme.GamePastelLavender
import com.mert.paticat.ui.theme.GamePastelYellow
import com.mert.paticat.ui.theme.GameVibrantReflex
import com.mert.paticat.ui.theme.GameVibrantReflexDark
import com.mert.paticat.ui.theme.ReflexDangerColor
import com.mert.paticat.ui.theme.ReflexProgressLowColor

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
            val timeoutMs = reflexDelayMs(uiState.reflexRound)
            roundTimerProgress.animateTo(0f, animationSpec = tween(timeoutMs.toInt(), easing = LinearEasing))
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("⚡", fontSize = 38.sp, modifier = Modifier.breath(amplitude = 0.06f))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.game_reflex_header),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = GameVibrantReflexDark
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
                GameVibrantReflexDark
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
                    roundTimerProgress.value > 0.25f -> ReflexProgressLowColor
                    else -> ReflexDangerColor
                },
                trackColor = GamePastelLavLight.copy(alpha = 0.4f)
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
                    accentColor = GameVibrantReflex,
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
            uiState.miniGameState == MiniGameState.PLAYING -> {
                LaunchedEffect(uiState.miniGameState) {
                    if (uiState.reflexTargets.isEmpty() && uiState.reflexRound == 0) {
                        kotlinx.coroutines.delay(1000)
                    }
                }

                PillowCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(32.dp),
                    backgroundColor = GamePastelLavLight.copy(alpha = 0.55f),
                    border = BorderStroke(1.5.dp, GamePastelLavender.copy(alpha = 0.4f)),
                    contentPadding = 8.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
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
                                            Surface(
                                                modifier = Modifier.fillMaxSize(),
                                                shape = RoundedCornerShape(16.dp),
                                                color = GamePastelLavender.copy(alpha = 0.15f)
                                            ) {}

                                            androidx.compose.animation.AnimatedVisibility(
                                                visible = target != null,
                                                enter = scaleIn(MarshmallowSpring.Bouncy) + fadeIn(tween(80)),
                                                exit = scaleOut(tween(120)) + fadeOut(tween(80))
                                            ) {
                                                target?.let { t ->
                                                    val targetColor = when {
                                                        uiState.reflexRound >= 8 -> ReflexDangerColor
                                                        uiState.reflexRound >= 5 -> ReflexProgressLowColor
                                                        else -> GamePastelLavender
                                                    }
                                                    val targetBorderColor = when {
                                                        uiState.reflexRound >= 8 -> ReflexDangerColor.copy(alpha = 0.45f)
                                                        uiState.reflexRound >= 5 -> ReflexProgressLowColor.copy(alpha = 0.45f)
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

                        if (uiState.reflexIsWaiting) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(GamePastelLavLight.copy(alpha = 0.78f)),
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
                                        color = GameVibrantReflexDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
            // PRE_GAME + any other (IDLE) state — show start UI as fallback.
            else -> {
                PillowCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    backgroundColor = GamePastelLavLight.copy(alpha = 0.65f),
                    border = BorderStroke(1.5.dp, GamePastelLavender.copy(alpha = 0.5f)),
                    contentPadding = 32.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
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
                            color = GameVibrantReflexDark,
                            textAlign = TextAlign.Center
                        )
                        ActionPillButton(
                            text = stringResource(R.string.btn_start_game),
                            onClick = { viewModel.startReflexGame() },
                            modifier = Modifier.fillMaxWidth(0.7f),
                            backgroundColor = GameVibrantReflex,
                            contentColor = Color.White
                        )
                    }
                }
            }
        }
    }
}
