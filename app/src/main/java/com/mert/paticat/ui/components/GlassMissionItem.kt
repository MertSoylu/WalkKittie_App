package com.mert.paticat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mert.paticat.R
import com.mert.paticat.domain.mission.missionStringRes
import com.mert.paticat.domain.model.Mission
import com.mert.paticat.domain.model.MissionType
import com.mert.paticat.ui.components.marshmallow.ChipPill
import com.mert.paticat.ui.components.marshmallow.PillowCard
import com.mert.paticat.ui.theme.AccentGold
import com.mert.paticat.ui.theme.PremiumBlue
import com.mert.paticat.ui.theme.PremiumPink
import com.mert.paticat.ui.theme.PremiumPurple
import com.mert.paticat.ui.theme.SuccessGreen

/**
 * Marshmallow mission row. Function name retained for callers.
 *
 * Title/description lookup uses the explicit [missionStringRes] map (no
 * reflection, no per-recomposition logging) and is memoized with [remember]
 * keyed on the mission's title/description so the lookup happens once per
 * mission identity, not on every recomposition.
 */
@Composable
fun GlassMissionItem(
    mission: Mission,
    liveSteps: Int = 0,
    liveWater: Int = 0,
    onMissionClick: (Mission) -> Unit = {},
) {
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

    val titleResId = remember(mission.title) { missionStringRes(mission.title) }
    val displayTitle = if (titleResId != 0) {
        stringResource(titleResId)
    } else {
        // Fallback to the raw key — keeps UI alive when a new key was added without
        // updating the map. Logging removed; once was per-recomposition spam.
        mission.title
    }

    val descResId = remember(mission.description) { missionStringRes(mission.description) }
    val displayDesc = if (descResId != 0) {
        // %d-formatted descriptions take targetValue; non-formatted ones ignore the arg.
        stringResource(descResId, mission.targetValue)
    } else {
        mission.description
    }

    PillowCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(itemAlpha),
        contentPadding = 14.dp,
        onClick = { onMissionClick(mission) },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) SuccessGreen.copy(alpha = 0.14f)
                        else iconColor.copy(alpha = 0.14f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isCompleted) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = stringResource(R.string.a11y_mission_complete),
                        tint = SuccessGreen,
                    )
                } else {
                    val icon = when (mission.type) {
                        MissionType.STEPS -> Icons.Default.DirectionsWalk
                        MissionType.WATER -> Icons.Default.LocalDrink
                        MissionType.GAME -> Icons.Default.SportsEsports
                        else -> Icons.Default.Star
                    }
                    Icon(icon, contentDescription = displayTitle, tint = iconColor)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                )
                Text(
                    text = displayDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal,
                )
                if (!isCompleted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (displayValue.toFloat() / mission.targetValue).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = iconColor,
                        trackColor = iconColor.copy(alpha = 0.1f),
                        strokeCap = StrokeCap.Round,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$displayValue / ${mission.targetValue}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (!isCompleted) {
                    ChipPill(
                        text = "+${mission.xpReward} XP",
                        backgroundColor = AccentGold.copy(alpha = 0.18f),
                        contentColor = AccentGold,
                    )
                    if (mission.foodPointReward > 0) {
                        ChipPill(
                            text = "+${mission.foodPointReward}",
                            leadingEmoji = "🪙",
                            backgroundColor = PremiumPink.copy(alpha = 0.16f),
                            contentColor = PremiumPink,
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.DoneAll,
                        contentDescription = stringResource(R.string.a11y_mission_complete),
                        tint = SuccessGreen.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}

/**
 * Backwards-compat alias retained for [MissionCard] which still references it.
 * New code should call [missionStringRes] directly.
 */
fun getMissionStringId(key: String): Int = missionStringRes(key)
