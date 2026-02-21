package com.mert.paticat.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.mert.paticat.ui.theme.*

/**
 * Level Info Screen - Explains how leveling works and shows progression
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelInfoScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.level_system_title), 
                        fontWeight = FontWeight.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.btn_cancel))
                    }
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
            // Current Level Card
            EntranceAnimation {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentGold.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🏆", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        val levelTitleStr = androidx.compose.ui.res.stringResource(
                            com.mert.paticat.domain.model.Cat.getLevelTitleResId(uiState.level)
                        )
                        Text(
                            androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.level_label_with_title, uiState.level, levelTitleStr),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = AccentGold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LinearProgressIndicator(
                            progress = { uiState.levelProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = AccentGold,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "${uiState.xpInCurrentLevel} / ${uiState.xpNeededForNextLevel} XP",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.level_next_label, uiState.xpNeededForNextLevel - uiState.xpInCurrentLevel),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // How to Earn XP Section
            EntranceAnimation(delay = 100) {
                Column {
                    Text(
                        androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.level_xp_gain_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    XpSourceCard(
                        icon = Icons.Default.DirectionsWalk,
                        title = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.level_xp_source_walk),
                        description = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.level_xp_walk_desc),
                        xp = "+10 XP",
                        color = PremiumPink
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    XpSourceCard(
                        icon = Icons.Default.SportsEsports,
                        title = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.level_xp_source_game_rps),
                        description = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.level_xp_rps_desc),
                        xp = "+20 XP",
                        color = PremiumMint
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    XpSourceCard(
                        icon = Icons.Default.Numbers,
                        title = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.level_xp_source_game_guess),
                        description = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.level_xp_guess_desc),
                        xp = "+50 XP",
                        color = PremiumBlue
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    XpSourceCard(
                        icon = Icons.Default.Restaurant,
                        title = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.level_xp_source_feed),
                        description = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.level_xp_feed_desc),
                        xp = "+2 XP",
                        color = PremiumPeach
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    XpSourceCard(
                        icon = Icons.Default.Bedtime,
                        title = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.level_xp_source_sleep),
                        description = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.level_xp_sleep_desc),
                        xp = "+5 XP",
                        color = PremiumPurple
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    XpSourceCard(
                        icon = Icons.Default.Task,
                        title = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.level_xp_source_mission),
                        description = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.level_xp_mission_desc),
                        xp = "+10~50 XP",
                        color = AccentGold
                    )
                }
            }
            
            // Level Requirements Table
            EntranceAnimation(delay = 200) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.level_requirements_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Show level requirements
                        listOf(
                            1 to 200,
                            2 to 800,
                            3 to 1800,
                            4 to 3200,
                            5 to 5000,
                            10 to 20000,
                            15 to 45000,
                            20 to 80000
                        ).forEach { (level, xp) ->
                            LevelRequirementRow(
                                level = level,
                                xpRequired = xp,
                                isCurrentLevel = level == uiState.level,
                                isPassed = level < uiState.level
                            )
                            if (level != 20) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }
            
            // Tips Section
            EntranceAnimation(delay = 300) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PremiumMint.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💡", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.level_tip_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = PremiumMint
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.level_tips_content),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun XpSourceCard(
    icon: ImageVector,
    title: String,
    description: String,
    xp: String,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Surface(
                color = color,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = xp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun LevelRequirementRow(
    level: Int,
    xpRequired: Int,
    isCurrentLevel: Boolean,
    isPassed: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = when {
                isPassed -> PremiumMint.copy(alpha = 0.2f)
                isCurrentLevel -> AccentGold.copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isPassed) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = PremiumMint,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        "$level",
                        fontWeight = FontWeight.Black,
                        color = if (isCurrentLevel) AccentGold else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            val levelTitleStr = androidx.compose.ui.res.stringResource(
                com.mert.paticat.domain.model.Cat.getLevelTitleResId(level)
            )
            Text(
                androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.level_label_with_title, level, levelTitleStr),
                fontWeight = if (isCurrentLevel) FontWeight.Black else FontWeight.Bold,
                color = if (isCurrentLevel) AccentGold else MaterialTheme.colorScheme.onSurface
            )
        }
        
        Text(
            "${xpRequired} XP",
            fontWeight = FontWeight.Bold,
            color = when {
                isPassed -> PremiumMint
                isCurrentLevel -> AccentGold
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
