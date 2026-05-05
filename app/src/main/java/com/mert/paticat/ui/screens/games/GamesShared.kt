@file:OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
package com.mert.paticat.ui.screens.games

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
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
import com.mert.paticat.ui.screens.cat.MiniGameState
import com.mert.paticat.ui.theme.GameResultLoseBg
import com.mert.paticat.ui.theme.GameResultWinBg
import com.mert.paticat.ui.theme.GameVibrantCatch
import com.mert.paticat.ui.theme.GameVibrantCatchDark
import com.mert.paticat.ui.theme.GameVibrantMemory
import com.mert.paticat.ui.theme.GameVibrantRPS
import kotlinx.coroutines.launch

// ════════════════════════════════════════════════════════════════════
//  SHARED — badge, result, action buttons
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
        MiniGameState.RESULT_WIN  -> GameResultWinBg
        MiniGameState.RESULT_LOSE -> GameResultLoseBg
        else                      -> GameVibrantCatchDark
    }

    var emojiVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { emojiVisible = true }

    val starScales = remember { List(3) { Animatable(0f) } }
    LaunchedEffect(gameState) {
        if (gameState == MiniGameState.RESULT_WIN) {
            val scope = this
            starScales.forEachIndexed { i, anim ->
                scope.launch {
                    anim.snapTo(0f)
                    kotlinx.coroutines.delay(i * 120L)
                    anim.animateTo(1f, MarshmallowSpring.Bouncy)
                    anim.animateTo(0.85f, tween(280))
                    anim.animateTo(1f, MarshmallowSpring.Bouncy)
                }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (gameState == MiniGameState.RESULT_WIN) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                starScales.forEach { scale ->
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer {
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
            enter = scaleIn(MarshmallowSpring.PrimaryEnter) + fadeIn()
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
            PillowCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                backgroundColor = resultColor.copy(alpha = 0.16f),
                border = BorderStroke(1.5.dp, resultColor.copy(alpha = 0.4f)),
                contentPadding = 16.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
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
        color = bgColor.copy(alpha = 0.92f),
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
