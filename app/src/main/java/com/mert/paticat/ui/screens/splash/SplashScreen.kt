package com.mert.paticat.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mert.paticat.R
import com.mert.paticat.ui.components.marshmallow.breath
import com.mert.paticat.ui.theme.AuroraBlobLavender
import com.mert.paticat.ui.theme.AuroraBlobPink
import com.mert.paticat.ui.theme.BackgroundLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    nextDestination: String?,
    onNavigate: (String) -> Unit,
) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }
    var animationFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
        launch {
            alpha.animateTo(targetValue = 1f, animationSpec = tween(700))
        }
        delay(1200)
        animationFinished = true
    }

    LaunchedEffect(animationFinished, nextDestination) {
        if (animationFinished && nextDestination != null) {
            onNavigate(nextDestination)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundLight,
                        AuroraBlobPink,
                        AuroraBlobLavender,
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value),
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .breath(periodMillis = 2400, amplitude = 0.06f),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_logo),
                    contentDescription = "PatiCat Logo",
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.splash_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
