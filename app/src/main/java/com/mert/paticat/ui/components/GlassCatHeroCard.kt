package com.mert.paticat.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mert.paticat.R
import com.mert.paticat.domain.model.Cat
import com.mert.paticat.ui.theme.PremiumBlue
import com.mert.paticat.ui.theme.PremiumPeach
import com.mert.paticat.ui.theme.PremiumPink

@Composable
fun GlassCatHeroCard(
    catName: String,
    level: Int,
    hunger: Int,
    happiness: Int,
    energy: Int,
    onFeedClick: () -> Unit,
    onCatClick: () -> Unit = {},
    onBoxClick: () -> Unit = {},
    isSleeping: Boolean = false,
    catImageRes: Int? = null,
    onPositioned: (LayoutCoordinates) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(32.dp)
            )
            .clickable { onBoxClick() }
            .onGloballyPositioned(onPositioned),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                        )
                    )
                )
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stats
            Column(
                modifier = Modifier.weight(1.5f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val levelTitleStr = stringResource(
                    Cat.getLevelTitleResId(level)
                )
                Text(
                    text = catName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${stringResource(R.string.cat_hero_level_prefix)} $level · $levelTitleStr",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Status Bars
                StatusBarMini(
                    icon = Icons.Default.Restaurant,
                    value = hunger,
                    color = PremiumPeach,
                    onClick = onFeedClick,
                    label = stringResource(R.string.cat_stat_hunger)
                )
                StatusBarMini(
                    icon = Icons.Default.BatteryChargingFull,
                    value = energy,
                    color = PremiumBlue,
                    label = stringResource(R.string.cat_stat_energy)
                )
                StatusBarMini(
                    icon = Icons.Default.Favorite,
                    value = happiness,
                    color = PremiumPink,
                    label = stringResource(R.string.cat_stat_happiness)
                )
            }

            // Avatar Emoji with soft circle platform
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Background circle — no clip on outer Box so emoji is never cut off
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                                )
                            )
                        )
                )
                val emojiRes = when {
                    isSleeping -> "😴"
                    hunger < 30 -> "😿"
                    energy < 30 -> if (happiness > 50) "😻" else "😿"
                    happiness >= 80 -> "😻"
                    happiness < 40 -> "😿"
                    else -> "😸"
                }
                if (catImageRes != null) {
                    Image(
                        painter = painterResource(catImageRes),
                        contentDescription = stringResource(R.string.cat_image_description, catName),
                        modifier = Modifier
                            .size(116.dp)
                            .pulsate(scaleRange = 0.99f..1.025f, duration = 1400)
                            .clickable(enabled = !isSleeping) { onCatClick() },
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = emojiRes,
                        fontSize = 72.sp,
                        modifier = Modifier
                            .pulsate()
                            .clickable(enabled = !isSleeping) { onCatClick() }
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBarMini(
    icon: ImageVector,
    value: Int,
    color: Color,
    onClick: (() -> Unit)? = null,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = (if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .heightIn(min = 28.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        val animatedProgress by animateFloatAsState(
            targetValue = value / 100f,
            animationSpec = tween(durationMillis = 1000),
            label = "statusProgress"
        )

        Box(modifier = Modifier.weight(1f).height(8.dp)) {
            // Neon Glow Layer Behind
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .shadow(
                        elevation = 8.dp,
                        spotColor = color,
                        ambientColor = color,
                        shape = CircleShape
                    )
            )

            // Actual Progress Indicator
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                color = color,
                trackColor = color.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )
        }

        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "%$value",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
