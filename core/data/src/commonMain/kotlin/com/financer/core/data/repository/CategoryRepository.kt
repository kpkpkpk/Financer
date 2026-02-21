package com.financer.core.data.repository

import com.financer.core.data.model.Category
import com.financer.core.data.model.TransactionType

interface CategoryRepository {
    fun getAll(): List<Category>
    fun getByType(type: TransactionType): List<Category>
    fun getById(id: Long): Category?
    fun insert(category: Category)
    fun deleteById(id: Long)
    fun isEmpty(): Boolean
}
