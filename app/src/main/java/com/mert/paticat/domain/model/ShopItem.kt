package com.mert.paticat.domain.model

import com.mert.paticat.R

/**
 * Represents a food item that can be purchased from the shop.
 * Each item has a gold price and provides different stat boosts when fed to the cat.
 */
data class ShopItem(
    val id: String,
    val nameResId: Int,
    val descResId: Int,
    val price: Int,          // Gold cost
    val hungerRestore: Int,  // +hunger when fed
    val happinessBoost: Int, // +happiness when fed
    val xpBoost: Int,        // +XP when fed
    val emoji: String
) {
    companion object {
        /** Maximum gold a player can hold */
        const val MAX_GOLD = 999
        /** Maximum quantity of a single food item in inventory */
        const val MAX_INVENTORY_PER_ITEM = 5
        /** Maximum gold ads per day */
        const val MAX_GOLD_ADS_PER_DAY = 5
        /** Gold earned per ad */
        const val GOLD_PER_AD = 10

        val DRY_FOOD = ShopItem(
            id = "dry_food",
            nameResId = R.string.shop_item_dry_food,
            descResId = R.string.shop_item_dry_food_desc,
            price = 10,
            hungerRestore = 5,
            happinessBoost = 1,
            xpBoost = 1,
            emoji = "🥫"
        )

        val CANNED_FOOD = ShopItem(
            id = "canned_food",
            nameResId = R.string.shop_item_canned_food,
            descResId = R.string.shop_item_canned_food_desc,
            price = 25,
            hungerRestore = 15,
            happinessBoost = 3,
            xpBoost = 2,
            emoji = "🥘"
        )

        val TUNA = ShopItem(
            id = "tuna",
            nameResId = R.string.shop_item_tuna,
            descResId = R.string.shop_item_tuna_desc,
            price = 40,
            hungerRestore = 25,
            happinessBoost = 5,
            xpBoost = 3,
            emoji = "🐟"
        )

        val PREMIUM_FEAST = ShopItem(
            id = "premium_feast",
            nameResId = R.string.shop_item_premium_feast,
            descResId = R.string.shop_item_premium_feast_desc,
            price = 60,
            hungerRestore = 40,
            happinessBoost = 10,
            xpBoost = 5,
            emoji = "🍗"
        )

        /** All available shop items, ordered by price */
        val ALL = listOf(DRY_FOOD, CANNED_FOOD, TUNA, PREMIUM_FEAST)

        fun getById(id: String): ShopItem? = ALL.find { it.id == id }
    }
}
