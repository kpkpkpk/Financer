package com.financer.core.data.repository

import com.financer.core.data.model.Transaction
import com.financer.core.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

data class CategoryStat(
    val categoryId: Long,
    val count: Long,
    val avgAmount: Long,
    val lastDate: String?
)

interface TransactionRepository {
    fun getAll(): Flow<List<Transaction>>
    fun getByPeriod(from: LocalDateTime, to: LocalDateTime): Flow<List<Transaction>>
    fun getByCategory(categoryId: Long): Flow<List<Transaction>>
    suspend fun getById(id: Long): Transaction?
    suspend fun insert(transaction: Transaction)
    suspend fun update(transaction: Transaction)
    suspend fun deleteById(id: Long)
    fun getSumByType(): Flow<Map<TransactionType, Long>>
    fun getSumByTypeAndPeriod(from: LocalDateTime, to: LocalDateTime): Flow<Map<TransactionType, Long>>
    fun getTotalCount(): Flow<Long>
    fun getCategoryStats(type: TransactionType): Flow<List<CategoryStat>>
}
