package com.mert.paticat.ui.screens.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mert.paticat.R
import com.mert.paticat.ui.components.EntranceAnimation
import com.mert.paticat.ui.components.bounceClick
import com.mert.paticat.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    var currentEditingStepGoal by remember { mutableIntStateOf(10000) }
    var currentEditingWaterGoal by remember { mutableIntStateOf(2000) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.profile_title),
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ==================== HERO SECTION ====================
            EntranceAnimation {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        )
                        .bounceClick {
                            nameEditValue = uiState.userName
                            showNameDialog = true
                        }
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Avatar — layered glow circle
                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.35f),
                                                Color.White.copy(alpha = 0.12f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("😺", fontSize = 44.sp)
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    uiState.userName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                val levelTitleStr = stringResource(
                                    com.mert.paticat.domain.model.Cat.getLevelTitleResId(uiState.level)
                                )
                                Text(
                                    stringResource(R.string.level_label_with_title, uiState.level, levelTitleStr),
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // XP gradient bar
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onNavigateToLevelInfo() }
                                ) {
                                    val animatedProgress by animateFloatAsState(
                                        targetValue = uiState.levelProgress,
                                        animationSpec = tween(durationMillis = 1000),
                                        label = "xp_progress"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(Color.White.copy(alpha = 0.25f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(animatedProgress)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(AccentGold, AccentGold.copy(alpha = 0.70f))
                                                    )
                                                )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "${uiState.xpInCurrentLevel}/${uiState.xpNeededForNextLevel} XP",
                                            fontSize = 10.sp,
                                            color = Color.White.copy(alpha = 0.75f),
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            stringResource(R.string.stat_details),
                                            fontSize = 10.sp,
                                            color = AccentGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = {
                                    nameEditValue = uiState.userName
                                    showNameDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, null, tint = Color.White)
                                }
                                IconButton(onClick = {
                                    catNameEditValue = uiState.cat.name
                                    showCatNameDialog = true
                                }) {
                                    Icon(Icons.Default.Pets, null, tint = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Cat name pill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable {
                                    catNameEditValue = uiState.cat.name
                                    showCatNameDialog = true
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "🐾 ${uiState.cat.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                                Icon(
                                    Icons.Default.Edit,
                                    null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ==================== THEME SECTION ====================
            EntranceAnimation(delay = 150) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Section header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Palette,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                stringResource(R.string.theme_selection_title),
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Dark mode toggle row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.DarkMode,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                stringResource(R.string.theme_dark_mode),
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp
                            )
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { viewModel.updateDarkMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                    checkedThumbColor = Color.White
                                )
                            )
                        }

                        Text(
                            stringResource(R.string.theme_color_title),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(ThemeColor.values()) { theme ->
                                val isSelected = theme.name.equals(currentThemeName, ignoreCase = true) ||
                                        (theme == ThemeColor.Pink && currentThemeName == "Standard")
                                ThemeSelectionItem(theme, isSelected) {
                                    viewModel.selectThemeColor(theme.name)
                                }
                            }
                        }
                    }
                }
            }

            // ==================== STATS SECTION ====================
            EntranceAnimation(delay = 250) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    ProfileStatCard(
                        label = stringResource(R.string.stat_streak),
                        value = "${uiState.currentStreak} ${stringResource(R.string.unit_day)}",
                        icon = Icons.Default.Whatshot,
                        color = AccentGold,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatCard(
                        label = stringResource(R.string.stat_best_streak),
                        value = "${uiState.longestStreak} ${stringResource(R.string.unit_day)}",
                        icon = Icons.Default.EmojiEvents,
                        color = PremiumMint,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ==================== GOALS SECTION ====================
            EntranceAnimation(delay = 350) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Section header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            PremiumPink.copy(alpha = 0.35f),
                                            PremiumPink.copy(alpha = 0.08f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Flag,
                                null,
                                tint = PremiumPink,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            stringResource(R.string.goals_title),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    GoalItem(
                        title = stringResource(R.string.goal_steps_title),
                        value = "${uiState.dailyStepGoal} ${stringResource(R.string.unit_steps)}",
                        icon = Icons.Default.DirectionsWalk,
                        color = PremiumPink
                    ) {
                        currentEditingStepGoal = uiState.dailyStepGoal
                        showStepGoalDialog = true
                    }

                    GoalItem(
                        title = stringResource(R.string.goal_water_title),
                        value = "${uiState.dailyWaterGoal} ml",
                        icon = Icons.Default.LocalDrink,
                        color = PremiumBlue
                    ) {
                        currentEditingWaterGoal = uiState.dailyWaterGoal
                        showWaterGoalDialog = true
                    }
                }
            }

            // ==================== SETTINGS SECTION ====================
            EntranceAnimation(delay = 450) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Section header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            PremiumPurple.copy(alpha = 0.35f),
                                            PremiumPurple.copy(alpha = 0.08f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                null,
                                tint = PremiumPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            stringResource(R.string.settings_title),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            // Notifications toggle
                            SettingsRow(
                                icon = Icons.Default.Notifications,
                                iconColor = PremiumPurple,
                                title = stringResource(R.string.settings_notifications),
                                trailingContent = {
                                    Switch(
                                        checked = uiState.notificationsEnabled,
                                        onCheckedChange = { viewModel.updateNotifications(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedTrackColor = PremiumPurple,
                                            checkedThumbColor = Color.White
                                        )
                                    )
                                }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                            )

                            // Language switcher
                            val currentLocale =
                                androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().get(0)?.language
                                    ?: androidx.core.os.LocaleListCompat.getAdjustedDefault()[0]?.language
                                    ?: "tr"

                            SettingsRow(
                                icon = Icons.Default.Language,
                                iconColor = PremiumBlue,
                                title = stringResource(R.string.settings_language),
                                modifier = Modifier.clickable {
                                    val newLang = if (currentLocale == "tr") "en" else "tr"
                                    val appLocale = androidx.core.os.LocaleListCompat.forLanguageTags(newLang)
                                    androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(appLocale)
                                    viewModel.updateLocale(newLang)
                                },
                                trailingContent = {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(PremiumBlue.copy(alpha = 0.10f))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            if (currentLocale == "tr") "🇹🇷 Türkçe" else "🇬🇧 English",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = PremiumBlue,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                            )

                            // Privacy policy
                            SettingsRow(
                                icon = Icons.Default.Security,
                                iconColor = PremiumMint,
                                title = stringResource(R.string.settings_privacy_policy),
                                modifier = Modifier.clickable { showPrivacyDialog = true },
                                trailingContent = {
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }

    // ==================== DIALOGS ====================
    if (showNameDialog) {
        NewEditDialog(
            title = stringResource(R.string.dialog_edit_name_title),
            label = stringResource(R.string.dialog_edit_name_label),
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
            title = stringResource(R.string.dialog_edit_cat_title),
            label = stringResource(R.string.dialog_edit_cat_label),
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
            title = stringResource(R.string.goal_steps_title),
            value = currentEditingStepGoal,
            step = 500,
            minValue = 2000,
            maxValue = 100000,
            unit = stringResource(R.string.unit_steps),
            color = PremiumPink,
            infoNote = stringResource(R.string.goal_change_next_day_note),
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
            title = stringResource(R.string.goal_water_title),
            value = currentEditingWaterGoal,
            step = 250,
            maxValue = 10000,
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
        val googlePrivacyUrl = stringResource(R.string.url_google_privacy_policy)
        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(stringResource(R.string.settings_privacy_policy), fontWeight = FontWeight.Black) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(stringResource(R.string.privacy_policy_content))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        googlePrivacyUrl,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { uriHandler.openUri(googlePrivacyUrl) }
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }, shape = RoundedCornerShape(12.dp)) {
                    Text(stringResource(R.string.btn_confirm), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}

// ==================== THEME SELECTION ITEM ====================

@Composable
fun ThemeSelectionItem(theme: ThemeColor, isSelected: Boolean, onClick: () -> Unit) {
    val color = when (theme) {
        ThemeColor.Pink -> PremiumPink
        ThemeColor.Blue -> PremiumBlue
        ThemeColor.Green -> PremiumMint
        ThemeColor.Purple -> PremiumPurple
        ThemeColor.Orange -> PremiumPeach
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.bounceClick { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(if (isSelected) 60.dp else 54.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected)
                        Brush.radialGradient(listOf(color, color.copy(alpha = 0.6f)))
                    else
                        Brush.radialGradient(listOf(color.copy(alpha = 0.85f), color.copy(alpha = 0.55f)))
                )
                .then(
                    if (isSelected)
                        Modifier.border(3.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                    else
                        Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            theme.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==================== PROFILE STAT CARD ====================

@Composable
fun ProfileStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(color.copy(alpha = 0.18f), color.copy(alpha = 0.04f))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(color.copy(alpha = 0.30f), color.copy(alpha = 0.08f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 20.sp, color = color)
            Text(
                label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Legacy alias
@Composable
fun NewProfileStatItem(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) =
    ProfileStatCard(label, value, icon, color, modifier)

// ==================== GOAL ITEM ====================

@Composable
fun GoalItem(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(color.copy(alpha = 0.12f), color.copy(alpha = 0.03f))
                )
            )
            .bounceClick { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(color.copy(alpha = 0.30f), color.copy(alpha = 0.08f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    value,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Legacy alias
@Composable
fun NewGoalItem(title: String, value: String, icon: ImageVector, color: Color, onClick: () -> Unit) =
    GoalItem(title, value, icon, color, onClick)

// ==================== SETTINGS ROW ====================

@Composable
fun SettingsRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    modifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(17.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            title,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 15.sp
        )
        trailingContent()
    }
}

// Legacy alias
@Composable
fun NewSettingsSwitchRow(icon: ImageVector, title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    SettingsRow(
        icon = icon,
        iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = title,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = PremiumPink,
                    checkedThumbColor = Color.White
                )
            )
        }
    )
}

// ==================== DIALOGS ====================

@Composable
fun NewEditDialog(
    title: String,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
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
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = PremiumPink)
            ) {
                Text(stringResource(R.string.btn_save), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    maxValue: Int = Int.MAX_VALUE,
    unit: String,
    color: Color,
    infoNote: String? = null,
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
                if (infoNote != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(color.copy(alpha = 0.08f))
                            .padding(10.dp)
                    ) {
                        Text(
                            infoNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = color,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Minus button
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(color.copy(alpha = 0.20f), color.copy(alpha = 0.06f))
                                )
                            )
                            .clickable { if (value - step >= minValue) onValueChange(value - step) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Remove, null, tint = color, modifier = Modifier.size(28.dp))
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(140.dp)
                    ) {
                        Text("$value", fontWeight = FontWeight.Black, fontSize = 38.sp, color = color)
                        Text(
                            unit,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Plus button
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(color.copy(alpha = 0.20f), color.copy(alpha = 0.06f))
                                )
                            )
                            .clickable { if (value + step <= maxValue) onValueChange(value + step) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, null, tint = color, modifier = Modifier.size(28.dp))
                    }
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.75f))))
                    .clickable { onConfirm() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.btn_confirm),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp)
    )
}
