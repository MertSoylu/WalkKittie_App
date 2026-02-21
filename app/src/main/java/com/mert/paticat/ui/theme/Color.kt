package com.mert.paticat.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Premium Modern Color Palette for Walkkittie
 * Focused on soft gradients, high contrast text, and vibrant primary actions.
 */

// --- Primary Design Tokens ---
val PremiumPink = Color(0xFFFF6B9D)
val PremiumPinkLight = Color(0xFFFFADCC)
val PremiumPinkDark = Color(0xFFE04E7D)

val PremiumBlue = Color(0xFF64B5F6)
val PremiumBlueLight = Color(0xFFBBDEFB)
val PremiumBlueDark = Color(0xFF1E88E5)

val PremiumMint = Color(0xFF66BB6A)
val PremiumMintLight = Color(0xFFA5D6A7)
val PremiumMintDark = Color(0xFF388E3C)

val PremiumPurple = Color(0xFF9575CD)
val PremiumPurpleLight = Color(0xFFD1C4E9)
val PremiumPurpleDark = Color(0xFF5E35B1)

val PremiumPeach = Color(0xFFFFA280)
val PremiumPeachLight = Color(0xFFFFCCBC)

val AccentGold = Color(0xFFFFD54F)
val AccentTeal = Color(0xFF26A69A)

// --- System Colors ---
val BackgroundLight = Color(0xFFFDFDFD)
val BackgroundDark = Color(0xFF000000)

val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0x330F0F0F) // ~20% opaque — glassmorphism

val CardOnLight = Color(0xFFF8F9FA)
val CardOnDark = Color(0x40161616) // ~25% opaque — glassmorphism

// --- Text Colors ---
val TextHighEmphasis = Color(0xFF1A1A1A)
val TextMediumEmphasis = Color(0xFF757575)
val TextLowEmphasis = Color(0xFFAAAAAA)
val TextOnPremium = Color(0xFFFFFFFF)

// --- Legacy Support (Mappings to keep build working) ---
val PastelPink = PremiumPink
val PastelPinkLight = PremiumPinkLight
val PastelPinkDark = PremiumPinkDark

val PastelBlue = PremiumBlue
val PastelBlueLight = PremiumBlueLight
val PastelBlueDark = PremiumBlueDark

val PastelMint = PremiumMint
val PastelMintLight = PremiumMintLight
val PastelMintDark = PremiumMintDark

val PastelLavender = PremiumPurple
val PastelLavenderLight = PremiumPurpleLight
val PastelLavenderDark = PremiumPurpleDark

val PastelPeach = PremiumPeach
val PastelPeachLight = PremiumPeachLight

val TextPrimary = TextHighEmphasis
val TextSecondary = TextMediumEmphasis
val TextOnPrimary = TextOnPremium

val SuccessGreen = PremiumMint
val WarningOrange = Color(0xFFFFB74D)
val ErrorRed = Color(0xFFEF5350)

val CardPink = PremiumPink.copy(alpha = 0.05f)
val CardBlue = PremiumBlue.copy(alpha = 0.05f)
val CardMint = PremiumMint.copy(alpha = 0.05f)
val CardLavender = PremiumPurple.copy(alpha = 0.05f)
