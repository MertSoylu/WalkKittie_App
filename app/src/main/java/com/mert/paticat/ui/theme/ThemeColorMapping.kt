package com.mert.paticat.ui.theme

fun resolveThemeColor(themeName: String?): ThemeColor = when (themeName?.lowercase()) {
    "standard", "pink", null -> ThemeColor.Pink
    "ocean", "blue" -> ThemeColor.Blue
    "nature", "green" -> ThemeColor.Green
    "sunset", "purple" -> ThemeColor.Purple
    "orange" -> ThemeColor.Orange
    else -> runCatching { ThemeColor.valueOf(themeName.replaceFirstChar { it.uppercase() }) }
        .getOrDefault(ThemeColor.Pink)
}
