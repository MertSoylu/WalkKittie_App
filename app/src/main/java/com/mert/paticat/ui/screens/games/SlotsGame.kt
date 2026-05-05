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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mert.paticat.R
import com.mert.paticat.ui.components.EntranceAnimation
import com.mert.paticat.ui.components.bounceClick
import com.mert.paticat.ui.components.glowPulse
import com.mert.paticat.ui.components.marshmallow.MarshmallowSpring
import com.mert.paticat.ui.components.marshmallow.PillowCard
import com.mert.paticat.ui.components.marshmallow.breath
import com.mert.paticat.ui.screens.cat.CatViewModel
import com.mert.paticat.ui.screens.cat.GameType
import com.mert.paticat.ui.screens.cat.GameUiState
import com.mert.paticat.ui.screens.cat.MiniGameState
import com.mert.paticat.ui.theme.GamePastelPeach
import com.mert.paticat.ui.theme.GamePastelPeachLight
import com.mert.paticat.ui.theme.GamePastelYellow
import com.mert.paticat.ui.theme.GameVibrantSlots
import com.mert.paticat.ui.theme.GameVibrantSlotsDark
import com.mert.paticat.ui.theme.GameVibrantSlotsLight
import kotlinx.coroutines.isActive

// ════════════════════════════════════════════════════════════════════
//  SLOTS GAME
// ════════════════════════════════════════════════════════════════════

@Composable
fun SlotsGame(
    uiState: GameUiState,
    viewModel: CatViewModel
) {
    val isResultState = uiState.miniGameState == MiniGameState.RESULT_WIN ||
        uiState.miniGameState == MiniGameState.RESULT_LOSE ||
        uiState.miniGameState == MiniGameState.RESULT_DRAW
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🎰", fontSize = 38.sp, modifier = Modifier.breath(amplitude = 0.05f))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.game_slots_header),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = GameVibrantSlotsDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.game_slots_instruction),
            style = MaterialTheme.typography.bodySmall,
            color = GameVibrantSlotsDark.copy(alpha = 0.65f)
        )
        Spacer(modifier = Modifier.height(20.dp))

        PillowCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            backgroundColor = GamePastelPeachLight.copy(alpha = 0.65f),
            border = BorderStroke(1.5.dp, GamePastelPeach.copy(alpha = 0.5f)),
            contentPadding = 20.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
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

                if (!isResultState) {
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
                                    Brush.horizontalGradient(listOf(GameVibrantSlots, GameVibrantSlotsDark))
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
                                    color = GameVibrantSlotsDark
                                )
                                Text(
                                    stringResource(R.string.btn_spinning),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = GameVibrantSlotsDark
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

        if (isResultState) {
            Spacer(modifier = Modifier.height(20.dp))
            EntranceAnimation { GameResultOverlay(uiState.miniGameState, uiState.lastReward) }
            Spacer(modifier = Modifier.height(16.dp))
            GameActionButtons(
                accentColor = GameVibrantSlots,
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
            // G3: Halt loop when host coroutine is cancelled (recomposition / dispose).
            while (isSpinning && internalIsSpinning && isActive) {
                currentEmoji = emojis.random()
                kotlinx.coroutines.delay(REEL_SPIN_FRAME_DELAY_MS)
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
            AnimatedContent(
                targetState = currentEmoji,
                transitionSpec = {
                    if (internalIsSpinning) {
                        (slideInVertically { it } + fadeIn(tween(40))) togetherWith
                            (slideOutVertically { -it } + fadeOut(tween(40)))
                    } else {
                        (scaleIn(MarshmallowSpring.Bouncy) + fadeIn()) togetherWith
                            fadeOut(tween(80))
                    }
                },
                label = "reel_emoji"
            ) { emoji ->
                Text(emoji, fontSize = 36.sp)
            }
        }
    }
}
