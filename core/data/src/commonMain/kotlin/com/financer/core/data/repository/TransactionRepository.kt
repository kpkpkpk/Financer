package com.financer.core.data.repository

import com.financer.core.data.model.Transaction
import com.financer.core.data.model.TransactionType

data class CategoryStat(
    val categoryId: Long,
    val count: Long,
    val avgAmount: Long,
    val lastDate: String?
)

interface TransactionRepository {
    fun getAll(): List<Transaction>
    fun getByPeriod(from: String, to: String): List<Transaction>
    fun getByCategory(categoryId: Long): List<Transaction>
    fun getById(id: Long): Transaction?
    fun insert(transaction: Transaction)
    fun update(transaction: Transaction)
    fun deleteById(id: Long)
    fun getSumByType(): Map<TransactionType, Long>
    fun getSumByTypeAndPeriod(from: String, to: String): Map<TransactionType, Long>
    fun getTotalCount(): Long
    fun getCategoryStats(type: TransactionType): List<CategoryStat>
}
