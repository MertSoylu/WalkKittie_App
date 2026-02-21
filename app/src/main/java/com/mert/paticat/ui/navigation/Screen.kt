package com.mert.paticat.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class representing all navigation destinations in the app.
 */
sealed class Screen(
    val route: String,
    val titleResId: Int,
    val selectedIcon: ImageVector = Icons.Default.Home,
    val unselectedIcon: ImageVector = Icons.Outlined.Home
) {
    // Bottom Nav Items
    data object Home : Screen(
        route = "home",
        titleResId = com.mert.paticat.R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    
    data object Cat : Screen(
        route = "cat",
        titleResId = com.mert.paticat.R.string.nav_cat,
        selectedIcon = Icons.Filled.Pets,
        unselectedIcon = Icons.Outlined.Pets
    )
    
    data object Statistics : Screen(
        route = "statistics",
        titleResId = com.mert.paticat.R.string.nav_stats,
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    )
    
    data object Profile : Screen(
        route = "profile",
        titleResId = com.mert.paticat.R.string.nav_profile,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
    
    // Onboarding Screens
    data object Splash : Screen(
        route = "splash",
        titleResId = com.mert.paticat.R.string.app_name, // Placeholder
        selectedIcon = Icons.Default.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    data object Welcome : Screen(
        route = "welcome",
        titleResId = com.mert.paticat.R.string.app_name, // Placeholder
        selectedIcon = Icons.Default.Home, // Not used in bottom nav
        unselectedIcon = Icons.Outlined.Home
    )
    
    data object Language : Screen(
        route = "language",
        titleResId = com.mert.paticat.R.string.settings_language,
        selectedIcon = Icons.Default.Home, // Not used
        unselectedIcon = Icons.Outlined.Home
    )
    
    data object Setup : Screen(
        route = "setup",
        titleResId = com.mert.paticat.R.string.nav_setup,
        selectedIcon = Icons.Default.Person, // Not used in bottom nav
        unselectedIcon = Icons.Outlined.Person
    )
    
    // Non-bottom nav screens
    data object Games : Screen(
        route = "games",
        titleResId = com.mert.paticat.R.string.nav_games,
        selectedIcon = Icons.Default.Pets, // Placeholder
        unselectedIcon = Icons.Outlined.Pets
    )

    data object MainApp : Screen(
        route = "main_app",
        titleResId = com.mert.paticat.R.string.app_name,
        selectedIcon = Icons.Default.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    
    data object LevelInfo : Screen(
        route = "level_info",
        titleResId = com.mert.paticat.R.string.app_name, // Placeholder
        selectedIcon = Icons.Default.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}

fun getBottomNavItems(): List<Screen> {
    return listOf(
        Screen.Home,
        Screen.Cat,
        Screen.Statistics,
        Screen.Profile
    )
}
