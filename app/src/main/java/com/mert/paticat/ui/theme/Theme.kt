package com.mert.paticat.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeColor {
    Pink,
    Blue,
    Green,
    Purple,
    Orange
}

@Composable
fun WalkkittieTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: ThemeColor = ThemeColor.Pink,
    content: @Composable () -> Unit
) {
    val primaryColor = when(themeColor) {
        ThemeColor.Pink -> PremiumPink
        ThemeColor.Blue -> PremiumBlue
        ThemeColor.Green -> PremiumMint
        ThemeColor.Purple -> PremiumPurple
        ThemeColor.Orange -> PremiumPeach
    }
    
    val secondaryColor = when(themeColor) {
        ThemeColor.Pink -> PremiumBlue
        ThemeColor.Blue -> PremiumPink
        ThemeColor.Green -> AccentGold
        ThemeColor.Purple -> PremiumPeach
        ThemeColor.Orange -> PremiumMint
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.7f), // Increased opacity for better text contrast
            secondary = secondaryColor,
            onSecondary = Color.White,
            background = BackgroundDark,
            onBackground = Color.White,
            surface = SurfaceDark,
            onSurface = Color.White,
            surfaceVariant = Color(0x40252525), // ~25% opaque — glassmorphism
            onSurfaceVariant = Color.LightGray
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.1f), // Lighter container
            onPrimaryContainer = Color.Black,
            secondary = secondaryColor,
            onSecondary = Color.White,
            background = BackgroundLight,
            surface = SurfaceLight,
            surfaceVariant = Color.White,
            onSurfaceVariant = TextMediumEmphasis
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            
            WindowCompat.getInsetsController(window, view).apply {
                 isAppearanceLightStatusBars = !darkTheme
                 isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
