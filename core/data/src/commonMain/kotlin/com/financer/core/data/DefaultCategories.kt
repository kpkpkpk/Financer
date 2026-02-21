package com.financer.core.data

import com.financer.core.data.model.Category
import com.financer.core.data.model.TransactionType
import com.financer.core.data.repository.CategoryRepository

object DefaultCategories {

    private val expenseCategories = listOf(
        Category(name = "Еда", emoji = "🍔", type = TransactionType.EXPENSE),
        Category(name = "Транспорт", emoji = "🚗", type = TransactionType.EXPENSE),
        Category(name = "Жильё", emoji = "🏠", type = TransactionType.EXPENSE),
        Category(name = "Одежда", emoji = "👕", type = TransactionType.EXPENSE),
        Category(name = "Здоровье", emoji = "💊", type = TransactionType.EXPENSE),
        Category(name = "Развлечения", emoji = "🎮", type = TransactionType.EXPENSE),
        Category(name = "Связь", emoji = "📱", type = TransactionType.EXPENSE),
        Category(name = "Образование", emoji = "🎓", type = TransactionType.EXPENSE),
        Category(name = "Путешествия", emoji = "✈️", type = TransactionType.EXPENSE),
        Category(name = "Покупки", emoji = "🛒", type = TransactionType.EXPENSE),
        Category(name = "Красота", emoji = "💇", type = TransactionType.EXPENSE),
        Category(name = "Питомцы", emoji = "🐕", type = TransactionType.EXPENSE),
        Category(name = "Подарки", emoji = "🎁", type = TransactionType.EXPENSE),
        Category(name = "Прочее", emoji = "📦", type = TransactionType.EXPENSE),
    )

    private val incomeCategories = listOf(
        Category(name = "Зарплата", emoji = "💰", type = TransactionType.INCOME),
        Category(name = "Фриланс", emoji = "💵", type = TransactionType.INCOME),
        Category(name = "Инвестиции", emoji = "📈", type = TransactionType.INCOME),
        Category(name = "Подарки", emoji = "🎁", type = TransactionType.INCOME),
        Category(name = "Возврат", emoji = "💸", type = TransactionType.INCOME),
        Category(name = "Прочее", emoji = "📦", type = TransactionType.INCOME),
    )

    fun prepopulateIfNeeded(repository: CategoryRepository) {
        if (repository.isEmpty()) {
            (expenseCategories + incomeCategories).forEach { category ->
                repository.insert(category)
            }
        }
    }
}
