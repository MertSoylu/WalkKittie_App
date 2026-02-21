package com.mert.paticat.ui.screens.welcome

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mert.paticat.ui.components.EntranceAnimation
import com.mert.paticat.ui.components.bounceClick
import com.mert.paticat.ui.components.pulsate
import com.mert.paticat.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    
    // Background gradient - Made slightly simpler for better text contrast
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                OnboardingPage(page = page)
            }
            
            // Pager Indicator
            Row(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val color = if (isSelected) PremiumPink else Color.LightGray
                    val width by animateDpAsState(if (isSelected) 24.dp else 8.dp)
                    
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(color)
                            .size(width = width, height = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Bottom Button
            Button(
                onClick = {
                    if (pagerState.currentPage < 2) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 32.dp)
                    .height(56.dp)
                    .bounceClick { 
                        if (pagerState.currentPage < 2) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            onFinish()
                        }
                     },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PremiumPink),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Text(
                    text = if (pagerState.currentPage == 2) androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.welcome_btn_start) else androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.welcome_btn_next),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun OnboardingPage(page: Int) {
    val pageContent = when (page) {
        0 -> Triple(
            androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.welcome_page1_title),
            androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.welcome_page1_desc),
            "😺"
        )
        1 -> Triple(
             androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.welcome_page2_title),
             androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.welcome_page2_desc),
            "📊"
        )
        else -> Triple(
             androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.welcome_page3_title),
             androidx.compose.ui.res.stringResource(com.mert.paticat.R.string.welcome_page3_desc),
            "🏆"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EntranceAnimation {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .pulsate()
                    .clip(CircleShape)
                    .background(
                        when (page) {
                            0 -> PremiumPink.copy(alpha = 0.1f)
                            1 -> PremiumBlue.copy(alpha = 0.1f)
                            else -> PremiumMint.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = pageContent.third, fontSize = 100.sp)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        EntranceAnimation(delay = 200) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = pageContent.first,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = Color.Black // Explicitly Black for max contrast
                )
    
                Spacer(modifier = Modifier.height(16.dp))
    
                Text(
                    text = pageContent.second,
                    style = MaterialTheme.typography.titleMedium, // Larger size
                    color = Color.DarkGray, // Darker gray
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 28.sp
                )
            }
        }
    }
}
