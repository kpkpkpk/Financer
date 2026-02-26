package com.financer.core.data.repository

import com.financer.core.data.model.Transaction
import com.financer.core.data.model.TransactionType
import kotlinx.datetime.LocalDateTime

data class CategoryStat(
    val categoryId: Long,
    val count: Long,
    val avgAmount: Long,
    val lastDate: String?
)

interface TransactionRepository {
    suspend fun getAll(): List<Transaction>
    suspend fun getByPeriod(from: LocalDateTime, to: LocalDateTime): List<Transaction>
    suspend fun getByCategory(categoryId: Long): List<Transaction>
    suspend fun getById(id: Long): Transaction?
    suspend fun insert(transaction: Transaction)
    suspend fun update(transaction: Transaction)
    suspend fun deleteById(id: Long)
    suspend fun getSumByType(): Map<TransactionType, Long>
    suspend fun getSumByTypeAndPeriod(from: LocalDateTime, to: LocalDateTime): Map<TransactionType, Long>
    suspend fun getTotalCount(): Long
    suspend fun getCategoryStats(type: TransactionType): List<CategoryStat>
}
