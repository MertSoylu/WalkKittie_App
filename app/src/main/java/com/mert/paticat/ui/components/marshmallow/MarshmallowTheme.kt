package com.mert.paticat.ui.components.marshmallow

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mert.paticat.ui.theme.Dimensions
import com.mert.paticat.ui.theme.MarshmallowOutlineSoft
import com.mert.paticat.ui.theme.MarshmallowOutlineSoftDark
import com.mert.paticat.ui.theme.MarshmallowShadow
import com.mert.paticat.ui.theme.MarshmallowShadowDark

/* =========================================================================
 *  Marshmallow component library — pillow surfaces, soft sheets, chips,
 *  action buttons, dialogs and switches.
 *
 *  Design rules:
 *   - Squircle radii from Dimensions.radius{S,M,L,Xl2}
 *   - Pillow shadow = soft drop blur with marshmallow-tinted alpha
 *   - Upper highlight = 1dp inner border lightening the top edge
 *   - Press feedback uses pillowPress (MarshmallowMotion)
 * ========================================================================= */

@Composable
fun isDarkTheme(): Boolean {
    val bg = MaterialTheme.colorScheme.background
    return (0.2126f * bg.red + 0.7152f * bg.green + 0.0722f * bg.blue) < 0.5f
}

/**
 * Pillow surface — the workhorse card. Soft drop shadow + faint inner top highlight.
 *
 * @param shape Override for non-default radius (defaults to [Dimensions.radiusL]).
 * @param onClick Optional press handler. When set, applies [pillowPress].
 */
@Composable
fun PillowCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Dimensions.radiusL),
    contentPadding: Dp = Dimensions.spaceMedium,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    border: BorderStroke? = null,
    elevation: Dp = Dimensions.pillowShadow,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val dark = isDarkTheme()
    val shadowColor = if (dark) MarshmallowShadowDark else MarshmallowShadow
    val resolvedBorder = border ?: BorderStroke(
        Dimensions.pillowOutline,
        if (dark) MarshmallowOutlineSoftDark else MarshmallowOutlineSoft,
    )

    val pressMod = if (onClick != null) {
        Modifier.pillowPress(onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = shadowColor,
                spotColor = shadowColor,
            )
            .clip(shape)
            .background(backgroundColor, shape)
            .border(resolvedBorder, shape)
            .then(pressMod)
            .padding(contentPadding)
    ) {
        content()
    }
}

/**
 * Tinted variant — soft mood/category gradient inside a pillow surface.
 */
@Composable
fun TintedPillowCard(
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Dimensions.radiusL),
    contentPadding: Dp = Dimensions.spaceMedium,
    elevation: Dp = Dimensions.pillowShadow,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val dark = isDarkTheme()
    val shadowColor = if (dark) MarshmallowShadowDark else MarshmallowShadow

    val pressMod = if (onClick != null) {
        Modifier.pillowPress(onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = shadowColor,
                spotColor = shadowColor,
            )
            .clip(shape)
            .background(brush = Brush.linearGradient(gradient), shape = shape)
            .border(
                Dimensions.pillowOutline,
                if (dark) MarshmallowOutlineSoftDark else MarshmallowOutlineSoft,
                shape,
            )
            .then(pressMod)
            .padding(contentPadding)
    ) {
        content()
    }
}

/**
 * Chip pill — tiny pillow used for category labels and inline status.
 */
@Composable
fun ChipPill(
    text: String,
    modifier: Modifier = Modifier,
    leadingEmoji: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(Dimensions.radiusS)
    val pressMod = if (onClick != null) Modifier.pillowPress(onClick = onClick) else Modifier
    Row(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor, shape)
            .then(pressMod)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (leadingEmoji != null) {
            Text(leadingEmoji, fontSize = 12.sp)
        }
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/**
 * Action pill button — primary CTA with pillow shadow + spring press.
 */
@Composable
fun ActionPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingEmoji: String? = null,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    val shape = RoundedCornerShape(Dimensions.radiusXl2)
    val dark = isDarkTheme()
    val bg = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.4f)
    val fg = if (enabled) contentColor else contentColor.copy(alpha = 0.6f)
    val shadowColor = if (dark) MarshmallowShadowDark else MarshmallowShadow

    Row(
        modifier = modifier
            .heightIn(min = 56.dp)
            .shadow(
                elevation = if (enabled) 12.dp else 0.dp,
                shape = shape,
                ambientColor = shadowColor,
                spotColor = shadowColor,
            )
            .clip(shape)
            .background(bg, shape)
            .pillowPress(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (leadingEmoji != null) {
            Text(leadingEmoji, fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = fg,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * Soft bottom sheet — pillow scrim + spring entrance + drag handle.
 *
 * Drag-to-dismiss is intentionally not handled here; pass a [onDismiss] tied to
 * an explicit close trigger (back press, X button) to keep behaviour predictable.
 */
@Composable
fun SoftSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    heightFraction: Float = 0.92f,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.42f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(initialScale = 0.95f) + fadeIn(),
                exit = scaleOut(targetScale = 0.95f) + fadeOut(),
            ) {
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .fillMaxHeight(heightFraction)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {},
                        )
                        .clip(RoundedCornerShape(topStart = Dimensions.radiusXl2, topEnd = Dimensions.radiusXl2))
                        .background(MaterialTheme.colorScheme.surface)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = Dimensions.spaceMedium, vertical = Dimensions.spaceMedium),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(width = 44.dp, height = 5.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.outlineVariant),
                        )
                        Spacer(Modifier.size(Dimensions.spaceMedium))
                        content()
                    }
                }
            }
        }
    }
}

/**
 * Marshmallow dialog — centered pillow with spring entrance.
 */
@Composable
fun MarshmallowDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
    ) {
        PillowCard(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.spaceMedium),
            shape = RoundedCornerShape(Dimensions.radiusXl2),
            contentPadding = Dimensions.spaceLarge,
            backgroundColor = MaterialTheme.colorScheme.surface,
            elevation = 24.dp,
        ) {
            content()
        }
    }
}

/**
 * Soft segmented tabs — pillow track + sliding marshmallow indicator.
 */
@Composable
fun MarshmallowTabs(
    selectedIndex: Int,
    items: List<String>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val trackShape = RoundedCornerShape(Dimensions.radiusXl2)
    Row(
        modifier = modifier
            .clip(trackShape)
            .background(MaterialTheme.colorScheme.surfaceVariant, trackShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val animBg by animateFloatAsState(
                targetValue = if (selected) 1f else 0f,
                label = "tab_bg_$index",
            )
            val pillShape = RoundedCornerShape(Dimensions.radiusXl2)
            val bg = if (selected)
                MaterialTheme.colorScheme.surface.copy(alpha = animBg)
            else Color.Transparent
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(pillShape)
                    .background(bg, pillShape)
                    .pillowPress(onClick = { onSelect(index) })
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * Soft switch — pillow track with sliding bouncy thumb.
 */
@Composable
fun SoftSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val trackShape = RoundedCornerShape(Dimensions.radiusXl2)
    val onColor = MaterialTheme.colorScheme.primary
    val offColor = MaterialTheme.colorScheme.surfaceVariant
    val animOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 2.dp,
        label = "switch_offset",
    )
    Box(
        modifier = modifier
            .size(width = 52.dp, height = 30.dp)
            .clip(trackShape)
            .background(if (checked) onColor else offColor, trackShape)
            .pillowPress(onClick = { onCheckedChange(!checked) }),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(start = animOffset)
                .size(26.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

/**
 * Section header — bold title + optional emoji prefix and trailing chip.
 */
@Composable
fun SectionHeader2(
    title: String,
    modifier: Modifier = Modifier,
    leadingEmoji: String? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingEmoji != null) {
            Text(leadingEmoji, fontSize = 22.sp)
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (trailing != null) trailing()
    }
}

