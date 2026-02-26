package com.mert.paticat.ui.screens.cat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mert.paticat.R
import com.mert.paticat.domain.model.CatMood
import com.mert.paticat.ui.components.EntranceAnimation
import com.mert.paticat.ui.components.bounceClick
import com.mert.paticat.ui.components.pulsate
import com.mert.paticat.ui.theme.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.mert.paticat.utils.SoundManager
import com.mert.paticat.ui.components.ParticleSystemCanvas
import com.mert.paticat.ui.components.rememberParticleSystem
import com.mert.paticat.ui.components.ParticleType
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.foundation.lazy.LazyColumn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatScreen(
    onNavigate: (String) -> Unit,
    viewModel: CatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // No need for playerChoice/catChoice here anymore as they are used in GamesScreen
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }
    
    // Dialog logic removed - now handled in GamesScreen

    // Ad Mob Rewarded Ad State removed - now in ViewModel
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val soundManager = remember { SoundManager(context) }
    var lastClickTime by remember { mutableStateOf(0L) }
    var catClicks by remember { mutableStateOf(0) }
    val particleSystem = rememberParticleSystem()
    var catCenter by remember { mutableStateOf(Offset.Zero) }

    // Heart animation state
    val petResult by viewModel.petResult.collectAsStateWithLifecycle()
    var showHeart by remember { mutableStateOf(false) }
    LaunchedEffect(petResult) {
        if (petResult == true) {
            showHeart = true
        }
    }
    LaunchedEffect(showHeart) {
        if (showHeart) {
            delay(800)
            showHeart = false
        }
    }

    // Ticker for sleep duration to refresh UI every second
    val isSleeping = viewModel.isCatSleeping()
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(isSleeping) {
        if (isSleeping) {
            while (true) {
                delay(1000)
                tick++
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }

    Scaffold(
        snackbarHost = { 
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 100.dp)
            ) 
        },
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(uiState.cat.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.cat_screen_subtitle), style = MaterialTheme.typography.bodySmall, color = TextMediumEmphasis)
                    }
                },
                actions = {
                    val isMaxFood = uiState.cat.foodPoints >= 150
                    
                    // Food Ad Button — show when not at max
                    if (!isMaxFood) {
                        val foodAdState = uiState.foodAdState
                        val adReady = foodAdState is AdState.Loaded && uiState.isNetworkAvailable
                        
                        // Decide content and interaction based on state
                        // Priority: Loading -> Error -> Ready/NotReady
                        
                        Surface(
                            color = if (adReady) PremiumMint else PremiumMint.copy(alpha = 0.3f), // Soluk renk
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .then(
                                    // Clickable only if ready or error (to retry)
                                    if (adReady) Modifier.bounceClick {
                                         val activity = context as? Activity
                                         activity?.let { viewModel.showFoodAd(it) }
                                    } else Modifier
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when (foodAdState) {
                                    AdState.Loading -> {
                                        // Loading Spinner
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    }
                                    AdState.Error -> {
                                        // Error Icon
                                        Text("⚠️", fontSize = 11.sp)
                                    }
                                    else -> {
                                        // Normal State (Loaded or Idle)
                                        Text("📺", fontSize = 11.sp)
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("+15", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                    
                    val animatedFoodPoints by animateIntAsState(targetValue = uiState.cat.foodPoints, label = "foodPts")
                    Surface(
                        color = if (isMaxFood) MaterialTheme.colorScheme.errorContainer else PremiumMint.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp), // Daha yuvarlak/büyük köşe
                        modifier = Modifier.clickable { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.showFoodStatus() 
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), // Daha büyük padding
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🍖", fontSize = 18.sp) // Daha büyük ikon
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isMaxFood) "$animatedFoodPoints (!)" else animatedFoodPoints.toString(),
                                fontWeight = FontWeight.Black, // Daha kalın font
                                fontSize = 18.sp, // Daha büyük font
                                color = if (isMaxFood) MaterialTheme.colorScheme.error else PremiumMintDark
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(horizontal = 16.dp, vertical = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cat Visual Area
                Box(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Modern Gradient Background with Pulsing
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(PremiumPink.copy(alpha = 0.15f), Color.Transparent)
                                )
                            )
                    )
                    
                    val isSleeping = viewModel.isCatSleeping()
                    val currentImageRes = getCatImageResource(uiState.cat, isSleeping)
                    
                    Crossfade(targetState = currentImageRes, animationSpec = tween(600), label = "catMood") { resId ->
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = null,
                            modifier = Modifier
                                .size(280.dp)
                                .onGloballyPositioned { coords ->
                                    val bounds = coords.boundsInWindow()
                                    catCenter = Offset(bounds.center.x, bounds.center.y)
                                }
                                .clickable(enabled = !isSleeping) {
                                    val now = System.currentTimeMillis()
                                    if (now - lastClickTime < 500) {
                                        catClicks++
                                    } else {
                                        catClicks = 1
                                    }
                                    lastClickTime = now

                                    if (catClicks > 3) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        catClicks = 0
                                    } else {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        soundManager.playPurr()
                                    }
                                    viewModel.petCat()
                                },
                            contentScale = ContentScale.Fit
                        )
                    }

                    // Floating Heart Animation
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showHeart,
                        enter = fadeIn(animationSpec = tween(200)),
                        exit = fadeOut(animationSpec = tween(600)) +
                               slideOutVertically(animationSpec = tween(600)) { -it },
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        androidx.compose.material3.Text(
                            text = "❤️",
                            fontSize = androidx.compose.ui.unit.TextUnit(48f, androidx.compose.ui.unit.TextUnitType.Sp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }
                
                // Cat Stats Panel
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (MaterialTheme.colorScheme.background == BackgroundDark) Color.Black.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        val animatedHunger by animateIntAsState(targetValue = uiState.cat.hunger, label = "hunger")
                        val animatedEnergy by animateIntAsState(targetValue = uiState.cat.energy, label = "energy")
                        val animatedHappy by animateIntAsState(targetValue = uiState.cat.happiness, label = "happiness")
                        
                        // "Tokluk" replaces "Açlık" to be consistent with 100=Good logic
                        PremiumStatBar(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.cat_stat_hunger), animatedHunger, PremiumPeach, "🍖")
                        Spacer(modifier = Modifier.height(20.dp))
                        PremiumStatBar(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.cat_stat_energy), animatedEnergy, PremiumBlue, "⚡")
                        Spacer(modifier = Modifier.height(20.dp))
                        PremiumStatBar(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.cat_stat_happiness), animatedHappy, PremiumPink, "💖")
                    }
                }

                // Ad Mob Rewarded Ad State
                val isSleeping = viewModel.isCatSleeping()
                
                 LaunchedEffect(uiState.cat.foodPoints, uiState.isNetworkAvailable, uiState.foodAdState) {
                      // Preload whenever food points are not at max and not already loaded/loading
                      if (uiState.cat.foodPoints < 150 && uiState.isNetworkAvailable && uiState.foodAdState is AdState.Idle) {
                          viewModel.loadFoodAd()
                      }
                 }
                
                LaunchedEffect(uiState.cat.energy, isSleeping, uiState.isNetworkAvailable, uiState.sleepAdState) {
                    // Preload if energy is low (< 30) OR sleeping
                    val shouldLoadSleepAd = (isSleeping || uiState.cat.energy < 30)
                    
                    if (shouldLoadSleepAd && uiState.isNetworkAvailable && uiState.sleepAdState is AdState.Idle) {
                        viewModel.loadSleepAd()
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                // Interaction Area
                val isCatSleeping = viewModel.isCatSleeping()
                
                if (isCatSleeping) {
                    // Sleep State UI
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1. Sleep Info Card (Horizontal Style)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = PremiumBlue.copy(alpha = 0.1f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 24.dp, horizontal = 24.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "💤 " + androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.cat_status_sleeping),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PremiumBlue
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                val remainingTime = remember(tick) { viewModel.getSleepRemainingTime() }
                                Text(
                                    text = remainingTime,
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. Ad Button & Status (Independent, Horizontal)
                        val sleepAdState = uiState.sleepAdState
                        val adReady = sleepAdState is AdState.Loaded && uiState.isNetworkAvailable
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .then(
                                    if (adReady) Modifier.bounceClick {
                                         val activity = context as? Activity
                                         activity?.let { viewModel.showSleepAd(it) }
                                    } else Modifier
                                ),
                            shape = RoundedCornerShape(16.dp),
                            color = if (adReady) PremiumPeach else PremiumPeach.copy(alpha = 0.3f),
                            shadowElevation = if (adReady) 4.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("📺", fontSize = 24.sp, color = if(adReady) Color.Unspecified else Color.White.copy(alpha=0.5f))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.cat_ad_reduce_sleep),
                                    fontSize = 18.sp, 
                                    fontWeight = FontWeight.Bold, 
                                    color = if(adReady) Color.White else Color.White.copy(alpha=0.5f)
                                )
                            }
                        }

                        // Status Text (Loading, No Internet or Error)
                        val statusText = when {
                            !uiState.isNetworkAvailable -> androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.cat_no_internet)
                            sleepAdState is AdState.Loading -> androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.cat_ad_loading)
                            sleepAdState is AdState.Error -> androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.cat_ad_error)
                            else -> null
                        }

                        if (statusText != null && !adReady) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (!uiState.isNetworkAvailable || sleepAdState is AdState.Error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                        }
                    }
                } else {
                    // Normal Interaction Rows
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.Top
                    ) {
                    // Besle
                        InteractionBtn(
                            text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.cat_action_feed),
                            emoji = "🍖",
                            color = PremiumPeach,
                            requirement = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.cat_feed_cost),
                            enabled = uiState.canFeed,
                            onClick = { 
                                particleSystem.emit(
                                    x = catCenter.x,
                                    y = catCenter.y - 100f,
                                    count = 15,
                                    type = ParticleType.HEART,
                                    color = Color(0xFFFF4081)
                                )
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.feedCat() 
                            }
                        )
                        
                        // Oyna
                        InteractionBtn(
                            text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.cat_action_play),
                            emoji = "🎮",
                            color = PremiumPink,
                            requirement = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.cat_play_room),
                            enabled = uiState.cat.energy >= 5,
                            onClick = { 
                                if (uiState.cat.energy >= 5) {
                                    onNavigate(com.mert.paticat.ui.navigation.Screen.Games.route)
                                } else {
                                    viewModel.setMessage(context.getString(com.mert.paticat.R.string.home_low_energy_snackbar))
                                }
                            }
                        )
                        
                        // Uyut
                        val sleepEnabled = uiState.cat.energy < 40
                        InteractionBtn(
                            text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.cat_action_sleep),
                            emoji = "💤",
                            color = PremiumBlue,
                            requirement = if (sleepEnabled) 
                                androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.cat_full_energy)
                            else 
                                androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.cat_not_tired_info, uiState.cat.energy), 
                            enabled = sleepEnabled,
                            onClick = { 
                                haptic.performHapticFeedback(if (sleepEnabled) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove)
                                viewModel.sleepCat() 
                            }
                        )
                    }
                }
                
                // Add bottom padding for the navigation bar
                Spacer(modifier = Modifier.height(110.dp))
            }
            
            // Draw particles on top
            ParticleSystemCanvas(
                state = particleSystem,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun GameChoiceBtn(emoji: String, choice: RockPaperScissors, onClick: (RockPaperScissors) -> Unit) {
    Surface(
        modifier = Modifier
            .size(64.dp)
            .bounceClick { onClick(choice) },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 28.sp)
        }
    }
}

@Composable
fun PremiumStatBar(label: String, value: Int, color: Color, icon: String) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("$icon $label", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.unit_percentage, value),
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant, // Adapted for theme
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
fun InteractionBtn(
    text: String, 
    emoji: String, 
    color: Color, 
    requirement: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val displayColor = if (enabled) color else Color.Gray
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .size(72.dp)
                .shadow(if (enabled) 8.dp else 0.dp, CircleShape)
                .bounceClick { if (enabled) onClick() },
            shape = CircleShape,
            color = displayColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 32.sp, color = if(enabled) Color.Unspecified else Color.White.copy(alpha=0.5f))
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(text, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(2.dp))
        Text(requirement, fontSize = 11.sp, color = if (enabled) color else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun getCatImageResource(cat: com.mert.paticat.domain.model.Cat, isSleeping: Boolean): Int {
    if (isSleeping) return com.mert.paticat.R.drawable.cat_sleep
    
    // Priority 1: Hunger (If starving, it overrides everything else unless sleeping)
    // < 30 means somewhat hungry.
    if (cat.hunger < 30) {
        return com.mert.paticat.R.drawable.cat_hungry 
    }
    
    // Priority 2: Energy (Tired)
    // < 30 means tired.
    if (cat.energy < 30) {
        // Tired and happy? or Tired and sad?
        return if (cat.happiness > 50) {
            com.mert.paticat.R.drawable.cat_tired_happy
        } else {
            com.mert.paticat.R.drawable.cat_tired_sad
        }
    }
    
    // Priority 3: Happiness/Excitement
    if (cat.happiness >= 80) {
        return com.mert.paticat.R.drawable.cat_excited
    }
    
    // Sad Check
    if (cat.happiness < 40) {
        return com.mert.paticat.R.drawable.cat_sad
    }
    
    // Default Happy
    return com.mert.paticat.R.drawable.cat_happy
}
