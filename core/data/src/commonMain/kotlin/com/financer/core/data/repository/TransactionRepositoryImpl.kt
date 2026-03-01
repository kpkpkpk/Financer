package com.financer.core.data.repository

import com.financer.core.data.db.FinancerDatabase
import com.financer.core.data.model.Transaction
import com.financer.core.data.model.TransactionType
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime

class TransactionRepositoryImpl(
    private val database: FinancerDatabase
) : TransactionRepository {

    private val queries get() = database.transactionEntityQueries

    override fun getAll(): Flow<List<Transaction>> = queries
        .selectAll()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { entities -> entities.map { it.toDomain() } }

    override fun getByPeriod(from: LocalDateTime, to: LocalDateTime): Flow<List<Transaction>> = queries
        .selectByPeriod(from.toString(), to.toString())
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { entities -> entities.map { it.toDomain() } }

    override fun getByCategory(categoryId: Long): Flow<List<Transaction>> = queries
        .selectByCategory(categoryId)
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: Long): Transaction? = withContext(Dispatchers.IO) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun insert(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            queries.insert(
                type = transaction.type.name,
                amount = transaction.amount,
                category_id = transaction.categoryId,
                date = transaction.date.toString(),
                note = transaction.note
            )
        }
    }

    override suspend fun update(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            queries.update(
                type = transaction.type.name,
                amount = transaction.amount,
                category_id = transaction.categoryId,
                date = transaction.date.toString(),
                note = transaction.note,
                id = transaction.id
            )
        }
    }

    override suspend fun deleteById(id: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteById(id)
        }
    }

    override fun getSumByType(): Flow<Map<TransactionType, Long>> = queries
        .sumByType()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { rows ->
            rows.associate {
                TransactionType.valueOf(it.type) to (it.total ?: 0L)
            }
        }

    override fun getSumByTypeAndPeriod(
        from: LocalDateTime,
        to: LocalDateTime
    ): Flow<Map<TransactionType, Long>> = queries
        .sumByTypeAndPeriod(from.toString(), to.toString())
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { rows ->
            rows.associate {
                TransactionType.valueOf(it.type) to (it.total ?: 0L)
            }
        }

    override fun getTotalCount(): Flow<Long> = queries
        .totalCount()
        .asFlow()
        .mapToOne(Dispatchers.IO)

    override fun getCategoryStats(type: TransactionType): Flow<List<CategoryStat>> {
        val countsFlow = queries.countByCategory().asFlow().mapToList(Dispatchers.IO)
        val avgsFlow = queries.avgAmountByCategory(type.name).asFlow().mapToList(Dispatchers.IO)
        val lastDatesFlow = queries.lastDateByCategory().asFlow().mapToList(Dispatchers.IO)

        return combine(countsFlow, avgsFlow, lastDatesFlow) { counts, avgs, lastDates ->
            val avgMap = avgs.associate {
                it.category_id to (it.avg_amount ?: 0L)
            }
            val lastDateMap = lastDates.associate { it.category_id to it.last_date }

            counts.map { countRow ->
                CategoryStat(
                    categoryId = countRow.category_id,
                    count = countRow.cnt,
                    avgAmount = avgMap[countRow.category_id] ?: 0L,
                    lastDate = lastDateMap[countRow.category_id]
                )
            }
        }
    }

    private fun com.financer.core.data.db.TransactionEntity.toDomain(): Transaction {
        return Transaction(
            id = id,
            type = TransactionType.valueOf(type),
            amount = amount,
            categoryId = category_id,
            date = LocalDateTime.parse(date),
            note = note
        )
    }
}
