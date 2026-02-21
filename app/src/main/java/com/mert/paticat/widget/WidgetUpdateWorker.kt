package com.mert.paticat.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mert.paticat.data.local.dao.CatDao
import com.mert.paticat.data.local.dao.DailyStatsDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.mert.paticat.domain.model.Cat
import com.mert.paticat.domain.model.CatMood
import androidx.glance.appwidget.updateAll

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val catDao: CatDao,
    private val dailyStatsDao: DailyStatsDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val catEntity = catDao.getCat().firstOrNull() ?: return Result.success()
            
            // Map Entity to Domain to use mood logic
            val cat = Cat(
                id = catEntity.id,
                name = catEntity.name,
                hunger = catEntity.hunger,
                happiness = catEntity.happiness,
                energy = catEntity.energy,
                xp = catEntity.xp,
                level = catEntity.level,
                foodPoints = catEntity.foodPoints,
                coins = catEntity.coins,
                isSleeping = catEntity.isSleeping,
                sleepEndTime = catEntity.sleepEndTime,
                lastUpdated = catEntity.lastUpdated,
                lastInteractionTime = catEntity.lastInteractionTime
            )
            
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            val today = LocalDate.now().format(formatter)
            val todayStats = dailyStatsDao.getStatsForDateOnce(today)
            val steps = todayStats?.steps ?: 0

            val emoji = when (cat.mood) {
                CatMood.IDLE -> "😸"
                CatMood.HAPPY -> "😻"
                CatMood.HUNGRY -> "😿"
                CatMood.SLEEPING -> "😴"
                CatMood.EXCITED -> "✨"
                else -> "😸"
            }

            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(PatiCatWidget::class.java)

            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[PatiCatWidget.KEY_CAT_NAME] = cat.name
                        this[PatiCatWidget.KEY_CAT_EMOJI] = emoji
                        this[PatiCatWidget.KEY_STEPS] = steps
                        this[PatiCatWidget.KEY_HUNGER] = cat.hunger
                        this[PatiCatWidget.KEY_HAPPINESS] = cat.happiness
                        this[PatiCatWidget.KEY_ENERGY] = cat.energy
                    }
                }
            }
            // Ask Glance to update all instances
            PatiCatWidget().updateAll(context)

            return Result.success()
        } catch (e: Exception) {
            android.util.Log.e("WidgetWorker", "Update failed", e)
            return Result.failure()
        }
    }
}
