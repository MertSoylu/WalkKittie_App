package com.mert.paticat.ui.screens.welcome

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.mert.paticat.R
import com.mert.paticat.ui.components.PatiCatBackground
import com.mert.paticat.ui.components.marshmallow.TintedPillowCard
import com.mert.paticat.ui.components.marshmallow.softEntrance
import com.mert.paticat.ui.theme.MoodHappyGradient
import com.mert.paticat.ui.theme.MoodSadGradient
import com.mert.paticat.ui.theme.PremiumBlueDark
import com.mert.paticat.ui.theme.PremiumPinkDark

@Composable
fun LanguageSelectionScreen(
    onLanguageSelected: (String) -> Unit,
) {
    LaunchedEffect(Unit) {
        val currentAppLocales = AppCompatDelegate.getApplicationLocales()
        if (!currentAppLocales.isEmpty) {
            val language = currentAppLocales.get(0)?.language
            if (language != null) {
                onLanguageSelected(language)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PatiCatBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                modifier = Modifier.softEntrance(delayMillis = 0),
                text = stringResource(R.string.language_welcome_title),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                modifier = Modifier.softEntrance(delayMillis = 80),
                text = stringResource(R.string.language_select_prompt),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(48.dp))

            Box(modifier = Modifier.softEntrance(delayMillis = 160)) {
                LanguageOptionCard(
                    language = stringResource(R.string.language_english_name),
                    flagEmoji = "🇬🇧",
                    nativeName = stringResource(R.string.language_english_native),
                    accent = PremiumBlueDark,
                    gradient = MoodSadGradient,
                    onClick = { setAppLocale("en") },
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.softEntrance(delayMillis = 240)) {
                LanguageOptionCard(
                    language = stringResource(R.string.language_turkish_name),
                    flagEmoji = "🇹🇷",
                    nativeName = stringResource(R.string.language_turkish_native),
                    accent = PremiumPinkDark,
                    gradient = MoodHappyGradient,
                    onClick = { setAppLocale("tr") },
                )
            }
        }
    }
}

@Composable
fun LanguageOptionCard(
    language: String,
    flagEmoji: String,
    nativeName: String,
    accent: Color,
    gradient: List<Color>,
    onClick: () -> Unit,
) {
    TintedPillowCard(
        gradient = gradient,
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        contentPadding = 0.dp,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(flagEmoji, fontSize = 40.sp)
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = language,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent,
                )
                Text(
                    text = nativeName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = accent.copy(alpha = 0.7f),
                )
            }
        }
    }
}

private fun setAppLocale(languageCode: String) {
    val appLocale = LocaleListCompat.forLanguageTags(languageCode)
    AppCompatDelegate.setApplicationLocales(appLocale)
}
