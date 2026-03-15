package com.financer.feature.transaction.domain

import com.financer.core.common.CategoryStatInput
import com.financer.core.common.getTopCategories
import com.financer.core.data.model.Category
import com.financer.core.data.model.TransactionType
import com.financer.core.data.repository.CategoryRepository
import com.financer.core.data.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class GetTopCategoriesUseCase(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) {

    suspend operator fun invoke(
        type: TransactionType,
        amount: Long,
    ): List<Category> {
        val categories = categoryRepository.getByType(type)
        if (categories.isEmpty()) return emptyList()
        if (amount <= 0L) return categories.take(5)

        val allowedIds = categories.map(Category::id).toSet()
        val stats = transactionRepository
            .getCategoryStats(type)
            .first()
            .filter { it.categoryId in allowedIds && it.count > 0L }

        if (stats.isEmpty()) return categories.take(5)

        val inputs = stats.map { stat ->
            CategoryStatInput(
                categoryId = stat.categoryId,
                transactionCount = stat.count,
                avgAmount = stat.avgAmount,
                daysSinceLastTransaction = daysSinceLastTransaction(stat.lastDate),
            )
        }

        val totalCount = inputs.sumOf(CategoryStatInput::transactionCount)
        if (totalCount == 0L) return categories.take(5)

        val categoriesById = categories.associateBy(Category::id)
        val topIds = getTopCategories(
            stats = inputs,
            totalTransactionCount = totalCount,
            currentAmount = amount,
        )

        val topCategories = topIds.mapNotNull(categoriesById::get)
        val remaining = categories.filterNot { category -> category.id in topIds }
        return (topCategories + remaining).take(5)
    }

    private fun daysSinceLastTransaction(value: String?): Long {
        if (value.isNullOrBlank()) return Long.MAX_VALUE / 4

        val transactionDate = runCatching {
            LocalDateTime.parse(value).date
        }.recoverCatching {
            LocalDate.parse(value.substringBefore('T'))
        }.getOrNull() ?: return Long.MAX_VALUE / 4

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return transactionDate.daysUntil(today).coerceAtLeast(0).toLong()
    }
}
