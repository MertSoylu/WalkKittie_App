package com.mert.paticat.ui.components.marshmallow

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mert.paticat.ui.navigation.Screen
import com.mert.paticat.ui.theme.Dimensions
import com.mert.paticat.ui.theme.MarshmallowOutlineSoft
import com.mert.paticat.ui.theme.MarshmallowOutlineSoftDark
import com.mert.paticat.ui.theme.MarshmallowShadow
import com.mert.paticat.ui.theme.MarshmallowShadowDark
import kotlin.math.abs

/**
 * Marshmallow navigation bar — pillow capsule, sliding indicator, spring icon scale.
 *
 * Drop-in replacement for the legacy `GlassFloatingBottomBar`: same parameter list.
 */
@Composable
fun MarshmallowNavBar(
    items: List<Screen>,
    selectedIndex: Int,
    pageOffset: Float,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dark = isDarkTheme()
    val barShape = RoundedCornerShape(Dimensions.radiusXl2)
    val primary = MaterialTheme.colorScheme.primary
    val shadowColor = if (dark) MarshmallowShadowDark else MarshmallowShadow
    val outline = if (dark) MarshmallowOutlineSoftDark else MarshmallowOutlineSoft

    Box(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .height(72.dp)
            .shadow(
                elevation = 22.dp,
                shape = barShape,
                ambientColor = shadowColor,
                spotColor = shadowColor,
            )
            .clip(barShape)
            .background(MaterialTheme.colorScheme.surface, barShape)
            .border(Dimensions.pillowOutline, outline, barShape),
    ) {
        val smoothedOffset = selectedIndex + pageOffset

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val barWidth = maxWidth
            val itemWidth = barWidth / items.size
            val pillPaddingH = 6.dp

            val pillTargetDp by animateDpAsState(
                targetValue = itemWidth * selectedIndex + pillPaddingH,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "pill_offset"
            )
            val pillLiveDp = (itemWidth * smoothedOffset) + pillPaddingH
            val isSwipingPrecisely = abs(pageOffset) > 0.01f
            val pillOffsetDp = if (isSwipingPrecisely) pillLiveDp else pillTargetDp

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 6.dp)
                    .width(itemWidth - pillPaddingH * 2)
                    .offset(x = pillOffsetDp)
                    .clip(RoundedCornerShape(Dimensions.radiusM))
                    .background(primary.copy(alpha = if (dark) 0.22f else 0.16f))
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEachIndexed { index, screen ->
                    val isSelected = selectedIndex == index

                    val animatedScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.18f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "icon_scale_$index"
                    )

                    val animatedColor by animateColorAsState(
                        targetValue = if (isSelected) primary
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        animationSpec = tween(280),
                        label = "icon_color_$index"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onItemSelected(index) }
                            )
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                            contentDescription = stringResource(screen.titleResId),
                            tint = animatedColor,
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = animatedScale
                                    scaleY = animatedScale
                                }
                                .size(22.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isSelected,
                            enter = androidx.compose.animation.fadeIn(tween(200)) +
                                androidx.compose.animation.slideInVertically(
                                    initialOffsetY = { -it / 2 },
                                    animationSpec = tween(200)
                                ),
                            exit = androidx.compose.animation.fadeOut(tween(150))
                        ) {
                            Text(
                                text = stringResource(screen.titleResId),
                                style = MaterialTheme.typography.labelSmall,
                                color = primary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
