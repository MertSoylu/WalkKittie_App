package com.mert.paticat.domain.model

import com.mert.paticat.R

enum class ShopCategory { FOOD, ENERGY, BOOST }

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
    val energyBoost: Int = 0, // +energy when fed
    val emoji: String,
    val category: ShopCategory = ShopCategory.FOOD,
    val isBoost: Boolean = false
) {
    companion object {
        /** Maximum gold a player can hold */
        const val MAX_GOLD = 200
        /** Maximum quantity of a single food item in inventory */
        const val MAX_INVENTORY_PER_ITEM = 5
        /** Maximum gold ads per day */
        const val MAX_GOLD_ADS_PER_DAY = 5
        /** Gold earned per ad */
        const val GOLD_PER_AD = 15

        // Boost IDs
        const val ID_STEP_MULTIPLIER = "step_multiplier"
        const val ID_XP_MULTIPLIER = "xp_multiplier"
        const val ID_COMBO_MULTIPLIER = "combo_multiplier"

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

        val ENERGY_BAR = ShopItem(
            id = "energy_bar",
            nameResId = R.string.shop_item_energy_bar,
            descResId = R.string.shop_item_energy_bar_desc,
            price = 40,
            hungerRestore = 5,
            happinessBoost = 1,
            xpBoost = 1,
            energyBoost = 50,
            emoji = "⚡",
            category = ShopCategory.ENERGY
        )

        val STEP_MULTIPLIER = ShopItem(
            id = ID_STEP_MULTIPLIER,
            nameResId = R.string.shop_item_step_multiplier,
            descResId = R.string.shop_item_step_multiplier_desc,
            price = 100,
            hungerRestore = 0,
            happinessBoost = 0,
            xpBoost = 0,
            emoji = "⚡",
            category = ShopCategory.BOOST,
            isBoost = true
        )

        val XP_MULTIPLIER = ShopItem(
            id = ID_XP_MULTIPLIER,
            nameResId = R.string.shop_item_xp_multiplier,
            descResId = R.string.shop_item_xp_multiplier_desc,
            price = 100,
            hungerRestore = 0,
            happinessBoost = 0,
            xpBoost = 0,
            emoji = "🌟",
            category = ShopCategory.BOOST,
            isBoost = true
        )

        val COMBO_MULTIPLIER = ShopItem(
            id = ID_COMBO_MULTIPLIER,
            nameResId = R.string.shop_item_combo_multiplier,
            descResId = R.string.shop_item_combo_multiplier_desc,
            price = 150,
            hungerRestore = 0,
            happinessBoost = 0,
            xpBoost = 0,
            emoji = "💎",
            category = ShopCategory.BOOST,
            isBoost = true
        )

        /** All available shop items, ordered by price */
        val ALL = listOf(DRY_FOOD, ENERGY_BAR, CANNED_FOOD, TUNA, PREMIUM_FEAST, STEP_MULTIPLIER, XP_MULTIPLIER, COMBO_MULTIPLIER)

        fun getById(id: String): ShopItem? = ALL.find { it.id == id }
        fun byCategory(cat: ShopCategory) = ALL.filter { it.category == cat }
    }
}
