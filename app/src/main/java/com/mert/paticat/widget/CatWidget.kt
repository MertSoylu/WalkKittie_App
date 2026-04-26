package com.mert.paticat.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.mert.paticat.MainActivity
import com.mert.paticat.R

class CatWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val hunger = prefs[intPreferencesKey("cat_hunger")] ?: 100
            val energy = prefs[intPreferencesKey("cat_energy")] ?: 100
            val level = prefs[intPreferencesKey("cat_level")] ?: 1

            // Simple Widget UI
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF2B2D30)) // Dark widget background
                    .padding(12.dp)
                    .clickable(actionStartActivity(android.content.Intent(context, MainActivity::class.java))),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "WalkKittie",
                    style = TextStyle(
                        color = androidx.glance.color.ColorProvider(day = Color.White, night = Color.White),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.height(8.dp))

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = GlanceModifier.defaultWeight()
                    ) {
                        StatRow("🍖", hunger)
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        StatRow("⚡", energy)
                    }

                    Spacer(modifier = GlanceModifier.width(16.dp))

                    Image(
                        provider = ImageProvider(R.drawable.ic_paw_small),
                        contentDescription = "Cat Icon",
                        modifier = GlanceModifier.size(48.dp)
                    )

                    Spacer(modifier = GlanceModifier.width(16.dp))

                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = GlanceModifier.defaultWeight()
                    ) {
                        Text(
                            text = "Level",
                            style = TextStyle(color = androidx.glance.color.ColorProvider(day = Color.LightGray, night = Color.LightGray), fontSize = 12.sp)
                        )
                        Text(
                            text = "$level",
                            style = TextStyle(color = androidx.glance.color.ColorProvider(day = Color.White, night = Color.White), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun StatRow(emoji: String, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = emoji,
            style = TextStyle(fontSize = 12.sp)
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        Text(
            text = "$value%",
            style = TextStyle(
                color = androidx.glance.color.ColorProvider(day = Color.White, night = Color.White),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
