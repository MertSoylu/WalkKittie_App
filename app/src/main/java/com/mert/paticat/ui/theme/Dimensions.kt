package com.mert.paticat.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Design system spacing and size constants.
 * All spacing follows 8dp grid. Touch targets follow Material Design 3 (min 48dp).
 */
object Dimensions {
    // Spacing grid (8dp base)
    val spaceXs = 4.dp
    val spaceSmall = 8.dp
    val spaceMedium = 16.dp
    val spaceLarge = 24.dp
    val spaceXl = 32.dp
    val space2Xl = 48.dp

    // Screen padding
    val screenPaddingHorizontal = 20.dp
    val screenPaddingVertical = 16.dp

    // Touch targets — minimum 48dp per Material Design 3
    val touchTargetMin = 48.dp
    val touchTargetSmall = 40.dp   // Only inside compound components where parent is 48dp+
    val iconSize = 24.dp
    val iconSizeLarge = 32.dp

    // Corner radii — standardized to multiples of 8dp
    val radiusSmall = 8.dp
    val radiusMedium = 16.dp
    val radiusLarge = 24.dp
    val radiusXl = 28.dp
    val radiusFull = 50.dp         // Equivalent to CircleShape for rectangular items

    // Card elevation
    val elevationCard = 4.dp
    val elevationDialog = 24.dp

    // Bottom navigation clearance
    val bottomNavClearance = 110.dp
}
