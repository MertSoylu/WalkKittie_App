package com.mert.paticat.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mert.paticat.R
import com.mert.paticat.domain.model.LevelData
import com.mert.paticat.ui.components.marshmallow.ChipPill
import com.mert.paticat.ui.components.marshmallow.PillowCard
import com.mert.paticat.ui.components.marshmallow.SectionHeader2
import com.mert.paticat.ui.components.marshmallow.softEntrance
import com.mert.paticat.ui.theme.AccentGold
import com.mert.paticat.ui.theme.PremiumMint
import com.mert.paticat.ui.theme.PremiumPeach
import com.mert.paticat.ui.theme.PremiumPink
import com.mert.paticat.ui.theme.PremiumPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelInfoScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.level_system_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.btn_cancel))
                    }
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

            Box(modifier = Modifier.softEntrance()) {
                PillowCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = AccentGold.copy(alpha = 0.16f),
                    contentPadding = 22.dp,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(AccentGold.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.EmojiEvents, null, modifier = Modifier.size(36.dp), tint = AccentGold)
                        }
                        Spacer(Modifier.height(12.dp))
                        val levelTitleStr = stringResource(
                            com.mert.paticat.domain.model.Cat.getLevelTitleResId(uiState.level),
                        )
                        Text(
                            text = stringResource(R.string.level_label_with_title, uiState.level, levelTitleStr),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentGold,
                        )
                        Spacer(Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { uiState.levelProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = AccentGold,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "${uiState.xpInCurrentLevel} / ${uiState.xpNeededForNextLevel} XP",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(
                                R.string.level_next_label,
                                uiState.xpNeededForNextLevel - uiState.xpInCurrentLevel,
                            ),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Column(modifier = Modifier.softEntrance(delayMillis = 100)) {
                SectionHeader2(
                    title = stringResource(R.string.level_xp_gain_title),
                    leadingEmoji = "✨",
                )
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    XpSourceCard(
                        icon = Icons.Default.SportsEsports,
                        title = stringResource(R.string.level_xp_source_games),
                        description = stringResource(R.string.level_xp_games_desc),
                        xp = "+20~40 XP",
                        color = PremiumPink,
                    )
                    XpSourceCard(
                        icon = Icons.Default.Restaurant,
                        title = stringResource(R.string.level_xp_source_feed),
                        description = stringResource(R.string.level_xp_feed_desc),
                        xp = "+2 XP",
                        color = PremiumPeach,
                    )
                    XpSourceCard(
                        icon = Icons.Default.Bedtime,
                        title = stringResource(R.string.level_xp_source_sleep),
                        description = stringResource(R.string.level_xp_sleep_desc),
                        xp = "+5 XP",
                        color = PremiumPurple,
                    )
                    XpSourceCard(
                        icon = Icons.Default.Task,
                        title = stringResource(R.string.level_xp_source_mission),
                        description = stringResource(R.string.level_xp_mission_desc),
                        xp = "+10~50 XP",
                        color = AccentGold,
                    )
                }
            }

            Box(modifier = Modifier.softEntrance(delayMillis = 200)) {
                PillowCard(modifier = Modifier.fillMaxWidth(), contentPadding = 18.dp) {
                    Column {
                        Text(
                            text = stringResource(R.string.level_requirements_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.semantics { heading() },
                        )
                        Spacer(Modifier.height(14.dp))

                        LevelSection(
                            titleRes = R.string.level_section_early,
                            rows = LevelData.EARLY_GAME,
                            currentLevel = uiState.level,
                        )
                        Spacer(Modifier.height(12.dp))
                        LevelSection(
                            titleRes = R.string.level_section_mid,
                            rows = LevelData.MID_GAME,
                            currentLevel = uiState.level,
                        )
                        Spacer(Modifier.height(12.dp))
                        LevelSection(
                            titleRes = R.string.level_section_late,
                            rows = LevelData.LATE_GAME,
                            currentLevel = uiState.level,
                        )
                    }
                }
            }

            Box(modifier = Modifier.softEntrance(delayMillis = 280)) {
                PillowCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = PremiumMint.copy(alpha = 0.10f),
                    contentPadding = 18.dp,
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💡", fontSize = 22.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.level_tip_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = PremiumMint,
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.level_tips_content),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun XpSourceCard(
    icon: ImageVector,
    title: String,
    description: String,
    xp: String,
    color: Color,
) {
    PillowCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = color.copy(alpha = 0.10f),
        contentPadding = 14.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold)
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ChipPill(
                text = xp,
                backgroundColor = color,
                contentColor = Color.White,
            )
        }
    }
}

@Composable
private fun LevelSection(titleRes: Int, rows: List<Pair<Int, Int>>, currentLevel: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.semantics { heading() },
    )
    Spacer(Modifier.height(8.dp))
    rows.forEachIndexed { idx, (level, xp) ->
        LevelRequirementRow(
            level = level,
            xpRequired = xp,
            isCurrentLevel = level == currentLevel,
            isPassed = level < currentLevel,
        )
        if (idx < rows.lastIndex) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
            )
        }
    }
}

@Composable
fun LevelRequirementRow(level: Int, xpRequired: Int, isCurrentLevel: Boolean, isPassed: Boolean) {
    // Out-of-bounds rows are silently dropped — keeps the table forward-compatible
    // if a future patch expands LevelData but leaves stale callers behind.
    if (level > LevelData.MAX_LEVEL) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isPassed -> PremiumMint.copy(alpha = 0.22f)
                        isCurrentLevel -> AccentGold.copy(alpha = 0.22f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isPassed) {
                Icon(Icons.Default.Check, null, tint = PremiumMint, modifier = Modifier.size(18.dp))
            } else {
                Text(
                    text = "$level",
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isCurrentLevel) AccentGold else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            val levelTitleStr = stringResource(
                com.mert.paticat.domain.model.Cat.getLevelTitleResId(level),
            )
            Text(
                text = stringResource(R.string.level_label_with_title, level, levelTitleStr),
                fontWeight = if (isCurrentLevel) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isCurrentLevel) AccentGold else MaterialTheme.colorScheme.onSurface,
            )
        }

        Text(
            text = "$xpRequired XP",
            fontWeight = FontWeight.Bold,
            color = when {
                isPassed -> PremiumMint
                isCurrentLevel -> AccentGold
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}
