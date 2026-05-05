package com.mert.paticat.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Marshmallow palette — softer, milkier evolution of the original premium tokens.
 * Legacy `Premium*` / `Pastel*` names are preserved for backwards compatibility,
 * but their values are re-tuned to the marshmallow scale.
 */

// --- Primary tokens (re-tuned for marshmallow softness) ---
val PremiumPink = Color(0xFFFF8FB6)
val PremiumPinkLight = Color(0xFFFFC2D8)
val PremiumPinkDark = Color(0xFFE56A92)

val PremiumBlue = Color(0xFF8FC8F7)
val PremiumBlueLight = Color(0xFFCDE6FA)
val PremiumBlueDark = Color(0xFF4A9FE0)

val PremiumMint = Color(0xFF8DD49B)
val PremiumMintLight = Color(0xFFC4ECCB)
val PremiumMintDark = Color(0xFF4FA86A)

val PremiumPurple = Color(0xFFB6A1E0)
val PremiumPurpleLight = Color(0xFFDFD2F0)
val PremiumPurpleDark = Color(0xFF7E5FC2)

val PremiumPeach = Color(0xFFFFB89C)
val PremiumPeachLight = Color(0xFFFFD9C7)

val AccentGold = Color(0xFFFFD980)
val AccentTeal = Color(0xFF7BC8BD)

// --- Surface system (light) ---
val BackgroundLight = Color(0xFFFFF7FA)         // soft pink-tinted off-white
val BackgroundLightAlt = Color(0xFFFFF1F5)
val SurfaceLight = Color(0xFFFFFFFF)
val MarshmallowSurface = Color(0xFFFFFDFB)      // sheet card
val MarshmallowSurfaceTinted = Color(0xFFFDF1F5) // mood-tinted card
val MarshmallowOutline = Color(0x1A000000)
val MarshmallowOutlineSoft = Color(0x0D000000)
val MarshmallowShadow = Color(0x14FF8FB6)       // pillow drop shadow tint

// --- Surface system (dark) ---
val BackgroundDark = Color(0xFF161324)          // deep plum-navy
val BackgroundDarkAlt = Color(0xFF1F1A30)
val SurfaceDark = Color(0xFF221C36)
val MarshmallowSurfaceDark = Color(0xFF2A2440)
val MarshmallowSurfaceTintedDark = Color(0xFF2F2848)
val MarshmallowOutlineDark = Color(0x33FFFFFF)
val MarshmallowOutlineSoftDark = Color(0x14FFFFFF)
val MarshmallowShadowDark = Color(0x33000000)

val CardOnLight = Color(0xFFFFF5F8)
val CardOnDark = Color(0x402A2440)

// --- Text ---
val TextHighEmphasis = Color(0xFF1F1A2E)
val TextMediumEmphasis = Color(0xFF6E6880)
val TextLowEmphasis = Color(0xFFA89FB8)
val TextOnPremium = Color(0xFFFFFFFF)

// --- Legacy aliases ---
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
val WarningOrange = Color(0xFFFFB874)
val ErrorRed = Color(0xFFEF7A7A)

// --- Cat stat semantic tokens ---
val HungerColor = PremiumPeach
val EnergyColor = PremiumBlue
val HappinessColor = PremiumPink

val CardPink = PremiumPink.copy(alpha = 0.06f)
val CardBlue = PremiumBlue.copy(alpha = 0.06f)
val CardMint = PremiumMint.copy(alpha = 0.06f)
val CardLavender = PremiumPurple.copy(alpha = 0.06f)

// --- Marshmallow mood gradient pairs ---
// Used by MarshmallowCatHero & TintedPillowCard.
val MoodHappyGradient = listOf(Color(0xFFFFD8E4), Color(0xFFFFC2D8))
val MoodExcitedGradient = listOf(Color(0xFFFFE8C7), Color(0xFFFFC9A2))
val MoodSadGradient = listOf(Color(0xFFCDE6FA), Color(0xFFB1D6F5))
val MoodHungryGradient = listOf(Color(0xFFFFD9C7), Color(0xFFFFB89C))
val MoodTiredGradient = listOf(Color(0xFFDFD2F0), Color(0xFFB6A1E0))
val MoodSleepGradient = listOf(Color(0xFF3B335B), Color(0xFF2A2440))
val MoodNeutralGradient = listOf(Color(0xFFFFF1F5), Color(0xFFFFE0EA))

// Aurora blob colors for the marshmallow background
val AuroraBlobPink = Color(0x40FFB7CD)
val AuroraBlobBlue = Color(0x40A6D5F5)
val AuroraBlobLavender = Color(0x40C9B8E8)
val AuroraBlobPeach = Color(0x40FFD0B8)

// --- Game palette ---
val GamePastelPink      = Color(0xFFFFB3C6)
val GamePastelPinkLight = Color(0xFFFFD6E0)
val GamePastelPeach     = Color(0xFFFFCBA4)
val GamePastelPeachLight= Color(0xFFFFE5CC)
val GamePastelMint      = Color(0xFFB5EAD7)
val GamePastelMintLight = Color(0xFFD4F5E9)
val GamePastelLavender  = Color(0xFFCDB4DB)
val GamePastelLavLight  = Color(0xFFE8D8F5)
val GamePastelBlue      = Color(0xFFBDE0FE)
val GamePastelYellow    = Color(0xFFFFF3B0)

val GameVibrantRPS        = Color(0xFFFF6B9D)
val GameVibrantRPSLight   = Color(0xFFFFADCC)
val GameVibrantSlots      = Color(0xFFFF9D5C)
val GameVibrantSlotsLight = Color(0xFFFFC299)
val GameVibrantMemory     = Color(0xFF6FCFC2)
val GameVibrantMemoryLight= Color(0xFFA8E5DC)
val GameVibrantReflex     = Color(0xFFA08CDC)
val GameVibrantReflexLight= Color(0xFFCBBDF0)
val GameVibrantCatch      = Color(0xFF6FA8D6)
val GameVibrantCatchLight = Color(0xFFA3CBE8)

// Game vibrant darker shades (for headers, buttons)
val GameVibrantRPSDark    = Color(0xFFB5294E)
val GameVibrantSlotsDark  = Color(0xFFBF3600)
val GameVibrantMemoryDark = Color(0xFF00796B)
val GameVibrantReflexDark = Color(0xFF512DA8)
val GameVibrantCatchDark  = Color(0xFF0D47A1)

// Game result semantic tokens
val GameResultWinBg    = Color(0xFF004D40)
val GameResultLoseBg   = Color(0xFF880E4F)
val GameResultDrawBg   = Color(0xFF1A237E)

// --- RPS choice tokens (semantic per choice) ---
val RpsRockColor      = Color(0xFFEF5350)   // red — Rock
val RpsRockColorDark  = Color(0xFFB71C1C)
val RpsPaperColor     = Color(0xFF26A69A)   // teal — Paper
val RpsPaperColorDark = Color(0xFF00695C)
val RpsScissorsColor      = Color(0xFFFFCA28)   // amber — Scissors
val RpsScissorsColorDark  = Color(0xFFF57F17)

// --- Reflex / Catch danger tokens ---
val ReflexDangerColor      = Color(0xFFEF5350)   // low timer warning
val ReflexProgressLowColor = Color(0xFFFFB74D)   // mid timer
val CatchHazardColor       = Color(0xFFEF5350)   // hazardous falling item
val CatchSafeColor         = Color(0xFF66BB6A)   // collectible falling item

// --- Chart goal-met indicator ---
val ChartGoalMetColor    = SuccessGreen
val ChartGoalUnmetColor  = TextMediumEmphasis
