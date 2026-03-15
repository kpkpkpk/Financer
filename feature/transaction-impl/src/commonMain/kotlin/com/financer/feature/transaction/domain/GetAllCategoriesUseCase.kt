package com.financer.feature.transaction.domain

import com.financer.core.data.model.Category
import com.financer.core.data.model.TransactionType
import com.financer.core.data.repository.CategoryRepository

class GetAllCategoriesUseCase(
    private val categoryRepository: CategoryRepository,
) {

    operator fun invoke(type: TransactionType): List<Category> {
        return categoryRepository.getByType(type)
    }
}
