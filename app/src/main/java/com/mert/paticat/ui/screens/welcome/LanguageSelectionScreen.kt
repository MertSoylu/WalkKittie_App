package com.mert.paticat.ui.screens.welcome

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.mert.paticat.ui.components.EntranceAnimation
import com.mert.paticat.ui.components.bounceClick
import com.mert.paticat.ui.theme.PremiumBlue
import com.mert.paticat.ui.theme.PremiumPink
import java.util.Locale

@Composable
fun LanguageSelectionScreen(
    onLanguageSelected: (String) -> Unit
) {
    val context = LocalContext.current
    
    // Check if locale is already set (e.g. after rotation or recreation due to setAppLocale)
    LaunchedEffect(Unit) {
        val currentAppLocales = AppCompatDelegate.getApplicationLocales()
        if (!currentAppLocales.isEmpty) {
            val language = currentAppLocales.get(0)?.language
            if (language != null) {
                // Determine if we actually selected a supported language
                onLanguageSelected(language)
            }
        }
    }

    // Background gradient similar to Onboarding
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            EntranceAnimation {
                Text(
                    text = "Welcome / Hoşgeldiniz",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            EntranceAnimation(delay = 100) {
                Text(
                    text = "Please select your language\nLütfen dilinizi seçin",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // English Option
            EntranceAnimation(delay = 200) {
                LanguageOptionCard(
                    language = "English",
                    flagEmoji = "🇬🇧",
                    nativeName = "English",
                    color = PremiumBlue,
                    isSelected = false, 
                    onClick = {
                        // Just set locale, Activity will recreate.
                        // On recreation, the LaunchedEffect above will trigger onLanguageSelected.
                        setAppLocale("en")
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Turkish Option
            EntranceAnimation(delay = 300) {
                LanguageOptionCard(
                    language = "Türkçe",
                    flagEmoji = "🇹🇷",
                    nativeName = "Turkish",
                    color = PremiumPink,
                    isSelected = false,
                    onClick = {
                        setAppLocale("tr")
                    }
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
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .bounceClick { onClick() }
            .clip(RoundedCornerShape(24.dp)), // Ensure clip matches shape
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(2.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() }
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(flagEmoji, fontSize = 40.sp)
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    language,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    nativeName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

private fun setAppLocale(languageCode: String) {
    val appLocale = LocaleListCompat.forLanguageTags(languageCode)
    AppCompatDelegate.setApplicationLocales(appLocale)
}
