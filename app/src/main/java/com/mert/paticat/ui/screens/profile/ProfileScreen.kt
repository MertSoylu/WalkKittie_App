package com.mert.paticat.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mert.paticat.ui.components.EntranceAnimation
import com.mert.paticat.ui.components.bounceClick
import com.mert.paticat.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToLevelInfo: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentThemeName by viewModel.currentThemeColor.collectAsState(initial = "Pink")
    val isDarkMode by viewModel.isDarkMode.collectAsState(initial = false)
    
    var showNameDialog by remember { mutableStateOf(false) }
    var showStepGoalDialog by remember { mutableStateOf(false) }
    var showWaterGoalDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    
    var showCatNameDialog by remember { mutableStateOf(false) }
    
    var nameEditValue by remember { mutableStateOf("") }
    var catNameEditValue by remember { mutableStateOf("") }
    
    // Dialog state for goals
    var currentEditingStepGoal by remember { mutableIntStateOf(10000) }
    var currentEditingWaterGoal by remember { mutableIntStateOf(2000) }
    
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.profile_title), 
                        fontWeight = FontWeight.Black, 
                        color = MaterialTheme.colorScheme.onSurface 
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent // Transparent because we have animated background in MainPager
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // --- Hero Section ---
            EntranceAnimation {
                Card(
                    modifier = Modifier.fillMaxWidth().bounceClick { 
                        nameEditValue = uiState.userName
                        showNameDialog = true 
                    },
                    shape = RoundedCornerShape(32.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(80.dp), 
                                    shape = CircleShape, 
                                    color = Color.White.copy(alpha = 0.25f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) { 
                                        Text("😺", fontSize = 42.sp) 
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(20.dp))
                                
                                Column {
                                    Text(
                                        uiState.userName, 
                                        style = MaterialTheme.typography.headlineSmall, 
                                        fontWeight = FontWeight.Black, 
                                        color = Color.White
                                    )
                                    val levelTitleStr = androidx.compose.ui.res.stringResource(
                                        com.mert.paticat.domain.model.Cat.getLevelTitleResId(uiState.level)
                                    )
                                    Text(
                                        androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.level_label_with_title, uiState.level, levelTitleStr), 
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Medium
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // XP Progress Bar - Tap to see level details
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onNavigateToLevelInfo() }
                                    ) {
                                        val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
                                            targetValue = uiState.levelProgress,
                                            animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000)
                                        )
                                        LinearProgressIndicator(
                                            progress = { animatedProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = AccentGold,
                                            trackColor = Color.White.copy(alpha = 0.3f),
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${uiState.xpInCurrentLevel}/${uiState.xpNeededForNextLevel} XP",
                                                fontSize = 10.sp,
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.stat_details),
                                                fontSize = 10.sp,
                                                color = AccentGold,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                
                                Spacer(modifier = Modifier.weight(1f))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(onClick = { 
                                        nameEditValue = uiState.userName
                                        showNameDialog = true 
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                                    }
                                    
                                    IconButton(onClick = { 
                                        catNameEditValue = uiState.cat.name
                                        showCatNameDialog = true 
                                    }) {
                                        Icon(Icons.Default.Pets, contentDescription = null, tint = Color.White)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Cat Name Display — Clickable to edit
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        catNameEditValue = uiState.cat.name
                                        showCatNameDialog = true
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "🐾 ${uiState.cat.name}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // --- Theme Selection Section (NEW) ---
            EntranceAnimation(delay = 150) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.theme_selection_title), 
                        fontWeight = FontWeight.Black, 
                        fontSize = 18.sp, 
                        color = MaterialTheme.colorScheme.onSurface 
                    )
                    
                    NewSettingsSwitchRow(
                        icon = Icons.Default.DarkMode,
                        title = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.theme_dark_mode),
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.updateDarkMode(it) }
                    )
                    
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.theme_color_title), 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 14.sp, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(ThemeColor.values()) { theme ->
                            // Simple mapping check or loose equality
                            val isSelected = theme.name.equals(currentThemeName, ignoreCase = true) || 
                                             (theme == ThemeColor.Pink && currentThemeName == "Standard") // Backwards compat
                            
                            ThemeSelectionItem(theme, isSelected) {
                                viewModel.selectThemeColor(theme.name)
                            }
                        }
                    }
                }
            }
            
            // --- Stats Section ---
            EntranceAnimation(delay = 250) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    NewProfileStatItem(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.stat_streak), "${uiState.currentStreak} ${androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.unit_day)}", Icons.Default.Whatshot, AccentGold, Modifier.weight(1f))
                    NewProfileStatItem(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.stat_best_streak), "${uiState.longestStreak} ${androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.unit_day)}", Icons.Default.EmojiEvents, PremiumMint, Modifier.weight(1f))
                }
            }
            
            // --- Goals Section ---
            EntranceAnimation(delay = 350) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.goals_title), 
                        fontWeight = FontWeight.Black, 
                        fontSize = 18.sp, 
                        color = MaterialTheme.colorScheme.onSurface 
                    )
                    
                    NewGoalItem(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.goal_steps_title), "${uiState.dailyStepGoal} ${androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.unit_steps)}", Icons.Default.DirectionsWalk, PremiumPink) {
                        currentEditingStepGoal = uiState.dailyStepGoal
                        showStepGoalDialog = true
                    }
                    
                    NewGoalItem(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.goal_water_title), "${uiState.dailyWaterGoal} ml", Icons.Default.LocalDrink, PremiumBlue) {
                        currentEditingWaterGoal = uiState.dailyWaterGoal
                        showWaterGoalDialog = true
                    }
                }
            }
            
            // --- Settings Section ---
            EntranceAnimation(delay = 450) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.settings_title), 
                        fontWeight = FontWeight.Black, 
                        fontSize = 18.sp, 
                        color = MaterialTheme.colorScheme.onSurface 
                    )
                    
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            NewSettingsSwitchRow(
                                icon = Icons.Default.Notifications,
                                title = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.settings_notifications),
                                checked = uiState.notificationsEnabled,
                                onCheckedChange = { viewModel.updateNotifications(it) }
                            )
                            
                            // Language Switcher (Simple toggle for now for demo)
                            val context = androidx.compose.ui.platform.LocalContext.current
                            val currentLocale = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().get(0)?.language 
                                ?: androidx.core.os.LocaleListCompat.getAdjustedDefault()[0]?.language 
                                ?: "tr"
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Simple toggle logic: if en -> tr, if tr -> en
                                        val newLang = if (currentLocale == "tr") "en" else "tr"
                                        val appLocale = androidx.core.os.LocaleListCompat.forLanguageTags(newLang)
                                        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(appLocale)
                                        viewModel.updateLocale(newLang)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.settings_language),
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = if(currentLocale == "tr") "🇹🇷 Türkçe" else "🇬🇧 English",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showPrivacyDialog = true }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.settings_privacy_policy),
                                    modifier = Modifier.weight(1f),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp
                                )
                                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(110.dp))
        }
    }
    
    // Dialogs (Keeping existing logic)
    if (showNameDialog) {
        NewEditDialog(
            title = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.dialog_edit_name_title), 
            label = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.dialog_edit_name_label), 
            value = nameEditValue, 
            onValueChange = { if (it.length <= 20) nameEditValue = it },
            onConfirm = { 
                if (nameEditValue.isNotBlank()) viewModel.updateUserName(nameEditValue.trim())
                showNameDialog = false
            },
            onDismiss = { showNameDialog = false }
        )
    }

    if (showCatNameDialog) {
        NewEditDialog(
            title = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.dialog_edit_cat_title), 
            label = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.dialog_edit_cat_label), 
            value = catNameEditValue, 
            onValueChange = { if (it.length <= 20) catNameEditValue = it },
            onConfirm = { 
                if (catNameEditValue.isNotBlank()) viewModel.updateCatName(catNameEditValue.trim())
                showCatNameDialog = false
            },
            onDismiss = { showCatNameDialog = false }
        )
    }

    if (showStepGoalDialog) {
        NewStepperDialog(
            title = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.goal_steps_title),
            value = currentEditingStepGoal,
            step = 500,
            minValue = 2000,
            unit = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.unit_steps),
            color = PremiumPink,
            onValueChange = { currentEditingStepGoal = it },
            onConfirm = {
                viewModel.updateStepGoal(currentEditingStepGoal)
                showStepGoalDialog = false
            },
            onDismiss = { showStepGoalDialog = false }
        )
    }

    if (showWaterGoalDialog) {
        NewStepperDialog(
            title = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.goal_water_title),
            value = currentEditingWaterGoal,
            step = 250,
            unit = "ml",
            color = PremiumBlue,
            onValueChange = { currentEditingWaterGoal = it },
            onConfirm = {
                viewModel.updateWaterGoal(currentEditingWaterGoal)
                showWaterGoalDialog = false
            },
            onDismiss = { showWaterGoalDialog = false }
        )
    }

    if (showPrivacyDialog) {
        val googlePrivacyUrl = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.url_google_privacy_policy)
        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
        
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.settings_privacy_policy), fontWeight = FontWeight.Black) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.privacy_policy_content))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = googlePrivacyUrl,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { uriHandler.openUri(googlePrivacyUrl) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.btn_confirm), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
fun ThemeSelectionItem(theme: ThemeColor, isSelected: Boolean, onClick: () -> Unit) {
    val color = when(theme) {
        ThemeColor.Pink -> PremiumPink
        ThemeColor.Blue -> PremiumBlue
        ThemeColor.Green -> PremiumMint
        ThemeColor.Purple -> PremiumPurple
        ThemeColor.Orange -> PremiumPeach
    }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.bounceClick { onClick() }) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = theme.name,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// --- Components (Reusing existing ones) ---

@Composable
fun NewProfileStatItem(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier, 
        shape = RoundedCornerShape(24.dp), 
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(), 
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 20.sp, color = color)
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NewGoalItem(title: String, value: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().bounceClick { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@Composable
fun NewSettingsSwitchRow(icon: ImageVector, title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), 
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title, 
            modifier = Modifier.weight(1f), 
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp
        )
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange, 
            colors = SwitchDefaults.colors(
                checkedTrackColor = PremiumPink,
                checkedThumbColor = Color.White
            )
        )
    }
}

@Composable
fun NewEditDialog(title: String, label: String, value: String, onValueChange: (String) -> Unit, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface) },
        text = { 
            OutlinedTextField(
                value = value, 
                onValueChange = onValueChange, 
                label = { Text(label) }, 
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(top = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PremiumPink,
                    cursorColor = PremiumPink
                )
            ) 
        },
        confirmButton = { 
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = PremiumPink)) { 
                Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.btn_save), fontWeight = FontWeight.Bold) 
            } 
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { 
                Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.btn_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant) 
            } 
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun NewStepperDialog(
    title: String, 
    value: Int, 
    step: Int, 
    minValue: Int = step,
    unit: String, 
    color: Color, 
    onValueChange: (Int) -> Unit, 
    onConfirm: () -> Unit, 
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                title, 
                fontWeight = FontWeight.Black, 
                textAlign = TextAlign.Center, 
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface
            ) 
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { 
                             if (value - step >= minValue) {
                                 onValueChange(value - step) 
                             }
                        },
                        modifier = Modifier.size(56.dp).background(color.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, null, tint = color, modifier = Modifier.size(32.dp))
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(140.dp)) {
                        Text("$value", fontWeight = FontWeight.Black, fontSize = 38.sp, color = color)
                        Text(unit, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    }
                    
                    IconButton(
                        onClick = { onValueChange(value + step) },
                        modifier = Modifier.size(56.dp).background(color.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Add, null, tint = color, modifier = Modifier.size(32.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = color),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text(androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.btn_confirm), fontWeight = FontWeight.Black, fontSize = 16.sp) }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp)
    )
}
