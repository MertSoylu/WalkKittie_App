package com.mert.paticat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mert.paticat.R
import com.mert.paticat.domain.model.Mission
import com.mert.paticat.domain.model.MissionType
import com.mert.paticat.ui.theme.AccentGold
import com.mert.paticat.ui.theme.PremiumBlue
import com.mert.paticat.ui.theme.PremiumPink
import com.mert.paticat.ui.theme.PremiumPurple
import com.mert.paticat.ui.theme.SuccessGreen

@Composable
fun GlassMissionItem(mission: Mission, liveSteps: Int = 0, liveWater: Int = 0, onMissionClick: (Mission) -> Unit = {}) {
    val displayValue = when (mission.type) {
        MissionType.STEPS -> kotlin.math.max(mission.currentValue, liveSteps)
        MissionType.WATER -> kotlin.math.max(mission.currentValue, liveWater)
        MissionType.GAME -> mission.currentValue
        else -> mission.currentValue
    }
    val isCompleted = displayValue >= mission.targetValue
    val itemAlpha = if (isCompleted) 0.6f else 1f

    val iconColor = when (mission.type) {
        MissionType.STEPS -> PremiumBlue
        MissionType.WATER -> PremiumBlue
        MissionType.GAME -> PremiumPurple
        else -> PremiumPink
    }

    val context = LocalContext.current

    // Resolve Title
    var titleResId = getMissionStringId(mission.title)
    if (titleResId == 0) {
        titleResId = context.resources.getIdentifier(mission.title, "string", context.packageName)
    }
    val displayTitle = if (titleResId != 0) context.getString(titleResId) else mission.title

    // Resolve and Format Description
    var descResId = getMissionStringId(mission.description)
    if (descResId == 0) {
        descResId = context.resources.getIdentifier(mission.description, "string", context.packageName)
    }
    val displayDesc = if (descResId != 0) {
        try {
            context.getString(descResId, mission.targetValue)
        } catch (e: Exception) {
            context.getString(descResId)
        }
    } else mission.description

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(itemAlpha)
            .clickable { onMissionClick(mission) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCompleted) 0.dp else 2.dp
        )
    ) {
        Row {
            // Left colored strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        color = if (isCompleted) SuccessGreen.copy(alpha = 0.4f) else iconColor,
                        shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                    )
            )

            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) SuccessGreen.copy(alpha = 0.1f)
                            else iconColor.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(Icons.Default.Check, null, tint = SuccessGreen)
                    } else {
                        val icon = when (mission.type) {
                            MissionType.STEPS -> Icons.Default.DirectionsWalk
                            MissionType.WATER -> Icons.Default.LocalDrink
                            MissionType.GAME -> Icons.Default.SportsEsports
                            else -> Icons.Default.Star
                        }
                        Icon(icon, null, tint = iconColor)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        displayTitle,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                    )

                    Text(
                        displayDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Normal
                    )

                    if (!isCompleted) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (displayValue.toFloat() / mission.targetValue).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = iconColor,
                            trackColor = iconColor.copy(alpha = 0.1f),
                            strokeCap = StrokeCap.Round
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "$displayValue / ${mission.targetValue}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Reward Badges
                Column(horizontalAlignment = Alignment.End) {
                    if (!isCompleted) {
                        Surface(
                            color = AccentGold.copy(alpha = 0.15f),
                            shape = CircleShape
                        ) {
                            Text(
                                "+${mission.xpReward} XP",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = AccentGold
                            )
                        }
                        if (mission.foodPointReward > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = PremiumPink.copy(alpha = 0.15f),
                                shape = CircleShape
                            ) {
                                Text(
                                    "+${mission.foodPointReward} 🪙",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = PremiumPink
                                )
                            }
                        }
                    } else {
                        Icon(Icons.Default.DoneAll, null, tint = SuccessGreen.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

fun getMissionStringId(key: String): Int {
    return when(key) {
        "mission_steps_tier1_title" -> R.string.mission_steps_tier1_title
        "mission_steps_tier1_desc" -> R.string.mission_steps_tier1_desc
        "mission_steps_tier2_title" -> R.string.mission_steps_tier2_title
        "mission_steps_tier2_desc" -> R.string.mission_steps_tier2_desc
        "mission_steps_tier3_title" -> R.string.mission_steps_tier3_title
        "mission_steps_tier3_desc" -> R.string.mission_steps_tier3_desc
        "mission_steps_tier4_title" -> R.string.mission_steps_tier4_title
        "mission_steps_tier4_desc" -> R.string.mission_steps_tier4_desc
        "mission_water_tier1_title" -> R.string.mission_water_tier1_title
        "mission_water_tier1_desc" -> R.string.mission_water_tier1_desc
        "mission_water_tier2_title" -> R.string.mission_water_tier2_title
        "mission_water_tier2_desc" -> R.string.mission_water_tier2_desc
        "mission_game_tier1_title" -> R.string.mission_game_tier1_title
        "mission_game_tier1_desc" -> R.string.mission_game_tier1_desc
        else -> 0
    }
}
