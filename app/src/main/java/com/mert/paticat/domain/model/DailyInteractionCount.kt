package com.mert.paticat.domain.model

/**
 * Domain model for daily interaction counts.
 * Replaces [com.mert.paticat.data.local.dao.DailyInteractionCount] in the domain layer
 * to avoid leaking data-layer types through the repository interface.
 */
data class DailyInteractionCount(
    val date: String,
    val type: String,
    val count: Int
)
