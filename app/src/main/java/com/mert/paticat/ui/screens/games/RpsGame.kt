@file:OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
package com.mert.paticat.ui.screens.games

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mert.paticat.R
import com.mert.paticat.ui.components.EntranceAnimation
import com.mert.paticat.ui.components.bounceClick
import com.mert.paticat.ui.components.marshmallow.MarshmallowSpring
import com.mert.paticat.ui.components.marshmallow.PillowCard
import com.mert.paticat.ui.components.pulsate
import com.mert.paticat.ui.screens.cat.CatViewModel
import com.mert.paticat.ui.screens.cat.GameType
import com.mert.paticat.ui.screens.cat.GameUiState
import com.mert.paticat.ui.screens.cat.MiniGameState
import com.mert.paticat.ui.screens.cat.RockPaperScissors
import com.mert.paticat.ui.theme.GamePastelBlue
import com.mert.paticat.ui.theme.GamePastelPink
import com.mert.paticat.ui.theme.GamePastelPinkLight
import com.mert.paticat.ui.theme.GameVibrantRPS
import com.mert.paticat.ui.theme.GameVibrantRPSDark
import com.mert.paticat.ui.theme.RpsPaperColor
import com.mert.paticat.ui.theme.RpsRockColor
import com.mert.paticat.ui.theme.RpsScissorsColor
import com.mert.paticat.ui.theme.RpsScissorsColorDark

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
    val isResultState = gameState.miniGameState == MiniGameState.RESULT_WIN ||
        gameState.miniGameState == MiniGameState.RESULT_LOSE ||
        gameState.miniGameState == MiniGameState.RESULT_DRAW
    val shakeOffset = remember { Animatable(0f) }
    var countdownNum by remember { mutableStateOf(3) }

    LaunchedEffect(isCountingDown) {
        if (isCountingDown) {
            countdownNum = 3
            repeat(3) {
                shakeOffset.animateTo(18f, tween(120, easing = FastOutSlowInEasing))
                shakeOffset.animateTo(-18f, tween(120, easing = FastOutSlowInEasing))
            }
            shakeOffset.animateTo(0f, MarshmallowSpring.Gentle)
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
            color = GameVibrantRPSDark
        )

        val statusText = when {
            isCountingDown -> stringResource(R.string.game_rps_countdown)
            isResultState -> ""
            else -> stringResource(R.string.game_win_reward)
        }
        if (statusText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                statusText,
                style = MaterialTheme.typography.bodySmall,
                color = GameVibrantRPSDark.copy(alpha = 0.65f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        PillowCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            backgroundColor = GamePastelPinkLight.copy(alpha = 0.55f),
            border = BorderStroke(1.5.dp, GamePastelPink.copy(alpha = 0.4f)),
            contentPadding = 20.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            color = GameVibrantRPSDark
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
                    textColor = GameVibrantRPSDark,
                    rotation = if (isCountingDown) -shakeOffset.value else 0f
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isResultState) {
            EntranceAnimation { GameResultOverlay(gameState.miniGameState, gameState.lastReward) }
            Spacer(modifier = Modifier.height(16.dp))
            GameActionButtons(
                accentColor = GameVibrantRPS,
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
                color = GameVibrantRPSDark.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RPSChoiceCard("🪨", stringResource(R.string.game_choice_rock), RockPaperScissors.ROCK,     Modifier.weight(1f).heightIn(min = 56.dp)) { viewModel.playRPS(it) }
                RPSChoiceCard("📄", stringResource(R.string.game_choice_paper), RockPaperScissors.PAPER,    Modifier.weight(1f).heightIn(min = 56.dp)) { viewModel.playRPS(it) }
                RPSChoiceCard("✂️", stringResource(R.string.game_choice_scissors), RockPaperScissors.SCISSORS, Modifier.weight(1f).heightIn(min = 56.dp)) { viewModel.playRPS(it) }
            }
        } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.height(80.dp)) {
                AnimatedContent(
                    targetState = countdownNum,
                    transitionSpec = {
                        (scaleIn(MarshmallowSpring.PrimaryEnter) + fadeIn(tween(80))) togetherWith
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
        RockPaperScissors.ROCK     -> RpsRockColor
        RockPaperScissors.PAPER    -> RpsPaperColor
        RockPaperScissors.SCISSORS -> RpsScissorsColor
    }
    val textCol = when (choice) {
        RockPaperScissors.SCISSORS -> RpsScissorsColorDark
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
            Text(emoji, fontSize = 36.sp, color = textCol)
        }
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = GameVibrantRPSDark
        )
    }
}
