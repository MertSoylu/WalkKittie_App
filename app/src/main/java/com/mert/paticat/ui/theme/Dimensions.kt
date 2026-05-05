package com.mert.paticat.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Marshmallow design system: spacing, sizes, radii.
 * Squircle radii are non-multiples of 8 to give the pillow/marshmallow feel.
 */
object Dimensions {
    // Spacing grid
    val spaceXs = 4.dp
    val spaceSmall = 8.dp
    val spaceMedium = 16.dp
    val spaceLarge = 24.dp
    val spaceXl = 32.dp
    val space2Xl = 48.dp

    // Screen padding
    val screenPaddingHorizontal = 20.dp
    val screenPaddingVertical = 16.dp
    val homeScreenHorizontalPadding = 18.dp

    // Touch targets
    val touchTargetMin = 48.dp
    val touchTargetSmall = 40.dp
    val iconSize = 24.dp
    val iconSizeLarge = 32.dp

    // --- Marshmallow squircle radii ---
    val radiusXs = 10.dp
    val radiusS = 14.dp
    val radiusM = 22.dp
    val radiusL = 32.dp
    val radiusXl2 = 40.dp

    // Legacy radius aliases
    val radiusSmall = radiusS
    val radiusMedium = radiusM
    val radiusLarge = radiusL
    val radiusXl = radiusXl2
    val radiusFull = 100.dp

    // Pillow shadow + outline thicknesses
    val pillowShadow = 18.dp
    val pillowShadowSmall = 10.dp
    val pillowOutline = 1.dp

    // Card elevation (kept for legacy components)
    val elevationCard = 0.dp
    val elevationDialog = 0.dp

    // Bottom navigation clearance
    val bottomNavClearance = 110.dp
}
