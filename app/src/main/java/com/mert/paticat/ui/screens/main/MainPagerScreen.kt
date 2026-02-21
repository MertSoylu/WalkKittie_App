package com.mert.paticat.ui.screens.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mert.paticat.MainViewModel
import com.mert.paticat.ui.components.TutorialOverlay
import com.mert.paticat.ui.components.TutorialTarget
import com.mert.paticat.ui.navigation.Screen
import com.mert.paticat.ui.navigation.getBottomNavItems
import com.mert.paticat.ui.screens.cat.CatScreen
import com.mert.paticat.ui.screens.home.HomeScreen
import com.mert.paticat.ui.screens.profile.ProfileScreen
import com.mert.paticat.ui.screens.statistics.StatisticsScreen
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPagerScreen(
    onNavigateToGames: () -> Unit,
    onNavigateToLevelInfo: () -> Unit = {},
    viewModel: MainViewModel = hiltViewModel()
) {
    val catName by viewModel.catName.collectAsState()
    val bottomNavItems = getBottomNavItems()
    
    val pagerState = rememberPagerState(pageCount = { bottomNavItems.size })
    val coroutineScope = rememberCoroutineScope()
    
    // Tutorial Logic
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("paticat_prefs", android.content.Context.MODE_PRIVATE) }
    var showTutorial by remember { mutableStateOf(false) }
    var currentStep by remember { mutableIntStateOf(0) }
    var currentTargets by remember { mutableStateOf<List<TutorialTarget>>(emptyList()) }

    // Page Change Effect for Tutorial
    LaunchedEffect(pagerState.currentPage, catName) {
        val currentPage = bottomNavItems[pagerState.currentPage]
        // Use versioned key to force show tutorial if needed/reset
        val prefKey = "tutorial_completed_v4_${currentPage.route}"
        
        if (!prefs.getBoolean(prefKey, false)) {
            val targets = when(currentPage) {
                Screen.Home -> listOf(
                    TutorialTarget(
                        0, 
                        context.getString(com.mert.paticat.R.string.tutorial_home_welcome_title),
                        context.getString(com.mert.paticat.R.string.tutorial_home_welcome_desc)
                    ),
                    TutorialTarget(
                        1, 
                        context.getString(com.mert.paticat.R.string.tutorial_home_cat_title),
                        context.getString(com.mert.paticat.R.string.tutorial_home_cat_desc, catName)
                    ),
                    TutorialTarget(
                        2,
                        context.getString(com.mert.paticat.R.string.tutorial_home_goals_title),
                        context.getString(com.mert.paticat.R.string.tutorial_home_goals_desc)
                    )
                )
                Screen.Cat -> listOf(
                    TutorialTarget(
                        0, 
                        context.getString(com.mert.paticat.R.string.tutorial_cat_care_title, catName),
                        context.getString(com.mert.paticat.R.string.tutorial_cat_care_desc, catName)
                    ),
                    TutorialTarget(
                        1, 
                        context.getString(com.mert.paticat.R.string.tutorial_cat_food_title),
                        context.getString(com.mert.paticat.R.string.tutorial_cat_food_desc, catName)
                    ),
                    TutorialTarget(
                        2, 
                        context.getString(com.mert.paticat.R.string.tutorial_cat_games_title),
                        context.getString(com.mert.paticat.R.string.tutorial_cat_games_desc, catName)
                    )
                )
                Screen.Statistics -> listOf(
                    TutorialTarget(
                        0, 
                        context.getString(com.mert.paticat.R.string.tutorial_stats_title),
                        context.getString(com.mert.paticat.R.string.tutorial_stats_desc)
                    ),
                    TutorialTarget(
                        1, 
                        context.getString(com.mert.paticat.R.string.tutorial_stats_water_title),
                        context.getString(com.mert.paticat.R.string.tutorial_stats_water_desc)
                    )
                )
                Screen.Profile -> listOf(
                    TutorialTarget(
                        0,
                        context.getString(com.mert.paticat.R.string.tutorial_profile_title),
                        context.getString(com.mert.paticat.R.string.tutorial_profile_desc)
                    )
                )
                else -> emptyList()
            }
            
            if (targets.isNotEmpty()) {
                currentTargets = targets
                if (!showTutorial) {
                    currentStep = 0
                    showTutorial = true
                }
            }
        }
    }

    // --- Main Layout ---
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Animated Background
        AnimatedGradientBackground()
        
        // 2. Content (Scaffold removed, handling padding manually/via content)
        // Adjust screen content to not be hidden by status/nav bars if transparent
        
        // We use a Box to contain Pager, and floating bar on top.
        // Pager needs padding BOTTOM to not be obscured by Floating Bar
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 0.dp) // Pager goes full height, content inside usually has scroll padding
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                // İçerikleri barındıran Box
                Box(modifier = Modifier.fillMaxSize()) {
                    when (bottomNavItems[page]) {
                        Screen.Home -> HomeScreen(
                            isVisible = (pagerState.currentPage == page),
                            onNavigate = { route ->
                                when (route) {
                                Screen.Games.route -> onNavigateToGames()
                                Screen.Cat.route -> {
                                    val catIndex = bottomNavItems.indexOf(Screen.Cat)
                                    if (catIndex != -1) coroutineScope.launch { pagerState.animateScrollToPage(catIndex) }
                                }
                            }
                        })
                        Screen.Cat -> CatScreen(onNavigate = { route ->
                            if (route == Screen.Games.route) onNavigateToGames()
                        })
                        Screen.Statistics -> StatisticsScreen()
                        Screen.Profile -> ProfileScreen(onNavigateToLevelInfo = onNavigateToLevelInfo)
                        else -> Box(Modifier.fillMaxSize())
                    }
                }
            }
        }
        
        // 3. Floating Navigation Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            GlassFloatingBottomBar(
                items = bottomNavItems,
                selectedIndex = pagerState.currentPage,
                onItemSelected = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }

        // 4. Tutorial Overlay
        if (showTutorial) {
            TutorialOverlay(
                show = showTutorial,
                currentStep = currentStep,
                targets = currentTargets,
                onNext = {
                    if (currentStep < currentTargets.size - 1) {
                        currentStep++
                    } else {
                        showTutorial = false
                        val currentPage = bottomNavItems[pagerState.currentPage]
                        prefs.edit().putBoolean("tutorial_completed_v4_${currentPage.route}", true).apply()
                    }
                },
                onSkip = {
                    showTutorial = false
                    val currentPage = bottomNavItems[pagerState.currentPage]
                    prefs.edit().putBoolean("tutorial_completed_v4_${currentPage.route}", true).apply()
                }
            )
        }
    }
}

@Composable
fun AnimatedGradientBackground() {
    // Check if the current theme is dark comparing the background color
    val isDark = MaterialTheme.colorScheme.background.red == 0f && 
                 MaterialTheme.colorScheme.background.green == 0f && 
                 MaterialTheme.colorScheme.background.blue == 0f
    
    // In dark mode, draw nothing here! 
    // The background will be the completely dark Activity background (from MainScreen)
    // allowing bright particles from MainScreen to shine through transparent scaffolds.
    if (isDark) {
        return
    }

    val color1 = MaterialTheme.colorScheme.primaryContainer
    val color2 = MaterialTheme.colorScheme.secondaryContainer
    val color3 = Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(color1.copy(alpha = 0.4f), color2.copy(alpha = 0.4f), color3)
                )
            )
    ) {
        // Glass Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.3f))
        )
    }
}

@Composable
fun GlassFloatingBottomBar(
    items: List<Screen>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .shadow(24.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha=0.5f)),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, screen ->
                val isSelected = selectedIndex == index
                val animatedScale by animateFloatAsState(if (isSelected) 1.15f else 1f)
                val animatedColor by animateColorAsState(
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )

                // Highlight pill background
                val bgColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.5f) else Color.Transparent)

                IconButton(
                    onClick = { onItemSelected(index) },
                    modifier = Modifier.graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
                    .clip(CircleShape)
                    .background(bgColor)
                    .size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = androidx.compose.ui.res.stringResource(screen.titleResId),
                        tint = animatedColor,
                        modifier = Modifier.size(if(isSelected) 28.dp else 24.dp)
                    )
                }
            }
        }
    }
}
