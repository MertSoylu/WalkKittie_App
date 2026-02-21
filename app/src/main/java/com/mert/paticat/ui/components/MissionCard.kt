package com.mert.paticat.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mert.paticat.domain.model.Mission
import com.mert.paticat.domain.model.MissionType
import com.mert.paticat.ui.theme.*

/**
 * Mission card component with progress tracking.
 * Redesigned for maximum visibility and theme compatibility.
 */
@Composable
fun MissionCard(
    mission: Mission,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (mission.type) {
        MissionType.STEPS -> Icons.Default.DirectionsWalk to PremiumBlue
        MissionType.WATER -> Icons.Default.LocalDrink to PremiumMint
        MissionType.CALORIES -> Icons.Default.LocalFireDepartment to PremiumPeach
        MissionType.STREAK -> Icons.Default.Star to AccentGold
    }
    
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val progressAnimation by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(durationMillis = 800),
        label = "mission_progress"
    )
    
    LaunchedEffect(mission.progress) {
        animatedProgress = mission.progress
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !mission.isCompleted) { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (mission.isCompleted) 
                SuccessGreen.copy(alpha = 0.08f) 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (mission.isCompleted) 0.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (mission.isCompleted) SuccessGreen.copy(alpha = 0.2f)
                        else color.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (mission.isCompleted) Icons.Default.Check else icon,
                    contentDescription = null,
                    tint = if (mission.isCompleted) SuccessGreen else color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content Section
            Column(modifier = Modifier.weight(1f)) {
                val context = androidx.compose.ui.platform.LocalContext.current
                var titleText = mission.title
                var descText = mission.description
                
                // Try to resolve title as resource
                var titleResId = getMissionStringId(mission.title)
                if (titleResId == 0) {
                     titleResId = context.resources.getIdentifier(mission.title, "string", context.packageName)
                }
                if (titleResId != 0) {
                    titleText = context.getString(titleResId)
                }
                
                // Try to resolve description as resource
                var descResId = getMissionStringId(mission.description)
                if (descResId == 0) {
                     descResId = context.resources.getIdentifier(mission.description, "string", context.packageName)
                }
                if (descResId != 0) {
                    // Try to format with targetValue if there's a placeholder
                    try {
                        descText = context.getString(descResId, mission.targetValue)
                    } catch (e: Exception) {
                        descText = context.getString(descResId)
                    }
                }

                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    textDecoration = if (mission.isCompleted) TextDecoration.LineThrough else null,
                    color = if (mission.isCompleted) 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = descText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // Progress Bar or Completion Status
                if (!mission.isCompleted) {
                    Column {
                        LinearProgressIndicator(
                            progress = { progressAnimation },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = color,
                            trackColor = color.copy(alpha = 0.15f),
                            strokeCap = StrokeCap.Round
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = "${mission.currentValue} / ${mission.targetValue}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Tamamlandı! 🎉",
                        color = SuccessGreen,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Reward Section
            Column(horizontalAlignment = Alignment.End) {
                if (mission.foodPointReward > 0) {
                    RewardBadge(
                        text = "+${mission.foodPointReward} 🍖",
                        color = PremiumPink
                    )
                }
                if (mission.xpReward > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    RewardBadge(
                        text = "+${mission.xpReward} XP",
                        color = AccentGold
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

private fun getMissionStringId(key: String): Int {
    return when(key) {
        "mission_steps_tier1_title" -> com.mert.paticat.R.string.mission_steps_tier1_title
        "mission_steps_tier1_desc" -> com.mert.paticat.R.string.mission_steps_tier1_desc
        "mission_steps_tier2_title" -> com.mert.paticat.R.string.mission_steps_tier2_title
        "mission_steps_tier2_desc" -> com.mert.paticat.R.string.mission_steps_tier2_desc
        "mission_steps_tier3_title" -> com.mert.paticat.R.string.mission_steps_tier3_title
        "mission_steps_tier3_desc" -> com.mert.paticat.R.string.mission_steps_tier3_desc
        "mission_steps_tier4_title" -> com.mert.paticat.R.string.mission_steps_tier4_title
        "mission_steps_tier4_desc" -> com.mert.paticat.R.string.mission_steps_tier4_desc
        else -> 0
    }
}
