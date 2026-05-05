package com.mert.paticat.ui.screens.games

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mert.paticat.R
import com.mert.paticat.ui.components.marshmallow.ActionPillButton
import com.mert.paticat.ui.components.marshmallow.ChipPill
import com.mert.paticat.ui.components.marshmallow.MarshmallowSpring
import com.mert.paticat.ui.components.marshmallow.TintedPillowCard
import com.mert.paticat.ui.screens.cat.GameType
import com.mert.paticat.ui.theme.GameVibrantCatch
import com.mert.paticat.ui.theme.GameVibrantCatchDark
import com.mert.paticat.ui.theme.GameVibrantCatchLight
import com.mert.paticat.ui.theme.GameVibrantMemory
import com.mert.paticat.ui.theme.GameVibrantMemoryDark
import com.mert.paticat.ui.theme.GameVibrantMemoryLight
import com.mert.paticat.ui.theme.GameVibrantRPS
import com.mert.paticat.ui.theme.GameVibrantRPSDark
import com.mert.paticat.ui.theme.GameVibrantRPSLight
import com.mert.paticat.ui.theme.GameVibrantReflex
import com.mert.paticat.ui.theme.GameVibrantReflexDark
import com.mert.paticat.ui.theme.GameVibrantReflexLight
import com.mert.paticat.ui.theme.GameVibrantSlots
import com.mert.paticat.ui.theme.GameVibrantSlotsDark
import com.mert.paticat.ui.theme.GameVibrantSlotsLight

// ════════════════════════════════════════════════════════════════════
//  PRE-GAME OVERLAY — pillow tinted modal before launching a game
// ════════════════════════════════════════════════════════════════════

private data class GamePreviewConfig(
    val emoji: String,
    val title: String,
    val description: String,
    val rewardHint: String,
    val gradientTop: Color,
    val gradientBottom: Color,
    val textColor: Color,
    val buttonColor: Color
)

@Composable
private fun previewConfigFor(gameType: GameType): GamePreviewConfig = when (gameType) {
    GameType.RPS -> GamePreviewConfig(
        emoji = "✊", title = stringResource(R.string.game_rps_title),
        description = stringResource(R.string.game_preview_rps_desc),
        rewardHint = stringResource(R.string.game_reward_rps),
        gradientTop = GameVibrantRPS, gradientBottom = GameVibrantRPSLight,
        textColor = Color.White, buttonColor = GameVibrantRPSDark
    )
    GameType.SLOTS -> GamePreviewConfig(
        emoji = "🎰", title = stringResource(R.string.game_slots_title),
        description = stringResource(R.string.game_preview_slots_desc),
        rewardHint = stringResource(R.string.game_reward_slots),
        gradientTop = GameVibrantSlots, gradientBottom = GameVibrantSlotsLight,
        textColor = Color.White, buttonColor = GameVibrantSlotsDark
    )
    GameType.MEMORY -> GamePreviewConfig(
        emoji = "🧠", title = stringResource(R.string.game_memory_title),
        description = stringResource(R.string.game_preview_memory_desc),
        rewardHint = stringResource(R.string.game_reward_memory),
        gradientTop = GameVibrantMemory, gradientBottom = GameVibrantMemoryLight,
        textColor = Color.White, buttonColor = GameVibrantMemoryDark
    )
    GameType.REFLEX -> GamePreviewConfig(
        emoji = "⚡", title = stringResource(R.string.game_reflex_title),
        description = stringResource(R.string.game_preview_reflex_desc),
        rewardHint = stringResource(R.string.game_reward_reflex),
        gradientTop = GameVibrantReflex, gradientBottom = GameVibrantReflexLight,
        textColor = Color.White, buttonColor = GameVibrantReflexDark
    )
    GameType.CATCH -> GamePreviewConfig(
        emoji = "🧺", title = stringResource(R.string.game_catch_title),
        description = stringResource(R.string.game_catch_instruction),
        rewardHint = stringResource(R.string.game_reward_catch),
        gradientTop = GameVibrantCatch, gradientBottom = GameVibrantCatchLight,
        textColor = Color.White, buttonColor = GameVibrantCatchDark
    )
}

// G-SPLIT: Was `private` in the monolith; now `internal` so GamesScreen lobby can reach it
// from a sibling file. Behavior unchanged.
@Composable
internal fun PreGameOverlay(
    gameType: GameType,
    catLevel: Int,
    catEnergy: Int,
    onPlay: () -> Unit,
    onDismiss: () -> Unit
) {
    val cfg = previewConfigFor(gameType)
    val isLocked  = catLevel < gameType.minLevel
    val hasEnergy = GAME_ROOM_LEVEL_LOCK_DISABLED || catEnergy >= gameType.energyCost
    val canPlay   = !isLocked && hasEnergy

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
            ) { it / 2 } + fadeIn(tween(220))
        ) {
            TintedPillowCard(
                gradient = listOf(cfg.gradientTop, cfg.gradientBottom),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 32.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(36.dp),
                contentPadding = 28.dp,
                elevation = 22.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    var emojiVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(120)
                        emojiVisible = true
                    }
                    AnimatedVisibility(
                        visible = emojiVisible,
                        enter = scaleIn(MarshmallowSpring.PrimaryEnter) + fadeIn()
                    ) {
                        Text(cfg.emoji, fontSize = 72.sp)
                    }

                    Text(
                        cfg.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = cfg.textColor,
                        textAlign = TextAlign.Center
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.22f)
                    ) {
                        Text(
                            cfg.description,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = cfg.textColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                    ) {
                        ChipPill(
                            text = "${gameType.energyCost}",
                            leadingEmoji = "⚡",
                            backgroundColor = Color.White.copy(alpha = 0.28f),
                            contentColor = if (!hasEnergy && !isLocked) Color(0xFFFFCDD2) else cfg.textColor
                        )
                        ChipPill(
                            text = cfg.rewardHint,
                            leadingEmoji = "🏆",
                            backgroundColor = Color.White.copy(alpha = 0.28f),
                            contentColor = cfg.textColor
                        )
                    }

                    if (!canPlay) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Black.copy(alpha = 0.20f)
                        ) {
                            Text(
                                if (isLocked) stringResource(R.string.game_pregame_locked, gameType.minLevel)
                                else          stringResource(R.string.game_pregame_low_energy, catEnergy, gameType.energyCost),
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = cfg.textColor.copy(alpha = 0.92f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    ActionPillButton(
                        text = stringResource(R.string.btn_play_game),
                        onClick = onPlay,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canPlay,
                        backgroundColor = cfg.buttonColor,
                        contentColor = Color.White
                    )

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            stringResource(R.string.btn_dismiss),
                            color = cfg.textColor.copy(alpha = 0.72f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
