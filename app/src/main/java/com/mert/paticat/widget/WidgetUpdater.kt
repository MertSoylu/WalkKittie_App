package com.mert.paticat.widget

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.mert.paticat.domain.model.Cat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object WidgetUpdater {
    @OptIn(DelicateCoroutinesApi::class)
    fun updateWidget(context: Context, cat: Cat) {
        GlobalScope.launch {
            try {
                val glanceId = GlanceAppWidgetManager(context).getGlanceIds(CatWidget::class.java).firstOrNull()
                if (glanceId != null) {
                    updateAppWidgetState(context, glanceId) { prefs ->
                        prefs[intPreferencesKey("cat_hunger")] = cat.hunger
                        prefs[intPreferencesKey("cat_energy")] = cat.energy
                        prefs[intPreferencesKey("cat_level")] = cat.level
                    }
                    CatWidget().update(context, glanceId)
                }
            } catch (e: Exception) {
                // Ignore if widget is not active
            }
        }
    }
}
