package com.mert.paticat.widget

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.mert.paticat.domain.model.Cat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object WidgetUpdater {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun updateWidget(context: Context, cat: Cat) {
        scope.launch {
            try {
                val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(CatWidget::class.java)
                for (glanceId in glanceIds) {
                    updateAppWidgetState(context, glanceId) { prefs ->
                        prefs[intPreferencesKey("cat_hunger")] = cat.hunger
                        prefs[intPreferencesKey("cat_energy")] = cat.energy
                        prefs[intPreferencesKey("cat_level")] = cat.level
                    }
                    CatWidget().update(context, glanceId)
                }
            } catch (e: IllegalStateException) {
                Log.w("WidgetUpdater", "No active widget to update", e)
            } catch (e: Exception) {
                Log.e("WidgetUpdater", "Widget update failed", e)
            }
        }
    }
}
