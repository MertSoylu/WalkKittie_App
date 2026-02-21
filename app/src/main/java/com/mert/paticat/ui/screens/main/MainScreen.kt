package com.mert.paticat.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.mert.paticat.MainViewModel
import com.mert.paticat.ui.navigation.PatiCatNavHost

import androidx.compose.ui.graphics.Color
import com.mert.paticat.ui.components.AnimatedBackground

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val startDestination by viewModel.startDestination.collectAsState()
    val levelUpEvent by viewModel.levelUpEvent.collectAsState()
    
    // Don't render until we have a start destination
    if (startDestination == null) {
        return
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
    ) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())
        
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                PatiCatNavHost(
                    navController = navController,
                    startDestination = startDestination!!,
                    onLanguageSelected = viewModel::setLanguageSelected
                )
            }
        }
        
        // App-Level Overlay Notification
        val rewardNotification by viewModel.rewardNotificationData.collectAsState()
        com.mert.paticat.ui.components.RewardNotificationArea(
            rewardData = rewardNotification,
            onDismiss = { viewModel.clearRewardNotification() }
        )
        
        // Level Up Celebration Overlay
        levelUpEvent?.let { newLevel ->
            com.mert.paticat.ui.components.LevelUpDialog(
                newLevel = newLevel,
                onDismiss = { viewModel.dismissLevelUpEvent(newLevel) }
            )
        }
    }
}
