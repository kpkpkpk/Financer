package com.financer.core.common

import kotlin.math.abs

/**
 * Input data for category scoring: per-category statistics aggregated from transaction history.
 * [avgAmount] is in kopecks.
 */
data class CategoryStatInput(
    val categoryId: Long,
    val transactionCount: Long,
    val avgAmount: Long,
    val daysSinceLastTransaction: Long
)

/**
 * Computes top-N recommended categories based on historical usage patterns.
 *
 * Score formula:
 *   score = 0.3 * frequency + 0.5 * amountProximity + 0.2 * recency
 *
 * @param stats per-category aggregated statistics
 * @param totalTransactionCount total number of transactions across all categories
 * @param currentAmount the amount being entered by the user, in kopecks
 * @param topN number of results to return (default 5)
 * @return list of category IDs sorted by descending score, limited to [topN]
 */
fun getTopCategories(
    stats: List<CategoryStatInput>,
    totalTransactionCount: Long,
    currentAmount: Long,
    topN: Int = 5
): List<Long> {
    if (stats.isEmpty() || totalTransactionCount == 0L) return emptyList()

    return stats
        .map { stat ->
            val frequencyScore = stat.transactionCount.toDouble() / totalTransactionCount
            val amountProximityScore = 1.0 / (1.0 + abs(currentAmount - stat.avgAmount).toDouble())
            val recencyScore = 1.0 / (1.0 + stat.daysSinceLastTransaction.toDouble())

            val score = 0.3 * frequencyScore + 0.5 * amountProximityScore + 0.2 * recencyScore
            stat.categoryId to score
        }
        .sortedByDescending { it.second }
        .take(topN)
        .map { it.first }
}
