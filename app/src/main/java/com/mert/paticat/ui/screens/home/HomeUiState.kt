package com.mert.paticat.ui.screens.home

import com.mert.paticat.domain.model.Cat
import com.mert.paticat.domain.model.DailyStats
import com.mert.paticat.domain.model.Mission
import java.time.LocalDate

/**
 * UI State for Home Screen
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val cat: Cat = Cat(),
    val todayStats: DailyStats = DailyStats(date = LocalDate.now()),
    val activeMissions: List<Mission> = emptyList(),
    val stepGoal: Int = 10000,
    val waterGoal: Int = 2000,
    val nativeAd: com.google.android.gms.ads.nativead.NativeAd? = null,
    val error: String? = null,
    val lastAddedWater: Int? = null
) {
    val stepProgress: Float
        get() = (todayStats.steps.toFloat() / stepGoal).coerceIn(0f, 1f)
    
    val waterProgress: Float
        get() = (todayStats.waterMl.toFloat() / waterGoal).coerceIn(0f, 1f)
}
