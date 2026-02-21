package com.mert.paticat.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mert.paticat.R
import com.mert.paticat.domain.model.Cat
import kotlinx.coroutines.delay

@Composable
fun LevelUpDialog(
    newLevel: Int,
    onDismiss: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val particleSystem = rememberParticleSystem()

    // Start celebration when composed
    LaunchedEffect(Unit) {
        showDialog = true
        // Fire confetti multiple times
        for (i in 0..5) {
            delay(300)
            particleSystem.emit(
                x = (200..800).random().toFloat(),
                y = 1000f,
                count = 30,
                type = ParticleType.STAR,
                color = listOf(Color.Red, Color.Yellow, Color.Green, Color.Blue, Color.Magenta).random()
            )
        }
    }

    if (showDialog) {
        // Full screen overlay for particles and dimming
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(enabled = false) {}, // Intercept clicks
            contentAlignment = Alignment.Center
        ) {
            // Draw particles behind dialog
            ParticleSystemCanvas(
                state = particleSystem,
                modifier = Modifier.fillMaxSize()
            )

            AnimatedVisibility(
                visible = showDialog,
                enter = scaleIn(animationSpec = tween(500, delayMillis = 100)),
                exit = scaleOut(animationSpec = tween(300))
            ) {
                 Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 1f)
                    ),
                    elevation = CardDefaults.cardElevation(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉 TEBRİKLER! 🎉",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Badge Icon Area
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(30.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏆", fontSize = 64.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Level $newLevel",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        val levelTitleRes = Cat.getLevelTitleResId(newLevel)
                        Text(
                            text = androidx.compose.ui.res.stringResource(levelTitleRes),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = com.mert.paticat.ui.theme.PremiumPink,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        Text(
                            text = "Kedinizle aranızdaki bağ güçlendi!\nYeni ünvanınızı gururla taşıyın.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                                .bounceClick {
                                    showDialog = false
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Muhteşem!", 
                                color = Color.White, 
                                fontSize = 18.sp, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            // Draw particles in front of dialog
            ParticleSystemCanvas(
                state = particleSystem,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
