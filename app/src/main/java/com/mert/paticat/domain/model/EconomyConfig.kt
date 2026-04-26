package com.mert.paticat.domain.model

/**
 * Centralized economy parameters for gold earn/spend balancing.
 */
object EconomyConfig {
    const val MAX_GOLD = 500
    const val MAX_INVENTORY_PER_ITEM = 5

    const val STEPS_PER_COIN = 150
    const val STEP_BOOST_MULTIPLIER = 2
    const val XP_BOOST_MULTIPLIER = 2

    const val DAILY_GOLD_AD_LIMIT = 3
    const val GOLD_PER_AD = 8
    const val MAX_SLEEP_ADS_PER_SLEEP = 3

    object ShopPrices {
        const val DRY_FOOD = 12
        const val ENERGY_BAR = 24
        const val CANNED_FOOD = 30
        const val TUNA = 48
        const val PREMIUM_FEAST = 70
        const val BOOST = 120
    }

    object MissionCoinRewards {
        const val STEPS_TIER_1 = 4
        const val STEPS_TIER_2 = 8
        const val STEPS_TIER_3 = 12
        const val STEPS_TIER_4 = 18
        const val GAME_TIER_1 = 4
    }

    object MiniGameRewards {
        const val RPS_WIN_GOLD = 6
        const val RPS_DRAW_GOLD = 3

        const val SLOTS_JACKPOT_GOLD = 18
        const val SLOTS_MATCH_TWO_GOLD = 6

        const val MEMORY_FAST_GOLD = 6
        const val MEMORY_MEDIUM_GOLD = 3
        const val MEMORY_SLOW_GOLD = 1

        const val REFLEX_HIGH_GOLD = 6
        const val REFLEX_MEDIUM_GOLD = 3
        const val REFLEX_LOW_GOLD = 1

        const val CATCH_HIGH_GOLD = 10
        const val CATCH_MEDIUM_GOLD = 6
        const val CATCH_LOW_GOLD = 3
        const val CATCH_MIN_GOLD = 1
    }
}

enum class EconomySource {
    STEP_REWARD,
    STEP_RECONCILIATION,
    MISSION_REWARD,
    GAME_REWARD,
    AD_REWARD,
    SHOP_PURCHASE,
    TUTORIAL_REWARD,
    UNKNOWN
}
