package com.mert.paticat.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mert.paticat.R
import com.mert.paticat.domain.model.Cat
import com.mert.paticat.ui.components.marshmallow.TintedPillowCard
import com.mert.paticat.ui.components.marshmallow.breath
import com.mert.paticat.ui.theme.MoodHappyGradient
import com.mert.paticat.ui.theme.MoodHungryGradient
import com.mert.paticat.ui.theme.MoodNeutralGradient
import com.mert.paticat.ui.theme.MoodSadGradient
import com.mert.paticat.ui.theme.MoodSleepGradient
import com.mert.paticat.ui.theme.MoodTiredGradient
import com.mert.paticat.ui.theme.PremiumBlue
import com.mert.paticat.ui.theme.PremiumPeach
import com.mert.paticat.ui.theme.PremiumPink

/**
 * Marshmallow cat hero — tinted pillow card with mood-driven gradient.
 * Function name retained as `GlassCatHeroCard` for binary compatibility with callers.
 */
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
    onPositioned: (LayoutCoordinates) -> Unit = {},
) {
    val gradient = when {
        isSleeping -> MoodSleepGradient
        hunger < 30 -> MoodHungryGradient
        energy < 30 && happiness > 50 -> MoodSadGradient
        energy < 30 -> MoodTiredGradient
        happiness >= 80 -> MoodHappyGradient
        happiness < 40 -> MoodSadGradient
        else -> MoodNeutralGradient
    }

    val onText = if (isSleeping) Color.White else MaterialTheme.colorScheme.onSurface
    val onSecondary = if (isSleeping) Color.White.copy(alpha = 0.7f)
        else MaterialTheme.colorScheme.onSurfaceVariant

    TintedPillowCard(
        gradient = gradient,
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned(onPositioned),
        contentPadding = 22.dp,
        onClick = onBoxClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1.5f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val levelTitleStr = stringResource(Cat.getLevelTitleResId(level))
                Text(
                    text = catName,
                    style = MaterialTheme.typography.titleLarge,
                    color = onText,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "${stringResource(R.string.cat_hero_level_prefix)} $level · $levelTitleStr",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = onSecondary,
                )
                StatusBarMini(
                    icon = Icons.Default.Restaurant,
                    value = hunger,
                    color = PremiumPeach,
                    onClick = onFeedClick,
                    label = stringResource(R.string.cat_stat_hunger),
                )
                StatusBarMini(
                    icon = Icons.Default.BatteryChargingFull,
                    value = energy,
                    color = PremiumBlue,
                    label = stringResource(R.string.cat_stat_energy),
                )
                StatusBarMini(
                    icon = Icons.Default.Favorite,
                    value = happiness,
                    color = PremiumPink,
                    label = stringResource(R.string.cat_stat_happiness),
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.6f),
                                    Color.White.copy(alpha = 0.0f),
                                ),
                            ),
                        ),
                )
                if (catImageRes != null) {
                    Image(
                        painter = painterResource(catImageRes),
                        contentDescription = stringResource(R.string.cat_image_description, catName),
                        modifier = Modifier
                            .size(120.dp)
                            .breath(amplitude = 0.04f, periodMillis = 2400)
                            .clickable(enabled = !isSleeping) { onCatClick() },
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    val catEmojiCd = stringResource(R.string.icon_cat_emoji)
                    Text(
                        text = "🐱",
                        fontSize = 72.sp,
                        modifier = Modifier
                            .breath()
                            .clickable(enabled = !isSleeping) { onCatClick() }
                            .semantics { contentDescription = catEmojiCd },
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
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = (if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .heightIn(min = 28.dp)
            .fillMaxWidth(),
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        val animatedProgress by animateFloatAsState(
            targetValue = value / 100f,
            animationSpec = tween(durationMillis = 1000),
            label = "statusProgress",
        )

        Box(modifier = Modifier.weight(1f).height(8.dp)) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                color = color,
                trackColor = Color.White.copy(alpha = 0.4f),
                strokeCap = StrokeCap.Round,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "%$value",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}
