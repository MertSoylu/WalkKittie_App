package com.mert.paticat.domain.model

object GameConstants {
    // Hunger Decay
    // Awake: 100→0 in ~12.5h → feeds 2-3x/day
    // Sleeping: slower so overnight sleep doesn't cause critical hunger by morning
    const val HUNGER_LOSS_PER_HOUR_SLEEPING = 3.0  // was 5.0
    const val HUNGER_LOSS_PER_HOUR_AWAKE = 8.0     // was 7.0

    // Energy (unchanged — kept intentionally slow)
    const val ENERGY_RECOVERY_PER_HOUR_SLEEPING = 25.0
    const val ENERGY_LOSS_PER_HOUR_AWAKE = -2.0

    // Happiness Decay
    // Sleep: good sleep rewards user for putting cat to sleep
    const val HAPPINESS_CHANGE_CRITICAL_HUNGER_SLEEP = -6.0  // was -8.0, less brutal
    const val HAPPINESS_CHANGE_LOW_HUNGER_SLEEP = -3.0       // was -4.0
    const val HAPPINESS_CHANGE_NORMAL_SLEEP = 3.0             // was 2.0, more rewarding

    // Awake: good condition net = -4 + 1 = -3/h → 8h workday drops ~24 pts (noticeable)
    // Normal: -4/h → 8h = -32 pts (clear urgency after half a day)
    // Critical: -4 - 3 = -7/h → rapid decay encourages immediate action
    const val HAPPINESS_LOSS_PER_HOUR_AWAKE = -4.0            // was -3.0
    const val HAPPINESS_GAIN_GOOD_CONDITION = 1.0             // was 2.0
    const val HAPPINESS_PENALTY_CRITICAL_CONDITION = 3.0      // was 2.0, stronger neglect penalty
}
