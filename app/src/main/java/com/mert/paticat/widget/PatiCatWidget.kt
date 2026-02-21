package com.mert.paticat.widget

import android.content.Context
import android.content.Intent
import androidx.glance.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.currentState
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import com.mert.paticat.MainActivity

class PatiCatWidget : GlanceAppWidget() {
    
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    
    companion object {
        val KEY_CAT_NAME = stringPreferencesKey("widget_cat_name")
        val KEY_CAT_EMOJI = stringPreferencesKey("widget_cat_emoji")
        val KEY_STEPS = intPreferencesKey("widget_steps")
        val KEY_HUNGER = intPreferencesKey("widget_hunger")
        val KEY_HAPPINESS = intPreferencesKey("widget_happiness")
        val KEY_ENERGY = intPreferencesKey("widget_energy")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }

    @Composable
    private fun WidgetContent() {
        // Read data using currentState with preferences state definition
        val prefs = currentState<Preferences>()
        val catName = prefs[KEY_CAT_NAME] ?: "PatiCat"
        val catEmoji = prefs[KEY_CAT_EMOJI] ?: "😸"
        val steps = prefs[KEY_STEPS] ?: 0
        val hunger = prefs[KEY_HUNGER] ?: 100
        val happiness = prefs[KEY_HAPPINESS] ?: 100
        val energy = prefs[KEY_ENERGY] ?: 100

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(16.dp)
                .clickable(actionStartActivity(Intent(LocalContext.current, MainActivity::class.java)))
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = catEmoji,
                        style = TextStyle(fontSize = 32.sp)
                    )
                    Spacer(modifier = GlanceModifier.width(12.dp))
                    Column {
                        Text(
                            text = catName,
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlanceTheme.colors.onSurface
                            )
                        )
                        Text(
                            text = "🐾 $steps adım",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = GlanceTheme.colors.onSurfaceVariant
                            )
                        )
                    }
                }
                
                Spacer(modifier = GlanceModifier.height(16.dp))
                
                Row(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WidgetStat(icon = "🍖", value = hunger)
                    Spacer(modifier = GlanceModifier.width(16.dp))
                    WidgetStat(icon = "❤️", value = happiness)
                    Spacer(modifier = GlanceModifier.width(16.dp))
                    WidgetStat(icon = "⚡", value = energy)
                }
            }
        }
    }

    @Composable
    private fun WidgetStat(icon: String, value: Int) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, style = TextStyle(fontSize = 14.sp))
            Spacer(modifier = GlanceModifier.width(4.dp))
            Text(
                text = "%$value",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onSurface
                )
            )
        }
    }
}
