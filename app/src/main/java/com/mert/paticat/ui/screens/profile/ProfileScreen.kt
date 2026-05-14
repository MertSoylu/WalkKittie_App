package com.mert.paticat.ui.screens.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.activity.ComponentActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import com.mert.paticat.ui.components.marshmallow.ActionPillButton
import com.mert.paticat.ui.components.marshmallow.MarshmallowDialog
import com.mert.paticat.ui.components.marshmallow.ChipPill
import com.mert.paticat.ui.components.marshmallow.PillowCard
import com.mert.paticat.ui.components.marshmallow.SectionHeader2
import com.mert.paticat.ui.components.marshmallow.SoftSwitch
import com.mert.paticat.ui.components.marshmallow.TintedPillowCard
import com.mert.paticat.ui.components.marshmallow.pillowPress
import com.mert.paticat.ui.components.marshmallow.softEntrance
import com.mert.paticat.ui.theme.AccentGold
import com.mert.paticat.ui.theme.PremiumBlue
import com.mert.paticat.ui.theme.PremiumMint
import com.mert.paticat.ui.theme.PremiumPeach
import com.mert.paticat.ui.theme.PremiumPink
import com.mert.paticat.ui.theme.PremiumPurple
import com.mert.paticat.ui.theme.ThemeColor
import com.mert.paticat.ui.theme.resolveThemeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToLevelInfo: () -> Unit = {},
    onResetComplete: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Defaults aligned with UserPreferencesRepository: SELECTED_THEME default = "Standard",
    // IS_DARK_MODE default = false. Avoids a brief mismatched render before DataStore emits.
    val currentThemeName by viewModel.currentThemeColor.collectAsStateWithLifecycle(initialValue = "Standard")
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle(initialValue = false)
    val resetComplete by viewModel.resetComplete.collectAsStateWithLifecycle()

    LaunchedEffect(resetComplete) {
        if (resetComplete) {
            onResetComplete()
            viewModel.acknowledgeReset()
        }
    }

    // TODO: Hoist dialog state to ProfileViewModel for config-change persistence.
    // Currently a rotation while a dialog is open silently dismisses it. Move
    // showNameDialog / showCatNameDialog / showStepGoalDialog / showWaterGoalDialog /
    // showPrivacyDialog / showResetDialog into a sealed ProfileDialog state in the VM
    // and drive these flags off it. (Skipped — non-trivial refactor.)
    var showNameDialog by remember { mutableStateOf(false) }
    var showCatNameDialog by remember { mutableStateOf(false) }
    var showStepGoalDialog by remember { mutableStateOf(false) }
    var showWaterGoalDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    var nameEditValue by remember { mutableStateOf("") }
    var catNameEditValue by remember { mutableStateOf("") }
    var currentEditingStepGoal by remember { mutableIntStateOf(10000) }
    var currentEditingWaterGoal by remember { mutableIntStateOf(2000) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        containerColor = Color.Transparent,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(2.dp))

            // Hero
            Box(modifier = Modifier.softEntrance()) {
                ProfileHero(
                    userName = uiState.userName,
                    catName = uiState.cat.name,
                    level = uiState.level,
                    levelProgress = uiState.levelProgress,
                    xpInCurrentLevel = uiState.xpInCurrentLevel.toInt(),
                    xpNeededForNextLevel = uiState.xpNeededForNextLevel.toInt(),
                    onEditName = {
                        nameEditValue = uiState.userName
                        showNameDialog = true
                    },
                    onEditCatName = {
                        catNameEditValue = uiState.cat.name
                        showCatNameDialog = true
                    },
                    onLevelInfo = onNavigateToLevelInfo,
                )
            }

            // Theme
            Box(modifier = Modifier.softEntrance(delayMillis = 80)) {
                PillowCard(modifier = Modifier.fillMaxWidth(), contentPadding = 18.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader2(
                            title = stringResource(R.string.theme_selection_title),
                            leadingEmoji = "🎨",
                        )

                        // TODO: Add a live light/dark theme preview thumbnail above this
                        // toggle so users can see the effect before flipping. Skipped here —
                        // this is a feature add, not a polish item.
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.DarkMode, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.theme_dark_mode),
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                            )
                            SoftSwitch(checked = isDarkMode, onCheckedChange = { viewModel.updateDarkMode(it) })
                        }

                        Text(
                            text = stringResource(R.string.theme_color_title),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp),
                        ) {
                            items(ThemeColor.values(), key = { it.name }) { theme ->
                                val isSelected = resolveThemeColor(currentThemeName) == theme
                                ThemeSelectionItem(theme, isSelected) {
                                    viewModel.selectThemeColor(theme.name)
                                }
                            }
                        }
                    }
                }
            }

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth().softEntrance(delayMillis = 160),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ProfileStatCard(
                    label = stringResource(R.string.stat_streak),
                    value = "${uiState.currentStreak} ${stringResource(R.string.unit_day)}",
                    icon = Icons.Default.Whatshot,
                    color = AccentGold,
                    modifier = Modifier.weight(1f),
                )
                ProfileStatCard(
                    label = stringResource(R.string.stat_best_streak),
                    value = "${uiState.longestStreak} ${stringResource(R.string.unit_day)}",
                    icon = Icons.Default.EmojiEvents,
                    color = PremiumMint,
                    modifier = Modifier.weight(1f),
                )
            }

            // Goals
            Column(
                modifier = Modifier.fillMaxWidth().softEntrance(delayMillis = 240),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SectionHeader2(
                    title = stringResource(R.string.goals_title),
                    leadingEmoji = "🎯",
                )
                GoalItem(
                    title = stringResource(R.string.goal_steps_title),
                    value = "${uiState.dailyStepGoal} ${stringResource(R.string.unit_steps)}",
                    icon = Icons.Default.DirectionsWalk,
                    color = PremiumPink,
                    onClick = {
                        currentEditingStepGoal = uiState.dailyStepGoal
                        showStepGoalDialog = true
                    },
                )
                GoalItem(
                    title = stringResource(R.string.goal_water_title),
                    value = "${uiState.dailyWaterGoal} ${stringResource(R.string.unit_ml)}",
                    icon = Icons.Default.LocalDrink,
                    color = PremiumBlue,
                    onClick = {
                        currentEditingWaterGoal = uiState.dailyWaterGoal
                        showWaterGoalDialog = true
                    },
                )
            }

            // Settings
            Column(
                modifier = Modifier.fillMaxWidth().softEntrance(delayMillis = 320),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SectionHeader2(
                    title = stringResource(R.string.settings_title),
                    leadingEmoji = "⚙️",
                )
                PillowCard(modifier = Modifier.fillMaxWidth(), contentPadding = 4.dp) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Notifications,
                            iconColor = PremiumPurple,
                            title = stringResource(R.string.settings_notifications),
                            trailingContent = {
                                SoftSwitch(
                                    checked = uiState.notificationsEnabled,
                                    onCheckedChange = { viewModel.updateNotifications(it) },
                                )
                            },
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                        )

                        val currentLocale = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().get(0)?.language
                            ?: androidx.core.os.LocaleListCompat.getAdjustedDefault()[0]?.language
                            ?: "tr"

                        // TODO(A4): expose isLocaleSwitching from ProfileViewModel so DataStore write
                        // completion gates AppCompatDelegate.setApplicationLocales.
                        var isLocaleSwitching by remember { mutableStateOf(false) }
                        // Flow emission of currentLocale change indicates the write landed.
                        LaunchedEffect(currentLocale) { isLocaleSwitching = false }

                        SettingsRow(
                            icon = Icons.Default.Language,
                            iconColor = PremiumBlue,
                            title = stringResource(R.string.settings_language),
                            modifier = Modifier.clickable(enabled = !isLocaleSwitching) {
                                val newLang = if (currentLocale == "tr") "en" else "tr"
                                isLocaleSwitching = true
                                viewModel.updateLocale(newLang)
                                val appLocale = androidx.core.os.LocaleListCompat.forLanguageTags(newLang)
                                androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(appLocale)
                            },
                            trailingContent = {
                                if (isLocaleSwitching) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = PremiumBlue,
                                    )
                                } else {
                                    ChipPill(
                                        text = stringResource(if (currentLocale == "tr") R.string.language_turkish else R.string.language_english),
                                        leadingEmoji = if (currentLocale == "tr") "🇹🇷" else "🇬🇧",
                                        backgroundColor = PremiumBlue.copy(alpha = 0.14f),
                                        contentColor = PremiumBlue,
                                    )
                                }
                            },
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                        )

                        SettingsRow(
                            icon = Icons.Default.Security,
                            iconColor = PremiumMint,
                            title = stringResource(R.string.settings_privacy_policy),
                            modifier = Modifier.clickable { showPrivacyDialog = true },
                            trailingContent = {
                                Icon(
                                    Icons.Default.ChevronRight,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                                )
                            },
                        )

                        val activity = LocalContext.current as? ComponentActivity
                        val consentInfo = remember(activity) {
                            activity?.let { UserMessagingPlatform.getConsentInformation(it) }
                        }
                        val showPrivacyOptions = remember(consentInfo) {
                            consentInfo?.privacyOptionsRequirementStatus ==
                                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
                        }
                        if (showPrivacyOptions && activity != null) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                            )
                            SettingsRow(
                                icon = Icons.Default.ManageAccounts,
                                iconColor = PremiumBlue,
                                title = stringResource(R.string.settings_privacy_options),
                                modifier = Modifier.clickable {
                                    UserMessagingPlatform.showPrivacyOptionsForm(activity) {}
                                },
                                trailingContent = {
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                                    )
                                },
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                        )

                        SettingsRow(
                            icon = Icons.Default.Warning,
                            iconColor = PremiumPeach,
                            title = stringResource(R.string.reset_progress),
                            modifier = Modifier.clickable { showResetDialog = true },
                            trailingContent = {
                                Icon(
                                    Icons.Default.ChevronRight,
                                    null,
                                    tint = PremiumPeach.copy(alpha = 0.7f),
                                )
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }

    // Dialogs
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
            onDismiss = { showNameDialog = false },
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
            onDismiss = { showCatNameDialog = false },
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
            onDismiss = { showStepGoalDialog = false },
        )
    }
    if (showWaterGoalDialog) {
        NewStepperDialog(
            title = stringResource(R.string.goal_water_title),
            value = currentEditingWaterGoal,
            step = 250,
            minValue = 500,
            maxValue = 10000,
            unit = stringResource(R.string.unit_ml),
            color = PremiumBlue,
            onValueChange = { currentEditingWaterGoal = it },
            onConfirm = {
                viewModel.updateWaterGoal(currentEditingWaterGoal)
                showWaterGoalDialog = false
            },
            onDismiss = { showWaterGoalDialog = false },
        )
    }
    if (showPrivacyDialog) {
        val googlePrivacyUrl = stringResource(R.string.url_google_privacy_policy)
        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(stringResource(R.string.settings_privacy_policy), fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(stringResource(R.string.privacy_policy_content))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = googlePrivacyUrl,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { uriHandler.openUri(googlePrivacyUrl) },
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }, shape = RoundedCornerShape(16.dp)) {
                    Text(stringResource(R.string.btn_confirm), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(28.dp),
        )
    }
    if (showResetDialog) {
        MarshmallowDialog(onDismissRequest = { showResetDialog = false }) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = stringResource(R.string.icon_warning),
                    tint = PremiumPeach,
                    modifier = Modifier.size(40.dp),
                )
                Text(
                    text = stringResource(R.string.reset_progress),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.reset_confirm_message),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.reset_irreversible_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ActionPillButton(
                        text = stringResource(R.string.btn_cancel),
                        modifier = Modifier.weight(1f),
                        onClick = { showResetDialog = false },
                    )
                    ActionPillButton(
                        text = stringResource(R.string.btn_confirm),
                        modifier = Modifier.weight(1f),
                        backgroundColor = PremiumPeach,
                        onClick = {
                            showResetDialog = false
                            viewModel.resetAllData()
                        },
                    )
                }
            }
        }
    }
}

// ==================== HERO ====================

@Composable
private fun ProfileHero(
    userName: String,
    catName: String,
    level: Int,
    levelProgress: Float,
    xpInCurrentLevel: Int,
    xpNeededForNextLevel: Int,
    onEditName: () -> Unit,
    onEditCatName: () -> Unit,
    onLevelInfo: () -> Unit,
) {
    val brandColor = MaterialTheme.colorScheme.primary
    val heroGradient = listOf(
        brandColor.copy(alpha = 0.18f),
        brandColor.copy(alpha = 0.34f),
    )
    TintedPillowCard(
        gradient = heroGradient,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 22.dp,
        onClick = onEditName,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.32f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("😺", fontSize = 44.sp)
                }
                Spacer(Modifier.width(18.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = brandColor,
                    )
                    val levelTitleStr = stringResource(
                        com.mert.paticat.domain.model.Cat.getLevelTitleResId(level),
                    )
                    Text(
                        text = stringResource(R.string.level_label_with_title, level, levelTitleStr),
                        color = brandColor.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Column(modifier = Modifier.fillMaxWidth().pillowPress(onClick = onLevelInfo)) {
                        val animatedProgress by animateFloatAsState(
                            targetValue = levelProgress,
                            animationSpec = tween(1000),
                            label = "xp_progress",
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color.White.copy(alpha = 0.45f)),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(
                                        Brush.horizontalGradient(listOf(AccentGold, AccentGold.copy(alpha = 0.7f))),
                                    ),
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "$xpInCurrentLevel/$xpNeededForNextLevel XP",
                                fontSize = 10.sp,
                                color = brandColor.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = stringResource(R.string.stat_details),
                                fontSize = 10.sp,
                                color = AccentGold,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEditName) { Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.profile_edit_name), tint = brandColor) }
                    IconButton(onClick = onEditCatName) { Icon(Icons.Default.Pets, contentDescription = stringResource(R.string.profile_edit_cat_name), tint = brandColor) }
                }
            }

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.4f))
                    .pillowPress(onClick = onEditCatName),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "🐾 $catName",
                        style = MaterialTheme.typography.titleMedium,
                        color = brandColor,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(R.string.profile_edit_cat_name),
                        tint = brandColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

// ==================== THEME PICKER ====================

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
        modifier = Modifier.pillowPress(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(if (isSelected) 60.dp else 54.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            color,
                            color.copy(alpha = if (isSelected) 0.6f else 0.55f),
                        ),
                    ),
                )
                .then(
                    if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                    else Modifier,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.theme_selected), tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = theme.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ==================== STAT CARD ====================

@Composable
fun ProfileStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
) {
    PillowCard(
        modifier = modifier,
        backgroundColor = color.copy(alpha = 0.12f),
        contentPadding = 14.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = color)
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ==================== GOAL ITEM ====================

@Composable
fun GoalItem(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
) {
    PillowCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = color.copy(alpha = 0.10f),
        contentPadding = 14.dp,
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = value,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.icon_chevron), tint = color, modifier = Modifier.size(20.dp))
        }
    }
}

// ==================== SETTINGS ROW ====================

@Composable
fun SettingsRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    modifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        )
        trailingContent()
    }
}

// ==================== DIALOGS ====================

@Composable
fun NewEditDialog(
    title: String,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.ExtraBold) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.padding(top = 6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp),
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
        shape = RoundedCornerShape(28.dp),
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
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                if (infoNote != null) {
                    PillowCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = color.copy(alpha = 0.10f),
                        contentPadding = 12.dp,
                    ) {
                        Text(
                            text = infoNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = color,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StepperRoundButton(
                        icon = Icons.Default.Remove,
                        color = color,
                        onClick = { if (value - step >= minValue) onValueChange(value - step) },
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(140.dp),
                    ) {
                        Text(text = "$value", fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, color = color)
                        Text(
                            text = unit,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    StepperRoundButton(
                        icon = Icons.Default.Add,
                        color = color,
                        onClick = { if (value + step <= maxValue) onValueChange(value + step) },
                    )
                }
            }
        },
        confirmButton = {
            ActionPillButton(
                text = stringResource(R.string.btn_confirm),
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = color,
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
    )
}

@Composable
private fun StepperRoundButton(icon: ImageVector, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .pillowPress(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = stringResource(R.string.icon_button), tint = color, modifier = Modifier.size(28.dp))
    }
}
