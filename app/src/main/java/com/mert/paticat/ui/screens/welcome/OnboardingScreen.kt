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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mert.paticat.R
import com.mert.paticat.ui.components.PatiCatBackground
import com.mert.paticat.ui.components.marshmallow.ActionPillButton
import com.mert.paticat.ui.components.marshmallow.breath
import com.mert.paticat.ui.components.marshmallow.softEntrance
import com.mert.paticat.ui.theme.MoodHappyGradient
import com.mert.paticat.ui.theme.MoodSadGradient
import com.mert.paticat.ui.theme.MoodSleepGradient
import com.mert.paticat.ui.theme.PremiumPink
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
) {
    val savedPage = rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(initialPage = savedPage.intValue, pageCount = { 3 })
    LaunchedEffect(pagerState.currentPage) { savedPage.intValue = pagerState.currentPage }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        PatiCatBackground()
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { page ->
                OnboardingPage(page = page)
            }

            Row(
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(3) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val color = if (isSelected) PremiumPink
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    val width by animateDpAsState(if (isSelected) 28.dp else 8.dp, label = "ind_$iteration")

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(color)
                            .size(width = width, height = 8.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ActionPillButton(
                text = if (pagerState.currentPage == 2)
                    stringResource(R.string.welcome_btn_start)
                else stringResource(R.string.welcome_btn_next),
                onClick = {
                    if (pagerState.currentPage < 2) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onFinish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .padding(bottom = 28.dp, top = 12.dp),
                leadingEmoji = if (pagerState.currentPage == 2) "🚀" else "→",
            )
        }
    }
}

@Composable
fun OnboardingPage(page: Int) {
    val (title, desc, emoji) = when (page) {
        0 -> Triple(
            stringResource(R.string.welcome_page1_title),
            stringResource(R.string.welcome_page1_desc),
            "🐾",
        )
        1 -> Triple(
            stringResource(R.string.welcome_page2_title),
            stringResource(R.string.welcome_page2_desc),
            "📊",
        )
        else -> Triple(
            stringResource(R.string.welcome_page3_title),
            stringResource(R.string.welcome_page3_desc),
            "🏆",
        )
    }
    val gradient = when (page) {
        0 -> MoodHappyGradient
        1 -> MoodSadGradient
        else -> MoodSleepGradient
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .softEntrance()
                .size(240.dp)
                .breath(amplitude = 0.05f)
                .clip(CircleShape)
                .background(Brush.linearGradient(gradient)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = emoji, fontSize = 110.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))

        Column(
            modifier = Modifier.softEntrance(delayMillis = 120),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = desc,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                lineHeight = 26.sp,
            )
        }
    }
}
