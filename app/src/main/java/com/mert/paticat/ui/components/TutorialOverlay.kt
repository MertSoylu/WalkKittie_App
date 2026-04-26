package com.mert.paticat.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import com.mert.paticat.R
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mert.paticat.ui.theme.*
import com.mert.paticat.utils.UiText

/**
 * Tutorial step data class - simplified without targeting specific screen areas
 */
data class TutorialTarget(
    val index: Int,
    val title: UiText,
    val description: UiText,
    val targetRect: Rect? = null // Not used in new design
)

/**
 * Modern, clean tutorial overlay that appears as a centered card
 * without complex cutout mechanics that cause visual issues
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TutorialOverlay(
    show: Boolean,
    currentStep: Int,
    targets: List<TutorialTarget>,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    if (!show || currentStep >= targets.size) return

    val target = targets[currentStep]

    // Animation for card entrance
    val animatedProgress by animateFloatAsState(
        targetValue = if (show) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tutorial_anim"
    )



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { /* Consume clicks */ },
        contentAlignment = Alignment.Center
    ) {
        // Main Tutorial Card
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(horizontal = 16.dp)
                .shadow(24.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onSkip
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.tutorial_btn_close),
                            tint = Color.Gray.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Step Indicator
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    targets.forEachIndexed { index, _ ->
                        val isActive = index == currentStep
                        val isPast = index < currentStep

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isActive) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isActive -> MaterialTheme.colorScheme.primary
                                        isPast -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        else -> Color.Gray.copy(alpha = 0.3f)
                                    }
                                )
                        )
                    }
                }

                // Cache title string down to avoid repeatedly calling asString()
                val targetTitleStr = target.title.asString()

                // Icon based on content
                val tutorialIcon: ImageVector? = when {
                    targetTitleStr.contains("Enerji") || targetTitleStr.contains("Adım") -> Icons.Filled.Bolt
                    targetTitleStr.contains("Sağlık") || targetTitleStr.contains("Durum") -> Icons.Filled.Favorite
                    targetTitleStr.contains("Oyun") -> Icons.Filled.SportsEsports
                    targetTitleStr.contains("İstatistik") || targetTitleStr.contains("Aktivite") -> Icons.Filled.BarChart
                    targetTitleStr.contains("Profil") -> Icons.Filled.Person
                    else -> null
                }

                if (tutorialIcon != null) {
                    Icon(
                        imageVector = tutorialIcon,
                        contentDescription = stringResource(R.string.icon_star),
                        modifier = Modifier.padding(bottom = 16.dp).size(56.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = when {
                            targetTitleStr.contains("Kedi") -> "🐱"
                            targetTitleStr.contains("Regl") || targetTitleStr.contains("Döngü") -> "🌸"
                            else -> "✨"
                        },
                        fontSize = 56.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Title
                Text(
                    text = targetTitleStr.replace(Regex("[⚡🐱❤️🎮📊🌸👤✨]"), "").trim(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Description
                Text(
                    text = target.description.asString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Button
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentStep == targets.size - 1)
                                androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.tutorial_btn_finish)
                            else
                                androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.tutorial_btn_next),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        if (currentStep < targets.size - 1) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = stringResource(R.string.icon_star),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Skip text for non-last steps
                if (currentStep < targets.size - 1) {
                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.tutorial_btn_skip),
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }

                // Progress text
                Text(
                    text = "${currentStep + 1} / ${targets.size}",
                    color = Color.Gray.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
