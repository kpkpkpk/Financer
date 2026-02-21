package com.financer.core.data.repository

import com.financer.core.data.db.FinancerDatabase
import com.financer.core.data.model.Category
import com.financer.core.data.model.TransactionType

class CategoryRepositoryImpl(
    private val database: FinancerDatabase
) : CategoryRepository {

    private val queries get() = database.categoryQueries

    override fun getAll(): List<Category> {
        return queries.selectAll().executeAsList().map { it.toDomain() }
    }

    override fun getByType(type: TransactionType): List<Category> {
        return queries.selectByType(type.name).executeAsList().map { it.toDomain() }
    }

    override fun getById(id: Long): Category? {
        return queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override fun insert(category: Category) {
        queries.insert(
            name = category.name,
            emoji = category.emoji,
            type = category.type.name,
            is_default = if (category.isDefault) 1L else 0L
        )
    }

    override fun deleteById(id: Long) {
        queries.deleteById(id)
    }

    override fun isEmpty(): Boolean {
        return queries.count().executeAsOne() == 0L
    }

    private fun com.financer.core.data.db.Category.toDomain(): Category {
        return Category(
            id = id,
            name = name,
            emoji = emoji,
            type = TransactionType.valueOf(type),
            isDefault = is_default == 1L
        )
    }
}
