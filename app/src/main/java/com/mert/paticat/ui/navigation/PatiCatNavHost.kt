package com.mert.paticat.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mert.paticat.ui.screens.cat.CatScreen
import com.mert.paticat.ui.screens.games.GamesScreen
import com.mert.paticat.ui.screens.profile.ProfileScreen
import com.mert.paticat.ui.screens.profile.LevelInfoScreen
import com.mert.paticat.ui.screens.statistics.StatisticsScreen
import com.mert.paticat.ui.screens.welcome.OnboardingScreen
import com.mert.paticat.ui.screens.welcome.SetupProfileScreen

/**
 * Main Navigation Host for the app.
 */
@Composable
fun PatiCatNavHost(
    navController: NavHostController,
    startDestination: String,
    onLanguageSelected: (String) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(animationSpec = tween(500))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(500))
        }
    ) {
        // First Launch Language Selection
        composable(Screen.Language.route) {
            com.mert.paticat.ui.screens.welcome.LanguageSelectionScreen(
                onLanguageSelected = { lang ->
                    onLanguageSelected(lang)
                    // Fallback navigation if activity doesn't restart
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Language.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Onboarding Flow
        composable(Screen.Welcome.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.Setup.route)
                }
            )
        }
        
        composable(Screen.Setup.route) {
            SetupProfileScreen(
                onSetupComplete = {
                    navController.navigate(Screen.MainApp.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        // Main App Container (Swipe Navigation)
        composable(Screen.MainApp.route) {
            com.mert.paticat.ui.screens.main.MainPagerScreen(
                onNavigateToGames = { navController.navigate(Screen.Games.route) },
                onNavigateToLevelInfo = { navController.navigate(Screen.LevelInfo.route) }
            )
        }
        
        // Full Screen Features
        composable(Screen.Games.route) {
            GamesScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.LevelInfo.route) {
            LevelInfoScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
