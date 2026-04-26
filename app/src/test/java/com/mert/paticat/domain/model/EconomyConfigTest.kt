package com.mert.paticat.domain.model

import org.junit.Assert.assertTrue
import org.junit.Test

class EconomyConfigTest {
    @Test
    fun `gold economy limits are sane`() {
        assertTrue(EconomyConfig.MAX_GOLD > 0)
        assertTrue(EconomyConfig.DAILY_GOLD_AD_LIMIT in 1..10)
        assertTrue(EconomyConfig.GOLD_PER_AD > 0)
        assertTrue(EconomyConfig.MAX_SLEEP_ADS_PER_SLEEP in 1..10)
    }

    @Test
    fun `shop prices increase with progression`() {
        assertTrue(EconomyConfig.ShopPrices.DRY_FOOD < EconomyConfig.ShopPrices.CANNED_FOOD)
        assertTrue(EconomyConfig.ShopPrices.CANNED_FOOD < EconomyConfig.ShopPrices.TUNA)
        assertTrue(EconomyConfig.ShopPrices.TUNA < EconomyConfig.ShopPrices.PREMIUM_FEAST)
        assertTrue(EconomyConfig.ShopPrices.ENERGY_BAR >= EconomyConfig.ShopPrices.DRY_FOOD)
    }
}
