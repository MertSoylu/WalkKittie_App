package com.mert.paticat.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.ui.res.stringResource
import com.mert.paticat.R
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mert.paticat.ui.theme.AccentGold
import com.mert.paticat.ui.theme.PremiumMint

data class RewardNotificationData(
    val xp: Int = 0,
    val gold: Int = 0,
    val isXpBoosted: Boolean = false,
    val isGoldBoosted: Boolean = false,
    val title: String? = null,
    val message: String = ""
)

@Composable
fun RewardNotificationArea(
    rewardData: RewardNotificationData?,
    onDismiss: () -> Unit
) {
    val isVisible = rewardData != null
    val dismissProgress = remember { Animatable(1f) }

    LaunchedEffect(rewardData) {
        if (rewardData != null) {
            dismissProgress.snapTo(1f)
            dismissProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
            )
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it - 80 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(animationSpec = tween(150)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)
        ) + fadeOut(animationSpec = tween(200)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 48.dp)
    ) {
        if (rewardData != null) {
            var dragOffset by remember { mutableStateOf(0f) }
            val animatedDrag by animateFloatAsState(
                targetValue = dragOffset,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "dragOffset"
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { translationY = animatedDrag }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (dragOffset < -60f) {
                                    onDismiss()
                                } else {
                                    dragOffset = 0f
                                }
                            }
                        ) { _, dragAmount ->
                            dragOffset = (dragOffset + dragAmount).coerceIn(-300f, 16f)
                        }
                    },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                shadowElevation = 16.dp,
                tonalElevation = 4.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 14.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.CardGiftcard,
                                contentDescription = stringResource(R.string.icon_coin),
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = rewardData.title
                                    ?: androidx.compose.ui.res.stringResource(R.string.reward_title),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (rewardData.message.isNotEmpty()) {
                                Text(
                                    text = rewardData.message,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            } else {
                                Row(
                                    modifier = Modifier.padding(top = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (rewardData.xp > 0) {
                                        RewardChip(
                                            icon = Icons.Filled.Star,
                                            amount = "+${rewardData.xp} XP",
                                            iconColor = AccentGold,
                                            chipColor = AccentGold.copy(alpha = 0.12f),
                                            badge = if (rewardData.isXpBoosted) "2x" else null
                                        )
                                    }
                                    if (rewardData.gold > 0) {
                                        RewardChip(
                                            icon = Icons.Filled.MonetizationOn,
                                            amount = "+${rewardData.gold}",
                                            iconColor = AccentGold,
                                            chipColor = AccentGold.copy(alpha = 0.12f),
                                            badge = if (rewardData.isGoldBoosted) "2x" else null
                                        )
                                    }
                                }
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { dismissProgress.value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        trackColor = Color.Transparent
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardChip(
    icon: ImageVector,
    amount: String,
    iconColor: Color,
    chipColor: Color,
    label: String = "Reward",
    badge: String? = null
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = chipColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(13.dp),
                tint = iconColor
            )
            Text(
                text = amount,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (badge != null) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = iconColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.28f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}
